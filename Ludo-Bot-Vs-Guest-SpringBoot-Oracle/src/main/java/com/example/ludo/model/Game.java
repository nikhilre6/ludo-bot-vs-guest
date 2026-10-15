package com.example.ludo.model;

public class Game {
    private String gameId;
    private String status;
    private String turn;
    private int dice;
    private int[] guestTokens = {0,0,0,0};
    private int[] botTokens = {0,0,0,0};
    private int guestConsecutiveSixes;
    private int botConsecutiveSixes;
    private String message;

    public String getGameId(){return gameId;}
    public void setGameId(String v){gameId=v;}
    public String getStatus(){return status;}
    public void setStatus(String v){status=v;}
    public String getTurn(){return turn;}
    public void setTurn(String v){turn=v;}
    public int getDice(){return dice;}
    public void setDice(int v){dice=v;}
    public int[] getGuestTokens(){return guestTokens;}
    public void setGuestTokens(int[] v){guestTokens=v;}
    public int[] getBotTokens(){return botTokens;}
    public void setBotTokens(int[] v){botTokens=v;}
    public int getGuestConsecutiveSixes(){return guestConsecutiveSixes;}
    public void setGuestConsecutiveSixes(int v){guestConsecutiveSixes=v;}
    public int getBotConsecutiveSixes(){return botConsecutiveSixes;}
    public void setBotConsecutiveSixes(int v){botConsecutiveSixes=v;}
    public String getMessage(){return message;}
    public void setMessage(String v){message=v;}
}
