package com.alexeus.graph;

import com.alexeus.graph.helper.UnitPackType;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.UnitType;
import com.alexeus.logic.struct.Army;
import com.alexeus.logic.struct.Unit;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.alexeus.graph.Constants.*;
import static com.alexeus.graph.helper.UnitPackType.*;
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

    private int UNIT_IMAGE_SIZE, HORIZONTAL_UNIT_INDENT, VERTICAL_UNIT_INDENT, HORIZONTAL_SHIP_INDENT, DEGREE_INDENT;
    private int trueUnitImageSize, trueHorizontalUnitIndent, trueVerticalUnitIndent, trueHorizontalShipIndent,
            trueDegreeIndent, trueDefenceWidth, trueDefenceHeight;

    private Image mapImage;

    private Image[] powerTokenImage = new Image[NUM_PLAYER];

    private Image[][] unitImage = new Image[NUM_PLAYER][NUM_UNIT_TYPES];

    private Image[] victoryImage = new Image[NUM_PLAYER];

    private Image[] influenceImage = new Image[NUM_PLAYER];

    private Image[] cardBackImage = new Image[NUM_PLAYER];

    private Image[] defenceImage = new Image[MAX_DEFENCE + 1];

    private BufferedImage[] areaFillImage = new BufferedImage[NUM_AREA];

    private BufferedImage[][] areaFillImagePlayer = new BufferedImage[NUM_AREA][NUM_PLAYER];

    private Image barbarianImage;

    private Image timeImage;

    private int defenceX[] = new int[NUM_AREA];
    private int defenceY[] = new int[NUM_AREA];
    private int areaBeginX[] = new int[NUM_AREA];
    private int areaBeginY[] = new int[NUM_AREA];
    private int armyX[] = new int[NUM_AREA];
    private int armyY[] = new int[NUM_AREA];
    private UnitPackType[] unitPackType = new UnitPackType[NUM_AREA];

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
        areaBeginX[0] = 0; areaBeginY[0] = 0;
        unitPackType[0] = line;

        armyX[1] = 7; armyY[1] = 1322;
        areaBeginX[1] = 0; areaBeginY[1] = 1163;
        unitPackType[1] = vertical;

        armyX[2] = 97; armyY[2] = 1701;
        areaBeginX[2] = 59; areaBeginY[2] = 1331;
        unitPackType[2] = triangleSquare;

        armyX[3] = 120; armyY[3] = 1962;
        areaBeginX[3] = 41; areaBeginY[3] = 1690;
        unitPackType[3] = triangleSquare;

        armyX[4] = 201; armyY[4] = 3208;
        areaBeginX[4] = 0; areaBeginY[4] = 2274;
        unitPackType[4] = line;

        armyX[5] = 92; armyY[5] = 2782;
        areaBeginX[5] = 45; areaBeginY[5] = 2451;
        unitPackType[5] = vertical;

        armyX[6] = 1402; armyY[6] = 3113;
        areaBeginX[6] = 763; areaBeginY[6] = 2541;
        unitPackType[6] = line;

        armyX[7] = 1234; armyY[7] = 2748;
        areaBeginX[7] = 859; areaBeginY[7] = 2629;
        unitPackType[7] = line;

        armyX[8] = 1480; armyY[8] = 2352;
        areaBeginX[8] = 1277; areaBeginY[8] = 1721;
        unitPackType[8] = line3square4;

        armyX[9] = 1216; armyY[9] = 2013;
        areaBeginX[9] = 1064; areaBeginY[9] = 1962;
        unitPackType[9] = triangleSquare;

        armyX[10] = 1471; armyY[10] = 1275;
        areaBeginX[10] = 795; areaBeginY[10] = 1003;
        unitPackType[10] = line;

        armyX[11] = 1455; armyY[11] = 687;
        areaBeginX[11] = 1146; areaBeginY[11] = 205;
        unitPackType[11] = line;

        armyX[12] = 404; armyY[12] = 521;
        armyX[13] = 344; armyY[13] = 1484;
        armyX[14] = 266; armyY[14] = 1894;
        armyX[15] = 173; armyY[15] = 1647;
        armyX[16] = 1442; armyY[16] = 2920;
        armyX[17] = 1259; armyY[17] = 2494;
        armyX[18] = 1499; armyY[18] = 2105;
        armyX[19] = 967; armyY[19] = 1155;
        for (int area = 12; area < 20; area++) {
            unitPackType[area] = port;
        }

        armyX[20] = 86; armyY[20] = 3083;
        areaBeginX[20] = 77; areaBeginY[20] = 3027;
        unitPackType[20] = triangleSquare;

        armyX[21] = 658; armyY[21] = 933;
        areaBeginX[21] = 307; areaBeginY[21] = 430;
        unitPackType[21] = line;

        armyX[22] = 411; armyY[22] = 918;
        areaBeginX[22] = 174; areaBeginY[22] = 714;
        unitPackType[22] = line;

        armyX[23] = 953; armyY[23] = 318;
        areaBeginX[23] = 684; areaBeginY[23] = 241;
        unitPackType[23] = line;

        armyX[24] = 1181; armyY[24] = 516;
        areaBeginX[24] = 1068; areaBeginY[24] = 408;
        unitPackType[24] = line3square4;

        armyX[25] = 904; armyY[25] = 900;
        areaBeginX[25] = 832; areaBeginY[25] = 709;
        unitPackType[25] = line;

        armyX[26] = 1122; armyY[26] = 905;
        areaBeginX[26] = 1050; areaBeginY[26] = 864;
        unitPackType[26] = line3square4;

        armyX[27] = 664; armyY[27] = 1325;
        areaBeginX[27] = 645; areaBeginY[27] = 1107;
        unitPackType[27] = line;

        armyX[28] = 800; armyY[28] = 1476;
        areaBeginX[28] = 690; areaBeginY[28] = 1426;
        unitPackType[28] = line;

        armyX[29] = 986; armyY[29] = 1435;
        areaBeginX[29] = 945; areaBeginY[29] = 1353;
        unitPackType[29] = line;

        armyX[30] = 1182; armyY[30] = 1563;
        areaBeginX[30] = 820; areaBeginY[30] = 1462;
        unitPackType[30] = line3square4;

        armyX[31] = 1110; armyY[31] = 1716;
        areaBeginX[31] = 1057; areaBeginY[31] = 1654;
        unitPackType[31] = line;

        armyX[32] = 336; armyY[32] = 1298;
        areaBeginX[32] = 181; areaBeginY[32] = 1201;
        unitPackType[32] = horizontal;

        armyX[33] = 474; armyY[33] = 1281;
        areaBeginX[33] = 450; areaBeginY[33] = 1165;
        unitPackType[33] = line3square4;

        armyX[34] = 603; armyY[34] = 1509;
        areaBeginX[34] = 515; areaBeginY[34] = 1396;
        unitPackType[34] = degree;

        armyX[35] = 626; armyY[35] = 1730;
        areaBeginX[35] = 548; areaBeginY[35] = 1655;
        unitPackType[35] = line3square4;

        armyX[36] = 382; armyY[36] = 1890;
        areaBeginX[36] = 216; areaBeginY[36] = 1768;
        unitPackType[36] = line3square4;

        armyX[37] = 548; armyY[37] = 1976;
        areaBeginX[37] = 508; areaBeginY[37] = 1832;
        unitPackType[37] = line;

        armyX[38] = 800; armyY[38] = 1886;
        areaBeginX[38] = 747; areaBeginY[38] = 1800;
        unitPackType[38] = line;

        armyX[39] = 746; armyY[39] = 2125;
        areaBeginX[39] = 510; areaBeginY[39] = 2019;
        unitPackType[39] = line;

        armyX[40] = 325; armyY[40] = 2212;
        areaBeginX[40] = 260; areaBeginY[40] = 2067;
        unitPackType[40] = line;

        armyX[41] = 369; armyY[41] = 2428;
        areaBeginX[41] = 262; areaBeginY[41] = 2358;
        unitPackType[41] = line;

        armyX[42] = 650; armyY[42] = 2378;
        areaBeginX[42] = 516; areaBeginY[42] = 2235;
        unitPackType[42] = line;

        armyX[43] = 576; armyY[43] = 2568;
        areaBeginX[43] = 469; areaBeginY[43] = 2508;
        unitPackType[43] = line3square4;

        armyX[44] = 323; armyY[44] = 2653;
        areaBeginX[44] = 148; areaBeginY[44] = 2619;
        unitPackType[44] = triangleSquare;

        armyX[45] = 447; armyY[45] = 2861;
        areaBeginX[45] = 292; areaBeginY[45] = 2725;
        unitPackType[45] = line;

        armyX[46] = 605; armyY[46] = 2987;
        areaBeginX[46] = 548; areaBeginY[46] = 2911;
        unitPackType[46] = line;

        armyX[47] = 1010; armyY[47] = 2958;
        areaBeginX[47] = 855; areaBeginY[47] = 2929;
        unitPackType[47] = line;

        armyX[48] = 1201; armyY[48] = 2897;
        areaBeginX[48] = 1053; areaBeginY[48] = 2844;
        unitPackType[48] = horizontal;

        armyX[49] = 762; armyY[49] = 2852;
        areaBeginX[49] = 723; areaBeginY[49] = 2832;
        unitPackType[49] = line;

        armyX[50] = 588; armyY[50] = 2726;
        areaBeginX[50] = 556; areaBeginY[50] = 2643;
        unitPackType[50] = line3square4;

        armyX[51] = 836; armyY[51] = 2618;
        areaBeginX[51] = 754; areaBeginY[51] = 2526;
        unitPackType[51] = line3square4;

        armyX[52] = 1100; armyY[52] = 2514;
        areaBeginX[52] = 1075; areaBeginY[52] = 2353;
        unitPackType[52] = line;

        armyX[53] = 1141; armyY[53] = 2310;
        areaBeginX[53] = 945; areaBeginY[53] = 2093;
        unitPackType[53] = triangleSquare;

        armyX[54] = 964; armyY[54] = 2208;
        areaBeginX[54] = 960; areaBeginY[54] = 2077;
        unitPackType[54] = triangleSquare;

        armyX[55] = 989; armyY[55] = 1963;
        areaBeginX[55] = 918; areaBeginY[55] = 1828;
        unitPackType[55] = line;

        armyX[56] = 1448; armyY[56] = 1928;
        areaBeginX[56] = 1373; areaBeginY[56] = 1883;
        unitPackType[56] = line;

        armyX[57] = 134; armyY[57] = 1555;
        areaBeginX[57] = 112; areaBeginY[57] = 1460;
        unitPackType[57] = line;

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
        for (int area = 0; area < NUM_AREA; area++) {
            if (area >= 12 && area < 20) continue;
            areaFillImage[area] = (BufferedImage) loadPic(AREA + area + PNG);
        }
        UNIT_IMAGE_SIZE = unitImage[0][0].getWidth(null);
        HORIZONTAL_SHIP_INDENT = (int) (UNIT_IMAGE_SIZE * 0.85);
        HORIZONTAL_UNIT_INDENT = (int) (UNIT_IMAGE_SIZE * 0.7);
        VERTICAL_UNIT_INDENT = (int) (UNIT_IMAGE_SIZE * 0.3);
        DEGREE_INDENT = (int) (UNIT_IMAGE_SIZE * 0.7);
        setTrueSizes();
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
        // Рисуем карту
        if (scale <= 2.5f) g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(mapImage, indent, 0, (int) (mapImage.getWidth(null) / scale), (int) (mapImage.getHeight(null) / scale), null);
        // окраска областей в цвета их владельцев
        Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
        g2d.setComposite(comp);
        for (int area = 0; area < NUM_AREA; area++) {
            int areaOwner = game.getAreaOwner(area);
            if (areaFillImage[area] != null && areaOwner >= 0) {
                if (areaFillImagePlayer[area][areaOwner] == null) {
                    areaFillImagePlayer[area][areaOwner] = dye(areaFillImage[area], HOUSE_COLOR[areaOwner]);
                }
                g2d.drawImage(areaFillImagePlayer[area][areaOwner], indent + (int) (areaBeginX[area] / scale), (int) (areaBeginY[area] / scale),
                        (int) (areaFillImage[area].getWidth(null) / scale), (int) (areaFillImage[area].getHeight(null) / scale), null);
            }
        }
        // Рисуем гарнизоны, юниты, жетоны власти и приказы
        comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
        g2d.setComposite(comp);
        for (int area = 0; area < NUM_AREA; area++) {
            int garrison = game.getGarrisonInArea(area);
            if (garrison > 0) {
                g2d.drawImage(defenceImage[garrison],
                        indent + (int) (defenceX[area] / scale), (int) (defenceY[area] / scale),
                        trueDefenceWidth, trueDefenceHeight, null);
            }
            // TODO отрисовка жетонов власти
            Army army = game.getArmyInArea(area);
            int trueUnitImageSize = (int) (UNIT_IMAGE_SIZE / scale);
            int armySize = army.getNumUnits();
            int xShift, yShift;
            if (armySize > 0) {
                ArrayList<Unit> units = army.getUnits();
                for (int index = 0; index < armySize; index++) {
                    // TODO изменить xShift, yShift в зависимости от типа пакования юнитов
                    xShift = 0;
                    yShift = 0;
                    g2d.drawImage(unitImage[army.getOwner()][units.get(index).getUnitType().getCode()],
                            indent + xShift + (int) (armyX[area] / scale), yShift + (int) (armyY[area] / scale),
                            trueUnitImageSize, trueUnitImageSize, null);
                }
            }
            // TODO отрисовка приказов
        }
        // TODO победные очки, треки влияния, снабжение (?), одичалые, время
    }

    private static BufferedImage dye(BufferedImage image, Color color)
    {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage dyed = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dyed.createGraphics();
        g.drawImage(image, 0,0, null);
        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(color);
        g.fillRect(0,0,w,h);
        g.dispose();
        return dyed;
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
        setTrueSizes();

        int offX = (int)(p.x * d) - p.x;
        int offY = (int)(p.y * d) - p.y;
        setLocation(getLocation().x - offX, getLocation().y - offY);
        getParent().doLayout();
    }

    private void setTrueSizes() {
        trueUnitImageSize = (int) (UNIT_IMAGE_SIZE / scale);
        trueHorizontalShipIndent = (int) (HORIZONTAL_SHIP_INDENT / scale);
        trueHorizontalUnitIndent = (int) (HORIZONTAL_UNIT_INDENT / scale);
        trueVerticalUnitIndent = (int) (VERTICAL_UNIT_INDENT / scale);
        trueDegreeIndent = (int) (DEGREE_INDENT / scale);
        trueDefenceWidth = (int) (defenceWidth / scale);
        trueDefenceHeight = (int) (defenceHeight / scale);
    }
}