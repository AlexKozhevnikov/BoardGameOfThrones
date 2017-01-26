package com.alexeus.control;

import com.alexeus.GotFrame;
import com.alexeus.control.enums.PlayRegimeType;
import com.alexeus.graph.MapPanel;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.GamePhase;

import static com.alexeus.logic.constants.MainConstants.*;

/**
 * Created by alexeus on 13.01.2017.
 * Класс управляет процессом игры
 */
public class Controller {

    private static Controller instance;

    private Game game;

    private Settings settings;

    private long timeFromLastInterrupt;

    private int gameRound;

    private Thread currentGameThread;

    //private MapPanel mapPanel;

    private Controller() {
        settings = Settings.getInstance();
    }

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    public void startNewGame() {
        if (currentGameThread != null) {
            currentGameThread.interrupt();
            settings.setPlayRegime(PlayRegimeType.none);
        }
        currentGameThread = new Thread() {
            @Override public void run() {
                game = Game.getInstance();
                game.prepareNewGame();
                //mapPanel = game.getMapPanel();
                timeFromLastInterrupt = System.currentTimeMillis();
                gameRound = game.getTime();
                game.setNewGamePhase(GamePhase.planningPhase);
            }
        };
        currentGameThread.start();
    }

    public Game getGame() {
        return game;
    }

    public void controlPoint(String text) {
        //mapPanel.repaintActionString(player, text.toString());
        GotFrame.getInstance().setTitle("Игра Престолов. " + game.getTime() + " раунд. " + text);
        waitingCore();
    }

    private void waitingCore() {
        int previousGameTime = gameRound;
        gameRound = game.getTime();
        try {
            switch (settings.getPlayRegime()) {
                case none:
                    synchronized (Game.getInstance()) {
                        game.wait();
                    }
                    break;
                case timeout:
                    long timeToWait = timeFromLastInterrupt + settings.getTimeoutMillis() - System.currentTimeMillis();
                    if (timeToWait > 0) {
                        synchronized (Game.getInstance()) {
                            game.wait(timeToWait);
                        }
                        if (settings.getPlayRegime() != PlayRegimeType.timeout) {
                            waitingCore();
                        }
                    }
                    setTimer();
                    break;
                case nextTurn:
                    if (previousGameTime != gameRound) {
                        synchronized (Game.getInstance()) {
                            game.wait();
                        }
                    }
                    break;
                case playEnd:
                    break;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void setTimer() {
        timeFromLastInterrupt = System.currentTimeMillis();
    }
}
