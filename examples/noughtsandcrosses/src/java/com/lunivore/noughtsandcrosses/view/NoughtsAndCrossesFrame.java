package com.lunivore.noughtsandcrosses.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.lunivore.noughtsandcrosses.game.Game;

@SuppressWarnings("serial")
public class NoughtsAndCrossesFrame extends JFrame {

    public NoughtsAndCrossesFrame(Game game) {
        setName(ComponentNames.NOUGHTSANDCROSSES);
        
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(new GridPanel(game), BorderLayout.NORTH);
        this.getContentPane().add(new MessageLabel(game), BorderLayout.CENTER);
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }
}
