package com.alexeus.ai;

import com.alexeus.logic.constants.MainConstants;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.*;
import com.alexeus.logic.struct.*;
import com.alexeus.map.GameOfThronesMap;

import java.util.*;

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
    protected ArrayList<HashSet<Integer>> areasWithTroopsOfPlayer;

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
    public String nameYourself() {
        return "Примитивный игрок за " + MainConstants.HOUSE_GENITIVE[houseNumber];
    }

    @Override
    public HashMap<Integer, Order> giveOrders() {
        areasWithTroopsOfPlayer = game.getAreasWithTroopsOfPlayer();
        HashSet<Integer> myAreas = areasWithTroopsOfPlayer.get(houseNumber);
        orderMap.clear();
        switch (houseNumber) {
            case 0:
                orderMap.put(8, Order.march);
                orderMap.put(53, Order.marchB);
                orderMap.put(56, Order.marchS);
                break;
            case 1:
                orderMap.put(3, Order.marchS);
                orderMap.put(37, Order.marchB);
                orderMap.put(36, Order.march);
                break;
            case 2:
                orderMap.put(11, Order.marchS);
                orderMap.put(25, Order.marchB);
                orderMap.put(21, Order.march);
                break;
            case 3:
                orderMap.put(7, Order.marchS);
                orderMap.put(47, Order.marchB);
                orderMap.put(48, Order.march);
                break;
            case 4:
                orderMap.put(2, Order.support);
                orderMap.put(13, Order.consolidatePower);
                orderMap.put(33, Order.marchB);
                orderMap.put(57, Order.march);
                break;
            case 5:
                orderMap.put(5, Order.support);
                orderMap.put(43, Order.marchB);
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
        System.out.println(MainConstants.HOUSE[houseNumber] + ": Пацаны, я узнал карту одичалых: " + card);
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
            System.out.println(MainConstants.HOUSE[houseNumber] + " объявляет забастовку: нет у него никаких набегов!");
            return null;
        }
    }

    @Override
    public MarchOrderPlayed playMarch() {
        areasWithMarchesOfPlayer = game.getAreasWithMarchesOfPlayer();
        int nMarches = areasWithMarchesOfPlayer.get(houseNumber).size();
        if (nMarches == 0) {
            System.out.println(MainConstants.HOUSE[houseNumber] + " объявляет забастовку: нет у него никаких походов!");
            return null;
        }
        int indexMarchToPlay = random.nextInt(nMarches);
        int curIndexMarch = 0;
        for (Integer areaFrom: areasWithMarchesOfPlayer.get(houseNumber)) {
            if (curIndexMarch != indexMarchToPlay) {
                curIndexMarch++;
            } else {
                MarchOrderPlayed march = new MarchOrderPlayed();
                march.setAreaFrom(areaFrom);
                Army myArmy = game.getArmyInArea(areaFrom);
                HashSet<Integer> areasToMove = game.getAccessibleAreas(areaFrom, houseNumber);
                game.printAreasInSet(areasToMove, "Возможные области для похода " + map.getAreaNameRusGenitive(areaFrom));

                areasToMove.add(-1);
                HashMap<Integer, Army> destinationsOfMarch = new HashMap<>();
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
                march.setLeaveToken(!map.getAreaType(areaFrom).isNaval() && game.getNumPowerTokensHouse(houseNumber) > 0);
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
    public int chooseAreaQueenOfThorns(HashSet<Integer> possibleVariants) {
        return getRandomElementOfSet(possibleVariants);
    }

    @Override
    public boolean useSword(BattleInfo battleInfo) {
        if (battleInfo.getAttacker() == houseNumber) {
            return battleInfo.getAttackerStrength() + 1 == battleInfo.getDefenderStrength();
        } else {
            assert(battleInfo.getDefender() == houseNumber);
            return battleInfo.getAttackerStrength() == battleInfo.getDefenderStrength() + 1;
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
    public String playConsolidatePower() {
        return "";
    }

    @Override
    public String muster() {
        return "";
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
}
