package com.lunivore.noughtsandcrosses.game;

public interface GameObserver {

    void gameChanged(Game game);

    void gameWon(Game game);
}
