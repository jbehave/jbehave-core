package com.lunivore.noughtsandcrosses.game;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.jbehave.Ensure.ensureThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;



public class GameModelBehaviour {

    @Test
    public void shouldStartEmptyAndTellObserver() {
        GameModel game = new GameModel();
        GameObserver observer = mock(GameObserver.class);
        game.addObserver(observer);
        
        verify(observer).gameChanged(game);
        
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                ensureThat(game.playerAt(column, row), equalTo(Player.NONE));
            }
        }
    }
    
    @Test
    public void shouldPlaceTokenForCurrentPlayerInTheGivenColumnAndRowStartingWithX() {
        GameModel game = new GameModel();
        GameObserver observer = mock(GameObserver.class);
        game.addObserver(observer);
        
        game.playerActsAt(0, 1);
        game.playerActsAt(0, 2);
        
        verify(observer, times(3)).gameChanged(game);
        
        ensureThat(game.playerAt(0, 0), equalTo(Player.NONE));
        ensureThat(game.playerAt(0, 1), equalTo(Player.X));
        ensureThat(game.playerAt(0, 2), equalTo(Player.O));
    }
    
    @Test
    public void shouldNotifyObserverWhenTheCurrentPlayerWins() {
        // Given a game which X is about to win
        GameModel game = new GameModel();
        game.playerActsAt(0, 0);
        game.playerActsAt(1, 0);
        game.playerActsAt(0, 1);
        game.playerActsAt(2, 0);
        

        MyGameObserver observer = new MyGameObserver();
        game.addObserver(observer);
        
        // When X wins the game
        game.playerActsAt(0, 2);
        
        // Then we should see X is the current player and the game is over
        ensureThat(observer.game.currentPlayer(), equalTo(Player.X));
    }
    
    private static class MyGameObserver implements GameObserver {

        private Game game;

        public void gameChanged(Game game) {
        }

        public void gameWon(Game game) {
            this.game = game;
        }
        
    }

}
