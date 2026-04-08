package com.aueb.reversi.dto;

public class NewGameRequest {
    private String playerColor = "B";
    private int level = 1;

    public String getPlayerColor() { return playerColor; }
    public void setPlayerColor(String playerColor) { this.playerColor = playerColor; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
