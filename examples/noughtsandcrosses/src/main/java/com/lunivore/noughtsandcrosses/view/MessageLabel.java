package com.lunivore.noughtsandcrosses.view;

import java.awt.Dimension;

import javax.swing.JLabel;

import com.lunivore.noughtsandcrosses.game.Game;
import com.lunivore.noughtsandcrosses.game.GameObserver;

@SuppressWarnings("serial")
public class MessageLabel extends JLabel implements GameObserver {

    public MessageLabel(Game game) {
        setName(ComponentNames.MESSAGE);
        setText("X's turn");
        setPreferredSize(new Dimension(150, 50));
        game.addObserver(this);
    }

    public void gameChanged(Game game) {
        setText(game.currentPlayer().asString() + "'s turn");
    }
    
    public void gameWon(Game game) {
        setText(game.currentPlayer().asString() + " wins!");
    }
}
