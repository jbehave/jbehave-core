package com.lunivore.noughtsandcrosses.view;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.lunivore.noughtsandcrosses.game.Game;
import com.lunivore.noughtsandcrosses.game.GameObserver;

@SuppressWarnings("serial")
public class GridPanel extends JPanel implements GameObserver {
    public static class OXButton extends JButton {
        private final int row;
        private final int column;

        public OXButton(String string, int row, int column) {
            this.row = row;
            this.column = column;
            setPreferredSize(new Dimension(50, 50));
            setName(string);
        }
    }

    private static final String NL = System.getProperty("line.separator");
    
    private OXButton[][] gridButtons = new OXButton[][] {
            new OXButton[] {new OXButton("a1", 0, 0), new OXButton("a2", 0, 1), new OXButton("a3", 0, 2)},
            new OXButton[] {new OXButton("b1", 1, 0), new OXButton("b2", 1, 1), new OXButton("b3", 1, 2)},
            new OXButton[] {new OXButton("c1", 2, 0), new OXButton("c2", 2, 1), new OXButton("c3", 2, 2)}
    };

    public GridPanel(final Game game) {
        setName(ComponentNames.GRID);
        setLayout(new GridLayout(3, 3));
        
        for (OXButton[] row : gridButtons) {
            for (final OXButton button : row) {
                add(button);
                button.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        game.playerActsAt(button.column, button.row);
                    }
                });
            }
        }
        
        game.addObserver(this);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < gridButtons.length; row++) {
            for (OXButton button : gridButtons[row]) {
                builder.append(text(button));
            }
            if (row < gridButtons.length - 1) { builder.append(NL); }
        }
        return builder.toString();
    }

    private String text(JButton button) {
        return button.getText() == "" ? "." : button.getText();
    }

    public void gameChanged(Game game) {
        for (OXButton[] row : gridButtons) {
            for (OXButton button : row) {
                button.setText(game.playerAt(button.column, button.row).asString());
            }
        }
    }

    public void gameWon(Game game) {
        gameChanged(game);
    }
}
