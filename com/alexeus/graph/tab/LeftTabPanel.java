package com.alexeus.graph.tab;

import com.alexeus.graph.util.ImageLoader;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

import static com.alexeus.graph.constants.Constants.*;
import static com.alexeus.graph.constants.Constants.EVENT;
import static com.alexeus.graph.constants.Constants.TABS;

/**
 * Created by alexeus on 17.01.2017.
 * Левая панель со вкладками
 */
public class LeftTabPanel extends JTabbedPane {

    private ChatTabPanel chatTab;
    private FightTabPanel fightTab;
    private EventTabPanel eventTab;
    private HouseTabPanel houseTab;

    private ImageIcon houseTabIcon, fightTabIcon, eventTabIcon, chatTabIcon;

    public LeftTabPanel() {
        super();
        loadPics();
        eventTab = new EventTabPanel();
        addTab("", eventTabIcon, eventTab, "События Вестероса");
        houseTab = new HouseTabPanel();
        addTab("", houseTabIcon, houseTab, "Карты домов");
        fightTab = new FightTabPanel();
        addTab("", fightTabIcon, fightTab, "Сражение");
        chatTab = new ChatTabPanel();
        addTab("", chatTabIcon, chatTab, "Чат");
        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                System.out.println("Tab: " + getSelectedIndex());
                setNewPreferredSize();
            }
        });
        setNewPreferredSize();
    }

    private void setNewPreferredSize() {
        switch (getSelectedIndex()) {
            // Вкладка "Дома" широкая, ей нужно много места
            case 1:
                setPreferredSize(new Dimension(HOUSE_TAB_WIDTH, TAB_PANEL_HEIGHT));
                break;
            case 2:
                setPreferredSize(new Dimension(FIGHT_TAB_WIDTH, TAB_PANEL_HEIGHT));
                break;
            default:
                setPreferredSize(new Dimension(OTHER_TAB_WIDTH, TAB_PANEL_HEIGHT));
                break;
        }
    }

    private void loadPics() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        houseTabIcon = imageLoader.getIcon(TABS + HOUSE, TAB_ICON_SIZE, TAB_ICON_SIZE);
        fightTabIcon = imageLoader.getIcon(TABS + FIGHT, TAB_ICON_SIZE, TAB_ICON_SIZE);
        chatTabIcon = imageLoader.getIcon(TABS + CHAT, TAB_ICON_SIZE, TAB_ICON_SIZE);
        eventTabIcon = imageLoader.getIcon(TABS + EVENT, TAB_ICON_SIZE, TAB_ICON_SIZE);
    }

    public EventTabPanel getEventTab() {
        return eventTab;
    }

    public FightTabPanel getFightTab() {
        return fightTab;
    }

    public ChatTabPanel getChatTab() {
        return chatTab;
    }

    public HouseTabPanel getHouseTab() {
        return houseTab;
    }
}
