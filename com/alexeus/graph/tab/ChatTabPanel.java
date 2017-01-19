package com.alexeus.graph.tab;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static com.alexeus.graph.constants.Constants.OTHER_TAB_WIDTH;
import static com.alexeus.graph.constants.Constants.TAB_PANEL_HEIGHT;

/**
 * Created by alexeus on 13.01.2017.
 * Закладка с чатом для игроков
 */
public class ChatTabPanel extends JPanel {

    private JTextArea chat;

    ChatTabPanel() {
        setLayout(new GridLayout());
        chat = new JTextArea();
        chat.setEditable(false);
        JScrollPane sp = new JScrollPane(chat);
        sp.setPreferredSize(new Dimension(OTHER_TAB_WIDTH - 10, TAB_PANEL_HEIGHT - 10));
        /*chat.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                System.out.println("Раз!");
                sp.getVerticalScrollBar().setValue(sp.getHorizontalScrollBar().getMaximum());
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                System.out.println("Раз!");
                sp.getVerticalScrollBar().setValue(sp.getHorizontalScrollBar().getMaximum());
            }
        });*/
        add(sp);
    }

    public JTextArea getChat() {
        return chat;
    }
}
