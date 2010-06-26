package com.lunivore.noughtsandcrosses;

import com.lunivore.noughtsandcrosses.game.GameModel;
import com.lunivore.noughtsandcrosses.view.NoughtsAndCrossesFrame;

public class NoughtsAndCrosses {

    public NoughtsAndCrosses() {
        new NoughtsAndCrossesFrame(new GameModel());
    }
    
    public static void main(String[] args) {
        new NoughtsAndCrosses();
    }
}
