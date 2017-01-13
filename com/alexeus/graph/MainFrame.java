package com.alexeus.graph;

import com.alexeus.logic.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by alexeus on 13.01.2017.
 * Мэйнфрейм!
 */
public class MainFrame {

    public static void createGUI() {
        JFrame frame = new JFrame("Board Game of Thrones");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());

        Font font = new Font("Verdana", Font.PLAIN, 11);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setFont(font);

        JMenuItem newGameItemMenu = new JMenuItem("Новая партия");
        newGameItemMenu.setFont(font);
        fileMenu.add(newGameItemMenu);

        JMenuItem openItem = new JMenuItem("Загрузить партию");
        openItem.setFont(font);
        fileMenu.add(openItem);

        JMenuItem exitItem = new JMenuItem("Выйти");
        exitItem.setFont(font);
        fileMenu.add(exitItem);

        newGameItemMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Game game = new Game(true);
                game.startNewGame();
            }
        });

        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        menuBar.add(fileMenu);

        frame.setJMenuBar(menuBar);

        frame.setPreferredSize(new Dimension(1200, 700));
        frame.pack();
        frame.setVisible(true);
    }
}
