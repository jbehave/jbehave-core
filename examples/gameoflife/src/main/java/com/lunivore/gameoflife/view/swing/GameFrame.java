package com.lunivore.gameoflife.view.swing;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.lunivore.gameoflife.domain.Game;
import com.lunivore.gameoflife.domain.GameObserver;
import com.lunivore.gameoflife.domain.Grid;


public class GameFrame extends JFrame implements GameObserver {

    private static final int HEIGHT = 20;
    private static final int WIDTH = 40;
    private static final int SCALE = 20;
    private static final long serialVersionUID = 1L;
    private final Game game;
    private JButton nextStep;
    private GameObserver delegateListener = GameObserver.NULL;

    public GameFrame(Game game) {
        this.game = game;
        
        setUpFrame();
        setUpGrid();
        
        createNextStepButton();

        this.pack();
        this.setVisible(true);
    }

    private void setUpFrame() {
        this.setName("game.frame");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
    }

    private void setUpGrid() {
        this.getContentPane().setLayout(new BorderLayout());
        ImageRenderer imageRenderer = new ImageRenderer(WIDTH, HEIGHT, SCALE);
        imageRenderer.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int column = e.getX() / SCALE;
                int row = e.getY() / SCALE;
                game.toggleCellAt(column, row);
            }
        });
        game.setObserver(imageRenderer);
        this.delegateListener = imageRenderer;
        this.getContentPane().add(imageRenderer, BorderLayout.CENTER);
    }

    private void createNextStepButton() {
        nextStep = new JButton("Next Step");
        nextStep.setName("next.step");
        nextStep.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                game.nextGeneration();
            }
        });
        this.add(nextStep, BorderLayout.SOUTH);
    }

    public void gridChanged(Grid grid) {
        delegateListener.gridChanged(grid);
    }
    
    public static void main(String[] args) {
        
        new GameFrame(new Game(40, 30)) ;
    }
}
