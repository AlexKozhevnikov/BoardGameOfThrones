package com.alexeus.logic;

import com.alexeus.ai.GotPlayerInterface;
import com.alexeus.ai.PrimitivePlayer;
import com.alexeus.logic.enums.*;
import com.alexeus.logic.struct.Army;
import com.alexeus.logic.struct.MarchOrderPlayed;
import com.alexeus.logic.struct.RaidOrderPlayed;
import com.alexeus.logic.struct.Unit;
import com.alexeus.map.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import static com.alexeus.logic.Constants.HOUSE;
import static com.alexeus.logic.Constants.HOUSE_GENITIVE;
import static com.alexeus.logic.Constants.NUM_PLAYER;

/**
 * Created by alexeus on 03.01.2017.
 * Основной класс игры. Здесь написана вся логика партии и правила.
 */
public class Game {

    private LinkedList<Deck1Cards> deck1;

    private LinkedList<Deck2Cards> deck2;

    private LinkedList<Deck3Cards> deck3;

    private LinkedList<WildlingCard> wildlingDeck;

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
    // последняя сыгранная карта одичалых
    private WildlingCard topWildlingCard;

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
     * Максимальное число доступных в обозримом будущем жетонов власти. Отличается от Constants.MAX_TOKENS,
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

    /*
     * **** Вспомогательные переменные для методов ****
     */
    // Количество разыгранных приказов с определённым кодом; нужно для валидации приказов каждого игрока
    private int[] nOrdersWithCode = new int[Constants.NUM_DIFFERENT_ORDERS];

    /*
     * Список множеств, состоящих из областей, где есть войска данного игрока. Меняется в течении игры каждый раз, когда
     * войска игрока перемещаются, захватывают или отступают в новую область.
     */
    private ArrayList<HashSet<Integer>> areasWithTroopsOfPlayer;

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
            armyInArea[area] = new Army();
        }
    }

    /**
     * Метод вызывается при старте новой игры. Все переменные принимают начальные значения, текущий прогресс теряется.
     */
    public void startNewGame() {
        System.out.println(Constants.NEW_GAME_BEGINS);
        System.out.println(Constants.PLAYERS);
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
        for (int player = 0; player < Constants.NUM_PLAYER; player++) {
            areasWithTroopsOfPlayer.add(new HashSet<>());
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
            nPowerTokensHouse[player] = Constants.INITIAL_TOKENS;
            maxPowerTokensHouse[player] = Constants.MAX_TOKENS;
        }
        // Устанавливаем начальные позиции на треках влияния
        for (int place = 0; place < NUM_PLAYER; place++) {
            thronePlayerOnPlace[place] = Constants.INITIAL_INFLUENCE_TRACKS_PLAYER_ON_PLACE[0][place];
            swordPlayerOnPlace[place] = Constants.INITIAL_INFLUENCE_TRACKS_PLAYER_ON_PLACE[1][place];
            ravenPlayerOnPlace[place] = Constants.INITIAL_INFLUENCE_TRACKS_PLAYER_ON_PLACE[2][place];
        }
        fillThronePlaceForPlayer();
        fillSwordPlaceForPlayer();
        fillRavenPlaceForPlayer();

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
        for (int player = 0; player < Constants.NUM_PLAYER; player++) {
            areasWithTroopsOfPlayer.get(player).clear();
        }
        for (int area = 0; area < GameOfThronesMap.NUM_AREA; area++) {
            int troopsOwner = armyInArea[area].getOwner();
            if(troopsOwner >= 0) {
                areasWithTroopsOfPlayer.get(troopsOwner).add(area);
            }
        }
        System.out.println("areasWithTroopsOfPlayer: " + areasWithTroopsOfPlayer);
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
                supply[areaOwner] = Math.min(supply[areaOwner] + map.getNumBarrel(area), Constants.MAX_SUPPLY);
            }
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            System.out.println(Constants.SUPPLY_OF + Constants.HOUSE_GENITIVE[player] + Constants.EQUALS + supply[player] + ".");
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
            System.out.println("У " + Constants.HOUSE_GENITIVE[player] + " " + victoryPoints[player] +
                    (victoryPoints[player] == 1 ? Constants.OF_CASTLE :
                    victoryPoints[player] > 1 && victoryPoints[player] < 5 ? Constants.OF_CASTLA : Constants.OF_CASTLES) + ".");
        }
    }

    private void getPlans() {
        HashMap<Integer, Order> orders;
        Order curOrder;
        for (int player = 0; player < NUM_PLAYER; player++) {
            for (int attempt = 0; attempt < Constants.MAX_TRIES_TO_GO; attempt++) {
                System.out.println();
                orders = playerInterface[player].giveOrders();
                System.out.println(Constants.ORDERS + Constants.HOUSE_GENITIVE[player] + ":");
                for (Map.Entry<Integer, Order> entry : orders.entrySet()) {
                    System.out.println(Constants.AREA_NUMBER + entry.getKey() +
                            " (" + map.getAreaNameRus(entry.getKey()) + ")" + Constants.COLON + entry.getValue());
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
        for (int orderCode = 0; orderCode < Constants.NUM_DIFFERENT_ORDERS; orderCode++) {
            nOrdersWithCode[orderCode] = 0;
        }
        // Проверяем каждый приказ отдельно
        for (Map.Entry<Integer, Order> entry : orders.entrySet()) {
            // Сперва проверяем, что в заявленной области действительно есть войска данного игрока
            if (!areasWithTroopsOfPlayer.get(player).contains(entry.getKey())) {
                if (!alright) stringBuffer.append("\n");
                stringBuffer.append(Constants.IN_AREA_NUMBER).append(entry.getKey())
                        .append(Constants.NO_TROOPS_OF).append(Constants.HOUSE_GENITIVE[player]);
                alright = false;
                continue;
            }

            nOrdersWithCode[entry.getValue().getCode()]++;
            if (entry.getValue().isStar()) {
                nStarOrders++;
            }

            if (map.getAreaType(entry.getKey()) == AreaType.sea && entry.getValue().orderType() == OrderType.consolidatePower) {
                if (!alright) stringBuffer.append("\n");
                stringBuffer.append(Constants.AREA_NUMBER).append(entry.getKey()).append(Constants.NO_CP_IN_SEA);
                alright = false;
            }
            if (map.getAreaType(entry.getKey()) == AreaType.port && entry.getValue().orderType() == OrderType.defence) {
                if (!alright) stringBuffer.append("\n");
                stringBuffer.append(Constants.AREA_NUMBER).append(entry.getKey()).append(Constants.NO_DEFENCE_IN_PORT);
                alright = false;
            }
        }
        // Проверяем, что количество приказов любого типа не превышает максимального возможного числа таких приказов
        for (int orderCode = 0; orderCode < Constants.NUM_DIFFERENT_ORDERS; orderCode++) {
            if (nOrdersWithCode[orderCode] > Order.maxNumOrdersWithCode[orderCode]) {
                if (!alright) stringBuffer.append("\n");
                stringBuffer.append(nOrdersWithCode[orderCode]).append(Constants.OF_ORDERS).append(Order.getOrderWithCode(orderCode))
                        .append(Constants.WHEN_MAX_OF_SUCH_ORDERS_IS).append(Order.maxNumOrdersWithCode[orderCode]);
                alright = false;
            }
        }
        // Проверяем, что соблюдено ограничение по звёздам
        int numStars = Constants.NUM_OF_STARS_ON_PLACE[ravenPlaceForPlayer[player]];
        if (nStarOrders > numStars) {
            if (!alright) stringBuffer.append("\n");
            stringBuffer.append(Constants.STAR_NUMBER_VIOLENCE).append(numStars).append(Constants.OF_STAR_ORDERS).append(nStarOrders);
            alright = false;
        }

        if (!alright) {
            stringBuffer.insert(0, Constants.ORDER_MISTAKES + Constants.HOUSE_GENITIVE[player] + ":\n");
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
        for (int attempt = 0; attempt < Constants.MAX_TRIES_TO_GO; attempt++) {
            String ravenUse = playerInterface[ravenHolder].useRaven();
            // Ворононосец выбрал просмотр карты одичалых
            if (ravenUse.equals(Constants.RAVEN_SEES_WILDLINGS_CODE)) {
                System.out.println(Constants.HOUSE[ravenHolder] + Constants.SEES_WILDLINGS_CARD);
                if (!playerInterface[ravenHolder].leaveWildlingCardOnTop(wildlingDeck.getFirst())) {
                    WildlingCard card = wildlingDeck.pollFirst();
                    wildlingDeck.addLast(card);
                    System.out.println(Constants.AND_BURIES);
                } else {
                    System.out.println(Constants.AND_LEAVES);
                }
                break;
            }
            // Ворононосец выбрал замену приказа
            if (ravenUse.charAt(0) >= '0' && ravenUse.charAt(0) <= '9') {
                System.out.println(Constants.HOUSE[ravenHolder] + Constants.CHANGES_ORDER);
                String[] s = ravenUse.split(" ");
                if (s.length != 2) {
                    System.out.println(Constants.RAVEN_CHANGE_ORDER_FORMAT_ERROR);
                    continue;
                }
                try {
                    int area = Integer.valueOf(s[0]);
                    int newOrderCode = Integer.valueOf(s[1]);
                    // TODO реализовать ёбаный кусок кода
                    System.out.println("ОШИБКА! НЕРЕАЛИЗОВАННЫЙ КУСОК КОДА!");
                    break;
                } catch (NumberFormatException ex) {
                    System.out.println(Constants.RAVEN_CHANGE_ORDER_PARSE_ERROR);
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
                for (attempt = 0; attempt < Constants.MAX_TRIES_TO_GO; attempt++) {
                    RaidOrderPlayed raid = playerInterface[player].playRaid();
                    // Проверяем вариант розыгрыша набега, полученный от игрока, на валидность
                    if (validateRaid(raid, player)) {
                        System.out.print(Constants.HOUSE[player]);
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
                            System.out.println(Constants.DELETES_RAID_FROM + map.getAreaNameRus(from));
                            orderInArea[from] = null;
                            areasWithRaids.get(player).remove(from);
                            break;
                        }
                        break;
                    }
                }
                // Если игрок не уложился в количество попыток, то удаляем все его приказы набегов
                if (attempt >= Constants.MAX_TRIES_TO_GO) {
                    System.out.println(HOUSE[player] + Constants.FAILED_TO_PLAY_RAID);
                    for (int area: areasWithRaids.get(player)) {
                        orderInArea[area] = null;
                    }
                    areasWithRaids.get(player).clear();
                }
            }
            place++;
            if (place == NUM_PLAYER) place = 0;
        }
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
            System.out.println(Constants.WRONG_AREAS_RAID_ERROR);
            return false;
        }
        if (orderInArea[from] == null || orderInArea[from].orderType() != OrderType.raid ||
                armyInArea[from].getOwner() != player) {
            System.out.println(Constants.NO_RAID_ERROR);
            return false;
        }

        // Случай результативного набега
        if (to >= 0 && to < GameOfThronesMap.NUM_AREA && to != from) {
            System.out.println(Constants.RAIDS_FROM + map.getAreaNameRus(from) +
                    Constants.RAIDS_TO + map.getAreaNameRus(to));
            if (map.getAdjacencyType(from, to) == AdjacencyType.noAdjacency) {
                System.out.println(Constants.NO_ADJACENT_RAID_ERROR);
                return false;
            }
            if (armyInArea[to].getOwner() == player) {
                System.out.println(Constants.DONT_RAID_YOURSELF_ERROR);
                return false;
            }
            if (armyInArea[to].getOwner() < 0 || orderInArea[to] == null) {
                System.out.println(Constants.NO_ONE_TO_RAID_THERE_ERROR);
                return false;
            }
            if (map.getAdjacencyType(from, to) == AdjacencyType.landToSea) {
                System.out.println(Constants.NO_RAID_FROM_LAND_TO_SEA_ERROR);
                return false;
            }
            if (orderInArea[to].orderType() == OrderType.march ||
                    orderInArea[to].orderType() == OrderType.defence && orderInArea[from] == Order.raid) {
                System.out.println(Constants.CANT_RAID_THIS_ORDER_ERROR);
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
        while(numNoMarchesPlayers < NUM_PLAYER){
            player = thronePlayerOnPlace[place];
            if (areasWithMarches.get(player).isEmpty()) {
                numNoMarchesPlayers++;
            } else {
                numNoMarchesPlayers = 0;
                for (attempt = 0; attempt < Constants.MAX_TRIES_TO_GO; attempt++) {
                    MarchOrderPlayed march = playerInterface[player].playMarch();
                    // Проверяем вариант розыгрыша похода, полученный от игрока, на валидность
                    if (validateMarch(march, player)) {
                        System.out.print(Constants.HOUSE[player]);
                        int from = march.getAreaFrom();
                        HashMap<Integer, Army> destinationsOfMarch = march.getDestinationsOfMarch();
                        if (destinationsOfMarch.size() == 0) {
                            // Случай "холостого" снятия приказа похода
                            System.out.println(Constants.DELETES_MARCH_FROM + map.getAreaNameRus(from));
                            orderInArea[from] = null;
                            areasWithMarches.get(player).remove(from);
                        } else {
                            // Случай результативного похода: перемещаем юниты, если нужно, начинаем бой,
                            // и обновляем попутные переменные
                            System.out.println(Constants.PLAYS_MARCH_FROM + map.getAreaNameRus(from));
                            int areaWhereBattleBegins = -1;
                            for (Map.Entry<Integer, Army> entry: destinationsOfMarch.entrySet()) {
                                int destinationArmyOwner = armyInArea[entry.getKey()].getOwner();
                                if (destinationArmyOwner >= 0 && destinationArmyOwner != player)
                                {
                                    // Если в области завязывается бой, то запоминаем её и проходим последней
                                    areaWhereBattleBegins = entry.getKey();
                                    attackingArmy = entry.getValue();
                                } else {
                                    // Иначе перемещаем войска и пробиваем гарнизоны
                                    System.out.print(entry.getValue().toString() +
                                            (entry.getValue().getSize() == 1 ? Constants.MOVES_TO : Constants.MOVE_TO) +
                                            map.getAreaNameRus(entry.getKey()));
                                    armyInArea[entry.getKey()].addSubArmy(entry.getValue());
                                    // Если в области имелся нейтральный гарнизон, то пробиваем его
                                    if (destinationArmyOwner < 0 && garrisonInArea[entry.getKey()] > 0) {
                                        System.out.println(Constants.GARRISON_IS_DEFEATED + HOUSE_GENITIVE[player] + " - " +
                                                calculatePowerOfPlayer(player, entry.getKey(), entry.getValue(),
                                                orderInArea[entry.getKey()].getModifier()) + Constants.GARRISON_STRENGTH_IS +
                                                garrisonInArea[entry.getKey()]);
                                        garrisonInArea[entry.getKey()] = 0;
                                    } else {
                                        System.out.println();
                                    }
                                }
                                armyInArea[from].deleteSubArmy(entry.getValue());
                            }
                            if (armyInArea[from].isEmpty() && houseHomeLandInArea[from] < 0) {
                                if (march.isLeaveToken()) {
                                    powerTokenOnArea[from] = player;
                                    nPowerTokensHouse[player]--;
                                    maxPowerTokensHouse[player]--;
                                } else {
                                    if (map.getNumCastle(from) > 0) {
                                        victoryPoints[player]--;
                                    }
                                }
                            }
                            renewHousesTroopsArea();
                            if (areaWhereBattleBegins >= 0) {
                                System.out.print(attackingArmy.toString() +
                                        (attackingArmy.getSize() == 1 ? Constants.MOVES_TO : Constants.MOVE_TO) +
                                        map.getAreaNameRus(areaWhereBattleBegins) + Constants.AND_FIGHTS);
                                playFight(areaWhereBattleBegins);
                            }
                        }
                    }
                }
                // Если игрок не уложился в количество попыток, то удаляем один его приказ похода
                if (attempt >= Constants.MAX_TRIES_TO_GO) {
                    System.out.println(HOUSE[player] + Constants.FAILED_TO_PLAY_MARCH);
                    for (int area: areasWithMarches.get(player)) {
                        orderInArea[area] = null;
                        areasWithMarches.get(player).remove(area);
                    }
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
            System.out.println(Constants.WRONG_AREAS_MARCH_ERROR);
            return false;
        }
        if (orderInArea[from] == null || orderInArea[from].orderType() != OrderType.march ||
                armyInArea[from].getOwner() != player) {
            System.out.println(Constants.NO_MARCH_ERROR);
            return false;
        }

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
                accessibleAreas = getAccessibleAreas(from);
            }
            for (Map.Entry<Integer, Army> entry : destinationsOfMarch.entrySet()) {
                // Проверяем, что в данную область назначения действительно можно пойти
                if (map.getAreaType(from).isNaval() && !map.getAreaType(entry.getKey()).isNaval()) {
                    System.out.println(Constants.CANT_MARCH_FROM_SEA_TO_LAND_ERROR);
                    return false;
                }
                if (map.getAreaType(from).isNaval() && map.getAdjacencyType(from, entry.getKey()) == AdjacencyType.noAdjacency) {
                    System.out.println(Constants.CANT_MARCH_THERE_ERROR);
                    return false;
                }
                if (map.getAreaType(from).isNaval() && map.getAreaType(entry.getKey()) == AreaType.port) {
                    int castleWithThisPort = map.getAdjacentAreas(entry.getKey()).get(1);
                    // Если замок, которому принадлежит порт, в который хочет зайти игрок, ему не принадлежит, то фейл
                    if (getAreaOwner(castleWithThisPort) != player) {
                        System.out.println(Constants.CANT_MARCH_IN_NOT_YOUR_PORT_ERROR);
                        return false;
                    }
                }
                if (!map.getAreaType(from).isNaval() && map.getAreaType(entry.getKey()).isNaval()) {
                    System.out.println(Constants.CANT_MARCH_FROM_LAND_TO_SEA_ERROR);
                    return false;
                }
                if (!map.getAreaType(from).isNaval() && !accessibleAreas.contains(entry.getKey())) {
                    System.out.println(Constants.NO_WAY_MARCH_ERROR);
                    return false;
                }
                // Проверяем, что марширующая армия не пуста
                if (entry.getValue().isEmpty()) {
                    System.out.println(Constants.EMPTY_ARMY_MARCH_ERROR);
                    return false;
                }

                // Считаем, начнётся ли битва в области назначения (пробивание нейтрального гарнизона тоже считается)
                int destinationArmyOwner = armyInArea[entry.getKey()].getOwner();
                if (destinationArmyOwner >= 0 && destinationArmyOwner != player ||
                        destinationArmyOwner < 0 && garrisonInArea[entry.getKey()] > 0) {
                    if (areaWhereBattleBegins < 0) {
                        if (destinationArmyOwner < 0 && garrisonInArea[entry.getKey()] > 0) {
                            if (calculatePowerOfPlayer(player, entry.getKey(), entry.getValue(),
                                    orderInArea[from].getModifier()) < garrisonInArea[entry.getKey()]) {
                                System.out.println(Constants.CANT_BEAT_NEUTRAL_GARRISON_ERROR);
                                return false;
                            }
                        }
                        areaWhereBattleBegins = entry.getKey();
                    } else {
                        System.out.println(Constants.CANT_BEGIN_TWO_BATTLES_BY_ONE_MARCH_ERROR);
                        return false;
                    }
                }

                // Проверки данной области назначения пройдены. Теперь учитываем юниты, перемещающиеся в эту область
                for (Unit unit : entry.getValue().getUnits()) {
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
                System.out.println(Constants.LACK_OF_UNITS_ERROR);
                return false;
            }
            // Проверка соблюдения ограничения по снабжению
            // TODO Написать часть, связанную со снабжением

            if (march.isLeaveToken() && nPowerTokensHouse[player] == 0) {
                System.out.println(Constants.NO_POWER_TOKENS_TO_LEAVE_ERROR);
                return false;
            }
        }
        return true;
    }

    /**
     * Метод формирует и возвращает множество областей, в которые может пойти игрок из данной области с учётом
     * правила переброски морем
     * @param areaFrom область похода
     * @return множество с доступными для похода областями
     */
    public HashSet<Integer> getAccessibleAreas(int areaFrom) {
        // Для морских территорий этот метод бессмысленен
        if (map.getAreaType(areaFrom).isNaval()) {
            return null;
        } else {
            HashSet<Integer> accessibleAreas = new HashSet<>();
            HashSet<Integer> seas = new HashSet<>();
            for (int area: map.getAdjacentAreas(areaFrom)) {
                if (map.getAreaType(area) == AreaType.sea) {
                    seas.add(area);
                } else if (map.getAreaType(area) == AreaType.land) {
                    accessibleAreas.add(area);
                }
            }
            for (Integer sea : seas) {
                for (int area : map.getAdjacentAreas(sea)) {
                    if (map.getAreaType(area) == AreaType.sea && !seas.contains(area)) {
                        seas.add(area);
                    } else if (map.getAreaType(area) == AreaType.land) {
                        accessibleAreas.add(area);
                    }
                }
            }

            System.out.print("Доступные для похода из " + map.getAreaNameRus(areaFrom) + " области: ");
            boolean firstFlag = true;
            for (int area: accessibleAreas) {
                if (firstFlag) {
                    firstFlag = false;
                } else {
                    System.out.print(", ");
                }
                System.out.print(map.getAreaNameRus(area));
            }
            return accessibleAreas;
        }
    }

    /**
     * Метод разыгрывает одно сражение за определённую область. На данный момент атакующая армия должна быть сохранена
     * в attackingArmy, а нападающий игрок может быть определён посредоством attackingArmy.getOwner();
     * @param areaOfBattle область, в которой происходит сражение
     */
    private void playFight(int areaOfBattle) {

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
    public int calculatePowerOfPlayer(int player, int area, Army army, int marchModifier) {
        int strength = marchModifier;
        // Прибавляем силу атакующих юнитов
        for (Unit unit: army.getUnits()) {
            if (unit.getUnitType() != UnitType.siegeEngine || map.getNumCastle(area) > 0) {
                strength += unit.getStrength();
            }
        }
        // Прибавляем силу поддеривающих юнитов
        for (int adjacentArea: map.getAdjacentAreas(area)) {
            if (getTroopsOwner(adjacentArea) == player && orderInArea[adjacentArea].orderType() == OrderType.support) {
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
     * Метод инициализирует колоды событий и одичалых
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
            wildlingsStrength += Constants.WILDLING_STRENGTH_INCREMENT;
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
            wildlingsStrength += Constants.WILDLING_STRENGTH_INCREMENT;
        }

        if (event3 != null) {
            deck3.addLast(event3);
        }
        event3 = deck3.pollFirst();
        if (event3.isWild()) {
            wildlingsStrength += Constants.WILDLING_STRENGTH_INCREMENT;
        }
    }

    private void wildlingAttack() {
        if (topWildlingCard != null) {
            wildlingDeck.addLast(topWildlingCard);
        }
        topWildlingCard = wildlingDeck.pollFirst();
        System.out.println(Constants.WILDLINGS_ATTACK + Constants.WITH_STRENGTH + wildlingsStrength);
        if (random.nextBoolean()) {
            System.out.print(Constants.NIGHT_WATCH_VICTORY);
            wildlingsStrength = 0;
        } else {
            System.out.print(Constants.NIGHT_WATCH_DEFEAT);
            wildlingsStrength = Math.max(0, wildlingsStrength - 2 * Constants.WILDLING_STRENGTH_INCREMENT);
        }
        System.out.println(" Карта одичалых: " + topWildlingCard);
    }

    // тестовый метод, проверяющий, что прокрутка колод событий и одичалых идёт правильно.
    private void testDecks() {
        while (time < Constants.LAST_TURN) {
            time++;
            chooseNewEvents();
            System.out.println("Раунд № " + time + ". События - " + event1 + "; " + event2 + "; " + event3);
            if (wildlingsStrength >= Constants.MAX_WILDLING_STRENGTH) {
                wildlingsStrength = Constants.MAX_WILDLING_STRENGTH;
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

    private void nullifyOrders() {
        for (int area = 0; area < GameOfThronesMap.NUM_AREA; area++) {
            orderInArea[area] = null;
        }
    }

    public GameOfThronesMap getMap() {
        return map;
    }

    public ArrayList<HashSet<Integer>> getAreasWithTroopsOfPlayer() {
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
            System.out.println(Constants.LINE_DELIMITER);
            //System.out.println("Состояние игры поменялось:" + event.getNewValue());

            if (gamePhase == GamePhase.westerosPhase) {
                if (time == Constants.LAST_TURN) {
                    gamePhase = GamePhase.end;
                } else {
                    time++;
                }
            }
            System.out.println(Constants.ROUND_NUMBER + time + ". " + (event.getNewValue()).toString());
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
                    nullifyOrders();
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
