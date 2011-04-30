package com.lunivore.gameoflife.domain;

public interface GameObserver {

    GameObserver NULL = new GameObserver() {

        public void gridChanged(Grid grid) {}
        
    };

    void gridChanged(Grid grid);

}
