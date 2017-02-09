package com.alexeus.graph;

import com.alexeus.GotFrame;
import com.alexeus.ai.GotPlayerInterface;
import com.alexeus.ai.PrimitivePlayer;
import com.alexeus.control.Controller;
import com.alexeus.control.enums.GameStatus;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.InitialPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static com.alexeus.graph.constants.Constants.HOUSE_COLOR;
import static com.alexeus.logic.constants.MainConstants.HOUSE;
import static com.alexeus.logic.constants.MainConstants.NUM_PLAYER;

public class Main {

    public static void main(String[] args) {
        //printFonts();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = GotFrame.getInstance();
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

        /*JMenuItem openItem = new JMenuItem("Загрузить партию");
        openItem.setFont(font);
        fileMenu.add(openItem);*/

        JMenuItem exitItem = new JMenuItem("Выйти");
        exitItem.setFont(font);
        fileMenu.add(exitItem);

        newGameItemMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPanel newGamePanel = new JPanel();
                newGamePanel.setLayout(new BorderLayout());
                JPanel playersPanel = new JPanel();
                playersPanel.setLayout(new GridBagLayout());
                String[] playerOptions = {"Примитивный игрок"};
                InitialPosition[] initialPositionOptions = InitialPosition.values();
                JLabel label;
                JComboBox<String> comboBox;
                ArrayList<JComboBox<String>> playerCombo = new ArrayList<>();
                GridBagConstraints c = new GridBagConstraints();
                c.insets = new Insets(5, 5, 5, 5);
                for (int player = 0; player < NUM_PLAYER; player++) {
                    c.gridy = player;
                    label = new JLabel(HOUSE[player]);
                    label.setBackground(HOUSE_COLOR[player]);
                    c.gridx = 0;
                    playersPanel.add(label, c);
                    comboBox = new JComboBox<>(playerOptions);
                    playerCombo.add(comboBox);
                    c.gridx = 1;
                    playersPanel.add(comboBox, c);
                }

                JPanel settingsPanel = new JPanel();
                settingsPanel.setLayout(new GridBagLayout());
                c = new GridBagConstraints();
                c.insets = new Insets(0, 10, 0, 10);
                label = new JLabel("Начальные позиции");
                settingsPanel.add(label, c);
                JComboBox<InitialPosition> iniCombo = new JComboBox<>(initialPositionOptions);
                iniCombo.setSelectedItem((InitialPosition) Game.getInstance().getInitialPosition());
                c.gridx = 1;
                settingsPanel.add(iniCombo, c);

                newGamePanel.add(playersPanel, BorderLayout.LINE_START);
                newGamePanel.add(settingsPanel, BorderLayout.LINE_END);
                int option = JOptionPane.showConfirmDialog(GotFrame.getInstance(), newGamePanel,
                        "Новая игра", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (option == JOptionPane.OK_OPTION) {
                    GotPlayerInterface[] playerInterfaces = new GotPlayerInterface[NUM_PLAYER];
                    for (int player = 0; player < NUM_PLAYER; player++) {
                        switch(playerCombo.get(player).getSelectedIndex()) {
                            case 0:
                                playerInterfaces[player] = new PrimitivePlayer(player);
                                break;
                        }
                    }
                    Game.getInstance().setInitialPosition((InitialPosition) iniCombo.getSelectedItem());
                    Game.getInstance().setPlayerInterfaces(playerInterfaces);
                    Controller controller = Controller.getInstance();
                    if (controller.getGameStatus() == GameStatus.running) {
                        controller.setGameStatus(GameStatus.interrupted);
                    } else if (controller.getGameStatus() == GameStatus.end) {
                        controller.setGameStatus(GameStatus.start);
                    }
                    synchronized (Controller.getControllerMonitor()) {
                        Controller.getControllerMonitor().notify();
                    }
                }
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
}
