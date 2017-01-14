package com.alexeus.control;

import com.alexeus.graph.MapPanel;
import com.alexeus.graph.tab.ChatTabPanel;
import com.alexeus.graph.tab.EventTabPanel;
import com.alexeus.graph.tab.FightTabPanel;
import com.alexeus.graph.tab.HouseTabPanel;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.GamePhase;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static com.alexeus.logic.constants.MainConstants.*;
import static com.alexeus.logic.constants.TextInfo.*;

/**
 * Created by alexeus on 13.01.2017.
 * Класс управляет процессом игры
 */
public class Controller {

    private MapPanel mapPanel;
    private JTextArea log;
    private ChatTabPanel chatTabPanel;
    private EventTabPanel eventTabPanel;
    private FightTabPanel fightTabPanel;
    private HouseTabPanel houseTabPanel;

    public Controller() {
    }

    public void startNewGame() {
        if (mapPanel == null) {
            System.err.println("Ничего не вышло.");
            System.exit(0);
        }
        Game game = new Game(true);
        game.prepareNewGame();

        log.append(NEW_GAME_BEGINS);
        log.append(PLAYERS);
        /*log.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                System.out.println("log removed!!");
                log.repaint();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                System.out.println("log inserted!");
                log.repaint();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                System.out.println("log changed!");
                log.repaint();
            }
        });*/
        mapPanel.setGame(game);
        mapPanel.repaint();

        // Игра престолов началась.
        //setNewGamePhase(GamePhase.planningPhase);
    }

    public void sendComponents(MapPanel mapPanel, JTextArea log, ChatTabPanel chatTabPanel, EventTabPanel eventTabPanel,
            FightTabPanel fightTabPanel, HouseTabPanel houseTabPanel) {
        this.mapPanel = mapPanel;
        this.log = log;
        this.chatTabPanel = chatTabPanel;
        this.eventTabPanel = eventTabPanel;
        this.fightTabPanel = fightTabPanel;
        this.houseTabPanel = houseTabPanel;
    }

}
