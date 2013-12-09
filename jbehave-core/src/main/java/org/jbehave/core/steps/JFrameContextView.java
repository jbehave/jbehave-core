package org.jbehave.core.steps;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;

import org.apache.commons.lang.StringEscapeUtils;

public class JFrameContextView implements ContextView {

    private JFrame frame;
    private JLabel label;
    private int width;
    private int height;
    private int x;
    private int y;

    /**
     * Creates view frame of default size - (380 x 85)
     */
    public JFrameContextView() {
        sized(380, 85);
        located(0, 0); // origin by default
    }

    /**
     * Builder-style way to set the preferred size for the frame
     * @param width width
     * @param height height
     * @return The SwingContextView
     */
    public JFrameContextView sized(final int width, final int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * Builder-style way to set the preferred location for the frame
     * @param x x position on screen
     * @param y y position on screen
     * @return The SwingContextView
     */
    public JFrameContextView located(final int x, final int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public synchronized void show(String scenario, String step) {
        if (frame == null) {
            initialize();
        }
        label.setText("<html><b>" + StringEscapeUtils.escapeHtml(scenario) + "</b><br/>" + StringEscapeUtils.escapeHtml(step) + "</html>");
    }

    public synchronized void close() {
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
            frame = null;
            label = null;
        }
    }

    private void initialize() {
        frame = new JFrame();
        label = new JLabel();
        frame.setAlwaysOnTop(true);
        frame.setSize(width, height);
        frame.setLocation(x, y);
        frame.setUndecorated(true);
        JPanel panel = new JPanel();
        frame.setContentPane(panel);
        panel.setLayout(new BorderLayout());
        label.setBorder(new EmptyBorder(3,3,3,3));
        panel.add(label, BorderLayout.CENTER);

        MouseInputAdapter mia = new MouseInputAdapter() {
            private Point mousePressedScreenCoords;
            private Point mousePressedCompCoords;
            public void mouseReleased(MouseEvent e) {
                mousePressedScreenCoords = null;
                mousePressedCompCoords = null;
            }
            public void mousePressed(MouseEvent e) {
                mousePressedScreenCoords = e.getLocationOnScreen();
                mousePressedCompCoords = e.getPoint();
            }
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                x = mousePressedScreenCoords.x + (currCoords.x - mousePressedScreenCoords.x) - mousePressedCompCoords.x;
                y = mousePressedScreenCoords.y + (currCoords.y - mousePressedScreenCoords.y) - mousePressedCompCoords.y;
                frame.setLocation(x,y);
            }
        };

        frame.addMouseListener(mia);
        frame.addMouseMotionListener(mia);

        frame.setVisible(true);
    }

}
