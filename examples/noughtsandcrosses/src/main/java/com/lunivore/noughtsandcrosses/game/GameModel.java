package com.lunivore.noughtsandcrosses.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameModel implements Game {

    private Map<Coord, Player> map = new HashMap<>();
    private Player currentPlayer = Player.X;
    private List<GameObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(GameObserver observer) {
        observers.add(observer);
        observer.gameChanged(this);
    }

    @Override
    public void playerActsAt(int column, int row) {
        map.put(new Coord(column, row), currentPlayer);
        if (gameWon()) {
            notifyObserversGameWon();
        } else {
            nextPlayer();
            notifyObserversGameChanged();
        }
    }

    private void notifyObserversGameWon() {
        for (GameObserver observer : observers) {
            observer.gameWon(this);
        }
    }

    private boolean gameWon() {
        for (WinningScenario scenario : WinningScenario.values()) {
            if(scenario.isAchievedIn(map)) {
                return true;
            }
        }
        return false;
    }

    private void notifyObserversGameChanged() {
        for (GameObserver observer : observers) {
            observer.gameChanged(this);
        }
    }

    private void nextPlayer() {
        currentPlayer = currentPlayer == Player.X ? Player.O : Player.X;
    }

    @Override
    public Player playerAt(int column, int row) {
        Player player = map.get(new Coord(column, row));
        return player == null ? Player.NONE : player;
    }

    @Override
    public Player currentPlayer() {
        return currentPlayer;
    }
}
