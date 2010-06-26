package com.lunivore.noughtsandcrosses.game;


public interface Game {

    public void addObserver(GameObserver observer);

    public void playerActsAt(int row, int column);

    public Player playerAt(int column, int row);

    public Player currentPlayer();
}
