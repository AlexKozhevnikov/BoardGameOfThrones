package com.alexeus;

import com.alexeus.graph.MainPanel;
import com.alexeus.logic.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("Board Game of Thrones");
                frame.setContentPane(new MainPanel());
                addMenu(frame);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    private static void addMenu(JFrame frame) {
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
    }
}
