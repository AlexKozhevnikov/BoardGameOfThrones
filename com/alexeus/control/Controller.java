package com.alexeus.control;

import com.alexeus.GotFrame;
import com.alexeus.control.enums.GameStatus;
import com.alexeus.control.enums.PlayRegimeType;
import com.alexeus.graph.EndGamePanel;
import com.alexeus.logic.Game;

import javax.swing.*;

import static com.alexeus.logic.constants.MainConstants.LAST_TURN;

/**
 * Created by alexeus on 13.01.2017.
 * Класс управляет процессом игры
 */
public class Controller {

    private static Controller instance;

    private Game game;

    private Settings settings;

    private long timeFromLastInterrupt;

    private volatile int time, previousGameTime;

    private static final Object gameMonitor = new Object();

    private static final Object controllerMonitor = new Object();

    private volatile GameStatus gameStatus;

    private Controller() {
        settings = Settings.getInstance();
        game = Game.getInstance();
    }

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    public void startController() {
        Thread.currentThread().setName("Controller Thread");
        while(true) {
            Thread t = new Thread("Game Thread") {
                @Override
                public void run() {
                    time = 1;
                    game.prepareNewGame();
                    previousGameTime = 0;
                    GotFrame.getInstance().setTitle("Игра Престолов");
                    timeFromLastInterrupt = System.currentTimeMillis();
                    for (; time <= LAST_TURN && gameStatus == GameStatus.running; time++) {
                        if (time > 1) {
                            game.playEvents();
                        }
                        if (gameStatus != GameStatus.running) {
                            break;
                        }

                        game.getPlans();
                        if (gameStatus != GameStatus.running) {
                            break;
                        }

                        game.getRavenDecision();
                        if (gameStatus != GameStatus.running) {
                            break;
                        }

                        game.playRaids();
                        if (gameStatus != GameStatus.running) {
                            break;
                        }

                        game.playMarches();
                        if (gameStatus != GameStatus.running) {
                            break;
                        }

                        game.playConsolidatePower();
                    }
                    if (gameStatus == GameStatus.running) {
                        time = LAST_TURN;
                        Game.getInstance().getModel().fillNumFortress();
                        gameStatus = GameStatus.end;
                        synchronized (controllerMonitor) {
                            controllerMonitor.notify();
                        }
                    }
                }
            };
            gameStatus = GameStatus.running;
            t.start();
            while (gameStatus == GameStatus.running) {
                synchronized (controllerMonitor) {
                    try {
                        controllerMonitor.wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            t.interrupt();
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            while (gameStatus == GameStatus.end) {
                JOptionPane.showConfirmDialog(GotFrame.getInstance(), new EndGamePanel(), "Игра закончена.",
                        JOptionPane.CLOSED_OPTION, JOptionPane.PLAIN_MESSAGE);
                synchronized (controllerMonitor) {
                    try {
                        controllerMonitor.wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            if (gameStatus == GameStatus.interrupted) {
                gameStatus = GameStatus.start;
            }
            Settings.getInstance().setPlayRegime(PlayRegimeType.none);
        }
    }

    /**
     * Метод для обработки паузы во время игры
     * @param text текст, который надлежит показать в окне игры
     */
    public void pause(String text) {
        if (gameStatus != GameStatus.running) return;
        GotFrame.getInstance().setTitle("Игра Престолов. " + time + " раунд. " + text);
        try {
            switch (settings.getPlayRegime()) {
                case none:
                    synchronized (gameMonitor) {
                        gameMonitor.wait();
                    }
                    break;
                case timeout:
                    long timeToWait = timeFromLastInterrupt + settings.getTimeoutMillis() - System.currentTimeMillis();
                    if (timeToWait > 0) {
                        synchronized (gameMonitor) {
                            gameMonitor.wait(timeToWait);
                        }
                        if (settings.getPlayRegime() != PlayRegimeType.timeout) {
                            pause(text);
                        }
                    }
                    setTimer();
                    break;
                case nextTurn:
                    if (previousGameTime != time) {
                        synchronized (gameMonitor) {
                            gameMonitor.wait();
                        }
                    }
                    break;
                case playEnd:
                    break;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        previousGameTime = time;
    }

    public Game getGame() {
        return game;
    }

    public int getTime() {
        return time;
    }

    public void setTimer() {
        timeFromLastInterrupt = System.currentTimeMillis();
    }

    public static Object getGameMonitor() {
        return gameMonitor;
    }

    public static Object getControllerMonitor() {
        return controllerMonitor;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }
}
