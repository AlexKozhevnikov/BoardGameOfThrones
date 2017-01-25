package com.alexeus.ai;

import com.alexeus.ai.struct.AreaStatus;
import com.alexeus.ai.struct.DummyArmy;
import com.alexeus.ai.struct.OrderScheme;
import com.alexeus.ai.struct.VotesForOrderInArea;
import com.alexeus.ai.util.PlayerUtils;
import com.alexeus.logic.constants.MainConstants;
import com.alexeus.logic.Game;
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

    protected Game game;

    protected GameOfThronesMap map;

    protected final int houseNumber;

    protected WildlingCard wildlingCardInfo = null;

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

    public PrimitivePlayer(Game newGame, int houseNumber) {
        game = newGame;
        random = new Random();
        map = game.getMap();
        this.houseNumber = houseNumber;
        orderMap = new HashMap<>();
        areasWithStatus = new HashMap<>();
        numOfOrderTypes = new HashMap<>();
        for (AreaStatus areaStatus: AreaStatus.values()) {
            areasWithStatus.put(areaStatus, new ArrayList<>());
        }
    }

    @Override
    public void nameYourself() {
        say("Я - Примитивный игрок за " + HOUSE_GENITIVE[houseNumber] + "!");
    }

    @Override
    public HashMap<Integer, Order> giveOrders() {
        areasWithTroopsOfPlayer = game.getAreasWithTroopsOfPlayer(houseNumber);
        // Множество с областями игрока, из которого мы будем удалять области, когда им уже отданы приказы
        Set<Integer> areasToCommand = new HashSet<>();
        areasToCommand.addAll(areasWithTroopsOfPlayer);
        PlayerUtils playerUtils = PlayerUtils.getInstance();
        int numFightsCanBegin;
        int numStars = game.getNumStars(houseNumber);
        int numRestingStars = numStars;
        OrderType forbiddenOrder = game.getForbiddenOrder();
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
                    int seaOwner = game.getTroopsOwner(map.getSeaNearPort(area));
                    if (seaOwner == houseNumber) {
                        status = AreaStatus.myPort;
                    } else if (seaOwner < 0) {
                        status = AreaStatus.freePort;
                    } else {
                        status = AreaStatus.invadedPort;
                    }
                    break;
                case sea:
                    numFightsCanBegin = playerUtils.getNumFightsCanBeginInArea(area);
                    if (numFightsCanBegin > 0) {
                        status = AreaStatus.outerSea;
                    } else {
                        for (int adjArea: map.getAdjacentAreas(area)) {
                            if (map.getAreaType(adjArea) == AreaType.sea && game.getAreaOwner(adjArea) < 0) {
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
                    numFightsCanBegin = playerUtils.getNumFightsCanBeginInArea(area);
                    status = numFightsCanBegin == 0 ? (map.getNumCastle(area) > 0 ? AreaStatus.musterCastle :
                            AreaStatus.innerLand) : AreaStatus.borderLand;
                    break;
            }
            areasWithStatus.get(status).add(area);
        }

        // Добавляем голоса за приказы в зависимости от статуса области
        VotesForOrderInArea voteInAreaForOrder = new VotesForOrderInArea();
        for (AreaStatus status: AreaStatus.values()) {
            for (int area : areasWithStatus.get(status)) {
                voteInAreaForOrder.addArea(area);
                int armySize = game.getArmyInArea(area).getSize();
                switch (status) {
                    case myPort:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.consolidatePower, 4);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.support,
                                playerUtils.getNumFightsToSupportFrom(map.getSeaNearPort(area)));
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, armySize * armySize * armySize);
                        break;
                    case invadedPort:
                        int enemyArmySize = game.getArmyInArea(map.getSeaNearPort(area)).getSize();
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.raid, enemyArmySize);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, armySize * armySize * armySize);
                        break;
                    case freePort:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.consolidatePower, 2);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, armySize * armySize * armySize);
                        break;
                    case innerSea:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.support,
                                playerUtils.getNumFightsToSupportFrom(area) * armySize);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, armySize);
                        break;
                    case hopefulSea:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.support,
                                playerUtils.getNumFightsToSupportFrom(area) * armySize);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, 10 * armySize);
                        break;
                    case outerSea:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.support,
                                Math.max(0, playerUtils.getNumFightsToSupportFrom(area) * armySize -
                                playerUtils.getNumAreasToRaid(area)));
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.raid, 2 * playerUtils.getNumAreasToRaidFrom(area));
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, 2 * armySize * armySize);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.defence, Math.max(0, 4 - armySize) *
                                (forbiddenOrder == OrderType.support ? 4 : 2));
                        break;
                    case innerLand:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.consolidatePower, 3 + 3 * map.getNumCrown(area));
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.support,
                                playerUtils.getNumFightsToSupportFrom(area) * armySize);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, 2 * armySize * armySize);
                        break;
                    case musterCastle:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.consolidatePowerS,
                                Math.max(0, (map.getNumCastle(area) == 2 ? 10 : 5) * (3 - armySize)));
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.support,
                                playerUtils.getNumFightsToSupportFrom(area) * armySize);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.consolidatePower, 1 + map.getNumCrown(area));
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, 2 * armySize * armySize);
                        break;
                    case borderLand:
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.raid, 2 * playerUtils.getNumAreasToRaidFrom(area));
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.march, 2 * armySize * armySize);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.consolidatePower,
                                (float) ((3 + 3 * map.getNumCrown(area)) /
                                Math.pow(forbiddenOrder == OrderType.raid ? 1 : 2, playerUtils.getNumAreasToRaid(area))));
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.support,
                                playerUtils.getNumFightsToSupportFrom(area) * armySize);
                        voteInAreaForOrder.setOrderVotesInArea(area, Order.defence,
                                playerUtils.getNumFightsCanBeginInArea(area) * 2);
                        break;
                }
            }
        }
        if (forbiddenOrder != null && forbiddenOrder != OrderType.march) {
            voteInAreaForOrder.forbidOrder(forbiddenOrder.mainVariant());
        }
        if (numStars == 0) {
            voteInAreaForOrder.forbidOrder(Order.consolidatePowerS);
        }

        // Заполняем схему приказов
        OrderScheme orderScheme = new OrderScheme();
        orderScheme.setOrderLimits(numOfOrderTypes);
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
                int musteredArea = -1;
                for (int area : areasWithStatus.get(AreaStatus.musterCastle)) {
                    vote = voteInAreaForOrder.getOrderVotesInArea(area, Order.consolidatePowerS);
                    numVotesForMuster += vote;
                    if (numVotesForMuster >= trueVote) {
                        orderScheme.addOrderInArea(area, Order.consolidatePowerS);
                        areasToCommand.remove(area);
                        musteredArea = area;
                        numOfOrderTypes.put(OrderType.consolidatePower,
                                numOfOrderTypes.get(OrderType.consolidatePower) - 1);
                        // Если закончились звёзды, то все остальные типы приказов режем нещадно
                        if (--numRestingStars == 0) {
                            cutOrdersWithoutStar();
                        }
                        break;
                    }
                }
                areasWithStatus.get(AreaStatus.musterCastle).remove((Integer) musteredArea);
            }
        }
        // Добавление походов. Первичные области для походов - те, которые больше всех соседствуют с вражескими
        // или же нуждаются в выводе войск, потому что уж слишком столпились
        Set<Integer> primaryMarchAreas = new HashSet<>();
        primaryMarchAreas.addAll(areasWithStatus.get(AreaStatus.invadedPort));
        primaryMarchAreas.addAll(areasWithStatus.get(AreaStatus.outerSea));
        primaryMarchAreas.addAll(areasWithStatus.get(AreaStatus.hopefulSea));
        primaryMarchAreas.addAll(areasWithStatus.get(AreaStatus.musterCastle));
        primaryMarchAreas.addAll(areasWithStatus.get(AreaStatus.innerLand));
        primaryMarchAreas.addAll(areasWithStatus.get(AreaStatus.borderLand));
        if (forbiddenOrder == OrderType.march || numRestingStars == 0) {
            numOfOrderTypes.put(OrderType.march, 2);
        }
        int numRestingMarches = numOfOrderTypes.get(OrderType.march);
        float totalVotes = 0;
        for (int area: primaryMarchAreas) {
            totalVotes += voteInAreaForOrder.getOrderVotesInArea(area, Order.march);
        }
        // Цикл по первичным областям
        while (!primaryMarchAreas.isEmpty() && numRestingMarches > 0) {
            float trueVote = random.nextFloat() * totalVotes;
            float curVote = 0;
            int potentialMarchArea = LittleThings.getRandomElementOfSet(primaryMarchAreas);
            for (int area: primaryMarchAreas) {
                curVote += voteInAreaForOrder.getOrderVotesInArea(area, Order.march);
                if (curVote >= trueVote) {
                    potentialMarchArea = area;
                    break;
                }
            }
            if (random.nextFloat() * voteInAreaForOrder.getOrderVotesInArea(potentialMarchArea, Order.march) >
                    voteInAreaForOrder.getTotalVotesInArea(potentialMarchArea)) {
                // Поход состоялся
                orderScheme.addOrderInArea(potentialMarchArea, Order.march);
                // Если закончились звёзды, то все остальные типы приказов режем безо всякого сожаления
                if (--numRestingStars == 0) {
                    cutOrdersWithoutStar();
                }
            }
            // Вне зависимости от того, состоялся ли поход, удаляем область из списка первичных претендентов
            primaryMarchAreas.remove(potentialMarchArea);
        }
        if (numRestingMarches == 0) {
            voteInAreaForOrder.forbidOrder(Order.march);
        }
        if (orderScheme.getNumOfOrders(Order.march) == 3) {
            numRestingStars -= 1;
        }

        // Оставшиеся приказы разыгрываем кое-как
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
            }
        }
    }

    @Override
    public String useRaven() {
        return MainConstants.RAVEN_SEES_WILDLINGS_CODE;
    }

    @Override
    public boolean leaveWildlingCardOnTop(WildlingCard card) {
        wildlingCardInfo = card;
        System.out.println("Пацаны, я узнал карту одичалых: " + card);
        return true;
    }

    @Override
    public RaidOrderPlayed playRaid() {
        areasWithRaidsOfPlayer = game.getAreasWithRaidsOfPlayer();
        Iterator<Integer> iterator = areasWithRaidsOfPlayer.get(houseNumber).iterator();
        int bestAreaToRaid = -1;
        int raidPrice = 0;
        Order orderDestination;
        if (iterator.hasNext()) {
            int areaOfRaid = iterator.next();
            HashSet<Integer> adjacentAreas = map.getAdjacentAreas(areaOfRaid);
            for (int destination : adjacentAreas) {
                if (game.getTroopsOwner(destination) == houseNumber || game.getTroopsOwner(destination) < 0) continue;
                orderDestination = game.getOrderInArea(destination);
                if (orderDestination == null || orderDestination.orderType() == OrderType.march ||
                        orderDestination.orderType() == OrderType.defence && game.getOrderInArea(areaOfRaid) == Order.raid ||
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
        areasWithMarchesOfPlayer = game.getAreasWithMarchesOfPlayer();
        int numAreasWithMarches = areasWithMarchesOfPlayer.get(houseNumber).size();
        if (numAreasWithMarches == 0) {
            say("Объявляю забастовку: нет у меня никаких походов!");
            return null;
        }
        boolean allVariantsConsidered = false;
        HashSet<MarchOrderPlayed> marches = new HashSet<>();
        // Случайно выбираем поход, который собираемся разыграть
        int areaFrom = LittleThings.getRandomElementOfSet(areasWithMarchesOfPlayer.get(houseNumber));
        ArrayList<Integer> areasToMove = new ArrayList<>();
        areasToMove.addAll(game.getAccessibleAreas(areaFrom, houseNumber));
        HashMap<Integer, Boolean> isBattleBeginInArea = new HashMap<>();
        // Удаление лишних областей
        ArrayList<Integer> areasToRemove = new ArrayList<>();
        for (int area: areasToMove) {
            if (map.getAreaType(area) == AreaType.port && game.getTroopsOwner(area) == houseNumber) {
                areasToRemove.add(area);
                continue;
            }
            isBattleBeginInArea.put(area, game.isBattleBeginInArea(area, houseNumber));
        }
        areasToMove.removeAll(areasToRemove);

        game.printAreasInCollection(areasToMove, "Возможные области для похода " + map.getAreaNameRusGenitive(areaFrom));
        areasToMove.add(-1);
        isBattleBeginInArea.put(-1, false);
        ArrayList<Unit> myUnits = game.getArmyInArea(areaFrom).getHealthyUnits();
        boolean isLeaveToken = !map.getAreaType(areaFrom).isNaval() && game.getNumPowerTokensHouse(houseNumber) > 0 &&
                game.getPowerTokenInArea(areaFrom) < 0;
        // Если в каком-то из вариантов походов побеждается нейтральный гарнизон, то выбор вариантов сужается.
        boolean isNeutralGarrisonDefeated = false;
        // Возня с меняющимися индексами
        int armySize = myUnits.size();
        int numAreasToMove = areasToMove.size();
        int leadIndex;
        int[] indexes = new int[armySize];

        while (!allVariantsConsidered) {
            // Проверка на количество начинающихся битв
            int nBattle = 0;
            int battleArea = -1;
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
                if (game.supplyTestForMarch(march)) {
                    // Если гарнизона нет, то добавляем вариант похода
                    if ((battleArea < 0 || !game.isNeutralGarrisonInArea(battleArea)) && !isNeutralGarrisonDefeated) {
                        marches.add(march);
                        System.out.println(march.toString() + ": " + march.hashCode());
                    } else {
                        // Если гарнизон есть, и мы его пробиваем, то всё ещё лучше
                        if (battleArea >= 0 && game.isNeutralGarrisonInArea(battleArea) &&
                                game.calculatePowerOfPlayerVersusGarrison(houseNumber, battleArea,
                                        destinationsOfMarch.get(battleArea), game.getOrderInArea(areaFrom).getModifier()) >=
                                        game.getGarrisonInArea(battleArea)) {
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
                    indexes[index] = 0;
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
        if (houseNumber == battleInfo.getAttacker()) {
            return SideOfBattle.attacker;
        } else if (houseNumber == battleInfo.getDefender()) {
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
            isCardActive[curCard] = game.isCardActive(houseNumber, curCard);
            if (isCardActive[curCard]) nActiveCards++;
        }
        int curActiveCardIndex = 0;
        int neededActiveCardIndex = random.nextInt(nActiveCards);
        for (int curCard = 0; curCard < MainConstants.NUM_HOUSE_CARDS; curCard++) {
            if (isCardActive[curCard]) {
                if (curActiveCardIndex == neededActiveCardIndex) {
                    return curCard;
                } else {
                    curActiveCardIndex++;
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
        int enemy = battleInfo.getAttacker() == houseNumber ? battleInfo.getDefender() : battleInfo.getAttacker();
        int[] pos = new int[NUM_TRACK];
        int bestPos = NUM_PLAYER;
        int bestTrack = TrackType.raven.getCode();
        for (int track = NUM_TRACK - 1; track >= 0; track--) {
            pos[track] = game.getInfluenceTrackPlaceForPlayer(track, enemy);
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
        game.printAreasInCollection(possibleAreas, "Области для удаления вражеского приказа бабкой");
        return LittleThings.getRandomElementOfSet(possibleAreas);
    }

    @Override
    public boolean useSword(BattleInfo battleInfo) {
        if (battleInfo.getAttacker() == houseNumber) {
            return battleInfo.getStrengthOnSide(SideOfBattle.attacker) + 1 ==
                   battleInfo.getStrengthOnSide(SideOfBattle.defender);
        } else {
            assert(battleInfo.getDefender() == houseNumber);
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
        game.printAreasInCollection(possibleAreas, "Области для удаления вражеского приказа Серсеей");
        return LittleThings.getRandomElementOfSet(possibleAreas);
    }

    @Override
    public int chooseCardPatchface(int enemy) {
        for (int curCard = 0; curCard < NUM_HOUSE_CARDS; curCard++) {
            if (game.isCardActive(enemy, curCard)) {
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
        int numTokens = game.getNumPowerTokensHouse(houseNumber);
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
        return new int[]{0, 1, 2, 3, 4, 5};
    }

    @Override
    public int wildlingBid(int strength) {
        int bet = random.nextInt(10);
        if (bet < 4) {
            return 0;
        } else if (bet < 9) {
            return 1;
        }
        return Math.min(2, game.getNumPowerTokensHouse(houseNumber));
    }

    @Override
    public int kingChoiceWildlings(WildlingCard card, ArrayList<Integer> pretenders, boolean isBidTop){
        if (pretenders.contains(houseNumber)) {
            if (isBidTop) {
                return houseNumber;
            } else {
                int bet = random.nextInt(pretenders.size() - 1);
                int selfIndex = pretenders.indexOf(houseNumber);
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
        PlayerUtils playerUtils = PlayerUtils.getInstance();
        areasWithTroopsOfPlayer = game.getAreasWithTroopsOfPlayer(houseNumber);
        HashMap<Integer, DummyArmy> armyInArea = playerUtils.makeDummyArmies(houseNumber);
        DisbandPlayed disbandVariant = new DisbandPlayed();
        int numDisbands;
        if (reason == DisbandReason.hordeCastle) {
            Set<Integer> disbandAreas = new HashSet<>();
            for (int area : areasWithTroopsOfPlayer) {
                if (map.getNumCastle(area) > 0 || armyInArea.get(area).getSize() >= 2) {
                    disbandAreas.add(area);
                }
            }
            int area = LittleThings.getRandomElementOfSet(disbandAreas);
            DummyArmy army = armyInArea.get(area);
            for (numDisbands = reason.getNumDisbands(); numDisbands > 0; numDisbands--) {
                UnitType unit = army.getWeakestUnit();
                army.removeUnit(unit);
                disbandVariant.addDisbandedUnit(area, unit);
            }
            return disbandVariant;
        }
        if (reason != DisbandReason.supply) {
            numDisbands = reason.getNumDisbands();
            // Сначала снимаем "халяву": пешек и корабли, в областях с которыми уже есть юниты
            HashMap<Integer, Integer> numDisbandSwipe = PlayerUtils.getNumDisbandSwipe(armyInArea);
            while (numDisbands > 0 && numDisbandSwipe.size() > 0) {
                int area = LittleThings.getRandomElementOfSet(numDisbandSwipe.keySet());
                UnitType disbandedUnit = map.getAreaType(area) == AreaType.land ? UnitType.pawn : UnitType.ship;
                disbandVariant.addDisbandedUnit(area, disbandedUnit);
                armyInArea.get(area).removeUnit(disbandedUnit);
                if (numDisbandSwipe.get(area) > 1) {
                    numDisbandSwipe.put(area, numDisbandSwipe.get(area) - 1);
                } else {
                    numDisbandSwipe.remove(area);
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
            return disbandVariant;
        }
        return disbandVariant;
    }

    @Override
    public UnitExecutionPlayed crowKillersLoseDecision(int numKnightsToDowngrade) {
        UnitExecutionPlayed targetAreas = new UnitExecutionPlayed();
        HashMap<Integer, Integer> numKnightInArea = game.getNumUnitsOfTypeInArea(houseNumber, UnitType.knight);
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
        HashMap<Integer, Integer> numKnightInArea = game.getNumUnitsOfTypeInArea(houseNumber, UnitType.knight);
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
        HashMap<Integer, Integer> numPawnInArea = game.getNumUnitsOfTypeInArea(houseNumber, UnitType.pawn);
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
            if (game.isCardActive(houseNumber, curCard)) {
                return curCard;
            }
        }
        return -1;
    }

    @Override
    public int mammothRidersTopDecision() {
        for (int curCard = 0; curCard < NUM_HOUSE_CARDS; curCard++) {
            if (!game.isCardActive(houseNumber, curCard)) {
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
            myPlaces[i] = game.getInfluenceTrackPlayerOnPlace(i, houseNumber);
            if (myPlaces[i] >= bottomPlace) {
                worseTrack = i;
                bottomPlace = myPlaces[i];
            }
        }
        return myPlaces[TrackType.raven.getCode()] >= 4 ? TrackType.raven : TrackType.getTrack(worseTrack);
    }

    @Override
    public TrackType aKingBeyondTheWallLoseDecision() {
        int swordPlace =  game.getInfluenceTrackPlayerOnPlace(TrackType.valyrianSword.getCode(), houseNumber);
        int ravenPlace =  game.getInfluenceTrackPlayerOnPlace(TrackType.raven.getCode(), houseNumber);
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
            myPlaces[i] = game.getInfluenceTrackPlayerOnPlace(i, houseNumber);
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

    protected void say(String text) {
        game.say(MainConstants.HOUSE[houseNumber] + ": " + text);
    }

    protected MusterPlayed musterInArea(int castleArea) {
        long time = System.currentTimeMillis();
        int numMusterPoints = map.getNumCastle(castleArea);
        if (numMusterPoints == 0) {
            say("Объявляю забастовку: очков сбора войск нету у меня!");
            return null;
        }
        HashSet<Integer> normNavalAreas = new HashSet<>();
        HashSet<Integer> mustHaveNavalAreas = new HashSet<>();
        if (game.getRestingUnitsOfType(houseNumber, UnitType.ship) > 0) {
            HashSet<Integer> adjacentAreas = map.getAdjacentAreas(castleArea);
            for (int area: adjacentAreas) {
                if (map.getAreaType(area) == AreaType.sea && game.getTroopsOwner(area) < 0) {
                    mustHaveNavalAreas.add(area);
                } else if (map.getAreaType(area) == AreaType.sea && game.getTroopsOwner(area) == houseNumber ||
                        map.getAreaType(area) == AreaType.port &&
                                (game.getTroopsOwner(area) < 0 ||
                                        game.getTroopsOwner(map.getSeaNearPort(area)) >= 0 &&
                                                game.getTroopsOwner(map.getSeaNearPort(area)) != houseNumber)){
                    normNavalAreas.add(area);
                }
            }
        }
        MusterPlayed template = new MusterPlayed(castleArea);
        int curAvailableShips = game.getRestingUnitsOfType(houseNumber, UnitType.ship);
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
        System.out.println("Сбор " + HOUSE_GENITIVE[houseNumber] + ": " + (System.currentTimeMillis() - time) + " мс");
        return LittleThings.getRandomElementOfSet(musterVariants);
    }

    protected HashSet<MusterPlayed> getMusterVariants(int castleArea, HashSet<Integer> navalAreas,
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
                    if (game.getRestingUnitsOfType(houseNumber, UnitType.pawn) > 0) {
                        muster = new MusterPlayed(template);
                        muster.addNewMusterable(castleArea, UnitType.pawn);
                        addNewMusterVariant(musterVariants, muster);
                    }
                    // Апгрейд
                    if (game.getArmyInArea(castleArea).hasUnitOfType(UnitType.pawn)) {
                        if (game.getRestingUnitsOfType(houseNumber, UnitType.knight) > 0) {
                            muster = new MusterPlayed(template);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToKnight);
                            musterVariants.add(muster);
                        }
                        if (game.getRestingUnitsOfType(houseNumber, UnitType.siegeEngine) > 0) {
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
                    if (game.getRestingUnitsOfType(houseNumber, UnitType.knight) > 0) {
                        muster = new MusterPlayed(template);
                        muster.addNewMusterable(castleArea, UnitType.knight);
                        addNewMusterVariant(musterVariants, muster);
                    }
                    // Вариант "Башня"
                    if (game.getRestingUnitsOfType(houseNumber, UnitType.siegeEngine) > 0) {
                        muster = new MusterPlayed(template);
                        muster.addNewMusterable(castleArea, UnitType.siegeEngine);
                        addNewMusterVariant(musterVariants, muster);
                    }
                    // Вариант "Две пешки"
                    if (game.getRestingUnitsOfType(houseNumber, UnitType.pawn) > 1) {
                        muster = new MusterPlayed(template);
                        muster.addNewMusterable(castleArea, UnitType.pawn);
                        muster.addNewMusterable(castleArea, UnitType.pawn);
                        addNewMusterVariant(musterVariants, muster);
                    }
                    // Варианты "Два апгрейда"
                    if (game.getArmyInArea(castleArea).getNumUnitOfType(UnitType.pawn) > 1) {
                        if (game.getRestingUnitsOfType(houseNumber, UnitType.siegeEngine) > 1) {
                            muster = new MusterPlayed(template);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToSiege);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToSiege);
                            musterVariants.add(muster);
                        }
                        if (game.getRestingUnitsOfType(houseNumber, UnitType.knight) > 1) {
                            muster = new MusterPlayed(template);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToKnight);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToKnight);
                            musterVariants.add(muster);
                        }
                        if (game.getRestingUnitsOfType(houseNumber, UnitType.siegeEngine) > 0 &&
                                game.getRestingUnitsOfType(houseNumber, UnitType.knight) > 0) {
                            muster = new MusterPlayed(template);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToSiege);
                            muster.addNewMusterable(castleArea, PawnPromotion.pawnToKnight);
                            musterVariants.add(muster);
                        }
                    }
                    // Вариант "Пешка + корабль"
                    if (game.getRestingUnitsOfType(houseNumber, UnitType.pawn) > 0 &&
                            curAvailableShips > 0) {
                        for (int area : navalAreas) {
                            muster = new MusterPlayed(template);
                            muster.addNewMusterable(castleArea, UnitType.pawn);
                            muster.addNewMusterable(area, UnitType.ship);
                            addNewMusterVariant(musterVariants, muster);
                        }
                    }
                    // Варианты "Апгрейд + корабль"
                    if (game.getArmyInArea(castleArea).hasUnitOfType(UnitType.pawn)) {
                        if (game.getRestingUnitsOfType(houseNumber, UnitType.knight) > 0 && curAvailableShips > 0) {
                            for (int area : navalAreas) {
                                muster = new MusterPlayed(template);
                                muster.addNewMusterable(castleArea, PawnPromotion.pawnToKnight);
                                muster.addNewMusterable(area, UnitType.ship);
                                addNewMusterVariant(musterVariants, muster);
                            }
                        }
                        if (game.getRestingUnitsOfType(houseNumber, UnitType.siegeEngine) > 0 && curAvailableShips > 0) {
                            for (int area : navalAreas) {
                                muster = new MusterPlayed(template);
                                muster.addNewMusterable(castleArea, PawnPromotion.pawnToSiege);
                                muster.addNewMusterable(area, UnitType.ship);
                                addNewMusterVariant(musterVariants, muster);
                            }
                        }
                    }
                    // Варианты "Два корабля"
                    if (game.getRestingUnitsOfType(houseNumber, UnitType.ship) > 1) {
                        for (int area1 : navalAreas) {
                            for (int area2: navalAreas) {
                                // Если море рядом с портом наше, то нет смысла строить в порту 2 корабля
                                if (map.getAreaType(area1) == AreaType.port && area1 == area2 &&
                                        game.getTroopsOwner(map.getSeaNearPort(area1)) == houseNumber) {
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

    protected void addNewMusterVariant(HashSet<MusterPlayed> set, MusterPlayed muster) {
        if (game.supplyTestForMuster(muster)) {
            set.add(muster);
        }
    }

    private void fillFirstTurnOrderMap() {
        orderMap.clear();
        switch (houseNumber) {
            case 0:
                orderMap.put(8, Order.support);
                orderMap.put(9, Order.support);
                orderMap.put(53, Order.consolidatePower);
                orderMap.put(56, Order.marchS);
                break;
            case 1:
                orderMap.put(3, Order.marchS);
                orderMap.put(37, Order.consolidatePower);
                orderMap.put(36, Order.consolidatePowerS);
                break;
            case 2:
                orderMap.put(11, Order.marchS);
                orderMap.put(25, Order.consolidatePower);
                orderMap.put(21, Order.consolidatePowerS);
                break;
            case 3:
                orderMap.put(7, Order.marchS);
                orderMap.put(47, Order.consolidatePower);
                orderMap.put(48, Order.consolidatePowerS);
                orderMap.put(52, Order.defence);
                break;
            case 4:
                orderMap.put(2, Order.marchB);
                orderMap.put(13, Order.consolidatePower);
                orderMap.put(33, Order.consolidatePower);
                orderMap.put(57, Order.march);
                break;
            case 5:
                orderMap.put(5, Order.marchB);
                orderMap.put(43, Order.consolidatePower);
                orderMap.put(41, Order.march);
        }
    }
}
