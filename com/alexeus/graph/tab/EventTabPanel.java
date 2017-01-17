package com.alexeus.graph.tab;

import com.alexeus.logic.constants.MainConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alexeus on 13.01.2017.
 * Вкладка с событиями Вестероса
 */
public class EventTabPanel extends JPanel {

    JScrollPane[] scrollPane = new JScrollPane[MainConstants.NUM_EVENT_DECKS];

    EventScroller[] es = new EventScroller[MainConstants.NUM_EVENT_DECKS];

    EventTabPanel() {
        setLayout(new GridLayout(3, 1));
        for (int i = 1; i <= MainConstants.NUM_EVENT_DECKS; i++) {
            es[i - 1] = new EventScroller(i);
            scrollPane[i - 1] = new JScrollPane(es[i - 1]);
            JScrollBar sb = scrollPane[i - 1].getVerticalScrollBar();
            sb.setUnitIncrement(15);
            add(scrollPane[i - 1]);
        }
    }

    public void displayNewEvents() {
        for (int i = 0; i < MainConstants.NUM_EVENT_DECKS; i++) {
            es[i].updatePreferredSize();
            JScrollBar sb = scrollPane[i].getVerticalScrollBar();
            sb.setValue(0);
        }
    }
}
