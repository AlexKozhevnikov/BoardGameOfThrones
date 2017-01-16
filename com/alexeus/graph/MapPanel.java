package com.alexeus.graph;

import com.alexeus.graph.helper.UnitPackType;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.Order;
import com.alexeus.logic.enums.TrackType;
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
    private int DEFENCE_WIDTH, DEFENCE_HEIGHT, POWER_WIDTH, POWER_HEIGHT, INFLUENCE_SIZE, VICTORY_SIZE,
            TRACK_VERTICAL_INDENT = 167;
    private int trueUnitImageSize, trueHorizontalUnitIndent, trueVerticalUnitIndent, trueHorizontalShipIndent,
            trueDegreeIndent, trueDefenceWidth, trueDefenceHeight, trueInfluenceSize, trueTokenWidth, trueTokenHeight,
            trueVictorySize, trueOrderSize;

    private Image mapImage;

    private Image[] powerTokenImage = new Image[NUM_PLAYER];

    private Image[][] unitImage = new Image[NUM_PLAYER][NUM_UNIT_TYPES];

    private Image[] victoryImage = new Image[NUM_PLAYER];

    private Image[] influenceImage = new Image[NUM_PLAYER];

    private Image[] defenceImage = new Image[MAX_DEFENCE + 1];

    private Image[] orderImage = new Image[NUM_DIFFERENT_ORDERS];

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
    private int orderX[] = new int[NUM_AREA];
    private int orderY[] = new int[NUM_AREA];
    private int trackBeginX[] = new int[NUM_TRACK];
    private int trackBeginY;
    private UnitPackType[] unitPackType = new UnitPackType[NUM_AREA];

    int[] xShift = new int[MAX_TROOPS_IN_AREA], yShift = new int[MAX_TROOPS_IN_AREA];

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
        trackBeginX[0] = 1710;
        trackBeginX[1] = 1875;
        trackBeginX[2] = 2037;
        trackBeginY = 934;

        armyX[0] = 110; armyY[0] = 583;
        areaBeginX[0] = 0; areaBeginY[0] = 0;
        unitPackType[0] = line;
        orderX[0] = 57; orderY[0] = 698;

        armyX[1] = 7; armyY[1] = 1357;
        areaBeginX[1] = 0; areaBeginY[1] = 1163;
        unitPackType[1] = vertical;
        orderX[1] = 24; orderY[1] = 1182;

        armyX[2] = 107; armyY[2] = 1721;
        areaBeginX[2] = 59; areaBeginY[2] = 1331;
        unitPackType[2] = triangleSquare;
        orderX[2] = 457; orderY[2] = 1613;

        armyX[3] = 146; armyY[3] = 1962;
        areaBeginX[3] = 41; areaBeginY[3] = 1690;
        unitPackType[3] = triangleSquare;
        orderX[3] = 163; orderY[3] = 1859;

        armyX[4] = 351; armyY[4] = 3188;
        areaBeginX[4] = 0; areaBeginY[4] = 2274;
        unitPackType[4] = line;
        orderX[4] = 460; orderY[4] = 3114;

        armyX[5] = 92; armyY[5] = 2752;
        areaBeginX[5] = 44; areaBeginY[5] = 2451;
        unitPackType[5] = vertical;
        orderX[5] = 262; orderY[5] = 2503;

        armyX[6] = 1402; armyY[6] = 3113;
        areaBeginX[6] = 763; areaBeginY[6] = 2541;
        unitPackType[6] = line;
        orderX[6] = 1317; orderY[6] = 3167;

        armyX[7] = 1200; armyY[7] = 2748;
        areaBeginX[7] = 859; areaBeginY[7] = 2629;
        unitPackType[7] = line;
        orderX[7] = 977; orderY[7] = 2747;

        armyX[8] = 1480; armyY[8] = 2382;
        areaBeginX[8] = 1277; areaBeginY[8] = 1721;
        unitPackType[8] = line3square4;
        orderX[8] = 1388; orderY[8] = 2181;

        armyX[9] = 1226; armyY[9] = 2030;
        areaBeginX[9] = 1064; areaBeginY[9] = 1962;
        unitPackType[9] = triangleSquare;
        orderX[9] = 1160; orderY[9] = 2112;

        armyX[10] = 1471; armyY[10] = 1275;
        areaBeginX[10] = 795; areaBeginY[10] = 1003;
        unitPackType[10] = line;
        orderX[10] = 1488; orderY[10] = 1382;

        armyX[11] = 1455; armyY[11] = 717;
        areaBeginX[11] = 1146; areaBeginY[11] = 205;
        unitPackType[11] = line;
        orderX[11] = 1354; orderY[11] = 783;

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
            orderX[area] = armyX[area] + INFLUENCE_SIZE;
            orderY[area] = armyY[area] + INFLUENCE_SIZE;
        }

        armyX[20] = 106; armyY[20] = 3083;
        areaBeginX[20] = 77; areaBeginY[20] = 3027;
        unitPackType[20] = triangleSquare;
        orderX[20] = 185; orderY[20] = 3066;

        armyX[21] = 678; armyY[21] = 933;
        areaBeginX[21] = 307; areaBeginY[21] = 430;
        unitPackType[21] = line;
        orderX[21] = 627; orderY[21] = 780;

        armyX[22] = 391; armyY[22] = 908;
        areaBeginX[22] = 173; areaBeginY[22] = 714;
        unitPackType[22] = line;
        orderX[22] = 311; orderY[22] = 1005;

        armyX[23] = 993; armyY[23] = 338;
        areaBeginX[23] = 684; areaBeginY[23] = 241;
        unitPackType[23] = line3square4;
        orderX[23] = 1065; orderY[23] = 262;

        armyX[24] = 1200; armyY[24] = 516;
        areaBeginX[24] = 1068; areaBeginY[24] = 408;
        unitPackType[24] = line3square4;
        orderX[24] = 1320; orderY[24] = 497;

        armyX[25] = 953; armyY[25] = 910;
        areaBeginX[25] = 833; areaBeginY[25] = 709;
        unitPackType[25] = line3square4;
        orderX[25] = 992; orderY[25] = 1050;

        armyX[26] = 1136; armyY[26] = 915;
        areaBeginX[26] = 1051; areaBeginY[26] = 865;
        unitPackType[26] = triangleSquare;
        orderX[26] = 1059; orderY[26] = 976;

        armyX[27] = 694; armyY[27] = 1325;
        areaBeginX[27] = 643; areaBeginY[27] = 1105;
        unitPackType[27] = line3square4;
        orderX[27] = 702; orderY[27] = 1216;

        armyX[28] = 800; armyY[28] = 1486;
        areaBeginX[28] = 690; areaBeginY[28] = 1425;
        unitPackType[28] = line;
        orderX[28] = 861; orderY[28] = 1444;

        armyX[29] = 1046; armyY[29] = 1435;
        areaBeginX[29] = 945; areaBeginY[29] = 1352;
        unitPackType[29] = line;
        orderX[29] = 1153; orderY[29] = 1390;

        armyX[30] = 1192; armyY[30] = 1578;
        areaBeginX[30] = 820; areaBeginY[30] = 1462;
        unitPackType[30] = line3square4;
        orderX[30] = 1264; orderY[30] = 1519;

        armyX[31] = 1140; armyY[31] = 1726;
        areaBeginX[31] = 1057; areaBeginY[31] = 1654;
        unitPackType[31] = line;
        orderX[31] = 1232; orderY[31] = 1718;

        armyX[32] = 306; armyY[32] = 1298;
        areaBeginX[32] = 181; areaBeginY[32] = 1201;
        unitPackType[32] = horizontal;
        orderX[32] = 387; orderY[32] = 1223;

        armyX[33] = 520; armyY[33] = 1281;
        areaBeginX[33] = 450; areaBeginY[33] = 1164;
        unitPackType[33] = line3square4;
        orderX[33] = 574; orderY[33] = 1200;

        armyX[34] = 623; armyY[34] = 1509;
        areaBeginX[34] = 515; areaBeginY[34] = 1396;
        unitPackType[34] = degree;
        orderX[34] = 558; orderY[34] = 1406;

        armyX[35] = 666; armyY[35] = 1740;
        areaBeginX[35] = 548; areaBeginY[35] = 1655;
        unitPackType[35] = line3square4;
        orderX[35] = 746; orderY[35] = 1698;

        armyX[36] = 422; armyY[36] = 1880;
        areaBeginX[36] = 216; areaBeginY[36] = 1768;
        unitPackType[36] = line3square4;
        orderX[36] = 382; orderY[36] = 1947;

        armyX[37] = 598; armyY[37] = 1986;
        areaBeginX[37] = 508; areaBeginY[37] = 1832;
        unitPackType[37] = line;
        orderX[37] = 697; orderY[37] = 1900;

        armyX[38] = 820; armyY[38] = 1886;
        areaBeginX[38] = 747; areaBeginY[38] = 1800;
        unitPackType[38] = line;
        orderX[38] = 883; orderY[38] = 1831;

        armyX[39] = 746; armyY[39] = 2145;
        areaBeginX[39] = 510; areaBeginY[39] = 2019;
        unitPackType[39] = line;
        orderX[39] = 596; orderY[39] = 2228;

        armyX[40] = 357; armyY[40] = 2212;
        areaBeginX[40] = 260; areaBeginY[40] = 2067;
        unitPackType[40] = line;
        orderX[40] = 290; orderY[40] = 2271;

        armyX[41] = 379; armyY[41] = 2428;
        areaBeginX[41] = 262; areaBeginY[41] = 2358;
        unitPackType[41] = line;
        orderX[41] = 441; orderY[41] = 2513;

        armyX[42] = 630; armyY[42] = 2390;
        areaBeginX[42] = 516; areaBeginY[42] = 2235;
        unitPackType[42] = line;
        orderX[42] = 763; orderY[42] = 2354;

        armyX[43] = 596; armyY[43] = 2585;
        areaBeginX[43] = 469; areaBeginY[43] = 2508;
        unitPackType[43] = line3square4;
        orderX[43] = 746; orderY[43] = 2552;

        armyX[44] = 330; armyY[44] = 2733;
        areaBeginX[44] = 148; areaBeginY[44] = 2619;
        unitPackType[44] = triangleSquare;
        orderX[44] = 232; orderY[44] = 2785;

        armyX[45] = 417; armyY[45] = 2851;
        areaBeginX[45] = 292; areaBeginY[45] = 2725;
        unitPackType[45] = line;
        orderX[45] = 385; orderY[45] = 2946;

        armyX[46] = 613; armyY[46] = 2987;
        areaBeginX[46] = 548; areaBeginY[46] = 2911;
        unitPackType[46] = line;
        orderX[46] = 666; orderY[46] = 2947;

        armyX[47] = 1040; armyY[47] = 2958;
        areaBeginX[47] = 854; areaBeginY[47] = 2929;
        unitPackType[47] = line;
        orderX[47] = 913; orderY[47] = 2989;

        armyX[48] = 1169; armyY[48] = 2897;
        areaBeginX[48] = 1053; areaBeginY[48] = 2844;
        unitPackType[48] = horizontal;
        orderX[48] = 1252; orderY[48] = 2895;

        armyX[49] = 852; armyY[49] = 2872;
        areaBeginX[49] = 723; areaBeginY[49] = 2832;
        unitPackType[49] = line;
        orderX[49] = 982; orderY[49] = 2869;

        armyX[50] = 633; armyY[50] = 2746;
        areaBeginX[50] = 556; areaBeginY[50] = 2643;
        unitPackType[50] = line3square4;
        orderX[50] = 635; orderY[50] = 2814;

        armyX[51] = 886; armyY[51] = 2628;
        areaBeginX[51] = 754; areaBeginY[51] = 2526;
        unitPackType[51] = line3square4;
        orderX[51] = 808; orderY[51] = 2757;

        armyX[52] = 1120; armyY[52] = 2500;
        areaBeginX[52] = 1075; areaBeginY[52] = 2353;
        unitPackType[52] = triangleSquare;
        orderX[52] = 1229; orderY[52] = 2605;

        armyX[53] = 1151; armyY[53] = 2315;
        areaBeginX[53] = 945; areaBeginY[53] = 2093;
        unitPackType[53] = triangleSquare;
        orderX[53] = 1057; orderY[53] = 2400;

        armyX[54] = 994; armyY[54] = 2160;
        areaBeginX[54] = 960; areaBeginY[54] = 2077;
        unitPackType[54] = triangleSquare;
        orderX[54] = 1029; orderY[54] = 2264;

        armyX[55] = 1029; armyY[55] = 1923;
        areaBeginX[55] = 918; areaBeginY[55] = 1828;
        unitPackType[55] = line;
        orderX[55] = 985; orderY[55] = 2000;

        armyX[56] = 1468; armyY[56] = 1998;
        areaBeginX[56] = 1373; areaBeginY[56] = 1883;
        unitPackType[56] = line;
        orderX[56] = 1463; orderY[56] = 1915;

        armyX[57] = 194; armyY[57] = 1565;
        areaBeginX[57] = 112; areaBeginY[57] = 1460;
        unitPackType[57] = line;
        orderX[57] = 320; orderY[57] = 1600;

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
            defenceImage[defence] = loadPic(DEFENCE + defence + PNG);
        }
        DEFENCE_WIDTH = defenceImage[MIN_DEFENCE].getWidth(null);
        DEFENCE_HEIGHT = defenceImage[MIN_DEFENCE].getHeight(null);
        for(int orderCode = 0; orderCode < NUM_DIFFERENT_ORDERS; orderCode++) {
            orderImage[orderCode] = loadPic(ORDER + orderCode + PNG);
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            powerTokenImage[player] = loadPic(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + PNG);
            influenceImage[player] = loadPic(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + INFLUENCE);
            victoryImage[player] = loadPic(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + VICTORY);
            for (UnitType unitType: UnitType.values()) {
                unitImage[player][unitType.getCode()] = loadPic(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + "_" +
                        unitType.engName() + PNG);
            }
        }
        INFLUENCE_SIZE = influenceImage[0].getWidth(null);
        POWER_WIDTH = powerTokenImage[0].getWidth(null);
        POWER_HEIGHT = powerTokenImage[0].getHeight(null);
        VICTORY_SIZE = victoryImage[0].getWidth(null);

        for (int area = 0; area < NUM_AREA; area++) {
            if (area >= 12 && area < 20) continue;
            areaFillImage[area] = (BufferedImage) loadPic(AREA + area + PNG);
        }
        UNIT_IMAGE_SIZE = unitImage[0][0].getWidth(null);
        HORIZONTAL_SHIP_INDENT = (int) (UNIT_IMAGE_SIZE * 0.85);
        HORIZONTAL_UNIT_INDENT = (int) (UNIT_IMAGE_SIZE * 0.7);
        VERTICAL_UNIT_INDENT = (int) (UNIT_IMAGE_SIZE * 0.3);
        DEGREE_INDENT = (int) (UNIT_IMAGE_SIZE * 0.6);
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
        if (scale > 2f) {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
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
            int areaOwner = game.getAreaOwner(area);
            int garrison = game.getGarrisonInArea(area);
            if (garrison > 0) {
                g2d.drawImage(defenceImage[garrison],
                        indent + (int) (defenceX[area] / scale), (int) (defenceY[area] / scale),
                        trueDefenceWidth, trueDefenceHeight, null);
            }
            // TODO отрисовка жетонов власти
            // Приказы
            Order order = game.getOrderInArea(area);
            if (order == Order.closed) {
                g2d.drawImage(influenceImage[areaOwner], indent + (int) (orderX[area] / scale),
                        (int) (orderY[area] / scale), trueOrderSize, trueOrderSize, null);
            } else if (order != null) {
                g2d.drawImage(orderImage[order.getCode()], indent + (int) (orderX[area] / scale),
                        (int) (orderY[area] / scale), trueOrderSize, trueOrderSize, null);
            }
            // Войска
            Army army = game.getArmyInArea(area);
            int trueUnitImageSize = (int) (UNIT_IMAGE_SIZE / scale);
            int armySize = army.getNumUnits();
            if (armySize > 0) {
                ArrayList<Unit> units = army.getUnits();
                UnitPackType packType = unitPackType[area];
                if (packType == triangleSquare && armySize == 3) {
                    xShift[0] = (int) (-(trueUnitImageSize + trueHorizontalShipIndent) / 4f);
                    xShift[1] = (int) ((trueUnitImageSize + trueHorizontalShipIndent) / 4f);
                    xShift[2] = (int) (-(trueUnitImageSize + trueHorizontalShipIndent) / 4f) + trueVerticalUnitIndent;
                    yShift[0] = (int) (-(trueUnitImageSize + trueHorizontalShipIndent) / 4f);
                    yShift[1] = (int) (-(trueUnitImageSize + trueHorizontalShipIndent) / 4f) + trueVerticalUnitIndent;
                    yShift[2] = (int) ((trueUnitImageSize + trueHorizontalShipIndent) / 4f);
                } else if ((packType == triangleSquare || packType == line3square4) && armySize == MAX_TROOPS_IN_AREA) {
                    xShift[1] = (int) ((trueUnitImageSize + trueHorizontalShipIndent + trueVerticalUnitIndent) / 4f);
                    xShift[0] = xShift[1] - trueHorizontalShipIndent;
                    xShift[2] = (int) (-(trueUnitImageSize + trueHorizontalShipIndent + trueVerticalUnitIndent) / 4f);
                    xShift[3] = xShift[2] + trueHorizontalShipIndent;
                    yShift[0] = (int) (-(trueUnitImageSize + trueHorizontalShipIndent + trueVerticalUnitIndent) / 4f);
                    yShift[1] = yShift[0] + trueVerticalUnitIndent;
                    yShift[3] = (int) ((trueUnitImageSize + trueHorizontalShipIndent + trueVerticalUnitIndent) / 4f);
                    yShift[2] = yShift[3] - trueVerticalUnitIndent;
                } else {
                    for (int index = 0; index < armySize; index++) {
                        if (packType == line || (packType == line3square4 && armySize <= 3) ||
                                (packType == triangleSquare && armySize <= 2)) {
                            xShift[index] = (int) ((area < 20 ? trueHorizontalShipIndent : trueHorizontalUnitIndent) *
                                    (index - (armySize - 1) / 2f));
                            yShift[index] = (int) (trueVerticalUnitIndent * (index - (armySize - 1) / 2f));
                        } else if (packType == degree) {
                            xShift[index] = (int) ((area < 20 ? trueDegreeIndent : trueDegreeIndent) *
                                    (index - (armySize - 1) / 2f));
                            yShift[index] = xShift[index];
                        } else if (packType == horizontal) {
                            xShift[index] = (int) ((area < 20 ? trueHorizontalShipIndent : trueHorizontalUnitIndent) *
                                    (index - (armySize - 1) / 2f));
                            yShift[index] = 0;
                        } else if (packType == vertical) {
                            xShift[index] = 0;
                            yShift[index] = (int) ((area < 20 ? trueHorizontalShipIndent : trueHorizontalUnitIndent) *
                                    (index - (armySize - 1) / 2f));
                        } else {
                            System.out.println(area);
                        }
                    }
                }
                for (int index = 0; index < armySize; index++) {
                    g2d.drawImage(unitImage[army.getOwner()][units.get(index).getUnitType().getCode()],
                            indent + xShift[index] + (int) (armyX[area] / scale), yShift[index] + (int) (armyY[area] / scale),
                            trueUnitImageSize, trueUnitImageSize, null);
                }
            }
        }
        // TODO победные очки, снабжение (?), одичалые, время
        // Рисуем треки влияния
        int playerOnPlace[] = null;
        for (int i = 0; i < NUM_TRACK; i++) {
            switch (i) {
                case 0:
                    playerOnPlace = game.getInfluenceTrackPlayerOnPlace(TrackType.ironThrone);
                    break;
                case 1:
                    playerOnPlace = game.getInfluenceTrackPlayerOnPlace(TrackType.valyrianSword);
                    break;
                case 2:
                    playerOnPlace = game.getInfluenceTrackPlayerOnPlace(TrackType.raven);
                    break;
            }
            for (int place = 0; place < NUM_PLAYER; place++) {
                g2d.drawImage(influenceImage[playerOnPlace[place]],
                        indent + (int) (trackBeginX[i] / scale),
                        (int) ((trackBeginY - place * TRACK_VERTICAL_INDENT) / scale),
                        trueInfluenceSize, trueInfluenceSize, null);
            }
        }
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
        trueDefenceWidth = (int) (DEFENCE_WIDTH / scale);
        trueDefenceHeight = (int) (DEFENCE_HEIGHT / scale);
        trueTokenWidth = (int) (POWER_WIDTH / scale);
        trueTokenHeight = (int) (POWER_HEIGHT / scale);
        trueInfluenceSize = (int) (INFLUENCE_SIZE * 1.05f / scale);
        trueOrderSize = (int) (INFLUENCE_SIZE / 1.25f / scale);
        trueVictorySize = (int) (VICTORY_SIZE / scale);
    }
}