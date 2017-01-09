package com.alexeus.ai;

import com.alexeus.logic.Constants;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.*;
import com.alexeus.logic.struct.MarchOrderPlayed;
import com.alexeus.logic.struct.RaidOrderPlayed;
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
        return "Примитивный игрок за " + Constants.HOUSE_GENITIVE[houseNumber];
    }

    @Override
    public HashMap<Integer, Order> giveOrders() {
        areasWithTroopsOfPlayer = game.getAreasWithTroopsOfPlayer();
        HashSet<Integer> myAreas = areasWithTroopsOfPlayer.get(houseNumber);
        orderMap.clear();
        if (houseNumber == 1) {
            orderMap.put(3, Order.raid);
            orderMap.put(36, Order.raidS);
            orderMap.put(37, Order.raid);
        } else if (houseNumber == 4) {
            orderMap.put(2, Order.raid);
            orderMap.put(13, Order.consolidatePower);
            orderMap.put(33, Order.consolidatePower);
            orderMap.put(57, Order.march);
        } else {
            for (Integer area : myAreas) {
                orderMap.put(area, Order.getOrderWithCode(random.nextInt(Constants.NUM_DIFFERENT_ORDERS)));
            }
        }
        return orderMap;
    }

    @Override
    public String useRaven() {
        return Constants.RAVEN_SEES_WILDLINGS_CODE;
    }

    @Override
    public boolean leaveWildlingCardOnTop(WildlingCard card) {
        wildlingCardInfo = card;
        System.out.println(Constants.HOUSE[houseNumber] + ": Пацаны, я узнал карту одичалых: " + card);
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
            List<Integer> adjacentAreas = map.getAdjacentAreas(areaOfRaid);
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
            System.out.println(Constants.HOUSE[houseNumber] + " объявляет забастовку: нет у него никаких набегов!");
            return null;
        }
    }

    @Override
    public MarchOrderPlayed playMarch() {
        areasWithMarchesOfPlayer = game.getAreasWithMarchesOfPlayer();
        Iterator<Integer> iterator = areasWithRaidsOfPlayer.get(houseNumber).iterator();
        if (iterator.hasNext()) {
            int areaOfMarch = iterator.next();
            return null;
        } else {
            System.out.println(Constants.HOUSE[houseNumber] + " объявляет забастовку: нет у него никаких походов!");
            return null;
        }
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
    public boolean useSword() {
        return false;
    }

    @Override
    public int eventToChoose(int deckNumber) {
        return random.nextInt(3);
    }

    @Override
    public int playHouseCard() {
        return 0;
    }

    @Override
    public boolean useRenly() {
        return true;
    }

    @Override
    public int chooseCardPatchface() {
        return 0;
    }

    @Override
    public boolean useTyrion() {
        return true;
    }

    @Override
    public int chooseAreaCercei() {
        return -1;
    }

    @Override
    public int chooseInfluenceTrackDoran() {
        return random.nextInt(3);
    }

    @Override
    public boolean useAeron() {
        return false;
    }

    @Override
    public boolean chooseAreaQueenOfThorns() {
        return false;
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
}
