package com.lunivore.noughtsandcrosses.game;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;



public class GameModelBehaviour {

    @org.junit.Test
    public void shouldStartEmptyAndTellObserver() {
        GameModel game = new GameModel();
        GameObserver observer = mock(GameObserver.class);
        game.addObserver(observer);
        
        verify(observer).gameChanged(game);
        
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                assertThat(game.playerAt(column, row), equalTo(Player.NONE));
            }
        }
    }
    
    @org.junit.Test
    public void shouldPlaceTokenForCurrentPlayerInTheGivenColumnAndRowStartingWithX() {
        GameModel game = new GameModel();
        GameObserver observer = mock(GameObserver.class);
        game.addObserver(observer);
        
        game.playerActsAt(0, 1);
        game.playerActsAt(0, 2);
        
        verify(observer, times(3)).gameChanged(game);
        
        assertThat(game.playerAt(0, 0), equalTo(Player.NONE));
        assertThat(game.playerAt(0, 1), equalTo(Player.X));
        assertThat(game.playerAt(0, 2), equalTo(Player.O));
    }
    
    @org.junit.Test
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
        assertThat(observer.game.currentPlayer(), equalTo(Player.X));
    }
    
    private static class MyGameObserver implements GameObserver {

        private Game game;

        @Override
        public void gameChanged(Game game) {
        }

        @Override
        public void gameWon(Game game) {
            this.game = game;
        }
        
    }

}
