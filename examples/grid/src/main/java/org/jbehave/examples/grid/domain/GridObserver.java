package org.jbehave.examples.grid.domain;

public interface GridObserver {

    GridObserver NULL = new GridObserver() {

        @Override
        public void gridChanged(Grid grid) {}
        
    };

    void gridChanged(Grid grid);

}
