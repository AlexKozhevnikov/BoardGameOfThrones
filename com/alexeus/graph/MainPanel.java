package com.alexeus.graph;

import com.alexeus.control.Controller;
import com.alexeus.graph.tab.*;
import com.alexeus.graph.util.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static com.alexeus.graph.constants.Constants.*;

/**
 * Created by alexeus on 13.01.2017.
 * Основная панель графического интерфейса
 */
public class MainPanel extends JPanel {

    private MapPanel mapPanel;
    private ImageIcon playIcon, playEndIcon;
    //private ImageIcon collapseIcon, returnIcon;
    private JButton leftPanelCollapser;

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
        leftPanel.add(tabbedPane, BorderLayout.PAGE_END);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton playButton = new JButton("", playIcon);
        playButton.setPreferredSize(new Dimension(BUTTON_ICON_SIZE, BUTTON_ICON_SIZE));
        JButton playEndButton = new JButton("", playEndIcon);
        playEndButton.setPreferredSize(new Dimension(BUTTON_ICON_SIZE, BUTTON_ICON_SIZE));
        buttonsPanel.add(playButton);
        buttonsPanel.add(playEndButton);
        buttonsPanel.setPreferredSize(new Dimension(BUTTON_PANEL_WIDTH, BUTTON_PANEL_HEIGHT));
        leftPanel.add(buttonsPanel, BorderLayout.PAGE_START);
        add(leftPanel, BorderLayout.LINE_END);
        Controller.getInstance().receiveComponents(mapPanel, tabbedPane);
        Controller.getInstance().startNewGame();
    }

    private void loadPics() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        playIcon = imageLoader.getIcon(PLAY, TAB_ICON_SIZE, TAB_ICON_SIZE);
        playEndIcon = imageLoader.getIcon(PLAY_END, TAB_ICON_SIZE, TAB_ICON_SIZE);
        //collapseIcon = imageLoader.getIcon(TABS + COLLAPSE, TAB_ICON_SIZE, TAB_ICON_SIZE);
        //returnIcon = imageLoader.getIcon(TABS + RETURN, TAB_ICON_SIZE, TAB_ICON_SIZE);
    }
}
