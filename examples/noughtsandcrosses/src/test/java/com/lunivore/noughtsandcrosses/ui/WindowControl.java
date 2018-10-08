package com.lunivore.noughtsandcrosses.ui;

import org.uispec4j.Trigger;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecAdapter;
import org.uispec4j.Window;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;

import com.lunivore.noughtsandcrosses.NoughtsAndCrosses;

public class WindowControl implements UISpecAdapter {

    private Window window;

    public WindowControl() {
        UISpec4J.init();
        reset();
    }

    @Override
    public Window getMainWindow() {
        return window;
    }

    public void reset() {
        window = WindowInterceptor.run(new Trigger() {
            @Override
            public void run() {
                NoughtsAndCrosses.main(new String[0]);
            }
        });
    }

    public void destroy() {
        window.dispose();
    }

    @SuppressWarnings("unchecked")
    public <T> T findComponent(Class<T> type, String name) {
        return (T)window.findUIComponent(ComponentMatchers.innerNameIdentity(name)).getAwtComponent();
    }
}
