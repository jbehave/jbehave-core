package com.lunivore.gameoflife.view.swing;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.lunivore.gameoflife.domain.GameObserver;
import com.lunivore.gameoflife.domain.Grid;

public class ImageRenderer extends JPanel implements GameObserver {

    private static final long serialVersionUID = 1L;

    private final int width;
    private final int height;
    private final int scale;

    private Grid cells = Grid.NULL;

    // This is here for testing purposes
    private GameObserver piggyBack = GameObserver.NULL;

    public ImageRenderer(int width, int height, int scale) {
        this.width = width;
        this.height = height;
        this.setName("image.renderer");
        Dimension size = new Dimension(width * scale, height * scale);
        this.setPreferredSize(size);
        this.setMaximumSize(size);
        this.setMinimumSize(size);
        this.scale = scale;
    }

    public void gridChanged(Grid grid) {
        this.cells = grid;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ImageRenderer.this.repaint();
                piggyBack.gridChanged(ImageRenderer.this.cells);
            }});
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                Color color = cells.hasLife(column, row) ? Color.BLACK : Color.WHITE;
                g.setColor(color);
                g.fillRect(column * scale, row * scale, scale, scale);
                g.setColor(Color.GRAY);
                g.drawRect(column * scale, row * scale, scale, scale);
            }
        }        
    }
    
    public void setPiggybackListener(GameObserver piggyBack) {
        this.piggyBack = piggyBack;
        piggyBack.gridChanged(cells);
    }
}
