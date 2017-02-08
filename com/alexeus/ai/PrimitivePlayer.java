package com.alexeus.ai;

import com.alexeus.ai.struct.AreaStatus;
import com.alexeus.ai.struct.DummyArmy;
import com.alexeus.ai.struct.OrderScheme;
import com.alexeus.ai.struct.VotesForOrderInArea;
import com.alexeus.ai.util.PlayerUtils;
import com.alexeus.logic.Game;
import com.alexeus.logic.GameModel;
import com.alexeus.logic.GameUtils;
import com.alexeus.logic.enums.*;
import com.alexeus.logic.struct.*;
import com.alexeus.map.GameOfThronesMap;
import com.alexeus.util.LittleThings;

import java.util.*;

import static com.alexeus.logic.constants.MainConstants.*;

/**
 * Created by alexeus on 03.01.2017.
 * Тупой, незамысловатый компьютерный игрок, который ничего не знает.
 * Может использоваться для заглушек и тестов класса Game.
 */
public class PrimitivePlayer implements GotPlayerInterface{

    protected Random random;

    protected GameModel model;

    protected GameOfThronesMap map;

    protected final int me;

    // здесь будут храниться области, в которых есть войска игрока
    protected Set<Integer> areasWithTroopsOfPlayer;

    // здесь будут храниться области, в которых есть набеги игрока
    protected ArrayList<HashSet<Integer>> areasWithRaidsOfPlayer;

    // здесь будут храниться области, в которых есть походы игрока
    protected ArrayList<HashSet<Integer>> areasWithMarchesOfPlayer;

    // здесь будут храниться области c войсками игрока, фасованные по статусам
    protected HashMap<AreaStatus, ArrayList<Integer>> areasWithStatus;

    // здесь будут храниться количества доступных приказов игрока в текущем раунде
    protected HashMap<OrderType, Integer> numOfOrderTypes;

    // здесь будут храниться приказы, которые игрок отдаёт в фазе планирования
    protected HashMap<Integer, Order> orderMap;

    protected PlayerUtils playerUtils;

    protected VotesForOrderInArea voteInAreaForOrder;

    protected Map<Integer, DummyArmy> armyInArea;

    protected OrderType forbiddenOrder;

    /**
     * Список карт одичалых, как его видит игрок. Если на определённом месте находится null, значит, игрок не знает,
     * что это за карта одичалых
     */
    protected LinkedList<WildlingCard> listOfWildlingCards;

    private float raidPrice = 2, cpPrice = 4, defencePrice = 1, supportPrice = 1, filledSeaPrice = 7, musterPrice = 7;

    /**
     * Вспомогательный массив индексов, нужный для генерации множества возможных походов
     */
    protected int[] indexes = new int[MAX_TROOPS_IN_AREA];

    /**
     * Вспомогательный массив, в котором хранятся индексы предыдущего юнита определённого типа.
     * Нужен для генерации множества возможных походов.
     */
    protected int[] indexOfLastSuchUnit = new int[NUM_UNIT_TYPES];

    /**
     * Вспомогательный массив, в котором хранится индекс для данного индекса с таким же типом юнита.
     * Нужен для генерации множества возможных походов
     */
    protected int[] indexToAlign = new int[MAX_TROOPS_IN_AREA];

    public PrimitivePlayer(Game newGame, int me) {
        model = newGame.getModel();
        random = new Random();
        map = model.getMap();
        this.me = me;
        orderMap = new HashMap<>();
        areasWithStatus = new HashMap<>();
        numOfOrderTypes = new HashMap<>();
        listOfWildlingCards = new LinkedList<>();
        for (int i = 0; i < WildlingCard.values().length; i++) {
            listOfWildlingCards.add(null);
        }
        for (AreaStatus areaStatus: AreaStatus.values()) {
            areasWithStatus.put(areaStatus, new ArrayList<>());
        }
        playerUtils = PlayerUtils.getInstance();
    }

    @Override
    public void nameYourself() {
        say("Я - Примитивный игрок за " + HOUSE_GENITIVE[me] + "!");
    }

    @Override
    public HashMap<Integer, Order> giveOrders() {
        areasWithTroopsOfPlayer = model.getAreasWithTroopsOfPlayer(me);
        Map<Integer, Integer> troopsNumberMap = model.getAreasWithTroopsOfPlayerAndSize(me);
        armyInArea = playerUtils.makeDummyArmies(me);

        // Множество с областями игрока, из которого мы будем удалять области, когда им уже отданы приказы
        Set<Integer> areasToCommand = new HashSet<>();
        areasToCommand.addAll(areasWithTroopsOfPlayer);
        int numFightsCanBegin;
        int numStars = model.getNumStars(me);
        int numRestingStars = numStars;
        forbiddenOrder = model.getForbiddenOrder();
        numOfOrderTypes.clear();
        for (OrderType type: OrderType.values()) {
            int n = 0;
            if (forbiddenOrder == type) {
                if (type == OrderType.march) {
                    n = 2;
                }
            } else {
                n = (numStars > 0 ? 3 : 2);
            }
            numOfOrderTypes.put(type, n);
        }
        orderMap.clear();
        for (AreaStatus areaStatus: AreaStatus.values()) {
            areasWithStatus.get(areaStatus).clear();
        }
        for (int area: areasWithTroopsOfPlayer) {
            AreaStatus status = null;
            switch (map.getAreaType(area)) {
                case port:
                    int seaOwner = model.getTroopsOwner(map.getSeaNearPort(area));
                    if (seaOwner == me) {
                        status = AreaStatus.myPort;
                    } else if (seaOwner < 0) {
                        status = AreaStatus.freePort;
                    } else {
                        status = AreaStatus.invadedPort;
                    }
                    break;
                case sea:
                    numFightsCanBegin = playerUtils.getNumPassiveFightsInArea(area);
                    if (numFightsCanBegin > 0) {
                        status = AreaStatus.outerSea;
                    } else {
                        for (int adjArea: map.getAdjacentAreas(area)) {
                            if (map.getAreaType(adjArea) == AreaType.sea && model.getAreaOwner(adjArea) < 0) {
                                status = AreaStatus.hopefulSea;
                                break;
                            }
                        }
                        if (status == null) {
                            status = AreaStatus.innerSea;
                        }
                    }
                    break;
                case land:
                    numFightsCanBegin = playerUtils.getNumPassiveFightsInArea(area);
                    status = numFightsCanBegin == 0 ? (map.getNumCastle(area) > 0 ? AreaStatus.musterCastle :
                            AreaStatus.innerLand) : AreaStatus.borderLand;
                    break;
            }
            areasWithStatus.get(status).add(area);
        }

        // Добавляем голоса за приказы (кроме подмоги) в зависимости от статуса области; подмоги добавляются позже.
        voteInAreaForOrder = new VotesForOrderInArea();
        for (AreaStatus status: AreaStatus.values()) {
            for (int area : areasWithStatus.get(status)) {
                voteInAreaForOrder.addArea(area);
                int armySize = model.getArmyInArea(area).getSize();
                switch (status) {
                    case myPort:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.consolidatePower, cpPrice);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march,
                                armySize * armySize * armySize * armySize);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.support, armySize *
                                playerUtils.getNumPassiveFightsInArea(area) * supportPrice);
                        break;
                    case invadedPort:
                        int enemyArmySize = model.getArmyInArea(map.getSeaNearPort(area)).getSize();
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.raid,
                                forbiddenOrder == OrderType.support ? 1 : enemyArmySize * raidPrice);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, 2 * armySize * armySize * armySize);
                        break;
                    case freePort:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.consolidatePower, cpPrice);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march,
                                filledSeaPrice + armySize * armySize * armySize * armySize);
                        break;
                    case innerSea:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, armySize);
                        break;
                    case hopefulSea:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, 5 * armySize * armySize);
                        break;
                    case outerSea:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.raid, 2 *
                                playerUtils.getNumAreasToRaidFrom(area));
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, armySize * armySize *
                                2 * (forbiddenOrder == OrderType.support ? 2 : 1));
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.defence, (4.5f - armySize) *
                                defencePrice * playerUtils.getNumPassiveFightsInArea(area) *
                                (forbiddenOrder == OrderType.support ? 2 : 1));
                        break;
                    case musterCastle:
                        int biggestFreeSupplyGroup =
                                GameUtils.getBiggestFreeArmySize(troopsNumberMap, model.getSupply(me));
                        if (biggestFreeSupplyGroup <= armySize) {
                            voteInAreaForOrder.setOrderVotesInArea(area, Order.consolidatePowerS, 0);
                        } else {
                            voteInAreaForOrder.setOrderVotesInArea(area, Order.consolidatePowerS,
                                    map.getNumCastle(area) * musterPrice);
                        }
                        // Отсутствие break - не ошибка. Замки со сбором власти также попадают в категорию innerLand
                    case innerLand:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.consolidatePower,
                                (1 + map.getNumCrown(area)) * cpPrice);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march,
                                2 * armySize * armyInArea.get(area).getStrength(true));
                        break;
                    case borderLand:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.raid, 2 *
                                playerUtils.getNumAreasToRaidFrom(area));
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, 2 * armySize *
                                armyInArea.get(area).getStrength(true));
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.consolidatePower,
                                (1 + map.getNumCrown(area)) * cpPrice /
                                (forbiddenOrder == OrderType.raid ? 1 : raidKoef(area)));
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.defence,
                                playerUtils.getNumPassiveFightsInArea(area) * defencePrice * (4.5f - armySize));
                        break;
                }
            }
        }
        fillSupportVotes(null);

        if (forbiddenOrder != null && forbiddenOrder != OrderType.march) {
            voteInAreaForOrder.forbidOrder(forbiddenOrder.mainVariant());
        }
        if (numStars == 0) {
            voteInAreaForOrder.forbidOrder(Order.consolidatePowerS);
        }

        // Заполняем схему приказов
        OrderScheme orderScheme = new OrderScheme();
        // Сбор войск - превыше всего
        if (numStars > 0 && numOfOrderTypes.get(OrderType.consolidatePower) > 0) {
            float numVotesForMuster = 0;
            float probabilityOfMuster = 0;
            float vote;
            for (int area : areasWithStatus.get(AreaStatus.musterCastle)) {
                vote = voteInAreaForOrder.getOrderVotesInArea(area, Order.consolidatePowerS);
                numVotesForMuster += vote;
                probabilityOfMuster += (1 - probabilityOfMuster) * (vote / voteInAreaForOrder.getTotalVotesInArea(area));
            }
            if (random.nextFloat() < probabilityOfMuster) {
                float trueVote = random.nextFloat() * numVotesForMuster;
                numVotesForMuster = 0;
                for (int area : areasWithStatus.get(AreaStatus.musterCastle)) {
                    vote = voteInAreaForOrder.getOrderVotesInArea(area, Order.consolidatePowerS);
                    numVotesForMuster += vote;
                    if (numVotesForMuster >= trueVote) {
                        orderScheme.addOrderInArea(area, Order.consolidatePowerS);
                        areasToCommand.remove(area);
                        numOfOrderTypes.put(OrderType.consolidatePower,
                                numOfOrderTypes.get(OrderType.consolidatePower) - 1);
                        // Если закончились звёзды, то все остальные типы приказов режем нещадно
                        if (--numRestingStars == 0) {
                            cutOrdersWithoutStar();
                            // Но сбор власти всё-таки оставляем, помним, что их по-прежнему 3 с учётом сбора войск
                            numOfOrderTypes.put(OrderType.consolidatePower, 2);
                        }
                        break;
                    }
                }
            }
        }

        /* Походы. Составляем список всех оставшихся областей.
         * Пытаемся поставить походы там, где они больше всего нужны, если не получается, то удаляем из этого списка;
         * позже ставим туда какой-то из оставшихся приказов. Если получается, то обновляем оценки поддержки для
         * всех соседних областей. Также если данная области была внешним морем, и мы поставили туда поход,
         * то для всех соседних внутренних морей ценность походов возрастает.
         */
        int numRestingMarches = numOfOrderTypes.get(OrderType.march);
        ArrayList<Integer> areasWithMarch = new ArrayList<>();
        float totalVotes = 0;
        for (int area: areasToCommand) {
            totalVotes += voteInAreaForOrder.getOrderVotesInArea(area, Order.march);
        }
        HashSet<Integer> primaryMarchAreas = new HashSet<>();
        primaryMarchAreas.addAll(areasToCommand);
        // Цикл по первичным областям
        while (!primaryMarchAreas.isEmpty() && numRestingMarches > 0 && totalVotes > 0) {
            float trueVote = random.nextFloat() * totalVotes;
            float curVote = 0;
            int potentialMarchArea = primaryMarchAreas.iterator().next();
            for (int area: primaryMarchAreas) {
                curVote += voteInAreaForOrder.getOrderVotesInArea(area, Order.march);
                if (curVote >= trueVote) {
                    potentialMarchArea = area;
                    break;
                }
            }
            if (voteInAreaForOrder.getOrderVotesInArea(potentialMarchArea, Order.march) >
                    random.nextFloat() * voteInAreaForOrder.getTotalVotesInArea(potentialMarchArea)) {
                // Поход состоялся
                orderScheme.addOrderInArea(potentialMarchArea, Order.march);
                numOfOrderTypes.put(OrderType.march, numOfOrderTypes.get(OrderType.march) - 1);
                totalVotes -= voteInAreaForOrder.getOrderVotesInArea(potentialMarchArea, Order.march);
                areasWithMarch.add(potentialMarchArea);
                fillSupportVotes(areasWithMarch);
                areasToCommand.remove(potentialMarchArea);
                numRestingMarches--;
            }
            // Вне зависимости от того, состоялся ли поход, удаляем область из списка первичных претендентов
            primaryMarchAreas.remove(potentialMarchArea);
        }
        if (numRestingMarches == 0) {
            voteInAreaForOrder.forbidOrder(Order.march);
        }
        if (orderScheme.getNumOfOrders(Order.march) == 3) {
            if (--numRestingStars == 0) {
                cutOrdersWithoutStar();
            }
        }

        // Оставшиеся приказы разыгрываем как получится
        voteInAreaForOrder.forbidOrder(Order.consolidatePowerS);
        while (!areasToCommand.isEmpty()) {
            int area = LittleThings.getRandomElementOfSet(areasToCommand);
            Order order = voteInAreaForOrder.giveCommand(area);
            if (order != null) {
                orderScheme.addOrderInArea(area, order);
                numOfOrderTypes.put(order.orderType(), numOfOrderTypes.get(order.orderType()) - 1);
                if (numOfOrderTypes.get(order.orderType()) == 0) {
                    voteInAreaForOrder.forbidOrder(order);
                }
                if (orderScheme.getNumOfOrders(order) == 3) {
                    if (--numRestingStars == 0) {
                        cutOrdersWithoutStar();
                    }
                }
            }
            if (numRestingStars < 0) {
                System.out.println("Вот она, жопа мира");
            }
            areasToCommand.remove(area);
        }

        // Обрабатываем схему приказов и составляем orderMap
        System.out.println(orderScheme);
        ArrayList<Integer> areasWithOrder;
        numRestingStars = numStars;
        boolean cpToMuster = orderScheme.getNumOfOrders(Order.consolidatePower) == 3;
        boolean musterUsed = false;
        for (OrderType type: OrderType.values()) {
            if (orderScheme.getNumOfOrders(type.mainVariant()) == 3) {
                numRestingStars--;
            }
        }
        // Сбор войск и власти
        if (orderScheme.getNumOfOrders(Order.consolidatePowerS) > 0) {
            areasWithOrder = orderScheme.getAreasWithOrder(Order.consolidatePowerS);
            orderMap.put(areasWithOrder.get(0), Order.consolidatePowerS);
            musterUsed = true;
            numRestingStars--;
        }
        areasWithOrder = orderScheme.getAreasWithOrder(Order.consolidatePower);
        for (int area: areasWithOrder) {
            if (cpToMuster || !musterUsed && map.getNumCastle(area) > 0 && numRestingStars > 0) {
                orderMap.put(area, Order.consolidatePowerS);
                cpToMuster = false;
                if (!musterUsed) {
                    musterUsed = true;
                    numRestingStars--;
                }
            } else {
                orderMap.put(area, Order.consolidatePower);
            }
        }
        // Походы
        areasWithOrder = orderScheme.getAreasWithOrder(Order.march);
        int marchMod = forbiddenOrder != OrderType.march && (numRestingStars > 0 || areasWithOrder.size() == 3) ? 1: 0;
        if (forbiddenOrder != OrderType.march && numRestingStars > 0 && areasWithOrder.size() > 0 && areasWithOrder.size() < 3) {
            numRestingStars--;
        }
        while (!areasWithOrder.isEmpty()) {
            int index = random.nextInt(areasWithOrder.size());
            orderMap.put(areasWithOrder.get(index), Order.getMarchWithMod(marchMod));
            marchMod--;
            areasWithOrder.remove(index);
        }
        // Подмоги
        boolean isStar;
        areasWithOrder = orderScheme.getAreasWithOrder(Order.support);
        if (!areasWithOrder.isEmpty()) {
            isStar = (areasWithOrder.size() == 3 || numRestingStars > 0) && forbiddenOrder != OrderType.support;
            if (isStar && areasWithOrder.size() < 3) {
                numRestingStars--;
            }
            if (isStar) {
                int index = random.nextInt(areasWithOrder.size());
                orderMap.put(areasWithOrder.get(index), Order.supportS);
                areasWithOrder.remove(index);
            }
            for (int area : areasWithOrder) {
                orderMap.put(area, Order.support);
            }
        }
        // Обороны
        areasWithOrder = orderScheme.getAreasWithOrder(Order.defence);
        if (!areasWithOrder.isEmpty()) {
            isStar = (areasWithOrder.size() == 3 || numRestingStars > 0) && forbiddenOrder != OrderType.defence;
            if (isStar && areasWithOrder.size() < 3) {
                numRestingStars--;
            }
            if (isStar) {
                int index = random.nextInt(areasWithOrder.size());
                orderMap.put(areasWithOrder.get(index), Order.defenceS);
                areasWithOrder.remove(index);
            }
            for (int area : areasWithOrder) {
                orderMap.put(area, Order.defence);
            }
        }
        // Набеги
        areasWithOrder = orderScheme.getAreasWithOrder(Order.raid);
        if (!areasWithOrder.isEmpty()) {
            isStar = (areasWithOrder.size() == 3 || numRestingStars > 0) && forbiddenOrder != OrderType.raid;
            if (isStar) {
                int index = random.nextInt(areasWithOrder.size());
                orderMap.put(areasWithOrder.get(index), Order.raidS);
                areasWithOrder.remove(index);
            }
            for (int area : areasWithOrder) {
                orderMap.put(area, Order.raid);
            }
        }

        // fillFirstTurnOrderMap();
        return orderMap;
    }

    /**
     * Метод подрезает число оставшихся типов приказов, когда исчерпано количество звёздных приказов
     */
    private void cutOrdersWithoutStar() {
        for (OrderType type: OrderType.values()) {
            if (numOfOrderTypes.get(type) > 0) {
                numOfOrderTypes.put(type, numOfOrderTypes.get(type) - 1);
                if (numOfOrderTypes.get(type) == 0) {
                    voteInAreaForOrder.forbidOrder(type.mainVariant());
                }
            }
        }
    }

    /**
     * Возвращает "коэффициент набегов", который снижает вероятность приказа сбора власти или подмоги,
     * если рядом есть области, откуда враг может набежать на нас
     * @param area номер области
     * @return коэффициент набегов
     */
    private float raidKoef(int area) {
        return (float) Math.pow(2, playerUtils.getNumAreasToRaid(area));
    }

    /**
     * Метод обновляет ценность приказа подмоги в граничащих областях при расстановке приказов.
     * Необходимо вызывать каждый раз, когда обновляется множество поставленных походов
     */
    private void fillSupportVotes(ArrayList<Integer> areasOfMarch) {
        if (forbiddenOrder == OrderType.support) return;
        for (AreaStatus status: AreaStatus.values()) {
            for (int area : areasWithStatus.get(status)) {
                int armySize = model.getArmyInArea(area).getSize();
                switch (status) {
                    case invadedPort:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.support,
                                armySize * playerUtils.getNumAllFightsToSupportFrom(area, areasOfMarch) * supportPrice);
                        break;
                    case innerSea:
                    case hopefulSea:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.support, 2 * armySize *
                                playerUtils.getNumAllFightsToSupportFrom(area, areasOfMarch) * supportPrice);
                        break;
                    case outerSea:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.support,
                                playerUtils.getNumAllFightsToSupportFrom(area, areasOfMarch) * armySize /
                                        (forbiddenOrder == OrderType.raid ? 1 : raidKoef(area)) * supportPrice);
                        break;
                    case borderLand:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.support,
                                playerUtils.getNumAllFightsToSupportFrom(area, areasOfMarch) * armySize *
                                        armyInArea.get(area).getStrength(false) /
                                        (forbiddenOrder == OrderType.raid ? 1 : raidKoef(area)) * supportPrice);
                        break;
                }
            }
        }
    }

    @Override
    public String useRaven() {
        return RAVEN_SEES_WILDLINGS_CODE;
    }

    @Override
    public boolean leaveWildlingCardOnTop(WildlingCard card) {
        System.out.println("Пацаны, я узнал карту одичалых: " + card);
        listOfWildlingCards.set(0, card);
        return true;
    }

    @Override
    public void tellWildlingsBuried() {
        WildlingCard card = listOfWildlingCards.pollFirst();
        listOfWildlingCards.addLast(card);
    }

    public void showPlayedWildlingsCard(WildlingCard card) {
        listOfWildlingCards.removeFirst();
        listOfWildlingCards.addLast(card);
    }

    @Override
    public RaidOrderPlayed playRaid() {
        areasWithRaidsOfPlayer = model.getAreasWithRaidsOfPlayer();
        Iterator<Integer> iterator = areasWithRaidsOfPlayer.get(me).iterator();
        int bestAreaToRaid = -1;
        int raidPrice = 0;
        Order orderDestination;
        if (iterator.hasNext()) {
            int areaOfRaid = iterator.next();
            HashSet<Integer> adjacentAreas = map.getAdjacentAreas(areaOfRaid);
            for (int destination : adjacentAreas) {
                if (model.getTroopsOwner(destination) == me || model.getTroopsOwner(destination) < 0) continue;
                orderDestination = model.getOrderInArea(destination);
                if (orderDestination == null || orderDestination.orderType() == OrderType.march ||
                        orderDestination.orderType() == OrderType.defence && model.getOrderInArea(areaOfRaid) == Order.raid ||
                        map.getAdjacencyType(areaOfRaid, destination) == AdjacencyType.landToSea) continue;
                int price = 0;
                switch (orderDestination) {
                    case raid:
                        price = 5;
                        break;
                    case raidS:
                        price = 6;
                        break;
                    case consolidatePower:
                        price = 30;
                        break;
                    case consolidatePowerS:
                        price = 35;
                        break;
                    case support:
                        price = 20;
                        break;
                    case supportS:
                        price = 25;
                        break;
                    case defence:
                        price = 10;
                        break;
                    case defenceS:
                        price = 12;
                }
                if (price > raidPrice) {
                    bestAreaToRaid = destination;
                    raidPrice = price;
                }
            }
            return new RaidOrderPlayed(areaOfRaid, bestAreaToRaid);
        } else {
            say("Объявляю забастовку: нет у меня никаких набегов!");
            return null;
        }
    }

    @Override
    public MarchOrderPlayed playMarch() {
        areasWithMarchesOfPlayer = model.getAreasWithMarchesOfPlayer();
        int numAreasWithMarches = areasWithMarchesOfPlayer.get(me).size();
        if (numAreasWithMarches == 0) {
            say("Объявляю забастовку: нет у меня никаких походов!");
            return null;
        }
        boolean allVariantsConsidered = false;
        HashSet<MarchOrderPlayed> marches = new HashSet<>();
        // Случайно выбираем поход, который собираемся разыграть
        int areaFrom = LittleThings.getRandomElementOfSet(areasWithMarchesOfPlayer.get(me));
        ArrayList<Integer> areasToMove = new ArrayList<>();
        areasToMove.addAll(model.getAccessibleAreas(areaFrom, me));
        HashMap<Integer, Boolean> isBattleBeginInArea = new HashMap<>();
        // Удаление лишних областей
        ArrayList<Integer> areasToRemove = new ArrayList<>();
        for (int area: areasToMove) {
            if (map.getAreaType(area) == AreaType.port && model.getTroopsOwner(area) == me) {
                areasToRemove.add(area);
                continue;
            }
            isBattleBeginInArea.put(area, model.isBattleBeginInArea(area, me));
        }
        areasToMove.removeAll(areasToRemove);

        model.printAreasInCollection(areasToMove, "Возможные области для похода " + map.getAreaNameRusGenitive(areaFrom));
        areasToMove.add(-1);
        isBattleBeginInArea.put(-1, false);
        ArrayList<Unit> myUnits = model.getArmyInArea(areaFrom).getHealthyUnits();
        boolean isLeaveToken = !map.getAreaType(areaFrom).isNaval() && model.getNumPowerTokensHouse(me) > 0 &&
                model.getPowerTokenInArea(areaFrom) < 0;
        // Если в каком-то из вариантов походов побеждается нейтральный гарнизон, то выбор вариантов сужается.
        boolean isNeutralGarrisonDefeated = false;
        boolean failFlag;
        // Возня с меняющимися индексами
        int armySize = myUnits.size();
        int numAreasToMove = areasToMove.size();
        int leadIndex;
        for (UnitType type: UnitType.values()) {
            indexOfLastSuchUnit[type.getCode()] = -1;
        }
        for (int i = 0; i < armySize; i++) {
            int unitTypeCode = myUnits.get(i).getUnitType().getCode();
            indexToAlign[i] = indexOfLastSuchUnit[unitTypeCode] < 0 ? -1 : indexOfLastSuchUnit[unitTypeCode];
            indexOfLastSuchUnit[unitTypeCode] = i;
            indexes[i] = 0;
        }

        while (!allVariantsConsidered) {
            for (int i = 0; i < armySize; i++) {
                System.out.print((i > 0 ? ", " : "") + indexes[i] + (i == armySize - 1 ? "\n" : ""));
            }
            // Проверка на количество начинающихся битв
            int nBattle = 0;
            int battleArea = -1;
            failFlag = false;
            for (int indexIndex = 0; indexIndex < armySize; indexIndex++) {
                int area = areasToMove.get(indexes[indexIndex]);
                if (isBattleBeginInArea.get(area) && area != battleArea) {
                    battleArea = area;
                    nBattle++;
                }
            }
            if (nBattle <= 1) {
                // Составляем вариант похода
                HashMap<Integer, ArrayList<UnitType>> destinationsOfMarch = new HashMap<>();
                for (int indexIndex = 0; indexIndex < armySize; indexIndex++) {
                    int area = areasToMove.get(indexes[indexIndex]);
                    if (area == -1) continue;
                    if (!destinationsOfMarch.containsKey(area)) {
                        destinationsOfMarch.put(area, new ArrayList<>());
                    }
                    destinationsOfMarch.get(area).add(myUnits.get(indexIndex).getUnitType());
                }
                MarchOrderPlayed march = new MarchOrderPlayed();
                march.setAreaFrom(areaFrom);
                march.setLeaveToken(isLeaveToken);
                march.setDestinationsOfMarch(destinationsOfMarch);
                // Проверка на снабжение
                if (!model.supplyTestForMarch(march)) {
                    failFlag = true;
                }
                // Проверка на число кораблей в порту
                for (Map.Entry<Integer, ArrayList<UnitType>> entry: destinationsOfMarch.entrySet()){
                    if (map.getAreaType(entry.getKey()) == AreaType.port &&
                            entry.getValue().size() + model.getArmyInArea(entry.getKey()).getSize() > MAX_TROOPS_IN_PORT) {
                        failFlag = true;
                    }
                }
                if (!failFlag) {
                    // Если гарнизона нет, то добавляем вариант похода
                    if ((battleArea < 0 || !model.isNeutralGarrisonInArea(battleArea)) && !isNeutralGarrisonDefeated) {
                        marches.add(march);
                        System.out.println(march.toString() + ": " + march.hashCode());
                    } else {
                        // Если гарнизон есть, и мы его пробиваем, то всё ещё лучше
                        if (battleArea >= 0 && model.isNeutralGarrisonInArea(battleArea) &&
                                model.calculatePowerOfPlayerVersusGarrison(me, battleArea,
                                        destinationsOfMarch.get(battleArea), model.getOrderInArea(areaFrom).getModifier()) >=
                                        model.getGarrisonInArea(battleArea)) {
                            if (!isNeutralGarrisonDefeated) {
                                isNeutralGarrisonDefeated = true;
                                marches.clear();
                            }
                            marches.add(march);
                            System.out.println(march.toString() + ": " + march.hashCode());
                        }
                    }
                }
            }
            // Изменяем индексы
            for (leadIndex = armySize - 1; leadIndex >= 0; leadIndex--) {
                if (indexes[leadIndex] < numAreasToMove - 1) {
                    break;
                }
            }
            if (leadIndex < 0) {
                allVariantsConsidered = true;
            } else {
                // Ведущий индекс увеличивается на единицу, следующие за ним становятся нулями
                indexes[leadIndex]++;
                for (int index = leadIndex + 1; index < armySize; index++) {
                    indexes[index] = indexToAlign[index] < 0 ? 0 : indexes[indexToAlign[index]];
                }
            }
        }
        MarchOrderPlayed march = LittleThings.getRandomElementOfSet(marches);
        return march.getDestinationsOfMarch().size() > 0 ? march : LittleThings.getRandomElementOfSet(marches);
    }

    @Override
    public int getNumCapturedShips(int portArea) {
        return 1;
    }

    @Override
    public SideOfBattle sideToSupport(BattleInfo battleInfo) {
        if (me == battleInfo.getAttacker()) {
            return SideOfBattle.attacker;
        } else if (me == battleInfo.getDefender()) {
            return SideOfBattle.defender;
        } else {
            float bet = random.nextFloat();
            if (bet < 0.4) {
                return SideOfBattle.attacker;
            } else if (bet > 0.6) {
                return SideOfBattle.defender;
            } else {
                return SideOfBattle.neutral;
            }
        }
    }

    @Override
    public int playHouseCard(BattleInfo battleInfo) {
        // Просто играем случайную карту из имеющихся активных
        boolean[] isCardActive = new boolean[NUM_HOUSE_CARDS];
        int nActiveCards = 0;
        for (int curCard = 0; curCard < NUM_HOUSE_CARDS; curCard++) {
            isCardActive[curCard] = model.isCardActive(me, curCard);
            if (isCardActive[curCard]) nActiveCards++;
        }
        if (nActiveCards > 0) {
            int curActiveCardIndex = 0;
            int neededActiveCardIndex = random.nextInt(nActiveCards);
            for (int curCard = 0; curCard < NUM_HOUSE_CARDS; curCard++) {
                if (isCardActive[curCard]) {
                    if (curActiveCardIndex == neededActiveCardIndex) {
                        return curCard;
                    } else {
                        curActiveCardIndex++;
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public boolean useTyrion(BattleInfo battleInfo, HouseCard opponentCard) {
        return true;
    }


    @Override
    public int chooseInfluenceTrackDoran(BattleInfo battleInfo) {
        int enemy = battleInfo.getAttacker() == me ? battleInfo.getDefender() : battleInfo.getAttacker();
        int[] pos = new int[NUM_TRACK];
        int bestPos = NUM_PLAYER;
        int bestTrack = TrackType.raven.getCode();
        for (int track = NUM_TRACK - 1; track >= 0; track--) {
            pos[track] = model.getTrackPlaceForPlayer(track, enemy);
            if (pos[track] < bestPos) {
                bestPos = pos[track];
                bestTrack = track;
            }
        }
        return bestTrack;
    }

    @Override
    public boolean useAeron(BattleInfo battleInfo) {
        return true;
    }

    @Override
    public int chooseAreaQueenOfThorns(HashSet<Integer> possibleAreas) {
        model.printAreasInCollection(possibleAreas, "Области для удаления вражеского приказа бабкой");
        return LittleThings.getRandomElementOfSet(possibleAreas);
    }

    @Override
    public boolean useSword(BattleInfo battleInfo) {
        if (battleInfo.getAttacker() == me) {
            return battleInfo.getStrengthOnSide(SideOfBattle.attacker) + 1 ==
                   battleInfo.getStrengthOnSide(SideOfBattle.defender);
        } else {
            assert(battleInfo.getDefender() == me);
            return battleInfo.getStrengthOnSide(SideOfBattle.defender) + 1 ==
                    battleInfo.getStrengthOnSide(SideOfBattle.attacker);
        }
    }

    @Override
    public int chooseAreaToRetreat(Army retreatingArmy, HashSet<Integer> possibleAreas) {
        return LittleThings.getRandomElementOfSet(possibleAreas);
    }

    @Override
    public int areaToUseRenly(HashSet<Integer> possibleAreas) {
        return LittleThings.getRandomElementOfSet(possibleAreas);
    }

    @Override
    public int chooseAreaCerseiLannister(HashSet<Integer> possibleAreas) {
        model.printAreasInCollection(possibleAreas, "Области для удаления вражеского приказа Серсеей");
        return LittleThings.getRandomElementOfSet(possibleAreas);
    }

    @Override
    public int chooseCardPatchface(int enemy) {
        for (int curCard = 0; curCard < NUM_HOUSE_CARDS; curCard++) {
            if (model.isCardActive(enemy, curCard)) {
                return curCard;
            }
        }
        return -1;
    }

    @Override
    public MusterPlayed playConsolidatePowerS(int castleArea) {
        return musterInArea(castleArea);
    }

    @Override
    public MusterPlayed muster(HashSet<Integer> castleAreas) {
        return musterInArea(LittleThings.getRandomElementOfSet(castleAreas));
    }

    @Override
    public int eventToChoose(int deckNumber) {
        return random.nextInt(3);
    }

    @Override
    public int bid(int track) {
        // считаем количество всевозможных комбинаций ставок на (3 - track) треков и выбираем случайную из них!
        int numTokens = model.getNumPowerTokensHouse(me);
        int totalCombinations = (int) (LittleThings.numCombinations(NUM_TRACK - track, numTokens + NUM_TRACK - track));
        int chosenCombination = random.nextInt(totalCombinations);
        int threshold = 0;
        for (int curBid = 0; curBid < numTokens; curBid++) {
            threshold += (int) (LittleThings.numCombinations(NUM_TRACK - track - 1, numTokens + NUM_TRACK - track - curBid - 1));
            if (chosenCombination < threshold) {
                return curBid;
            }
        }
        return numTokens;
    }

    @Override
    public int[] kingChoiceInfluenceTrack(int track, int[] bids) {
        boolean[] isPlayerCounted = new boolean[NUM_PLAYER];
        int[] newPlayerOnPlace = new int[NUM_PLAYER];
        ArrayList<Integer> pretenders = new ArrayList<>();
        for (int curPlace = 0; curPlace < NUM_PLAYER; ) {
            int curMaxBid = -1;
            for (int player = 0; player < NUM_PLAYER; player++) {
                if (bids[player] > curMaxBid && !isPlayerCounted[player]) {
                    pretenders.clear();
                    pretenders.add(player);
                    curMaxBid = bids[player];
                } else if (bids[player] == curMaxBid) {
                    pretenders.add(player);
                }
            }
            while(!pretenders.isEmpty()) {
                if (pretenders.contains(me)) {
                    newPlayerOnPlace[curPlace] = me;
                    isPlayerCounted[me] = true;
                    pretenders.remove((Integer) me);
                } else {
                    int nextOnTrackIndex = random.nextInt(pretenders.size());
                    newPlayerOnPlace[curPlace] = pretenders.get(nextOnTrackIndex);
                    isPlayerCounted[pretenders.get(nextOnTrackIndex)] = true;
                    pretenders.remove(nextOnTrackIndex);
                }
                curPlace++;
            }
        }
        return newPlayerOnPlace;
    }

    @Override
    public int wildlingBid(int strength) {
        int money = model.getNumPowerTokensHouse(me);
        float unitPrice = 4;
        float winProfit = 0;
        float loseProfit = 0;
        boolean isKing = model.getTrackPlayerOnPlace(0, 0) == me;
        boolean firstFlag = true;
        WildlingCard knownCard = null;
        int numPossibleCards = 0;
        Set<WildlingCard> forbiddenCards = new HashSet<>();
        for (WildlingCard card: listOfWildlingCards) {
            if (firstFlag) {
                firstFlag = false;
                if (card != null) {
                    knownCard = card;
                    break;
                }
            }
            if (card != null) {
                forbiddenCards.add(card);
            }
        }
        if (knownCard != null) {
            for (WildlingCard card : WildlingCard.values()) {
                if (!forbiddenCards.contains(card) && knownCard != card) {
                    forbiddenCards.add(card);
                }
            }
        }
        // Подготовительная информация
        int sumPositions = 0;
        int worsePosition = 0;
        int bestPosition = NUM_PLAYER;
        int bestSwordOrRavenPosition = NUM_PLAYER;
        int preemptiveRaidCheater = model.getPreemptiveRaidCheater();
        int sumMoney = 0;
        int minOtherMoney = Integer.MAX_VALUE;
        int pos;
        for (int track = 0; track < NUM_TRACK; track++) {
            pos = model.getTrackPlaceForPlayer(track, me);
            sumPositions += pos;
            if (pos > worsePosition) {
                pos = worsePosition;
            }
            if (bestPosition > pos) {
                bestPosition = pos;
            }
            if (track > 0 && bestSwordOrRavenPosition > pos) {
                bestSwordOrRavenPosition = pos;
            }
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (player != preemptiveRaidCheater) {
                int playerMoney = model.getNumPowerTokensHouse(player);
                sumMoney += playerMoney;
                if (player != me && playerMoney < minOtherMoney) {
                    minOtherMoney = playerMoney;
                }
            }
        }
        HashMap<Integer, DummyArmy> armyInArea = playerUtils.makeDummyArmies(me);
        int numDisbandSwipe = PlayerUtils.getNumDisbandSwipe(armyInArea);
        Map<Integer, Integer> troopsNumberMap = model.getAreasWithTroopsOfPlayerAndSize(me);
        int biggestFreeSupplyCell = GameUtils.getBiggestFreeArmySize(troopsNumberMap, model.getSupply(me));
        int firstActiveCardStrength = model.getFirstActiveHouseCard(me).getStrength();
        int firstPassiveCardStrength = model.getFirstPassiveHouseCard(me) != null ?
                model.getFirstPassiveHouseCard(me).getStrength() : 0;

        // Считаем пользу от выигрыша и вред от проигрыша
        for (WildlingCard card: WildlingCard.values()) {
            if (!forbiddenCards.contains(card)) {
                numPossibleCards++;
                switch (card) {
                    case shapechangerScout:
                        winProfit += 2;
                        loseProfit += Math.max(money - 2, 0);
                        break;
                    case rattleShirtRaiders:
                        winProfit += model.getSupply(me) == MAX_SUPPLY ? 0 : 2;
                        loseProfit += model.getSupply(me) == 0 ? 0 : 3;
                        break;
                    case crowKillers:
                        int numAliveKnights = model.getNumAliveUnits(me, UnitType.knight);
                        winProfit += unitPrice * Math.min(2, MAX_NUM_OF_UNITS[UnitType.knight.getCode()] - numAliveKnights);
                        loseProfit += unitPrice * Math.max(0, numAliveKnights - 2);
                        break;
                    case aKingBeyondTheWall:
                        winProfit += 2 * worsePosition;
                        loseProfit += 2 * Math.max(0, NUM_TRACK * (NUM_PLAYER - 1) - sumPositions - 3 - bestSwordOrRavenPosition);
                        break;
                    case preemptiveRaid:
                        winProfit += 2;
                        loseProfit += 2 * Math.max(0, Math.min(2, NUM_PLAYER - 1 - bestPosition));
                        break;
                    case mammothRiders:
                        winProfit += 2 * Math.max(0, firstPassiveCardStrength - firstActiveCardStrength +
                                firstPassiveCardStrength / model.getNumActiveCardsOfPlayer(me));
                        loseProfit += unitPrice * (numDisbandSwipe > 2 ? 1 : 2);
                        break;
                    case hordeDescends:
                        winProfit += biggestFreeSupplyCell > 1 ? unitPrice * 4 : 2;
                        loseProfit += (model.isVulnerableToHorde(me) ? unitPrice * 4 : unitPrice);
                        break;
                    case massingOnTheMilkwater:
                        winProfit += 2 * (4 - firstActiveCardStrength);
                        loseProfit += 2;
                        break;
                }
            }
        }
        winProfit /= numPossibleCards;
        loseProfit /= numPossibleCards;
        float averageBid = 1f * strength / (preemptiveRaidCheater >= 0 ? NUM_PLAYER - 1 : NUM_PLAYER);
        float[] vote = new float[money + 1];
        float sumVotes = 0;
        for (int bid = 0; bid <= money; bid++) {
            // Отношение данной ставки к средней ставке, нужной для победы (поправка +1 королю за право выбора)
            float rate = averageBid > 0 ? ((isKing ? 1 : 0) + bid) / averageBid : 1.001f + (isKing ? 1 : 0) + bid;
            if (sumMoney > strength) {
                // Можем и победить
                vote[bid] = rate <= 1 ?
                        Math.min(averageBid - bid, 1f / (Math.max(0.001f, loseProfit * 0.5f * (1 - rate)))) :
                        Math.max(winProfit * rate / (1 + rate) - bid, 0);
            } else {
                // Победить невозможно
                vote[bid] = rate <= 1? Math.max(1f / (0.001f + bid + loseProfit * (1 - rate)), 0) : 0;
            }
            sumVotes += vote[bid];
        }
        int minBidNotToLose = Math.min(Math.max(0, (int) (Math.ceil(averageBid) - (isKing ? 0.99f : 0))),
                minOtherMoney + (isKing ? 0 : 1));
        if (minBidNotToLose <= money) {
            float adding = loseProfit / 4;
            vote[minBidNotToLose] += adding;
            sumVotes += adding;
        }
        float trueVote = random.nextFloat() * sumVotes;
        float curVote = 0;
        for (int curBid = 0; curBid <= money; curBid++) {
            curVote += vote[curBid];
            if (curVote >= trueVote) {
                return curBid;
            }
        }
        return 0;
    }

    @Override
    public int kingChoiceWildlings(WildlingCard card, ArrayList<Integer> pretenders, boolean isBidTop){
        if (pretenders.contains(me)) {
            if (isBidTop) {
                return me;
            } else {
                int bet = random.nextInt(pretenders.size() - 1);
                int selfIndex = pretenders.indexOf(me);
                if (bet >= selfIndex) {
                    bet += 1;
                }
                return pretenders.get(bet);
            }
        } else {
            return pretenders.get(random.nextInt(pretenders.size()));
        }
    }

    @Override
    public DisbandPlayed disband(DisbandReason reason) {
        areasWithTroopsOfPlayer = model.getAreasWithTroopsOfPlayer(me);
        HashMap<Integer, DummyArmy> armyInArea = playerUtils.makeDummyArmies(me);
        DisbandPlayed disbandVariant = new DisbandPlayed();
        int numDisbands;
        if (reason == DisbandReason.hordeCastle) {
            // Нужно распустить 2 юнита в одном из своих замков и крепостей
            ArrayList<Integer> disbandAreas = model.getHordeVulnerableCastles(me);
            int area = disbandAreas.get(random.nextInt(disbandAreas.size()));
            DummyArmy army = armyInArea.get(area);
            for (numDisbands = reason.getNumDisbands(); numDisbands > 0; numDisbands--) {
                UnitType unit = army.getWeakestUnit();
                army.removeUnit(unit);
                disbandVariant.addDisbandedUnit(area, unit);
            }
            return disbandVariant;
        }
        if (reason != DisbandReason.supply) {
            // Нужно распустить фиксированное число юнитов
            numDisbands = reason.getNumDisbands();
            // Сначала снимаем "халяву": пешек и корабли, в областях с которыми уже есть юниты
            HashMap<Integer, Integer> disbandSwipes = PlayerUtils.getDisbandSwipes(armyInArea);
            while (numDisbands > 0 && disbandSwipes.size() > 0) {
                int area = LittleThings.getRandomElementOfSet(disbandSwipes.keySet());
                UnitType disbandedUnit = map.getAreaType(area) == AreaType.land ? UnitType.pawn : UnitType.ship;
                disbandVariant.addDisbandedUnit(area, disbandedUnit);
                armyInArea.get(area).removeUnit(disbandedUnit);
                if (disbandSwipes.get(area) > 1) {
                    disbandSwipes.put(area, disbandSwipes.get(area) - 1);
                } else {
                    disbandSwipes.remove(area);
                }
                numDisbands--;
            }
            while (numDisbands > 0) {
                int area = LittleThings.getRandomElementOfSet(armyInArea.keySet());
                DummyArmy army = armyInArea.get(area);
                UnitType disbandedUnit;
                if (army.getSize() == 1) {
                    disbandedUnit = army.pollNextUnit();
                    armyInArea.remove(area);
                } else {
                    // Если после удаления халявы в армии по прежнему больше 1 юнита, то там остались только кони и башни
                    disbandedUnit = army.hasUnitOfType(UnitType.siegeEngine) ? UnitType.siegeEngine : UnitType.knight;
                    army.removeUnit(disbandedUnit);
                }
                disbandVariant.addDisbandedUnit(area, disbandedUnit);
                numDisbands--;
            }
        } else {
            // Нужно распустить войска из-за снабжения
            ArrayList<Integer> areasWithSuchArmies = new ArrayList<>();
            int supplyLevel = model.getSupply(me);
            Map<Integer, Integer> areasWithTroopsOfPlayerAndSize = new HashMap<>();
            areasWithTroopsOfPlayerAndSize.putAll(model.getAreasWithTroopsOfPlayerAndSize(me));
            do {
                int breakingArmySize = GameUtils.getBreakingArmySize(areasWithTroopsOfPlayerAndSize, supplyLevel);
                areasWithSuchArmies.clear();
                for (Map.Entry<Integer, Integer> entry: areasWithTroopsOfPlayerAndSize.entrySet()) {
                    if (entry.getValue() == breakingArmySize) {
                        areasWithSuchArmies.add(entry.getKey());
                    }
                }
                int area = areasWithSuchArmies.get(random.nextInt(areasWithSuchArmies.size()));
                UnitType weakestUnit = armyInArea.get(area).getWeakestUnit();
                armyInArea.get(area).removeUnit(weakestUnit);
                disbandVariant.addDisbandedUnit(area, weakestUnit);
                areasWithTroopsOfPlayerAndSize.put(area, areasWithTroopsOfPlayerAndSize.get(area) - 1);
            } while (!GameUtils.supplyTest(areasWithTroopsOfPlayerAndSize, supplyLevel));
        }
        return disbandVariant;
    }

    @Override
    public UnitExecutionPlayed crowKillersLoseDecision(int numKnightsToDowngrade) {
        UnitExecutionPlayed targetAreas = new UnitExecutionPlayed();
        HashMap<Integer, Integer> numKnightInArea = model.getNumUnitsOfTypeInArea(me, UnitType.knight);
        for (int numRestingUpgrades = numKnightsToDowngrade; numRestingUpgrades > 0; numRestingUpgrades--) {
            int area = LittleThings.getRandomElementOfSet(numKnightInArea.keySet());
            targetAreas.addUnit(area);
            if (numKnightInArea.get(area) == 1) {
                numKnightInArea.remove(area);
            } else {
                numKnightInArea.put(area, numKnightInArea.get(area) - 1);
            }
        }
        return targetAreas;
    }

    @Override
    public UnitExecutionPlayed crowKillersKillKnights(int numKnightsToKill) {
        UnitExecutionPlayed targetAreas = new UnitExecutionPlayed();
        HashMap<Integer, Integer> numKnightInArea = model.getNumUnitsOfTypeInArea(me, UnitType.knight);
        for (int numRestingUpgrades = numKnightsToKill; numRestingUpgrades > 0; numRestingUpgrades--) {
            int area = LittleThings.getRandomElementOfSet(numKnightInArea.keySet());
            targetAreas.addUnit(area);
            if (numKnightInArea.get(area) == 1) {
                numKnightInArea.remove(area);
            } else {
                numKnightInArea.put(area, numKnightInArea.get(area) - 1);
            }
        }
        return targetAreas;
    }

    @Override
    public UnitExecutionPlayed crowKillersTopDecision(int numPawnsToUpgrade) {
        UnitExecutionPlayed targetAreas = new UnitExecutionPlayed();
        HashMap<Integer, Integer> numPawnInArea = model.getNumUnitsOfTypeInArea(me, UnitType.pawn);
        for (int numRestingUpgrades = numPawnsToUpgrade; numRestingUpgrades > 0; numRestingUpgrades--) {
            int area = LittleThings.getRandomElementOfSet(numPawnInArea.keySet());
            targetAreas.addUnit(area);
            if (numPawnInArea.get(area) == 1) {
                numPawnInArea.remove(area);
            } else {
                numPawnInArea.put(area, numPawnInArea.get(area) - 1);
            }
        }
        return targetAreas;
    }

    @Override
    public int massingOnTheMilkwaterLoseDecision() {
        for (int curCard = NUM_HOUSE_CARDS - 1; curCard >= 0; curCard--) {
            if (model.isCardActive(me, curCard)) {
                return curCard;
            }
        }
        return -1;
    }

    @Override
    public int mammothRidersTopDecision() {
        for (int curCard = 0; curCard < NUM_HOUSE_CARDS; curCard++) {
            if (!model.isCardActive(me, curCard)) {
                return curCard;
            }
        }
        return -1;
    }

    @Override
    public TrackType aKingBeyondTheWallTopDecision() {
        int[] myPlaces = new int[NUM_TRACK];
        int bottomPlace = 0;
        int worseTrack = -1;
        for (int i = 0; i < NUM_TRACK; i++) {
            myPlaces[i] = model.getTrackPlaceForPlayer(i, me);
            if (myPlaces[i] >= bottomPlace) {
                worseTrack = i;
                bottomPlace = myPlaces[i];
            }
        }
        return myPlaces[TrackType.raven.getCode()] >= 4 ? TrackType.raven : TrackType.getTrack(worseTrack);
    }

    @Override
    public TrackType aKingBeyondTheWallLoseDecision() {
        int swordPlace =  model.getTrackPlaceForPlayer(TrackType.valyrianSword.getCode(), me);
        int ravenPlace =  model.getTrackPlaceForPlayer(TrackType.raven.getCode(), me);
        if (swordPlace > 0) {
            return ravenPlace >= 4 ? TrackType.raven : TrackType.valyrianSword;
        } else {
            return ravenPlace >= 2 ? TrackType.raven : TrackType.valyrianSword;
        }
    }

    @Override
    public Object preemptiveRaidBottomDecision() {
        int[] myPlaces = new int[NUM_TRACK];
        int bestPlace = NUM_PLAYER;
        ArrayList<Integer> bestTracks = new ArrayList<>();
        for (int i = 0; i < NUM_TRACK; i++) {
            myPlaces[i] = model.getTrackPlaceForPlayer(i, me);
            if (myPlaces[i] < bestPlace) {
                bestTracks.clear();
                bestTracks.add(i);
                bestPlace = myPlaces[i];
            } else if (myPlaces[i] == bestPlace) {
                bestTracks.add(i);
            }
        }
        if (bestPlace > 0) {
            if (bestTracks.contains(TrackType.valyrianSword.getCode())) {
                return TrackType.valyrianSword;
            }
            if (bestTracks.contains(TrackType.ironThrone.getCode())) {
                return TrackType.ironThrone;
            }
            if (bestPlace >= 3) {
                return TrackType.raven;
            }
        }
        return disband(DisbandReason.wildlingCommonDisband);
    }

    private void say(String text) {
        model.say(HOUSE[me] + ": " + text);
    }

    private MusterPlayed musterInArea(int castleArea) {
        long time = System.currentTimeMillis();
        int numMusterPoints = map.getNumCastle(castleArea);
        if (numMusterPoints == 0) {
            say("Объявляю забастовку: очков сбора войск нету у меня!");
            return null;
        }
        HashSet<Integer> normNavalAreas = new HashSet<>();
        HashSet<Integer> mustHaveNavalAreas = new HashSet<>();
        if (model.getRestingUnits(me, UnitType.ship) > 0) {
            HashSet<Integer> adjacentAreas = map.getAdjacentAreas(castleArea);
            for (int area: adjacentAreas) {
                if (map.getAreaType(area) == AreaType.sea && model.getTroopsOwner(area) < 0) {
                    mustHaveNavalAreas.add(area);
                } else if (map.getAreaType(area) == AreaType.sea && model.getTroopsOwner(area) == me ||
                        map.getAreaType(area) == AreaType.port &&
                                (model.getTroopsOwner(area) < 0 ||
                                        model.getTroopsOwner(map.getSeaNearPort(area)) >= 0 &&
                                                model.getTroopsOwner(map.getSeaNearPort(area)) != me)){
                    normNavalAreas.add(area);
                }
            }
        }
        MusterPlayed template = new MusterPlayed(castleArea);
        int curAvailableShips = model.getRestingUnits(me, UnitType.ship);
        for (int mustHaveArea: mustHaveNavalAreas) {
            if (numMusterPoints == 0 || curAvailableShips == 0) {
                break;
            }
            template.addNewMusterable(mustHaveArea, UnitType.ship);
            numMusterPoints--;
            curAvailableShips--;
        }
        // После того, как мы поставили корабли в стратегически важные моря, считаем, остались ли у нас очки сбора
        // Если не остались, то возвращаем единственно возможный план сбора войск
        if (numMusterPoints == 0) {
            return template;
        }
        normNavalAreas.addAll(mustHaveNavalAreas);
        // Если остались, то пробуем использовать оставшиеся очки сбора и выбираем случайный сбор из допустимых
        HashSet<MusterPlayed> musterVariants = getMusterVariants(castleArea, normNavalAreas,
                template, numMusterPoints, curAvailableShips);
        System.out.println("Сбор " + HOUSE_GENITIVE[me] + ": " + (System.currentTimeMillis() - time) + " мс");
        return LittleThings.getRandomElementOfSet(musterVariants);
    }

    private HashSet<MusterPlayed> getMusterVariants(int castleArea, HashSet<Integer> navalAreas,
                                                      MusterPlayed template, int musterPoints, int curAvailableShips) {
        HashSet<MusterPlayed> musterVariants = new HashSet<>();
        MusterPlayed muster;
        for (int restingPoints = musterPoints; restingPoints >= 0; restingPoints--) {
            switch (restingPoints) {
                case 0:
                    if (musterVariants.isEmpty()) {
                        musterVariants.add(template);
                    }
                    break;
                case 1:
                    if (!musterVariants.isEmpty()) {
                        break;
                    }
                    // Пешка
                    if (model.getRestingUnits(me, UnitType.pawn) > 0) {
                        muster = new MusterPlayed(template);
                        muster.addNewMusterable(castleArea, UnitType.pawn);
                        addNewMusterVariant(musterVariants, muster);
                    }
                    // Апгрейд
                    if (model.getArmyInArea(castleArea).hasUnitOfType(UnitType.pawn)) {
                        if (model.getRestingUnits(me, UnitType.knight) > 0) {
                            muster = new MusterPlayed(template);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToKnight);
                            musterVariants.add(muster);
                        }
                        if (model.getRestingUnits(me, UnitType.siegeEngine) > 0) {
                            muster = new MusterPlayed(template);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToSiege);
                            musterVariants.add(muster);
                        }
                    }
                    // Корабль
                    if (curAvailableShips > 0) {
                        for (int area : navalAreas) {
                            muster = new MusterPlayed(template);
                            muster.addNewMusterable(area, UnitType.ship);
                            addNewMusterVariant(musterVariants, muster);
                        }
                    }
                    break;
                case 2:
                    // Вариант "Конь"
                    if (model.getRestingUnits(me, UnitType.knight) > 0) {
                        muster = new MusterPlayed(template);
                        muster.addNewMusterable(castleArea, UnitType.knight);
                        addNewMusterVariant(musterVariants, muster);
                    }
                    // Вариант "Башня"
                    if (model.getRestingUnits(me, UnitType.siegeEngine) > 0) {
                        muster = new MusterPlayed(template);
                        muster.addNewMusterable(castleArea, UnitType.siegeEngine);
                        addNewMusterVariant(musterVariants, muster);
                    }
                    // Вариант "Две пешки"
                    if (model.getRestingUnits(me, UnitType.pawn) > 1) {
                        muster = new MusterPlayed(template);
                        muster.addNewMusterable(castleArea, UnitType.pawn);
                        muster.addNewMusterable(castleArea, UnitType.pawn);
                        addNewMusterVariant(musterVariants, muster);
                    }
                    // Варианты "Два апгрейда"
                    if (model.getArmyInArea(castleArea).getNumUnitOfType(UnitType.pawn) > 1) {
                        if (model.getRestingUnits(me, UnitType.siegeEngine) > 1) {
                            muster = new MusterPlayed(template);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToSiege);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToSiege);
                            musterVariants.add(muster);
                        }
                        if (model.getRestingUnits(me, UnitType.knight) > 1) {
                            muster = new MusterPlayed(template);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToKnight);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToKnight);
                            musterVariants.add(muster);
                        }
                        if (model.getRestingUnits(me, UnitType.siegeEngine) > 0 &&
                                model.getRestingUnits(me, UnitType.knight) > 0) {
                            muster = new MusterPlayed(template);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToSiege);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToKnight);
                            musterVariants.add(muster);
                        }
                    }
                    // Вариант "Пешка + корабль"
                    if (model.getRestingUnits(me, UnitType.pawn) > 0 &&
                            curAvailableShips > 0) {
                        for (int area : navalAreas) {
                            muster = new MusterPlayed(template);
                            muster.addNewMusterable(castleArea, UnitType.pawn);
                            muster.addNewMusterable(area, UnitType.ship);
                            addNewMusterVariant(musterVariants, muster);
                        }
                    }
                    // Варианты "Апгрейд + корабль"
                    if (model.getArmyInArea(castleArea).hasUnitOfType(UnitType.pawn)) {
                        if (model.getRestingUnits(me, UnitType.knight) > 0 && curAvailableShips > 0) {
                            for (int area : navalAreas) {
                                muster = new MusterPlayed(template);
                                muster.addNewMusterable(castleArea, PawnPromotion.pawnToKnight);
                                muster.addNewMusterable(area, UnitType.ship);
                                addNewMusterVariant(musterVariants, muster);
                            }
                        }
                        if (model.getRestingUnits(me, UnitType.siegeEngine) > 0 && curAvailableShips > 0) {
                            for (int area : navalAreas) {
                                muster = new MusterPlayed(template);
                                muster.addNewMusterable(castleArea, PawnPromotion.pawnToSiege);
                                muster.addNewMusterable(area, UnitType.ship);
                                addNewMusterVariant(musterVariants, muster);
                            }
                        }
                    }
                    // Варианты "Два корабля"
                    if (model.getRestingUnits(me, UnitType.ship) > 1) {
                        for (int area1 : navalAreas) {
                            for (int area2: navalAreas) {
                                // Если море рядом с портом наше, то нет смысла строить в порту 2 корабля
                                if (map.getAreaType(area1) == AreaType.port && area1 == area2 &&
                                        model.getTroopsOwner(map.getSeaNearPort(area1)) == me) {
                                    continue;
                                }
                                muster = new MusterPlayed(template);
                                muster.addNewMusterable(area1, UnitType.ship);
                                muster.addNewMusterable(area2, UnitType.ship);
                                addNewMusterVariant(musterVariants, muster);
                            }
                        }
                    }
                    break;
            }
        }
        return musterVariants;
    }

    private void addNewMusterVariant(HashSet<MusterPlayed> set, MusterPlayed muster) {
        if (model.supplyTestForMuster(muster) && model.portTroopsTestForMuster(muster)) {
            set.add(muster);
        }
    }
}
