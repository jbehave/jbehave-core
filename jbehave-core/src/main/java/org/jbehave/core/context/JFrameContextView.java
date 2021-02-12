package org.jbehave.core.context;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;

public class JFrameContextView implements ContextView {

    protected JFrame frame;
    protected JLabel label;
    protected int width;
    protected int height;
    protected int x;
    protected int y;

    /**
     * Creates view frame of default size - (380 x 85)
     */
    public JFrameContextView() {
        sized(380, 85);
        located(0, 0); // origin by default
    }

    /**
     * Builder-style way to set the preferred size for the frame
     *
     * @param width the width
     * @param height height
     * @return The JFrameContextView
     */
    public JFrameContextView sized(final int width, final int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * Builder-style way to set the preferred location for the frame
     *
     * @param x the x position on screen
     * @param y the y position on screen
     * @return The JFrameContextView
     */
    public JFrameContextView located(final int x, final int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public synchronized void show(String story, String scenario, String step) {
        if (frame == null) {
            initialize();
        }
        label.setText(formatText(story, scenario, step));
        try {
            TimeUnit.MILLISECONDS.sleep(pauseInMillis());
        } catch (InterruptedException e) {
            // continue
        }
    }

    protected String formatText(String story, String scenario, String step) {
        return format(labelTemplate(), (story != null ? escapeHtml4(story) : ""), (scenario != null ? escapeHtml4(scenario) : ""), escapeHtml4(step));
    }

    protected String labelTemplate() {
        return "<html><h3>{0}</h3><h4>{1}</h4><p>{2}</p></html>";
    }

    protected long pauseInMillis() {
        return 250;
    }

    @Override
    public synchronized void close() {
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
            frame = null;
            label = null;
        }
    }

    protected void initialize() {
        frame = new JFrame();
        label = new JLabel();
        frame.setAlwaysOnTop(true);
        frame.setSize(width, height);
        frame.setLocation(x, y);
        frame.setUndecorated(true);
        JPanel panel = new JPanel();
        frame.setContentPane(panel);
        panel.setLayout(new BorderLayout());
        label.setBorder(new EmptyBorder(3, 3, 3, 3));
        panel.add(label, BorderLayout.CENTER);

        MouseInputAdapter mia = new MouseInputAdapter() {
            private Point mousePressedScreenCoords;
            private Point mousePressedCompCoords;

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressedScreenCoords = null;
                mousePressedCompCoords = null;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mousePressedScreenCoords = e.getLocationOnScreen();
                mousePressedCompCoords = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                x = mousePressedScreenCoords.x
                        + (currCoords.x - mousePressedScreenCoords.x)
                        - mousePressedCompCoords.x;
                y = mousePressedScreenCoords.y
                        + (currCoords.y - mousePressedScreenCoords.y)
                        - mousePressedCompCoords.y;
                frame.setLocation(x, y);
            }
        };

        frame.addMouseListener(mia);
        frame.addMouseMotionListener(mia);

        frame.setVisible(true);
    }

}
