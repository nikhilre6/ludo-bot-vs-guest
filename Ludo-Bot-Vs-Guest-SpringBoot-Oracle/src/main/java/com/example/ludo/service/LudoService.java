package com.example.ludo.service;

import com.example.ludo.model.Game;
import com.example.ludo.repository.GameRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LudoService {
    private static final int FINISH=57;
    private static final Set<Integer> SAFE=Set.of(1,9,14,22,27,35,40,48,53);
    private final GameRepository repo;
    private final Random random=new Random();

    public LudoService(GameRepository repo){this.repo=repo;}

    public Game newGame(){
        Game g=new Game();
        g.setGameId(UUID.randomUUID().toString());
        g.setStatus("PLAYING");
        g.setTurn("GUEST");
        g.setDice(0);
        g.setMessage("Your turn. Roll the dice.");
        repo.save(g);
        return g;
    }

    public Game getGame(String id){
        return repo.find(id).orElseThrow(()->new IllegalArgumentException("Game not found"));
    }

    public Game roll(String id){
        Game g=getGame(id);
        if(!"PLAYING".equals(g.getStatus()))return g;
        if(!"GUEST".equals(g.getTurn()))throw new IllegalStateException("It is not the guest turn.");

        int dice=rollDice();
        g.setDice(dice);

        if(dice==6){
            g.setGuestConsecutiveSixes(g.getGuestConsecutiveSixes()+1);
            if(g.getGuestConsecutiveSixes()==3){
                Arrays.fill(g.getGuestTokens(),0);
                g.setGuestConsecutiveSixes(0);
                g.setTurn("BOT");
                g.setMessage("Three sixes! Your tokens return HOME. Bot turn.");
                repo.update(g);
                return botTurn(g);
            }
        }else g.setGuestConsecutiveSixes(0);

        if(movable(g.getGuestTokens(),dice).isEmpty()){
            if(dice==6){
                g.setTurn("GUEST");
                g.setMessage("No token can move. Roll again.");
                repo.update(g);
                return g;
            }
            g.setTurn("BOT");
            g.setMessage("No move available. Bot is playing.");
            repo.update(g);
            return botTurn(g);
        }

        g.setMessage("Dice: "+dice+". Select a token.");
        repo.update(g);
        return g;
    }

    public Game moveGuest(String id,int index){
        Game g=getGame(id);
        if(!"PLAYING".equals(g.getStatus()))return g;
        if(!"GUEST".equals(g.getTurn()))throw new IllegalStateException("Not your turn.");
        if(index<0||index>3)throw new IllegalArgumentException("Token index must be 0-3.");

        int dice=g.getDice();
        if(!canMove(g.getGuestTokens()[index],dice))
            throw new IllegalArgumentException("This token cannot move.");

        int pos=next(g.getGuestTokens()[index],dice);
        g.getGuestTokens()[index]=pos;
        capture(g,true,pos);

        if(won(g.getGuestTokens())){
            g.setStatus("GUEST_WON");
            g.setMessage("Congratulations! You won.");
            repo.update(g);
            return g;
        }

        if(dice==6||pos==FINISH){
            g.setTurn("GUEST");
            g.setMessage("Good move! Roll again.");
            repo.update(g);
            return g;
        }

        g.setTurn("BOT");
        g.setMessage("Bot is thinking...");
        repo.update(g);
        return botTurn(g);
    }

    private Game botTurn(Game g){
        if(!"PLAYING".equals(g.getStatus()))return g;

        int dice=rollDice();
        g.setDice(dice);

        if(dice==6){
            g.setBotConsecutiveSixes(g.getBotConsecutiveSixes()+1);
            if(g.getBotConsecutiveSixes()==3){
                Arrays.fill(g.getBotTokens(),0);
                g.setBotConsecutiveSixes(0);
                g.setTurn("GUEST");
                g.setMessage("Bot rolled three sixes and lost its progress. Your turn.");
                repo.update(g);
                return g;
            }
        }else g.setBotConsecutiveSixes(0);

        List<Integer> moves=movable(g.getBotTokens(),dice);
        if(moves.isEmpty()){
            if(dice==6){
                repo.update(g);
                return botTurn(g);
            }
            g.setTurn("GUEST");
            g.setMessage("Bot cannot move. Your turn.");
            repo.update(g);
            return g;
        }

        int index=chooseBot(g,dice,moves);
        int pos=next(g.getBotTokens()[index],dice);
        g.getBotTokens()[index]=pos;
        capture(g,false,pos);

        if(won(g.getBotTokens())){
            g.setStatus("BOT_WON");
            g.setMessage("Bot won this game.");
            repo.update(g);
            return g;
        }

        if(dice==6||pos==FINISH){
            repo.update(g);
            return botTurn(g);
        }

        g.setTurn("GUEST");
        g.setMessage("Bot moved token "+(index+1)+". Your turn.");
        repo.update(g);
        return g;
    }

    private int chooseBot(Game g,int dice,List<Integer> moves){
        for(int i:moves){
            int target=next(g.getBotTokens()[i],dice);
            if(target>0&&target<FINISH&&!SAFE.contains(target))
                for(int p:g.getGuestTokens())if(p==target)return i;
        }
        for(int i:moves)if(next(g.getBotTokens()[i],dice)==FINISH)return i;
        return moves.stream().max(Comparator.comparingInt(i->g.getBotTokens()[i])).orElse(moves.get(0));
    }

    private int rollDice(){return random.nextInt(6)+1;}

    private List<Integer> movable(int[] a,int dice){
        List<Integer> r=new ArrayList<>();
        for(int i=0;i<4;i++)if(canMove(a[i],dice))r.add(i);
        return r;
    }

    private boolean canMove(int pos,int dice){
        if(pos==0)return dice==6;
        return pos+dice<=FINISH;
    }

    private int next(int pos,int dice){
        if(pos==0)return 1;
        return Math.min(FINISH,pos+dice);
    }

    private void capture(Game g,boolean guest,int position){
        if(position<=0||position>=FINISH||SAFE.contains(position))return;
        int[] defender=guest?g.getBotTokens():g.getGuestTokens();
        for(int i=0;i<4;i++)if(defender[i]==position)defender[i]=0;
    }

    private boolean won(int[] a){
        for(int p:a)if(p!=FINISH)return false;
        return true;
    }
}
