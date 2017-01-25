package com.alexeus.control;

import com.alexeus.GotFrame;
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

    // Игра
    private Game game;

    private Settings settings;

    private long timeFromLastInterrupt;

    private MapPanel mapPanel;

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
        game = Game.getInstance();
        game.prepareNewGame();
        mapPanel = game.getMapPanel();
        timeFromLastInterrupt = System.currentTimeMillis();
        game.setNewGamePhase(GamePhase.planningPhase);
    }

    public Game getGame() {
        return game;
    }

    public void interruption(String text) {
        //mapPanel.repaintActionString(player, text.toString());
        GotFrame.getInstance().setTitle("Игра Престолов. " + game.getTime() + " раунд. " + text);
        try {
            switch (Settings.getInstance().getPlayRegime()) {
                case none:
                    synchronized (Game.getInstance()) {
                        game.wait();
                    }
                    break;
                case timeout:
                    Thread.sleep(timeFromLastInterrupt + settings.getTimeoutMillis() - System.currentTimeMillis());
                    setTimer();
                    break;
                case playEnd:
                    break;
            }
        } catch (InterruptedException ex) {
            System.err.println("Друзья, нечто ужасное случилось! Хватит это терпеть, давайте дебажить!");
            ex.printStackTrace();
        }
    }

    public void setTimer() {
        timeFromLastInterrupt = System.currentTimeMillis();
    }
}
