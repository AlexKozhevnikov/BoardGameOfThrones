package com.alexeus.graph;

import com.alexeus.logic.Game;
import com.alexeus.logic.enums.UnitType;
import com.alexeus.logic.struct.Army;
import com.alexeus.logic.struct.Unit;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import static com.alexeus.graph.Constants.*;
import static com.alexeus.logic.constants.MainConstants.*;
import static com.alexeus.map.GameOfThronesMap.NUM_AREA;

/**
 * Created by alexeus on 13.01.2017.
 * Панель, на которой расположена карта со всеми юнитами и жетонами
 */
@SuppressWarnings("serial")
public class MapPanel extends JPanel{

    private final float MIN_SCALE = 0.3f;

    private final float MAX_SCALE = 5f;

    private float scale = 2.5f;

    // Отступ нужен, чтобы центрировать карту
    private int indent = 0;

    private Image mapImage;

    private Image[] powerTokenImage = new Image[NUM_PLAYER];

    private Image[][] unitImage = new Image[NUM_PLAYER][NUM_UNIT_TYPES];

    private Image[] victoryImage = new Image[NUM_PLAYER];

    private Image[] influenceImage = new Image[NUM_PLAYER];

    private Image[] cardBackImage = new Image[NUM_PLAYER];

    private Image[] defenceImage = new Image[MAX_DEFENCE + 1];

    private Image barbarianImage;

    private Image timeImage;

    private int defenceX[] = new int[NUM_AREA];
    private int defenceY[] = new int[NUM_AREA];
    private int armyX[] = new int[NUM_AREA];
    private int armyY[] = new int[NUM_AREA];

    private int unitImageSize;
    private int defenceWidth, defenceHeight;

    private Dimension preferredSize;

    private Game game;

    MapPanel() {
        loadPics();
        preferredSize = new Dimension((int) (mapImage.getWidth(null) / scale),
                (int) (mapImage.getHeight(null) / scale));
        setPreferredSize(preferredSize);
        defenceX[21] = 822;
        defenceY[21] = 769;
        defenceX[36] = 367;
        defenceY[36] = 2073;
        defenceX[56] = 1549;
        defenceY[56] = 2000;
        defenceX[57] = 306;
        defenceY[57] = 1705;
        defenceX[41] = 512;
        defenceY[41] = 2532;
        defenceX[48] = 1351;
        defenceY[48] = 2969;
        defenceX[54] = 1057;
        defenceY[54] = 2161;
        defenceX[31] = 1186;
        defenceY[31] = 1736;
        armyX[0] = 83; armyY[0] = 571;
        armyX[1] = 7; armyY[1] = 1322;
        armyX[2] = 97; armyY[2] = 1701;
        armyX[3] = 120; armyY[3] = 1962;
        armyX[4] = 201; armyY[4] = 3208;
        armyX[5] = 92; armyY[5] = 2782;
        armyX[6] = 1402; armyY[6] = 3113;
        armyX[7] = 1234; armyY[7] = 2748;
        armyX[8] = 1480; armyY[8] = 2352;
        armyX[9] = 1216; armyY[9] = 2013;
        armyX[10] = 1471; armyY[10] = 1275;
        armyX[11] = 1455; armyY[11] = 687;
        armyX[12] = 404; armyY[12] = 521;
        armyX[13] = 344; armyY[13] = 1484;
        armyX[14] = 266; armyY[14] = 1894;
        armyX[15] = 173; armyY[15] = 1647;
        armyX[16] = 1442; armyY[16] = 2920;
        armyX[17] = 1259; armyY[17] = 2494;
        armyX[18] = 1499; armyY[18] = 2105;
        armyX[19] = 967; armyY[19] = 1155;

        armyX[20] = 86; armyY[20] = 3083;
        armyX[21] = 658; armyY[21] = 933;
        armyX[22] = 411; armyY[22] = 918;
        armyX[23] = 953; armyY[23] = 318;
        armyX[24] = 1181; armyY[24] = 516;
        armyX[25] = 904; armyY[25] = 900;
        armyX[26] = 1122; armyY[26] = 905;
        armyX[27] = 664; armyY[27] = 1325;
        armyX[28] = 800; armyY[28] = 1476;
        armyX[29] = 986; armyY[29] = 1435;
        armyX[30] = 1182; armyY[30] = 1563;
        armyX[31] = 1110; armyY[31] = 1716;
        armyX[32] = 336; armyY[32] = 1298;
        armyX[33] = 474; armyY[33] = 1281;
        armyX[34] = 603; armyY[34] = 1509;
        armyX[35] = 626; armyY[35] = 1730;
        armyX[36] = 382; armyY[36] = 1890;
        armyX[37] = 548; armyY[37] = 1976;
        armyX[38] = 800; armyY[38] = 1886;
        armyX[39] = 746; armyY[39] = 2125;
        armyX[40] = 325; armyY[40] = 2212;
        armyX[41] = 369; armyY[41] = 2428;
        armyX[42] = 650; armyY[42] = 2378;
        armyX[43] = 576; armyY[43] = 2568;
        armyX[44] = 447; armyY[44] = 2861;
        armyX[45] = 323; armyY[45] = 2653;
        armyX[46] = 605; armyY[46] = 2987;
        armyX[47] = 1010; armyY[47] = 2958;
        armyX[48] = 1201; armyY[48] = 2897;
        armyX[49] = 762; armyY[49] = 2852;
        armyX[50] = 588; armyY[50] = 2726;
        armyX[51] = 836; armyY[51] = 2618;
        armyX[52] = 1100; armyY[52] = 2514;
        armyX[53] = 1141; armyY[53] = 2310;
        armyX[54] = 964; armyY[54] = 2208;
        armyX[55] = 989; armyY[55] = 1963;
        armyX[56] = 1448; armyY[56] = 1928;
        armyX[57] = 134; armyY[57] = 1555;

        for (int area = 0; area < NUM_AREA; area++) {
            // Поправляем по центру картинки гарнизонов
            if (defenceX[area] > 0) {
                defenceX[area] -= defenceImage[2].getWidth(null) / 2;
                defenceY[area] -= defenceImage[2].getHeight(null) / 2;
            }
        }
    }

    private void loadPics() {
        mapImage = loadPic(MAP_FILE);
        barbarianImage = loadPic(BARBARIAN);
        timeImage = loadPic(TIME);
        for(int defence = MIN_DEFENCE; defence <= MAX_DEFENCE; defence++) {
            defenceImage[defence] = loadPic(DEFENCE + "\\" + defence + PNG);
        }
        defenceWidth = defenceImage[MIN_DEFENCE].getWidth(null);
        defenceHeight = defenceImage[MIN_DEFENCE].getHeight(null);
        for (int player = 0; player < NUM_PLAYER; player++) {
            powerTokenImage[player] = loadPic(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + PNG);
            influenceImage[player] = loadPic(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + INFLUENCE);
            victoryImage[player] = loadPic(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + VICTORY);
            cardBackImage[player] = loadPic(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + CARD_BACK);
            for (UnitType unitType: UnitType.values()) {
                unitImage[player][unitType.getCode()] = loadPic(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + "_" +
                        unitType.engName() + PNG);
            }
        }
        unitImageSize = unitImage[0][0].getWidth(null);
    }

    private Image loadPic(String path) {
        File file = new File(WAY + path);
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setGame(Game game) {
        this.game = game;
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
        for (int area = 0; area < NUM_AREA; area++) {
            int garrison = game.getGarrisonInArea(area);
            if (garrison > 0) {
                g2d.drawImage(defenceImage[garrison],
                        indent + (int) (defenceX[area] / scale), (int) (defenceY[area] / scale),
                        (int) (defenceWidth / scale), (int) (defenceHeight / scale), null);
            }
            Army army = game.getArmyInArea(area);
            int trueUnitImageSize = (int) (unitImageSize / scale);
            if (!army.isEmpty()) {
                int xShift = 0, yShift = 0;
                for (Unit unit: army.getUnits()) {
                    g2d.drawImage(unitImage[army.getOwner()][unit.getUnitType().getCode()],
                            indent + xShift + (int) (armyX[area] / scale), yShift + (int) (armyY[area] / scale),
                            trueUnitImageSize, trueUnitImageSize, null);
                    xShift += (trueUnitImageSize * (area < 20 ? 0.85 : 0.7));
                    yShift += (trueUnitImageSize * 0.3);
                }
            }
        }
    }

    void updatePreferredSize(int n, Point p) {
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