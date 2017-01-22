package com.alexeus.graph;

import com.alexeus.ai.PrimitivePlayer;
import com.alexeus.control.Controller;
import com.alexeus.control.Settings;
import com.alexeus.logic.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        //printFonts();
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
                Controller.getInstance().startNewGame();
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

    private static void printFonts() {
        String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String font: fonts)
        {
            System.out.println(font);
        }
    }

    private static void testBids() {
        Settings.getInstance().setPassByRegime(true);
        Game game = Game.getInstance();
        game.prepareNewGame();
        game.forceBid();
    }
}
