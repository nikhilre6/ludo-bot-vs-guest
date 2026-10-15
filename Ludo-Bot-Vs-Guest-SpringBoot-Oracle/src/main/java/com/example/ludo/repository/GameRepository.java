package com.example.ludo.repository;

import com.example.ludo.model.Game;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.util.Optional;

@Repository
public class GameRepository {
    private final JdbcTemplate jdbc;
    public GameRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}

    public void save(Game g){
        jdbc.update(
            "INSERT INTO LUDO_GAMES " +
            "(GAME_ID,STATUS,TURN_PLAYER,DICE_VALUE,GUEST_TOKENS,BOT_TOKENS,GUEST_SIXES,BOT_SIXES,MESSAGE) " +
            "VALUES (?,?,?,?,?,?,?,?,?)",
            g.getGameId(),g.getStatus(),g.getTurn(),g.getDice(),
            encode(g.getGuestTokens()),encode(g.getBotTokens()),
            g.getGuestConsecutiveSixes(),g.getBotConsecutiveSixes(),g.getMessage()
        );
    }

    public void update(Game g){
        jdbc.update(
            "UPDATE LUDO_GAMES SET STATUS=?,TURN_PLAYER=?,DICE_VALUE=?,GUEST_TOKENS=?,BOT_TOKENS=?,GUEST_SIXES=?,BOT_SIXES=?,MESSAGE=? WHERE GAME_ID=?",
            g.getStatus(),g.getTurn(),g.getDice(),
            encode(g.getGuestTokens()),encode(g.getBotTokens()),
            g.getGuestConsecutiveSixes(),g.getBotConsecutiveSixes(),g.getMessage(),g.getGameId()
        );
    }

    public Optional<Game> find(String id){
        return jdbc.query("SELECT * FROM LUDO_GAMES WHERE GAME_ID=?",this::map,id)
                   .stream().findFirst();
    }

    private Game map(ResultSet rs,int row)throws java.sql.SQLException{
        Game g=new Game();
        g.setGameId(rs.getString("GAME_ID"));
        g.setStatus(rs.getString("STATUS"));
        g.setTurn(rs.getString("TURN_PLAYER"));
        g.setDice(rs.getInt("DICE_VALUE"));
        g.setGuestTokens(decode(rs.getString("GUEST_TOKENS")));
        g.setBotTokens(decode(rs.getString("BOT_TOKENS")));
        g.setGuestConsecutiveSixes(rs.getInt("GUEST_SIXES"));
        g.setBotConsecutiveSixes(rs.getInt("BOT_SIXES"));
        g.setMessage(rs.getString("MESSAGE"));
        return g;
    }

    private String encode(int[] a){return a[0]+","+a[1]+","+a[2]+","+a[3];}
    private int[] decode(String s){
        String[] p=s.split(",");
        int[] a=new int[4];
        for(int i=0;i<4;i++)a[i]=Integer.parseInt(p[i]);
        return a;
    }
}
