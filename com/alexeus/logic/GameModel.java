package com.alexeus.logic;

import com.alexeus.control.Controller;
import com.alexeus.control.Settings;
import com.alexeus.control.enums.GameStatus;
import com.alexeus.graph.MapPanel;
import com.alexeus.graph.enums.TabEnum;
import com.alexeus.graph.tab.EventTabPanel;
import com.alexeus.graph.tab.FightTabPanel;
import com.alexeus.graph.tab.HouseTabPanel;
import com.alexeus.graph.tab.LeftTabPanel;
import com.alexeus.logic.enums.*;
import com.alexeus.logic.struct.*;
import com.alexeus.map.*;
import com.alexeus.util.LittleThings;

import javax.swing.*;
import java.util.*;

import static com.alexeus.logic.constants.MainConstants.*;
import static com.alexeus.logic.constants.TextErrors.*;
import static com.alexeus.logic.constants.TextInfo.*;
import static com.alexeus.map.GameOfThronesMap.NUM_AREA;

/**
 * Created by alexeus on 03.01.2017.
 * Макет игры, в котором содержатся переменные партии и методы для их обработки.
 */
public class GameModel {

    // Все 3 колоды событий в одном объекте
    private ArrayList<LinkedList<Happenable>> decks;

    private LinkedList<WildlingCard> wildlingDeck;

    private HouseCard[][] houseCardOfPlayer;

    private GameOfThronesMap map;

    private Random random;

    // текущая сила одичалых
    private int wildlingsStrength;

    private int preemptiveRaidCheater;

    // события текущего раунда
    private Happenable[] event = new Happenable[NUM_EVENT_DECKS];
    // Запрещённый в данном раунде приказ
    private OrderType forbiddenOrder;
    // последняя сыгранная карта одичалых
    private WildlingCard topWildlingCard;

    // Карты двух конфликтующих Домов в последней битве
    private HouseCard houseCardOfSide[] = new HouseCard[2];

    private int[] playerOnSide = new int[2];

    /*
     * Массив приказов, которые сейчас лежат в данных областях карты
     */
    private Order[] orderInArea = new Order[NUM_AREA];

    /*
     * Массив из армий, находящихся в каждой из областей карты. Пустые при инициализации.
     */
    private Army[] armyInArea = new Army[NUM_AREA];

    /*
     * Здесь хранится армия, атаковавшая другого игрока, и поэтому временно находящаяся на территории врага
     */
    private Army attackingArmy;

    /*
     * Здесь хранится армия, защищавшая область и проигравшая, и поэтому временно находящаяся на территории врага.
     * После отступления обнуляется.
     */
    private Army retreatingArmy;

    /*
     * Массив из гарнизонов в областях. Нейтральный лорд, если при этом в области нет армий игроков
     */
    private int[] garrisonInArea = new int[NUM_AREA];

    /*
     * Массив из жетонов власти, лежащих на областях.
     * Если на области нет жетона власти, то соответстующая переменная равна -1.
     */
    private int[] powerTokenOnArea = new int[NUM_AREA];

    // Текущее снабжение каждого дома
    private int[] supply = new int[NUM_PLAYER];

    // Текущее количество замков у каждого дома
    private int[] victoryPoints = new int[NUM_PLAYER];

    // Количество крепостей (двухэтажных замков) у каждого дома. Заполняется только в конце игры.
    private int[] numFortress = new int[NUM_PLAYER];

    // Число доступных жетонов власти каждого дома
    private int[] nPowerTokensHouse = new int[NUM_PLAYER];

    /*
     * Максимальное число доступных в обозримом будущем жетонов власти. Отличается от MainConstants.MAX_TOKENS,
     * потому что жетоны власти игрока, лежащие на областях карты, могут быть получены обратно только
     * при завоевании области другим игроком
     */
    private int[] maxPowerTokensHouse = new int[NUM_PLAYER];

    /*
     * Престольная земля - это область, на которой есть фамильный герб; если эту область оставит захватчик, она снова
     * перейдёт под контроль дома, чьей престольной землёй она является.
     */
    private int[] houseHomeLandInArea = new int[NUM_AREA];

    // Массивы, хранящие необходимые сведения о позициях по треку влияния
    private int[][] trackPlayerOnPlace = new int[NUM_TRACK][NUM_PLAYER];
    private int[][] trackPlaceForPlayer = new int[NUM_TRACK][NUM_PLAYER];
    // Был ли использован меч в этом раунде
    private boolean isSwordUsed;

    /*
     * Количества юнитов в резерве у каждого из домов
     */
    private int[][] restingUnitsOfPlayerAndType = new int[NUM_PLAYER][NUM_UNIT_TYPES];

    /*
     * Максимальное количество юнитов у каждого из домов
     */
    private int[][] maxUnitsOfPlayerAndType = new int[NUM_PLAYER][NUM_UNIT_TYPES];

    /*
     * **** Вспомогательные переменные для методов ****
     */

    private int areaOfBattle, areaOfMarch;

    private int winnerSide, winner, loser;

    // Информация о последнем случившемся бое
    private BattleInfo battleInfo;

    private HashSet<Integer> areasToRetreat = new HashSet<>();

    private HashSet<Integer> supporters = new HashSet<>();

    private ArrayList<Integer> areasOfSupport = new ArrayList<>();

    private SideOfBattle[] supportOfPlayer = new SideOfBattle[NUM_PLAYER];

    // Количество разыгранных приказов с определённым кодом; нужно для валидации приказов каждого игрока
    private int[] nOrdersWithCode = new int[NUM_DIFFERENT_ORDERS];

    // Текущие ставки
    private int[] currentBids = new int[NUM_PLAYER];
    // Трек текущих ставок
    private int currentBidTrack;

    /*
     * Список карт, "область, где есть войска данного игрока"-"количество юнитов". Меняется в течении игры каждый раз,
     * когда войска игрока перемещаются, захватывают или отступают в новую область. Также важен при учёте снабжения.
     */
    private ArrayList<HashMap<Integer, Integer>> areasWithTroopsOfPlayer = new ArrayList<>();

    /*
     * Список множеств, состоящих из областей, где есть набеги данного игрока.
     */
    private ArrayList<HashSet<Integer>> areasWithRaids = new ArrayList<>();

    /*
     * Список множеств, состоящих из областей, где есть походы данного игрока.
     */
    private ArrayList<HashSet<Integer>> areasWithMarches = new ArrayList<>();

    /*
     * Список множеств, состоящих из областей, где есть сборы власти данного игрока.
     */
    private ArrayList<HashSet<Integer>> areasWithCPs = new ArrayList<>();

    /*
     * В этом вспомогательном массиве хранятся количества активных карт каждого игроков
     */
    private int[] numActiveHouseCardsOfPlayer = new int[NUM_PLAYER];

    private HouseCard temporaryInactiveCard;

    /*
     * Вспомогательное множество областей
     */
    private HashSet<Integer> accessibleAreaSet = new HashSet<>();

    /*
     * Вспомогательная карта жрунов снабжения. Используется при валидации походов
     */
    private HashMap<Integer, Integer> virtualAreasWithTroops = new HashMap<>();

    /*
     * Количество оставшихся карт событий определённого типа
     */
    private HashMap<Happenable, Integer> numRemainingCards = new HashMap<>();

    private MapPanel mapPanel;

    private LeftTabPanel tabPanel;

    private HouseTabPanel houseTabPanel;

    private FightTabPanel fightTabPanel;

    private EventTabPanel eventTabPanel;

    private JTextArea chat;

    public GameModel() {
        map = new GameOfThronesMap();
        random = new Random();
    }

    public void prepareNewGame(InitialPosition ini) {
        for (int i = 0; i < NUM_PLAYER; i++) {
            currentBids[i] = 0;
            numFortress[i] = 0;
        }
        for (int area = 0; area < NUM_AREA; area++) {
            powerTokenOnArea[area] = -1;
            houseHomeLandInArea[area] = -1;
            armyInArea[area] = new Army();
            orderInArea[area] = null;
        }
        battleInfo = null;
        attackingArmy = new Army();
        retreatingArmy = new Army();
        isSwordUsed = false;
        areaOfBattle = -1;
        areaOfMarch = -1;
        currentBidTrack = -1;
        preemptiveRaidCheater = -1;
        forbiddenOrder = null;
        for (int i = 0; i < 2; i++) {
            houseCardOfSide[i] = null;
            playerOnSide[i] = -1;
        }
        decks = new ArrayList<>();
        for (int deckNumber = 0; deckNumber < NUM_EVENT_DECKS; deckNumber++) {
            decks.add(new LinkedList<>());
            event[deckNumber] = null;
        }
        wildlingDeck = new LinkedList<>();
        topWildlingCard = null;

        // Инициализация вспомогательных множеств
        areasWithTroopsOfPlayer.clear();
        areasWithRaids.clear();
        areasWithMarches.clear();
        areasWithCPs.clear();
        for (int player = 0; player < NUM_PLAYER; player++) {
            areasWithTroopsOfPlayer.add(new HashMap<>());
            areasWithRaids.add(new HashSet<>());
            areasWithMarches.add(new HashSet<>());
            areasWithCPs.add(new HashSet<>());
        }
        // готовим карты домов и инициализируем колоды событий и одичалых
        houseCardOfPlayer = new HouseCard[NUM_PLAYER][NUM_HOUSE_CARDS];
        initializeDecks();
        // Ставим начальные войска/гарнизоны и обновляем информацию во вспомогательных множествах
        switch(ini) {
            case standard:
                setInitialPosition();
                break;
            case trash:
                setTrashPosition();
        }
        renewHousesTroopsInAllAreas();
        adjustVictoryPoints();
        adjustSupply();
    }


    /* Метод устанавливает начальную позицию Игры престолов II редакции. */
    private void setInitialPosition() {
        // Баратеон
        armyInArea[8].addUnit(UnitType.ship, 0);
        armyInArea[8].addUnit(UnitType.ship, 0);
        armyInArea[53].addUnit(UnitType.pawn, 0);
        armyInArea[56].addUnit(UnitType.knight, 0);
        armyInArea[56].addUnit(UnitType.pawn, 0);

        // Ланнистер
        armyInArea[3].addUnit(UnitType.ship, 1);
        armyInArea[37].addUnit(UnitType.pawn, 1);
        armyInArea[36].addUnit(UnitType.knight, 1);
        armyInArea[36].addUnit(UnitType.pawn, 1);

        // Старк
        armyInArea[11].addUnit(UnitType.ship, 2);
        armyInArea[25].addUnit(UnitType.pawn, 2);
        armyInArea[21].addUnit(UnitType.knight, 2);
        armyInArea[21].addUnit(UnitType.pawn, 2);

        // Мартелл
        armyInArea[7].addUnit(UnitType.ship, 3);
        armyInArea[47].addUnit(UnitType.pawn, 3);
        armyInArea[48].addUnit(UnitType.knight, 3);
        armyInArea[48].addUnit(UnitType.pawn, 3);

        // Грейджой
        armyInArea[2].addUnit(UnitType.ship, 4);
        armyInArea[13].addUnit(UnitType.ship, 4);
        armyInArea[33].addUnit(UnitType.pawn, 4);
        armyInArea[57].addUnit(UnitType.knight, 4);
        armyInArea[57].addUnit(UnitType.pawn, 4);

        // Тирелл
        armyInArea[5].addUnit(UnitType.ship, 5);
        armyInArea[43].addUnit(UnitType.pawn, 5);
        armyInArea[41].addUnit(UnitType.knight, 5);
        armyInArea[41].addUnit(UnitType.pawn, 5);

        // Баловство
        /* armyInArea[39].addUnit(UnitType.knight, 1);
        armyInArea[39].addUnit(UnitType.knight, 1);
        armyInArea[39].addUnit(UnitType.knight, 1);
        armyInArea[50].addUnit(UnitType.knight, 1);
        armyInArea[49].addUnit(UnitType.knight, 2);
        armyInArea[49].addUnit(UnitType.knight, 2);
        armyInArea[49].addUnit(UnitType.knight, 2);
        armyInArea[30].addUnit(UnitType.knight, 5);
        armyInArea[30].addUnit(UnitType.knight, 5);
        armyInArea[30].addUnit(UnitType.knight, 5);
        armyInArea[28].addUnit(UnitType.knight, 5); */

        // TODO обнулить гарнизоны и даомашние клумбы
        // Нейтральные лорды
        garrisonInArea[31] = 6;
        garrisonInArea[54] = 5;
        // Гарнизоны
        houseHomeLandInArea[56] = 0;
        garrisonInArea[56] = 2;
        houseHomeLandInArea[36] = 1;
        garrisonInArea[36] = 2;
        houseHomeLandInArea[21] = 2;
        garrisonInArea[21] = 2;
        houseHomeLandInArea[48] = 3;
        garrisonInArea[48] = 2;
        houseHomeLandInArea[57] = 4;
        garrisonInArea[57] = 2;
        houseHomeLandInArea[41] = 5;
        garrisonInArea[41] = 2;
        // Дикие люди
        wildlingsStrength = 2;
        // Устанавливаем начальные количаства жетонов власти
        for (int player = 0; player < NUM_PLAYER; player++) {
            nPowerTokensHouse[player] = INITIAL_TOKENS;
            maxPowerTokensHouse[player] = MAX_TOKENS;
            System.arraycopy(MAX_NUM_OF_UNITS, 0, maxUnitsOfPlayerAndType[player], 0, NUM_UNIT_TYPES);
        }
        // Устанавливаем начальные позиции на треках влияния
        for (int track = 0; track < NUM_TRACK; track++) {
            System.arraycopy(INITIAL_INFLUENCE_TRACKS_PLAYER_ON_PLACE[track], 0, trackPlayerOnPlace[track], 0, NUM_PLAYER);
            fillTrackPlaceForPlayer(track);
        }
    }

    private void setTrashPosition() {
        houseHomeLandInArea[56] = 0;
        garrisonInArea[56] = 3;
        houseHomeLandInArea[36] = 1;
        garrisonInArea[36] = 3;
        houseHomeLandInArea[21] = 2;
        garrisonInArea[21] = 3;
        houseHomeLandInArea[48] = 3;
        garrisonInArea[48] = 3;
        houseHomeLandInArea[57] = 4;
        garrisonInArea[57] = 3;
        houseHomeLandInArea[41] = 5;
        garrisonInArea[41] = 3;
        wildlingsStrength = 10;
        for (int i = 0; i < NUM_AREA; i++) {
            armyInArea[i].deleteAllUnits();
            if (i >= 12 && i < 20) {
                continue;
            }
            int owner = houseHomeLandInArea[i] >= 0 ? houseHomeLandInArea[i] : random.nextInt(NUM_PLAYER);
            for (int j = 0; j < 3; j++) {
                armyInArea[i].addUnit(map.getAreaType(i).isNaval() ? UnitType.ship :
                        random.nextFloat() > 0.7 ? UnitType.knight : UnitType.pawn, owner);
            }
        }
        for (int i = 12; i < 20; i++) {
            int castleOwner = getAreaOwner(map.getCastleWithPort(i));
            if (castleOwner >= 0) {
                armyInArea[i].addUnit(UnitType.ship, castleOwner);
            }
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            maxUnitsOfPlayerAndType[player][UnitType.pawn.getCode()] = 20;
            maxUnitsOfPlayerAndType[player][UnitType.ship.getCode()] = 20;
            maxUnitsOfPlayerAndType[player][UnitType.siegeEngine.getCode()] = 2;
            maxUnitsOfPlayerAndType[player][UnitType.knight.getCode()] = 10;
            for (int card = 0; card < NUM_HOUSE_CARDS; card++) {
                boolean isActive = random.nextBoolean();
                houseCardOfPlayer[player][card].setActive(isActive);
                if (!isActive) {
                    numActiveHouseCardsOfPlayer[player]--;
                }
            }
            if (numActiveHouseCardsOfPlayer[player] == 0) {
                int activeCardIndex = random.nextInt(NUM_HOUSE_CARDS);
                houseCardOfPlayer[player][activeCardIndex].setActive(true);
                numActiveHouseCardsOfPlayer[player] = 1;
            }
        }
        // Устанавливаем начальные количаства жетонов власти
        for (int player = 0; player < NUM_PLAYER; player++) {
            maxPowerTokensHouse[player] = MAX_TOKENS;
            nPowerTokensHouse[player] = random.nextInt(MAX_TOKENS / 2) + 3;
        }
        // Устанавливаем начальные позиции на треках влияния
        for (int track = 0; track < NUM_TRACK; track++) {
            boolean[] isChosen = new boolean[NUM_PLAYER];
            for (int place = 0; place < NUM_PLAYER; place++) {
                int aimBet = random.nextInt(NUM_PLAYER - place);
                int curBet = 0;
                for (int player = 0; player < NUM_PLAYER; player++) {
                    if (!isChosen[player]) {
                        if (aimBet == curBet) {
                            isChosen[player] = true;
                            trackPlayerOnPlace[track][place] = player;
                            break;
                        } else {
                            curBet++;
                        }
                    }
                }
            }
            fillTrackPlaceForPlayer(track);
        }
    }

    /**
     * Метод обновляет значения areasWithTroopsOfPlayer для всех игроков
     */
    private void renewHousesTroopsInAllAreas() {
        for (int player = 0; player < NUM_PLAYER; player++) {
            System.arraycopy(maxUnitsOfPlayerAndType[player], 0, restingUnitsOfPlayerAndType[player], 0, NUM_UNIT_TYPES);
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            areasWithTroopsOfPlayer.get(player).clear();
        }
        for (int area = 0; area < NUM_AREA; area++) {
            int troopsOwner = armyInArea[area].getOwner();
            if(troopsOwner >= 0) {
                areasWithTroopsOfPlayer.get(troopsOwner).put(area, armyInArea[area].getSize());
                for (UnitType unitType: UnitType.values()) {
                    restingUnitsOfPlayerAndType[troopsOwner][unitType.getCode()] -= armyInArea[area].getNumUnitOfType(unitType);
                }
            }
        }
    }

    /**
     * Добавляет данную область во вспомогательную коллекцию areasWithTroopsOfPlayer, если там есть отряды
     * @param area номер области
     */
    private void renewHouseTroopsInArea(int area) {
        if (armyInArea[area].getOwner() >= 0) {
            areasWithTroopsOfPlayer.get(armyInArea[area].getOwner()).put(area, armyInArea[area].getSize());
        }
    }

    /**
     * Метод считает и изменяет количество замков у каждого из игроков.
     * Кроме того, должен вызываться каждый раз, когда число замков какого-то из игроков
     * меняется в положительную сторону, поскольку нужно отслеживать условие досрочной победы.
     */
    void adjustVictoryPoints() {
        for (int player = 0; player < NUM_PLAYER; player++) {
            victoryPoints[player] = 0;
        }
        for (int area = 0; area < NUM_AREA; area++) {
            int areaOwner = getAreaOwner(area);
            if (areaOwner >= 0 && map.getNumCastle(area) > 0) {
                victoryPoints[areaOwner]++;
            }
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (victoryPoints[player] >= 7) {
                // Если данная модель - основная, то инициируем досрочное завершение потока игры
                if (this == Game.getInstance().getModel()) {
                    fillNumFortress();
                    Controller.getInstance().setGameStatus(GameStatus.end);
                    synchronized (Controller.getControllerMonitor()) {
                        Controller.getControllerMonitor().notify();
                    }
                }
            }
        }
        mapPanel.repaintVictory();
    }

    public void fillNumFortress() {
        for (int area = 0; area < NUM_AREA; area++) {
            if (map.getNumCastle(area) == 2 && getAreaOwner(area) >= 0) {
                numFortress[getAreaOwner(area)]++;
            }
        }
    }

    /**
     * Метод проверяет приказы, полученные от игрока, на соответсвие правилам
     * @param orders карта область-приказ, полученная от игрока
     * @param player номер игрока
     * @return true, если приказы валидны
     */
    boolean validateOrders(HashMap<Integer, Order> orders, int player) {
        boolean alright = true;
        StringBuilder sb = new StringBuilder();
        int nStarOrders = 0;
        // Обнуляем количества приказов определённого типа
        for (int orderCode = 0; orderCode < NUM_DIFFERENT_ORDERS; orderCode++) {
            nOrdersWithCode[orderCode] = 0;
        }
        // Проверяем каждый приказ отдельно
        for (Map.Entry<Integer, Order> entry : orders.entrySet()) {
            int areaOfOrder = entry.getKey();
            Order order = entry.getValue();
            // Сперва проверяем, что в заявленной области действительно есть войска данного игрока
            if (!areasWithTroopsOfPlayer.get(player).containsKey(areaOfOrder)) {
                if (!alright) sb.append("\n");
                sb.append(IN_AREA_NUMBER).append(areaOfOrder)
                        .append(NO_TROOPS_OF).append(HOUSE_GENITIVE[player]);
                alright = false;
                continue;
            }

            nOrdersWithCode[order.getCode()]++;
            if (order.isStar()) {
                nStarOrders++;
            }
            // В морях не бывает сбора власти
            if (map.getAreaType(areaOfOrder) == AreaType.sea && order.orderType() == OrderType.consolidatePower) {
                if (!alright) sb.append("\n");
                sb.append(AREA_NUMBER).append(areaOfOrder).append(NO_CP_IN_SEA);
                alright = false;
            }
            // В порту не может быть обороны
            if (map.getAreaType(areaOfOrder) == AreaType.port && order.orderType() == OrderType.defence) {
                if (!alright) sb.append("\n");
                sb.append(AREA_NUMBER).append(areaOfOrder).append(NO_DEFENCE_IN_PORT);
                alright = false;
            }
            // Проверяем, что нет приказов запрещённого событием третьей колоды приказа
            if (order.orderType() == forbiddenOrder && order != Order.marchB && order != Order.march) {
                if (!alright) sb.append("\n");
                sb.append(AREA_NUMBER).append(areaOfOrder).append(PROHIBITED_ORDER)
                        .append(order.toString());
                alright = false;
            }
        }
        // Проверяем, что количество приказов любого типа не превышает максимального возможного числа таких приказов
        for (int orderCode = 0; orderCode < NUM_DIFFERENT_ORDERS; orderCode++) {
            if (nOrdersWithCode[orderCode] > Order.maxNumOrdersWithCode[orderCode]) {
                if (!alright) sb.append("\n");
                sb.append(nOrdersWithCode[orderCode]).append(OF_ORDERS).append(Order.getOrderWithCode(orderCode))
                        .append(WHEN_MAX_OF_SUCH_ORDERS_IS).append(Order.maxNumOrdersWithCode[orderCode]);
                alright = false;
            }
        }

        // Проверяем, что соблюдено ограничение по звёздам
        int numStars = NUM_OF_STARS_ON_PLACE[trackPlaceForPlayer[TrackType.raven.getCode()][player]];
        if (nStarOrders > numStars) {
            if (!alright) sb.append("\n");
            sb.append(STAR_NUMBER_VIOLENCE).append(numStars).append(OF_STAR_ORDERS).append(nStarOrders);
            alright = false;
        }

        if (!alright) {
            sb.insert(0, ORDER_MISTAKES + HOUSE_GENITIVE[player] + ":\n");
            say(sb.toString());
        }
        return alright;
    }

    void executeRaid(RaidOrderPlayed raid) {
        int from = raid.getAreaFrom();
        int to = raid.getAreaTo();
        int player = armyInArea[from].getOwner();
        if (to >= 0 && to < NUM_AREA && to != from) {
            say(HOUSE[player] + RAIDS + map.getAreaNameRusGenitive(from) +
                    TO + map.getAreaNameRusAccusative(to) + ".");
            // Разыгрываем результативный набег, снимаем два приказа и обновляем попутные переменные
            int raidedPlayer = armyInArea[to].getOwner();
            //noinspection ConstantConditions
            switch (orderInArea[to].orderType()) {
                case raid:
                    areasWithRaids.get(raidedPlayer).remove(to);
                    break;
                case consolidatePower:
                    areasWithCPs.get(raidedPlayer).remove(to);
                    if (nPowerTokensHouse[player] < maxPowerTokensHouse[player]) {
                        nPowerTokensHouse[player]++;
                        say(HOUSE[player] + GAINS_POWER);
                        if (!Settings.getInstance().isPassByRegime()) {
                            houseTabPanel.repaintHouse(player);
                        }
                    }
                    if (nPowerTokensHouse[raidedPlayer] > 0) {
                        nPowerTokensHouse[raidedPlayer]--;
                        say(HOUSE[raidedPlayer] + LOSES_POWER);
                        if (!Settings.getInstance().isPassByRegime()) {
                            houseTabPanel.repaintHouse(raidedPlayer);
                        }
                    }
                    break;
            }
            orderInArea[to] = null;
            orderInArea[from] = null;
            areasWithRaids.get(player).remove(from);
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintArea(to);
            }
        } else {
            // Разыгрываем "холостой" набег, снимаем приказ и обновляем попутные переменные
            say(HOUSE[player] + DELETES_RAID + map.getAreaNameRusGenitive(from));
            orderInArea[from] = null;
            areasWithRaids.get(player).remove(from);
        }
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintArea(from);
        }
    }

    /**
     * Метод для валидации варианта разыгрыша набега, полученного от игрока
     * @param raid   вариант розыгрыша набега
     * @param player номер игрока
     * @return true, если набег можно разыграть таким образом
     */
    boolean validateRaid(RaidOrderPlayed raid, int player) {
        int from = raid.getAreaFrom();
        int to = raid.getAreaTo();

        // Банальные проверки на валидность областей и наличие набега, происходят для всех вариантов набегов без исключения
        if (from < 0 || from >= NUM_AREA || to >= NUM_AREA) {
            say(WRONG_AREAS_RAID_ERROR);
            return false;
        }
        if (orderInArea[from] == null || orderInArea[from].orderType() != OrderType.raid ||
                armyInArea[from].getOwner() != player) {
            say(NO_RAID_ERROR);
            return false;
        }

        // Случай результативного набега
        if (to >= 0 && to < NUM_AREA && to != from) {
            if (map.getAdjacencyType(from, to) == AdjacencyType.noAdjacency) {
                say(NO_ADJACENT_RAID_ERROR);
                return false;
            }
            if (armyInArea[to].getOwner() == player) {
                say(DONT_RAID_YOURSELF_ERROR);
                return false;
            }
            if (armyInArea[to].getOwner() < 0 || orderInArea[to] == null) {
                say(NO_ONE_TO_RAID_THERE_ERROR);
                return false;
            }
            if (map.getAdjacencyType(from, to) == AdjacencyType.landToSea) {
                say(NO_RAID_FROM_LAND_TO_SEA_ERROR);
                return false;
            }
            if (orderInArea[to].orderType() == OrderType.march ||
                    orderInArea[to].orderType() == OrderType.defence && orderInArea[from] == Order.raid) {
                say(CANT_RAID_THIS_ORDER_ERROR);
                return false;
            }
        }
        return true;
    }

    /**
     * Метод для валидации варианта розыгрыша похода, полученного от игрока
     * @param march  вариант розыгрыша похода
     * @param player номер игрока
     * @return true, если так разыграть поход можно
     */
    boolean validateMarch(MarchOrderPlayed march, int player) {
        int from = march.getAreaFrom();
        HashMap<Integer, ArrayList<UnitType>> destinationsOfMarch = march.getDestinationsOfMarch();

        // Банальные проверки на валидность областей и наличие похода, происходят для всех вариантов походов без исключения
        boolean wrongAreasFlag = false;
        for (int areaDestination: destinationsOfMarch.keySet()) {
            if (areaDestination < 0 || areaDestination >= NUM_AREA) {
                wrongAreasFlag = true;
            }
        }
        if (wrongAreasFlag || from < 0 || from >= NUM_AREA) {
            say(WRONG_AREAS_MARCH_ERROR);
            return false;
        }
        if (orderInArea[from] == null || orderInArea[from].orderType() != OrderType.march ||
                armyInArea[from].getOwner() != player) {
            say(NO_MARCH_ERROR);
            return false;
        }

        System.out.print(MARCH_VALIDATION + map.getAreaNm(from) + ": ");
        boolean firstFlag = true;
        for (Map.Entry<Integer, ArrayList<UnitType>> entry : destinationsOfMarch.entrySet()) {
            firstFlag = LittleThings.printDelimiter(firstFlag);
            int curDestination = entry.getKey();
            ArrayList<UnitType> curUnits = entry.getValue();
            System.out.print(GameUtils.getUnitsShortString(curUnits) + " -> " + map.getAreaNm(curDestination));
        }
        System.out.println();

        if (destinationsOfMarch.size() > 0) {
            // Дополнительные проверки в случае результативного похода
            int areaWhereBattleBegins = -1;
            Set<Integer> accessibleAreas = null;
            int[] numUnitsOfType = new int[NUM_UNIT_TYPES];
            if (!map.getAreaType(from).isNaval()) {
                /*
                 * Составляем множество областей, в которые может пойти игрок из данной области с учётом
                 * правила переброски морем
                 */
                accessibleAreas = getAccessibleAreas(from, player);
            }
            for (Map.Entry<Integer, ArrayList<UnitType>> entry : destinationsOfMarch.entrySet()) {
                int curDestination = entry.getKey();
                ArrayList<UnitType> curUnits = entry.getValue();
                // Проверяем, что в данную область назначения действительно можно пойти
                if (map.getAreaType(from).isNaval() && !map.getAreaType(curDestination).isNaval()) {
                    say(CANT_MARCH_FROM_SEA_TO_LAND_ERROR);
                    return false;
                }
                if (map.getAreaType(from).isNaval() && map.getAdjacencyType(from, curDestination) == AdjacencyType.noAdjacency) {
                    say(CANT_MARCH_THERE_ERROR);
                    return false;
                }
                if (map.getAreaType(from).isNaval() && map.getAreaType(curDestination) == AreaType.port) {
                    // Если замок, которому принадлежит порт, в который хочет зайти игрок, ему не принадлежит, то фейл
                    if (!canMoveInPort(curDestination, player)) {
                        say(CANT_MARCH_IN_NOT_YOUR_PORT_ERROR);
                        return false;
                    }
                }
                if (!map.getAreaType(from).isNaval() && map.getAreaType(curDestination).isNaval()) {
                    say(CANT_MARCH_FROM_LAND_TO_SEA_ERROR);
                    return false;
                }
                if (!map.getAreaType(from).isNaval() && !accessibleAreas.contains(curDestination)) {
                    say(NO_WAY_MARCH_ERROR);
                    return false;
                }
                // Проверяем, что марширующая армия не пуста
                if (curUnits.isEmpty()) {
                    say(EMPTY_ARMY_MARCH_ERROR);
                    return false;
                }

                // Считаем, начнётся ли битва в области назначения (пробивание нейтрального гарнизона тоже считается)
                int destinationArmyOwner = getTroopsOrGarrisonOwner(curDestination);
                if (destinationArmyOwner >= 0 && destinationArmyOwner != player ||
                        garrisonInArea[curDestination] > 0 && destinationArmyOwner < 0) {
                    if (areaWhereBattleBegins < 0) {
                        if (destinationArmyOwner < 0 && garrisonInArea[curDestination] > 0) {
                            int playerStrength = calculatePowerOfPlayerVersusGarrison(player, curDestination, curUnits,
                                    orderInArea[from].getModifier());
                            if (playerStrength < garrisonInArea[curDestination]) {
                                say(CANT_BEAT_NEUTRAL_GARRISON_ERROR_PLAYER_STRENGTH + playerStrength +
                                        GARRISON_STRENGTH_IS + garrisonInArea[curDestination]);
                                return false;
                            }
                        }
                        areaWhereBattleBegins = curDestination;
                    } else {
                        say(CANT_BEGIN_TWO_BATTLES_BY_ONE_MARCH_ERROR);
                        return false;
                    }
                }

                // Проверяем, что в порту не оказывается более 3 кораблей
                if (map.getAreaType(curDestination) == AreaType.port &&
                        curUnits.size() + armyInArea[curDestination].getSize() > MAX_TROOPS_IN_PORT) {
                    say(TOO_MANY_SHIPS_IN_PORT_ERROR);
                    return false;
                }

                // Проверки данной области назначения пройдены. Теперь учитываем юниты, перемещающиеся в эту область
                for (UnitType type : curUnits) {
                    numUnitsOfType[type.getCode()]++;
                }
            }

            // Проверка наличия необходимых юнитов в области отправления
            for (Unit unit : armyInArea[from].getUnits()) {
                if (unit.isWounded()) continue;
                numUnitsOfType[unit.getUnitType().getCode()]--;
            }
            for (int i = 0; i < NUM_UNIT_TYPES; i++) {
                if (numUnitsOfType[i] > 0) {
                    say(LACK_OF_UNITS_ERROR);
                    return false;
                }
            }

            // Проверка по снабжению
            if (!supplyTestForMarch(march)) {
                say(SUPPLY_VIOLATION_ERROR);
                return false;
            }

            // Проверки возможности оставить жетон власти
            if (march.getIsLeaveToken() && nPowerTokensHouse[player] == 0) {
                say(NO_POWER_TOKENS_TO_LEAVE_ERROR);
                return false;
            }
            if (march.getIsLeaveToken() && map.getAreaType(from).isNaval()) {
                say(CANT_LEAVE_POWER_TOKEN_IN_SEA_ERROR);
                return false;
            }
        }
        return true;
    }

    /**
     * Метод исполняет один приказ сбора войск
     * @param musterVariant вариант сбора войск
     * @param player        номер игрока
     */
    void executeMuster(MusterPlayed musterVariant, int player) {
        int nMusteredObjects = musterVariant.getNumberMusterUnits();
        for (int i = 0; i < nMusteredObjects; i++) {
            int area = musterVariant.getArea(i);
            Musterable musterObject = musterVariant.getMusterUnit(i);
            if (musterObject instanceof UnitType) {
                Unit unit = new Unit (((UnitType) musterObject), player);
                armyInArea[area].addUnit(unit);
                restingUnitsOfPlayerAndType[player][unit.getUnitType().getCode()]--;
                renewHouseTroopsInArea(area);
            } else {
                UnitType targetType = ((PawnPromotion) musterObject).getTargetType();
                armyInArea[area].changeType(UnitType.pawn, targetType);
                restingUnitsOfPlayerAndType[player][UnitType.pawn.getCode()]++;
                restingUnitsOfPlayerAndType[player][targetType.getCode()]--;
            }
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintArea(area);
            }
            say(HOUSE[player] + musterObject.getActionString() +
                    map.getAreaNameRusLocative(area));
        }

        int castleArea = musterVariant.getCastleArea();
        safeDeleteOrderInArea(castleArea, player);
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    void safeDeleteOrderInArea(int area, int player) {
        if (orderInArea[area] != null) {
            if (orderInArea[area].orderType() == OrderType.march) {
                areasWithMarches.get(player).remove(area);
            } else if (orderInArea[area].orderType() == OrderType.consolidatePower) {
                areasWithCPs.get(player).remove(area);
            }
            orderInArea[area] = null;
        }
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintArea(area);
        }
    }

    /**
     * Метод добавляет игроку некоторое число заработанных жетонов
     * @param player  номер игрока
     * @param earning заработок
     */
    void earnTokens(int player, int earning) {
        nPowerTokensHouse[player] = Math.max(Math.min(maxPowerTokensHouse[player], nPowerTokensHouse[player] + earning), 0);
        int absSum = Math.abs(earning);
        say(HOUSE[player] + (earning >= 0 ? EARNS: LOSES_MONEY) + absSum +
                (absSum == 1 ? POWER_TOKEN : absSum < 5 ? POWER_TOKENA : POWER_TOKENS) +
                NOW_HE_HAS + nPowerTokensHouse[player] +
                (nPowerTokensHouse[player] == 1 ? POWER_TOKEN : nPowerTokensHouse[player] < 5 ? POWER_TOKENA : POWER_TOKENS));
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    /**
     * Метод валидирует розыгрыш сбора власти на определённой территории
     * @param muster вариант сбора власти
     * @param player номер игрока
     * @return true, если можно так сделать
     */
    boolean validateMuster(MusterPlayed muster, int player) {
        int from = muster.getCastleArea();
        if (from < 0 || from >= NUM_AREA || getAreaOwner(from) != player) {
            say(WRONG_AREAS_TO_MUSTER_ERROR);
            return false;
        }
        int numNeededUnitsOfType[] = new int[NUM_UNIT_TYPES];
        int numNeededPawns = 0;
        int spentMusterPoints = 0;
        int numUnits = muster.getNumberMusterUnits();
        for (int i = 0; i < numUnits; i++) {
            int area = muster.getArea(i);
            if (map.getAreaType(area).isNaval() && map.getAdjacencyType(from, area) == AdjacencyType.noAdjacency ||
                    !map.getAreaType(area).isNaval() && from != area ||
                    map.getAreaType(area) == AreaType.sea && getTroopsOwner(area) >= 0 && getTroopsOwner(area) != player) {
                say(CANT_MUSTER_HERE_ERROR);
                return false;
            }
            if (muster.getMusterUnit(i) instanceof PawnPromotion) {
                numNeededPawns++;
                numNeededUnitsOfType[UnitType.pawn.getCode()]--;
                numNeededUnitsOfType[((PawnPromotion) muster.getMusterUnit(i)).getTargetType().getCode()]++;
            } else {
                numNeededUnitsOfType[muster.getMusterUnit(i).getCode()]++;
            }
            spentMusterPoints += muster.getMusterUnit(i).getNumMusterPoints();
        }

        if (spentMusterPoints > map.getNumCastle(from)) {
            say(MUSTER_POINTS_VIOLATED_ERROR);
            return false;
        }
        if (armyInArea[from].getNumUnitOfType(UnitType.pawn) < numNeededPawns) {
            say(NO_PAWN_TO_PROMOTE_ERROR);
            return false;
        }
        if (!portTroopsTestForMuster(muster)) {
            say(TOO_MANY_SHIPS_IN_PORT_ERROR);
            return false;
        }
        for (UnitType unitType: UnitType.values()) {
            if (numNeededUnitsOfType[unitType.getCode()] > restingUnitsOfPlayerAndType[player][unitType.getCode()]) {
                say(NO_UNITS_TO_PUT_ERROR + unitType);
                return false;
            }
        }
        boolean result = supplyTestForMuster(muster);
        if (!result) {
            say(MUSTER_SUPPLY_VIOLATION_ERROR);
        }
        return result;
    }

    /**
     * Метод формирует и возвращает множество областей, которые соединены с данной областью с учётом
     * правила переброски морем для данного игрока.
     * @param areaFrom область похода
     * @param player   игрок, для которого рассчитывается множество областей
     * @return множество с доступными для похода областями
     */
    public Set<Integer> getAccessibleAreas(int areaFrom, int player) {
        HashSet<Integer> accessibleAreas = new HashSet<>();
        if (map.getAreaType(areaFrom).isNaval()) {
            // Для морских территорий просто выдаём список соседних территорий
            for (int adjacentArea: map.getAdjacentAreas(areaFrom)) {
                switch (map.getAreaType(adjacentArea)) {
                    case sea:
                        accessibleAreas.add(adjacentArea);
                        break;
                    case port:
                        if (canMoveInPort(adjacentArea, player)) {
                            accessibleAreas.add(adjacentArea);
                        }
                        break;
                }
            }
        } else {
            // Для сухопутных территорий также рассматриваем вариант переброски морем
            // здесь мы будем хранить список морей, которые уже рассмотрели
            accessibleAreaSet.clear();
            for (int area: map.getAdjacentAreas(areaFrom)) {
                // Если мы нашли море, принадлежащее игроку, то включаем его в множество морей, которые дальше будут рассматриваться
                if (map.getAreaType(area) == AreaType.sea && getTroopsOwner(area) == player) {
                    accessibleAreas.addAll(getAreasAdjacentToSea(area, player));
                } else if (map.getAreaType(area) == AreaType.land) {
                    accessibleAreas.add(area);
                }
            }
            if (accessibleAreas.contains(areaFrom)) {
                accessibleAreas.remove(areaFrom);
            }
        }
        return accessibleAreas;
    }

    private HashSet<Integer> getAreasAdjacentToSea(int seaAreaFrom, int player) {
        accessibleAreaSet.add(seaAreaFrom);
        HashSet<Integer> areaSet = new HashSet<>();
        for (int area: map.getAdjacentAreas(seaAreaFrom)) {
            // Если мы нашли море, принадлежащее игроку, то и его рассматриваем
            if (map.getAreaType(area) == AreaType.sea && getTroopsOwner(area) == player && !accessibleAreaSet.contains(area)) {
                areaSet.addAll(getAreasAdjacentToSea(area, player));
            } else if (map.getAreaType(area) == AreaType.land) {
                areaSet.add(area);
            }
        }
        return areaSet;
    }

    /**
     * Метод проверяет, могут ли корабли данного игрока зайти в данный порт
     * @param portArea область с портом
     * @param player   номер игрока
     * @return true, если можно
     */
    private boolean canMoveInPort(int portArea, int player) {
        // Если замок, которому принадлежит порт, в который хочет зайти игрок, принадлежит ему, то всё хорошо
        return getAreaOwner(map.getCastleWithPort(portArea)) == player;
    }

    public int getNumStars(int player) {
        return Math.max(0, Math.min(4 - trackPlaceForPlayer[TrackType.raven.getCode()][player], 3));
    }

    /**
     * Метод возвращает области, из которых может прийти атака на данную область.
     * Если в области есть отряды или гарнизон игрока, то учитываются только вражеские области;
     * в противном случае учитываются области всех игроков, откуда может прийти атака
     * @param area номер области
     * @return Множество областей с возможными походами
     */
    public HashSet<Integer> getAreasWithProbableMarch(int area) {
        HashSet<Integer> probableMarchAreas = new HashSet<>();
        int areaOwner = getTroopsOrGarrisonOwner(area);
        if (map.getAreaType(area).isNaval()) {
            HashSet<Integer> adjacentAreas = map.getAdjacentAreas(area);
            for (int adjArea: adjacentAreas) {
                if (map.getAreaType(adjArea).isNaval() &&
                        getTroopsOwner(adjArea) >= 0 && getTroopsOwner(adjArea) != areaOwner) {
                    probableMarchAreas.add(adjArea);
                }
            }
        } else {
            Set<Integer> accessibleForPlayerAreas;
            for (int player = 0; player < NUM_PLAYER; player++) {
                if (player == areaOwner) continue;
                accessibleForPlayerAreas = getAccessibleAreas(area, player);
                for (int enemyArea: accessibleForPlayerAreas) {
                    if (getTroopsOwner(enemyArea) == player) {
                        probableMarchAreas.add(enemyArea);
                    }
                }
            }
        }
        return probableMarchAreas;
    }

    private void renewHandExceptCard(int player, HouseCard card) {
        say(HOUSE[player] + GETS_NEW_DECK);
        for (int i = 0; i < NUM_HOUSE_CARDS; i++) {
            if (houseCardOfPlayer[player][i] != card && !houseCardOfPlayer[player][i].isActive()) {
                houseCardOfPlayer[player][i].setActive(true);
                numActiveHouseCardsOfPlayer[player]++;
            }
        }
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    private void countBattleVariables() {
        battleInfo.countBattleVariables();
        if (!Settings.getInstance().isPassByRegime()) {
            fightTabPanel.repaint();
        }
    }

    void useTyrion() {
        int enemySide = houseCardOfSide[0] == HouseCard.tyrionLannister ? 1 : 0;
        say(TYRION_CANCELS);
        temporaryInactiveCard = houseCardOfSide[enemySide];
        battleInfo.setHouseCardForPlayer(playerOnSide[enemySide], null);
        if (Settings.getInstance().isTrueAutoSwitchTabs()) {
            tabPanel.setSelectedIndex(TabEnum.fight.getCode());
        }
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(temporaryInactiveCard.house());
        }
        countBattleVariables();
    }

    void playMace() {
        int heroSide = houseCardOfSide[0] == HouseCard.maceTyrell ? 0 : 1;
        // Чёрная Рыба защищает от свойства Мейса Тирелла
        if (houseCardOfSide[1 - heroSide] == HouseCard.theBlackfish) {
            say(BLACKFISH_SAVES + HOUSE_GENITIVE[playerOnSide[1 - heroSide]] + FROM_LOSSES);
            return;
        }
        Army armyToSearchForFootmen = heroSide == 0 ? armyInArea[areaOfBattle] : attackingArmy;
        Unit victimOfMace = armyToSearchForFootmen.getUnitOfType(UnitType.pawn);
        if (victimOfMace != null) {
            say(MACE_EATS_MAN + map.getAreaNameRusLocative(areaOfBattle));
            battleInfo.deleteUnit(heroSide == 0 ? SideOfBattle.defender : SideOfBattle.attacker, victimOfMace);
            armyToSearchForFootmen.killUnit(victimOfMace, KillingReason.mace, this);
            renewArea(areaOfBattle, playerOnSide[1 - heroSide]);
            countBattleVariables();
            if (!Settings.getInstance().isPassByRegime()) {
                houseTabPanel.repaintHouse(playerOnSide[1 - heroSide]);
            }
        } else {
            say(houseCardOfSide[heroSide].getName() + NO_EFFECT);
        }
    }

    HashSet<Integer> getRenlyAreas() {
        accessibleAreaSet.clear();
        if (armyInArea[areaOfBattle].hasUnitOfType(UnitType.pawn)) {
            accessibleAreaSet.add(areaOfBattle);
        }
        for (int adjacentArea: map.getAdjacentAreas(areaOfBattle)) {
            if (getTroopsOwner(adjacentArea) == winner && orderInArea[adjacentArea] != null &&
                    orderInArea[adjacentArea].orderType() == OrderType.support &&
                    armyInArea[adjacentArea].hasUnitOfType(UnitType.pawn) &&
                    supportOfPlayer[winner] != null &&
                    supportOfPlayer[winner].getCode() >= 0) {
                accessibleAreaSet.add(adjacentArea);
            }
        }
        return accessibleAreaSet;
    }

    void playRenly(int area) {
        int player = getTroopsOwner(area);
        say(RENLY_MAKES_KNIGHT + map.getAreaNameRusLocative(area) + ".");
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintArea(area);
        }

        armyInArea[area].changeType(UnitType.pawn, UnitType.knight);
        restingUnitsOfPlayerAndType[player][UnitType.pawn.getCode()]++;
        restingUnitsOfPlayerAndType[player][UnitType.knight.getCode()]--;
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    HashSet<Integer> getQueenOfThornsAreas(int enemy) {
        accessibleAreaSet.clear();
        for (int adjacentArea: map.getAdjacentAreas(areaOfBattle)) {
            if (orderInArea[adjacentArea] != null && adjacentArea != areaOfMarch &&
                    getTroopsOwner(adjacentArea) == enemy) {
                accessibleAreaSet.add(adjacentArea);
            }
        }
        return accessibleAreaSet;
    }

    void playQueenOfThorns(int area, int enemy) {
        if (Settings.getInstance().isTrueAutoSwitchTabs()) {
            tabPanel.setSelectedIndex(TabEnum.fight.getCode());
        }
        say(QUEEN_OF_THORNS_REMOVES_ORDER + map.getAreaNameRusGenitive(area));
        if (orderInArea[area] != null) {
            if (orderInArea[area].orderType() == OrderType.support && supportOfPlayer[enemy] != SideOfBattle.neutral) {
                battleInfo.deleteArmy(enemy == 1 ? SideOfBattle.defender : SideOfBattle.attacker, armyInArea[area]);
                battleInfo.deleteSupportOfPlayer(enemy, orderInArea[area].getModifier());
                countBattleVariables();
            }
            safeDeleteOrderInArea(area, enemy);
        }
    }

    HashSet<Integer> getCerseiAreas() {
        accessibleAreaSet.clear();
        for (int area: areasWithTroopsOfPlayer.get(loser).keySet()) {
            if (orderInArea[area] != null) {
                accessibleAreaSet.add(area);
            }
        }
        accessibleAreaSet.remove(areaOfMarch);
        return accessibleAreaSet;
    }

    void playCersei(int area) {
        if (Settings.getInstance().isTrueAutoSwitchTabs()) {
            tabPanel.setSelectedIndex(TabEnum.fight.getCode());
        }
        say(CERSEI_REMOVES_ORDER + map.getAreaNameRusGenitive(area));
        safeDeleteOrderInArea(area, loser);
    }

    void playDoran(int trackToPissOff, int enemy) {
        if (trackToPissOff >= 0 && trackToPissOff < NUM_TRACK) {
            say(DORAN_ABUSES + HOUSE_GENITIVE[enemy] +
                    TrackType.getTrack(trackToPissOff).onTheTrack());
            pissOffOnTrack(enemy, trackToPissOff);
            countBattleVariables();
        } else {
            say(HouseCard.doranMartell + NO_EFFECT);
        }
    }

    void playPatchface(int enemy, int numCard) {
        houseCardOfPlayer[enemy][numCard].setActive(false);
        numActiveHouseCardsOfPlayer[enemy]--;
        if (numActiveHouseCardsOfPlayer[enemy] == 0) {
            renewHandExceptCard(enemy, houseCardOfPlayer[enemy][numCard]);
        }
        say(PATCHPACE_DELETES_CARD + houseCardOfPlayer[enemy][numCard].getName() + ".");
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(enemy);
        }
    }

    /**
     * Метод возвращает боевую силу игрока при его походе на определённую область определённой армией.
     * Считается поддержка игрока, но не считаются поддержки и защиты других игроков.
     * Нужен при пробивании нейтральных гарнизонов.
     * @param player        игрок
     * @param area          область, в которую собирается неумолимо надвигаться несокрушимая армада
     * @param units         армия из марширующих юнитов
     * @param marchModifier модификатор похода
     * @return итоговая боевая сила игрока
     */
    public int calculatePowerOfPlayerVersusGarrison(int player, int area, ArrayList<UnitType> units, int marchModifier) {
        int strength = marchModifier;
        // Прибавляем силу атакующих юнитов
        for (UnitType type: units) {
            if (type != UnitType.siegeEngine || map.getNumCastle(area) > 0) {
                strength += type.getStrength();
            }
        }
        // Прибавляем силу поддеривающих юнитов
        for (int adjacentArea: map.getAdjacentAreas(area)) {
            if (getTroopsOwner(adjacentArea) == player &&
                    orderInArea[adjacentArea] != null && orderInArea[adjacentArea].orderType() == OrderType.support) {
                strength += orderInArea[adjacentArea].getModifier();
                for (Unit unit: armyInArea[adjacentArea].getUnits()) {
                    if (unit.getUnitType() != UnitType.siegeEngine || map.getNumCastle(area) > 0) {
                        strength += unit.getStrength();
                    }
                }
            }
        }
        return strength;
    }

    /**
     * Вернуть на руку все карты данного игрока (Русе Болтон или победа на Молоководной)
     * @param player номер игрока
     */
    void returnAllCards (int player) {
        for (int card = 0; card < NUM_HOUSE_CARDS; card++) {
            if (!houseCardOfPlayer[player][card].isActive()) {
                houseCardOfPlayer[player][card].setActive(true);
            }
        }
        numActiveHouseCardsOfPlayer[player] = NUM_HOUSE_CARDS;
        say (HOUSE[player] + RETURNS_ALL_CARDS);
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    /**
     * Метод возвращает высшую по боевой силе активную карту игрока
     * @param player номер игрока
     * @return первая активная карта дома
     */
    public HouseCard getFirstActiveHouseCard(int player) {
        for (int i = 0; i < NUM_HOUSE_CARDS; i++) {
            if (houseCardOfPlayer[player][i].isActive()) {
                return houseCardOfPlayer[player][i];
            }
        }
        return null;
    }

    /**
     * Метод возвращает высшую по боевой силе неактивную карту игрока
     * @param player номер игрока
     * @return первая активная карта дома
     */
    public HouseCard getFirstPassiveHouseCard(int player) {
        for (int i = 0; i < NUM_HOUSE_CARDS; i++) {
            if (!houseCardOfPlayer[player][i].isActive()) {
                return houseCardOfPlayer[player][i];
            }
        }
        return null;
    }

    private void announceWinnerAndLoser() {
        winnerSide = battleInfo.getWinnerSide();
        winner = playerOnSide[winnerSide];
        loser = playerOnSide[1 - winnerSide];
    }

    void winAndKill() {
        if (!Settings.getInstance().isPassByRegime()) {
            fightTabPanel.repaint();
        }
        announceWinnerAndLoser();
        say(HOUSE[winner] + WINS_THE_BATTLE);
        // Подсчитываем потери проигравшего
        System.out.println("У " + HOUSE_GENITIVE[winner] + " " + battleInfo.getSwordsOnSide(winnerSide) +
                SWORDS_U + HOUSE_GENITIVE[loser] + " " + battleInfo.getTowersOnSide(1 - winnerSide) + OF_TOWERS);
        int numKilledUnits = battleInfo.getNumKilled();
        if (numKilledUnits == 0) {
            System.out.println(HOUSE[loser] + HAS_NO_LOSSES);
        } else if (houseCardOfSide[1 - winnerSide] == HouseCard.theBlackfish) {
            say(BLACKFISH_SAVES + HOUSE_GENITIVE[loser] + FROM_LOSSES);
            numKilledUnits = 0;
        } else {
            System.out.println(HOUSE[loser] + LOSES + numKilledUnits +
                    (numKilledUnits > 1 ? TROOPS : TROOP));
        }
        if (winnerSide == 0) {
            armyInArea[areaOfBattle].killSomeUnits(numKilledUnits, KillingReason.sword, this);
        } else {
            attackingArmy.killSomeUnits(numKilledUnits, KillingReason.sword, this);
        }
    }

    void attackerWon() {
        areasWithTroopsOfPlayer.get(loser).remove(areaOfBattle);
        // Удаление жетона власти с области, если он там был
        if (powerTokenOnArea[areaOfBattle] >= 0) {
            maxPowerTokensHouse[powerTokenOnArea[areaOfBattle]]++;
            powerTokenOnArea[areaOfBattle] = -1;
        }
        // Удаление приказа из области, если он там был
        if (orderInArea[areaOfBattle] != null) {
            safeDeleteOrderInArea(areaOfBattle, loser);
        }
        // Уничтожение гарнизона в области, если он там был
        if (garrisonInArea[areaOfBattle] > 0) {
            say(GARRISON + map.getAreaNameRusLocative(areaOfBattle) + IS_DEFEATED_M);
            garrisonInArea[areaOfBattle] = 0;
        }
        // Лорас Тирелл контрится Арианной
        if (houseCardOfSide[0] == HouseCard.serLorasTyrell && houseCardOfSide[1] != HouseCard.arianneMartell) {
            say(LORAS_RULES);
            int marchModifier = orderInArea[areaOfMarch].getModifier();
            Order marchOrder = marchModifier == -1 ? Order.marchB : (marchModifier == 0 ? Order.march : Order.marchS);
            orderInArea[areaOfBattle] = marchOrder;
            areasWithMarches.get(winner).add(areaOfBattle);
        }

        retreatingArmy.addSubArmy(armyInArea[areaOfBattle]);
        armyInArea[areaOfBattle].deleteAllUnits();
        // Арианна
        if (houseCardOfSide[1] == HouseCard.arianneMartell) {
            say(ARIANNA_RULES_AND_PUTS_BACK + map.getAreaNameRusAccusative(areaOfMarch) + ".");
            armyInArea[areaOfMarch].addSubArmy(attackingArmy);
            renewHouseTroopsInArea(areaOfMarch);
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintArea(areaOfMarch);
            }
        } else {
            armyInArea[areaOfBattle].addSubArmy(attackingArmy);
            renewHouseTroopsInArea(areaOfBattle);
        }
        attackingArmy.deleteAllUnits();
        if (map.getNumCastle(areaOfBattle) > 0) {
            adjustVictoryPoints();
        }
    }

    void calculateRetreatAreas() {
        int numRetreatingUnits = retreatingArmy.getSize();
        Set<Integer> rawAreasToRetreat = getAccessibleAreas(areaOfBattle, loser);
        areasToRetreat.clear();
        int minLosses = numRetreatingUnits;
        int curLosses;
        // Подготавливаем информацию по снабжению
        virtualAreasWithTroops.clear();
        virtualAreasWithTroops.putAll(areasWithTroopsOfPlayer.get(loser));

        for (int area: rawAreasToRetreat) {
            // Нельзя отступить в область, откуда пришла атака
            if (area == areaOfMarch) continue;
            // Нельзя отступить в область, где есть нейтральный гарнизон
            if (isNeutralGarrisonInArea(area)) continue;
            int areaOwner = getAreaOwner(area);
            if (areaOwner < 0 || areaOwner == loser) {
                if (getTroopsOwner(area) < 0) {
                    curLosses = 0;
                } else {
                    int previousNumberOfUnits = virtualAreasWithTroops.get(area);
                    for (curLosses = 0; curLosses < numRetreatingUnits; curLosses++) {
                        virtualAreasWithTroops.put(area, previousNumberOfUnits + numRetreatingUnits - curLosses);
                        if (GameUtils.supplyTest(virtualAreasWithTroops, supply[loser])) break;
                    }
                    virtualAreasWithTroops.put(area, previousNumberOfUnits);
                }
                if (map.getAreaType(area) == AreaType.port) {
                    curLosses = Math.max(curLosses,
                            armyInArea[area].getSize() + retreatingArmy.getSize() - MAX_TROOPS_IN_PORT);
                }
                if (curLosses < minLosses) {
                    minLosses = curLosses;
                    areasToRetreat.clear();
                    areasToRetreat.add(area);
                } else if (curLosses == minLosses) {
                    areasToRetreat.add(area);
                }
            }
        }

        System.out.println(MINIMAL_LOSSES + minLosses + ".");
        if (minLosses == numRetreatingUnits) {
            retreatingArmy.killAllUnits(KillingReason.noAreaToRetreat, this);
        } else {
            if (minLosses > 0) {
                retreatingArmy.killSomeUnits(minLosses, KillingReason.supplyLimit, this);
            }
        }
    }

    void defenderWon() {
        int marchAreaOwner = getAreaOwner(areaOfMarch);
        // Если область, из которой был поход, стала вражеской (как это бывает с престольными землями, на которых
        // не оставили жетонов власти), то все оставшиеся юниты уничтожаются.
        if (marchAreaOwner < 0 || marchAreaOwner == loser) {
            // Если остались атакующие юниты, то они перемещаются в область, откуда нападали
            if (!attackingArmy.isEmpty()) {
                int minLosses;
                // Подготавливаем информацию по снабжению
                virtualAreasWithTroops.clear();
                virtualAreasWithTroops.putAll(areasWithTroopsOfPlayer.get(loser));
                if (getTroopsOwner(areaOfMarch) < 0) {
                    minLosses = 0;
                } else {
                    int previousNumberOfUnits = virtualAreasWithTroops.get(areaOfMarch);
                    for (minLosses = 0; minLosses < attackingArmy.getSize(); minLosses++) {
                        virtualAreasWithTroops.put(areaOfMarch, previousNumberOfUnits + attackingArmy.getSize()
                                - minLosses);
                        if (GameUtils.supplyTest(virtualAreasWithTroops, supply[loser])) break;
                        virtualAreasWithTroops.put(areaOfMarch, previousNumberOfUnits);
                    }
                }
                System.out.println(MINIMAL_LOSSES + minLosses + ".");
                if (minLosses > 0) {
                    attackingArmy.killSomeUnits(minLosses, KillingReason.supplyLimit, this);
                }
                attackingArmy.woundAllTroops(this);
                if (attackingArmy.getSize() > 0) {
                    say(HOUSE[loser] + RETREATS_IN +
                            map.getAreaNameRusAccusative(areaOfMarch));
                    armyInArea[areaOfMarch].addSubArmy(attackingArmy);
                    renewArea(areaOfMarch, loser);
                }
                attackingArmy.deleteAllUnits();
            }
        } else {
            attackingArmy.killAllUnits(KillingReason.noAreaToRetreat, this);
        }
    }

    public boolean isHigherOnThrone(int hero, int enemy) {
        return trackPlaceForPlayer[0][hero] < trackPlaceForPlayer[0][enemy];
    }

    public boolean isHigherOnSword(int hero, int enemy) {
        return trackPlaceForPlayer[1][hero] < trackPlaceForPlayer[1][enemy];
    }

    public boolean isStannisActive() {
        return houseCardOfPlayer[0][0].isActive();
    }

    public int getDefenceBonusInArea(int area) {
        return orderInArea[area] != null && orderInArea[area].orderType() == OrderType.defence ?
                orderInArea[area].getModifier() : 0;
    }

    public void unitStoreIncreased(UnitType type, int player) {
        restingUnitsOfPlayerAndType[player][type.getCode()]++;
    }

    /**
     * Спускает игрока в самый конец по одному из треков влияния
     * @param player опускаемый игрок
     * @param track  номер трека влияния
     */
    void pissOffOnTrack(int player, int track) {
        int place = trackPlaceForPlayer[track][player];
        System.arraycopy(trackPlayerOnPlace[track], place + 1, trackPlayerOnPlace[track], place, NUM_PLAYER - 1 - place);
        trackPlayerOnPlace[track][NUM_PLAYER - 1] = player;
        fillTrackPlaceForPlayer(track);
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintTracks();
        }
        printOrderOnTrack(track);
    }

    /**
     * Метод понижает игрока на определённое число позиций по одному из треков влияния
     * @param player        номер игрока
     * @param track         номер трека
     * @param positionDecay количество позиций, которые сдаёт игрок по треку
     */
    void descendOnTrack(int player, int track, int positionDecay) {
        int place = trackPlaceForPlayer[track][player];
        int newPlace = Math.min(place + positionDecay, NUM_PLAYER - 1);
        System.arraycopy(trackPlayerOnPlace[track], place + 1, trackPlayerOnPlace[track], place, newPlace - place);
        trackPlayerOnPlace[track][newPlace] = player;
        fillTrackPlaceForPlayer(track);
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintTracks();
        }
        printOrderOnTrack(track);
    }

    /**
     * Поднимает игрока на верхнюю позицию по одному из треков влияния
     * @param player поднимаемый игрок
     * @param track  номер трека влияния
     */
    void enlightenOnTrack(int player, int track) {
        int place;
        place = trackPlaceForPlayer[track][player];
        System.arraycopy(trackPlayerOnPlace[track], 0, trackPlayerOnPlace[track], 1, place);
        trackPlayerOnPlace[track][0] = player;
        fillTrackPlaceForPlayer(track);
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintTracks();
        }
        printOrderOnTrack(track);
    }

    /**
     * Метод выводит порядок игроков на определённом треке влияния
     * @param track номер трека влияния
     */
    private void printOrderOnTrack(int track) {
        System.out.print(NEW_ORDER_ON_TRACK + TrackType.getTrack(track) + ": ");
        for (int place = 0; place < NUM_PLAYER; place++) {
            if (place != 0) {
                System.out.print(", ");
            }
            System.out.print(HOUSE[trackPlayerOnPlace[track][place]]);
        }
        System.out.println();
    }

    /**
     * Метод инициализирует колоды событий, одичалых и домов
     */
    private void initializeDecks() {
        for (int i = 0; i < NUM_EVENT_DECKS; i++) {
            fillDeck(i);
        }
        wildlingDeck.addAll(Arrays.asList(WildlingCard.values()));
        int curIndexForPlayer[] = new int[NUM_PLAYER];
        for (HouseCard houseCard: HouseCard.values()) {
            int house = houseCard.house();
            if (house >= 0) {
                houseCardOfPlayer[house][curIndexForPlayer[house]] = houseCard;
                houseCardOfPlayer[house][curIndexForPlayer[house]].setActive(true);
                curIndexForPlayer[house]++;
            }
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            numActiveHouseCardsOfPlayer[player] = NUM_HOUSE_CARDS;
        }
        Collections.shuffle(wildlingDeck);
    }

    /**
     * Метод составляет новую определённую колоду событий из всех карточек
     * @param deckNumber номер колоды событий от 0 до 2
     */
    private void fillDeck(int deckNumber) {
        decks.get(deckNumber).clear();
        for (Happenable card: deckNumber == 0 ? Deck1Cards.values() :
                (deckNumber == 1 ? Deck2Cards.values() : Deck3Cards.values())) {
            numRemainingCards.put(card, card.getNumOfCards());
            for (int i = 0; i < card.getNumOfCards(); i++) {
                decks.get(deckNumber).add(card);
            }
        }
        Collections.shuffle(decks.get(deckNumber));
    }

    /**
     * Метод изменяет снабжение игрока на определённую величину. Используется при нашествии разбойников гремучей рубашки.
     * @param player   номер игрока
     * @param modifier модификатор снабжения
     */
    void changeSupply(int player, int modifier) {
        supply[player] += modifier;
        if (supply[player] < 0) {
            supply[player] = 0;
        } else if (supply[player] > MAX_SUPPLY) {
            supply[player] = MAX_SUPPLY;
        }
        say(SUPPLY_OF + HOUSE_GENITIVE[player] + EQUALS + supply[player] + ".");
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintSupply();
        }
    }

    /**
     * Метод обнуляет все приказы, использованный меч и лечит всех юнитов в начале нового раунда
     */
    void nullifyOrdersAndVariables() {
        for (int area = 0; area < NUM_AREA; area++) {
            orderInArea[area] = null;
            armyInArea[area].healAllUnits();
        }
        forbiddenOrder = null;
        isSwordUsed = false;
        mapPanel.repaint();
    }

    /**
     * Метод вытаскивает три новых события при наступлении фазы Вестероса
     */
    void chooseNewEvents() {
        for (int i = 0; i < NUM_EVENT_DECKS; i++) {
            event[i] = decks.get(i).pollFirst();
            while (event[i] == Deck1Cards.winterIsComing1 || event[i] == Deck2Cards.winterIsComing2) {
                fillDeck(i);
                event[i] = decks.get(i).pollFirst();
            }
            if (event[i].isWild() && wildlingsStrength < MAX_WILDLING_STRENGTH) {
                wildlingsStrength += WILDLING_STRENGTH_INCREMENT;
            }
            numRemainingCards.put(event[i], numRemainingCards.get(event[i]) - 1);
        }
        say(NEW_EVENTS + event[0].getName() + "; " + event[1].getName() + "; " + event[2].getName() + ".");
        if (Settings.getInstance().isTrueAutoSwitchTabs()) {
            tabPanel.setSelectedIndex(TabEnum.event.getCode());
        } else if (!Settings.getInstance().isPassByRegime()){
            eventTabPanel.repaint();
        }
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintWildlings();
        }
    }

    /**
     * Метод считает и изменяет снабжение каждого из игроков
     */
    void adjustSupply() {
        int time = Controller.getInstance().getTime();
        // На первом ходу мы подсчитываем снабжение с помощью этого метода,
        // но не выводим на экран сопутствующие событию фразы.
        if (time > 1) {
            say(SUPPLY_HAPPENS);
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            supply[player] = 0;
        }
        for (int area = 0; area < NUM_AREA; area++) {
            int areaOwner = getAreaOwner(area);
            if (areaOwner >= 0 && map.getNumBarrel(area) > 0) {
                supply[areaOwner] = Math.min(supply[areaOwner] + map.getNumBarrel(area), MAX_SUPPLY);
            }
        }
        if (time > 1) {
            for (int player = 0; player < NUM_PLAYER; player++) {
                say(SUPPLY_OF + HOUSE_GENITIVE[player] + EQUALS + supply[player] + ".");
            }
        }
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintSupply();
        }
    }

    /**
     * Метод располагает игроков на определённом треке в порядке их ставок, сразу после вскрытия этих ставок
     * @param track номер трека
     */
    void trackPreArrange(int track) {
        boolean isPlayerCounted[] = new boolean[NUM_PLAYER];
        int newTrackPlayerOnPlace[] = new int[NUM_PLAYER];
        for (int place = 0; place < NUM_PLAYER; place++) {
            int curMaxBid = -1;
            int curMaxBidder = -1;
            for (int player = 0; player < NUM_PLAYER; player++) {
                if (!isPlayerCounted[player] && (currentBids[player] > curMaxBid || currentBids[player] == curMaxBid &&
                        trackPlaceForPlayer[0][player] < trackPlaceForPlayer[0][curMaxBidder])) {
                    curMaxBidder = player;
                    curMaxBid = currentBids[player];
                }
            }
            newTrackPlayerOnPlace[place] = curMaxBidder;
            isPlayerCounted[curMaxBidder] = true;
        }
        trackPlayerOnPlace[track] = newTrackPlayerOnPlace;
        fillTrackPlaceForPlayer(track);
    }

    /**
     * Метод разыгрывает событие "Игра престолов"
     */
    void playGameOfThrones() {
        int[] earningOfPlayer = new int[NUM_PLAYER];
        for (int area = 0; area < NUM_AREA; area++) {
            int areaOwner = getAreaOwner(area);
            if (areaOwner >= 0) {
                earningOfPlayer[areaOwner] += map.getNumCrown(area);
                if (map.getAreaType(area) == AreaType.port && (getTroopsOwner(map.getSeaNearPort(area)) < 0 ||
                        getTroopsOwner(map.getSeaNearPort(area)) == areaOwner)) {
                    earningOfPlayer[areaOwner]++;
                }
            }
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (earningOfPlayer[player] > 0) {
                earnTokens(player, earningOfPlayer[player]);
            }
        }
    }

    /**
     * Метод спешивает всех рыцарей игрока. Вызывается при победе "Убийц ворон".
     * @param player номер игрока
     */
    void dismountAllKnights(int player) {
        ArrayList<Integer> areasWithKnights = getAreasWithUnitsOfType(player, UnitType.knight);
        for (int area: areasWithKnights) {
            while (armyInArea[area].hasUnitOfType(UnitType.knight)) {
                downgradeKnightInArea(area, player);
            }
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintArea(area);
            }
        }
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    /**
     * Метод уничтожает всех рыцарей игрока. Вызывается при победе "Убийц ворон".
     * @param player номер игрока
     */
    void killAllKnights(int player) {
        ArrayList<Integer> areasWithKnights = getAreasWithUnitsOfType(player, UnitType.knight);
        for (int area: areasWithKnights) {
            while (armyInArea[area].hasUnitOfType(UnitType.knight)) {
                armyInArea[area].killUnitOfType(UnitType.knight, this);
                restingUnitsOfPlayerAndType[player][UnitType.knight.getCode()]++;
            }
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintArea(area);
            }
        }
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    private void upgradePawnInArea(int area, int player) {
        armyInArea[area].changeType(UnitType.pawn, UnitType.knight);
        restingUnitsOfPlayerAndType[player][UnitType.pawn.getCode()]++;
        restingUnitsOfPlayerAndType[player][UnitType.knight.getCode()]--;
    }

    /**
     * Метод валидирует вариант улучшения пешек, предложенный игроком
     * @param pawnUpgradeVariant вариант улучшения пешек
     * @param maxPawnsToUpgrade  максимальное число пешек, которых можно таким образом посвятить в рыцари
     * @return true, если такой вариант допустим
     */
    boolean validatePawnsToUpgrade(UnitExecutionPlayed pawnUpgradeVariant, int player, int maxPawnsToUpgrade) {
        if (pawnUpgradeVariant.getNumUnits() > maxPawnsToUpgrade) {
            say(TOO_MUCH_PAWNS_TO_UPGRAGE_ERROR);
            return false;
        }
        HashMap<Integer, Integer> numUnitsInArea = pawnUpgradeVariant.getNumberOfUnitsInArea();
        for (int area: numUnitsInArea.keySet()) {
            if (area < 0 || area >= NUM_AREA) {
                say(WRONG_AREA_ERROR);
                return false;
            }
            if (armyInArea[area].getOwner() != player) {
                say(NOT_YOURS_ERROR);
                return false;
            }
            if (armyInArea[area].getNumUnitOfType(UnitType.pawn) < numUnitsInArea.get(area)) {
                say(LACK_OF_PAWNS_TO_UPGRADE_ERROR);
                return false;
            }
        }
        return true;
    }

    private void downgradeKnightInArea(int area, int player) {
        armyInArea[area].changeType(UnitType.knight, UnitType.pawn);
        restingUnitsOfPlayerAndType[player][UnitType.knight.getCode()]++;
        restingUnitsOfPlayerAndType[player][UnitType.pawn.getCode()]--;
    }

    /**
     * Метод валидирует вариант экзекуции над рыцарями, предложенный игроком
     * @param executionVariant  вариант расстановки юнитов для экзекуции
     * @param player            номер игрока
     * @param numberKnightsToDo максимальное число рыцарей, которых можно таким образом подвергнуть экзекуции
     * @return true, если такой вариант допустим
     */
    boolean validateKnightsToDo(UnitExecutionPlayed executionVariant, int player, int numberKnightsToDo) {
        if (executionVariant.getNumUnits() != numberKnightsToDo) {
            say(WRONG_NUMBER_OF_KNIGHTS_TO_DO_ERROR);
            return false;
        }
        HashMap<Integer, Integer> numUnitsInArea = executionVariant.getNumberOfUnitsInArea();
        for (int area: numUnitsInArea.keySet()) {
            if (area < 0 || area >= NUM_AREA) {
                say(WRONG_AREA_ERROR);
                return false;
            }
            if (armyInArea[area].getOwner() != player) {
                say(NOT_YOURS_ERROR);
                return false;
            }
            if (armyInArea[area].getNumUnitOfType(UnitType.knight) < numUnitsInArea.get(area)) {
                say(LACK_OF_KNIGHTS_TO_DO_ERROR);
                return false;
            }
        }
        return true;
    }

    boolean validateDisband(DisbandPlayed disbandVariant, int player, DisbandReason reason) {
        HashMap<Integer, ArrayList<UnitType>> disbands = disbandVariant.getDisbandUnits();
        int numDisbands = disbandVariant.getNumberDisbands();
        if (numDisbands == 0){
            say(EMPTY_DISBAND_ERROR);
            return false;
        }
        if (reason != DisbandReason.supply && reason.getNumDisbands() != numDisbands) {
            say(WRONG_DISBANDS_NUMBER_ERROR + reason.getNumDisbands() + DISBAND_RECEIVED + numDisbands);
            return false;
        }
        int[] typesNumber = new int[NUM_UNIT_TYPES];
        for (int area: disbands.keySet()) {
            if (area < 0 || area >= NUM_AREA) {
                say(WRONG_AREA_ERROR);
                return false;
            }
            if (armyInArea[area].getOwner() != player) {
                say(NOT_YOURS_ERROR);
                return false;
            }
            for (UnitType unitType: UnitType.values()) {
                typesNumber[unitType.getCode()] = 0;
            }
            for (UnitType type: disbands.get(area)) {
                typesNumber[type.getCode()]++;
            }
            for (UnitType unitType: UnitType.values()) {
                if (armyInArea[area].getNumUnitOfType(unitType) < typesNumber[unitType.getCode()]) {
                    say(UNSUFFICIENT_UNITS_IN_ARMY + unitType + FOR_DISBAND);
                    return false;
                }
            }
        }
        if (reason == DisbandReason.hordeCastle && disbands.size() > 1) {
            say(HORDE_CASTLE_MULTIPLE_AREAS_ERROR);
            return false;
        }
        if (reason == DisbandReason.supply) {
            boolean result = supplyTestForDisband(disbandVariant, player);
            if (!result) {
                say(DISBAND_SUPPLY_VIOLATION_ERROR);
            }
            return result;
        }
        return true;
    }

    /**
     * Метод возвращает список областей, где у определённого игрока есть юниты определённого типа
     * @param player номер игрока
     * @param type   тип юнитов
     * @return список областей
     */
    ArrayList<Integer> getAreasWithUnitsOfType(int player, UnitType type) {
        ArrayList<Integer> areasWithUnits = new ArrayList<>();
        for (int area: areasWithTroopsOfPlayer.get(player).keySet()) {
            if (armyInArea[area].hasUnitOfType(type)) {
                areasWithUnits.add(area);
            }
        }
        return areasWithUnits;
    }

    /**
     * Метод возвращает количества юнитов игрока определённого типа в его областях
     * @param player номер игрока
     * @param type   тип юнита
     * @return карта, в которой ключом является номер области,
     *         а значением - количество юнитов выбранного типа в этой области
     */
    public HashMap<Integer, Integer> getNumUnitsOfTypeInArea(int player, UnitType type) {
        HashMap<Integer, Integer> numUnitsOfTypeInArea = new HashMap<>();
        for (int area: areasWithTroopsOfPlayer.get(player).keySet()) {
            if (armyInArea[area].hasUnitOfType(type)) {
                numUnitsOfTypeInArea.put(area, armyInArea[area].getNumUnitOfType(type));
            }
        }
        return numUnitsOfTypeInArea;
    }

    HashSet<Integer> getAreasToRetreat() {
        return areasToRetreat;
    }

    /**
     * Метод возвращает полное количество всех юнитов определённого игрока
     * @param player номер игрока
     * @return количество юнитов игрока
     */
    int getTotalNumberOfUnits(int player) {
        int total = 0;
        for (Map.Entry<Integer, Integer> entry: areasWithTroopsOfPlayer.get(player).entrySet()) {
            total += entry.getValue();
        }
        return total;
    }

    void renewArea(int area, int exOwner) {
        if (exOwner >= 0) {
            areasWithTroopsOfPlayer.get(exOwner).remove(area);
        }
        renewHouseTroopsInArea(area);
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintArea(area);
        }
    }

    void setOrderInArea(int area, Order order) {
        orderInArea[area] = order;
    }

    @SuppressWarnings("ConstantConditions")
    void safeSetOrderInArea(int area, Order order, int player) {
        if (orderInArea[area] != null) {
            switch(orderInArea[area].orderType()) {
                case raid:
                    areasWithRaids.get(player).remove(area);
                    break;
                case march:
                    areasWithMarches.get(player).remove(area);
                    break;
                case consolidatePower:
                    areasWithCPs.get(player).remove(area);
                    break;
            }
        }
        if (order != null) {
            switch(order.orderType()) {
                case raid:
                    areasWithRaids.get(player).add(area);
                    break;
                case march:
                    areasWithMarches.get(player).add(area);
                    break;
                case consolidatePower:
                    areasWithCPs.get(player).add(area);
                    break;
            }
        }
        setOrderInArea(area, order);
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintArea(area);
        }
    }

    boolean hasRaids(int player) {
        return !areasWithRaids.get(player).isEmpty();
    }

    boolean hasMarches(int player) {
        return !areasWithMarches.get(player).isEmpty();
    }

    void removeAllRaidsOfPlayer(int player) {
        for (int area: areasWithRaids.get(player)) {
            setOrderInArea(area, null);
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintArea(area);
            }
        }
    }

    public GameOfThronesMap getMap() {
        return map;
    }

    void setCurrentBidTrack(int cbt) {
        currentBidTrack = cbt;
    }

    void setCurrentBids(int[] bids) {
        currentBids = bids;
    }

    /**
     * Метод возвращает множество областей, где есть войска определённого игрока
     * @param player номер игрока
     * @return множество областей
     */
    public Set<Integer> getAreasWithTroopsOfPlayer(int player) {
        return areasWithTroopsOfPlayer.get(player).keySet();
    }

    /**
     * Метод возвращает карту областей, где есть войска определённого игрока, с количествами юнитов в них
     * @param player номер игрока
     * @return карта область-размер армии игрока в этой области
     */
    public Map<Integer, Integer> getAreasWithTroopsOfPlayerAndSize(int player) {
        return areasWithTroopsOfPlayer.get(player);
    }

    public ArrayList<HashSet<Integer>> getAreasWithRaidsOfPlayer() {
        return areasWithRaids;
    }

    public ArrayList<HashSet<Integer>> getAreasWithMarchesOfPlayer() {
        return areasWithMarches;
    }

    public ArrayList<HashSet<Integer>> getAreasWithCPsOfPlayer() {
        return areasWithCPs;
    }

    public int getNumPowerTokensHouse(int player) {
        return nPowerTokensHouse[player];
    }

    void printAreaWithTroopsOfPlayers() {
        for (int player = 0; player < NUM_PLAYER; player++) {
            System.out.print(AREAS_WITH_TROOPS_OF + HOUSE_GENITIVE[player] + ": ");
            boolean firstFlag = true;
            for (int area: areasWithTroopsOfPlayer.get(player).keySet()) {
                firstFlag = LittleThings.printDelimiter(firstFlag);
                System.out.print(map.getAreaNameRus(area) + " (" + areasWithTroopsOfPlayer.get(player).get(area) + ")");
            }
            System.out.println();
        }
    }

    public int getGarrisonInArea(int area) {
        return garrisonInArea[area];
    }

    /**
     * Метод возвращает приказ, лежащий в данной области карты
     * @param area номер области
     * @return приказ в данной области, или null, если там нет приказа
     */
    public Order getOrderInArea(int area) {
        return orderInArea[area];
    }

    public int getWildlingsStrength() {
        return wildlingsStrength;
    }

    public int[] getSupply() {
        return supply;
    }

    public int getSupply(int player) {
        return supply[player];
    }

    public int[] getVictoryPoints() {
        return victoryPoints;
    }

    public int getVictoryPoints(int player) {
        return victoryPoints[player];
    }

    public int getNumFortress(int player) {
        return numFortress[player];
    }

    public Army getAttackingArmy() {
        return attackingArmy;
    }

    public Army getRetreatingArmy() {
        return retreatingArmy;
    }

    int getHouseHomeLandInArea(int area) {
        return houseHomeLandInArea[area];
    }

    public BattleInfo getBattleInfo() {
        return battleInfo;
    }

    public int getNumActiveCardsOfPlayer(int player) {
        return numActiveHouseCardsOfPlayer[player];
    }

    public int getBattleArea() {
        return battleInfo == null ? -1 : battleInfo.getAreaOfBattle();
    }

    public boolean getIsSwordUsed() {
        return isSwordUsed;
    }

    public int getTokensOfPlayer(int player) {
        return nPowerTokensHouse[player];
    }

    public int getMaxTokensOfPlayer(int player) {
        return maxPowerTokensHouse[player];
    }

    public int getCurrentBiddingTrack() {
        return currentBidTrack;
    }

    public int getCurrentBidOfPlayer(int player) {
        return currentBids[player];
    }

    /**
     * Возвращает текущее событие в определённой колоде
     * @param deckNumber номер колоды от 1 до 3
     * @return текущее событие этой колоды
     */
    public Happenable getEvent(int deckNumber) {
        return event[deckNumber - 1];
    }

    public int getNumRemainingCards(Happenable h) {
        return numRemainingCards.get(h);
    }

    public OrderType getForbiddenOrder() {
        return forbiddenOrder;
    }

    /**
     * Метод возвращает номер дома, которому принадлежит жетон власти на этой области
     * @param area номер области
     * @return номер дома-автора жетона власти, или -1, если жетона власти на области нет.
     */
    public int getPowerTokenInArea(int area) {
        return powerTokenOnArea[area];
    }

    /**
     * Метод возвращает владельца войск в данной области карты
     * @param area номер области
     * @return номер Дома, которому принадлежат войска, или -1, если войск нет
     */
    public int getTroopsOwner(int area) {
        return armyInArea[area].getOwner();
    }

    /**
     * Метод возвращает владельца войск в данной области карты или гарнизона, если он есть. Если ничего из этого нет,
     * то возвращает -1.
     * @param area номер области
     * @return номер Дома, которому принадлежат войска или гарнизон
     */
    public int getTroopsOrGarrisonOwner(int area) {
        int troopsOwner = getTroopsOwner(area);
        if (troopsOwner >= 0) {
            return troopsOwner;
        } else if (getAreaOwner(area) >= 0 && garrisonInArea[area] > 0) {
            return getAreaOwner(area);
        } else {
            return -1;
        }
    }

    public int getAreaOwner(int area) {
        int troopsOwner = getTroopsOwner(area);
        if (troopsOwner >= 0) {
            return troopsOwner;
        } else if (powerTokenOnArea[area] >= 0) {
            return powerTokenOnArea[area];
        } else {
            return houseHomeLandInArea[area];
        }
    }

    WildlingCard getFirstWildlingsCard() {
        return wildlingDeck.getFirst();
    }

    void buryWildlings() {
        WildlingCard card = wildlingDeck.pollFirst();
        wildlingDeck.addLast(card);
    }

    WildlingCard getTopWildlingsCard() {
        return topWildlingCard;
    }

    public int getTopWildlingsCardCode() {
        return topWildlingCard == null ? -1 : topWildlingCard.getCode();
    }

    /**
     * Метод возвращает место определённого игрока на определённом треке влияния
     * @param track  номер трека влияния
     * @param player номер игрока
     * @return место
     */
    public int getTrackPlaceForPlayer(int track, int player) {
        return trackPlaceForPlayer[track][player];
    }

    /**
     * Метод возвращает игрока на определённом месте треке влияния
     * @param track номер трека влияния
     * @param place место
     * @return место
     */
    public int getTrackPlayerOnPlace(int track, int place) {
        return trackPlayerOnPlace[track][place];
    }

    /**
     * Метод возвращает массив игроков на определённом месте треке влияния
     * @param track номер трека влияния
     * @return место
     */
    public int[] getTrackPlayerOnPlace(int track) {
        return trackPlayerOnPlace[track];
    }

    /**
     * Метод отвечает, активна ли определённая карта определённого игрока
     * @param player    игрок
     * @param cardIndex номер карты
     * @return true, если активна
     */
    public boolean isCardActive(int player, int cardIndex) {
        return houseCardOfPlayer[player][cardIndex].isActive();
    }

    public Army getArmyInArea(int area) {
        return armyInArea[area];
    }

    /**
     * Метод возвращает количество юнитов определённого типа в запаса определённого игрока
     * @param player   номер игрока
     * @param unitType тип юнита
     * @return количество оставшихся в запасе юнитов
     */
    public int getRestingUnits(int player, UnitType unitType) {
        return restingUnitsOfPlayerAndType[player][unitType.getCode()];
    }

    /**
     * Метод отвечает, начнётся ли бой в области, если определённый игрок сунется туда своими отрядами
     * @param area   номер области
     * @param player номер игрока
     * @return true, если начнётся бой
     */
    public boolean isBattleBeginInArea(int area, int player) {
        return armyInArea[area].getOwner() >= 0 && armyInArea[area].getOwner() != player ||
                getAreaOwner(area) != player && garrisonInArea[area] > 0;
    }

    /**
     * Метод отвечает, есть ли в данной области нейтральный гарнизон
     * @param area номер области
     * @return true, если есть нейтральный гарнизон
     */
    public boolean isNeutralGarrisonInArea(int area) {
        return getAreaOwner(area) < 0 && garrisonInArea[area] > 0;
    }

    /**
     * Для данного варианта похода проверяет, нарушается ли предел снабжения.
     * Вынесено в отдельный метод, чтобы игроки тоже могли проверять свои походы на снабжение.
     * @param march вариант похода
     * @return true, если всё нормально
     */
    public boolean supplyTestForMarch(MarchOrderPlayed march) {
        int from = march.getAreaFrom();
        int player = getTroopsOwner(from);
        virtualAreasWithTroops.clear();
        virtualAreasWithTroops.putAll(areasWithTroopsOfPlayer.get(player));
        for (Map.Entry<Integer, ArrayList<UnitType>> entry : march.getDestinationsOfMarch().entrySet()) {
            int curDestination = entry.getKey();
            ArrayList<UnitType> curUnits = entry.getValue();
            int nLeavingUnits = curUnits.size();
            int newNTroops = nLeavingUnits;
            if (virtualAreasWithTroops.containsKey(curDestination)) {
                newNTroops += virtualAreasWithTroops.get(curDestination);
            }
            virtualAreasWithTroops.put(curDestination, newNTroops);
            int nRestingUnitsFrom = virtualAreasWithTroops.get(from) - nLeavingUnits;
            if (nRestingUnitsFrom > 0) {
                virtualAreasWithTroops.put(from, nRestingUnitsFrom);
            } else {
                virtualAreasWithTroops.remove(from);
            }
        }
        return GameUtils.supplyTest(virtualAreasWithTroops, supply[player]);
    }

    /**
     * Для данного варианта сбора войск в области проверяет, нарушается ли предел снабжения.
     * Вынесено в отдельный метод, чтобы игроки тоже могли проверять свои сборы войск на снабжение.
     * @param muster вариант сбора войск
     * @return true, если всё нормально
     */
    public boolean supplyTestForMuster(MusterPlayed muster) {
        int areaOfCastle = muster.getCastleArea();
        int player = getAreaOwner(areaOfCastle);
        virtualAreasWithTroops.clear();
        virtualAreasWithTroops.putAll(areasWithTroopsOfPlayer.get(player));
        for (int index = 0; index < muster.getNumberMusterUnits(); index++) {
            int area = muster.getArea(index);
            Musterable musterable = muster.getMusterUnit(index);
            if (musterable instanceof UnitType) {
                if (virtualAreasWithTroops.containsKey(area)) {
                    int previousNumOfTroops = virtualAreasWithTroops.get(area);
                    virtualAreasWithTroops.put(area, previousNumOfTroops + 1);
                } else {
                    virtualAreasWithTroops.put(area, 1);
                }
            }
        }
        return GameUtils.supplyTest(virtualAreasWithTroops, supply[player]);
    }

    /**
     * Проверка на снабжения для данного варианта роспуска войск. Имеет смысл только для роспуска войск в результате
     * уменьшения уровня снабжения.
     * @param disbandVariant вариант роспуска войск
     * @param player         номер игрока
     * @return true, если вписываемся в снабжение
     */
    public boolean supplyTestForDisband(DisbandPlayed disbandVariant, int player) {
        virtualAreasWithTroops.clear();
        virtualAreasWithTroops.putAll(areasWithTroopsOfPlayer.get(player));
        for (Map.Entry<Integer, ArrayList<UnitType>> entry: disbandVariant.getDisbandUnits().entrySet()) {
            int area = entry.getKey();
            int numDeletedUnits = entry.getValue().size();
            virtualAreasWithTroops.put(area, virtualAreasWithTroops.get(area) - numDeletedUnits);
        }
        return GameUtils.supplyTest(virtualAreasWithTroops, supply[player]);
    }

    public boolean portTroopsTestForMuster(MusterPlayed muster) {
        int numShipsInPort = 0;
        for (int i = 0; i < muster.getNumberMusterUnits(); i++) {
            if (map.getAreaType(muster.getArea(i)) == AreaType.port) {
                if (numShipsInPort == 0) {
                    numShipsInPort = armyInArea[muster.getArea(i)].getSize();
                }
                numShipsInPort++;
            }
        }
        return numShipsInPort <= MAX_TROOPS_IN_PORT;
    }

    void moveAttackingUnits(ArrayList<UnitType> unitTypes, int from) {
        int player = armyInArea[from].getOwner();
        for (UnitType type: unitTypes) {
            Unit unit = armyInArea[from].getUnitOfType(type);
            armyInArea[from].deleteUnit(unit);
            attackingArmy.addUnit(unit);
        }
        renewArea(from, player);
        renewArea(areaOfBattle, -1);
    }

    void moveUnits(ArrayList<UnitType> unitTypes, int from, int to) {
        int player = armyInArea[from].getOwner();
        // Если в области имелся нейтральный гарнизон, то пробиваем его
        if (isNeutralGarrisonInArea(to)) {
            say(GARRISON_IS_DEFEATED + HOUSE_GENITIVE[player] + " - " +
                    calculatePowerOfPlayerVersusGarrison(player, to, unitTypes, orderInArea[from].getModifier()) +
                    GARRISON_STRENGTH_IS + garrisonInArea[to]);
            garrisonInArea[to] = 0;
        }
        int exOwner = getAreaOwner(to);
        for (UnitType type: unitTypes) {
            Unit unit = armyInArea[from].getUnitOfType(type);
            armyInArea[from].deleteUnit(unit);
            armyInArea[to].addUnit(unit);
        }
        renewArea(from, player);
        renewArea(to, exOwner);
        if (map.getNumCastle(to) > 0) {
            adjustVictoryPoints();
        }
    }

    void leavePowerToken(int area, int player) {
        say(HOUSE[player] + LEAVES_POWER_TOKEN + map.getAreaNameRusLocative(area) + ".");
        powerTokenOnArea[area] = player;
        nPowerTokensHouse[player]--;
        maxPowerTokensHouse[player]--;
    }

    void loseVictoryPoints(int player) {
        victoryPoints[player]--;
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintVictory();
        }
    }

    void tryToDeletePowerToken(int area) {
        int powerTokenOwner = powerTokenOnArea[area];
        if (powerTokenOwner >= 0) {
            powerTokenOnArea[area] = -1;
            maxPowerTokensHouse[powerTokenOwner]++;
            if (!Settings.getInstance().isPassByRegime()) {
                houseTabPanel.repaintHouse(powerTokenOwner);
            }
        }
    }

    void sayPreBattleText() {
        say(attackingArmy.toString() +
                (attackingArmy.getSize() == 1 ? MOVES_TO : MOVE_TO) +
                map.getAreaNameRusAccusative(areaOfBattle) +
                (attackingArmy.getSize() == 1 ? AND_FIGHTS : AND_FIGHT));
    }

    void prepareBattle(int areaOfMarch) {
        this.areaOfMarch = areaOfMarch;
        playerOnSide[0] = attackingArmy.getOwner();
        if (armyInArea[areaOfBattle].getOwner() >= 0) {
            playerOnSide[1] = armyInArea[areaOfBattle].getOwner();
        } else if (houseHomeLandInArea[areaOfBattle] >= 0) {
            playerOnSide[1] = houseHomeLandInArea[areaOfBattle];
        } else {
            // Предполагаем, что все области с гарнизонами, принадлежащие игрокам, также являются престольными землями.
            // Это верно для классической Игры престолов, но может быть неверно для дополнений.
            say(NO_DEFENDER_ERROR);
            return;
        }
        say(BATTLE_BEGINS_FOR + map.getAreaNameRusAccusative(areaOfBattle) + BETWEEN +
                HOUSE_ABLATIVE[playerOnSide[0]] + " и " + HOUSE_ABLATIVE[playerOnSide[1]] + ".");
        if (Settings.getInstance().isTrueAutoSwitchTabs()) {
            tabPanel.setSelectedIndex(TabEnum.fight.getCode());
        }
        if (battleInfo == null) {
            battleInfo = new BattleInfo(this);
        }
        battleInfo.setNewBattle(playerOnSide[0], playerOnSide[1], areaOfBattle,
                orderInArea[areaOfMarch].getModifier(), map.getNumCastle(areaOfBattle) > 0);
        battleInfo.addArmyToSide(SideOfBattle.attacker, attackingArmy);
        battleInfo.setGarrisonModifier(garrisonInArea[areaOfBattle]);
        if (!armyInArea[areaOfBattle].isEmpty()) {
            battleInfo.addArmyToSide(SideOfBattle.defender, armyInArea[areaOfBattle]);
        }

        if (orderInArea[areaOfBattle] != null && orderInArea[areaOfBattle].orderType() == OrderType.defence) {
            battleInfo.setDefenceModifier(orderInArea[areaOfBattle].getModifier());
        }
        if (!Settings.getInstance().isPassByRegime()) {
            fightTabPanel.repaint();
        }
    }

    void prepareSupportInfo() {
        supporters.clear();
        areasOfSupport.clear();
        for (int adjacentArea: map.getAdjacentAreas(areaOfBattle)) {
            if (orderInArea[adjacentArea] != null && orderInArea[adjacentArea].orderType() == OrderType.support &&
                    map.getAdjacencyType(adjacentArea, areaOfBattle) != AdjacencyType.landToSea &&
                    map.getAdjacencyType(adjacentArea, areaOfBattle) != AdjacencyType.portOfCastle &&
                    map.getAdjacencyType(adjacentArea, areaOfBattle) != AdjacencyType.castleWithPort) {
                supporters.add(getTroopsOwner(adjacentArea));
                areasOfSupport.add(adjacentArea);
            }
        }
    }

    ArrayList<Integer> getAreasOfSupport() {
        return areasOfSupport;
    }

    HashSet<Integer> getSupporters() {
        return supporters;
    }

    void setSupports(SideOfBattle[] supportOfPlayer) {
        System.arraycopy(supportOfPlayer, 0, this.supportOfPlayer, 0, NUM_PLAYER);
        for (int areaOfSupport: areasOfSupport) {
            battleInfo.addSupportingArmyToSide(supportOfPlayer[getTroopsOwner(areaOfSupport)], armyInArea[areaOfSupport],
                    orderInArea[areaOfSupport].getModifier());
        }
        if (!Settings.getInstance().isPassByRegime()) {
            fightTabPanel.repaint();
        }
    }

    void setHouseCardOnSide(int side, HouseCard card) {
        houseCardOfSide[side] = card;
        battleInfo.setHouseCardForPlayer(battleInfo.getPlayerOnSide(side), card);
        countBattleVariables();
    }

    int getAreaWhereBattleBegins() {
        return areaOfBattle;
    }

    void decreaseNumActiveHouseCards(int player) {
        numActiveHouseCardsOfPlayer[player]--;
    }

    HouseCard getHouseCardOfPlayer(int player, int numCard) {
        return houseCardOfPlayer[player][numCard];
    }

    void useSword() {
        int swordsMan = trackPlayerOnPlace[TrackType.valyrianSword.getCode()][0];
        battleInfo.useSwordOnSide(playerOnSide[0] == swordsMan ? SideOfBattle.attacker : SideOfBattle.defender);
        isSwordUsed = true;
        say(HOUSE[swordsMan] + USES_SWORD);
    }

    void destabilizeCards() {
        // Делаем неактивными карты, которые были сыграны, и возвращаем к жизни карту, отменённую Тирионом
        if (temporaryInactiveCard != null) {
            temporaryInactiveCard.setActive(true);
            numActiveHouseCardsOfPlayer[temporaryInactiveCard.house()]++;
            houseTabPanel.repaintHouse(temporaryInactiveCard.house());
            temporaryInactiveCard = null;
        }
        for (int side = 0; side < 2; side++) {
            if (houseCardOfSide[side] != HouseCard.none) {
                // Если это была последняя активная карта игрока, то обновляем ему колоду
                if (numActiveHouseCardsOfPlayer[playerOnSide[side]] == 0) {
                    renewHandExceptCard(playerOnSide[side], houseCardOfSide[side]);
                }
            }
        }
    }

    void retreat(int area) {
        armyInArea[area].addSubArmy(retreatingArmy);
        say(HOUSE[loser] + RETREATS_IN + map.getAreaNameRusAccusative(area));
        renewArea(area, loser);
        retreatingArmy.deleteAllUnits();
        if (!map.getAreaType(areaOfBattle).isNaval()) {
            adjustVictoryPoints();
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintVictory();
            }
        }
    }

    void deleteRandomMarchOfPlayer(int player) {
        int area = LittleThings.getRandomElementOfSet(areasWithMarches.get(player));
        orderInArea[area] = null;
        areasWithMarches.get(player).remove(area);
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintArea(area);
        }
    }

    void removeAreaWithMarch(int area, int player) {
        areasWithMarches.get(player).remove(area);
    }

    void clearAreasWithCP() {
        for (int player = 0; player < NUM_PLAYER; player++) {
            areasWithCPs.get(player).clear();
        }
    }

    void tryToToWreckSomeShips(int portArea, int exOwner, int numShipsToShips) {
        safeDeleteOrderInArea(portArea, exOwner);
        areasWithTroopsOfPlayer.get(exOwner).remove(portArea);
        if (numShipsToShips > 0) {
            armyInArea[portArea].wreckSomeShips(numShipsToShips, this);
        }
    }

    void captureAllShips(int portArea, int exOwner, int newOwner) {
        armyInArea[portArea].setOwner(newOwner);
        int numCapturedShips = armyInArea[portArea].getSize();
        renewArea(portArea, exOwner);
        restingUnitsOfPlayerAndType[exOwner][UnitType.ship.getCode()] += numCapturedShips;
        restingUnitsOfPlayerAndType[newOwner][UnitType.ship.getCode()] -= numCapturedShips;
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(exOwner);
            houseTabPanel.repaintHouse(newOwner);
        }
    }

    void wreckAllShips(int portArea) {
        int exOwner = armyInArea[portArea].getOwner();
        restingUnitsOfPlayerAndType[exOwner][UnitType.ship.getCode()] += armyInArea[portArea].getSize();
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(exOwner);
        }
        armyInArea[portArea].killAllUnits(KillingReason.shipwreck, this);
        safeDeleteOrderInArea(portArea, exOwner);
        renewArea(portArea, exOwner);
    }

    void wreckRestingShips(int portArea, int newOwner, int numShipsToKill) {
        armyInArea[portArea].killSomeUnits(numShipsToKill, KillingReason.shipwreck, this);
        renewArea(portArea, newOwner);
        restingUnitsOfPlayerAndType[newOwner][UnitType.ship.getCode()] += numShipsToKill;
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(newOwner);
        }
    }

    void setForbiddenOrder(OrderType type) {
        forbiddenOrder = type;
    }

    ArrayList<HashSet<Integer>> getAreasWithMusterOfPlayers() {
        ArrayList<HashSet<Integer>> areasOfPlayerWithMuster = new ArrayList<>();
        for (int player = 0; player < NUM_PLAYER; player++) {
            areasOfPlayerWithMuster.add(new HashSet<>());
        }
        for (int area = 0; area < NUM_AREA; area++) {
            if (map.getNumCastle(area) > 0) {
                int areaOwner = getAreaOwner(area);
                if (areaOwner >= 0) {
                    areasOfPlayerWithMuster.get(areaOwner).add(area);
                }
            }
        }
        return areasOfPlayerWithMuster;
    }

    HashSet<Integer> getAreasWithMusterOfPlayer(int player) {
        HashSet<Integer> areasWithMuster = new HashSet<>();
        for (int area = 0; area < NUM_AREA; area++) {
            if (map.getNumCastle(area) > 0 && getAreaOwner(area) == player) {
                areasWithMuster.add(area);
            }
        }
        return areasWithMuster;
    }

    void playerPays(int player, int sum) {
        nPowerTokensHouse[player] -= sum;
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    void setTrackPlayerOnPlace(int trackCode, int place, int player) {
        trackPlayerOnPlace[trackCode][place] = player;
    }

    void wildlingsBeginToAttack() {
        say(WILDLINGS_ATTACK_WITH_STRENGTH + wildlingsStrength + "!");
        if (topWildlingCard != null) {
            wildlingDeck.addLast(topWildlingCard);
        }
        topWildlingCard = null;
        mapPanel.repaintWildlingsCard();
    }

    public int getPreemptiveRaidCheater() {
        return preemptiveRaidCheater;
    }

    void revealWildlings() {
        topWildlingCard = wildlingDeck.pollFirst();
        StringBuilder sb = new StringBuilder(BIDS_ARE);
        boolean firstFlag = true;
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (player == preemptiveRaidCheater) continue;
            if (firstFlag) {
                firstFlag = false;
            } else {
                sb.append(", ");
            }
            sb.append(HOUSE[player]).append(": ").append(currentBids[player]);
        }
        say(sb.toString());
        say(WILDLINGS_ARE + topWildlingCard);
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintTracks();
            mapPanel.repaintWildlingsCard();
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (player == preemptiveRaidCheater) continue;
            nPowerTokensHouse[player] -= currentBids[player];
        }
        houseTabPanel.repaint();
    }

    void setWildlingsStrength(int strength) {
        wildlingsStrength = strength;
    }

    void loseAllMoney(int player) {
        say(HOUSE[player] + LOSES_ALL_MONEY);
        nPowerTokensHouse[player] = 0;
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    void loseHighestCards(int player) {
        if (numActiveHouseCardsOfPlayer[player] > 1) {
            int maxCardStrength = -1;
            int lastLostCard = -1;
            for (int card = 0; card < NUM_HOUSE_CARDS; card++) {
                if (houseCardOfPlayer[player][card].isActive() &&
                        houseCardOfPlayer[player][card].getStrength() > maxCardStrength) {
                    maxCardStrength = houseCardOfPlayer[player][card].getStrength();
                }
            }
            for (int card = 0; card < NUM_HOUSE_CARDS; card++) {
                if (houseCardOfPlayer[player][card].isActive() &&
                        houseCardOfPlayer[player][card].getStrength() == maxCardStrength) {
                    houseCardOfPlayer[player][card].setActive(false);
                    numActiveHouseCardsOfPlayer[player]--;
                    lastLostCard = card;
                    say(HOUSE[player] + LOSES_CARD + houseCardOfPlayer[player][card].getName());
                }
            }
            if (numActiveHouseCardsOfPlayer[player] == 0) {
                renewHandExceptCard(player, houseCardOfPlayer[player][lastLostCard]);
            }
            if (!Settings.getInstance().isPassByRegime()) {
                houseTabPanel.repaintHouse(player);
            }
        }
    }

    void loseCard(HouseCard card) {
        card.setActive(false);
        int player = card.house();
        numActiveHouseCardsOfPlayer[player]--;
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
        say(HOUSE[player] + LOSES_CARD + card.getName());
    }

    void returnCard(HouseCard card) {
        card.setActive(true);
        int player = card.house();
        numActiveHouseCardsOfPlayer[player]++;
        say(HOUSE[player] + RETURNS_CARD + card.getName() + ".");
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    /**
     * Метод возвращает список областей, где есть замок, и как минимум два отряда
     * @param player номер Дома
     * @return список уязвимых к низшей ставке областей
     */
    public ArrayList<Integer> getHordeVulnerableCastles(int player) {
        ArrayList<Integer> normCastles = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry: areasWithTroopsOfPlayer.get(player).entrySet()) {
            if (map.getNumCastle(entry.getKey()) > 0 && entry.getValue() > 1) {
                normCastles.add(entry.getKey());
            }
        }
        return normCastles;
    }

    /**
     * Метод отвечает, грозит ли игроку потерять двух юнитов в замке, если он окажется низшей ставкой
     * при "нашествии орды"
     * @param player номер игрока
     * @return true, если есть хотя бы один такой замок
     */
    public boolean isVulnerableToHorde(int player) {
        for (Map.Entry<Integer, Integer> entry: areasWithTroopsOfPlayer.get(player).entrySet()) {
            if (map.getNumCastle(entry.getKey()) > 0 && entry.getValue() > 1) {
                return true;
            }
        }
        return false;
    }

    void killAllUnitsInArea(int area) {
        int player = armyInArea[area].getOwner();
        armyInArea[area].killAllUnits(KillingReason.wildlings, this);
        renewArea(area, player);
    }

    public int getMaxUnitsOfPlayerAndType(int player, UnitType unitType) {
        return maxUnitsOfPlayerAndType[player][unitType.getCode()];
    }

    public int getNumAliveUnits(int player, UnitType unitType) {
        return maxUnitsOfPlayerAndType[player][unitType.getCode()] - restingUnitsOfPlayerAndType[player][unitType.getCode()];
    }

    void setPreemptiveRaidCheater(int player) {
        preemptiveRaidCheater = player;
        if (player >= 0) {
            say(HOUSE[player] + DOESNT_TAKE_PART);
        }
    }

    void executeKnightKilling(UnitExecutionPlayed variantToKillKnights, int player) {
        HashMap<Integer, Integer> numUnitsInArea = variantToKillKnights.getNumberOfUnitsInArea();
        for (int area : numUnitsInArea.keySet()) {
            for (int index = 0; index < numUnitsInArea.get(area); index++) {
                armyInArea[area].killUnitOfType(UnitType.knight, this);
                restingUnitsOfPlayerAndType[player][UnitType.knight.getCode()]++;
                renewHouseTroopsInArea(area);
            }
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintArea(area);
            }
        }
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    void executeKnightDismounting(UnitExecutionPlayed executionVariant, int player) {
        HashMap<Integer, Integer> numUnitsInArea = executionVariant.getNumberOfUnitsInArea();
        for (int area : numUnitsInArea.keySet()) {
            for (int index = 0; index < numUnitsInArea.get(area); index++) {
                downgradeKnightInArea(area, player);
            }
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintArea(area);
            }
        }
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    void executePawnUpgrade(UnitExecutionPlayed pawnUpgradeVariant, int player) {
        HashMap<Integer, Integer> numUnitsInArea = pawnUpgradeVariant.getNumberOfUnitsInArea();
        for (int area : numUnitsInArea.keySet()) {
            for (int index = 0; index < numUnitsInArea.get(area); index++) {
                upgradePawnInArea(area, player);
            }
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintArea(area);
            }
        }
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    void genericKnightDismount(int player, int numKnightToDowngrade) {
        ArrayList<Integer> areasWithKnights = getAreasWithUnitsOfType(player, UnitType.knight);
        int numKnightDowngraded = 0;
        outer:
        for (int area : areasWithKnights) {
            while (getArmyInArea(area).hasUnitOfType(UnitType.knight)) {
                downgradeKnightInArea(area, player);
                numKnightDowngraded++;
                if (numKnightDowngraded == numKnightToDowngrade) {
                    break outer;
                }
            }
        }
    }

    void genericSupplyDisband(int player) {
        ArrayList<Integer> areasWithSuchArmies = new ArrayList<>();
        do {
            int breakingArmySize = GameUtils.getBreakingArmySize(areasWithTroopsOfPlayer.get(player), supply[player]);
            areasWithSuchArmies.clear();
            for (Map.Entry<Integer, Integer> entry: areasWithTroopsOfPlayer.get(player).entrySet()) {
                if (entry.getValue() == breakingArmySize) {
                    areasWithSuchArmies.add(entry.getKey());
                }
            }
            int area = areasWithSuchArmies.get(random.nextInt(areasWithSuchArmies.size()));
            armyInArea[area].killWeakestUnit(this);
            renewArea(area, player);
        } while (!GameUtils.supplyTest(areasWithTroopsOfPlayer.get(player), supply[player]));
    }

    int genericHordeCastleDisband(int player) {
        Set<Integer> disbandAreas = new HashSet<>();
        for (int area : areasWithTroopsOfPlayer.get(player).keySet()) {
            if (map.getNumCastle(area) > 0 || armyInArea[area].getSize() >= 2) {
                disbandAreas.add(area);
            }
        }
        int area = LittleThings.getRandomElementOfSet(disbandAreas);
        armyInArea[area].killWeakestUnit(this);
        armyInArea[area].killWeakestUnit(this);
        renewArea(area, player);
        return area;
    }

    int genericOneOtherDisband(int player) {
        int area = LittleThings.getRandomElementOfSet(areasWithTroopsOfPlayer.get(player).keySet());
        armyInArea[area].killWeakestUnit(this);
        renewArea(area, player);
        return area;
    }

    void woundAllRetreatingUnits() {
        retreatingArmy.woundAllTroops(this);
    }

    /**
     * Метод заполняет массив trackPlaceForPlayer для определённого трека влияния.
     * Должен вызываться каждый раз при изменении trackPlayerOnPlace для соответствующего трека.
     * @param track номер трека влияния
     */
    void fillTrackPlaceForPlayer(int track) {
        for (int place = 0; place < NUM_PLAYER; place++) {
            trackPlaceForPlayer[track][trackPlayerOnPlace[track][place]] = place;
        }
    }

    void receiveComponents(MapPanel mapPanel, LeftTabPanel tabPanel) {
        this.mapPanel = mapPanel;
        this.tabPanel = tabPanel;
        this.chat = tabPanel.getChatTab().getChat();
        this.eventTabPanel = tabPanel.getEventTab();
        this.fightTabPanel = tabPanel.getFightTab();
        this.houseTabPanel = tabPanel.getHouseTab();
    }

    void setAreaWhereBattleBegins(int area) {
        areaOfBattle = area;
    }

    /**
     * Метод пишет в чат определённый текст.
     * @param text текст, который должен быть написан.
     */
    public void say(String text) {
        if (!Settings.getInstance().isPassByRegime()) {
            chat.append(text + "\n");
        } else {
            System.out.println(text);
        }
    }

    /**
     * Метод выводит все области в коллекции под их русскими именами
     * @param areas  множество областей
     * @param text сопутствующий текст
     */
    public void printAreasInCollection(Collection<Integer> areas, String text) {
        System.out.print(text + ": ");
        boolean firstFlag = true;
        for (int area: areas) {
            firstFlag = LittleThings.printDelimiter(firstFlag);
            System.out.print(map.getAreaNameRus(area));
        }
        System.out.println();
    }
}
