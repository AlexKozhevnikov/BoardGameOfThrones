package com.alexeus.graph;

import com.alexeus.control.Controller;
import com.alexeus.control.enums.PlayRegimeType;
import com.alexeus.control.Settings;
import com.alexeus.graph.tab.*;
import com.alexeus.graph.util.ImageLoader;
import com.alexeus.logic.Game;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

import static com.alexeus.graph.constants.Constants.*;

/**
 * Created by alexeus on 13.01.2017.
 * Основная панель графического интерфейса
 */
public class MainPanel extends JPanel {

    private MapPanel mapPanel;
    private ImageIcon playIcon, playEndIcon, nextIcon, pauseIcon, nextTurnIcon;
    //private ImageIcon collapseIcon, returnIcon;
    //private JButton leftPanelCollapser;

    public MainPanel() {
        setLayout(new BorderLayout());
        loadPics();
        /*leftPanelCollapser = new JButton("", collapseIcon);
        leftPanelCollapser.setPreferredSize(new Dimension(COLLAPSE_ICON_SIZE, COLLAPSE_ICON_SIZE));
        leftPanelCollapser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isPanelVisible = leftPanel.isVisible();
                leftPanel.setVisible(!isPanelVisible);
                leftPanelCollapser.setIcon(isPanelVisible ? returnIcon : collapseIcon);
            }
        });*/
        // Отрисовка карты
        mapPanel = new MapPanel();
        mapPanel.setAutoscrolls(true);
        JScrollPane scrollPane = new JScrollPane(mapPanel);
        scrollPane.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private Point origin;
            @Override
            public void mousePressed(MouseEvent e) {
                origin = new Point(e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin != null) {
                    JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, mapPanel);
                    if (viewPort != null) {
                        int deltaX = origin.x - e.getX();
                        int deltaY = origin.y - e.getY();

                        Rectangle view = viewPort.getViewRect();
                        view.x += deltaX;
                        view.y += deltaY;

                        mapPanel.scrollRectToVisible(view);
                    }
                }
            }
        };
        mapPanel.addMouseListener(mouseAdapter);
        mapPanel.addMouseMotionListener(mouseAdapter);
        mapPanel.addMouseWheelListener(new MouseWheelListener() {
             @Override
             public void mouseWheelMoved(MouseWheelEvent e) {
                 mapPanel.updatePreferredSize(e.getWheelRotation(), e.getPoint());
             }}
        );
        add(scrollPane, BorderLayout.CENTER);
        // отрисовка элементов левой панели
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        LeftTabPanel tabbedPane = new LeftTabPanel();
        /*tabbedPane.addChangeListener(new ChangeListener() {
            // TODO Не работает! Подстраивает размер под предыдущую выбранную вкладку, а не под новую. Почему?..
            @Override
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof JTabbedPane) {
                    mapPanel.adjustIndent();
                }
            }
        });*/
        leftPanel.add(tabbedPane, BorderLayout.PAGE_END);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton nextButton = new JButton("", nextIcon);
        nextButton.setPreferredSize(new Dimension(BUTTON_ICON_SIZE, BUTTON_ICON_SIZE));
        JButton playButton = new JButton("", playIcon);
        playButton.setPreferredSize(new Dimension(BUTTON_ICON_SIZE, BUTTON_ICON_SIZE));
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlayRegimeType previousPlayRegime = Settings.getInstance().getPlayRegime();
                playButton.setIcon(previousPlayRegime == PlayRegimeType.timeout ? playIcon: pauseIcon);
                Settings.getInstance().setPlayRegime(previousPlayRegime == PlayRegimeType.timeout ?
                        PlayRegimeType.none : PlayRegimeType.timeout);
                // Если раньше режим был выключен, то теперь мы его ВКЛЮЧИЛИ, и должны прервать ожидание.
                if (previousPlayRegime == PlayRegimeType.none) {
                    synchronized (Controller.getGameMonitor()) {
                        Controller.getInstance().setTimer();
                        Controller.getGameMonitor().notify();
                    }
                }
            }
        });
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playButton.setIcon(playIcon);
                Settings.getInstance().setPlayRegime(PlayRegimeType.none);
                synchronized (Controller.getGameMonitor()) {
                    Controller.getGameMonitor().notify();
                }
            }
        });
        JButton nexTurnButton = new JButton("", nextTurnIcon);
        nexTurnButton.setPreferredSize(new Dimension(BUTTON_ICON_SIZE, BUTTON_ICON_SIZE));
        nexTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playButton.setIcon(playIcon);
                Settings.getInstance().setPlayRegime(PlayRegimeType.nextTurn);
                synchronized (Controller.getGameMonitor()) {
                    Controller.getGameMonitor().notify();
                }
            }
        });
        JButton playEndButton = new JButton("", playEndIcon);
        playEndButton.setPreferredSize(new Dimension(BUTTON_ICON_SIZE, BUTTON_ICON_SIZE));
        playEndButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playButton.setIcon(playIcon);
                Settings.getInstance().setPlayRegime(PlayRegimeType.playEnd);
                synchronized (Controller.getGameMonitor()) {
                    Controller.getGameMonitor().notify();
                }
            }
        });
        buttonsPanel.add(nextButton);
        buttonsPanel.add(playButton);
        buttonsPanel.add(nexTurnButton);
        buttonsPanel.add(playEndButton);
        buttonsPanel.setPreferredSize(new Dimension(BUTTON_PANEL_WIDTH, BUTTON_PANEL_HEIGHT));
        leftPanel.add(buttonsPanel, BorderLayout.PAGE_START);
        add(leftPanel, BorderLayout.LINE_END);
        Game.getInstance().receiveComponents(mapPanel, tabbedPane);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                    repaint();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                Controller.getInstance().startController();
            }
        };
        t.start();
    }

    private void loadPics() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        playIcon = imageLoader.getIcon(PLAY, TAB_ICON_SIZE, TAB_ICON_SIZE);
        playEndIcon = imageLoader.getIcon(PLAY_END, TAB_ICON_SIZE, TAB_ICON_SIZE);
        nextIcon = imageLoader.getIcon(NEXT, TAB_ICON_SIZE, TAB_ICON_SIZE);
        pauseIcon = imageLoader.getIcon(PAUSE, TAB_ICON_SIZE, TAB_ICON_SIZE);
        nextTurnIcon = imageLoader.getIcon(NEXT_TURN, TAB_ICON_SIZE, TAB_ICON_SIZE);
        //collapseIcon = imageLoader.getIcon(TABS + COLLAPSE, TAB_ICON_SIZE, TAB_ICON_SIZE);
        //returnIcon = imageLoader.getIcon(TABS + RETURN, TAB_ICON_SIZE, TAB_ICON_SIZE);
    }
}
