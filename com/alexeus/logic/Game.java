package com.alexeus.logic;

import com.alexeus.ai.GotPlayerInterface;
import com.alexeus.ai.PrimitivePlayer;
import com.alexeus.logic.enums.*;
import com.alexeus.logic.struct.*;
import com.alexeus.map.*;
import com.alexeus.util.LittleThings;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import static com.alexeus.logic.constants.MainConstants.*;
import static com.alexeus.logic.constants.TextErrors.*;
import static com.alexeus.logic.constants.TextInfo.*;

/**
 * Created by alexeus on 03.01.2017.
 * Основной класс игры. Здесь написана вся логика партии и правила.
 */
public class Game {

    private LinkedList<Deck1Cards> deck1;

    private LinkedList<Deck2Cards> deck2;

    private LinkedList<Deck3Cards> deck3;

    private LinkedList<WildlingCard> wildlingDeck;

    private HouseCard[][] houseCardOfPlayer;

    private GameOfThronesMap map;

    private Random random;

    private GotPlayerInterface[] playerInterface;

    // номер раунда от начала игры
    private int time;

    // текущая сила одичалых
    private int wildlingsStrength;

    // события текущего раунда
    private Deck1Cards event1;
    private Deck2Cards event2;
    private Deck3Cards event3;
    // Запрещённый в данном раунде приказ
    private OrderType prohibitedOrder = null;
    // последняя сыгранная карта одичалых
    private WildlingCard topWildlingCard;

    // Карты двух конфликтующих Домов в последней битве
    private HouseCard houseCardOfSide[] = new HouseCard[2];

    private int[] playerOnSide = new int[2];
    private int[] swordsOnSide = new int[2];
    private int[] towersOnSide = new int[2];
    private int[] cardStrengthOnSide = new int[2];
    private int[] bonusStrengthOnSide = new int[2];
    private int[] numSupportsOnSide = new int[2];

    // состояние игры
    private GamePhase gamePhase;

    /*
     * Массив приказов, которые сейчас лежат в данных областях карты
     */
    private Order[] orderInArea = new Order[GameOfThronesMap.NUM_AREA];

    /*
     * Массив из армий, находящихся в каждой из областей карты. Пустые при инициализации.
     */
    private Army[] armyInArea = new Army[GameOfThronesMap.NUM_AREA];

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
    private int[] garrisonInArea = new int[GameOfThronesMap.NUM_AREA];

    /*
     * Массив из жетонов власти, лежащих на областях.
     * Если на области нет жетона власти, то соответстующая переменная равна -1.
     */
    private int[] powerTokenOnArea = new int[GameOfThronesMap.NUM_AREA];

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
    private int[] houseHomeLandInArea = new int[GameOfThronesMap.NUM_AREA];

    // Массивы, хранящие необходимые сведения о позициях по треку влияния
    private int[] thronePlayerOnPlace = new int[NUM_PLAYER];
    private int[] thronePlaceForPlayer = new int[NUM_PLAYER];
    private int[] swordPlayerOnPlace = new int[NUM_PLAYER];
    private int[] swordPlaceForPlayer = new int[NUM_PLAYER];
    private int[] ravenPlayerOnPlace = new int[NUM_PLAYER];
    private int[] ravenPlaceForPlayer = new int[NUM_PLAYER];
    // Был ли использован меч в этом раунде
    private boolean swordUsed = false;

    /*
     * Количества юнитов в резерве у каждого из домов
     */
    private int[][] restingUnitsOfPlayerAndType = new int[NUM_PLAYER][NUM_UNIT_TYPES];

    /*
     * **** Вспомогательные переменные для методов ****
     */
    // Количество разыгранных приказов с определённым кодом; нужно для валидации приказов каждого игрока
    private int[] nOrdersWithCode = new int[NUM_DIFFERENT_ORDERS];

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

    /**
     * Конструктор.
     * @param dump "затычка", нужная для игры лошков
     * TODO избавиться от неё, когда напишу нормальных игроков
     */
    public Game(boolean dump) {
        map = new GameOfThronesMap();
        deck1 = new LinkedList<>();
        deck2 = new LinkedList<>();
        deck3 = new LinkedList<>();
        wildlingDeck = new LinkedList<>();
        random = new Random();
        addListener(new GamePhaseChangeListener());
        if (dump) {
            playerInterface = new PrimitivePlayer[NUM_PLAYER];
            for (int i = 0; i < NUM_PLAYER; i++) {
                playerInterface[i] = new PrimitivePlayer(this, i);
            }
        }
        for (int area = 0; area < GameOfThronesMap.NUM_AREA; area++) {
            powerTokenOnArea[area] = -1;
            houseHomeLandInArea[area] = -1;
            armyInArea[area] = new Army(this);
        }
    }

    /**
     * Метод вызывается при старте новой игры. Все переменные принимают начальные значения, текущий прогресс теряется.
     */
    public void startNewGame() {
        System.out.println(NEW_GAME_BEGINS);
        System.out.println(PLAYERS);
        for (int player = 0; player < NUM_PLAYER; player++) {
            System.out.println(playerInterface[player].nameYourself());
        }

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
        // Ставим начальные войска/гарнизоны и обновляем информацию во вспомогательных множествах
        setInitialPosition();
        renewHousesTroopsArea();
        adjustSupply();
        adjustVictoryPoints();
        time = 1;
        wildlingsStrength = 2;
        // Устанавливаем начальные количаства жетонов власти
        for (int player = 0; player < NUM_PLAYER; player++) {
            printSupplyEaters(player);
            nPowerTokensHouse[player] = INITIAL_TOKENS;
            maxPowerTokensHouse[player] = MAX_TOKENS;
            numActiveHouseCardsOfPlayer[player] = NUM_HOUSE_CARDS;
        }
        // Устанавливаем начальные позиции на треках влияния
        for (int place = 0; place < NUM_PLAYER; place++) {
            thronePlayerOnPlace[place] = INITIAL_INFLUENCE_TRACKS_PLAYER_ON_PLACE[0][place];
            swordPlayerOnPlace[place] = INITIAL_INFLUENCE_TRACKS_PLAYER_ON_PLACE[1][place];
            ravenPlayerOnPlace[place] = INITIAL_INFLUENCE_TRACKS_PLAYER_ON_PLACE[2][place];
        }
        fillThronePlaceForPlayer();
        fillSwordPlaceForPlayer();
        fillRavenPlaceForPlayer();
        // готовим карты домов и инициализируем колоды событий и одичалых
        houseCardOfPlayer = new HouseCard[NUM_PLAYER][NUM_HOUSE_CARDS];
        initializeDecks();

        //testDecks();

        // Игра престолов началась.
        setNewGamePhase(GamePhase.planningPhase);
    }

    /* Метод устанавливает начальную позицию Игры престолов II редакции. */
    private void setInitialPosition() {
        // Баратеон
        armyInArea[8].addUnit(UnitType.ship, 0);
        armyInArea[8].addUnit(UnitType.ship, 0);
        armyInArea[0].addUnit(UnitType.ship, 0);
        armyInArea[6].addUnit(UnitType.ship, 0);
        armyInArea[4].addUnit(UnitType.ship, 0);
        armyInArea[1].addUnit(UnitType.ship, 0);
        armyInArea[53].addUnit(UnitType.pawn, 0);
        armyInArea[56].addUnit(UnitType.knight, 0);
        armyInArea[56].addUnit(UnitType.pawn, 0);
        garrisonInArea[56] = 2;
        houseHomeLandInArea[56] = 0;

        // Ланнистер
        armyInArea[3].addUnit(UnitType.ship, 1);
        armyInArea[37].addUnit(UnitType.pawn, 1);
        armyInArea[36].addUnit(UnitType.knight, 1);
        armyInArea[36].addUnit(UnitType.pawn, 1);
        houseHomeLandInArea[36] = 1;
        garrisonInArea[36] = 2;

        // Старк
        armyInArea[11].addUnit(UnitType.ship, 2);
        armyInArea[25].addUnit(UnitType.pawn, 2);
        armyInArea[21].addUnit(UnitType.knight, 2);
        armyInArea[21].addUnit(UnitType.pawn, 2);
        houseHomeLandInArea[21] = 2;
        garrisonInArea[21] = 2;

        // Мартелл
        armyInArea[7].addUnit(UnitType.ship, 3);
        armyInArea[47].addUnit(UnitType.pawn, 3);
        armyInArea[48].addUnit(UnitType.knight, 3);
        armyInArea[48].addUnit(UnitType.pawn, 3);
        houseHomeLandInArea[48] = 3;
        garrisonInArea[48] = 2;

        // Грейджой
        armyInArea[2].addUnit(UnitType.ship, 4);
        armyInArea[13].addUnit(UnitType.ship, 4);
        armyInArea[33].addUnit(UnitType.pawn, 4);
        armyInArea[57].addUnit(UnitType.knight, 4);
        armyInArea[57].addUnit(UnitType.pawn, 4);
        houseHomeLandInArea[57] = 4;
        garrisonInArea[57] = 2;

        // Тирелл
        armyInArea[5].addUnit(UnitType.ship, 5);
        armyInArea[43].addUnit(UnitType.pawn, 5);
        armyInArea[41].addUnit(UnitType.knight, 5);
        armyInArea[41].addUnit(UnitType.pawn, 5);
        houseHomeLandInArea[41] = 5;
        garrisonInArea[41] = 2;

        // Нейтральные лорды
        garrisonInArea[31] = 6;
        garrisonInArea[54] = 5;
    }

    /**
     * Метод обновляет значения areasWithTroopsOfPlayer для всех игроков
     */
    private void renewHousesTroopsArea() {
        for (int player = 0; player < NUM_PLAYER; player++) {
            for (int unitTypeCode = 0; unitTypeCode < NUM_UNIT_TYPES; unitTypeCode++) {
                restingUnitsOfPlayerAndType[player][unitTypeCode] = MAX_NUM_OF_UNITS[unitTypeCode];
            }
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            areasWithTroopsOfPlayer.get(player).clear();
        }
        for (int area = 0; area < GameOfThronesMap.NUM_AREA; area++) {
            int troopsOwner = armyInArea[area].getOwner();
            if(troopsOwner >= 0) {
                areasWithTroopsOfPlayer.get(troopsOwner).put(area, armyInArea[area].getNumUnits());
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
    private void addHouseTroopsInArea(int area) {
        if (armyInArea[area].getOwner() >= 0) {
            areasWithTroopsOfPlayer.get(armyInArea[area].getOwner()).put(area, armyInArea[area].getNumUnits());
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
     * Метод считает и изменяет снабжение каждого из игроков
     */
    private void adjustSupply() {
        for (int player = 0; player < NUM_PLAYER; player++) {
            supply[player] = 0;
        }
        for (int area = 0; area < GameOfThronesMap.NUM_AREA; area++) {
            int areaOwner = getAreaOwner(area);
            if (areaOwner >= 0 && map.getNumBarrel(area) > 0) {
                supply[areaOwner] = Math.min(supply[areaOwner] + map.getNumBarrel(area), MAX_SUPPLY);
            }
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            System.out.println(SUPPLY_OF + HOUSE_GENITIVE[player] + EQUALS + supply[player] + ".");
        }
    }

    /**
     * Метод считает и изменяет количество замков у каждого из игроков
     */
    private void adjustVictoryPoints() {
        for (int player = 0; player < NUM_PLAYER; player++) {
            victoryPoints[player] = 0;
        }
        for (int area = 0; area < GameOfThronesMap.NUM_AREA; area++) {
            int areaOwner = getAreaOwner(area);
            if (areaOwner >= 0 && map.getNumCastle(area) > 0) {
                victoryPoints[areaOwner]++;
            }
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            System.out.println("У " + HOUSE_GENITIVE[player] + " " + victoryPoints[player] +
                    (victoryPoints[player] == 1 ? OF_CASTLE :
                    victoryPoints[player] > 1 && victoryPoints[player] < 5 ? OF_CASTLA : OF_CASTLES) + ".");
        }
    }

    private void getPlans() {
        HashMap<Integer, Order> orders;
        Order curOrder;
        for (int player = 0; player < NUM_PLAYER; player++) {
            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                System.out.println();
                orders = playerInterface[player].giveOrders();
                System.out.println(ORDERS + HOUSE_GENITIVE[player] + ":");
                for (Map.Entry<Integer, Order> entry : orders.entrySet()) {
                    System.out.println(AREA_NUMBER + entry.getKey() +
                            " (" + map.getAreaNameRus(entry.getKey()) + ")" + ": " + entry.getValue());
                }
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
        StringBuffer stringBuffer = new StringBuffer();
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
                if (!alright) stringBuffer.append("\n");
                stringBuffer.append(IN_AREA_NUMBER).append(areaOfOrder)
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
                if (!alright) stringBuffer.append("\n");
                stringBuffer.append(AREA_NUMBER).append(areaOfOrder).append(NO_CP_IN_SEA);
                alright = false;
            }
            // В порту не может быть обороны
            if (map.getAreaType(areaOfOrder) == AreaType.port && order.orderType() == OrderType.defence) {
                if (!alright) stringBuffer.append("\n");
                stringBuffer.append(AREA_NUMBER).append(areaOfOrder).append(NO_DEFENCE_IN_PORT);
                alright = false;
            }
            // Проверяем, что нет приказов запрещённого событием третьей колоды приказа
            if (order.orderType() == prohibitedOrder && order != Order.marchB && order != Order.march) {
                if (!alright) stringBuffer.append("\n");
                stringBuffer.append(AREA_NUMBER).append(areaOfOrder).append(PROHIBITED_ORDER)
                        .append(order.toString());
                alright = false;
            }
        }
        // Проверяем, что количество приказов любого типа не превышает максимального возможного числа таких приказов
        for (int orderCode = 0; orderCode < NUM_DIFFERENT_ORDERS; orderCode++) {
            if (nOrdersWithCode[orderCode] > Order.maxNumOrdersWithCode[orderCode]) {
                if (!alright) stringBuffer.append("\n");
                stringBuffer.append(nOrdersWithCode[orderCode]).append(OF_ORDERS).append(Order.getOrderWithCode(orderCode))
                        .append(WHEN_MAX_OF_SUCH_ORDERS_IS).append(Order.maxNumOrdersWithCode[orderCode]);
                alright = false;
            }
        }

        // Проверяем, что соблюдено ограничение по звёздам
        int numStars = NUM_OF_STARS_ON_PLACE[ravenPlaceForPlayer[player]];
        if (nStarOrders > numStars) {
            if (!alright) stringBuffer.append("\n");
            stringBuffer.append(STAR_NUMBER_VIOLENCE).append(numStars).append(OF_STAR_ORDERS).append(nStarOrders);
            alright = false;
        }

        if (!alright) {
            stringBuffer.insert(0, ORDER_MISTAKES + HOUSE_GENITIVE[player] + ":\n");
            System.out.println(stringBuffer);
        }
        return alright;
    }

    /**
     * Метод разруливает ситуацию после фазы замыслов, когда владелец посыльного ворона может
     * поменять один свой приказ или посмотреть верхнюю карту одичалых
     */
    private void getRavenDecision() {
        int ravenHolder = ravenPlayerOnPlace[0];
        for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
            String ravenUse = playerInterface[ravenHolder].useRaven();
            // Ворононосец выбрал просмотр карты одичалых
            if (ravenUse.equals(RAVEN_SEES_WILDLINGS_CODE)) {
                System.out.println(HOUSE[ravenHolder] + SEES_WILDLINGS_CARD);
                if (!playerInterface[ravenHolder].leaveWildlingCardOnTop(wildlingDeck.getFirst())) {
                    WildlingCard card = wildlingDeck.pollFirst();
                    wildlingDeck.addLast(card);
                    System.out.println(AND_BURIES);
                } else {
                    System.out.println(AND_LEAVES);
                }
                break;
            }
            // Ворононосец выбрал замену приказа
            if (ravenUse.charAt(0) >= '0' && ravenUse.charAt(0) <= '9') {
                System.out.println(HOUSE[ravenHolder] + CHANGES_ORDER);
                String[] s = ravenUse.split(" ");
                if (s.length != 2) {
                    System.out.println(RAVEN_CHANGE_ORDER_FORMAT_ERROR);
                    continue;
                }
                try {
                    int area = Integer.valueOf(s[0]);
                    int newOrderCode = Integer.valueOf(s[1]);
                    // TODO реализовать ёбаный кусок кода
                    System.out.println("ОШИБКА! НЕРЕАЛИЗОВАННЫЙ КУСОК КОДА!");
                    break;
                } catch (NumberFormatException ex) {
                    System.out.println(RAVEN_CHANGE_ORDER_PARSE_ERROR);
                }
            }
        }

        // Если есть событие "Море штормов", то после ворона сразу переходим к походом, иначе сначала разыгрываем набеги
        setNewGamePhase(event3 == Deck3Cards.seaOfStorms ? GamePhase.marchPhase : GamePhase.raidPhase);
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
            player = thronePlayerOnPlace[place];
            if (areasWithRaids.get(player).isEmpty()) {
                numNoRaidsPlayers++;
            } else {
                numNoRaidsPlayers = 0;
                System.out.println(HOUSE[player] + MUST_PLAY_RAID);
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    RaidOrderPlayed raid = playerInterface[player].playRaid();
                    // Проверяем вариант розыгрыша набега, полученный от игрока, на валидность
                    if (validateRaid(raid, player)) {
                        System.out.print(HOUSE[player]);
                        int from = raid.getAreaFrom();
                        int to = raid.getAreaTo();
                        if (to >= 0 && to < GameOfThronesMap.NUM_AREA && to != from) {
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
                                    }
                                    if (nPowerTokensHouse[raidedPlayer] > 0) {
                                        nPowerTokensHouse[raidedPlayer]--;
                                    }
                                    break;
                            }
                            orderInArea[to] = null;
                            orderInArea[from] = null;
                            areasWithRaids.get(player).remove(from);
                        } else {
                            // Разыгрываем "холостой" набег, снимаем приказ и обновляем попутные переменные
                            System.out.println(DELETES_RAID + map.getAreaNameRusGenitive(from));
                            orderInArea[from] = null;
                            areasWithRaids.get(player).remove(from);
                            break;
                        }
                        break;
                    }
                }
                // Если игрок не уложился в количество попыток, то удаляем все его приказы набегов
                if (attempt >= MAX_TRIES_TO_GO) {
                    System.out.println(HOUSE[player] + FAILED_TO_PLAY_RAID);
                    for (int area: areasWithRaids.get(player)) {
                        orderInArea[area] = null;
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
        if (from < 0 || from >= GameOfThronesMap.NUM_AREA || to >= GameOfThronesMap.NUM_AREA) {
            System.out.println(WRONG_AREAS_RAID_ERROR);
            return false;
        }
        if (orderInArea[from] == null || orderInArea[from].orderType() != OrderType.raid ||
                armyInArea[from].getOwner() != player) {
            System.out.println(NO_RAID_ERROR);
            return false;
        }

        // Случай результативного набега
        if (to >= 0 && to < GameOfThronesMap.NUM_AREA && to != from) {
            System.out.println(RAIDS + map.getAreaNameRusGenitive(from) + TO + map.getAreaNameRusAccusative(to) + ".");
            if (map.getAdjacencyType(from, to) == AdjacencyType.noAdjacency) {
                System.out.println(NO_ADJACENT_RAID_ERROR);
                return false;
            }
            if (armyInArea[to].getOwner() == player) {
                System.out.println(DONT_RAID_YOURSELF_ERROR);
                return false;
            }
            if (armyInArea[to].getOwner() < 0 || orderInArea[to] == null) {
                System.out.println(NO_ONE_TO_RAID_THERE_ERROR);
                return false;
            }
            if (map.getAdjacencyType(from, to) == AdjacencyType.landToSea) {
                System.out.println(NO_RAID_FROM_LAND_TO_SEA_ERROR);
                return false;
            }
            if (orderInArea[to].orderType() == OrderType.march ||
                    orderInArea[to].orderType() == OrderType.defence && orderInArea[from] == Order.raid) {
                System.out.println(CANT_RAID_THIS_ORDER_ERROR);
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
            player = thronePlayerOnPlace[place];
            if (areasWithMarches.get(player).isEmpty()) {
                numNoMarchesPlayers++;
            } else {
                numNoMarchesPlayers = 0;
                System.out.println(HOUSE[player] + MUST_PLAY_MARCH);
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    MarchOrderPlayed march = playerInterface[player].playMarch();
                    // Проверяем вариант розыгрыша похода, полученный от игрока, на валидность
                    if (validateMarch(march, player)) {
                        System.out.print(HOUSE[player]);
                        int from = march.getAreaFrom();
                        HashMap<Integer, Army> destinationsOfMarch = march.getDestinationsOfMarch();
                        if (destinationsOfMarch.size() == 0) {
                            // Случай "холостого" снятия приказа похода
                            System.out.println(DELETES_MARCH + map.getAreaNameRusGenitive(from));
                            orderInArea[from] = null;
                            areasWithMarches.get(player).remove(from);
                        } else {
                            // Случай результативного похода: перемещаем юниты, если нужно, начинаем бой,
                            // и обновляем попутные переменные
                            System.out.println(PLAYS_MARCH + map.getAreaNameRusGenitive(from) + ".");
                            int areaWhereBattleBegins = -1;
                            for (Map.Entry<Integer, Army> entry: destinationsOfMarch.entrySet()) {
                                int curDestination = entry.getKey();
                                Army curArmy = entry.getValue();
                                int destinationArmyOwner = armyInArea[curDestination].getOwner();
                                if (destinationArmyOwner >= 0 && destinationArmyOwner != player ||
                                        houseHomeLandInArea[curDestination] >= 0 &&
                                        houseHomeLandInArea[curDestination] != player && garrisonInArea[curDestination] > 0)
                                {
                                    // Если в области завязывается бой, то запоминаем её и проходим последней
                                    areaWhereBattleBegins = curDestination;
                                    attackingArmy = curArmy;
                                } else {
                                    // Иначе перемещаем войска и пробиваем гарнизоны
                                    System.out.print(curArmy.toString() +
                                            (curArmy.getSize() == 1 ? MOVES_TO : MOVE_TO) +
                                            map.getAreaNameRusAccusative(curDestination) + ".");
                                    armyInArea[curDestination].addSubArmy(curArmy);
                                    addHouseTroopsInArea(curDestination);
                                    if (map.getNumCastle(curDestination) > 0) {
                                        victoryPoints[player]++;
                                    }
                                    // Если в области имелся нейтральный гарнизон, то пробиваем его
                                    if (destinationArmyOwner < 0 && garrisonInArea[curDestination] > 0) {
                                        System.out.println(GARRISON_IS_DEFEATED + HOUSE_GENITIVE[player] + " - " +
                                                calculatePowerOfPlayerVersusGarrison(player, curDestination, curArmy,
                                                orderInArea[curDestination].getModifier()) + GARRISON_STRENGTH_IS +
                                                garrisonInArea[curDestination]);
                                        garrisonInArea[curDestination] = 0;
                                    } else {
                                        System.out.println();
                                    }
                                }
                                armyInArea[from].deleteSubArmy(curArmy);
                            }
                            areasWithTroopsOfPlayer.get(player).remove(from);
                            addHouseTroopsInArea(from);
                            if (armyInArea[from].isEmpty() && houseHomeLandInArea[from] < 0) {
                                if (march.isLeaveToken()) {
                                    System.out.println(HOUSE[player] + LEAVES_POWER_TOKEN +
                                            map.getAreaNameRusLocative(from) + ".");
                                    powerTokenOnArea[from] = player;
                                    nPowerTokensHouse[player]--;
                                    maxPowerTokensHouse[player]--;
                                } else {
                                    if (map.getNumCastle(from) > 0) {
                                        victoryPoints[player]--;
                                    }
                                }
                            }
                            if (victoryPoints[player] >= NUM_CASTLES_TO_WIN) {
                                setNewGamePhase(GamePhase.end);
                                return;
                            }
                            if (areaWhereBattleBegins >= 0) {
                                System.out.println(attackingArmy.toString() +
                                        (attackingArmy.getSize() == 1 ? MOVES_TO : MOVE_TO) +
                                        map.getAreaNameRusAccusative(areaWhereBattleBegins) +
                                        (attackingArmy.getSize() == 1 ? AND_FIGHTS : AND_FIGHT));
                                playFight(areaWhereBattleBegins, from, orderInArea[from].getModifier());
                            }
                            orderInArea[from] = null;
                            areasWithMarches.get(player).remove(from);
                        }
                        break;
                    }
                }
                // Если игрок не уложился в количество попыток, то удаляем один его приказ похода
                if (attempt >= MAX_TRIES_TO_GO) {
                    System.out.println(HOUSE[player] + FAILED_TO_PLAY_MARCH);
                    int area = areasWithMarches.get(player).iterator().next();
                    orderInArea[area] = null;
                    areasWithMarches.get(player).remove(area);
                } else {
                    printAreaWithTroopsOfPlayers();
                }
            }
            place++;
            if (place == NUM_PLAYER) place = 0;
        }
    }

    /**
     * Метод для валидации варианта розыгрыша похода, полученного от игрока
     * @param march  вариант розыгрыша похода
     * @param player номер игрока
     * @return true, если так разыграть поход можно
     */
    private boolean validateMarch(MarchOrderPlayed march, int player) {
        int from = march.getAreaFrom();
        HashMap<Integer, Army> destinationsOfMarch = march.getDestinationsOfMarch();

        // Банальные проверки на валидность областей и наличие похода, происходят для всех вариантов походов без исключения
        boolean wrongAreasFlag = false;
        for (int areaDestination: destinationsOfMarch.keySet()) {
            if (areaDestination < 0 || areaDestination >= GameOfThronesMap.NUM_AREA) {
                wrongAreasFlag = true;
            }
        }
        if (wrongAreasFlag || from < 0 || from >= GameOfThronesMap.NUM_AREA) {
            System.out.println(WRONG_AREAS_MARCH_ERROR);
            return false;
        }
        if (orderInArea[from] == null || orderInArea[from].orderType() != OrderType.march ||
                armyInArea[from].getOwner() != player) {
            System.out.println(NO_MARCH_ERROR);
            return false;
        }

        System.out.print(MARCH_VALIDATION + map.getAreaNm(from) + ": ");
        boolean firstFlag = true;
        for (Map.Entry<Integer, Army> entry : destinationsOfMarch.entrySet()) {
            firstFlag = LittleThings.printDelimiter(firstFlag);
            int curDestination = entry.getKey();
            Army curArmy = entry.getValue();
            System.out.print(curArmy.getShortString() + " -> " + map.getAreaNm(curDestination));
        }
        System.out.println();

        if (destinationsOfMarch.size() > 0) {
            // Дополнительные проверки в случае результативного похода
            int areaWhereBattleBegins = -1;
            HashSet<Integer> accessibleAreas = null;
            int nPawns = 0, nKnights = 0, nShips = 0, nSiegeEngines = 0;
            if (!map.getAreaType(from).isNaval()) {
                /*
                 * Составляем множество областей, в которые может пойти игрок из данной области с учётом
                 * правила переброски морем
                 */
                accessibleAreas = getAccessibleAreas(from, player);
            }
            for (Map.Entry<Integer, Army> entry : destinationsOfMarch.entrySet()) {
                int curDestination = entry.getKey();
                Army curArmy = entry.getValue();
                // Проверяем, что в данную область назначения действительно можно пойти
                if (map.getAreaType(from).isNaval() && !map.getAreaType(curDestination).isNaval()) {
                    System.out.println(CANT_MARCH_FROM_SEA_TO_LAND_ERROR);
                    return false;
                }
                if (map.getAreaType(from).isNaval() && map.getAdjacencyType(from, curDestination) == AdjacencyType.noAdjacency) {
                    System.out.println(CANT_MARCH_THERE_ERROR);
                    return false;
                }
                if (map.getAreaType(from).isNaval() && map.getAreaType(curDestination) == AreaType.port) {
                    // Если замок, которому принадлежит порт, в который хочет зайти игрок, ему не принадлежит, то фейл
                    if (!canMoveInPort(curDestination, player)) {
                        System.out.println(CANT_MARCH_IN_NOT_YOUR_PORT_ERROR);
                        return false;
                    }
                }
                if (!map.getAreaType(from).isNaval() && map.getAreaType(curDestination).isNaval()) {
                    System.out.println(CANT_MARCH_FROM_LAND_TO_SEA_ERROR);
                    return false;
                }
                if (!map.getAreaType(from).isNaval() && !accessibleAreas.contains(curDestination)) {
                    System.out.println(NO_WAY_MARCH_ERROR);
                    return false;
                }
                // Проверяем, что марширующая армия не пуста
                if (curArmy.isEmpty()) {
                    System.out.println(EMPTY_ARMY_MARCH_ERROR);
                    return false;
                }

                // Считаем, начнётся ли битва в области назначения (пробивание нейтрального гарнизона тоже считается)
                int destinationArmyOwner = armyInArea[curDestination].getOwner();
                if (destinationArmyOwner >= 0 && destinationArmyOwner != player ||
                        garrisonInArea[curDestination] > 0 && destinationArmyOwner != player) {
                    if (areaWhereBattleBegins < 0) {
                        if (destinationArmyOwner < 0 && garrisonInArea[curDestination] > 0) {
                            int playerStrength = calculatePowerOfPlayerVersusGarrison(player, curDestination, curArmy,
                                    orderInArea[from].getModifier());
                            if (playerStrength < garrisonInArea[curDestination]) {
                                System.out.println(CANT_BEAT_NEUTRAL_GARRISON_ERROR_PLAYER_STRENGTH + playerStrength +
                                        GARRISON_STRENGTH_IS + garrisonInArea[curDestination]);
                                return false;
                            }
                        }
                        areaWhereBattleBegins = curDestination;
                    } else {
                        System.out.println(CANT_BEGIN_TWO_BATTLES_BY_ONE_MARCH_ERROR);
                        return false;
                    }
                }

                // Проверки данной области назначения пройдены. Теперь учитываем юниты, перемещающиеся в эту область
                for (Unit unit : curArmy.getUnits()) {
                    switch (unit.getUnitType()) {
                        case pawn:
                            nPawns++;
                            break;
                        case knight:
                            nKnights++;
                            break;
                        case ship:
                            nShips++;
                            break;
                        case siegeEngine:
                            nSiegeEngines++;
                            break;
                    }
                }
            }

            // Проверка наличия необходимых юнитов в области отправления
            for (Unit unit : armyInArea[from].getUnits()) {
                if (unit.isWounded()) continue;
                switch (unit.getUnitType()) {
                    case pawn:
                        if (nPawns > 0) nPawns--;
                        break;
                    case knight:
                        if (nKnights > 0) nKnights--;
                        break;
                    case ship:
                        if (nShips > 0) nShips--;
                        break;
                    case siegeEngine:
                        if (nSiegeEngines > 0) nSiegeEngines--;
                        break;
                }
            }
            if (nPawns > 0 || nKnights > 0 || nShips > 0 || nSiegeEngines > 0) {
                System.out.println(LACK_OF_UNITS_ERROR);
                return false;
            }

            // Проверка по снабжению
            if (!supplyTestForMarch(march)) {
                System.out.println(SUPPLY_VIOLATION_ERROR);
                return false;
            }

            // Проверки возможности оставить жетон власти
            if (march.isLeaveToken() && nPowerTokensHouse[player] == 0) {
                System.out.println(NO_POWER_TOKENS_TO_LEAVE_ERROR);
                return false;
            }
            if (march.isLeaveToken() && map.getAreaType(from).isNaval()) {
                System.out.println(CANT_LEAVE_POWER_TOKEN_IN_SEA_ERROR);
                return false;
            }
        }
        return true;
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
        HashSet<Integer> adjacentAreas = map.getAdjacentAreas(portArea);
        int castleWithThisPort = -1;
        // Ищем замок, которому принадлежит этот порт
        for (int curArea: adjacentAreas) {
            if (map.getNumCastle(curArea) > 0) {
                castleWithThisPort = curArea;
                break;
            }
        }
        // Если замок, которому принадлежит порт, в который хочет зайти игрок, принадлежит ему, то всё хорошо
        return getAreaOwner(castleWithThisPort) == player;
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
            System.out.println(NO_DEFENDER_ERROR);
            return;
        }
        System.out.println(BATTLE_BEGINS_FOR + map.getAreaNameRusAccusative(areaOfBattle) + BETWEEN +
                HOUSE_ABLATIVE[playerOnSide[0]] + " и " + HOUSE_ABLATIVE[playerOnSide[1]] + ".");
        BattleInfo battleInfo = new BattleInfo(playerOnSide[0], playerOnSide[1], areaOfBattle, map.getNumCastle(areaOfBattle) > 0);
        battleInfo.addStrength(SideOfBattle.attacker, marchModifier);
        battleInfo.addArmyToSide(SideOfBattle.attacker, attackingArmy);
        battleInfo.addStrength(SideOfBattle.defender, garrisonInArea[areaOfBattle]);
        if (!armyInArea[areaOfBattle].isEmpty()) {
            battleInfo.addArmyToSide(SideOfBattle.defender, armyInArea[areaOfBattle]);
        }

        if (orderInArea[areaOfBattle] != null && orderInArea[areaOfBattle].orderType() == OrderType.defence) {
            battleInfo.addStrength(SideOfBattle.defender, orderInArea[areaOfBattle].getModifier());
        }
        // Учитываем подмоги из соседних областей
        SideOfBattle[] supportOfPlayer = new SideOfBattle[NUM_PLAYER];
        HashSet<Integer> supporters = new HashSet<>();
        ArrayList<Integer> areasOfSupport = new ArrayList<>();
        // Спрашиваем игроков, кого они поддержат. Не имеет смысла, если есть событие "Паутина лжи"
        if (event3 != Deck3Cards.webOfLies) {
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
            for (int player: supporters) {
                firstFlag = LittleThings.printDelimiter(firstFlag);
                System.out.print(HOUSE[player]);
            }
            if (!supporters.isEmpty()) {
                System.out.println(CAN_SUPPORT_SOMEBODY);
            }

            for (int player : supporters) {
                for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    sideOfBattle = playerInterface[player].sideToSupport(battleInfo);
                    System.out.print(HOUSE[player]);
                    switch (sideOfBattle) {
                        case attacker:
                            System.out.println(SUPPORTS + HOUSE_GENITIVE[playerOnSide[0]] + ".");
                            break;
                        case defender:
                            System.out.println(SUPPORTS + HOUSE_GENITIVE[playerOnSide[1]] + ".");
                            break;
                        default:
                            System.out.println(SUPPORTS_NOBODY);
                    }
                    if (sideOfBattle == SideOfBattle.attacker && player == playerOnSide[1] ||
                            sideOfBattle == SideOfBattle.defender && player == playerOnSide[0]) {
                        System.out.println(CANT_SUPPORT_AGAINST_YOURSELF_ERROR);
                    } else {
                        supportOfPlayer[player] = sideOfBattle;
                        break;
                    }
                }
                if (supportOfPlayer[player] == null) {
                    // Если игрок так и не смог определиться с подмогой, он никому её не оказывает
                    System.out.println(HOUSE[player] + SUPPORTS_NOBODY);
                    supportOfPlayer[player] = SideOfBattle.neutral;
                }
            }

            // Добавляем боевую силу поддерживающих войск
            for (int areaOfSupport: areasOfSupport) {
                battleInfo.addArmyToSide(supportOfPlayer[getTroopsOwner(areaOfSupport)], armyInArea[areaOfSupport]);
                if (supportOfPlayer[getTroopsOwner(areaOfSupport)] == SideOfBattle.attacker) {
                    numSupportsOnSide[0]++;
                }
                if (supportOfPlayer[getTroopsOwner(areaOfSupport)] == SideOfBattle.defender) {
                    numSupportsOnSide[1]++;
                }
            }
        }

        // Выбираем карты дома
        System.out.println(HOUSES_CHOOSE_CARDS);
        System.out.println(RELATION_OF_FORCES_IS + battleInfo.getAttackerStrength() + VERSUS +
                battleInfo.getDefenderStrength() + ".");
        houseCardOfSide[0] = getHouseCard(battleInfo, playerOnSide[0]);
        houseCardOfSide[1] = getHouseCard(battleInfo, playerOnSide[1]);

        countBattleVariables(battleInfo);

        int firstSideOnThrone = thronePlaceForPlayer[playerOnSide[0]] < thronePlaceForPlayer[playerOnSide[1]] ? 0 : 1;
        HouseCard temporaryInactiveCard = null;
        boolean propertyUsed;
        // Карты отмены: Тирион
        for (int curSide = 0; curSide < 2; curSide++) {
            int heroSide = (curSide + firstSideOnThrone) % 2;
            if (houseCardOfSide[heroSide] == HouseCard.tyrionLannister) {
                System.out.println(HOUSE[playerOnSide[heroSide]] + CAN_USE_SPECIAL_PROPERTY_OF_CARD + houseCardOfSide[heroSide]);
                switch (houseCardOfSide[heroSide]) {
                    case tyrionLannister:
                        boolean useTyrion = playerInterface[playerOnSide[heroSide]].useTyrion(battleInfo, houseCardOfSide[1 - heroSide]);
                        if (useTyrion) {
                            System.out.println(TYRION_CANCELS);
                            temporaryInactiveCard = houseCardOfSide[1 - heroSide];
                            houseCardOfSide[1 - heroSide].setActive(false);
                            numActiveHouseCardsOfPlayer[playerOnSide[1 - heroSide]]--;
                            houseCardOfSide[1 - heroSide] = null;
                            countBattleVariables(battleInfo);
                            houseCardOfSide[1 - heroSide] = getHouseCard(battleInfo, playerOnSide[1 - heroSide]);
                            countBattleVariables(battleInfo);
                        } else {
                            System.out.println(houseCardOfSide[heroSide] + NO_EFFECT);
                        }
                }
            }
        }
        // Немедленные карты: Мейс, Бабка, Доран и Эйерон
        for (int curSide = 0; curSide < 2; curSide++) {
            int heroSide = (curSide + firstSideOnThrone) % 2;
            if (houseCardOfSide[heroSide].getCardInitiative() == CardInitiative.immediately) {
                System.out.println(HOUSE[playerOnSide[heroSide]] + CAN_USE_SPECIAL_PROPERTY_OF_CARD + houseCardOfSide[heroSide]);
                switch (houseCardOfSide[heroSide]) {
                    case maceTyrell:
                        // Чёрная Рыба защищает от свойства Мейса Тирелла
                        if (houseCardOfSide[1 - heroSide] == HouseCard.theBlackfish) {
                            System.out.println(BLACKFISH_SAVES + HOUSE_GENITIVE[playerOnSide[1 - heroSide]] + FROM_LOSSES);
                            break;
                        }
                        Army armyToSearchForFootmen = heroSide == 0 ? armyInArea[areaOfBattle] : attackingArmy;
                        Unit victimOfMace = armyToSearchForFootmen.getUnitOfType(UnitType.pawn);
                        if (victimOfMace != null) {
                            System.out.println(MACE_EATS_MAN + map.getAreaNameRusLocative(areaOfBattle));
                            battleInfo.deleteUnit(heroSide == 0 ? SideOfBattle.defender : SideOfBattle.attacker, victimOfMace);
                            armyToSearchForFootmen.killUnit(victimOfMace, KillingReason.mace);
                            countBattleVariables(battleInfo);
                        } else {
                            System.out.println(houseCardOfSide[heroSide] + NO_EFFECT);
                        }
                        break;
                    case queenOfThorns:
                        propertyUsed = false;
                        accessibleAreaSet.clear();
                        for (int adjacentArea: map.getAdjacentAreas(areaOfBattle)) {
                            if (orderInArea[adjacentArea] != null && getTroopsOwner(adjacentArea) == playerOnSide[1 - heroSide]) {
                                accessibleAreaSet.add(adjacentArea);
                            }
                        }
                        if (!accessibleAreaSet.isEmpty()) {
                            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                                int area = playerInterface[playerOnSide[heroSide]].chooseAreaQueenOfThorns(accessibleAreaSet);
                                // Если область меньше нуля, значит, игрок отказался использовать свойство карты
                                if (area < 0) {
                                    break;
                                }
                                if (area >= GameOfThronesMap.NUM_AREA) {
                                    System.out.println(INVALID_AREA_ERROR);
                                }
                                System.out.println(QUEEN_OF_THORNS_REMOVES_ORDER + map.getAreaNameRusGenitive(area));
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
                                                battleInfo.deleteArmy(heroSide == 0 ? SideOfBattle.defender : SideOfBattle.attacker,
                                                        armyInArea[area]);
                                                numSupportsOnSide[1 - heroSide]--;
                                                countBattleVariables(battleInfo);
                                            }
                                    }
                                    orderInArea[area] = null;
                                    break;
                                } else {
                                    System.out.println(INVALID_AREA_ERROR);
                                }
                            }
                        }
                        if (!propertyUsed) {
                            System.out.println(houseCardOfSide[heroSide] + NO_EFFECT);
                        }
                        break;
                    case doranMartell:
                        TrackType trackToPissOff = playerInterface[playerOnSide[heroSide]].chooseInfluenceTrackDoran(battleInfo);
                        if (trackToPissOff != null ) {
                            System.out.println(DORAN_ABUSES + HOUSE_GENITIVE[playerOnSide[1 - heroSide]] +
                                    trackToPissOff.onTheTrack());
                            pissOffOnTrack(playerOnSide[1 - heroSide], trackToPissOff);
                            countBattleVariables(battleInfo);
                        } else {
                            System.out.println(houseCardOfSide[heroSide] + NO_EFFECT);
                        }
                        break;
                    case aeronDamphair:
                        propertyUsed = false;
                        if (nPowerTokensHouse[playerOnSide[heroSide]] >= 2 &&
                                numActiveHouseCardsOfPlayer[playerOnSide[heroSide]] >= 2) {
                            boolean useDamphair = playerInterface[playerOnSide[heroSide]].useAeron(battleInfo);
                            if (useDamphair) {
                                System.out.println(AERON_RUNS_AWAY + HOUSE[playerOnSide[heroSide]] + MUST_CHOOSE_OTHER_CARD);
                                propertyUsed = true;
                                houseCardOfSide[heroSide].setActive(false);
                                numActiveHouseCardsOfPlayer[playerOnSide[heroSide]]--;
                                houseCardOfSide[heroSide] = getHouseCard(battleInfo, playerOnSide[heroSide]);
                                countBattleVariables(battleInfo);
                            }
                        }
                        if (!propertyUsed) {
                            System.out.println(houseCardOfSide[heroSide] + NO_EFFECT);
                        }
                        break;
                }
            }
        }

        battleInfo.addStrength(SideOfBattle.attacker, cardStrengthOnSide[0] + bonusStrengthOnSide[0]);
        battleInfo.addStrength(SideOfBattle.defender, cardStrengthOnSide[1] + bonusStrengthOnSide[1]);
        // Валирийский меч
        int swordsMan = swordPlayerOnPlace[0];
        if (!swordUsed && (playerOnSide[0] == swordsMan || playerOnSide[1] == swordsMan)) {
            boolean useSword = playerInterface[swordsMan].useSword(battleInfo);
            if (useSword) {
                battleInfo.addStrength(playerOnSide[0] == swordsMan ? SideOfBattle.attacker : SideOfBattle.defender, 1);
                swordUsed = true;
                System.out.println(HOUSE[swordsMan] + USES_SWORD);
                printRelationOfForces(battleInfo, false);
            }
        }
        // Делаем неактивными карты, которые были сыграны, и возвращаем к жизни карту, отменённую Тирионом
        if (temporaryInactiveCard != null) {
            temporaryInactiveCard.setActive(true);
            numActiveHouseCardsOfPlayer[temporaryInactiveCard.house()]++;
        }
        for (int side = 0; side < 2; side++) {
            if (houseCardOfSide[side] != HouseCard.none) {
                houseCardOfSide[side].setActive(false);
                numActiveHouseCardsOfPlayer[playerOnSide[side]]--;
                // Если это была последняя активная карта игрока, то обновляем ему колоду
                if (numActiveHouseCardsOfPlayer[playerOnSide[side]] == 0) {
                    renewHandExceptCard(playerOnSide[side], houseCardOfSide[side]);
                }
            }
        }
        // Определяем победителя
        int winnerSide = battleInfo.getAttackerStrength() > battleInfo.getDefenderStrength() ||
                battleInfo.getAttackerStrength() == battleInfo.getDefenderStrength() &&
                swordPlaceForPlayer[playerOnSide[0]] < swordPlaceForPlayer[playerOnSide[1]] ? 0 : 1;
        int winner = playerOnSide[winnerSide];
        int loser = playerOnSide[1 - winnerSide];
        System.out.println(HOUSE[winner] + WINS_THE_BATTLE);
        // Подсчитываем потери проигравшего
        System.out.println("У " + HOUSE_GENITIVE[winner] + " " + swordsOnSide[winnerSide] +
                SWORDS_U + HOUSE_GENITIVE[loser] + " " + towersOnSide[1 - winnerSide] + OF_TOWERS);
        int numKilledUnits = Math.max(0, swordsOnSide[winnerSide] - towersOnSide[1 - winnerSide]);
        if (numKilledUnits == 0) {
            System.out.println(HOUSE[loser] + HAS_NO_LOSSES);
        } else if (houseCardOfSide[1 - winnerSide] == HouseCard.theBlackfish) {
            System.out.println(BLACKFISH_SAVES + HOUSE_GENITIVE[loser] + FROM_LOSSES);
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
                System.out.println(GARRISON + map.getAreaNameRusLocative(areaOfBattle) + IS_DEFEATED_M);
                garrisonInArea[areaOfBattle] = 0;
            }
            // Лорас Тирелл контрится Арианной
            if (houseCardOfSide[0] == HouseCard.serLorasTyrell && houseCardOfSide[1] != HouseCard.arianneMartell) {
                System.out.println(LORAS_RULES);
                Order marchOrder = marchModifier == -1 ? Order.marchB : (marchModifier == 0 ? Order.march : Order.marchS);
                orderInArea[areaOfBattle] = marchOrder;
                areasWithMarches.get(winner).add(areaOfBattle);
            }

            retreatingArmy = armyInArea[areaOfBattle];
            // Арианна
            if (houseCardOfSide[1] == HouseCard.arianneMartell) {
                System.out.println(ARIANNA_RULES_AND_PUTS_BACK + map.getAreaNameRusAccusative(areaOfMarch) + ".");
                armyInArea[areaOfMarch].addSubArmy(attackingArmy);
                addHouseTroopsInArea(areaOfMarch);
            } else {
                armyInArea[areaOfBattle] = attackingArmy;
                addHouseTroopsInArea(areaOfBattle);
            }
            attackingArmy = null;
            // Отступление
            if (retreatingArmy.getNumUnits() > 0) {
                int numRetreatingUnits = retreatingArmy.getNumUnits();
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
                    int areaOwner = getAreaOwner(area);
                    if (areaOwner < 0 || areaOwner == playerOnSide[1 - winnerSide]) {
                        if (getTroopsOwner(area) < 0) {
                            curLosses = 0;
                        } else {
                            int previousNumberOfUnits = virtualAreasWithTroops.get(area);
                            for (curLosses = 0; curLosses < numRetreatingUnits; curLosses++) {
                                virtualAreasWithTroops.put(area, previousNumberOfUnits + numRetreatingUnits - curLosses);
                                if (supplyTest(virtualAreasWithTroops, loser)) break;
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
                        retreatingArmy.killSomeUnits(minLosses);
                    }
                    printAreasInSet(trueAreasToRetreat, "Области для отступления");
                    switch (trueAreasToRetreat.size()) {
                        case 0:
                            System.out.println(NO_AREAS_TO_RETREAT);
                        case 1:
                            // Если имеется единтсвенная область для отступления, то отступаем туда автоматически
                            if (minLosses > 0) {
                                retreatingArmy.killSomeUnits(minLosses);
                            }
                            int onlyArea = trueAreasToRetreat.iterator().next();
                            armyInArea[onlyArea].addSubArmy(retreatingArmy);
                            addHouseTroopsInArea(onlyArea);
                            System.out.println(HOUSE[loser] + RETREATS_IN +
                                    map.getAreaNameRusAccusative(onlyArea));
                            break;
                        default:
                            // Если есть выбор, то спрашиваем игрока, куда ему отступить
                            int player = houseCardOfSide[winnerSide] == HouseCard.robbStark ?
                                    playerOnSide[winnerSide] : loser;
                            boolean successFlag = false;
                            System.out.println(HOUSE[loser] + MUST_RETREAT);
                            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                                int area = playerInterface[player].chooseAreaToRetreat(retreatingArmy, trueAreasToRetreat);
                                System.out.println(HOUSE[loser] + RETREATS_IN +
                                        map.getAreaNameRusAccusative(area));
                                if (trueAreasToRetreat.contains(area)) {
                                    armyInArea[area].addSubArmy(retreatingArmy);
                                    addHouseTroopsInArea(area);
                                    successFlag = true;
                                    break;
                                } else {
                                    System.out.println(CANT_RETREAT_THERE_ERROR);
                                }
                            }
                            // Если игрок так и не смог отступить, то отступаем в первую доступную область
                            if (!successFlag) {
                                int area = trueAreasToRetreat.iterator().next();
                                armyInArea[area].addSubArmy(retreatingArmy);
                                addHouseTroopsInArea(area);
                                System.out.println(HOUSE[loser] + RETREATS_IN +
                                        map.getAreaNameRusAccusative(area));
                            }
                            break;
                    }
                }
            }
            retreatingArmy = null;
            if (!map.getAreaType(areaOfBattle).isNaval()) {
                adjustVictoryPoints();
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
                        for (minLosses = 0; minLosses < attackingArmy.getNumUnits(); minLosses++) {
                            virtualAreasWithTroops.put(areaOfMarch, previousNumberOfUnits + attackingArmy.getNumUnits()
                                    - minLosses);
                            if (supplyTest(virtualAreasWithTroops, loser)) break;
                            virtualAreasWithTroops.put(areaOfMarch, previousNumberOfUnits);
                        }
                    }
                    System.out.println(MINIMAL_LOSSES + minLosses + ".");
                    if (minLosses > 0) {
                        attackingArmy.killSomeUnits(minLosses);
                    }
                    if (attackingArmy.getNumUnits() > 0) {
                        System.out.println(HOUSE[loser] + RETREATS_IN +
                                map.getAreaNameRusAccusative(areaOfMarch));
                        armyInArea[areaOfMarch].addSubArmy(attackingArmy);
                        addHouseTroopsInArea(areaOfMarch);
                    }
                }
            } else {
                attackingArmy.killAllUnits(KillingReason.noAreaToRetreat);
            }
        }
        attackingArmy = null;
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
                                    supportOfPlayer[player].getCode() == heroSide) {
                                accessibleAreaSet.add(adjacentArea);
                            }
                        }
                        if (accessibleAreaSet.size() > 0) {
                            System.out.println(RENLY_CAN_MAKE_KNIGHT);
                            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                                int area = playerInterface[player].areaToUseRenly(accessibleAreaSet);
                                // Если область меньше нуля, значит, игрок отказался использовать свойство карты
                                if (area < 0) {
                                    break;
                                }
                                if (area >= GameOfThronesMap.NUM_AREA) {
                                    System.out.println(INVALID_AREA_ERROR);
                                }
                                System.out.println(RENLY_MAKES_KNIGHT + map.getAreaNameRusLocative(area) + ".");
                                if (accessibleAreaSet.contains(area)) {
                                    boolean success = armyInArea[area].changeType(UnitType.pawn, UnitType.knight);
                                    if (success) {
                                        restingUnitsOfPlayerAndType[player][UnitType.pawn.getCode()]--;
                                        restingUnitsOfPlayerAndType[player][UnitType.knight.getCode()]++;
                                        propertyUsed = true;
                                    } else {
                                        System.out.println(CANT_CHANGE_UNIT_TYPE_ERROR);
                                    }
                                    break;
                                } else {
                                    System.out.println(INVALID_AREA_ERROR);
                                }
                            }
                        }
                        if (!propertyUsed) {
                            System.out.println(houseCardOfSide[heroSide] + NO_EFFECT);
                        }
                        break;
                    case cerseiLannister:
                        if (heroSide != winnerSide) break;
                        accessibleAreaSet.clear();
                        System.out.println(CERSEI_CAN_REMOVE_ANY_ORDER);
                        propertyUsed = false;
                        for (int area: areasWithTroopsOfPlayer.get(playerOnSide[1 - heroSide]).keySet()) {
                            if (orderInArea[area] != null) {
                                accessibleAreaSet.add(area);
                            }
                        }
                        if (accessibleAreaSet.size() > 0) {
                            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                                int area = playerInterface[player].chooseAreaCerseiLannister(accessibleAreaSet);
                                // Если область меньше нуля, значит, игрок отказался использовать свойство карты
                                if (area < 0) {
                                    break;
                                }
                                if (area >= GameOfThronesMap.NUM_AREA) {
                                    System.out.println(INVALID_AREA_ERROR);
                                }
                                System.out.println(CERSEI_REMOVES_ORDER + map.getAreaNameRusGenitive(area));
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
                                    break;
                                } else {
                                    System.out.println(INVALID_AREA_ERROR);
                                }
                            }
                        }
                        if (!propertyUsed) {
                            System.out.println(houseCardOfSide[heroSide] + NO_EFFECT);
                        }
                        break;
                    case tywinLannister:
                        if (heroSide != winnerSide) break;
                        nPowerTokensHouse[player] = Math.min(nPowerTokensHouse[player] + 2, maxPowerTokensHouse[player]);
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
                System.out.println(PATCHPACE_CAN_DELETE_ANY_CARD);
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
                        System.out.println(PATCHPACE_DELETES_CARD + houseCardOfPlayer[playerOnSide[1 - curSide]][card] + "\".");
                        break;
                    } else {
                        System.out.println(CARD_IS_NOT_ACTIVE_ERROR);
                    }
                }
                if (!propertyUsed) {
                    System.out.println(houseCardOfSide[curSide] + NO_EFFECT);
                }
            }
        }
    }

    private void renewHandExceptCard(int player, HouseCard card) {
        System.out.println(HOUSE[player] + GETS_NEW_DECK);
        for (int i = 0; i < NUM_HOUSE_CARDS; i++) {
            if (houseCardOfPlayer[player][i] != card) {
                houseCardOfPlayer[player][i].setActive(true);
                numActiveHouseCardsOfPlayer[player]++;
            }
        }
    }

    /**
     * Метод возвращает боевую силу игрока при его походе на определённую область определённой армией.
     * Считается поддержка игрока, но не считаются поддержки и защиты других игроков.
     * Нужен при пробивании нейтральных гарнизонов.
     * @param player        игрок
     * @param area          область, в которую собирается неумолимо надвигаться несокрушимая армада
     * @param army          армия из марширующих юнитов
     * @param marchModifier модификатор похода
     * @return итоговая боевая сила игрока
     */
    private int calculatePowerOfPlayerVersusGarrison(int player, int area, Army army, int marchModifier) {
        int strength = marchModifier;
        // Прибавляем силу атакующих юнитов
        for (Unit unit: army.getUnits()) {
            if (unit.getUnitType() != UnitType.siegeEngine || map.getNumCastle(area) > 0) {
                strength += unit.getStrength();
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
     * Метод возвращает боевую силу юнитов, атакующих область area
     * @param area атакуемая область
     * @return боевая сила атакующих юнитов
     */
    private int attackingUnitsStrength(int area) {
        int strength = 0;
        for (Unit unit: attackingArmy.getUnits()) {
            if (unit.getUnitType() != UnitType.siegeEngine || map.getNumCastle(area) > 0) {
                strength += unit.getStrength();
            }
        }
        return strength;
    }

    /**
     * Метод подсчитывает боевую силу войск в определённой области карты
     * @param area             область
     * @param isAttackOnCastle true, если войска атакуют или поддерживают атаку на замок/крепость
     * @return боевая сила войск в области
     */
    public int unitsStrengthInArea(int area, boolean isAttackOnCastle) {
        int strength = 0;
        for (Unit unit: armyInArea[area].getUnits()) {
            if (!unit.isWounded() && (unit.getUnitType() != UnitType.siegeEngine || isAttackOnCastle)) {
                strength += unit.getStrength();
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
        System.out.println(HOUSE[player] + PLAYS_HOUSE_CARD + chosenCard + "\".");
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

    /**
     * Метод рассчитывает стандартные переменные боя - сила карты, мечи, башни и бонусы к боевой силе из-за свойств карт
     * @param battleInfo информация о бое
     */
    private void countBattleVariables(BattleInfo battleInfo) {
        for (int curSide = 0; curSide < 2; curSide++) {
            cardStrengthOnSide[curSide] = houseCardOfSide[curSide] == null ? 0 : houseCardOfSide[curSide].getStrength();
            swordsOnSide[curSide] = houseCardOfSide[curSide] == null ? 0 : houseCardOfSide[curSide].getNumSwords();
            towersOnSide[curSide] = houseCardOfSide[curSide] == null ? 0 : houseCardOfSide[curSide].getNumTowers();
            bonusStrengthOnSide[curSide] = 0;
        }

        for (int side = 0; side < 2; side++) {
            if (houseCardOfSide[side] == null) continue;
            if (houseCardOfSide[side].getCardInitiative() == CardInitiative.bonus) {
                switch (houseCardOfSide[side]) {
                    case stannisBaratheon:
                        if (thronePlaceForPlayer[playerOnSide[side]] > thronePlaceForPlayer[playerOnSide[1 - side]]) {
                            bonusStrengthOnSide[side] = 1;
                        }
                        break;
                    case serDevosSeaworth:
                        if (!houseCardOfPlayer[0][0].isActive()) {
                            bonusStrengthOnSide[side] = 1;
                            swordsOnSide[side] = 1;
                        }
                        break;
                    case salladhorSaan:
                        if (numSupportsOnSide[side] > 0) {
                            bonusStrengthOnSide[side] -= battleInfo.getNumEnemyShips(playerOnSide[side],
                                    side == 0 ? SideOfBattle.attacker : SideOfBattle.defender);
                            bonusStrengthOnSide[1 - side] -= battleInfo.getNumEnemyShips(playerOnSide[side],
                                    side == 0 ? SideOfBattle.defender : SideOfBattle.attacker);
                        }
                        break;
                    case serKevanLannister:
                        if (side == 0) {
                            bonusStrengthOnSide[side] += battleInfo.getNumFriendlyUnits(SideOfBattle.attacker, UnitType.pawn);
                        }
                        break;
                    case catylynStark:
                        break;
                    case nymeriaSand:
                        if (side == 0) {
                            swordsOnSide[side]++;
                        } else {
                            towersOnSide[side]++;
                        }
                        break;
                    case ashaGreyjoy:
                        if (numSupportsOnSide[side] == 0) {
                            swordsOnSide[side] += 2;
                            towersOnSide[side]++;
                        }
                        break;
                    case theonGreyjoy:
                        if (side == 1 && map.getNumCastle(battleInfo.getAreaOfBattle()) > 0) {
                            bonusStrengthOnSide[side] = 1;
                            swordsOnSide[side] = 1;
                        }
                        break;
                    case victarionGreyjoy:
                        // Салладор Саан подавляет свойство Виктариона
                        if (houseCardOfSide[1 - side] == HouseCard.salladhorSaan) break;
                        if (side == 0) {
                            bonusStrengthOnSide[side] += battleInfo.getNumFriendlyUnits(SideOfBattle.attacker, UnitType.ship);
                        }
                        break;
                    case balonGreyjoy:
                        cardStrengthOnSide[1 - side] = 0;
                        break;
                }
            }
        }
        printRelationOfForces(battleInfo, true);
    }

    public void unitStoreIncreased(UnitType type, int player) {
        restingUnitsOfPlayerAndType[player][type.getCode()]++;
    }

    /**
     * Спускает игрока в самых конец по одному из треков влияния
     * @param player    опускаемый игрок
     * @param trackType трек влияния
     */
    private void pissOffOnTrack(int player, TrackType trackType) {
        int place;
        switch (trackType) {
            case ironThrone:
                place = thronePlaceForPlayer[player];
                for (int curPlace = place; curPlace < NUM_PLAYER - 1; curPlace++) {
                    thronePlayerOnPlace[curPlace] = thronePlayerOnPlace[curPlace + 1];
                }
                thronePlayerOnPlace[NUM_PLAYER - 1] = player;
                fillThronePlaceForPlayer();
                break;
            case valyrianSword:
                place = swordPlaceForPlayer[player];
                for (int curPlace = place; curPlace < NUM_PLAYER - 1; curPlace++) {
                    swordPlayerOnPlace[curPlace] = swordPlayerOnPlace[curPlace + 1];
                }
                swordPlayerOnPlace[NUM_PLAYER - 1] = player;
                fillSwordPlaceForPlayer();
                break;
            case raven:
                place = ravenPlaceForPlayer[player];
                for (int curPlace = place; curPlace < NUM_PLAYER - 1; curPlace++) {
                    ravenPlayerOnPlace[curPlace] = ravenPlayerOnPlace[curPlace + 1];
                }
                ravenPlayerOnPlace[NUM_PLAYER - 1] = player;
                fillRavenPlaceForPlayer();
                break;
        }
        printOrderOnTrack(trackType);
    }

    /**
     * Метод выводит порядок игроков на определённом треке влияния
     * @param trackType трек влияния
     */
    private void printOrderOnTrack(TrackType trackType) {
        System.out.print(NEW_ORDER_ON_TRACK + trackType + ": ");
        for (int place = 0; place < NUM_PLAYER; place++) {
            if (place != 0) {
                System.out.print(", ");
            }
            System.out.print(HOUSE[trackType == TrackType.ironThrone ? thronePlayerOnPlace[place] :
                    trackType == TrackType.valyrianSword ? swordPlayerOnPlace[place] : ravenPlayerOnPlace[place]]);
        }
        System.out.println();
    }

    /**
     * Выводит соотношение сил в сражении
     * @param battleInfo     информация о сражении
     * @param isAddStrengths нужно ли добавлять силы карт и бонусов карт
     */
    private void printRelationOfForces(BattleInfo battleInfo, boolean isAddStrengths) {
        System.out.println(RELATION_OF_FORCES_IS +
                (battleInfo.getAttackerStrength() + (isAddStrengths ? cardStrengthOnSide[0] + bonusStrengthOnSide[0] : 0)) +
                VERSUS +
                (battleInfo.getDefenderStrength() + (isAddStrengths ? cardStrengthOnSide[1] + bonusStrengthOnSide[1] : 0)) + ".");
    }

    /**
     * Метод инициализирует колоды событий, одичалых и домов
     */
    private void initializeDecks() {
        for (int i = 0; i < 3; i++) {
            deck1.add(Deck1Cards.muster);
            deck1.add(Deck1Cards.supply);
            deck2.add(Deck2Cards.clashOfKings);
            deck2.add(Deck2Cards.gameOfThrones);
            deck3.add(Deck3Cards.wildlingsAttack);
            if(i < 2) {
                deck1.add(Deck1Cards.throneOfSwords);
                deck2.add(Deck2Cards.darkWingsDarkWords);
                deck3.add(Deck3Cards.devotedToSword);
            }
        }
        deck1.add(Deck1Cards.lastDayOfSummer);
        deck1.add(Deck1Cards.winterIsComing);
        deck2.add(Deck2Cards.lastDayOfSummer);
        deck2.add(Deck2Cards.winterIsComing);
        deck3.add(Deck3Cards.seaOfStorms);
        deck3.add(Deck3Cards.rainOfAutumn);
        deck3.add(Deck3Cards.feastForCrows);
        deck3.add(Deck3Cards.webOfLies);
        deck3.add(Deck3Cards.stormOfSwords);
        wildlingDeck.addAll(Arrays.asList(WildlingCard.values()));
        int curIndexForPlayer[] = new int[NUM_PLAYER];
        for (HouseCard houseCard: HouseCard.values()) {
            int house = houseCard.house();
            if (house >= 0) {
                houseCardOfPlayer[house][curIndexForPlayer[house]] = houseCard;
                curIndexForPlayer[house]++;
            }
        }
        Collections.shuffle(deck1);
        Collections.shuffle(deck2);
        Collections.shuffle(deck3);
        Collections.shuffle(wildlingDeck);
    }

    /**
     * Метод вытаскивает три новых события при наступлении фазы Вестероса
     */
    private void chooseNewEvents() {

        if (event1 != null) {
            deck1.addLast(event1);
        }
        event1 = deck1.pollFirst();
        while (event1 == Deck1Cards.winterIsComing) {
            deck1.addLast(event1);
            Collections.shuffle(deck1);
            event1 = deck1.pollFirst();
        }
        if (event1.isWild()) {
            wildlingsStrength += WILDLING_STRENGTH_INCREMENT;
        }

        if (event2 != null) {
            deck2.addLast(event2);
        }
        event2 = deck2.pollFirst();
        while (event2 == Deck2Cards.winterIsComing) {
            deck2.addLast(event2);
            Collections.shuffle(deck2);
            event2 = deck2.pollFirst();
        }
        if (event2.isWild()) {
            wildlingsStrength += WILDLING_STRENGTH_INCREMENT;
        }

        if (event3 != null) {
            deck3.addLast(event3);
        }
        event3 = deck3.pollFirst();
        if (event3.isWild()) {
            wildlingsStrength += WILDLING_STRENGTH_INCREMENT;
        }
    }

    private void wildlingAttack() {
        if (topWildlingCard != null) {
            wildlingDeck.addLast(topWildlingCard);
        }
        topWildlingCard = wildlingDeck.pollFirst();
        System.out.println(WILDLINGS_ATTACK + WITH_STRENGTH + wildlingsStrength);
        if (random.nextBoolean()) {
            System.out.print(NIGHT_WATCH_VICTORY);
            wildlingsStrength = 0;
        } else {
            System.out.print(NIGHT_WATCH_DEFEAT);
            wildlingsStrength = Math.max(0, wildlingsStrength - 2 * WILDLING_STRENGTH_INCREMENT);
        }
        System.out.println(" Карта одичалых: " + topWildlingCard);
    }

    // тестовый метод, проверяющий, что прокрутка колод событий и одичалых идёт правильно.
    private void testDecks() {
        while (time < LAST_TURN) {
            time++;
            chooseNewEvents();
            System.out.println("Раунд № " + time + ". События - " + event1 + "; " + event2 + "; " + event3);
            if (wildlingsStrength >= MAX_WILDLING_STRENGTH) {
                wildlingsStrength = MAX_WILDLING_STRENGTH;
                wildlingAttack();
            }
            if (event3 == Deck3Cards.wildlingsAttack) {
                wildlingAttack();
            }
            System.out.println("Сила одичалых: " + wildlingsStrength + ". Колоды: ");
            System.out.println(deck1);
            System.out.println(deck2);
            System.out.println(deck3);
            System.out.println("***");
        }
    }

    private void nullifyOrdersAndVariables() {
        for (int area = 0; area < GameOfThronesMap.NUM_AREA; area++) {
            orderInArea[area] = null;
            armyInArea[area].healAllUnits();
        }
        prohibitedOrder = null;
        swordUsed = false;
    }

    public GameOfThronesMap getMap() {
        return map;
    }

    public ArrayList<HashMap<Integer, Integer>> getAreasWithTroopsOfPlayer() {
        return areasWithTroopsOfPlayer;
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

    /**
     * Метод возвращает владельца войск в данной области карты
     * @param area номер области
     * @return номер Дома, которому принадлежат войска, или -1, если войск нет
     */
    public int getTroopsOwner(int area) {
        return armyInArea[area].getOwner();
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

    /**
     * Метод возвращает место определённого игрока на определённом треке влияния
     * @param player    номер игрока
     * @param trackType трек влияния
     * @return место
     */
    public int getInfluenceTrackPlaceForPlayer(int player, TrackType trackType) {
        switch (trackType) {
            case ironThrone:
                return thronePlaceForPlayer[player];
            case valyrianSword:
                return swordPlaceForPlayer[player];
            case raven:
                return ravenPlaceForPlayer[player];
        }
        return -1;
    }

    /**
     * Метод возвращает игрока на определённом месте треке влияния
     * @param place     место на треке влияния
     * @param trackType трек влияния
     * @return место
     */
    public int getInfluenceTrackPlayerOnPlace(int place, TrackType trackType) {
        switch (trackType) {
            case ironThrone:
                return thronePlayerOnPlace[place];
            case valyrianSword:
                return thronePlayerOnPlace[place];
            case raven:
                return thronePlayerOnPlace[place];
        }
        return -1;
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
     * Метод выводит все области из множества под их русскими именами
     * @param areas  множество областей
     * @rapam text сопутствующий текст
     */
    public void printAreasInSet(HashSet<Integer> areas, String text) {
        System.out.print(text + ": ");
        boolean firstFlag = true;
        for (int area: areas) {
            firstFlag = LittleThings.printDelimiter(firstFlag);
            System.out.print(map.getAreaNameRus(area));
        }
        System.out.println();
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
        for (Map.Entry<Integer, Army> entry : march.getDestinationsOfMarch().entrySet()) {
            int curDestination = entry.getKey();
            Army curArmy = entry.getValue();
            int nLeavingUnits = curArmy.getNumUnits();
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
        return supplyTest(virtualAreasWithTroops, supply[player]);
    }

    /**
     * Метод проверяет, нарушается ли предел снабжения при данном расположении войск игрока
     * @param areasWithTroops карта с парами область-количество юнитов
     * @param supplyLevel     уровень снабжения игрока
     * @return true, если предел снабжения не нарушается
     */
    public boolean supplyTest(HashMap<Integer, Integer> areasWithTroops, int supplyLevel) {
        int[] nFreeGroups = new int[MAX_TROOPS_IN_AREA - 1];
        System.arraycopy(SUPPLY_NUM_GROUPS[supplyLevel], 0, nFreeGroups, 0, SUPPLY_NUM_GROUPS[supplyLevel].length);
        for (Map.Entry<Integer, Integer> entry: areasWithTroops.entrySet()) {
            int armySize = entry.getValue();
            // Если это армия, то занимаем свободную ячейку. Если это не удаётся сделать, значит предел снабжения превышен.
            if (armySize > 1) {
                boolean isThereFreeCell = false;
                for (int cellSize = armySize - 2; cellSize < MAX_TROOPS_IN_AREA - 1; cellSize++) {
                    if (nFreeGroups[cellSize] > 0) {
                        isThereFreeCell = true;
                        nFreeGroups[cellSize]--;
                        break;
                    }
                }
                if (!isThereFreeCell) {
                    return false;
                }
            }
        }
        return true;
    }

    private PropertyChangeSupport gamePhaseChangeSupport = new PropertyChangeSupport(this);

    private void addListener(PropertyChangeListener listener) {
        gamePhaseChangeSupport.addPropertyChangeListener(listener);
    }

    /*private void removeListener(PropertyChangeListener listener) {
        gamePhaseChangeSupport.removePropertyChangeListener(listener);
    }*/

    private void setNewGamePhase(GamePhase newValue) {
        GamePhase oldValue = gamePhase;
        gamePhase = newValue;
        gamePhaseChangeSupport.firePropertyChange("gamePhase", oldValue, newValue);
    }

    /**
     * Метод заполняет массив thronePlaceForPlayer. Должен вызываться каждый раз при изменении thronePlayerOnPlace.
     */
    private void fillThronePlaceForPlayer() {
        for (int place = 0; place < NUM_PLAYER; place++) {
            thronePlaceForPlayer[thronePlayerOnPlace[place]] = place;
        }
    }

    /**
     * Метод заполняет массив swordPlaceForPlayer. Должен вызываться каждый раз при изменении swordPlayerOnPlace.
     */
    private void fillSwordPlaceForPlayer() {
        for (int place = 0; place < NUM_PLAYER; place++) {
            swordPlaceForPlayer[swordPlayerOnPlace[place]] = place;
        }
    }

    /**
     * Метод заполняет массив ravenPlaceForPlayer. Должен вызываться каждый раз при изменении ravenPlayerOnPlace.
     */
    private void fillRavenPlaceForPlayer() {
        for (int place = 0; place < NUM_PLAYER; place++) {
            ravenPlaceForPlayer[ravenPlayerOnPlace[place]] = place;
        }
    }

    // Класс-слушатель, который реагирует на изменение состояния игры и вызывает необходимые методы
    private class GamePhaseChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            System.out.println(LINE_DELIMITER);
            //System.out.println("Состояние игры поменялось:" + event.getNewValue());

            if (gamePhase == GamePhase.westerosPhase) {
                if (time == LAST_TURN) {
                    gamePhase = GamePhase.end;
                } else {
                    time++;
                }
            }
            System.out.println(ROUND_NUMBER + time + ". " + (event.getNewValue()).toString());
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
                case westerosPhase:
                    nullifyOrdersAndVariables();
                    chooseNewEvents();
                    // playNewEvents();
                    break;
                default:
                    System.out.println("Ну блять, я уже не знаю, что делать!");
                    break;
            }
        }
    }
}
