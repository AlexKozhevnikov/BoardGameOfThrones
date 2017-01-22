package com.alexeus.graph;

import com.alexeus.graph.util.ImageLoader;
import com.alexeus.graph.util.PictureTormentor;
import com.alexeus.graph.enums.UnitPackType;
import com.alexeus.logic.Game;
import com.alexeus.logic.constants.MainConstants;
import com.alexeus.logic.enums.GamePhase;
import com.alexeus.logic.enums.Order;
import com.alexeus.logic.enums.TrackType;
import com.alexeus.logic.enums.UnitType;
import com.alexeus.logic.struct.Army;
import com.alexeus.logic.struct.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static com.alexeus.graph.constants.Constants.*;
import static com.alexeus.graph.enums.UnitPackType.*;
import static com.alexeus.logic.constants.MainConstants.*;
import static com.alexeus.map.GameOfThronesMap.NUM_AREA;

/**
 * Created by alexeus on 13.01.2017.
 * Панель, на которой расположена карта со всеми юнитами и жетонами
 */
@SuppressWarnings("serial")
public class MapPanel extends JPanel{

    private final float MIN_SCALE = 0.3f;

    private final float MAX_SCALE = 4.8f;

    private float scale = 2.5f;

    // Отступ нужен, чтобы центрировать карту
    private int indent = 0;

    private int UNIT_IMAGE_SIZE, HORIZONTAL_UNIT_INDENT, VERTICAL_UNIT_INDENT, HORIZONTAL_SHIP_INDENT, DEGREE_INDENT;
    private int DEFENCE_WIDTH, DEFENCE_HEIGHT, TOKEN_WIDTH, TOKEN_HEIGHT, INFLUENCE_SIZE,
            TRACK_VERTICAL_INDENT, TRACK_BEGIN_X[] = new int[NUM_TRACK], TRACK_BEGIN_Y,
            VICTORY_BEGIN_X, VICTORY_BEGIN_Y, VICTORY_VERTICAL_INDENT, VICTORY_SIZE,
            WILDLINGS_TOKEN_X, WILDLINGS_TOKEN_Y, WILDLINGS_TOKEN_WIDTH, WILDLINGS_TOKEN_HEIGHT, WILDLINGS_HORIZONTAL_INDENT,
            SUPPLY_BEGIN_X, SUPPLY_BEGIN_Y, SUPPLY_VERTICAL_INDENT, SUPPLY_WIDTH, SUPPLY_HEIGHT,
            TIME_BEGIN_X, TIME_BEGIN_Y, TIME_WIDTH, TIME_HEIGHT,
            TRACKS_AREA_X, TRACKS_AREA_Y, TRACKS_AREA_WIDTH, TRACKS_AREA_HEIGHT,
            SUPPLY_AREA_X, SUPPLY_AREA_Y, SUPPLY_AREA_WIDTH, SUPPLY_AREA_HEIGHT,
            VICTORY_AREA_X, VICTORY_AREA_Y, VICTORY_AREA_WIDTH, VICTORY_AREA_HEIGHT,
            WILDLING_AREA_X, WILDLING_AREA_Y, WILDLING_AREA_WIDTH, WILDLING_AREA_HEIGHT;
    private float TIME_VERTICAL_INDENT;
    private int trueUnitImageSize, trueHorizontalUnitIndent, trueVerticalUnitIndent, trueHorizontalShipIndent,
            trueDegreeIndent, trueDefenceWidth, trueDefenceHeight, trueInfluenceSize, trueTokenWidth, trueTokenHeight,
            trueOrderSize;

    private BufferedImage mapImage;

    private BufferedImage[] tokenImage = new BufferedImage[NUM_PLAYER];

    private BufferedImage[][] unitImage = new BufferedImage[NUM_PLAYER][NUM_UNIT_TYPES];

    private BufferedImage[][] woundUnitImage = new BufferedImage[NUM_PLAYER][NUM_UNIT_TYPES];

    private BufferedImage[] victoryImage = new BufferedImage[NUM_PLAYER];

    private BufferedImage[] supplyImage = new BufferedImage[NUM_PLAYER];

    private BufferedImage[] influenceImage = new BufferedImage[NUM_PLAYER];

    private BufferedImage[] defenceImage = new BufferedImage[MAX_DEFENCE + 1];

    private BufferedImage[] orderImage = new BufferedImage[NUM_DIFFERENT_ORDERS];

    private BufferedImage[] areaFillImage = new BufferedImage[NUM_AREA];

    private BufferedImage[][] areaFillImagePlayer = new BufferedImage[NUM_AREA][NUM_PLAYER];

    private BufferedImage wildlingTokenImage;

    private BufferedImage timeImage;

    private Dimension preferredSize;

    private AffineTransform heal, wound;

    private int defenceX[] = new int[NUM_AREA];
    private int defenceY[] = new int[NUM_AREA];
    private int tokenX[] = new int[NUM_AREA];
    private int tokenY[] = new int[NUM_AREA];
    private int areaBeginX[] = new int[NUM_AREA];
    private int areaBeginY[] = new int[NUM_AREA];
    private int armyX[] = new int[NUM_AREA];
    private int armyY[] = new int[NUM_AREA];
    private int orderX[] = new int[NUM_AREA];
    private int orderY[] = new int[NUM_AREA];
    private int attackerArmyX[] = new int[NUM_AREA];
    private int attackerArmyY[] = new int[NUM_AREA];
    private UnitPackType[] unitPackType = new UnitPackType[NUM_AREA];

    // Вспомогательные переменные
    private int[] xShift = new int[MAX_TROOPS_IN_AREA], yShift = new int[MAX_TROOPS_IN_AREA];
    private ArrayList<Integer> chosenPlayers = new ArrayList<>();

    MapPanel() {
        loadPics();
        loadVariables();
        setTrueSizes();
        preferredSize = new Dimension((int) (mapImage.getWidth(null) / scale),
                (int) (mapImage.getHeight(null) / scale));
        setPreferredSize(preferredSize);
    }

    @Override
    public void paintComponent(Graphics g) {
        Game game = Game.getInstance();
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        // Рисуем карту
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(mapImage, indent, 0, (int) (mapImage.getWidth(null) / scale), (int) (mapImage.getHeight(null) / scale), null);
        // окраска областей в цвета их владельцев
        Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
        g2d.setComposite(comp);
        for (int area = 0; area < NUM_AREA; area++) {
            int areaOwner = game.getAreaOwner(area);
            if (areaFillImage[area] != null && areaOwner >= 0) {
                if (areaFillImagePlayer[area][areaOwner] == null) {
                    areaFillImagePlayer[area][areaOwner] =
                            PictureTormentor.dye(areaFillImage[area], HOUSE_COLOR[areaOwner]);
                }
                g2d.drawImage(areaFillImagePlayer[area][areaOwner], indent + (int) (areaBeginX[area] / scale), (int) (areaBeginY[area] / scale),
                        (int) (areaFillImage[area].getWidth(null) / scale), (int) (areaFillImage[area].getHeight(null) / scale), null);
            }
        }
        // Рисуем гарнизоны, юниты, жетоны власти и приказы
        comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
        g2d.setComposite(comp);
        GamePhase gamePhase = game.getGamePhase();
        for (int area = 0; area < NUM_AREA; area++) {
            int areaOwner = game.getAreaOwner(area);
            int garrison = game.getGarrisonInArea(area);
            if (garrison > 0) {
                g2d.drawImage(defenceImage[garrison],
                        indent + (int) (defenceX[area] / scale), (int) (defenceY[area] / scale),
                        trueDefenceWidth, trueDefenceHeight, null);
            }
            // Жетон власти на области
            if (game.getPowerTokenInArea(area) >= 0) {
                g2d.drawImage(tokenImage[areaOwner], indent + (int) (tokenX[area] / scale),
                        (int) (tokenY[area] / scale), trueTokenWidth, trueTokenHeight, null);
            }
            // Приказы
            Order order = game.getOrderInArea(area);
            if (order != null) {
                if (order == Order.closed || gamePhase == GamePhase.planningPhase) {
                    g2d.drawImage(influenceImage[areaOwner], indent + (int) (orderX[area] / scale),
                            (int) (orderY[area] / scale), trueOrderSize, trueOrderSize, null);
                } else {
                    g2d.drawImage(orderImage[order.getCode()], indent + (int) (orderX[area] / scale),
                            (int) (orderY[area] / scale), trueOrderSize, trueOrderSize, null);
                }
            }
            // Войска
            Army army = game.getArmyInArea(area);
            int armySize = army.getNumUnits();
            if (armySize > 0) {
                fillShifts(army, area);
                ArrayList<Unit> units = army.getUnits();
                for (int index = 0; index < armySize; index++) {
                    g2d.drawImage(units.get(index).isWounded() ?
                            woundUnitImage[army.getOwner()][units.get(index).getUnitType().getCode()] :
                            unitImage[army.getOwner()][units.get(index).getUnitType().getCode()],
                            indent + xShift[index] + (int) (armyX[area] / scale), yShift[index] + (int) (armyY[area] / scale),
                            trueUnitImageSize, trueUnitImageSize, null);
                }
            }
        }
        // Рисуем юнитов-атакующих и юнитов-отступающих, если таковые имеются
        Army army = game.getAttackingArmy() != null ? game.getAttackingArmy() : (game.getRetreatingArmy() != null ?
                game.getRetreatingArmy() : null);
        if (army != null && !army.isEmpty()) {
            int area = game.getBattleArea();
            fillShifts(army, area);
            ArrayList<Unit> units = army.getUnits();
            for (int index = 0; index < army.getUnits().size(); index++) {
                g2d.drawImage(units.get(index).isWounded() ?
                        woundUnitImage[army.getOwner()][units.get(index).getUnitType().getCode()]:
                        unitImage[army.getOwner()][units.get(index).getUnitType().getCode()],
                        indent + xShift[index] + (int) (attackerArmyX[area] / scale),
                        yShift[index] + (int) (attackerArmyY[area] / scale),
                        trueUnitImageSize, trueUnitImageSize, null);
            }
        }

        // Рисуем жетон времени
        g2d.drawImage(timeImage,
                indent + (int) (TIME_BEGIN_X / scale),
                (int) ((TIME_BEGIN_Y - game.getTime() * TIME_VERTICAL_INDENT) / scale),
                (int) (TIME_WIDTH / scale), (int) (TIME_HEIGHT / scale), null);
        // Рисуем жетон одичалых
        g2d.drawImage(wildlingTokenImage,
                indent + (int) ((WILDLINGS_TOKEN_X +WILDLINGS_HORIZONTAL_INDENT * game.getWildlingsStrength() /
                        MainConstants.WILDLING_STRENGTH_INCREMENT) / scale), (int) ((WILDLINGS_TOKEN_Y) / scale),
                (int) (WILDLINGS_TOKEN_WIDTH / scale), (int) (WILDLINGS_TOKEN_HEIGHT / scale), null);
        // Рисуем победные очки
        int[] victoryPoints = game.getVictoryPoints();
        for (int victory = 1; victory <= MainConstants.NUM_CASTLES_TO_WIN; victory++) {
            chosenPlayers.clear();
            for (int player = 0; player < NUM_PLAYER; player++) {
                if (victoryPoints[player] == victory) {
                    chosenPlayers.add(player);
                }
            }
            for (int index = 0; index < chosenPlayers.size(); index++) {
                g2d.drawImage(victoryImage[chosenPlayers.get(index)],
                        indent + (int) ((VICTORY_BEGIN_X + TOKEN_INDENT_X[chosenPlayers.size()][index] * VICTORY_SIZE) / scale),
                        (int) ((VICTORY_BEGIN_Y + TOKEN_INDENT_Y[chosenPlayers.size()][index] * VICTORY_SIZE -
                                VICTORY_VERTICAL_INDENT * victory) / scale),
                        (int) (VICTORY_SIZE / scale), (int) (VICTORY_SIZE / scale), null);
            }
        }
        // Рисуем снабжение
        int[] supply = game.getSupply();
        for (int supplyLevel = 0; supplyLevel <= MAX_SUPPLY ; supplyLevel++) {
            chosenPlayers.clear();
            for (int player = 0; player < NUM_PLAYER; player++) {
                if (supply[player] == supplyLevel) {
                    chosenPlayers.add(player);
                }
            }
            for (int index = 0; index < chosenPlayers.size(); index++) {
                g2d.drawImage(supplyImage[chosenPlayers.get(index)],
                        indent + (int) ((SUPPLY_BEGIN_X + TOKEN_INDENT_X[chosenPlayers.size()][index] * SUPPLY_WIDTH) / scale),
                        (int) ((SUPPLY_BEGIN_Y + TOKEN_INDENT_Y[chosenPlayers.size()][index] * SUPPLY_HEIGHT -
                                SUPPLY_VERTICAL_INDENT * supplyLevel) / scale),
                        (int) (SUPPLY_WIDTH / scale), (int) (SUPPLY_HEIGHT / scale), null);
            }
        }
        // Рисуем треки влияния
        int playerOnPlace[];
        int curBiddingTrack = game.getCurrentBiddingTrack();
        g2d.setFont(new Font("Liberation Mono", Font.BOLD, (int) (70 / scale)));
        for (int track = 0; track < NUM_TRACK; track++) {
            playerOnPlace = game.getInfluenceTrackPlayerOnPlace(track);
            for (int place = 0; place < NUM_PLAYER; place++) {
                g2d.drawImage(influenceImage[playerOnPlace[place]],
                        indent + (int) (TRACK_BEGIN_X[track] / scale),
                        (int) ((TRACK_BEGIN_Y - place * TRACK_VERTICAL_INDENT) / scale),
                        trueInfluenceSize, trueInfluenceSize, null);
                if (curBiddingTrack == track) {
                    int bid = game.getCurrentBidOfPlayer(playerOnPlace[place]);
                    g2d.drawString(String.valueOf(bid),
                            indent + (int) ((TRACK_BEGIN_X[track] - bid < 10 ? BID_TEXT_X_INDENT : 2 * BID_TEXT_X_INDENT) / scale + trueInfluenceSize / 2f),
                            (int) ((TRACK_BEGIN_Y - place * TRACK_VERTICAL_INDENT + BID_TEXT_Y_INDENT) / scale + trueInfluenceSize / 2f));
                }
            }
        }
    }

    public void repaintArea(int area) {
        if (area >= 12 && area < 20) {
            area = Game.getInstance().getMap().getCastleWithPort(area);
        }
        System.out.println("Обновилась область: " + Game.getInstance().getMap().getAreaNameRus(area));
        repaint(new Rectangle((int) (indent + areaBeginX[area] / scale), (int) (areaBeginY[area] / scale),
                (int) (areaFillImage[area].getWidth() / scale), (int) (areaFillImage[area].getHeight() / scale)));
    }

    public void repaintTracks() {
        repaint(new Rectangle((int) (indent + TRACKS_AREA_X / scale), (int) (TRACKS_AREA_Y / scale),
                (int) (TRACKS_AREA_WIDTH  / scale), (int) (TRACKS_AREA_HEIGHT / scale)));
    }

    public void repaintSupply() {
        repaint(new Rectangle((int) (indent + SUPPLY_AREA_X / scale), (int) (SUPPLY_AREA_Y / scale),
                (int) (SUPPLY_AREA_WIDTH  / scale), (int) (SUPPLY_AREA_HEIGHT / scale)));
    }

    public void repaintVictory() {
        repaint(new Rectangle((int) (indent + VICTORY_AREA_X / scale), (int) (VICTORY_AREA_Y / scale),
                (int) (VICTORY_AREA_WIDTH  / scale), (int) (VICTORY_AREA_HEIGHT / scale)));
    }

    public void repaintWildlings() {
        repaint(new Rectangle((int) (indent + WILDLING_AREA_X / scale), (int) (WILDLING_AREA_Y / scale),
                (int) (WILDLING_AREA_WIDTH  / scale), (int) (WILDLING_AREA_HEIGHT / scale)));
    }

    /**
     * Метод вызывается при изменении масштаба путём прокрутки мышиного колеса. Изменяет масштаб и текущую локацию.
     * @param n количество квантов прокрутки мышиного колеса
     * @param p точка в которой был курсор в момент прокрутки мышиного колеса (относительно него будем центрировать)
     */
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
        } else {
            indent = 0;
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

    /**
     * Метод заполняет настоящие размеры элементов на карте с учётом масштаба
     */
    private void setTrueSizes() {
        trueUnitImageSize = (int) (UNIT_IMAGE_SIZE / scale);
        trueHorizontalShipIndent = (int) (HORIZONTAL_SHIP_INDENT / scale);
        trueHorizontalUnitIndent = (int) (HORIZONTAL_UNIT_INDENT / scale);
        trueVerticalUnitIndent = (int) (VERTICAL_UNIT_INDENT / scale);
        trueDegreeIndent = (int) (DEGREE_INDENT / scale);
        trueDefenceWidth = (int) (DEFENCE_WIDTH / scale);
        trueDefenceHeight = (int) (DEFENCE_HEIGHT / scale);
        trueTokenWidth = (int) (TOKEN_WIDTH / scale);
        trueTokenHeight = (int) (TOKEN_HEIGHT / scale);
        trueInfluenceSize = (int) (INFLUENCE_SIZE * 1.31f / scale);
        trueOrderSize = (int) (INFLUENCE_SIZE / scale);
    }

    /**
     * Метод заполняет вспомогательные массивы xShift, yShift для вывода армии в обпределённой области
     * @param army армия
     * @param area номер области
     */
    private void fillShifts(Army army, int area) {
        ArrayList<Unit> units = army.getUnits();
        int armySize = units.size();
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
                } else if (packType == port) {
                    xShift[index] = (int) (trueUnitImageSize * PORT_X_KOEF[armySize][index]);
                    yShift[index] = (int) (trueUnitImageSize * PORT_Y_KOEF[armySize][index]);
                } else {
                    System.out.println(area);
                }
            }
        }
    }

    private void loadPics() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        mapImage = imageLoader.getImage(MAP_FILE);
        wildlingTokenImage = imageLoader.getImage(WILDLING_TOKEN);
        timeImage = imageLoader.getImage(TIME);
        for(int defence = MIN_DEFENCE; defence <= MAX_DEFENCE; defence++) {
            defenceImage[defence] = imageLoader.getImage(DEFENCE + defence + PNG);
        }
        for(int orderCode = 0; orderCode < NUM_DIFFERENT_ORDERS; orderCode++) {
            orderImage[orderCode] = imageLoader.getImage(ORDER + orderCode + PNG);
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            tokenImage[player] = imageLoader.getImage(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + POWER);
            influenceImage[player] = imageLoader.getImage(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + INFLUENCE);
            victoryImage[player] = imageLoader.getImage(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + VICTORY);
            supplyImage[player] = imageLoader.getImage(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + SUPPLY);
            for (UnitType unitType: UnitType.values()) {
                unitImage[player][unitType.getCode()] = imageLoader.getImage(HOUSE_ENG[player] + "\\" +
                        HOUSE_ENG[player] + "_" + unitType.engName() + PNG);
                woundUnitImage[player][unitType.getCode()] =
                        PictureTormentor.getRotatedPicture(unitImage[player][unitType.getCode()]);
            }
        }
        for (int area = 0; area < NUM_AREA; area++) {
            if (area >= 12 && area < 20) continue;
            areaFillImage[area] = imageLoader.getImage(AREA + area + PNG);
        }
    }

    private void loadVariables() {
        UNIT_IMAGE_SIZE = unitImage[0][0].getWidth(null);
        INFLUENCE_SIZE = influenceImage[0].getWidth(null);
        TOKEN_WIDTH = tokenImage[0].getWidth(null);
        TOKEN_HEIGHT = tokenImage[0].getHeight(null);
        SUPPLY_WIDTH = supplyImage[0].getWidth(null);
        SUPPLY_HEIGHT = supplyImage[0].getHeight(null);
        VICTORY_SIZE = victoryImage[0].getWidth(null);
        WILDLINGS_TOKEN_WIDTH = wildlingTokenImage.getWidth(null);
        WILDLINGS_TOKEN_HEIGHT = wildlingTokenImage.getHeight(null);
        TIME_WIDTH = timeImage.getWidth(null);
        TIME_HEIGHT = timeImage.getHeight(null);
        DEFENCE_WIDTH = defenceImage[MIN_DEFENCE].getWidth(null);
        DEFENCE_HEIGHT = defenceImage[MIN_DEFENCE].getHeight(null);
        HORIZONTAL_SHIP_INDENT = (int) (UNIT_IMAGE_SIZE * 0.85);
        HORIZONTAL_UNIT_INDENT = (int) (UNIT_IMAGE_SIZE * 0.7);
        VERTICAL_UNIT_INDENT = (int) (UNIT_IMAGE_SIZE * 0.3);
        DEGREE_INDENT = (int) (UNIT_IMAGE_SIZE * 0.6);

        TRACK_BEGIN_X[0] = 1710;
        TRACK_BEGIN_X[1] = 1875;
        TRACK_BEGIN_X[2] = 2037;
        TRACK_BEGIN_Y = 934;
        TRACK_VERTICAL_INDENT = 167;
        WILDLINGS_TOKEN_X = 423;
        WILDLINGS_TOKEN_Y = 87;
        WILDLINGS_HORIZONTAL_INDENT = 114;
        TIME_BEGIN_X = 1736;
        TIME_BEGIN_Y = 3214;
        TIME_VERTICAL_INDENT = 96.4f;
        VICTORY_BEGIN_X = 2009;
        VICTORY_BEGIN_Y = 3277;
        VICTORY_VERTICAL_INDENT = 146;
        SUPPLY_BEGIN_X = 1806;
        SUPPLY_BEGIN_Y = 2039;
        SUPPLY_VERTICAL_INDENT = 107;

        TRACKS_AREA_X = 1670;
        TRACKS_AREA_Y = 0;
        TRACKS_AREA_WIDTH = 520;
        TRACKS_AREA_HEIGHT = 1070;
        SUPPLY_AREA_X = 1680;
        SUPPLY_AREA_Y = 1330;
        SUPPLY_AREA_WIDTH = 510;
        SUPPLY_AREA_HEIGHT = 850;
        VICTORY_AREA_X = 1920;
        VICTORY_AREA_Y = 2200;
        VICTORY_AREA_WIDTH = 270;
        VICTORY_AREA_HEIGHT = 1100;
        WILDLING_AREA_X = 410;
        WILDLING_AREA_Y = 70;
        WILDLING_AREA_WIDTH = 820;
        WILDLING_AREA_HEIGHT = 130;
        int portOrderIndent = (int) ((PORT_SIZE - INFLUENCE_SIZE) * 0.85);

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

        armyX[0] = 110; armyY[0] = 583;
        attackerArmyX[0] = 107; attackerArmyY[0] = 1065;
        areaBeginX[0] = 0; areaBeginY[0] = 0;
        unitPackType[0] = line;
        orderX[0] = 57; orderY[0] = 698;

        armyX[1] = 7; armyY[1] = 1357;
        attackerArmyX[1] = 7; attackerArmyY[1] = 1898;
        areaBeginX[1] = 0; areaBeginY[1] = 1163;
        unitPackType[1] = vertical;
        orderX[1] = 24; orderY[1] = 1182;

        armyX[2] = 107; armyY[2] = 1721;
        attackerArmyX[2] = 488; attackerArmyY[2] = 1508;
        areaBeginX[2] = 58; areaBeginY[2] = 1331;
        unitPackType[2] = triangleSquare;
        orderX[2] = 457; orderY[2] = 1613;

        armyX[3] = 146; armyY[3] = 1962;
        attackerArmyX[3] = 107; attackerArmyY[3] = 2070;
        areaBeginX[3] = 41; areaBeginY[3] = 1690;
        unitPackType[3] = triangleSquare;
        orderX[3] = 163; orderY[3] = 1859;

        armyX[4] = 391; armyY[4] = 3208;
        attackerArmyX[4] = 115; attackerArmyY[4] = 2374;
        areaBeginX[4] = 0; areaBeginY[4] = 2275;
        unitPackType[4] = line;
        orderX[4] = 460; orderY[4] = 3114;

        armyX[5] = 92; armyY[5] = 2752;
        attackerArmyX[5] = 159; attackerArmyY[5] = 2910;
        areaBeginX[5] = 44; areaBeginY[5] = 2451;
        unitPackType[5] = vertical;
        orderX[5] = 262; orderY[5] = 2503;

        armyX[6] = 1402; armyY[6] = 3113;
        attackerArmyX[6] = 1511; attackerArmyY[6] = 2654;
        areaBeginX[6] = 762; areaBeginY[6] = 2541;
        unitPackType[6] = line;
        orderX[6] = 1317; orderY[6] = 3167;

        armyX[7] = 1200; armyY[7] = 2748;
        attackerArmyX[7] = 1281; attackerArmyY[7] = 2722;
        areaBeginX[7] = 859; areaBeginY[7] = 2629;
        unitPackType[7] = line;
        orderX[7] = 977; orderY[7] = 2747;

        armyX[8] = 1480; armyY[8] = 2372;
        attackerArmyX[8] = 1470; attackerArmyY[8] = 2462;
        areaBeginX[8] = 1277; areaBeginY[8] = 1721;
        unitPackType[8] = line3square4;
        orderX[8] = 1388; orderY[8] = 2181;

        armyX[9] = 1226; armyY[9] = 2030;
        attackerArmyX[9] = 1216; attackerArmyY[9] = 2110;
        areaBeginX[9] = 1066; areaBeginY[9] = 1962;
        unitPackType[9] = triangleSquare;
        orderX[9] = 1160; orderY[9] = 2112;

        armyX[10] = 1471; armyY[10] = 1225;
        attackerArmyX[10] = 1475; attackerArmyY[10] = 1305;
        areaBeginX[10] = 795; areaBeginY[10] = 1003;
        unitPackType[10] = line;
        orderX[10] = 1488; orderY[10] = 1382;

        armyX[11] = 1455; armyY[11] = 717;
        attackerArmyX[11] = 1455; attackerArmyY[11] = 917;
        areaBeginX[11] = 1146; areaBeginY[11] = 205;
        unitPackType[11] = line;
        orderX[11] = 1354; orderY[11] = 783;

        armyX[12] = 391; armyY[12] = 508;
        armyX[13] = 327; armyY[13] = 1463;
        armyX[14] = 248; armyY[14] = 1874;
        armyX[15] = 155; armyY[15] = 2628;
        armyX[16] = 1422; armyY[16] = 2900;
        armyX[17] = 1239; armyY[17] = 2475;
        armyX[18] = 1481; armyY[18] = 2086;
        armyX[19] = 946; armyY[19] = 1135;
        for (int area = 12; area < 20; area++) {
            unitPackType[area] = port;
            orderX[area] = armyX[area] + portOrderIndent;
            orderY[area] = armyY[area] + portOrderIndent;
        }

        armyX[20] = 106; armyY[20] = 3083;
        attackerArmyX[20] = 178; attackerArmyY[20] = 3071;
        areaBeginX[20] = 77; areaBeginY[20] = 3027;
        unitPackType[20] = triangleSquare;
        orderX[20] = 185; orderY[20] = 3066;
        tokenX[20] = 106; tokenY[20] = 3163;

        armyX[21] = 698; armyY[21] = 933;
        attackerArmyX[21] = 837; attackerArmyY[21] = 612;
        areaBeginX[21] = 307; areaBeginY[21] = 430;
        unitPackType[21] = line;
        orderX[21] = 627; orderY[21] = 780;
        tokenX[21] = 681; tokenY[21] = 678;

        armyX[22] = 391; armyY[22] = 908;
        attackerArmyX[22] = 476; attackerArmyY[22] = 865;
        areaBeginX[22] = 173; areaBeginY[22] = 714;
        unitPackType[22] = line;
        orderX[22] = 311; orderY[22] = 1005;
        tokenX[22] = 442; tokenY[22] = 1037;

        armyX[23] = 993; armyY[23] = 338;
        attackerArmyX[23] = 800; attackerArmyY[23] = 390;
        areaBeginX[23] = 684; areaBeginY[23] = 241;
        unitPackType[23] = line3square4;
        orderX[23] = 1065; orderY[23] = 262;
        tokenX[23] = 845; tokenY[23] = 410;

        armyX[24] = 1200; armyY[24] = 516;
        attackerArmyX[24] = 1247; attackerArmyY[24] = 625;
        areaBeginX[24] = 1068; areaBeginY[24] = 408;
        unitPackType[24] = triangleSquare;
        orderX[24] = 1320; orderY[24] = 497;
        tokenX[24] = 1278; tokenY[24] = 655;

        armyX[25] = 953; armyY[25] = 910;
        attackerArmyX[25] = 1065; attackerArmyY[25] = 780;
        areaBeginX[25] = 833; areaBeginY[25] = 709;
        unitPackType[25] = line3square4;
        orderX[25] = 992; orderY[25] = 1050;
        tokenX[25] = 1095; tokenY[25] = 800;

        armyX[26] = 1136; armyY[26] = 915;
        attackerArmyX[26] = 1104; attackerArmyY[26] = 997;
        areaBeginX[26] = 1051; areaBeginY[26] = 865;
        unitPackType[26] = triangleSquare;
        orderX[26] = 1059; orderY[26] = 976;
        tokenX[26] = 1246; tokenY[26] = 992;

        armyX[27] = 694; armyY[27] = 1325;
        attackerArmyX[27] = 740; attackerArmyY[27] = 1231;
        areaBeginX[27] = 643; areaBeginY[27] = 1105;
        unitPackType[27] = line3square4;
        orderX[27] = 702; orderY[27] = 1216;
        tokenX[27] = 818; tokenY[27] = 1257;

        armyX[28] = 800; armyY[28] = 1486;
        attackerArmyX[28] = 777; attackerArmyY[28] = 1524;
        areaBeginX[28] = 690; areaBeginY[28] = 1425;
        unitPackType[28] = line;
        orderX[28] = 861; orderY[28] = 1444;
        tokenX[28] = 808; tokenY[28] = 1597;

        armyX[29] = 1046; armyY[29] = 1435;
        attackerArmyX[29] = 1031; attackerArmyY[29] = 1485;
        areaBeginX[29] = 945; areaBeginY[29] = 1352;
        unitPackType[29] = line;
        orderX[29] = 1153; orderY[29] = 1390;
        tokenX[29] = 1023; tokenY[29] = 1527;

        armyX[30] = 1192; armyY[30] = 1578;
        attackerArmyX[30] = 937; attackerArmyY[30] = 1679;
        areaBeginX[30] = 820; areaBeginY[30] = 1462;
        unitPackType[30] = line3square4;
        orderX[30] = 1264; orderY[30] = 1519;
        tokenX[30] = 1030; tokenY[30] = 1770;

        armyX[31] = 1140; armyY[31] = 1726;
        attackerArmyX[31] = 1211; attackerArmyY[31] = 1688;
        areaBeginX[31] = 1057; areaBeginY[31] = 1654;
        unitPackType[31] = line;
        orderX[31] = 1232; orderY[31] = 1718;
        tokenX[31] = 1248; tokenY[31] = 1816;

        armyX[32] = 306; armyY[32] = 1298;
        attackerArmyX[32] = 256; attackerArmyY[32] = 1330;
        areaBeginX[32] = 181; areaBeginY[32] = 1201;
        unitPackType[32] = horizontal;
        orderX[32] = 387; orderY[32] = 1223;
        tokenX[32] = 222; tokenY[32] = 1355;

        armyX[33] = 520; armyY[33] = 1281;
        attackerArmyX[33] = 579; attackerArmyY[33] = 1217;
        areaBeginX[33] = 450; areaBeginY[33] = 1164;
        unitPackType[33] = line3square4;
        orderX[33] = 574; orderY[33] = 1200;
        tokenX[33] = 613; tokenY[33] = 1351;

        armyX[34] = 623; armyY[34] = 1509;
        attackerArmyX[34] = 652; attackerArmyY[34] = 1560;
        areaBeginX[34] = 515; areaBeginY[34] = 1396;
        unitPackType[34] = degree;
        orderX[34] = 558; orderY[34] = 1406;
        tokenX[34] = 725; tokenY[34] = 1623;

        armyX[35] = 666; armyY[35] = 1740;
        attackerArmyX[35] = 608; attackerArmyY[35] = 1809;
        areaBeginX[35] = 548; areaBeginY[35] = 1655;
        unitPackType[35] = line3square4;
        orderX[35] = 746; orderY[35] = 1698;
        tokenX[35] = 616; tokenY[35] = 1795;

        armyX[36] = 422; armyY[36] = 1880;
        attackerArmyX[36] = 406; attackerArmyY[36] = 1992;
        areaBeginX[36] = 216; areaBeginY[36] = 1768;
        unitPackType[36] = line3square4;
        orderX[36] = 382; orderY[36] = 1947;
        tokenX[36] = 460; tokenY[36] = 1995;

        armyX[37] = 598; armyY[37] = 1986;
        attackerArmyX[37] = 643; attackerArmyY[37] = 1944;
        areaBeginX[37] = 508; areaBeginY[37] = 1832;
        unitPackType[37] = line;
        orderX[37] = 697; orderY[37] = 1900;
        tokenX[37] = 582; tokenY[37] = 2080;

        armyX[38] = 830; armyY[38] = 1886;
        attackerArmyX[38] = 855; attackerArmyY[38] = 1833;
        areaBeginX[38] = 747; areaBeginY[38] = 1800;
        unitPackType[38] = line;
        orderX[38] = 873; orderY[38] = 1831;
        tokenX[38] = 908; tokenY[38] = 1990;

        armyX[39] = 746; armyY[39] = 2145;
        attackerArmyX[39] = 717; attackerArmyY[39] = 2196;
        areaBeginX[39] = 510; areaBeginY[39] = 2019;
        unitPackType[39] = line;
        orderX[39] = 596; orderY[39] = 2228;
        tokenX[39] = 835; tokenY[39] = 2126;

        armyX[40] = 357; armyY[40] = 2212;
        attackerArmyX[40] = 385; attackerArmyY[40] = 2150;
        areaBeginX[40] = 260; areaBeginY[40] = 2067;
        unitPackType[40] = line;
        orderX[40] = 290; orderY[40] = 2271;
        tokenX[40] = 462; tokenY[40] = 2323;

        armyX[41] = 379; armyY[41] = 2428;
        attackerArmyX[41] = 400; attackerArmyY[41] = 2390;
        areaBeginX[41] = 262; areaBeginY[41] = 2358;
        unitPackType[41] = line;
        orderX[41] = 441; orderY[41] = 2513;
        tokenX[41] = 464; tokenY[41] = 2589;

        armyX[42] = 630; armyY[42] = 2390;
        attackerArmyX[42] = 780; attackerArmyY[42] = 2380;
        areaBeginX[42] = 516; areaBeginY[42] = 2235;
        unitPackType[42] = line;
        orderX[42] = 750; orderY[42] = 2354;
        tokenX[42] = 894; tokenY[42] = 2418;

        armyX[43] = 586; armyY[43] = 2585;
        attackerArmyX[43] = 710; attackerArmyY[43] = 2550;
        areaBeginX[43] = 469; areaBeginY[43] = 2508;
        unitPackType[43] = line3square4;
        orderX[43] = 734; orderY[43] = 2552;
        tokenX[43] = 528; tokenY[43] = 2681;

        armyX[44] = 417; armyY[44] = 2851;
        attackerArmyX[44] = 464; attackerArmyY[44] = 2801;
        areaBeginX[44] = 292; areaBeginY[44] = 2725;
        unitPackType[44] = line;
        orderX[44] = 385; orderY[44] = 2940;
        tokenX[44] = 368; tokenY[44] = 2931;

        armyX[45] = 330; armyY[45] = 2733;
        attackerArmyX[45] = 389; attackerArmyY[45] = 2655;
        areaBeginX[45] = 148; areaBeginY[45] = 2619;
        unitPackType[45] = triangleSquare;
        orderX[45] = 232; orderY[45] = 2780;
        tokenX[45] = 350; tokenY[45] = 2674;

        armyX[46] = 623; armyY[46] = 2992;
        attackerArmyX[46] = 643; attackerArmyY[46] = 2942;
        areaBeginX[46] = 548; areaBeginY[46] = 2911;
        unitPackType[46] = line;
        orderX[46] = 763; orderY[46] = 3005;
        tokenX[46] = 692; tokenY[46] = 2974;

        armyX[47] = 1070; armyY[47] = 2968;
        attackerArmyX[47] = 926; attackerArmyY[47] = 2998;
        areaBeginX[47] = 854; areaBeginY[47] = 2929;
        unitPackType[47] = line;
        orderX[47] = 913; orderY[47] = 2989;
        tokenX[47] = 1272; tokenY[47] = 3018;

        armyX[48] = 1159; armyY[48] = 2897;
        attackerArmyX[48] = 1282; attackerArmyY[48] = 2916;
        areaBeginX[48] = 1053; areaBeginY[48] = 2844;
        unitPackType[48] = horizontal;
        orderX[48] = 1282; orderY[48] = 2895;
        tokenX[48] = 1410; tokenY[48] = 2875;

        armyX[49] = 832; armyY[49] = 2862;
        attackerArmyX[49] = 787; attackerArmyY[49] = 2900;
        areaBeginX[49] = 723; areaBeginY[49] = 2832;
        unitPackType[49] = line;
        orderX[49] = 952; orderY[49] = 2863;
        tokenX[49] = 838; tokenY[49] = 2942;

        armyX[50] = 623; armyY[50] = 2736;
        attackerArmyX[50] = 645; attackerArmyY[50] = 2826;
        areaBeginX[50] = 556; areaBeginY[50] = 2643;
        unitPackType[50] = line3square4;
        orderX[50] = 625; orderY[50] = 2804;
        tokenX[50] = 738; tokenY[50] = 2684;

        armyX[51] = 886; armyY[51] = 2628;
        attackerArmyX[51] = 820; attackerArmyY[51] = 2728;
        areaBeginX[51] = 754; areaBeginY[51] = 2526;
        unitPackType[51] = line3square4;
        orderX[51] = 798; orderY[51] = 2747;
        tokenX[51] = 1024; tokenY[51] = 2572;

        armyX[52] = 1125; armyY[52] = 2500;
        attackerArmyX[52] = 1209; attackerArmyY[52] = 2580;
        areaBeginX[52] = 1075; areaBeginY[52] = 2353;
        unitPackType[52] = triangleSquare;
        orderX[52] = 1196; orderY[52] = 2585;
        tokenX[52] = 1210; tokenY[52] = 2455;

        armyX[53] = 1151; armyY[53] = 2315;
        attackerArmyX[53] = 1025; attackerArmyY[53] = 2411;
        areaBeginX[53] = 945; areaBeginY[53] = 2093;
        unitPackType[53] = triangleSquare;
        orderX[53] = 1057; orderY[53] = 2385;
        tokenX[53] = 1268; tokenY[53] = 2274;

        armyX[54] = 994; armyY[54] = 2160;
        attackerArmyX[54] = 1014; attackerArmyY[54] = 2246;
        areaBeginX[54] = 960; areaBeginY[54] = 2077;
        unitPackType[54] = triangleSquare;
        orderX[54] = 1029; orderY[54] = 2258;
        tokenX[54] = 1147; tokenY[54] = 2260;

        armyX[55] = 1039; armyY[55] = 1923;
        attackerArmyX[55] = 1035; attackerArmyY[55] = 1977;
        areaBeginX[55] = 918; areaBeginY[55] = 1828;
        unitPackType[55] = line;
        orderX[55] = 975; orderY[55] = 1990;
        tokenX[55] = 1177; tokenY[55] = 1965;

        armyX[56] = 1468; armyY[56] = 1998;
        attackerArmyX[56] = 1500; attackerArmyY[56] = 1927;
        areaBeginX[56] = 1371; areaBeginY[56] = 1883;
        unitPackType[56] = line;
        orderX[56] = 1473; orderY[56] = 1915;
        tokenX[56] = 1461; tokenY[56] = 1983;

        armyX[57] = 194; armyY[57] = 1565;
        attackerArmyX[57] = 235; attackerArmyY[57] = 1639;
        areaBeginX[57] = 112; areaBeginY[57] = 1460;
        unitPackType[57] = line;
        orderX[57] = 320; orderY[57] = 1600;
        tokenX[57] = 287; tokenY[57] = 1593;

        for (int area = 0; area < NUM_AREA; area++) {
            // Поправляем по центру картинки гарнизонов
            if (defenceX[area] > 0) {
                defenceX[area] -= defenceImage[2].getWidth(null) / 2;
                defenceY[area] -= defenceImage[2].getHeight(null) / 2;
            }
            if (tokenX[area] > 0) {
                tokenX[area] -= tokenImage[0].getWidth(null) / 2;
                tokenY[area] -= tokenImage[0].getHeight(null) / 2;
            }
        }

        heal = new AffineTransform();
        wound = new AffineTransform();
        wound.rotate(Math.toRadians(90.0));
        heal.rotate(Math.toRadians(-90.0));
    }
}