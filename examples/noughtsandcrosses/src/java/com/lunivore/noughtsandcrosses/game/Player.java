package com.lunivore.noughtsandcrosses.game;

public enum Player {

    O("O"), X("X"), NONE("");
    
    private final String rendered;

    private Player(String rendered) {
        this.rendered = rendered;
    }

    public String asString() {
        return rendered;
    }
}
