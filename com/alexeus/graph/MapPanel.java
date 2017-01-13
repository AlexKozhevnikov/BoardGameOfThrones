package com.alexeus.graph;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import static com.alexeus.graph.Constants.*;

/**
 * Created by alexeus on 13.01.2017.
 * Панель, на которой расположена карта со всеми юнитами и жетонами
 */
@SuppressWarnings("serial")
public class MapPanel extends JPanel{

    private final double MIN_SCALE = 0.3;

    private final double MAX_SCALE = 5;

    // Отступ нужен, чтобы центрировать карту
    private int indent = 0;

    private Image mapImage;

    double scale = 3;

    private Dimension preferredSize;

    public MapPanel() {
        loadPics();
        preferredSize = new Dimension((int) (mapImage.getWidth(null) / scale),
                (int) (mapImage.getHeight(null) / scale));
        setPreferredSize(preferredSize);
    }

    private void loadPics() {
        File file = new File(WAY + MAP_FILE);
        try {
            mapImage = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Custom painting codes on this JPanel
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(mapImage, indent, 0, (int) (mapImage.getWidth(null) / scale), (int) (mapImage.getHeight(null) / scale), null);
    }

    public void updatePreferredSize(int n, Point p) {
        double d = Math.pow(1.1, -n);
        double oldScale = scale;
        scale /= d;
        if (scale < MIN_SCALE) {
            scale = MIN_SCALE;
        } else if (scale > MAX_SCALE) {
            scale = MAX_SCALE;
        }
        d = oldScale/ scale;
        int scorlPaneWidth = getParent().getWidth();
        if (mapImage.getWidth(null) / scale < scorlPaneWidth) {
            indent = (int) (scorlPaneWidth - (mapImage.getWidth(null)) / scale) / 2;
        }
        int w = (int) (mapImage.getWidth(null) / scale)  + 2 * indent;
        int h = (int) (mapImage.getHeight(null) / scale);
        preferredSize.setSize(w, h);

        int offX = (int)(p.x * d) - p.x;
        int offY = (int)(p.y * d) - p.y;
        setLocation(getLocation().x - offX, getLocation().y - offY);
        getParent().doLayout();
    }
}