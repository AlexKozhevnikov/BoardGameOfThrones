package com.alexeus.control;

import com.alexeus.GotFrame;
import com.alexeus.control.enums.GameStatus;
import com.alexeus.control.enums.PlayRegimeType;
import com.alexeus.graph.EndGamePanel;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.GamePhase;

import javax.swing.*;

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

    private String gameStateText;

    private static final Object gameStateMonitor = new Object();

    private static final Object controllerMonitor = new Object();

    private volatile GameStatus gameStatus = GameStatus.none;

    private volatile boolean isGameRunning;

    private static int gameNumber = 0;

    private Controller() {
        settings = Settings.getInstance();
    }

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    public void startController() {
        Thread.currentThread().setName("Controller Thread");
        while (true) {
            Thread currentGameThread = new Thread("Game thread " + ++gameNumber) {
                @Override
                public void run() {
                    game = Game.getInstance();
                    game.prepareNewGame();
                    //mapPanel = game.getMapPanel();
                    timeFromLastInterrupt = System.currentTimeMillis();
                    gameRound = game.getTime();
                    game.setNewGamePhase(GamePhase.planningPhase);
                }
            };
            gameStatus = GameStatus.running;
            GotFrame.getInstance().setTitle("Игра Престолов");
            currentGameThread.start();
            // Даём возможность игре установить начальные значения
            synchronized (gameStateMonitor) {
                try {
                    gameStateMonitor.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            isGameRunning = true;

            // Основной цикл контроллера во время игры
            while (gameStatus == GameStatus.running) {
                GotFrame.getInstance().setTitle("Игра Престолов. " + game.getTime() + " раунд. " + gameStateText);
                int previousGameTime = gameRound;
                gameRound = game.getTime();
                try {
                    switch (settings.getPlayRegime()) {
                        case none:
                            synchronized (controllerMonitor) {
                                controllerMonitor.wait();
                            }
                            break;
                        case timeout:
                            long timeToWait = timeFromLastInterrupt + settings.getTimeoutMillis() - System.currentTimeMillis();
                            if (timeToWait > 0) {
                                synchronized (controllerMonitor) {
                                    controllerMonitor.wait(timeToWait);
                                }
                                if (settings.getPlayRegime() != PlayRegimeType.timeout) {
                                    continue;
                                }
                            }
                            setTimer();
                            break;
                        case nextTurn:
                            if (previousGameTime != gameRound) {
                                synchronized (controllerMonitor) {
                                    controllerMonitor.wait();
                                }
                            }
                            break;
                        case playEnd:
                            break;
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                Game.wakeGame();
                synchronized (gameStateMonitor) {
                    try {
                        gameStateMonitor.notify();
                        gameStateMonitor.wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            currentGameThread.interrupt();
            if (gameStatus == GameStatus.none) {
                JOptionPane.showConfirmDialog(GotFrame.getInstance(), new EndGamePanel(), "Игра закончена.",
                        JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE);
            }
            isGameRunning = false;
            synchronized (controllerMonitor) {
                if (gameStatus == GameStatus.interrupted) {
                    controllerMonitor.notify();
                }
                while (!isGameRunning) {
                    try {
                        Thread.sleep(20);
                        controllerMonitor.wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public Game getGame() {
        return game;
    }

    public void setTimer() {
        timeFromLastInterrupt = System.currentTimeMillis();
    }

    public static Object getMonitor() {
        return gameStateMonitor;
    }

    public static Object getControllerMonitor() {
        return controllerMonitor;
    }

    public void setGameRunning() {
        isGameRunning = true;
    }

    public boolean getGameRunning() {
        return isGameRunning;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public void setText(String text) {
        gameStateText = text;
    }
}
