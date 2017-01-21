package com.alexeus.ai;

import com.alexeus.logic.constants.MainConstants;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.*;
import com.alexeus.logic.struct.*;
import com.alexeus.map.GameOfThronesMap;

import java.util.*;

import static com.alexeus.logic.constants.MainConstants.HOUSE_GENITIVE;

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

    // здесь будут храниться области, в которых есть войска игроков
    protected ArrayList<HashMap<Integer, Integer>> areasWithTroopsOfPlayer;

    // здесь будут храниться области, в которых есть набеги игроков
    protected ArrayList<HashSet<Integer>> areasWithRaidsOfPlayer;

    // здесь будут храниться области, в которых есть походы игроков
    protected ArrayList<HashSet<Integer>> areasWithMarchesOfPlayer;

    // здесь будут храниться области, в которых есть сборы власти игрков
    protected ArrayList<HashSet<Integer>> areasWithCPsOfPlayer;

    // здесь будут храниться приказы, которые игрок отдаёт в фазе планирования
    protected HashMap<Integer, Order> orderMap;

    public PrimitivePlayer(Game newGame, int houseNumber) {
        game = newGame;
        random = new Random();
        map = game.getMap();
        this.houseNumber = houseNumber;
        orderMap = new HashMap<>();
    }

    @Override
    public void nameYourself() {
        say("Я - Примитивный игрок за " + HOUSE_GENITIVE[houseNumber] + "!");
    }

    @Override
    public HashMap<Integer, Order> giveOrders() {
        areasWithTroopsOfPlayer = game.getAreasWithTroopsOfPlayer();
        Set<Integer> myAreas = areasWithTroopsOfPlayer.get(houseNumber).keySet();
        orderMap.clear();
        switch (houseNumber) {
            case 0:
                orderMap.put(8, Order.march);
                orderMap.put(53, Order.consolidatePower);
                orderMap.put(56, Order.consolidatePowerS);
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
        /* for (Integer area : myAreas) {
            orderMap.put(area, Order.getOrderWithCode(random.nextInt(MainConstants.NUM_DIFFERENT_ORDERS)));
        }*/
        return orderMap;
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
        int nMarches = areasWithMarchesOfPlayer.get(houseNumber).size();
        if (nMarches == 0) {
            say("Объявляю забастовку: нет у меня никаких походов!");
            return null;
        }
        int indexMarchToPlay = random.nextInt(nMarches);
        int curIndexMarch = 0;
        MarchOrderPlayed march = new MarchOrderPlayed();
        for (Integer areaFrom: areasWithMarchesOfPlayer.get(houseNumber)) {
            if (curIndexMarch != indexMarchToPlay) {
                curIndexMarch++;
            } else {
                march.setAreaFrom(areaFrom);
                march.setLeaveToken(!map.getAreaType(areaFrom).isNaval() && game.getNumPowerTokensHouse(houseNumber) > 0);
                Army myArmy = game.getArmyInArea(areaFrom);
                HashSet<Integer> areasToMove = game.getAccessibleAreas(areaFrom, houseNumber);
                game.printAreasInSet(areasToMove, "Возможные области для похода " + map.getAreaNameRusGenitive(areaFrom));

                areasToMove.add(-1);
                HashMap<Integer, Army> destinationsOfMarch = new HashMap<>();
                // Пытаемся подобрать случайные места назначения, пока не будет успешный тест на снабжение
                do {
                    destinationsOfMarch.clear();
                    for (Unit unit: myArmy.getUnits()) {
                        if (unit.isWounded()) continue;
                        int area;
                        int nTry = 0;
                        do {
                            area = getRandomElementOfSet(areasToMove);
                            nTry++;
                        } while (/* area >= 0 && game.getGarrisonInArea(area) > 0 ||*/ area < 0 && nTry == 1);
                        if (area == -1) continue;
                        if (destinationsOfMarch.containsKey(area)) {
                            destinationsOfMarch.get(area).addUnit(unit);
                        } else {
                            destinationsOfMarch.put(area, new Army(unit, game));
                        }
                    }
                    march.setDestinationsOfMarch(destinationsOfMarch);
                } while (!game.supplyTestForMarch(march));

                return march;
            }
        }
        return null;
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
        boolean[] isCardActive = new boolean[MainConstants.NUM_HOUSE_CARDS];
        int nActiveCards = 0;
        for (int curCard = 0; curCard < MainConstants.NUM_HOUSE_CARDS; curCard++) {
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
    public TrackType chooseInfluenceTrackDoran(BattleInfo battleInfo) {
        int enemy = battleInfo.getAttacker() == houseNumber ? battleInfo.getDefender() : battleInfo.getAttacker();
        int[] pos = new int[3];
        pos[0] = game.getInfluenceTrackPlaceForPlayer(enemy, TrackType.ironThrone);
        pos[1] = game.getInfluenceTrackPlaceForPlayer(enemy, TrackType.valyrianSword);
        pos[2] = game.getInfluenceTrackPlaceForPlayer(enemy, TrackType.raven);
        int bestPos = Math.min(Math.min(pos[0], pos[1]), pos[2]);
        if (bestPos == pos[2]) {
            return TrackType.raven;
        } else if (bestPos == pos[1]) {
            return TrackType.valyrianSword;
        } else {
            return TrackType.ironThrone;
        }
    }

    @Override
    public boolean useAeron(BattleInfo battleInfo) {
        return true;
    }

    @Override
    public int chooseAreaQueenOfThorns(HashSet<Integer> possibleAreas) {
        game.printAreasInSet(possibleAreas, "Области для удаления вражеского приказа бабкой");
        return getRandomElementOfSet(possibleAreas);
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
        return getRandomElementOfSet(possibleAreas);
    }

    @Override
    public int areaToUseRenly(HashSet<Integer> possibleAreas) {
        return getRandomElementOfSet(possibleAreas);
    }

    @Override
    public int chooseAreaCerseiLannister(HashSet<Integer> possibleAreas) {
        game.printAreasInSet(possibleAreas, "Области для удаления вражеского приказа Серсеей");
        return getRandomElementOfSet(possibleAreas);
    }

    @Override
    public int chooseCardPatchface(int enemy) {
        for (int curCard = 0; curCard < MainConstants.NUM_HOUSE_CARDS; curCard++) {
            if (game.isCardActive(enemy, curCard)) {
                return curCard;
            }
        }
        return -1;
    }

    @Override
    public MusterPlayed playConsolidatePowerS(int castleArea) {
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
        return getRandomElementOfSet(musterVariants);
    }

    @Override
    public MusterPlayed muster(HashSet<Integer> castleAreas) {
        return null;
    }

    @Override
    public int eventToChoose(int deckNumber) {
        return random.nextInt(3);
    }

    @Override
    public int bid(int track) {
        return 0;
    }

    @Override
    public int wildlingBid() {
        // Необходимо сбросить информацию о верхней карте одичалых, добытую посыльным вороном, если таковая имелась.
        wildlingCardInfo = null;
        return 0;
    }

    @Override
    public int kingChoiceTop(int pretenders) {
        return 0;
    }

    @Override
    public int kingChoiceBottom(int pretenders) {
        return 0;
    }

    @Override
    public String kingChoiceInfluenceTrack(int track, int[] bids) {
        return null;
    }

    @Override
    public String disbanding(DisbandReason reason) {
        return null;
    }

    @Override
    public String crowKillersLoseDecision() {
        return null;
    }

    @Override
    public String crowKillersTopDecision() {
        return null;
    }

    @Override
    public String massingOnTheMilkwaterTopDecision() {
        return null;
    }

    @Override
    public String massingOnTheMilkwaterLoseDecision() {
        return null;
    }

    @Override
    public String aKingBeyondTheWallTopDecision() {
        return null;
    }

    @Override
    public String aKingBeyondTheWallLoseDecision() {
        return null;
    }

    @Override
    public String preemptiveRaidBottomDecision() {
        return null;
    }

    protected void say(String text) {
        game.say(MainConstants.HOUSE[houseNumber] + ": " + text);
    }

    /**
     * Метод возвращает случайный элемент множества
     * @param set множество
     * @return случайный элемент
     */
    protected <T> T getRandomElementOfSet(Set<T> set) {
        int size = set.size();
        int curIndex = 0;
        int needIndex = random.nextInt(size);
        for (T element: set) {
            if (curIndex == needIndex) {
                return element;
            } else {
                curIndex++;
            }
        }
        return null;
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
                    // Вариант "конь"
                    if (game.getRestingUnitsOfType(houseNumber, UnitType.knight) > 0) {
                        muster = new MusterPlayed(template);
                        muster.addNewMusterable(castleArea, UnitType.knight);
                        addNewMusterVariant(musterVariants, muster);
                    }
                    // Вариант "башня"
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
                    // Варианты "два апгрейда"
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
                    // Варианты "2 корабля"
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
}
