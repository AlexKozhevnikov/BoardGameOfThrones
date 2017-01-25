package com.alexeus.logic;

import com.alexeus.GotFrame;
import com.alexeus.ai.GotPlayerInterface;
import com.alexeus.ai.PrimitivePlayer;
import com.alexeus.control.Controller;
import com.alexeus.control.Settings;
import com.alexeus.control.ControlText;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import static com.alexeus.control.ControlText.*;
import static com.alexeus.logic.constants.MainConstants.*;
import static com.alexeus.logic.constants.TextErrors.*;
import static com.alexeus.logic.constants.TextInfo.*;
import static com.alexeus.map.GameOfThronesMap.NUM_AREA;

/**
 * Created by alexeus on 03.01.2017.
 * Основной класс игры. Здесь написана вся логика партии и правила.
 */
public class Game {

    private static Game instance;

    // Все 3 колоды событий в одном объекте
    private ArrayList<LinkedList<Happenable>> decks;

    private LinkedList<WildlingCard> wildlingDeck;

    private HouseCard[][] houseCardOfPlayer;

    private GameOfThronesMap map;

    private Random random;

    private GotPlayerInterface[] playerInterface;

    // номер раунда от начала игры
    private int time;

    // текущая сила одичалых
    private int wildlingsStrength;

    private int preemptiveRaidCheater = -1;

    // события текущего раунда
    private Happenable[] event = new Happenable[NUM_EVENT_DECKS];
    // Запрещённый в данном раунде приказ
    private OrderType forbiddenOrder = null;
    // последняя сыгранная карта одичалых
    private WildlingCard topWildlingCard;

    // Карты двух конфликтующих Домов в последней битве
    private HouseCard houseCardOfSide[] = new HouseCard[2];

    private int[] playerOnSide = new int[2];

    // состояние игры
    private GamePhase gamePhase;

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
    private boolean isSwordUsed = false;

    /*
     * Количества юнитов в резерве у каждого из домов
     */
    private int[][] restingUnitsOfPlayerAndType = new int[NUM_PLAYER][NUM_UNIT_TYPES];

    /*
     * **** Вспомогательные переменные для методов ****
     */

    // Информация о последнем случившемся бое
    private BattleInfo battleInfo;

    // Количество разыгранных приказов с определённым кодом; нужно для валидации приказов каждого игрока
    private int[] nOrdersWithCode = new int[NUM_DIFFERENT_ORDERS];

    // Текущие ставки
    private int[] currentBids = new int[NUM_PLAYER];
    // Трек текущих ставок
    private int currentBidTrack;

    /*
     * Список карт, "область, где есть войска данного игрока"-"количество юнитов". Меняется в течении игры каждый раз,
     * когда войска игрока перемещаются, захватывают или отступают в новую область. Также важно при учёте снабжения.
     */
    private ArrayList<HashMap<Integer, Integer>> areasWithTroopsOfPlayer;

    /*
     * Список множеств, состоящих из областей, где есть набеги данного игрока.
     */
    private ArrayList<HashSet<Integer>> areasWithRaids;

    /*
     * Список множеств, состоящих из областей, где есть походы данного игрока.
     */
    private ArrayList<HashSet<Integer>> areasWithMarches;

    /*
     * Список множеств, состоящих из областей, где есть походы данного игрока.
     */
    private ArrayList<HashSet<Integer>> areasWithCPs;

    /*
     * В этом вспомогательном массиве хранятся количества активных карт каждого игроков
     */
    private int[] numActiveHouseCardsOfPlayer = new int[NUM_PLAYER];

    /*
     * Вспомогательное множество областей
     */
    private HashSet<Integer> accessibleAreaSet = new HashSet<>();

    /*
     * Вспомогательная карта жрунов снабжения. Используется при валидации походов
     */
    private HashMap<Integer, Integer> virtualAreasWithTroops = new HashMap<>();

    /*
     * Количество оставшихся карт события определённого типа
     */
    private HashMap<Happenable, Integer> numRemainingCards = new HashMap<>();

    private MapPanel mapPanel;

    private LeftTabPanel tabPanel;

    private HouseTabPanel houseTabPanel;

    private FightTabPanel fightTabPanel;

    private EventTabPanel eventTabPanel;

    private JTextArea chat;

    /**
     * Конструктор.
     */
    private Game() {
        map = new GameOfThronesMap();
        random = new Random();
        addListener(new GamePhaseChangeListener());
        playerInterface = new PrimitivePlayer[NUM_PLAYER];
        for (int i = 0; i < NUM_PLAYER; i++) {
            playerInterface[i] = new PrimitivePlayer(this, i);
        }
        currentBidTrack = -1;
    }

    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    /**
     * Метод вызывается при старте новой игры. Все переменные принимают начальные значения, текущий прогресс,
     * если таковой имелся, теряется.
     */
    public void prepareNewGame() {
        for (int area = 0; area < NUM_AREA; area++) {
            powerTokenOnArea[area] = -1;
            houseHomeLandInArea[area] = -1;
            armyInArea[area] = new Army(this);
        }
        decks = new ArrayList<>();
        for (int deckNumber = 1; deckNumber <= NUM_EVENT_DECKS; deckNumber++) {
            decks.add(new LinkedList<>());
        }
        wildlingDeck = new LinkedList<>();

        // тестовая распечатка полей карты
        // map.print();

        // Инициализация вспомогательных множеств
        areasWithTroopsOfPlayer = new ArrayList<>();
        areasWithRaids = new ArrayList<>();
        areasWithMarches = new ArrayList<>();
        areasWithCPs = new ArrayList<>();
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
        setInitialPosition();
        adjustSupply();
        for (int player = 0; player < NUM_PLAYER; player++) {
            printSupplyEaters(player);
        }
        adjustVictoryPoints();
        battleInfo = null;
        say(NEW_GAME_BEGINS);
        for (int player = 0; player < NUM_PLAYER; player++) {
            playerInterface[player].nameYourself();
        }
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

        /*// Баловство
        armyInArea[52].addUnit(UnitType.pawn, 3);
        armyInArea[9].addUnit(UnitType.ship, 0);
        armyInArea[9].addUnit(UnitType.ship, 0);
        armyInArea[55].addUnit(UnitType.pawn, 2);
        armyInArea[56].addUnit(UnitType.pawn, 0);*/

        // Нейтральные лорды
        garrisonInArea[31] = 6;
        garrisonInArea[54] = 5;
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
        time = 1;
        wildlingsStrength = 2;
        // Устанавливаем начальные количаства жетонов власти
        for (int player = 0; player < NUM_PLAYER; player++) {
            nPowerTokensHouse[player] = INITIAL_TOKENS;
            maxPowerTokensHouse[player] = MAX_TOKENS;
            numActiveHouseCardsOfPlayer[player] = NUM_HOUSE_CARDS;
        }
        // Устанавливаем начальные позиции на треках влияния
        for (int track = 0; track < NUM_TRACK; track++) {
            System.arraycopy(INITIAL_INFLUENCE_TRACKS_PLAYER_ON_PLACE[track], 0, trackPlayerOnPlace[track], 0, NUM_PLAYER);
            fillTrackPlaceForPlayer(track);
        }
        renewHousesTroopsInAllAreas();
    }

    public void prepareNewShit(int shittingPlayer) {
        garrisonInArea[31] = 6;
        garrisonInArea[54] = 5;
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
        for (int i = 0; i < NUM_AREA; i++) {
            armyInArea[i].deleteAllUnits();
            if (i >= 12 && i < 20) {
                continue;
            }
            int owner = houseHomeLandInArea[i] >= 0 ? houseHomeLandInArea[i] : shittingPlayer;
            armyInArea[i].addUnit(map.getAreaType(i).isNaval() ? UnitType.ship : UnitType.pawn, owner);
            if (!armyInArea[i].isEmpty()) {
                orderInArea[i] = Order.closed;
            }
        }
        for (int i = 12; i < 20; i++) {
            int castleOwner = getAreaOwner(map.getCastleWithPort(i));
            if (castleOwner >= 0) {
                armyInArea[i].addUnit(UnitType.ship, castleOwner);
            }
            orderInArea[i] = Order.closed;
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            for (int card = 0; card < NUM_HOUSE_CARDS; card++) {
                houseCardOfPlayer[player][card].setActive(random.nextBoolean());
            }
        }

        renewHousesTroopsInAllAreas();
    }

    /**
     * Метод обновляет значения areasWithTroopsOfPlayer для всех игроков
     */
    private void renewHousesTroopsInAllAreas() {
        for (int player = 0; player < NUM_PLAYER; player++) {
            System.arraycopy(MAX_NUM_OF_UNITS, 0, restingUnitsOfPlayerAndType[player], 0, NUM_UNIT_TYPES);
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

    private void printSupplyEaters(int player) {
        System.out.print(ARMIES + HOUSE_GENITIVE[player] + ": ");
        boolean firstFlag = true;
        for (Map.Entry<Integer, Integer> entry: areasWithTroopsOfPlayer.get(player).entrySet()) {
            if (entry.getValue() > 1) {
                firstFlag = LittleThings.printDelimiter(firstFlag);
                System.out.print(map.getAreaNameRus(entry.getKey()) + " (" + entry.getValue() + ")");
            }
        }
        System.out.println();
    }

    /**
     * Метод считает и изменяет количество замков у каждого из игроков
     */
    private void adjustVictoryPoints() {
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
                setNewGamePhase(GamePhase.end);
            }
        }
    }

    private void getPlans() {
        setClosedOrders();
        mapPanel.repaint();
        if (Settings.getInstance().isTrueAutoSwitchTabs()) {
            tabPanel.setSelectedIndex(TabEnum.chat.getCode());
        }
        HashMap<Integer, Order> orders;
        Order curOrder;
        for (int player = 0; player < NUM_PLAYER; player++) {
            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                orders = playerInterface[player].giveOrders();
                if (validateOrders(orders, player)) {
                    // Успех: сохраняем приказы и заполняем вспомогательные множества
                    for (Integer area: orders.keySet()) {
                        curOrder = orders.get(area);
                        if (curOrder != null) {
                            orderInArea[area] = curOrder;
                            //noinspection ConstantConditions, ибо нефиг
                            switch(curOrder.orderType()) {
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
                    }
                    break;
                }
            }
        }
        Controller.getInstance().interruption(PLAYERS + GIVE_ORDERS);
        // Ставки на ворону уже достаточно покрасовались, пора и честь знать
        currentBidTrack = -1;
        setNewGamePhase(GamePhase.ravenPhase);
    }

    /**
     * Метод проверяет приказы, полученные от игрока, на соответсвие правилам
     * @param orders карта область-приказ, полученная от игрока
     * @param player номер игрока
     * @return true, если приказы валидны
     */
    private boolean validateOrders(HashMap<Integer, Order> orders, int player) {
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

    /**
     * Метод разруливает ситуацию после фазы замыслов, когда владелец посыльного ворона может
     * поменять один свой приказ или посмотреть верхнюю карту одичалых
     */
    private void getRavenDecision() {
        deleteVoidOrders();
        if (Settings.getInstance().isTrueAutoSwitchTabs() && tabPanel.getSelectedIndex() != TabEnum.chat.getCode()) {
            tabPanel.setSelectedIndex(TabEnum.chat.getCode());
        }
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaint();
        }
        int ravenHolder = trackPlayerOnPlace[TrackType.raven.getCode()][0];
        for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
            String ravenUse = playerInterface[ravenHolder].useRaven();
            // Ворононосец выбрал просмотр карты одичалых
            if (ravenUse.equals(RAVEN_SEES_WILDLINGS_CODE)) {
                say(HOUSE[ravenHolder] + SEES_WILDLINGS_CARD);
                Controller.getInstance().interruption(HOUSE[ravenHolder] + SEES_WILDLINGS);
                if (!playerInterface[ravenHolder].leaveWildlingCardOnTop(wildlingDeck.getFirst())) {
                    WildlingCard card = wildlingDeck.pollFirst();
                    wildlingDeck.addLast(card);
                    say(AND_BURIES);
                } else {
                    say(AND_LEAVES);
                }
                break;
            }
            // Ворононосец выбрал замену приказа
            if (ravenUse.charAt(0) >= '0' && ravenUse.charAt(0) <= '9') {
                say(HOUSE[ravenHolder] + CHANGES_ORDER);
                Controller.getInstance().interruption(HOUSE[ravenHolder] + CHANGES_ORDER);
                String[] s = ravenUse.split(" ");
                if (s.length != 2) {
                    say(RAVEN_CHANGE_ORDER_FORMAT_ERROR);
                    continue;
                }
                try {
                    int area = Integer.valueOf(s[0]);
                    int newOrderCode = Integer.valueOf(s[1]);
                    // TODO реализовать ёбаный кусок кода
                    System.out.println("ОШИБКА! НЕРЕАЛИЗОВАННЫЙ КУСОК КОДА!");
                    break;
                } catch (NumberFormatException ex) {
                    say(RAVEN_CHANGE_ORDER_PARSE_ERROR);
                }
            }
        }
        // Если есть событие "Море штормов", то после ворона сразу переходим к походом, иначе сначала разыгрываем набеги
        setNewGamePhase(event[2] == Deck3Cards.seaOfStorms ? GamePhase.marchPhase : GamePhase.raidPhase);
    }

    /**
     * Метод разыгрывает набеги
     */
    private void playRaids() {
        int numNoRaidsPlayers = 0;
        int place = 0;
        int player;
        int attempt;
        while(numNoRaidsPlayers < NUM_PLAYER){
            player = trackPlayerOnPlace[TrackType.ironThrone.getCode()][place];
            if (areasWithRaids.get(player).isEmpty()) {
                numNoRaidsPlayers++;
            } else {
                numNoRaidsPlayers = 0;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    RaidOrderPlayed raid = playerInterface[player].playRaid();
                    // Проверяем вариант розыгрыша набега, полученный от игрока, на валидность
                    if (validateRaid(raid, player)) {
                        Controller.getInstance().interruption(HOUSE[player] + PLAYS_RAID);
                        int from = raid.getAreaFrom();
                        int to = raid.getAreaTo();
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
                        break;
                    }
                }
                // Если игрок не уложился в количество попыток, то удаляем все его приказы набегов
                if (attempt >= MAX_TRIES_TO_GO) {
                    say(HOUSE[player] + FAILED_TO_PLAY_RAID);
                    for (int area: areasWithRaids.get(player)) {
                        orderInArea[area] = null;
                        if (!Settings.getInstance().isPassByRegime()) {
                            mapPanel.repaintArea(area);
                        }
                    }
                    areasWithRaids.get(player).clear();
                }
            }
            place++;
            if (place == NUM_PLAYER) place = 0;
        }
        setNewGamePhase(GamePhase.marchPhase);
    }

    /**
     * Метод для валидации варианта разыгрыша набега, полученного от игрока
     * @param raid   вариант розыгрыша набега
     * @param player номер игрока
     * @return true, если набег можно разыграть таким образом
     */
    private boolean validateRaid(RaidOrderPlayed raid, int player) {
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
     * Метод разыгрывает походы
     */
    private void playMarches() {
        int numNoMarchesPlayers = 0;
        int place = 0;
        int player;
        int attempt;
        printAreaWithTroopsOfPlayers();
        while(numNoMarchesPlayers < NUM_PLAYER){
            player = trackPlayerOnPlace[TrackType.ironThrone.getCode()][place];
            if (areasWithMarches.get(player).isEmpty()) {
                numNoMarchesPlayers++;
            } else {
                numNoMarchesPlayers = 0;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    MarchOrderPlayed march = playerInterface[player].playMarch();
                    // Проверяем вариант розыгрыша похода, полученный от игрока, на валидность
                    if (validateMarch(march, player)) {
                        Controller.getInstance().interruption(HOUSE[player] + PLAYS_MARCH);
                        int from = march.getAreaFrom();
                        HashMap<Integer, ArrayList<UnitType>> destinationsOfMarch = march.getDestinationsOfMarch();
                        if (destinationsOfMarch.size() == 0) {
                            // Случай "холостого" снятия приказа похода
                            say(HOUSE[player] + DELETES_MARCH + map.getAreaNameRusGenitive(from));
                            orderInArea[from] = null;
                            areasWithMarches.get(player).remove(from);
                            if (Settings.getInstance().isTrueAutoSwitchTabs() &&
                                    tabPanel.getSelectedIndex() == TabEnum.fight.getCode()) {
                                tabPanel.setSelectedIndex(TabEnum.chat.getCode());
                            }
                        } else {
                            // Случай результативного похода: перемещаем юниты, если нужно, начинаем бой,
                            // и обновляем попутные переменные
                            int areaWhereBattleBegins = -1;
                            for (Map.Entry<Integer, ArrayList<UnitType>> entry: destinationsOfMarch.entrySet()) {
                                int curDestination = entry.getKey();
                                ArrayList<UnitType> curUnits = entry.getValue();
                                Army curArmy = new Army(this, curUnits, player);
                                int destinationArmyOwner = getTroopsOrGarrisonOwner(curDestination);
                                if (destinationArmyOwner >= 0 && destinationArmyOwner != player ||
                                        houseHomeLandInArea[curDestination] >= 0 &&
                                        houseHomeLandInArea[curDestination] != player && garrisonInArea[curDestination] > 0) {
                                    // Если в области завязывается бой, то запоминаем её и проходим последней
                                    areaWhereBattleBegins = curDestination;
                                    attackingArmy = curArmy;
                                } else {
                                    // Иначе перемещаем войска и пробиваем гарнизоны
                                    say(curArmy.toString() +
                                            (curArmy.getSize() == 1 ? MOVES_TO : MOVE_TO) +
                                            map.getAreaNameRusAccusative(curDestination) + ".");
                                    int destinationOwner = getAreaOwner(curDestination);

                                    if (destinationOwner >= 0 && destinationOwner != player) {
                                        if (map.getNumCastle(curDestination) > 0) {
                                            victoryPoints[destinationOwner]--;
                                            tryToCaptureShips(curDestination, player);
                                        }
                                        if (powerTokenOnArea[curDestination] >= 0) {
                                            powerTokenOnArea[curDestination] = -1;
                                            maxPowerTokensHouse[destinationOwner]++;
                                            if (!Settings.getInstance().isPassByRegime()) {
                                                houseTabPanel.repaintHouse(player);
                                            }
                                        }
                                    }
                                    // Если в области имелся нейтральный гарнизон, то пробиваем его
                                    if (isNeutralGarrisonInArea(curDestination)) {
                                        say(GARRISON_IS_DEFEATED + HOUSE_GENITIVE[player] + " - " +
                                                calculatePowerOfPlayerVersusGarrison(player, curDestination, curUnits,
                                                orderInArea[from].getModifier()) + GARRISON_STRENGTH_IS +
                                                garrisonInArea[curDestination]);
                                        garrisonInArea[curDestination] = 0;
                                    }

                                    armyInArea[curDestination].addSubArmy(curArmy);
                                    renewHouseTroopsInArea(curDestination);
                                    if (map.getNumCastle(curDestination) > 0 && destinationOwner != player) {
                                        adjustVictoryPoints();
                                    }
                                    if (!Settings.getInstance().isPassByRegime()) {
                                        mapPanel.repaintArea(curDestination);
                                    }
                                }
                                armyInArea[from].deleteSubArmy(curArmy);
                            }
                            areasWithTroopsOfPlayer.get(player).remove(from);
                            renewHouseTroopsInArea(from);
                            if (armyInArea[from].isEmpty() && houseHomeLandInArea[from] != player &&
                                    powerTokenOnArea[from] < 0) {
                                if (march.getIsLeaveToken()) {
                                    say(HOUSE[player] + LEAVES_POWER_TOKEN +
                                            map.getAreaNameRusLocative(from) + ".");
                                    powerTokenOnArea[from] = player;
                                    nPowerTokensHouse[player]--;
                                    maxPowerTokensHouse[player]--;
                                } else {
                                    if (map.getNumCastle(from) > 0) {
                                        victoryPoints[player]--;
                                        // Если мы покинули чью-то столицу, не оставив жетона, то столица возвращается
                                        // владельцу, и тот может что-нибудь сделать с кораблями в порту.
                                        if (houseHomeLandInArea[from] >= 0) {
                                            adjustVictoryPoints();
                                            tryToCaptureShips(from, houseHomeLandInArea[from]);
                                        } else {
                                            // Если мы покинули замок с портом, то все корабли в порту немедленно уничтожаются
                                            tryToDestroyNeutralShips(from);
                                        }
                                    }
                                }
                            }
                            if (!Settings.getInstance().isPassByRegime()) {
                                mapPanel.repaintArea(from);
                                mapPanel.repaintVictory();
                            }
                            if (areaWhereBattleBegins >= 0) {
                                say(attackingArmy.toString() +
                                        (attackingArmy.getSize() == 1 ? MOVES_TO : MOVE_TO) +
                                        map.getAreaNameRusAccusative(areaWhereBattleBegins) +
                                        (attackingArmy.getSize() == 1 ? AND_FIGHTS : AND_FIGHT));
                                if (!Settings.getInstance().isPassByRegime()) {
                                    mapPanel.repaintArea(areaWhereBattleBegins);
                                }
                                playFight(areaWhereBattleBegins, from, orderInArea[from].getModifier());
                            } else {
                                if (Settings.getInstance().isTrueAutoSwitchTabs() &&
                                        tabPanel.getSelectedIndex() == TabEnum.fight.getCode()) {
                                    tabPanel.setSelectedIndex(TabEnum.chat.getCode());
                                }
                            }
                            orderInArea[from] = null;
                            areasWithMarches.get(player).remove(from);
                        }
                        if (!Settings.getInstance().isPassByRegime()) {
                            mapPanel.repaintArea(from);
                        }
                        break;
                    }
                }
                // Если игрок не уложился в количество попыток, то удаляем один его приказ похода
                if (attempt >= MAX_TRIES_TO_GO) {
                    say(HOUSE[player] + FAILED_TO_PLAY_MARCH);
                    int area = areasWithMarches.get(player).iterator().next();
                    orderInArea[area] = null;
                    areasWithMarches.get(player).remove(area);
                    if (!Settings.getInstance().isPassByRegime()) {
                        mapPanel.repaintArea(area);
                    }
                } else {
                    printAreaWithTroopsOfPlayers();
                }
            }
            place++;
            if (place == NUM_PLAYER) place = 0;
        }
        setNewGamePhase(event[2] == Deck3Cards.feastForCrows ?
                (time < LAST_TURN ? GamePhase.westerosPhase : GamePhase.end) : GamePhase.consolidatePowerPhase);
    }

    /**
     * Метод для валидации варианта розыгрыша похода, полученного от игрока
     * @param march  вариант розыгрыша похода
     * @param player номер игрока
     * @return true, если так разыграть поход можно
     */
    private boolean validateMarch(MarchOrderPlayed march, int player) {
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
            HashSet<Integer> accessibleAreas = null;
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

    private void playConsolidatePower() {
        int areaWithMuster[] = new int[NUM_PLAYER];
        for (int player = 0; player < NUM_PLAYER; player++) {
            int earning = 0;
            areaWithMuster[player] = -1;
            for (int area: areasWithCPs.get(player)) {
                if (orderInArea[area] == Order.consolidatePowerS && map.getNumCastle(area) > 0) {
                    areaWithMuster[player] = area;
                } else {
                    earning += map.getNumCrown(area) + 1;
                    orderInArea[area] = null;
                    if (!Settings.getInstance().isPassByRegime()) {
                        mapPanel.repaintArea(area);
                    }
                }
            }
            areasWithCPs.get(player).clear();
            if (earning > 0) {
                earnTokens(player, earning);
            }
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (areaWithMuster[player] >= 0) {
                say(HOUSE[player] + CAN_MUSTER + map.getAreaNameRusLocative(areaWithMuster[player]) + ".");
                int attempt;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    MusterPlayed musterVariant = playerInterface[player].playConsolidatePowerS(areaWithMuster[player]);
                    if (validateMuster(musterVariant, player)) {
                        Controller.getInstance().interruption(HOUSE[player] + PLAYS_CP);
                        if (Settings.getInstance().isTrueAutoSwitchTabs() &&
                                tabPanel.getSelectedIndex() == TabEnum.fight.getCode()) {
                            tabPanel.setSelectedIndex(TabEnum.chat.getCode());
                        }
                        int nMusteredObjects = musterVariant.getNumberMusterUnits();
                        if (nMusteredObjects > 0) {
                            doMuster(musterVariant, player);
                        } else {
                            int earning = map.getNumCrown(areaWithMuster[player]) + 1;
                            earnTokens(player, earning);
                        }
                        break;
                    }
                }
                if (attempt >= MAX_TRIES_TO_GO) {
                    earnTokens(player, map.getNumCrown(areaWithMuster[player]) + 1);
                }
                orderInArea[areaWithMuster[player]] = null;
                if (!Settings.getInstance().isPassByRegime()) {
                    mapPanel.repaintArea(areaWithMuster[player]);
                    houseTabPanel.repaintHouse(player);
                }
            }
        }
        setNewGamePhase(time < LAST_TURN ? GamePhase.westerosPhase : GamePhase.end);
    }

    /**
     * Метод исполняет один приказ сбора войск
     * @param musterVariant вариант сбора войск
     * @param player        номер игрока
     */
    private void doMuster(MusterPlayed musterVariant, int player) {
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
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    private void tryToCaptureShips(int castleArea, int player) {
        int portArea = map.getPortOfCastle(castleArea);
        if (portArea >= 0 && !armyInArea[portArea].isEmpty()) {
            int numShips = armyInArea[portArea].getSize();
            int numCapturedShips = Math.min(restingUnitsOfPlayerAndType[player][UnitType.ship.getCode()], numShips);
            areasWithTroopsOfPlayer.get(armyInArea[portArea].getOwner()).remove(portArea);
            if (numShips > numCapturedShips) {
                armyInArea[portArea].killSomeUnits(numShips - numCapturedShips, KillingReason.navyLimit);
            }
            if (numCapturedShips > 0) {
                int exOwner = armyInArea[portArea].getOwner();
                armyInArea[portArea].setOwner(player);
                renewArea(portArea, exOwner);
                restingUnitsOfPlayerAndType[exOwner][UnitType.ship.getCode()] += numShips;
                restingUnitsOfPlayerAndType[player][UnitType.ship.getCode()] -= numCapturedShips;
                if (!Settings.getInstance().isPassByRegime()) {
                    houseTabPanel.repaintHouse(exOwner);
                    houseTabPanel.repaintHouse(player);
                }
                say(HOUSE[player] + CAN_CAPTURE_OR_DESTROY_SHIPS);
                int restShips = playerInterface[player].getNumCapturedShips(portArea);
                Controller.getInstance().interruption(HOUSE[player] + CAPTURES_SHIPS);
                if (restShips > numCapturedShips) {
                    restShips = numCapturedShips;
                    System.out.println(FLEET_VIOLATION_ERROR);
                }
                if (restShips < 0) {
                    restShips = 0;
                    System.out.println(DESTROY_EVERYTHING_ERROR);
                }
                say(HOUSE[player] + (restShips == 0 ? DESTROYS_ALL_SHIPS : CAPTURES + restShips +
                        (restShips == 1 ? OF_SHIP : OF_SHIPA)));
                if (restShips < numCapturedShips) {
                    armyInArea[portArea].killSomeUnits(numCapturedShips - restShips, KillingReason.shipwreck);
                    if (orderInArea[portArea] != null) {
                        if (orderInArea[portArea].orderType() == OrderType.march) {
                            areasWithMarches.get(exOwner).remove(portArea);
                        } else if (orderInArea[portArea].orderType() == OrderType.consolidatePower) {
                            areasWithCPs.get(exOwner).remove(portArea);
                        }
                        orderInArea[portArea] = null;
                    }
                    renewArea(portArea, player);
                    restingUnitsOfPlayerAndType[player][UnitType.ship.getCode()] += numCapturedShips - restShips;
                    if (!Settings.getInstance().isPassByRegime()) {
                        houseTabPanel.repaintHouse(player);
                    }
                }
            }
        }
    }

    private void tryToDestroyNeutralShips(int castleArea) {
        int portArea = map.getPortOfCastle(castleArea);
        if (portArea >= 0 && !armyInArea[portArea].isEmpty()) {
            int exOwner = armyInArea[portArea].getOwner();
            restingUnitsOfPlayerAndType[exOwner][UnitType.ship.getCode()] += armyInArea[portArea].getSize();
            if (!Settings.getInstance().isPassByRegime()) {
                houseTabPanel.repaintHouse(exOwner);
            }
            armyInArea[portArea].killAllUnits(KillingReason.shipwreck);
            if (orderInArea[portArea] != null) {
                if (orderInArea[portArea].orderType() == OrderType.march) {
                    areasWithMarches.get(exOwner).remove(portArea);
                } else if (orderInArea[portArea].orderType() == OrderType.consolidatePower) {
                    areasWithCPs.get(exOwner).remove(portArea);
                }
                orderInArea[portArea] = null;
            }
            renewArea(portArea, exOwner);
        }
    }

    /**
     * Метод добавляет игроку некоторое число заработанных жетонов
     * @param player  номер игрока
     * @param earning заработок
     */
    private void earnTokens(int player, int earning) {
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
    private boolean validateMuster(MusterPlayed muster, int player) {
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
            if (map.getAreaType(muster.getArea(i)).isNaval() &&
                    map.getAdjacencyType(from, muster.getArea(i)) == AdjacencyType.noAdjacency ||
                    !map.getAreaType(muster.getArea(i)).isNaval() && from != muster.getArea(i) ||
                    map.getAreaType(muster.getArea(i)) == AreaType.sea &&
                    getTroopsOwner(muster.getArea(i)) >= 0 &&  getTroopsOwner(muster.getArea(i)) != player) {
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
    public HashSet<Integer> getAccessibleAreas(int areaFrom, int player) {
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
            HashSet<Integer> accessibleForPlayerAreas;
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

    /**
     * Метод разыгрывает одно сражение за определённую область. На данный момент атакующая армия должна быть сохранена
     * в attackingArmy, а нападающий игрок может быть определён посредоством attackingArmy.getOwner();
     * @param areaOfBattle  область, в которой происходит сражение
     * @param areaOfMarch   область, из которой пришла атака
     * @param marchModifier модификатор похода, которым было начато сражение
     */
    private void playFight(int areaOfBattle, int areaOfMarch, int marchModifier) {
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
            battleInfo = new BattleInfo();
        }
        battleInfo.setNewBattle(playerOnSide[0], playerOnSide[1], areaOfBattle, marchModifier,
                map.getNumCastle(areaOfBattle) > 0);
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
        // Учитываем подмоги из соседних областей
        SideOfBattle[] supportOfPlayer = new SideOfBattle[NUM_PLAYER];
        HashSet<Integer> supporters = new HashSet<>();
        ArrayList<Integer> areasOfSupport = new ArrayList<>();
        // Спрашиваем игроков, кого они поддержат. Не имеет смысла, если есть событие "Паутина лжи"
        if (event[2] != Deck3Cards.webOfLies) {
            for (int adjacentArea: map.getAdjacentAreas(areaOfBattle)) {
                if (orderInArea[adjacentArea] != null && orderInArea[adjacentArea].orderType() == OrderType.support &&
                        map.getAdjacencyType(adjacentArea, areaOfBattle) != AdjacencyType.landToSea &&
                        map.getAdjacencyType(adjacentArea, areaOfBattle) != AdjacencyType.portOfCastle &&
                        map.getAdjacencyType(adjacentArea, areaOfBattle) != AdjacencyType.castleWithPort) {
                    supporters.add(getTroopsOwner(adjacentArea));
                    areasOfSupport.add(adjacentArea);
                }
            }
            SideOfBattle sideOfBattle;
            boolean firstFlag = true;
            StringBuilder sb = new StringBuilder();
            for (int player: supporters) {
                if (!firstFlag) sb.append(", ");
                firstFlag = false;
                sb.append(HOUSE[player]);
            }
            if (!supporters.isEmpty()) {
                sb.append(CAN_SUPPORT_SOMEBODY);
                say(sb.toString());
            }

            if (supporters.size() > 0) {
                Controller.getInstance().interruption(supporters.size() > 1 ? PLAYERS: HOUSE[supporters.iterator().next()] +
                        (supporters.size() > 1 ? ESTABLISH_SUPPORT: ESTABLISHES_SUPPORT));
            }
            for (int player : supporters) {
                for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    sideOfBattle = playerInterface[player].sideToSupport(battleInfo);
                    switch (sideOfBattle) {
                        case attacker:
                            say(HOUSE[player] + SUPPORTS + HOUSE_GENITIVE[playerOnSide[0]] + ".");
                            break;
                        case defender:
                            say(HOUSE[player] + SUPPORTS + HOUSE_GENITIVE[playerOnSide[1]] + ".");
                            break;
                        default:
                            say(HOUSE[player] + SUPPORTS_NOBODY);
                    }
                    if (sideOfBattle == SideOfBattle.attacker && player == playerOnSide[1] ||
                            sideOfBattle == SideOfBattle.defender && player == playerOnSide[0]) {
                        say(CANT_SUPPORT_AGAINST_YOURSELF_ERROR);
                    } else {
                        supportOfPlayer[player] = sideOfBattle;
                        break;
                    }
                }
                if (supportOfPlayer[player] == null) {
                    // Если игрок так и не смог определиться с подмогой, он никому её не оказывает
                    say(HOUSE[player] + SUPPORTS_NOBODY);
                    supportOfPlayer[player] = SideOfBattle.neutral;
                }
            }

            // Добавляем боевую силу поддерживающих войск
            for (int areaOfSupport: areasOfSupport) {
                battleInfo.addSupportingArmyToSide(supportOfPlayer[getTroopsOwner(areaOfSupport)],
                        armyInArea[areaOfSupport], orderInArea[areaOfSupport].getModifier());
            }
            if (!Settings.getInstance().isPassByRegime()) {
                fightTabPanel.repaint();
            }
        }

        // Выбираем карты дома
        Controller.getInstance().interruption(PLAYERS + CHOOSE_CARD);
        houseCardOfSide[0] = getHouseCard(battleInfo, playerOnSide[0]);
        houseCardOfSide[1] = getHouseCard(battleInfo, playerOnSide[1]);

        countBattleVariables();
        int firstSideOnThrone = trackPlaceForPlayer[TrackType.ironThrone.getCode()][playerOnSide[0]] <
                trackPlaceForPlayer[TrackType.ironThrone.getCode()][playerOnSide[1]] ? 0 : 1;
        HouseCard temporaryInactiveCard = null;
        boolean propertyUsed;
        // Карты отмены: Тирион
        for (int curSide = 0; curSide < 2; curSide++) {
            int heroSide = (curSide + firstSideOnThrone) % 2;
            if (houseCardOfSide[heroSide] == HouseCard.tyrionLannister) {
                say(HOUSE[playerOnSide[heroSide]] + CAN_USE_SPECIAL_PROPERTY_OF_CARD +
                        houseCardOfSide[heroSide].getName());
                switch (houseCardOfSide[heroSide]) {
                    case tyrionLannister:
                        boolean useTyrion = playerInterface[playerOnSide[heroSide]].useTyrion(battleInfo,
                                houseCardOfSide[1 - heroSide]);
                        Controller.getInstance().interruption(TYRION);
                        if (useTyrion) {
                            say(TYRION_CANCELS);
                            temporaryInactiveCard = houseCardOfSide[1 - heroSide];
                            battleInfo.setHouseCardForPlayer(playerOnSide[1 - heroSide], null);
                            countBattleVariables();
                            if (Settings.getInstance().isTrueAutoSwitchTabs()) {
                                tabPanel.setSelectedIndex(TabEnum.fight.getCode());
                            }
                            Controller.getInstance().interruption(HOUSE[playerOnSide[1 - heroSide]] + CHOOSES_CARD);
                            houseCardOfSide[1 - heroSide] = getHouseCard(battleInfo, playerOnSide[1 - heroSide]);
                            countBattleVariables();
                        } else {
                            say(houseCardOfSide[heroSide].getName() + NO_EFFECT);
                        }
                }
            }
        }
        // Немедленные карты: Мейс, Бабка, Доран и Эйерон
        for (int curSide = 0; curSide < 2; curSide++) {
            int heroSide = (curSide + firstSideOnThrone) % 2;
            if (houseCardOfSide[heroSide].getCardInitiative() == CardInitiative.immediately) {
                switch (houseCardOfSide[heroSide]) {
                    case maceTyrell:
                        // Чёрная Рыба защищает от свойства Мейса Тирелла
                        if (houseCardOfSide[1 - heroSide] == HouseCard.theBlackfish) {
                            say(BLACKFISH_SAVES + HOUSE_GENITIVE[playerOnSide[1 - heroSide]] + FROM_LOSSES);
                            break;
                        }
                        Army armyToSearchForFootmen = heroSide == 0 ? armyInArea[areaOfBattle] : attackingArmy;
                        Unit victimOfMace = armyToSearchForFootmen.getUnitOfType(UnitType.pawn);
                        if (victimOfMace != null) {
                            say(MACE_EATS_MAN + map.getAreaNameRusLocative(areaOfBattle));
                            battleInfo.deleteUnit(heroSide == 0 ? SideOfBattle.defender : SideOfBattle.attacker, victimOfMace);
                            armyToSearchForFootmen.killUnit(victimOfMace, KillingReason.mace);
                            renewArea(areaOfBattle, playerOnSide[1 - heroSide]);
                            countBattleVariables();
                            if (!Settings.getInstance().isPassByRegime()) {
                                houseTabPanel.repaintHouse(playerOnSide[1 - heroSide]);
                            }
                        } else {
                            say(houseCardOfSide[heroSide].getName() + NO_EFFECT);
                        }
                        break;
                    case queenOfThorns:
                        propertyUsed = false;
                        accessibleAreaSet.clear();
                        for (int adjacentArea: map.getAdjacentAreas(areaOfBattle)) {
                            if (orderInArea[adjacentArea] != null && adjacentArea != areaOfMarch &&
                                    getTroopsOwner(adjacentArea) == playerOnSide[1 - heroSide]) {
                                accessibleAreaSet.add(adjacentArea);
                            }
                        }
                        if (!accessibleAreaSet.isEmpty()) {
                            say(HOUSE[playerOnSide[heroSide]] + CAN_USE_SPECIAL_PROPERTY_OF_CARD +
                                    houseCardOfSide[heroSide].getName());
                            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                                int area = playerInterface[playerOnSide[heroSide]].chooseAreaQueenOfThorns(accessibleAreaSet);
                                Controller.getInstance().interruption(QUEEN_OF_THORNS);
                                // Если область меньше нуля, значит, игрок отказался использовать свойство карты
                                if (area < 0) {
                                    break;
                                }
                                if (area >= NUM_AREA) {
                                    say(INVALID_AREA_ERROR);
                                }
                                if (Settings.getInstance().isTrueAutoSwitchTabs()) {
                                    tabPanel.setSelectedIndex(TabEnum.fight.getCode());
                                }
                                say(QUEEN_OF_THORNS_REMOVES_ORDER + map.getAreaNameRusGenitive(area));
                                if (accessibleAreaSet.contains(area)) {
                                    propertyUsed = true;
                                    //noinspection ConstantConditions
                                    switch (orderInArea[area].orderType()) {
                                        case march:
                                            areasWithMarches.get(playerOnSide[1 - heroSide]).remove(area);
                                            break;
                                        case consolidatePower:
                                            areasWithCPs.get(playerOnSide[1 - heroSide]).remove(area);
                                            break;
                                        case support:
                                            // Если противник сам себя поддержал, а мы снимаем поддержку,
                                            // то боевая сила поддержки аннулируется
                                            if (supportOfPlayer[playerOnSide[1 - heroSide]] != SideOfBattle.neutral) {
                                                battleInfo.deleteArmy(heroSide == 0 ? SideOfBattle.defender :
                                                        SideOfBattle.attacker, armyInArea[area]);
                                                battleInfo.deleteSupportOfPlayer(playerOnSide[1 - heroSide],
                                                        orderInArea[area].getModifier());
                                                countBattleVariables();
                                            }
                                    }
                                    orderInArea[area] = null;
                                    if (!Settings.getInstance().isPassByRegime()) {
                                        mapPanel.repaintArea(area);
                                    }
                                    break;
                                } else {
                                    say(INVALID_AREA_ERROR);
                                }
                            }
                        }
                        if (!propertyUsed) {
                            say(houseCardOfSide[heroSide].getName() + NO_EFFECT);
                        }
                        break;
                    case doranMartell:
                        say(HOUSE[playerOnSide[heroSide]] + CAN_USE_SPECIAL_PROPERTY_OF_CARD +
                                houseCardOfSide[heroSide].getName());
                        int trackToPissOff = playerInterface[playerOnSide[heroSide]].chooseInfluenceTrackDoran(battleInfo);
                        Controller.getInstance().interruption(DORAN);
                        if (trackToPissOff >= 0 && trackToPissOff < NUM_TRACK) {
                            say(DORAN_ABUSES + HOUSE_GENITIVE[playerOnSide[1 - heroSide]] +
                                    TrackType.getTrack(trackToPissOff).onTheTrack());
                            pissOffOnTrack(playerOnSide[1 - heroSide], trackToPissOff);
                            countBattleVariables();
                        } else {
                            say(houseCardOfSide[heroSide].getName() + NO_EFFECT);
                        }
                        break;
                    case aeronDamphair:
                        propertyUsed = false;
                        if (nPowerTokensHouse[playerOnSide[heroSide]] >= 2 &&
                                numActiveHouseCardsOfPlayer[playerOnSide[heroSide]] >= 2) {
                            say(HOUSE[playerOnSide[heroSide]] + CAN_USE_SPECIAL_PROPERTY_OF_CARD +
                                    houseCardOfSide[heroSide].getName());
                            boolean useDamphair = playerInterface[playerOnSide[heroSide]].useAeron(battleInfo);
                            Controller.getInstance().interruption(AERON);
                            if (useDamphair) {
                                say(AERON_RUNS_AWAY + HOUSE[playerOnSide[heroSide]] + MUST_CHOOSE_OTHER_CARD);
                                propertyUsed = true;
                                battleInfo.setHouseCardForPlayer(playerOnSide[heroSide], null);
                                countBattleVariables();
                                if (Settings.getInstance().isTrueAutoSwitchTabs()) {
                                    tabPanel.setSelectedIndex(TabEnum.fight.getCode());
                                }
                                Controller.getInstance().interruption(HOUSE[playerOnSide[heroSide]] + CHOOSES_CARD);
                                houseCardOfSide[heroSide] = getHouseCard(battleInfo, playerOnSide[heroSide]);
                                countBattleVariables();
                            }
                        }
                        if (!propertyUsed) {
                            say(houseCardOfSide[heroSide].getName() + NO_EFFECT);
                        }
                        break;
                }
            }
        }

        // Валирийский меч
        int swordsMan = trackPlayerOnPlace[TrackType.valyrianSword.getCode()][0];
        if (!isSwordUsed && (playerOnSide[0] == swordsMan || playerOnSide[1] == swordsMan)) {
            boolean useSword = playerInterface[swordsMan].useSword(battleInfo);
            if (Settings.getInstance().isTrueAutoSwitchTabs()) {
                tabPanel.setSelectedIndex(TabEnum.fight.getCode());
            }
            Controller.getInstance().interruption(HOUSE[swordsMan] + CAN_USE_SWORD);
            if (useSword) {
                battleInfo.useSwordOnSide(playerOnSide[0] == swordsMan ? SideOfBattle.attacker : SideOfBattle.defender);
                isSwordUsed = true;
                say(HOUSE[swordsMan] + USES_SWORD);
            }
        }
        // Делаем неактивными карты, которые были сыграны, и возвращаем к жизни карту, отменённую Тирионом
        if (temporaryInactiveCard != null) {
            temporaryInactiveCard.setActive(true);
            numActiveHouseCardsOfPlayer[temporaryInactiveCard.house()]++;
            houseTabPanel.repaintHouse(temporaryInactiveCard.house());
        }
        for (int side = 0; side < 2; side++) {
            if (houseCardOfSide[side] != HouseCard.none) {
                // Если это была последняя активная карта игрока, то обновляем ему колоду
                if (numActiveHouseCardsOfPlayer[playerOnSide[side]] == 0) {
                    renewHandExceptCard(playerOnSide[side], houseCardOfSide[side]);
                }
            }
        }
        // Определяем победителя
        int winnerSide = battleInfo.resolveFight().getCode();
        int winner = playerOnSide[winnerSide];
        int loser = playerOnSide[1 - winnerSide];
        if (!Settings.getInstance().isPassByRegime()) {
            fightTabPanel.repaint();
        }
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
            // Победил нападающий
            armyInArea[areaOfBattle].woundAndKillTroops(numKilledUnits, KillingReason.sword);
            areasWithTroopsOfPlayer.get(loser).remove(areaOfBattle);
            // Удаление жетона власти с области, если он там был
            if (powerTokenOnArea[areaOfBattle] >= 0) {
                maxPowerTokensHouse[powerTokenOnArea[areaOfBattle]]++;
                powerTokenOnArea[areaOfBattle] = -1;
            }
            // Удаление приказа из области, если он там был
            if (orderInArea[areaOfBattle] != null) {
                //noinspection ConstantConditions
                switch (orderInArea[areaOfBattle].orderType()) {
                    case march:
                        areasWithMarches.get(loser).remove(areaOfBattle);
                        break;
                    case consolidatePower:
                        areasWithCPs.get(loser).remove(areaOfBattle);
                        break;
                }
                orderInArea[areaOfBattle] = null;
            }
            // Уничтожение гарнизона в области, если он там был
            if (garrisonInArea[areaOfBattle] > 0) {
                say(GARRISON + map.getAreaNameRusLocative(areaOfBattle) + IS_DEFEATED_M);
                garrisonInArea[areaOfBattle] = 0;
            }
            // Лорас Тирелл контрится Арианной
            if (houseCardOfSide[0] == HouseCard.serLorasTyrell && houseCardOfSide[1] != HouseCard.arianneMartell) {
                say(LORAS_RULES);
                Order marchOrder = marchModifier == -1 ? Order.marchB : (marchModifier == 0 ? Order.march : Order.marchS);
                orderInArea[areaOfBattle] = marchOrder;
                areasWithMarches.get(winner).add(areaOfBattle);
            }

            retreatingArmy = armyInArea[areaOfBattle];
            // Арианна
            if (houseCardOfSide[1] == HouseCard.arianneMartell) {
                say(ARIANNA_RULES_AND_PUTS_BACK + map.getAreaNameRusAccusative(areaOfMarch) + ".");
                retreatingArmy = new Army(this);
                retreatingArmy.addSubArmy(armyInArea[areaOfBattle]);
                armyInArea[areaOfMarch].addSubArmy(attackingArmy);
                armyInArea[areaOfBattle].deleteAllUnits();
                renewHouseTroopsInArea(areaOfMarch);
                if (!Settings.getInstance().isPassByRegime()) {
                    mapPanel.repaintArea(areaOfMarch);
                }
            } else {
                armyInArea[areaOfBattle] = attackingArmy;
                renewHouseTroopsInArea(areaOfBattle);
            }
            attackingArmy = null;

            // Захват кораблей в порту
            if (map.getNumCastle(areaOfBattle) > 0) {
                if (getAreaOwner(areaOfBattle) < 0) {
                    tryToDestroyNeutralShips(areaOfBattle);
                } else if (getAreaOwner(areaOfBattle) == winner) {
                    tryToCaptureShips(areaOfBattle, winner);
                }
            }
            // Отступление
            if (retreatingArmy.getSize() > 0) {
                int numRetreatingUnits = retreatingArmy.getSize();
                HashSet<Integer> areasToRetreat = getAccessibleAreas(areaOfBattle, loser);
                HashSet<Integer> trueAreasToRetreat = new HashSet<>();
                int minLosses = numRetreatingUnits;
                int curLosses;
                // Подготавливаем информацию по снабжению
                virtualAreasWithTroops.clear();
                virtualAreasWithTroops.putAll(areasWithTroopsOfPlayer.get(loser));

                for (int area: areasToRetreat) {
                    // Нельзя отступить в область, откуда пришла атака
                    if (area == areaOfMarch) continue;
                    // Нельзя отступить в область, где есть нейтральный гарнизон
                    if (isNeutralGarrisonInArea(area)) continue;
                    int areaOwner = getAreaOwner(area);
                    if (areaOwner < 0 || areaOwner == playerOnSide[1 - winnerSide]) {
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
                        if (curLosses < minLosses) {
                            minLosses = curLosses;
                            trueAreasToRetreat.clear();
                            trueAreasToRetreat.add(area);
                        } else if (curLosses == minLosses) {
                            trueAreasToRetreat.add(area);
                        }
                    }
                }

                System.out.println(MINIMAL_LOSSES + minLosses + ".");
                if (minLosses == numRetreatingUnits) {
                    retreatingArmy.killAllUnits(KillingReason.noAreaToRetreat);
                } else {
                    if (minLosses > 0) {
                        retreatingArmy.killSomeUnits(minLosses, KillingReason.supplyLimit);
                    }
                    printAreasInCollection(trueAreasToRetreat, "Области для отступления");
                    switch (trueAreasToRetreat.size()) {
                        case 0:
                            say(NO_AREAS_TO_RETREAT);
                        case 1:
                            // Если имеется единтсвенная область для отступления, то отступаем туда автоматически
                            int onlyArea = trueAreasToRetreat.iterator().next();
                            armyInArea[onlyArea].addSubArmy(retreatingArmy);
                            say(HOUSE[loser] + RETREATS_IN + map.getAreaNameRusAccusative(onlyArea));
                            renewArea(onlyArea, loser);
                            break;
                        default:
                            // Если есть выбор, то спрашиваем игрока, куда ему отступить
                            int player = houseCardOfSide[winnerSide] == HouseCard.robbStark ?
                                    playerOnSide[winnerSide] : loser;
                            boolean successFlag = false;
                            say(HOUSE[loser] + MUST_RETREAT);
                            if (!Settings.getInstance().isPassByRegime()) {
                                mapPanel.repaintArea(areaOfBattle);
                            }
                            Controller.getInstance().interruption(HOUSE[loser] + RETREATS);
                            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                                int area = playerInterface[player].chooseAreaToRetreat(retreatingArmy, trueAreasToRetreat);
                                say(HOUSE[loser] + RETREATS_IN +
                                        map.getAreaNameRusAccusative(area));
                                if (trueAreasToRetreat.contains(area)) {
                                    armyInArea[area].addSubArmy(retreatingArmy);
                                    renewArea(area, loser);
                                    successFlag = true;
                                    break;
                                } else {
                                    say(CANT_RETREAT_THERE_ERROR);
                                }
                            }
                            // Если игрок так и не смог отступить, то отступаем в первую доступную область
                            if (!successFlag) {
                                int area = trueAreasToRetreat.iterator().next();
                                armyInArea[area].addSubArmy(retreatingArmy);
                                renewArea(area, loser);
                                say(HOUSE[loser] + RETREATS_IN +
                                        map.getAreaNameRusAccusative(area));
                            }
                            break;
                    }
                }
            }
            retreatingArmy = null;
            if (!map.getAreaType(areaOfBattle).isNaval()) {
                adjustVictoryPoints();
                if (!Settings.getInstance().isPassByRegime()) {
                    mapPanel.repaintVictory();
                }
            }
        } else {
            // Победил защищающийся
            int marchAreaOwner = getAreaOwner(areaOfMarch);
            // Если область, из которой был поход, стала вражеской (как это бывает с престольными землями, на которых
            // не оставили жетонов власти), то все оставшиеся юниты уничтожаются.
            if (marchAreaOwner < 0 || marchAreaOwner == playerOnSide[1 - winnerSide]) {
                attackingArmy.woundAndKillTroops(numKilledUnits, KillingReason.sword);
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
                        attackingArmy.killSomeUnits(minLosses, KillingReason.supplyLimit);
                    }
                    if (attackingArmy.getSize() > 0) {
                        say(HOUSE[loser] + RETREATS_IN +
                                map.getAreaNameRusAccusative(areaOfMarch));
                        armyInArea[areaOfMarch].addSubArmy(attackingArmy);
                        renewArea(areaOfMarch, loser);
                    }
                }
            } else {
                attackingArmy.killAllUnits(KillingReason.noAreaToRetreat);
            }
            attackingArmy = null;
        }
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintArea(areaOfBattle);
        }
        // Разыгрываем эффект карт "после боя"
        for (int curSide = 0; curSide < 2; curSide++) {
            int heroSide = (curSide + firstSideOnThrone) % 2;
            if (houseCardOfSide[heroSide].getCardInitiative() == CardInitiative.afterFight) {
                int player = playerOnSide[heroSide];
                switch (houseCardOfSide[heroSide]) {
                    case renlyBaratheon:
                        propertyUsed = false;
                        if (heroSide != winnerSide || restingUnitsOfPlayerAndType[player][UnitType.knight.getCode()] == 0) {
                            break;
                        }
                        accessibleAreaSet.clear();
                        if (armyInArea[areaOfBattle].hasUnitOfType(UnitType.pawn)) {
                            accessibleAreaSet.add(areaOfBattle);
                        }
                        for (int adjacentArea: map.getAdjacentAreas(areaOfBattle)) {
                            if (getTroopsOwner(adjacentArea) == player && orderInArea[adjacentArea] != null &&
                                    orderInArea[adjacentArea].orderType() == OrderType.support &&
                                    supportOfPlayer[player] != null && supportOfPlayer[player].getCode() == heroSide) {
                                accessibleAreaSet.add(adjacentArea);
                            }
                        }
                        if (accessibleAreaSet.size() > 0) {
                            say(RENLY_CAN_MAKE_KNIGHT);
                            Controller.getInstance().interruption(RENLY);
                            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                                int area = playerInterface[player].areaToUseRenly(accessibleAreaSet);
                                // Если область меньше нуля, значит, игрок отказался использовать свойство карты
                                if (area < 0) {
                                    break;
                                }
                                if (area >= NUM_AREA) {
                                    say(INVALID_AREA_ERROR);
                                }
                                say(RENLY_MAKES_KNIGHT + map.getAreaNameRusLocative(area) + ".");
                                if (!Settings.getInstance().isPassByRegime()) {
                                    mapPanel.repaintArea(area);
                                }
                                if (accessibleAreaSet.contains(area)) {
                                    boolean success = armyInArea[area].changeType(UnitType.pawn, UnitType.knight);
                                    if (success) {
                                        restingUnitsOfPlayerAndType[player][UnitType.pawn.getCode()]--;
                                        restingUnitsOfPlayerAndType[player][UnitType.knight.getCode()]++;
                                        propertyUsed = true;
                                        if (!Settings.getInstance().isPassByRegime()) {
                                            houseTabPanel.repaintHouse(player);
                                        }
                                    } else {
                                        say(CANT_CHANGE_UNIT_TYPE_ERROR);
                                    }
                                    break;
                                } else {
                                    say(INVALID_AREA_ERROR);
                                }
                            }
                        }
                        if (!propertyUsed) {
                            say(houseCardOfSide[heroSide].getName() + NO_EFFECT);
                        }
                        break;
                    case cerseiLannister:
                        if (heroSide != winnerSide) break;
                        accessibleAreaSet.clear();
                        propertyUsed = false;
                        for (int area: areasWithTroopsOfPlayer.get(playerOnSide[1 - heroSide]).keySet()) {
                            if (orderInArea[area] != null) {
                                accessibleAreaSet.add(area);
                            }
                        }
                        if (accessibleAreaSet.size() > 0) {
                            say(CERSEI_CAN_REMOVE_ANY_ORDER);
                            Controller.getInstance().interruption(CERCEI);
                            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                                int area = playerInterface[player].chooseAreaCerseiLannister(accessibleAreaSet);
                                // Если область меньше нуля, значит, игрок отказался использовать свойство карты
                                if (area < 0) {
                                    break;
                                }
                                if (area >= NUM_AREA) {
                                    say(INVALID_AREA_ERROR);
                                }
                                say(CERSEI_REMOVES_ORDER + map.getAreaNameRusGenitive(area));
                                if (getTroopsOwner(area) == playerOnSide[1 - heroSide] && orderInArea[area] != null) {
                                    propertyUsed = true;
                                    //noinspection ConstantConditions
                                    switch (orderInArea[area].orderType()) {
                                        case march:
                                            areasWithMarches.get(playerOnSide[1 - heroSide]).remove(area);
                                            break;
                                        case consolidatePower:
                                            areasWithCPs.get(playerOnSide[1 - heroSide]).remove(area);
                                            break;
                                    }
                                    orderInArea[area] = null;
                                    if (!Settings.getInstance().isPassByRegime()) {
                                        mapPanel.repaintArea(area);
                                    }
                                    break;
                                } else {
                                    say(INVALID_AREA_ERROR);
                                }
                            }
                        }
                        if (!propertyUsed) {
                            say(houseCardOfSide[heroSide].getName() + NO_EFFECT);
                        }
                        break;
                    case tywinLannister:
                        if (heroSide != winnerSide) break;
                        earnTokens(playerOnSide[heroSide], 2);
                        break;
                    case rooseBolton:
                        if (heroSide == winnerSide) break;
                        returnAllCards(player);
                        break;
                }
            }
        }

        // Разыгрываем Пестряка
        for (int curSide = 0; curSide < 2; curSide++) {
            if (houseCardOfSide[curSide] == HouseCard.patchface) {
                propertyUsed = false;
                say(PATCHPACE_CAN_DELETE_ANY_CARD);
                Controller.getInstance().interruption(PATCHFACE);
                for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    int card = playerInterface[playerOnSide[curSide]].chooseCardPatchface(playerOnSide[1 - curSide]);
                    // Если карта меньше нуля, значит, игрок отказался использовать свойство карты
                    if (card < 0) {
                        break;
                    }
                    if (houseCardOfPlayer[playerOnSide[1 - curSide]][card].isActive()) {
                        propertyUsed = true;
                        houseCardOfPlayer[playerOnSide[1 - curSide]][card].setActive(false);
                        numActiveHouseCardsOfPlayer[playerOnSide[1 - curSide]]--;
                        if (numActiveHouseCardsOfPlayer[playerOnSide[1 - curSide]] == 0) {
                            renewHandExceptCard(playerOnSide[1 - curSide], houseCardOfPlayer[playerOnSide[1 - curSide]][card]);
                        }
                        say(PATCHPACE_DELETES_CARD + houseCardOfPlayer[playerOnSide[1 - curSide]][card].getName() + ".");
                        if (!Settings.getInstance().isPassByRegime()) {
                            houseTabPanel.repaintHouse(playerOnSide[1 - curSide]);
                        }
                        break;
                    } else {
                        say(CARD_IS_NOT_ACTIVE_ERROR);
                    }
                }
                if (!propertyUsed) {
                    say(houseCardOfSide[curSide].getName() + NO_EFFECT);
                }
            }
        }
        System.out.print("Количество оставшихся карт домов: ");
        for (int player = 0; player < NUM_PLAYER; player++) {
            System.out.print((player > 0 ? ", ": "") + HOUSE[player] + ": " + numActiveHouseCardsOfPlayer[player]);
        }
        System.out.println();
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

    private void countBattleVariables () {
        battleInfo.countBattleVariables();
        if (!Settings.getInstance().isPassByRegime()) {
            fightTabPanel.repaint();
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
     * Метод обращается к игроку и получает от него карту дома, которую после возвращает.
     * @param battleInfo информация о сражении
     * @param player     номер игрока
     * @return карта дома, которую выбрал игрок
     */
    private HouseCard getHouseCard(BattleInfo battleInfo, int player) {
        HouseCard chosenCard = null;
        if (numActiveHouseCardsOfPlayer[player] == 1) {
            chosenCard = getFirstActiveHouseCard(player);
        } else if (numActiveHouseCardsOfPlayer[player] == 0) {
            chosenCard = HouseCard.none;
        } else {
            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                int givenCard = playerInterface[player].playHouseCard(battleInfo);
                if (givenCard >= 0 && givenCard < NUM_HOUSE_CARDS && houseCardOfPlayer[player][givenCard].isActive()) {
                    chosenCard = houseCardOfPlayer[player][givenCard];
                }
            }
        }
        // Если игрок не уложился в число попыток, то выбираем первую активную его карту
        if (chosenCard == null) {
            chosenCard = getFirstActiveHouseCard(player);
        }
        assert chosenCard != null;
        chosenCard.setActive(false);
        if (chosenCard != HouseCard.none) {
            numActiveHouseCardsOfPlayer[player]--;
        }
        say(HOUSE[player] + PLAYS_HOUSE_CARD + chosenCard.getName() + "\".");
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
        battleInfo.setHouseCardForPlayer(player, chosenCard);
        return chosenCard;
    }

    /**
     * Вернуть на руку все карты данного игрока (Русе Болтон или победа на Молоководной)
     * @param player номер игрока
     */
    private void returnAllCards (int player) {
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
     * Метод возвращает первую активную карту игрока
     * @param player номер игрока
     * @return первая активная карта дома
     */
    private HouseCard getFirstActiveHouseCard(int player) {
        for (int i = 0; i < NUM_HOUSE_CARDS; i++) {
            if (houseCardOfPlayer[player][i].isActive()) {
                return houseCardOfPlayer[player][i];
            }
        }
        return null;
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
    private void pissOffOnTrack(int player, int track) {
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
    private void descendOnTrack(int player, int track, int positionDecay) {
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
    private void enlightenOnTrack(int player, int track) {
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
                curIndexForPlayer[house]++;
            }
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
    private void changeSupply(int player, int modifier) {
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
    private void nullifyOrdersAndVariables() {
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
    private void chooseNewEvents() {
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
        Controller.getInstance().interruption(EVENTS + event[0].getName() + "; " + event[1].getName() + "; " + event[2].getName());
    }

    /**
     * Метод разыгрывает новые события Вестероса
     */
    private void playNewEvents() {
        if (wildlingsStrength == MAX_WILDLING_STRENGTH) {
            wildlingAttack();
        }
        // Событие №1
        switch ((Deck1Cards) event[0]) {
            case muster:
                playMuster();
                break;
            case supply:
                adjustSupply();
                break;
            case throneOfSwords:
                int king = getInfluenceTrackPlayerOnPlace(TrackType.ironThrone.getCode(), 0);
                say(HOUSE[king] + MUST_CHOOSE_EVENT);
                int attempt;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    int kingChoice = playerInterface[king].eventToChoose(1);
                    Controller.getInstance().interruption(HOUSE[king] + CHOOSES_EVENT);
                    if (kingChoice < 0 || kingChoice >= NUM_EVENT_CHOICES) {
                        say(WRONG_EVENT_CHOICE_ERROR);
                    } else {
                        switch (kingChoice) {
                            case 0:
                                adjustSupply();
                                break;
                            case 1:
                                playMuster();
                                break;
                            case 2:
                                say(RELAX_NOTHING_HAPPENS);
                                break;
                        }
                        break;
                    }
                }
                if (attempt == MAX_TRIES_TO_GO) {
                    say(RELAX_NOTHING_HAPPENS);
                }
                break;
        }
        // Событие №2
        switch ((Deck2Cards) event[1]) {
            case clashOfKings:
                playClash();
                break;
            case gameOfThrones:
                playGameOfThrones();
                break;
            case darkWingsDarkWords:
                int ravenHolder = getInfluenceTrackPlayerOnPlace(TrackType.raven.getCode(), 0);
                say(HOUSE[ravenHolder] + MUST_CHOOSE_EVENT);
                int attempt;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    int ravenChoice = playerInterface[ravenHolder].eventToChoose(2);
                    Controller.getInstance().interruption(HOUSE[ravenHolder] + CHOOSES_EVENT);
                    if (ravenChoice < 0 || ravenChoice >= NUM_EVENT_CHOICES) {
                        say(WRONG_EVENT_CHOICE_ERROR);
                    } else {
                        switch (ravenChoice) {
                            case 0:
                                playClash();
                                break;
                            case 1:
                                playGameOfThrones();
                                break;
                            case 2:
                                say(RELAX_NOTHING_HAPPENS);
                                break;
                        }
                        break;
                    }
                }
                if (attempt == MAX_TRIES_TO_GO) {
                    say(RELAX_NOTHING_HAPPENS);
                }
                break;
        }
        // Событие №3
        switch ((Deck3Cards) event[2]) {
            case wildlingsAttack:
                wildlingAttack();
                break;
            case devotedToSword:
                int swordsman = getInfluenceTrackPlayerOnPlace(TrackType.valyrianSword.getCode(), 0);
                say(HOUSE[swordsman] + MUST_CHOOSE_EVENT);
                int attempt;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    int swordChoice = playerInterface[swordsman].eventToChoose(3);
                    Controller.getInstance().interruption(HOUSE[swordsman] + CHOOSES_EVENT);
                    if (swordChoice < 0 || swordChoice >= NUM_EVENT_CHOICES) {
                        say(WRONG_EVENT_CHOICE_ERROR);
                    } else {
                        switch (swordChoice) {
                            case 0:
                                forbiddenOrder = OrderType.defence;
                                say(STORM_OF_SWORDS);
                                break;
                            case 1:
                                forbiddenOrder = OrderType.march;
                                say(RAIN_OF_AUTUMN);
                                break;
                            case 2:
                                say(RELAX_NOTHING_HAPPENS);
                                break;
                        }
                        break;
                    }
                }
                if (attempt == MAX_TRIES_TO_GO) {
                    say(RELAX_NOTHING_HAPPENS);
                }
                break;
            case seaOfStorms:
                forbiddenOrder = OrderType.raid;
                say(SEA_OF_STORMS);
                break;
            case rainOfAutumn:
                forbiddenOrder = OrderType.march;
                say(RAIN_OF_AUTUMN);
                break;
            case feastForCrows:
                forbiddenOrder = OrderType.consolidatePower;
                say(FEAST_FOR_CROWS);
                break;
            case webOfLies:
                forbiddenOrder = OrderType.support;
                say(WEB_OF_LIES);
                break;
            case stormOfSwords:
                forbiddenOrder = OrderType.defence;
                say(STORM_OF_SWORDS);
                break;
        }
        setNewGamePhase(GamePhase.planningPhase);
    }

    /**
     * Метод считает и изменяет снабжение каждого из игроков
     */
    private void adjustSupply() {
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
        verifyNewSupplyLimits();
    }

    /**
     * Метод разыгрывает событие "Сбор войск"
     */
    private void playMuster() {
        say(MUSTER_HAPPENS);
        ArrayList<HashSet<Integer>> areasOfPlayerWithMuster = new ArrayList<>();
        for (int player = 0; player < NUM_PLAYER; player++) {
            areasOfPlayerWithMuster.add(new HashSet<>());
        }
        for (int area = 0; area < NUM_AREA; area++) {
            if (map.getNumCastle(area) > 0 && getAreaOwner(area) >= 0) {
                areasOfPlayerWithMuster.get(getAreaOwner(area)).add(area);
                orderInArea[area] = Order.consolidatePowerS;
            }
        }
        mapPanel.repaint();
        for (int place = 0; place < NUM_PLAYER; place++) {
            int player = trackPlayerOnPlace[0][place];
            say(HOUSE[player] + CAN_MUSTER_TROOPS);
            while (!areasOfPlayerWithMuster.get(player).isEmpty()) {
                int attempt;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    MusterPlayed musterVariant = playerInterface[player].muster(areasOfPlayerWithMuster.get(player));
                    if (validateMuster(musterVariant, player)) {
                        Controller.getInstance().interruption(HOUSE[player] + MUSTERS);
                        int nMusteredObjects = musterVariant.getNumberMusterUnits();
                        if (nMusteredObjects > 0) {
                            doMuster(musterVariant, player);
                        }
                        int castleArea = musterVariant.getCastleArea();
                        areasOfPlayerWithMuster.get(player).remove(castleArea);
                        orderInArea[castleArea] = null;
                        if (!Settings.getInstance().isPassByRegime()) {
                            mapPanel.repaintArea(castleArea);
                            houseTabPanel.repaintHouse(player);
                        }
                        break;
                    }
                }
                if (attempt >= MAX_TRIES_TO_GO) {
                    say(HOUSE[player] + FAILED_TO_PLAY_MUSTER);
                    for (int castleArea: areasOfPlayerWithMuster.get(player)) {
                        orderInArea[castleArea] = null;
                        if (!Settings.getInstance().isPassByRegime()) {
                            mapPanel.repaintArea(castleArea);
                        }
                    }
                    areasOfPlayerWithMuster.get(player).clear();
                }
            }
        }
    }

    /**
     * Метод разыгрывает сбор войск в одном из замков игрока
     * @param player номер игрока
     */
    private void playOneMuster(int player) {
        HashSet<Integer> areasWithMuster = new HashSet<>();
        for (int area: areasWithTroopsOfPlayer.get(player).keySet()) {
            if (map.getNumCastle(area) > 0) {
                areasWithMuster.add(area);
                orderInArea[area] = Order.consolidatePowerS;
                mapPanel.repaintArea(area);
            }
        }
        if (areasWithMuster.size() == 0) return;
        say(HOUSE[player] + CAN_MUSTER_IN_ONE_CASTLE);
        int attempt;
        for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
            MusterPlayed musterVariant = playerInterface[player].muster(areasWithMuster);
            if (validateMuster(musterVariant, player)) {
                Controller.getInstance().interruption(HOUSE[player] + CAN_ONE_MUSTER);
                int nMusteredObjects = musterVariant.getNumberMusterUnits();
                if (nMusteredObjects > 0) {
                    doMuster(musterVariant, player);
                }
                if (!Settings.getInstance().isPassByRegime()) {
                    houseTabPanel.repaintHouse(player);
                }
                break;
            }
        }
        if (attempt >= MAX_TRIES_TO_GO) {
            say(HOUSE[player] + FAILED_TO_PLAY_MUSTER);
        }

        for (int area: areasWithMuster) {
            orderInArea[area] = null;
            mapPanel.repaintArea(area);
        }
    }

    /**
     * Метод разыгрывает событие "Битва королей"
     */
    private void playClash() {
        say(CLASH_HAPPENS);
        int attempt;
        int[] newPlayerOnPlace;
        boolean[] isPlayerCounted = new boolean[NUM_PLAYER];
        for (int track = 0; track < NUM_TRACK; track++) {
            say(CLASH_FOR + TrackType.getTrack(track) + PLAYERS_MAKE_BIDS);
            int king = trackPlayerOnPlace[TrackType.ironThrone.getCode()][0];
            int[] tempBids = new int[NUM_PLAYER];
            // получаем от игроков ставки
            for (int player = 0; player < NUM_PLAYER; player++) {
                isPlayerCounted[player] = false;
                if (nPowerTokensHouse[player] == 0) {
                    tempBids[player] = 0;
                    continue;
                }
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    tempBids[player] = playerInterface[player].bid(track);
                    if (tempBids[player] >= 0 && tempBids[player] <= nPowerTokensHouse[player]) {
                        break;
                    }
                    say(WRONG_BID_ERROR);
                }
                if (attempt >= MAX_TRIES_TO_GO) {
                    say(HOUSE[player] + FAILED_TO_BID);
                    tempBids[player] = 0;
                }
            }
            Controller.getInstance().interruption(PLAYERS + BID[track]);
            currentBids = tempBids;
            currentBidTrack = track;
            for (int player = 0; player < NUM_PLAYER; player++) {
                nPowerTokensHouse[player] -= currentBids[player];
            }
            trackPreArrange(track);
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintTracks();
                houseTabPanel.repaint();
            }
            // решение королём ничьих
            for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                newPlayerOnPlace = playerInterface[king].kingChoiceInfluenceTrack(track, currentBids);
                Controller.getInstance().interruption(HOUSE[king] + DECIDES_TIES);
                isPlayerCounted[newPlayerOnPlace[0]] = true;
                boolean alrightFlag = true;
                for (int place = 1; place < NUM_PLAYER; place++) {
                    if (isPlayerCounted[newPlayerOnPlace[place]]) {
                        say(PLAYER_COUNTED_TWICE_ERROR);
                        alrightFlag = false;
                        break;
                    }
                    if (currentBids[newPlayerOnPlace[place]] > currentBids[newPlayerOnPlace[place - 1]]) {
                        say(WRONG_TRACK_ORDER_ERROR);
                        alrightFlag = false;
                        break;
                    }
                }
                if (alrightFlag) {
                    System.arraycopy(newPlayerOnPlace, 0, trackPlayerOnPlace[track], 0, NUM_PLAYER);
                    fillTrackPlaceForPlayer(track);
                    break;
                }
            }
            if (attempt >= MAX_TRIES_TO_GO) {
                say(HOUSE[king] + FAILED_TO_KING);
                // Король не справился со своими обязанностями, так что все ничьи разрешаем случайным образом
                isPlayerCounted = new boolean[NUM_PLAYER];
                ArrayList<Integer> pretenders = new ArrayList<>();
                for (int curPlace = 0; curPlace < NUM_PLAYER; ) {
                    int curMaxBid = -1;
                    for (int player = 0; player < NUM_PLAYER; player++) {
                        if (currentBids[player] > curMaxBid && !isPlayerCounted[player]) {
                            pretenders.clear();
                            pretenders.add(player);
                            curMaxBid = currentBids[player];
                        } else if (currentBids[player] == curMaxBid) {
                            pretenders.add(player);
                        }
                    }
                    while(!pretenders.isEmpty()) {
                        int nextOnTrackIndex = random.nextInt(pretenders.size());
                        trackPlayerOnPlace[track][curPlace] = pretenders.get(nextOnTrackIndex);
                        isPlayerCounted[pretenders.get(nextOnTrackIndex)] = true;
                        pretenders.remove(nextOnTrackIndex);
                        curPlace++;
                    }
                }
                fillTrackPlaceForPlayer(track);
            }
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintTracks();
            }
        }
    }

    /**
     * Метод располагает игроков на определённом треке в порядке их ставок, сразу после вскрытия этих ставок
     * @param track номер трека
     */
    private void trackPreArrange(int track) {
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
    private void playGameOfThrones() {
        say(GAME_OF_THRONES_HAPPENS);
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
     * Метод проверяет, укладываются ли игроки в новый лимит по снабжению, и если нет, то требует от них распустить
     * лишние войска. Вызывается после события "Снабжение" и после поражения от одичальнических охотников на снабжение
     */
    private void verifyNewSupplyLimits() {
        for (int place = 0; place < NUM_PLAYER; place++) {
            int player = trackPlayerOnPlace[TrackType.ironThrone.getCode()][place];
            if (!GameUtils.supplyTest(areasWithTroopsOfPlayer.get(player), supply[player])) {
                playDisband(player, DisbandReason.supply);
            }
        }
    }

    /**
     * Метод разыгрывает нападение одичалых
     */
    private void wildlingAttack() {
        say(WILDLINGS_ATTACK_WITH_STRENGTH + wildlingsStrength + "!");
        if (topWildlingCard != null) {
            wildlingDeck.addLast(topWildlingCard);
        }
        topWildlingCard = null;
        mapPanel.repaintWildlingsCard();
        // получаем от игроков ставки
        int[] tempBids = new int[NUM_PLAYER];
        int attempt;
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (player == preemptiveRaidCheater) continue;
            if (nPowerTokensHouse[player] == 0) {
                tempBids[player] = 0;
                continue;
            }
            for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                tempBids[player] = playerInterface[player].wildlingBid(wildlingsStrength);
                if (tempBids[player] >= 0 && tempBids[player] <= nPowerTokensHouse[player]) {
                    break;
                }
                say(WRONG_BID_ERROR);
            }
            if (attempt >= MAX_TRIES_TO_GO) {
                say(HOUSE[player] + FAILED_TO_BID);
                tempBids[player] = 0;
            }
        }
        Controller.getInstance().interruption(PLAYERS + FIGHT_WILDLINGS);
        currentBids = tempBids;
        currentBidTrack = -1;
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
        int nightWatchStrength = 0;
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (player == preemptiveRaidCheater) continue;
            nPowerTokensHouse[player] -= currentBids[player];
            nightWatchStrength += currentBids[player];
        }
        houseTabPanel.repaint();

        boolean isNightWatchWon = nightWatchStrength >= wildlingsStrength;
        if (isNightWatchWon) {
            // Победа ночного дозора
            say(NIGHT_WATCH_VICTORY);
            wildlingsStrength = 0;
        } else {
            // Победа одичалых
            say(NIGHT_WATCH_DEFEAT);
            wildlingsStrength = Math.max(0, wildlingsStrength - 2 * WILDLING_STRENGTH_INCREMENT);
        }
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintWildlings();
        }
        // Тишина за стеной - находить высшую/низшую ставку не имеет смысла
        if (topWildlingCard == WildlingCard.silenceAtTheWall) {
            say(SILENCE_AT_THE_WALL);
            return;
        }
        // Находим высшую или низшую ставку
        int exclusiveBidder = -1;
        int exclusiveBid = isNightWatchWon ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        ArrayList<Integer> curExBidders = new ArrayList<>();
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (player == preemptiveRaidCheater) continue;
            if (isNightWatchWon && currentBids[player] > exclusiveBid ||
                    !isNightWatchWon && currentBids[player] < exclusiveBid) {
                exclusiveBid = currentBids[player];
                curExBidders.clear();
                curExBidders.add(player);
            } else if (currentBids[player] == exclusiveBid) {
                curExBidders.add(player);
            }
        }
        if (curExBidders.size() == 1) {
            exclusiveBidder = curExBidders.get(0);
        } else {
            int king = getInfluenceTrackPlayerOnPlace(TrackType.ironThrone.getCode(), 0);
            Controller.getInstance().interruption(HOUSE[king] + CHOOSES +
                    (isNightWatchWon ? CHOOSES_TOP_BID : CHOOSES_BOTTOM_BID));
            for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                exclusiveBidder = playerInterface[king].kingChoiceWildlings(topWildlingCard, curExBidders, isNightWatchWon);
                if (curExBidders.contains(exclusiveBidder)) {
                    break;
                }
            }
            // Король не шмог, поэтому случайно выбираем высшую/низшую ставку
            if (attempt >= MAX_TRIES_TO_GO) {
                say(HOUSE[king] + FAILED_TO_KING);
                int exclusiveBidIndex = random.nextInt(curExBidders.size());
                exclusiveBidder = curExBidders.get(exclusiveBidIndex);
            }
        }
        say((isNightWatchWon ? TOP_BID_IS : BOTTOM_BID_IS) + HOUSE[exclusiveBidder] + "!");
        // Разыгрываем эффекты карт одичалых
        int player;
        switch (topWildlingCard) {
            // Охотники на снабжение
            case rattleShirtRaiders:
                if (isNightWatchWon) {
                    changeSupply(exclusiveBidder, 1);
                } else {
                    changeSupply(exclusiveBidder, -2);
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = trackPlayerOnPlace[0][place];
                        if (player == exclusiveBidder || player == preemptiveRaidCheater) continue;
                        changeSupply(player, -1);
                    }
                    verifyNewSupplyLimits();
                }
                if (!Settings.getInstance().isPassByRegime()) {
                    mapPanel.repaintSupply();
                }
                break;
            // Разведчик-оборотень
            case shapechangerScout:
                if (isNightWatchWon) {
                    nPowerTokensHouse[exclusiveBidder] += currentBids[exclusiveBidder];
                    say(HOUSE[exclusiveBidder] + SKINCHANGER_SCOUT_WIN);
                } else {
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = trackPlayerOnPlace[0][place];
                        if (player == preemptiveRaidCheater) continue;
                        if (player == exclusiveBidder) {
                            nPowerTokensHouse[player] = 0;
                            say(HOUSE[player] + LOSES_ALL_MONEY);
                            if (!Settings.getInstance().isPassByRegime()) {
                                houseTabPanel.repaintHouse(exclusiveBidder);
                            }
                        } else {
                            earnTokens(player, -2);
                        }
                    }
                }
                break;
            // Сбор на молоководной
            case massingOnTheMilkwater:
                if (isNightWatchWon) {
                    returnAllCards(exclusiveBidder);
                } else {
                    // Низшая ставка
                    if (numActiveHouseCardsOfPlayer[exclusiveBidder] > 1) {
                        int maxCardStrength = -1;
                        int lastLostCard = -1;
                        for (int card = 0; card < NUM_HOUSE_CARDS; card++) {
                            if (houseCardOfPlayer[exclusiveBidder][card].isActive() &&
                                    houseCardOfPlayer[exclusiveBidder][card].getStrength() > maxCardStrength) {
                                maxCardStrength = houseCardOfPlayer[exclusiveBidder][card].getStrength();
                            }
                        }
                        for (int card = 0; card < NUM_HOUSE_CARDS; card++) {
                            if (houseCardOfPlayer[exclusiveBidder][card].isActive() &&
                                    houseCardOfPlayer[exclusiveBidder][card].getStrength() == maxCardStrength) {
                                houseCardOfPlayer[exclusiveBidder][card].setActive(false);
                                numActiveHouseCardsOfPlayer[exclusiveBidder]--;
                                lastLostCard = card;
                                say(HOUSE[exclusiveBidder] + LOSES_CARD + houseCardOfPlayer[exclusiveBidder][card].getName());
                            }
                        }
                        if (numActiveHouseCardsOfPlayer[exclusiveBidder] == 0) {
                            renewHandExceptCard(exclusiveBidder, houseCardOfPlayer[exclusiveBidder][lastLostCard]);
                        }
                        if (!Settings.getInstance().isPassByRegime()) {
                            houseTabPanel.repaintHouse(exclusiveBidder);
                        }
                    }
                    // Прочие ставки
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = trackPlayerOnPlace[0][place];
                        if (player == exclusiveBidder || player == preemptiveRaidCheater ||
                                numActiveHouseCardsOfPlayer[player] == 1) continue;
                        HouseCard chosenCard = null;
                        for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                            int card = playerInterface[player].massingOnTheMilkwaterLoseDecision();
                            if (card >= 0 && card < NUM_HOUSE_CARDS && houseCardOfPlayer[player][card].isActive()) {
                                Controller.getInstance().interruption(HOUSE[player] + LOSES_ONE_CARD);
                                chosenCard = houseCardOfPlayer[player][card];
                                break;
                            }
                        }
                        if (attempt >= MAX_TRIES_TO_GO) {
                            chosenCard = getFirstActiveHouseCard(player);
                        }
                        assert chosenCard != null;
                        chosenCard.setActive(false);
                        numActiveHouseCardsOfPlayer[player]--;
                        if (!Settings.getInstance().isPassByRegime()) {
                            houseTabPanel.repaintHouse(player);
                        }
                        say(HOUSE[player] + LOSES_CARD + chosenCard.getName());
                    }
                    if (!Settings.getInstance().isPassByRegime()) {
                        houseTabPanel.repaint();
                    }
                }
                break;
            // Наездники на мамонтах
            case mammothRiders:
                if (isNightWatchWon) {
                    // Высшая ставка
                    if (numActiveHouseCardsOfPlayer[exclusiveBidder] < NUM_HOUSE_CARDS) {
                        HouseCard chosenCard = null;
                        say(HOUSE[exclusiveBidder] + CAN_RETURN_CARD);
                        for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                            int card = playerInterface[exclusiveBidder].mammothRidersTopDecision();
                            // Если номер карты отрицательный, значит, игрок не хочет возвращать карту Дома
                            if (card < 0) {
                                break;
                            }
                            if (card >= 0 && card < NUM_HOUSE_CARDS && !houseCardOfPlayer[exclusiveBidder][card].isActive()) {
                                chosenCard = houseCardOfPlayer[exclusiveBidder][card];
                                break;
                            }
                        }
                        if (attempt >= MAX_TRIES_TO_GO) {
                            say(HOUSE[exclusiveBidder] + FAILED_TO_WILD);
                        }
                        if (chosenCard != null) {
                            chosenCard.setActive(true);
                            numActiveHouseCardsOfPlayer[exclusiveBidder]++;
                            say(HOUSE[exclusiveBidder] + RETURNS_CARD + chosenCard.getName() + ".");
                            if (!Settings.getInstance().isPassByRegime()) {
                                houseTabPanel.repaintHouse(exclusiveBidder);
                            }
                        }
                    }
                } else {
                    // Низшая ставка
                    playDisband(exclusiveBidder, DisbandReason.mammothTreadDown);
                    // Прочие ставки
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = trackPlayerOnPlace[0][place];
                        if (player == exclusiveBidder || player == preemptiveRaidCheater) continue;
                        playDisband(player, DisbandReason.wildlingCommonDisband);
                    }
                }
                break;
            // Король за стеной
            case aKingBeyondTheWall:
                TrackType track;
                if (isNightWatchWon) {
                    say(HOUSE[exclusiveBidder] + CAN_ENLIGHTEN_ON_TRACK);
                    track = playerInterface[exclusiveBidder].aKingBeyondTheWallTopDecision();
                    Controller.getInstance().interruption(HOUSE[exclusiveBidder] + ENLIGHTENS);
                    if (track != null) {
                        say(HOUSE[exclusiveBidder] + ENLIGHTENS_ON_TRACK + track.onTheTrack() + ".");
                        enlightenOnTrack(exclusiveBidder, track.getCode());
                    }
                } else {
                    // Низшая ставка
                    say(HOUSE[exclusiveBidder] + DESCENDS_ON_ALL_TRACKS);
                    for (int trackId = 0; trackId < NUM_TRACK; trackId++) {
                        pissOffOnTrack(exclusiveBidder, trackId);
                    }
                    // Прочие ставки
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = trackPlayerOnPlace[0][place];
                        if (player == exclusiveBidder || player == preemptiveRaidCheater) continue;
                        say(HOUSE[player] + MUST_DESCEND_ON_TRACK);
                        track = playerInterface[player].aKingBeyondTheWallLoseDecision();
                        Controller.getInstance().interruption(HOUSE[player] + IS_PISSED_UP);
                        if (track == null || track == TrackType.ironThrone) {
                            track = TrackType.getTrack(random.nextInt(2) + 1);
                        }
                        assert (track != null);
                        say(HOUSE[player] + DESCENDS_ON_TRACK + track.onTheTrack() + ".");
                        pissOffOnTrack(player, track.getCode());
                    }
                }
                break;
            // Нашествие орды
            case hordeDescends:
                if (isNightWatchWon) {
                    // Высшая ставка
                    playOneMuster(exclusiveBidder);
                } else {
                    // Низшая ставка
                    ArrayList<Integer> normCastles = new ArrayList<>();
                    for (Map.Entry<Integer, Integer> entry: areasWithTroopsOfPlayer.get(exclusiveBidder).entrySet()) {
                        if (map.getNumCastle(entry.getKey()) > 0 && entry.getValue() > 1) {
                            normCastles.add(entry.getKey());
                        }
                    }
                    if (normCastles.size() == 0) {
                        playDisband(exclusiveBidder, DisbandReason.wildlingCommonDisband);
                    } else if (normCastles.size() == 1 && armyInArea[normCastles.get(0)].getSize() == 2) {
                        armyInArea[normCastles.get(0)].killAllUnits(KillingReason.wildlings);
                    } else {
                        playDisband(exclusiveBidder, DisbandReason.hordeCastle);
                    }
                    // Прочие ставки
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = trackPlayerOnPlace[0][place];
                        if (player == exclusiveBidder || player == preemptiveRaidCheater) continue;
                        playDisband(player, DisbandReason.hordeBite);
                    }
                }
                break;
            // Убийцы ворон
            case crowKillers:
                if (isNightWatchWon) {
                    // Высшая ставка
                    int numAlivePawns = MAX_NUM_OF_UNITS[UnitType.pawn.getCode()] -
                            restingUnitsOfPlayerAndType[exclusiveBidder][UnitType.pawn.getCode()];
                    int numRestingKnight = restingUnitsOfPlayerAndType[exclusiveBidder][UnitType.knight.getCode()];
                    int maxPawnsToUpgrade = Math.min(Math.min(2, numRestingKnight), numAlivePawns);
                    if (maxPawnsToUpgrade > 0) {
                        upgradeSomePawns(exclusiveBidder, maxPawnsToUpgrade);
                    }
                } else {
                    // Низшая ставка
                    int numRestingPawns = restingUnitsOfPlayerAndType[exclusiveBidder][UnitType.pawn.getCode()];
                    int numAliveKnights = MAX_NUM_OF_UNITS[UnitType.knight.getCode()] -
                            restingUnitsOfPlayerAndType[exclusiveBidder][UnitType.knight.getCode()];
                    int numKnightsToKill = Math.max(0, numAliveKnights - numRestingPawns);
                    // Убиваем коней, которых нельзя превратить в пешки
                    if (numKnightsToKill > 0) {
                        killSomeKnights(exclusiveBidder, numKnightsToKill, numAliveKnights);
                        break;
                    }
                    // Спешиваем всех коней
                    dismountAllKnights(exclusiveBidder);
                    if (!Settings.getInstance().isPassByRegime()) {
                        houseTabPanel.repaintHouse(exclusiveBidder);
                    }
                    // Прочие ставки
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = trackPlayerOnPlace[0][place];
                        if (player == exclusiveBidder || player == preemptiveRaidCheater) continue;
                        numRestingPawns = restingUnitsOfPlayerAndType[player][UnitType.pawn.getCode()];
                        numAliveKnights = MAX_NUM_OF_UNITS[UnitType.knight.getCode()] -
                                restingUnitsOfPlayerAndType[player][UnitType.knight.getCode()];
                        int numKnightToDowngrade = Math.min(numAliveKnights, 2);
                        numKnightsToKill = Math.max(0, numKnightToDowngrade - numRestingPawns);
                        // Если пехотинцев не хватает, то убиваем часть коней
                        if (numKnightsToKill > 0) {
                            killSomeKnights(player, numKnightsToKill, numAliveKnights);
                        }
                        numKnightToDowngrade -= numKnightsToKill;
                        numAliveKnights -= numKnightsToKill;
                        if (numKnightToDowngrade > 0) {
                            downgradeSomeKnights(player, numKnightToDowngrade, numAliveKnights);
                        }
                        if (!Settings.getInstance().isPassByRegime()) {
                            houseTabPanel.repaintHouse(player);
                        }
                    }
                }
                break;
            case preemptiveRaid:
                if (isNightWatchWon) {
                    // Высшая ставка
                    wildlingsStrength = PREEMPTIVE_RAID_WILDLINGS_STRENGTH;
                    preemptiveRaidCheater = exclusiveBidder;
                    say(HOUSE[exclusiveBidder] + DOESNT_TAKE_PART);
                    wildlingAttack();
                    preemptiveRaidCheater = -1;
                } else {
                    // Низшая ставка
                    int bestPlace = NUM_PLAYER;
                    for (int trackId = 0; trackId < NUM_TRACK; trackId++) {
                        if (bestPlace > trackPlaceForPlayer[trackId][exclusiveBidder]) {
                            bestPlace = trackPlaceForPlayer[trackId][exclusiveBidder];
                        }
                    }
                    for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                        Object decision = playerInterface[exclusiveBidder].preemptiveRaidBottomDecision();
                        Controller.getInstance().interruption(HOUSE[exclusiveBidder] + PREEMPTIVE_DECIDES);
                        if (decision instanceof TrackType) {
                            if (trackPlaceForPlayer[((TrackType) decision).getCode()][exclusiveBidder] == bestPlace) {
                                say(HOUSE[exclusiveBidder] + PREEMPTIVE_RAID_TRACK + ((TrackType) decision).onTheTrack());
                                descendOnTrack(exclusiveBidder, ((TrackType) decision).getCode(), 2);
                                break;
                            }
                        }
                        if (decision instanceof DisbandPlayed) {
                            if (validateDisband(((DisbandPlayed) decision), exclusiveBidder,
                                    DisbandReason.wildlingCommonDisband)) {
                                say(HOUSE[exclusiveBidder] + PREEMPTIVE_RAID_DISBAND);
                                executeDisband((DisbandPlayed) decision, exclusiveBidder);
                                break;
                            }
                        }
                    }
                    if (attempt >= MAX_TRIES_TO_GO) {
                        say(HOUSE[exclusiveBidder] + FAILED_TO_WILD);
                        for (int trackId = 0; trackId < NUM_TRACK; trackId++) {
                            if (trackPlaceForPlayer[trackId][exclusiveBidder] == bestPlace) {
                                descendOnTrack(exclusiveBidder, trackId, 2);
                                break;
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     * Метод спешивает всех рыцарей игрока. Вызывается при победе "Убийц ворон".
     * @param player номер игрока
     */
    private void dismountAllKnights(int player) {
        ArrayList<Integer> areasWithKnights = getAreasWithUnitsOfType(player, UnitType.knight);
        for (int area: areasWithKnights) {
            while (armyInArea[area].hasUnitOfType(UnitType.knight)) {
                downgradeKnightInArea(area, player);
            }
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintArea(area);
            }
        }
    }

    /**
     * Метод уничтожает всех рыцарей игрока. Вызывается при победе "Убийц ворон".
     * @param player номер игрока
     */
    private void killAllKnights(int player) {
        ArrayList<Integer> areasWithKnights = getAreasWithUnitsOfType(player, UnitType.knight);
        for (int area: areasWithKnights) {
            while (armyInArea[area].hasUnitOfType(UnitType.knight)) {
                armyInArea[area].killUnitOfType(UnitType.knight);
                restingUnitsOfPlayerAndType[player][UnitType.knight.getCode()]++;
            }
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintArea(area);
            }
        }
    }

    /**
     * Метод улучшает пехотинцев из-за высшей ставки против "Убийц ворон"
     * @param player            номер игрока
     * @param maxPawnsToUpgrade максимальное число пешек, которых можно таким образом посвятить в рыцари
     */
    private void upgradeSomePawns(int player, int maxPawnsToUpgrade) {
        int attempt;
        for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
            UnitExecutionPlayed pawnUpgradeVariant = playerInterface[player].
                    crowKillersTopDecision(maxPawnsToUpgrade);
            if (validatePawnsToUpgrade(pawnUpgradeVariant, player, maxPawnsToUpgrade)) {
                Controller.getInstance().interruption(HOUSE[player] + UPGRADES_PAWNS);
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
                break;
            }
        }
        if (attempt >= MAX_TRIES_TO_GO) {
            say(HOUSE[player] + FAILED_TO_WILD);
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
    private boolean validatePawnsToUpgrade(UnitExecutionPlayed pawnUpgradeVariant, int player, int maxPawnsToUpgrade) {
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

    /**
     * Метод убивает часть рыцарей определённого игрока. Может вызваться при победе карты "Убийцы ворон"
     * @param player           номер игрока
     * @param numKnightsToKill количество рыцарей, которых нужно убить
     * @param numAliveKnights  количество живых рыцарей игрока
     */
    private void killSomeKnights(int player, int numKnightsToKill, int numAliveKnights) {
        int attempt;
        ArrayList<Integer> areasWithKnights = getAreasWithUnitsOfType(player, UnitType.knight);
        if (numKnightsToKill == numAliveKnights) {
            killAllKnights(player);
        } else {
            // Спрашиваем у игрока, каких коней убить
            say(HOUSE[player] + MUST_DISBAND_SOME_KNIGHTS);
            for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                UnitExecutionPlayed variantToKillKnights = playerInterface[player].
                        crowKillersKillKnights(numKnightsToKill);
                if (validateKnightsToDo(variantToKillKnights, player, numKnightsToKill)) {
                    Controller.getInstance().interruption(HOUSE[player] + KILLS_KNIGHTS);
                    // Спешиваем выбранных коней
                    HashMap<Integer, Integer> numUnitsInArea = variantToKillKnights.getNumberOfUnitsInArea();
                    for (int area : numUnitsInArea.keySet()) {
                        for (int index = 0; index < numUnitsInArea.get(area); index++) {
                            armyInArea[area].killUnitOfType(UnitType.knight);
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
            }
            if (attempt >= MAX_TRIES_TO_GO) {
                // Если игрок не смог выбрать нескольких коней для умерщвления, то убиваем их всех!
                say(HOUSE[player] + FAILED_TO_WILD);
                for (int area : areasWithKnights) {
                    while (armyInArea[area].hasUnitOfType(UnitType.knight)) {
                        armyInArea[area].killUnitOfType(UnitType.knight);
                        restingUnitsOfPlayerAndType[player][UnitType.knight.getCode()]++;
                    }
                    renewHouseTroopsInArea(area);
                    if (!Settings.getInstance().isPassByRegime()) {
                        mapPanel.repaintArea(area);
                    }
                }
            }
        }
    }

    /**
     * Метод спешивает часть рыцарей определённого игрока до пехотинцев. Может вызваться при победе карты "Убийцы ворон"
     * @param player               номер игрока
     * @param numKnightToDowngrade количество рыцарей, которых нужно спешить
     * @param numAliveKnights      количество живых рыцарей игрока
     */
    private void downgradeSomeKnights(int player, int numKnightToDowngrade, int numAliveKnights) {
        if (numAliveKnights == numKnightToDowngrade) {
            // Если выбора нет, то автоматически спешиваем всех коней игрока
            dismountAllKnights(player);
        } else {
            // Если выбор есть, то спешиваем нужное число коней, обратившись к игроку
            say(HOUSE[player] + MUST_DOWNGRADE_SOME_KNIGHTS);
            ArrayList<Integer> areasWithKnights = getAreasWithUnitsOfType(player, UnitType.knight);
            int attempt;
            for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                UnitExecutionPlayed executionVariant = playerInterface[player].
                        crowKillersLoseDecision(numKnightToDowngrade);
                if (validateKnightsToDo(executionVariant, player, numKnightToDowngrade)) {
                    Controller.getInstance().interruption(HOUSE[player] + DOWNGRADES_KNIGHTS);
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
                    break;
                }
            }
            if (attempt >= MAX_TRIES_TO_GO) {
                // Если игрок не шмог, то спешиваем первых попавшихся коней
                say(HOUSE[player] + FAILED_TO_WILD);
                int numKnightDowngraded = 0;
                outer:
                for (int area : areasWithKnights) {
                    while (armyInArea[area].hasUnitOfType(UnitType.knight)) {
                        downgradeKnightInArea(area, player);
                        numKnightDowngraded++;
                        if (numKnightDowngraded == numKnightToDowngrade) {
                            break outer;
                        }
                    }
                }
            }
        }
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
    private boolean validateKnightsToDo(UnitExecutionPlayed executionVariant, int player, int numberKnightsToDo) {
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

    /**
     * Метод разыгрывает вариант роспуска войск
     * @param player номер игрока
     * @param reason причина роспуска войск
     */
    private void playDisband(int player, DisbandReason reason) {
        // Если у игрока не больше войск, чем требуется распустить, то он распускает их все.
        if (reason != DisbandReason.supply && reason != DisbandReason.hordeCastle) {
            int numDisbandUnits = reason.getNumDisbands();
            int numUnits = getTotalNumberOfUnits(player);
            if (numDisbandUnits >= numUnits) {
                killAllUnits(player, KillingReason.wildlings);
            }
        }
        // Иначе он сам решает, какие войска распустить.
        say(HOUSE[player] + MUST_DISBAND_TROOPS);
        int attempt;
        for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
            DisbandPlayed disbandVariant = playerInterface[player].disband(reason);
            if (validateDisband(disbandVariant, player, reason)) {
                Controller.getInstance().interruption(HOUSE[player] + DISBANDS);
                executeDisband(disbandVariant, player);
                break;
            }
        }
        if (attempt >= MAX_TRIES_TO_GO) {
            say(HOUSE[player] + FAILED_TO_DISBAND);
            if (reason == DisbandReason.supply) {
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
                    armyInArea[area].killWeakestUnit();
                    renewArea(area, player);
                } while (!GameUtils.supplyTest(areasWithTroopsOfPlayer.get(player), supply[player]));
            } else {
                if (reason == DisbandReason.hordeCastle) {
                    Set<Integer> disbandAreas = new HashSet<>();
                    for (int area : areasWithTroopsOfPlayer.get(player).keySet()) {
                        if (map.getNumCastle(area) > 0 || armyInArea[area].getSize() >= 2) {
                            disbandAreas.add(area);
                        }
                    }
                    int area = LittleThings.getRandomElementOfSet(disbandAreas);
                    armyInArea[area].killWeakestUnit();
                    armyInArea[area].killWeakestUnit();
                    renewArea(area, player);
                } else {
                    for (int numDisbands = reason.getNumDisbands(); numDisbands > 0; numDisbands--) {
                        int area = LittleThings.getRandomElementOfSet(areasWithTroopsOfPlayer.get(player).keySet());
                        armyInArea[area].killWeakestUnit();
                        renewArea(area, player);
                    }
                }
            }
        }
    }

    private void executeDisband(DisbandPlayed disbandVariant, int player) {
        HashMap<Integer, ArrayList<UnitType>> disbands = disbandVariant.getDisbandUnits();
        for (int area: disbands.keySet()) {
            for (UnitType unitType: disbands.get(area)) {
                armyInArea[area].killUnitOfType(unitType);
            }
            renewArea(area, player);
        }
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    private boolean validateDisband(DisbandPlayed disbandVariant, int player, DisbandReason reason) {
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
            return supplyTestForDisband(disbandVariant, player);
        }
        return true;
    }

    /**
     * Метод уничтожает всех юнитов игрока
     * @param player номер игрока
     */
    private void killAllUnits(int player, KillingReason reason) {
        for (int area: areasWithTroopsOfPlayer.get(player).keySet()) {
            armyInArea[area].killAllUnits(reason);
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintArea(area);
            }
        }
        areasWithTroopsOfPlayer.get(player).clear();
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    /**
     * Метод возвращает список областей, где у определённого игрока есть юниты определённого типа
     * @param player номер игрока
     * @param type   тип юнитов
     * @return список областей
     */
    private ArrayList<Integer> getAreasWithUnitsOfType(int player, UnitType type) {
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

    /**
     * Метод устанавливает закрытые жетоны приказов во все области, где есть войска игроков
     */
    private void setClosedOrders() {
        for (int area = 0; area < NUM_AREA; area++) {
            if (!armyInArea[area].isEmpty()) {
                orderInArea[area] = Order.closed;
            }
        }
    }

    /**
     * Метод удаляет пустые приказы после вскрытия всех приказов
     */
    private void deleteVoidOrders() {
        for (int area = 0; area < NUM_AREA; area++) {
            if (orderInArea[area] == Order.closed) {
                orderInArea[area] = null;
            }
        }
    }

    /**
     * Метод возвращает полное количество всех юнитов определённого игрока
     * @param player номер игрока
     * @return количество юнитов игрока
     */
    private int getTotalNumberOfUnits(int player) {
        int total = 0;
        for (Map.Entry<Integer, Integer> entry: areasWithTroopsOfPlayer.get(player).entrySet()) {
            total += entry.getValue();
        }
        return total;
    }

    private void renewArea(int area, int exOwner) {
        areasWithTroopsOfPlayer.get(exOwner).remove(area);
        renewHouseTroopsInArea(area);
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintArea(area);
        }
    }

    public GameOfThronesMap getMap() {
        return map;
    }

    /**
     * Метод возвращает множество областей, где есть войска определённого игрока
     * @param player номер игрока
     * @return множество областей
     */
    public Set<Integer> getAreasWithTroopsOfPlayer(int player) {
        return areasWithTroopsOfPlayer.get(player).keySet();
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

    public void printAreaWithTroopsOfPlayers() {
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

    public int getTime() {
        return time;
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

    public Army getAttackingArmy() {
        return attackingArmy;
    }

    public Army getRetreatingArmy() {
        return retreatingArmy;
    }

    public BattleInfo getBattleInfo() {
        return battleInfo;
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

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public MapPanel getMapPanel() {
        return mapPanel;
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

    public int getTopWildlingsCardCode() {
        return topWildlingCard == null ? -1 : topWildlingCard.getCode();
    }

    /**
     * Метод возвращает место определённого игрока на определённом треке влияния
     * @param track  номер трека влияния
     * @param player номер игрока
     * @return место
     */
    public int getInfluenceTrackPlaceForPlayer(int track, int player) {
        return trackPlaceForPlayer[track][player];
    }

    /**
     * Метод возвращает игрока на определённом месте треке влияния
     * @param track номер трека влияния
     * @param place место
     * @return место
     */
    public int getInfluenceTrackPlayerOnPlace(int track, int place) {
        return trackPlayerOnPlace[track][place];
    }

    /**
     * Метод возвращает массив игроков на определённом месте треке влияния
     * @param track номер трека влияния
     * @return место
     */
    public int[] getInfluenceTrackPlayerOnPlace(int track) {
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
    public int getRestingUnitsOfType(int player, UnitType unitType) {
        return restingUnitsOfPlayerAndType[player][unitType.getCode()];
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
        boolean result = GameUtils.supplyTest(virtualAreasWithTroops, supply[player]);
        if (!result) {
            say(DISBAND_SUPPLY_VIOLATION_ERROR);
        }
        return result;

    }

    private PropertyChangeSupport gamePhaseChangeSupport = new PropertyChangeSupport(this);

    private void addListener(PropertyChangeListener listener) {
        gamePhaseChangeSupport.addPropertyChangeListener(listener);
    }

    /*private void removeListener(PropertyChangeListener listener) {
        gamePhaseChangeSupport.removePropertyChangeListener(listener);
    }*/

    public void setNewGamePhase(GamePhase newValue) {
        GamePhase oldValue = gamePhase;
        gamePhase = newValue;
        gamePhaseChangeSupport.firePropertyChange("gamePhase", oldValue, newValue);
    }

    /**
     * Метод заполняет массив trackPlaceForPlayer для определённого трека влияния.
     * Должен вызываться каждый раз при изменении trackPlayerOnPlace для соответствующего трека.
     * @param track номер трека влияния
     */
    private void fillTrackPlaceForPlayer(int track) {
        for (int place = 0; place < NUM_PLAYER; place++) {
            trackPlaceForPlayer[track][trackPlayerOnPlace[track][place]] = place;
        }
    }

    public void receiveComponents(MapPanel mapPanel, LeftTabPanel tabPanel) {
        this.mapPanel = mapPanel;
        this.tabPanel = tabPanel;
        this.chat = tabPanel.getChatTab().getChat();
        this.eventTabPanel = tabPanel.getEventTab();
        this.fightTabPanel = tabPanel.getFightTab();
        this.houseTabPanel = tabPanel.getHouseTab();
    }

    public void forceBid() {
        for (int i = 0; i < NUM_PLAYER; i++) {
            System.out.println(playerInterface[i].bid(1));
        }
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

    // Класс-слушатель, который реагирует на изменение состояния игры и вызывает необходимые методы
    private class GamePhaseChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            say(LINE_DELIMITER);
            //System.out.println("Состояние игры поменялось:" + event.getNewValue());

            if (gamePhase == GamePhase.westerosPhase) {
                time++;
            }
            say(ROUND_NUMBER + time + ". " + (event.getNewValue()).toString());
            // в зависимости от фазы игры выполняем разные действия
            switch ((GamePhase) event.getNewValue()) {
                case planningPhase:
                    getPlans();
                    break;
                case ravenPhase:
                    getRavenDecision();
                    break;
                case raidPhase:
                    playRaids();
                    break;
                case marchPhase:
                    playMarches();
                    break;
                case consolidatePowerPhase:
                    playConsolidatePower();
                    break;
                case westerosPhase:
                    nullifyOrdersAndVariables();
                    chooseNewEvents();
                    playNewEvents();
                    break;
                default:
                    adjustVictoryPoints();
                    JOptionPane.showMessageDialog(GotFrame.getInstance(), "Игра закончена.");
                    break;
            }
        }
    }
}
