package com.alexeus.graph;

import com.alexeus.graph.tab.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.alexeus.graph.Constants.*;

/**
 * Created by alexeus on 13.01.2017.
 * Основная панель графического интерфейса
 */
public class MainPanel extends JPanel {

    private static final int FRAME_WIDTH = 1300;
    private static final int FRAME_HEIGHT = 670;
    private static final int MAP_WIDTH = 900;
    private static final int MAP_HEIGHT = 600;
    private static final int BUTTON_PANEL_HEIGHT = 100;
    private static final int TAB_PANEL_HEIGHT = MAP_HEIGHT - BUTTON_PANEL_HEIGHT;
    private static final int LOG_HEIGHT = FRAME_HEIGHT - MAP_HEIGHT;
    private static final int LOG_WIDTH = FRAME_WIDTH - LOG_HEIGHT;
    private static final int LEFT_PANEL_WIDTH = FRAME_WIDTH - MAP_WIDTH;
    private static final int BUTTON_ICON_SIZE = (int) (BUTTON_PANEL_HEIGHT * 0.8);
    private static final int TAB_ICON_SIZE = (int) (LEFT_PANEL_WIDTH / 5.5);
    private static final int COLLAPSE_ICON_SIZE = LOG_HEIGHT;

    MapPanel mapPanel;
    JTextArea log;
    JPanel leftPanel;
    ImageIcon houseTabIcon, fightTabIcon, eventTabIcon, chatTabIcon, playIcon, playEndIcon, collapseIcon, returnIcon;
    ChatTabPanel chatTab;
    FightTabPanel fightTab;
    EventTabPanel eventTab;
    HouseTabPanel houseTab;
    JButton leftPanelCollapser;

    public MainPanel() {
        setLayout(new BorderLayout());
        loadPics();
        JPanel bottomPanel = new JPanel(new BorderLayout());
        // Лог под картой с описанием событий
        log = new JTextArea();
        log.setEditable(false);
        log.setPreferredSize(new Dimension(LOG_WIDTH, LOG_HEIGHT));
        leftPanelCollapser = new JButton("", collapseIcon);
        leftPanelCollapser.setPreferredSize(new Dimension(COLLAPSE_ICON_SIZE, COLLAPSE_ICON_SIZE));
        leftPanelCollapser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isPanelVisible = leftPanel.isVisible();
                leftPanel.setVisible(!isPanelVisible);
                leftPanelCollapser.setIcon(isPanelVisible ? returnIcon : collapseIcon);
            }
        });
        bottomPanel.add(log, BorderLayout.LINE_START);
        bottomPanel.add(leftPanelCollapser, BorderLayout.LINE_END);
        add(bottomPanel, BorderLayout.PAGE_END);
        // Отрисовка карты
        mapPanel = new MapPanel();
        mapPanel.setAutoscrolls(true);
        JScrollPane scrollPane = new JScrollPane(mapPanel);
        scrollPane.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        MouseAdapter ma = new MouseAdapter() {
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
        mapPanel.addMouseListener(ma);
        mapPanel.addMouseMotionListener(ma);
        mapPanel.addMouseWheelListener(new MouseWheelListener() {
             @Override
             public void mouseWheelMoved(MouseWheelEvent e) {
                 mapPanel.updatePreferredSize(e.getWheelRotation(), e.getPoint());
             }}
        );
        add(scrollPane, BorderLayout.CENTER);
        // отрисовка элементов левой панели
        leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, MAP_HEIGHT));

        JTabbedPane tabbedPane = new JTabbedPane();
        eventTab = new EventTabPanel();
        tabbedPane.addTab("", eventTabIcon, eventTab, "События Вестероса");
        houseTab = new HouseTabPanel();
        tabbedPane.addTab("", houseTabIcon, houseTab, "Карты домов");
        fightTab = new FightTabPanel();
        tabbedPane.addTab("", fightTabIcon, fightTab, "Сражение");
        chatTab = new ChatTabPanel();
        tabbedPane.addTab("", chatTabIcon, chatTab, "Чат");
        leftPanel.add(tabbedPane, BorderLayout.PAGE_END);
        tabbedPane.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, TAB_PANEL_HEIGHT));

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton playButton = new JButton("", playIcon);
        playButton.setPreferredSize(new Dimension(BUTTON_ICON_SIZE, BUTTON_ICON_SIZE));
        JButton playEndButton = new JButton("", playEndIcon);
        playEndButton.setPreferredSize(new Dimension(BUTTON_ICON_SIZE, BUTTON_ICON_SIZE));
        buttonsPanel.add(playButton);
        buttonsPanel.add(playEndButton);
        buttonsPanel.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, BUTTON_PANEL_HEIGHT));
        leftPanel.add(buttonsPanel, BorderLayout.PAGE_START);

        add(leftPanel, BorderLayout.LINE_END);
    }

    private void loadPics() {
        File file = new File(WAY + TABS + HOUSE);
        try {
            houseTabIcon = new ImageIcon(getScaledImage(ImageIO.read(file), TAB_ICON_SIZE, TAB_ICON_SIZE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        file = new File(WAY + TABS + FIGHT);
        try {
            fightTabIcon = new ImageIcon(getScaledImage(ImageIO.read(file), TAB_ICON_SIZE, TAB_ICON_SIZE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        file = new File(WAY + TABS + CHAT);
        try {
            chatTabIcon = new ImageIcon(getScaledImage(ImageIO.read(file), TAB_ICON_SIZE, TAB_ICON_SIZE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        file = new File(WAY + TABS + EVENT);
        try {
            eventTabIcon = new ImageIcon(getScaledImage(ImageIO.read(file), TAB_ICON_SIZE, TAB_ICON_SIZE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        file = new File(WAY + PLAY);
        try {
            playIcon = new ImageIcon(getScaledImage(ImageIO.read(file), BUTTON_ICON_SIZE, BUTTON_ICON_SIZE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        file = new File(WAY + PLAY_END);
        try {
            playEndIcon = new ImageIcon(getScaledImage(ImageIO.read(file), BUTTON_ICON_SIZE, BUTTON_ICON_SIZE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        file = new File(WAY + TABS + COLLAPSE);
        try {
            collapseIcon = new ImageIcon(getScaledImage(ImageIO.read(file), COLLAPSE_ICON_SIZE, COLLAPSE_ICON_SIZE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        file = new File(WAY + TABS + RETURN);
        try {
            returnIcon = new ImageIcon(getScaledImage(ImageIO.read(file), COLLAPSE_ICON_SIZE, COLLAPSE_ICON_SIZE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Image getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }
}
