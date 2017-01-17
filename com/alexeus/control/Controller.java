package com.alexeus.control;

import com.alexeus.graph.MapPanel;
import com.alexeus.graph.tab.*;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.Deck1Cards;
import com.alexeus.logic.enums.Deck2Cards;
import com.alexeus.logic.enums.Deck3Cards;
import com.alexeus.logic.enums.GamePhase;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.util.LinkedList;

import static com.alexeus.logic.constants.MainConstants.*;
import static com.alexeus.logic.constants.TextInfo.*;

/**
 * Created by alexeus on 13.01.2017.
 * Класс управляет процессом игры
 */
public class Controller {

    private static Controller instance;

    // Элементы графического интерфейса
    private MapPanel mapPanel;
    private JTextArea chat;
    private ChatTabPanel chatTabPanel;
    private EventTabPanel eventTabPanel;
    private FightTabPanel fightTabPanel;
    private HouseTabPanel houseTabPanel;

    // Игра
    private Game game;

    private Controller() {
    }

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    public void startNewGame() {
        if (mapPanel == null) {
            System.err.println("Контроллеру не дали грибов, и он обиделся!");
            System.exit(0);
        }
        game = Game.getInstance();
        game.prepareNewGame();
        eventTabPanel.displayNewEvents();

        //log.append(NEW_GAME_BEGINS);
        //log.append(PLAYERS);
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
        //mapPanel.repaint();

        // Игра престолов началась.
        //setNewGamePhase(GamePhase.planningPhase);
    }

    public void receiveComponents(MapPanel mapPanel, LeftTabPanel tabPanel) {
        this.mapPanel = mapPanel;
        this.chatTabPanel = tabPanel.getChatTab();
        this.eventTabPanel = tabPanel.getEventTab();
        this.fightTabPanel = tabPanel.getFightTab();
        this.houseTabPanel = tabPanel.getHouseTab();
        chat = null;
    }

    public Game getGame() {
        return game;
    }
}
