package com.alexeus.logic;

import com.alexeus.ai.GotPlayerInterface;
import com.alexeus.ai.PrimitivePlayer;
import com.alexeus.control.Controller;
import com.alexeus.control.Settings;
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

    private GameModel model;

    private GameOfThronesMap map;

    private Random random;

    private GotPlayerInterface[] playerInterface;

    private HouseCard[] houseCardOfSide = new HouseCard[2];

    /*
     * **** Вспомогательные переменные для методов ****
     */

    private HashSet<Integer> accessibleAreaSet = new HashSet<>();

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
        model = new GameModel();
        random = new Random();
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
        playerInterface = new PrimitivePlayer[NUM_PLAYER];
        for (int i = 0; i < NUM_PLAYER; i++) {
            playerInterface[i] = new PrimitivePlayer(this, i);
        }
        model.prepareNewGame();
        chat.setText("");
        eventTabPanel.repaint();
        fightTabPanel.repaint();
        houseTabPanel.repaint();
        say(NEW_GAME_BEGINS);
        for (int player = 0; player < NUM_PLAYER; player++) {
            playerInterface[player].nameYourself();
        }
    }

    public void getPlans() {
        say(ROUND_NUMBER + Controller.getInstance().getTime() + ". " + GamePhase.planningPhase);
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.setSecrecy(true);
        }
        mapPanel.repaint();
        if (Settings.getInstance().isTrueAutoSwitchTabs()) {
            tabPanel.setSelectedIndex(TabEnum.chat.getCode());
        }
        HashMap<Integer, Order> orders;
        Order curOrder;
        for (int player = 0; player < NUM_PLAYER; player++) {
            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                orders = playerInterface[player].giveOrders();
                if (model.validateOrders(orders, player)) {
                    // Успех: сохраняем приказы и заполняем вспомогательные множества
                    for (Integer area: orders.keySet()) {
                        curOrder = orders.get(area);
                        model.safeSetOrderInArea(area, curOrder, player);
                    }
                    break;
                }
            }
        }
        controlPoint(PLAYERS + GIVE_ORDERS);
        // Ставки на ворону уже достаточно покрасовались, пора и честь знать
        model.setCurrentBidTrack(-1);
    }

    /**
     * Метод разруливает ситуацию после фазы замыслов, когда владелец посыльного ворона может
     * поменять один свой приказ или посмотреть верхнюю карту одичалых
     */
    public void getRavenDecision() {
        say(ROUND_NUMBER + Controller.getInstance().getTime() + ". " + GamePhase.ravenPhase);
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.setSecrecy(false);
        }
        if (Settings.getInstance().isTrueAutoSwitchTabs() && tabPanel.getSelectedIndex() != TabEnum.chat.getCode()) {
            tabPanel.setSelectedIndex(TabEnum.chat.getCode());
        }
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaint();
        }
        int ravenHolder = model.getTrackPlayerOnPlace(TrackType.raven.getCode(), 0);
        for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
            String ravenUse = playerInterface[ravenHolder].useRaven();
            // Ворононосец выбрал просмотр карты одичалых
            if (ravenUse.equals(RAVEN_SEES_WILDLINGS_CODE)) {
                say(HOUSE[ravenHolder] + SEES_WILDLINGS_CARD);
                controlPoint(HOUSE[ravenHolder] + SEES_WILDLINGS);
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                if (!playerInterface[ravenHolder].leaveWildlingCardOnTop(model.getFirstWildlingsCard())) {
                    model.buryWildlings();
                    for (int player = 0; player < NUM_PLAYER; player++) {
                        playerInterface[player].tellWildlingsBuried();
                    }
                    say(AND_BURIES);
                } else {
                    say(AND_LEAVES);
                }
                break;
            }
            // Ворононосец выбрал замену приказа
            if (ravenUse.charAt(0) >= '0' && ravenUse.charAt(0) <= '9') {
                say(HOUSE[ravenHolder] + CHANGES_ORDER);
                controlPoint(HOUSE[ravenHolder] + CHANGES_ORDER);
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
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
    }

    /**
     * Метод разыгрывает набеги
     */
    public void playRaids() {
        say(ROUND_NUMBER + Controller.getInstance().getTime() + ". " + GamePhase.raidPhase);
        int numNoRaidsPlayers = 0;
        int place = 0;
        int player;
        int attempt;
        while(numNoRaidsPlayers < NUM_PLAYER){
            player = model.getTrackPlayerOnPlace(TrackType.ironThrone.getCode(), place);
            if (!model.hasRaids(player)) {
                numNoRaidsPlayers++;
            } else {
                numNoRaidsPlayers = 0;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    RaidOrderPlayed raid = playerInterface[player].playRaid();
                    // Проверяем вариант розыгрыша набега, полученный от игрока, на валидность
                    if (model.validateRaid(raid, player)) {
                        controlPoint(HOUSE[player] + PLAYS_RAID);
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        model.executeRaid(raid);
                        break;
                    }
                }
                // Если игрок не уложился в количество попыток, то удаляем все его приказы набегов
                if (attempt >= MAX_TRIES_TO_GO) {
                    say(HOUSE[player] + FAILED_TO_PLAY_RAID);
                    model.removeAllRaidsOfPlayer(player);
                }
            }
            place++;
            if (place == NUM_PLAYER) place = 0;
        }
    }

    /**
     * Метод разыгрывает походы
     */
    public void playMarches() {
        say(ROUND_NUMBER + Controller.getInstance().getTime() + ". " + GamePhase.marchPhase);
        int numNoMarchesPlayers = 0;
        int place = 0;
        int player;
        int attempt;
        model.printAreaWithTroopsOfPlayers();
        while(numNoMarchesPlayers < NUM_PLAYER){
            player = model.getTrackPlayerOnPlace(TrackType.ironThrone.getCode(), place);
            if (!model.hasMarches(player)) {
                numNoMarchesPlayers++;
            } else {
                numNoMarchesPlayers = 0;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    MarchOrderPlayed march = playerInterface[player].playMarch();
                    // Проверяем вариант розыгрыша похода, полученный от игрока, на валидность
                    if (model.validateMarch(march, player)) {
                        controlPoint(HOUSE[player] + PLAYS_MARCH);
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        int from = march.getAreaFrom();
                        HashMap<Integer, ArrayList<UnitType>> destinationsOfMarch = march.getDestinationsOfMarch();
                        if (destinationsOfMarch.size() == 0) {
                            // Случай "холостого" снятия приказа похода
                            say(HOUSE[player] + DELETES_MARCH + map.getAreaNameRusGenitive(from));
                            model.safeDeleteOrderInArea(from, player);
                            if (Settings.getInstance().isTrueAutoSwitchTabs() &&
                                    tabPanel.getSelectedIndex() == TabEnum.fight.getCode()) {
                                tabPanel.setSelectedIndex(TabEnum.chat.getCode());
                            }
                        } else {
                            // Случай результативного похода: перемещаем юниты, если нужно, начинаем бой,
                            // и обновляем попутные переменные
                            for (Map.Entry<Integer, ArrayList<UnitType>> entry: destinationsOfMarch.entrySet()) {
                                int curDestination = entry.getKey();
                                ArrayList<UnitType> curUnits = entry.getValue();
                                int destinationArmyOwner = model.getTroopsOrGarrisonOwner(curDestination);
                                if (destinationArmyOwner >= 0 && destinationArmyOwner != player ||
                                        model.getHouseHomeLandInArea(curDestination) >= 0 &&
                                        model.getHouseHomeLandInArea(curDestination) != player &&
                                                model.getGarrisonInArea(curDestination) > 0) {
                                    // Если в области завязывается бой, то запоминаем её и проходим последней
                                    model.setAreaWhereBattleBegins(curDestination);
                                    model.moveAttackingUnits(curUnits, from);
                                } else {
                                    // Иначе перемещаем войска и пробиваем гарнизоны
                                    say(LittleThings.unitTypesToString(curUnits) +
                                            (curUnits.size() == 1 ? MOVES_TO : MOVE_TO) +
                                            map.getAreaNameRusAccusative(curDestination) + ".");
                                    int destinationOwner = model.getAreaOwner(curDestination);

                                    if (destinationOwner >= 0 && destinationOwner != player) {
                                        if (map.getNumCastle(curDestination) > 0) {
                                            model.loseVictoryPoints(destinationOwner);
                                            tryToCaptureShips(curDestination, player);
                                            if (Thread.currentThread().isInterrupted()) {
                                                return;
                                            }
                                        }
                                        model.tryToDeletePowerToken(curDestination);
                                    }
                                    model.moveUnits(curUnits, from, curDestination);
                                }
                            }
                            if (model.getArmyInArea(from).isEmpty() && model.getHouseHomeLandInArea(from) != player &&
                                    model.getPowerTokenInArea(from) < 0) {
                                if (march.getIsLeaveToken()) {
                                    model.leavePowerToken(from, player);
                                } else {
                                    if (map.getNumCastle(from) > 0) {
                                        model.loseVictoryPoints(player);
                                        // Если мы покинули чью-то столицу, не оставив жетона, то столица возвращается
                                        // владельцу, и тот может что-нибудь сделать с кораблями в порту.
                                        if (model.getHouseHomeLandInArea(from) >= 0) {
                                            model.adjustVictoryPoints();
                                            tryToCaptureShips(from, model.getHouseHomeLandInArea(from));
                                        } else {
                                            // Если мы покинули замок с портом, то все корабли в порту немедленно уничтожаются
                                            tryToDestroyNeutralShips(from);
                                        }
                                        if (Thread.currentThread().isInterrupted()) {
                                            return;
                                        }
                                    }
                                }
                            }

                            if (model.getAreaWhereBattleBegins() >= 0) {
                                model.sayPreBattleText();
                                playFight(from);
                                model.setAreaWhereBattleBegins(-1);
                                if (Thread.currentThread().isInterrupted()) {
                                    return;
                                }
                            } else {
                                if (Settings.getInstance().isTrueAutoSwitchTabs() &&
                                        tabPanel.getSelectedIndex() == TabEnum.fight.getCode()) {
                                    tabPanel.setSelectedIndex(TabEnum.chat.getCode());
                                }
                            }
                            model.safeDeleteOrderInArea(from, player);
                        }
                        model.removeAreaWithMarch(from, player);
                        break;
                    }
                }
                // Если игрок не уложился в количество попыток, то удаляем один его приказ похода
                if (attempt >= MAX_TRIES_TO_GO) {
                    say(HOUSE[player] + FAILED_TO_PLAY_MARCH);
                    model.deleteRandomMarchOfPlayer(player);
                } else {
                    model.printAreaWithTroopsOfPlayers();
                }
            }
            place++;
            if (place == NUM_PLAYER) place = 0;
        }
    }

    public void playConsolidatePower() {
        say(ROUND_NUMBER + Controller.getInstance().getTime() + ". " + GamePhase.consolidatePowerPhase);
        int areaWithMuster[] = new int[NUM_PLAYER];
        for (int player = 0; player < NUM_PLAYER; player++) {
            int earning = 0;
            areaWithMuster[player] = -1;
            for (int area: model.getAreasWithCPsOfPlayer().get(player)) {
                if (model.getOrderInArea(area) == Order.consolidatePowerS && map.getNumCastle(area) > 0) {
                    areaWithMuster[player] = area;
                } else {
                    earning += map.getNumCrown(area) + 1;
                    model.setOrderInArea(area, null);
                    if (!Settings.getInstance().isPassByRegime()) {
                        mapPanel.repaintArea(area);
                    }
                }
            }
            if (earning > 0) {
                model.earnTokens(player, earning);
            }
        }
        model.clearAreasWithCP();
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (areaWithMuster[player] >= 0) {
                say(HOUSE[player] + CAN_MUSTER + map.getAreaNameRusLocative(areaWithMuster[player]) + ".");
                int attempt;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    MusterPlayed musterVariant = playerInterface[player].playConsolidatePowerS(areaWithMuster[player]);
                    if (model.validateMuster(musterVariant, player)) {
                        controlPoint(HOUSE[player] + PLAYS_CP);
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        if (Settings.getInstance().isTrueAutoSwitchTabs() &&
                                tabPanel.getSelectedIndex() == TabEnum.fight.getCode()) {
                            tabPanel.setSelectedIndex(TabEnum.chat.getCode());
                        }
                        int nMusteredObjects = musterVariant.getNumberMusterUnits();
                        if (nMusteredObjects > 0) {
                            model.executeMuster(musterVariant, player);
                        } else {
                            model.earnTokens(player, map.getNumCrown(areaWithMuster[player]) + 1);
                        }
                        break;
                    }
                }
                if (attempt >= MAX_TRIES_TO_GO) {
                    model.earnTokens(player, map.getNumCrown(areaWithMuster[player]) + 1);
                }
                model.safeDeleteOrderInArea(areaWithMuster[player], player);
                if (!Settings.getInstance().isPassByRegime()) {
                    houseTabPanel.repaintHouse(player);
                }
            }
        }
    }

    private void tryToCaptureShips(int castleArea, int player) {
        int portArea = map.getPortOfCastle(castleArea);
        if (portArea >= 0) {
            Army armyInPort = model.getArmyInArea(portArea);
            if (!armyInPort.isEmpty()) {
                int numShips = armyInPort.getSize();
                int exOwner = armyInPort.getOwner();
                int numCapturedShips = Math.min(model.getRestingUnits(player, UnitType.ship), numShips);
                model.tryToToWreckSomeShips(portArea, exOwner, numShips - numCapturedShips);
                if (numCapturedShips > 0) {
                    model.captureAllShips(portArea, exOwner, player);
                    say(HOUSE[player] + CAN_CAPTURE_OR_DESTROY_SHIPS);
                    int restShips = playerInterface[player].getNumCapturedShips(portArea);
                    controlPoint(HOUSE[player] + CAPTURES_SHIPS);
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
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
                        model.wreckRestingShips(portArea, player, numCapturedShips - restShips);
                    }
                }
            }
        }
    }

    private void tryToDestroyNeutralShips(int castleArea) {
        int portArea = map.getPortOfCastle(castleArea);
        if (portArea >= 0 && !model.getArmyInArea(portArea).isEmpty()) {
            model.wreckAllShips(portArea);
        }
    }

    /**
     * Метод разыгрывает одно сражение за определённую область. На данный момент атакующая армия должна быть сохранена
     * в attackingArmy, а нападающий игрок может быть определён посредоством attackingArmy.getOwner();
     * @param areaOfMarch область, из которой пришла атака
     */
    private void playFight(int areaOfMarch) {
        // Заполняем переменные битвы
        model.prepareBattle(areaOfMarch);
        BattleInfo battleInfo = model.getBattleInfo();
        int[] playerOnSide = battleInfo.getPlayersOnSides();
        // Учитываем подмоги из соседних областей
        if (model.getForbiddenOrder() != OrderType.support) {
            model.prepareSupportInfo();
            SideOfBattle[] supportOfPlayer = new SideOfBattle[NUM_PLAYER];
            HashSet<Integer> supporters = model.getSupporters();
            SideOfBattle sideOfBattle;
            boolean firstFlag = true;
            StringBuilder sb = new StringBuilder();
            for (int player: model.getSupporters()) {
                if (!firstFlag) sb.append(", ");
                firstFlag = false;
                sb.append(HOUSE[player]);
            }
            if (!supporters.isEmpty()) {
                sb.append(CAN_SUPPORT_SOMEBODY);
                say(sb.toString());
            }

            if (supporters.size() > 0) {
                controlPoint(supporters.size() > 1 ? PLAYERS: HOUSE[supporters.iterator().next()] +
                        (supporters.size() > 1 ? ESTABLISH_SUPPORT: ESTABLISHES_SUPPORT));
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
            for (int player : supporters) {
                for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    sideOfBattle = playerInterface[player].sideToSupport(battleInfo);
                    switch (sideOfBattle) {
                        case attacker:
                            say(HOUSE[player] + SUPPORTS + HOUSE_GENITIVE[battleInfo.getAttacker()] + ".");
                            break;
                        case defender:
                            say(HOUSE[player] + SUPPORTS + HOUSE_GENITIVE[battleInfo.getDefender()] + ".");
                            break;
                        default:
                            say(HOUSE[player] + SUPPORTS_NOBODY);
                    }
                    if (sideOfBattle == SideOfBattle.attacker && player == battleInfo.getDefender() ||
                            sideOfBattle == SideOfBattle.defender && player == battleInfo.getAttacker()) {
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
            model.setSupports(supportOfPlayer);
        }

        // Выбираем карты дома
        controlPoint(PLAYERS + CHOOSE_CARD);
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        for (int side = 0; side < 2; side++) {
            houseCardOfSide[side] = getHouseCard(battleInfo, playerOnSide[side]);
            model.setHouseCardOnSide(side, houseCardOfSide[side]);
        }

        int firstSideOnThrone = model.getTrackPlaceForPlayer(TrackType.ironThrone.getCode(), playerOnSide[0]) <
                model.getTrackPlaceForPlayer(TrackType.ironThrone.getCode(), playerOnSide[1]) ? 0 : 1;
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
                        controlPoint(TYRION);
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        if (useTyrion) {
                            model.useTyrion();
                            controlPoint(HOUSE[playerOnSide[1 - heroSide]] + CHOOSES_CARD);
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                            houseCardOfSide[1 - heroSide] = getHouseCard(battleInfo, playerOnSide[1 - heroSide]);
                            model.setHouseCardOnSide(1 - heroSide, houseCardOfSide[1 - heroSide]);
                        } else {
                            say(houseCardOfSide[heroSide].getName() + NO_EFFECT);
                        }
                        break;
                }
            }
        }
        // Немедленные карты: Мейс, Бабка, Доран и Эйерон
        for (int curSide = 0; curSide < 2; curSide++) {
            int heroSide = (curSide + firstSideOnThrone) % 2;
            if (houseCardOfSide[heroSide].getCardInitiative() == CardInitiative.immediately) {
                switch (houseCardOfSide[heroSide]) {
                    case maceTyrell:
                        model.playMace();
                        break;
                    case queenOfThorns:
                        propertyUsed = false;
                        accessibleAreaSet = model.getQueenOfThornsAreas(playerOnSide[1 - heroSide]);
                        if (!accessibleAreaSet.isEmpty()) {
                            say(HOUSE[playerOnSide[heroSide]] + CAN_USE_SPECIAL_PROPERTY_OF_CARD +
                                    houseCardOfSide[heroSide].getName());
                            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                                int area = playerInterface[playerOnSide[heroSide]].chooseAreaQueenOfThorns(accessibleAreaSet);
                                controlPoint(QUEEN_OF_THORNS);
                                if (Thread.currentThread().isInterrupted()) {
                                    return;
                                }
                                // Если область меньше нуля, значит, игрок отказался использовать свойство карты
                                if (area < 0) {
                                    break;
                                }
                                if (area >= NUM_AREA || !accessibleAreaSet.contains(area)) {
                                    say(INVALID_AREA_ERROR);
                                } else {
                                    propertyUsed = true;
                                    model.playQueenOfThorns(area, 1 - heroSide);
                                    break;
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
                        controlPoint(DORAN);
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        model.playDoran(trackToPissOff, playerOnSide[1 - heroSide]);
                        break;
                    case aeronDamphair:
                        propertyUsed = false;
                        if (model.getNumPowerTokensHouse(playerOnSide[heroSide]) >= 2 &&
                                model.getNumActiveCardsOfPlayer(playerOnSide[heroSide]) >= 1) {
                            say(HOUSE[playerOnSide[heroSide]] + CAN_USE_SPECIAL_PROPERTY_OF_CARD +
                                    houseCardOfSide[heroSide].getName());
                            boolean useDamphair = playerInterface[playerOnSide[heroSide]].useAeron(battleInfo);
                            controlPoint(AERON);
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                            if (useDamphair) {
                                say(AERON_RUNS_AWAY + HOUSE[playerOnSide[heroSide]] + MUST_CHOOSE_OTHER_CARD);
                                propertyUsed = true;
                                model.setHouseCardOnSide(heroSide, null);
                                if (Settings.getInstance().isTrueAutoSwitchTabs()) {
                                    tabPanel.setSelectedIndex(TabEnum.fight.getCode());
                                }
                                controlPoint(HOUSE[playerOnSide[heroSide]] + CHOOSES_CARD);
                                if (Thread.currentThread().isInterrupted()) {
                                    return;
                                }
                                houseCardOfSide[heroSide] = getHouseCard(battleInfo, playerOnSide[heroSide]);
                                model.setHouseCardOnSide(heroSide, houseCardOfSide[heroSide]);
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
        int swordsMan = model.getTrackPlayerOnPlace(TrackType.valyrianSword.getCode(), 0);
        if (!model.getIsSwordUsed() && (playerOnSide[0] == swordsMan || playerOnSide[1] == swordsMan)) {
            boolean useSword = playerInterface[swordsMan].useSword(battleInfo);
            if (Settings.getInstance().isTrueAutoSwitchTabs()) {
                tabPanel.setSelectedIndex(TabEnum.fight.getCode());
            }
            controlPoint(HOUSE[swordsMan] + CAN_USE_SWORD);
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            if (useSword) {
                model.useSword();
            }
        }
        model.destabilizeCards();

        // Определяем победителя
        int winnerSide = battleInfo.resolveFight().getCode();
        int winner = playerOnSide[winnerSide];
        int loser = playerOnSide[1 - winnerSide];
        model.winAndKill();
        if (winnerSide == 0) {
            // Победил нападающий
            model.attackerWon();
            // Захват кораблей в порту
            int areaOfBattle = model.getBattleArea();
            if (map.getNumCastle(areaOfBattle) > 0) {
                if (model.getAreaOwner(areaOfBattle) < 0) {
                    tryToDestroyNeutralShips(areaOfBattle);
                } else if (model.getAreaOwner(areaOfBattle) == winner) {
                    tryToCaptureShips(areaOfBattle, winner);
                }
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }

            // Отступление
            Army retreatingArmy = model.getRetreatingArmy();
            if (retreatingArmy.getSize() > 0) {
                model.calculateRetreatAreas();

                if (retreatingArmy.getSize() > 0) {
                    model.woundAllRetreatingUnits();
                    HashSet<Integer> areasToRetreat = model.getAreasToRetreat();
                    model.printAreasInCollection(areasToRetreat, "Области для отступления");
                    switch (areasToRetreat.size()) {
                        case 0:
                            say(NO_AREAS_TO_RETREAT);
                            break;
                        case 1:
                            // Если имеется единтсвенная область для отступления, то отступаем туда автоматически
                            int onlyArea = areasToRetreat.iterator().next();
                            model.retreat(onlyArea);
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
                            controlPoint(HOUSE[loser] + RETREATS);
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                                int area = playerInterface[player].chooseAreaToRetreat(retreatingArmy, areasToRetreat);
                                if (areasToRetreat.contains(area)) {
                                    model.retreat(area);
                                    successFlag = true;
                                    break;
                                } else {
                                    say(CANT_RETREAT_THERE_ERROR);
                                }
                            }
                            // Если игрок так и не смог отступить, то отступаем в первую доступную область
                            if (!successFlag) {
                                int area = areasToRetreat.iterator().next();
                                model.retreat(area);
                            }
                            break;
                    }
                }
            }
        } else {
            // Победил защищающийся
            model.defenderWon();
        }
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintArea(model.getBattleArea());
        }
        // Разыгрываем эффект карт "после боя"
        for (int curSide = 0; curSide < 2; curSide++) {
            int heroSide = (curSide + firstSideOnThrone) % 2;
            if (houseCardOfSide[heroSide].getCardInitiative() == CardInitiative.afterFight) {
                int player = playerOnSide[heroSide];
                switch (houseCardOfSide[heroSide]) {
                    case renlyBaratheon:
                        propertyUsed = false;
                        if (heroSide != winnerSide || model.getRestingUnits(player, UnitType.knight) == 0) {
                            break;
                        }
                        accessibleAreaSet = model.getRenlyAreas();

                        if (accessibleAreaSet.size() > 0) {
                            say(RENLY_CAN_MAKE_KNIGHT);
                            controlPoint(RENLY);
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                                int area = playerInterface[player].areaToUseRenly(accessibleAreaSet);
                                // Если область меньше нуля, значит, игрок отказался использовать свойство карты
                                if (area < 0) {
                                    break;
                                }
                                if (area >= NUM_AREA || !accessibleAreaSet.contains(area)) {
                                    say(INVALID_AREA_ERROR);
                                } else {
                                    model.playRenly(area);
                                    propertyUsed = true;
                                    break;
                                }
                            }
                        }
                        if (!propertyUsed) {
                            say(houseCardOfSide[heroSide].getName() + NO_EFFECT);
                        }
                        break;
                    case cerseiLannister:
                        if (heroSide != winnerSide) break;
                        propertyUsed = false;
                        accessibleAreaSet = model.getCerseiAreas();
                        if (accessibleAreaSet.size() > 0) {
                            say(CERSEI_CAN_REMOVE_ANY_ORDER);
                            controlPoint(CERSEI);
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                            for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                                int area = playerInterface[player].chooseAreaCerseiLannister(accessibleAreaSet);
                                // Если область меньше нуля, значит, игрок отказался использовать свойство карты
                                if (area < 0) {
                                    break;
                                }
                                if (area >= NUM_AREA || !accessibleAreaSet.contains(area)) {
                                    say(INVALID_AREA_ERROR);
                                } else {
                                    model.playCersei(area);
                                    propertyUsed = true;
                                    break;
                                }
                            }
                        }
                        if (!propertyUsed) {
                            say(houseCardOfSide[heroSide].getName() + NO_EFFECT);
                        }
                        break;
                    case tywinLannister:
                        if (heroSide != winnerSide) break;
                        model.earnTokens(playerOnSide[heroSide], 2);
                        break;
                    case rooseBolton:
                        if (heroSide == winnerSide) break;
                        model.returnAllCards(player);
                        break;
                }
            }
        }

        // Разыгрываем Пестряка
        for (int curSide = 0; curSide < 2; curSide++) {
            if (houseCardOfSide[curSide] == HouseCard.patchface) {
                propertyUsed = false;
                say(PATCHPACE_CAN_DELETE_ANY_CARD);
                controlPoint(PATCHFACE);
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    int card = playerInterface[playerOnSide[curSide]].chooseCardPatchface(playerOnSide[1 - curSide]);
                    // Если карта меньше нуля, значит, игрок отказался использовать свойство карты
                    if (card < 0) {
                        break;
                    }
                    if (!model.getHouseCardOfPlayer(playerOnSide[1 - curSide], card).isActive()) {
                        say(CARD_IS_NOT_ACTIVE_ERROR);
                    } else {
                        propertyUsed = true;
                        model.playPatchface(playerOnSide[1 - curSide], card);
                        break;
                    }
                }
                if (!propertyUsed) {
                    say(houseCardOfSide[curSide].getName() + NO_EFFECT);
                }
            }
        }
    }

    /**
     * Метод обращается к игроку и получает от него карту дома, которую после возвращает.
     * @param battleInfo информация о сражении
     * @param player     номер игрока
     * @return карта дома, которую выбрал игрок
     */
    private HouseCard getHouseCard(BattleInfo battleInfo, int player) {
        HouseCard chosenCard = null;
        int numActiveCards = model.getNumActiveCardsOfPlayer(player);
        if (numActiveCards == 0) {
            chosenCard = HouseCard.none;
        } else {
            if (numActiveCards == 1) {
                chosenCard = model.getFirstActiveHouseCard(player);
            } else {
                for (int attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    int givenCard = playerInterface[player].playHouseCard(battleInfo);
                    if (givenCard >= 0 && givenCard < NUM_HOUSE_CARDS &&
                            model.getHouseCardOfPlayer(player, givenCard).isActive()) {
                        chosenCard = model.getHouseCardOfPlayer(player, givenCard);
                    }
                }
                // Если игрок не уложился в число попыток, то выбираем первую активную его карту
                if (chosenCard == null) {
                    chosenCard = model.getFirstActiveHouseCard(player);
                }
            }
            chosenCard.setActive(false);
            model.decreaseNumActiveHouseCards(player);
            say(HOUSE[player] + PLAYS_HOUSE_CARD + chosenCard.getName() + "\".");
            if (!Settings.getInstance().isPassByRegime()) {
                houseTabPanel.repaintHouse(player);
            }
        }
        return chosenCard;
    }


    /**
     * Метод разыгрывает новые события Вестероса
     */
    private void playNewEvents() {
        if (model.getWildlingsStrength() == MAX_WILDLING_STRENGTH) {
            playWildlingAttack();
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
        }
        // Событие №1
        switch ((Deck1Cards) model.getEvent(1)) {
            case muster:
                playMuster();
                break;
            case supply:
                adjustSupply();
                break;
            case throneOfSwords:
                int king = model.getTrackPlayerOnPlace(TrackType.ironThrone.getCode(), 0);
                say(HOUSE[king] + MUST_CHOOSE_EVENT);
                int attempt;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    int kingChoice = playerInterface[king].eventToChoose(1);
                    controlPoint(HOUSE[king] + CHOOSES_EVENT);
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
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
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        // Событие №2
        switch ((Deck2Cards) model.getEvent(2)) {
            case clashOfKings:
                playClash();
                break;
            case gameOfThrones:
                playGameOfThrones();
                break;
            case darkWingsDarkWords:
                int ravenHolder = model.getTrackPlayerOnPlace(TrackType.raven.getCode(), 0);
                say(HOUSE[ravenHolder] + MUST_CHOOSE_EVENT);
                int attempt;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    int ravenChoice = playerInterface[ravenHolder].eventToChoose(2);
                    controlPoint(HOUSE[ravenHolder] + CHOOSES_EVENT);
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
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
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        // Событие №3
        switch ((Deck3Cards) model.getEvent(3)) {
            case wildlingsAttack:
                playWildlingAttack();
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                break;
            case devotedToSword:
                int swordsman = model.getTrackPlayerOnPlace(TrackType.valyrianSword.getCode(), 0);
                say(HOUSE[swordsman] + MUST_CHOOSE_EVENT);
                int attempt;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    int swordChoice = playerInterface[swordsman].eventToChoose(3);
                    controlPoint(HOUSE[swordsman] + CHOOSES_EVENT);
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    if (swordChoice < 0 || swordChoice >= NUM_EVENT_CHOICES) {
                        say(WRONG_EVENT_CHOICE_ERROR);
                    } else {
                        switch (swordChoice) {
                            case 0:
                                model.setForbiddenOrder(OrderType.defence);
                                say(STORM_OF_SWORDS);
                                break;
                            case 1:
                                model.setForbiddenOrder(OrderType.march);
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
                model.setForbiddenOrder(OrderType.raid);
                say(SEA_OF_STORMS);
                break;
            case rainOfAutumn:
                model.setForbiddenOrder(OrderType.march);
                say(RAIN_OF_AUTUMN);
                break;
            case feastForCrows:
                model.setForbiddenOrder(OrderType.consolidatePower);
                say(FEAST_FOR_CROWS);
                break;
            case webOfLies:
                model.setForbiddenOrder(OrderType.support);
                say(WEB_OF_LIES);
                break;
            case stormOfSwords:
                model.setForbiddenOrder(OrderType.defence);
                say(STORM_OF_SWORDS);
                break;
        }
    }

    /**
     * Метод считает и изменяет снабжение каждого из игроков
     */
    private void adjustSupply() {
        model.adjustSupply();
        verifyNewSupplyLimits();
    }

    /**
     * Метод разыгрывает событие "Сбор войск"
     */
    private void playMuster() {
        say(MUSTER_HAPPENS);
        ArrayList<HashSet<Integer>> areasWithMusterOfPlayers = model.getAreasWithMusterOfPlayers();
        for (int place = 0; place < NUM_PLAYER; place++) {
            int player = model.getTrackPlayerOnPlace(TrackType.ironThrone.getCode(), place);
            say(HOUSE[player] + CAN_MUSTER_TROOPS);
            if (!Settings.getInstance().isPassByRegime()) {
                for (int area: areasWithMusterOfPlayers.get(player)) {
                    model.safeSetOrderInArea(area, Order.consolidatePowerS, player);
                }
            }
            while (!areasWithMusterOfPlayers.get(player).isEmpty()) {
                int attempt;
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    MusterPlayed musterVariant = playerInterface[player].muster(areasWithMusterOfPlayers.get(player));
                    if (model.validateMuster(musterVariant, player)) {
                        controlPoint(HOUSE[player] + MUSTERS);
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        model.executeMuster(musterVariant, player);
                        areasWithMusterOfPlayers.get(player).remove(musterVariant.getCastleArea());
                        break;
                    }
                }
                if (attempt >= MAX_TRIES_TO_GO) {
                    say(HOUSE[player] + FAILED_TO_PLAY_MUSTER);
                    for (int castleArea: areasWithMusterOfPlayers.get(player)) {
                        model.safeDeleteOrderInArea(castleArea, player);
                    }
                }
            }
        }
    }

    /**
     * Метод разыгрывает сбор войск в одном из замков игрока
     * @param player номер игрока
     */
    private void playOneMuster(int player) {
        HashSet<Integer> areasWithMuster = model.getAreasWithMusterOfPlayer(player);
        if (areasWithMuster.size() == 0) return;
        say(HOUSE[player] + CAN_MUSTER_IN_ONE_CASTLE);
        int attempt;
        for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
            MusterPlayed musterVariant = playerInterface[player].muster(areasWithMuster);
            if (model.validateMuster(musterVariant, player)) {
                controlPoint(HOUSE[player] + CAN_ONE_MUSTER);
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                int nMusteredObjects = musterVariant.getNumberMusterUnits();
                if (nMusteredObjects > 0) {
                    model.executeMuster(musterVariant, player);
                }
                break;
            }
        }
        if (attempt >= MAX_TRIES_TO_GO) {
            say(HOUSE[player] + FAILED_TO_PLAY_MUSTER);
        }

        for (int area: areasWithMuster) {
            model.setOrderInArea(area, null);
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintArea(area);
            }
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
        if (Settings.getInstance().isTrueAutoSwitchTabs()) {
            tabPanel.setSelectedIndex(TabEnum.house.getCode());
        }
        for (int track = 0; track < NUM_TRACK; track++) {
            say(CLASH_FOR + TrackType.getTrack(track) + PLAYERS_MAKE_BIDS);
            int king = model.getTrackPlayerOnPlace(TrackType.ironThrone.getCode(), 0);
            int[] tempBids = new int[NUM_PLAYER];
            // получаем от игроков ставки
            for (int player = 0; player < NUM_PLAYER; player++) {
                isPlayerCounted[player] = false;
                if (model.getNumPowerTokensHouse(player) == 0) {
                    tempBids[player] = 0;
                    continue;
                }
                for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                    tempBids[player] = playerInterface[player].bid(track);
                    if (tempBids[player] >= 0 && tempBids[player] <= model.getNumPowerTokensHouse(player)) {
                        break;
                    }
                    say(WRONG_BID_ERROR);
                }
                if (attempt >= MAX_TRIES_TO_GO) {
                    say(HOUSE[player] + FAILED_TO_BID);
                    tempBids[player] = 0;
                }
            }
            controlPoint(PLAYERS + BID[track]);
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            model.setCurrentBids(tempBids);
            model.setCurrentBidTrack(track);
            for (int player = 0; player < NUM_PLAYER; player++) {
                model.playerPays(player, tempBids[player]);
            }
            model.trackPreArrange(track);
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintTracks();
                houseTabPanel.repaint();
            }
            // решение королём ничьих
            for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                newPlayerOnPlace = playerInterface[king].kingChoiceInfluenceTrack(track, tempBids);
                controlPoint(HOUSE[king] + DECIDES_TIES);
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                isPlayerCounted[newPlayerOnPlace[0]] = true;
                boolean alrightFlag = true;
                for (int place = 1; place < NUM_PLAYER; place++) {
                    if (isPlayerCounted[newPlayerOnPlace[place]]) {
                        say(PLAYER_COUNTED_TWICE_ERROR);
                        alrightFlag = false;
                        break;
                    }
                    if (tempBids[newPlayerOnPlace[place]] > tempBids[newPlayerOnPlace[place - 1]]) {
                        say(WRONG_TRACK_ORDER_ERROR);
                        alrightFlag = false;
                        break;
                    }
                }
                if (alrightFlag) {
                    System.arraycopy(newPlayerOnPlace, 0, model.getTrackPlayerOnPlace(track), 0, NUM_PLAYER);
                    model.fillTrackPlaceForPlayer(track);
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
                        if (tempBids[player] > curMaxBid && !isPlayerCounted[player]) {
                            pretenders.clear();
                            pretenders.add(player);
                            curMaxBid = tempBids[player];
                        } else if (tempBids[player] == curMaxBid) {
                            pretenders.add(player);
                        }
                    }
                    while(!pretenders.isEmpty()) {
                        int nextOnTrackIndex = random.nextInt(pretenders.size());
                        model.setTrackPlayerOnPlace(track, curPlace, pretenders.get(nextOnTrackIndex));
                        isPlayerCounted[pretenders.get(nextOnTrackIndex)] = true;
                        pretenders.remove(nextOnTrackIndex);
                        curPlace++;
                    }
                }
                model.fillTrackPlaceForPlayer(track);
            }
            if (!Settings.getInstance().isPassByRegime()) {
                mapPanel.repaintTracks();
            }
        }
    }

    /**
     * Метод разыгрывает событие "Игра престолов"
     */
    private void playGameOfThrones() {
        say(GAME_OF_THRONES_HAPPENS);
        model.playGameOfThrones();
    }

    /**
     * Метод проверяет, укладываются ли игроки в новый лимит по снабжению, и если нет, то требует от них распустить
     * лишние войска. Вызывается после события "Снабжение" и после поражения от одичальнических охотников на снабжение
     */
    private void verifyNewSupplyLimits() {
        for (int place = 0; place < NUM_PLAYER; place++) {
            int player = model.getTrackPlayerOnPlace(TrackType.ironThrone.getCode(), place);
            if (!GameUtils.supplyTest(model.getAreasWithTroopsOfPlayerAndSize(player), model.getSupply(player))) {
                playDisband(player, DisbandReason.supply);
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
        }
    }

    /**
     * Метод разыгрывает нападение одичалых
     */
    private void playWildlingAttack() {
        model.wildlingsBeginToAttack();
        // получаем от игроков ставки
        int wildlingsStrength = model.getWildlingsStrength();
        int preemptiveRaidCheater = model.getPreemptiveRaidCheater();
        int[] tempBids = new int[NUM_PLAYER];
        int attempt;
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (player == preemptiveRaidCheater) continue;
            int money = model.getNumPowerTokensHouse(player);
            if (money == 0) {
                tempBids[player] = 0;
                continue;
            }
            for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                tempBids[player] = playerInterface[player].wildlingBid(wildlingsStrength);
                if (tempBids[player] >= 0 && tempBids[player] <= money) {
                    break;
                }
                say(WRONG_BID_ERROR);
            }
            if (attempt >= MAX_TRIES_TO_GO) {
                say(HOUSE[player] + FAILED_TO_BID);
                tempBids[player] = 0;
            }
        }
        controlPoint(PLAYERS + FIGHT_WILDLINGS);
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        model.setCurrentBids(tempBids);
        model.setCurrentBidTrack(-1);
        int nightWatchStrength = 0;
        model.revealWildlings();
        WildlingCard wildlingCard = model.getTopWildlingsCard();
        for (int player = 0; player < NUM_PLAYER; player++) {
            playerInterface[player].showPlayedWildlingsCard(wildlingCard);
            nightWatchStrength += tempBids[player];
        }
        boolean isNightWatchWon = nightWatchStrength >= wildlingsStrength;
        if (isNightWatchWon) {
            // Победа ночного дозора
            say(NIGHT_WATCH_VICTORY);
            model.setWildlingsStrength(0);
        } else {
            // Победа одичалых
            say(NIGHT_WATCH_DEFEAT);
            model.setWildlingsStrength(Math.max(0, wildlingsStrength - 2 * WILDLING_STRENGTH_INCREMENT));
        }
        if (!Settings.getInstance().isPassByRegime()) {
            mapPanel.repaintWildlings();
        }
        // Тишина за стеной - находить высшую/низшую ставку не имеет смысла
        if (wildlingCard == WildlingCard.silenceAtTheWall) {
            say(SILENCE_AT_THE_WALL);
            return;
        }
        // Находим высшую или низшую ставку
        int exclusiveBidder = -1;
        int exclusiveBid = isNightWatchWon ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        ArrayList<Integer> curExBidders = new ArrayList<>();
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (player == preemptiveRaidCheater) continue;
            if (isNightWatchWon && tempBids[player] > exclusiveBid ||
                    !isNightWatchWon && tempBids[player] < exclusiveBid) {
                exclusiveBid = tempBids[player];
                curExBidders.clear();
                curExBidders.add(player);
            } else if (tempBids[player] == exclusiveBid) {
                curExBidders.add(player);
            }
        }
        if (curExBidders.size() == 1) {
            exclusiveBidder = curExBidders.get(0);
        } else {
            int king = model.getTrackPlayerOnPlace(TrackType.ironThrone.getCode(), 0);
            controlPoint(HOUSE[king] + CHOOSES +
                    (isNightWatchWon ? CHOOSES_TOP_BID : CHOOSES_BOTTOM_BID));
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                exclusiveBidder = playerInterface[king].kingChoiceWildlings(wildlingCard, curExBidders, isNightWatchWon);
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
        switch (wildlingCard) {
            // Охотники на снабжение
            case rattleShirtRaiders:
                if (isNightWatchWon) {
                    model.changeSupply(exclusiveBidder, 1);
                } else {
                    model.changeSupply(exclusiveBidder, -2);
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = model.getTrackPlayerOnPlace(0, place);
                        if (player == exclusiveBidder || player == preemptiveRaidCheater) continue;
                        model.changeSupply(player, -1);
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
                    model.playerPays(exclusiveBidder, -tempBids[exclusiveBidder]);
                    say(HOUSE[exclusiveBidder] + SKINCHANGER_SCOUT_WIN);
                } else {
                    model.loseAllMoney(exclusiveBidder);
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = model.getTrackPlayerOnPlace(0, place);
                        if (player == preemptiveRaidCheater || player == exclusiveBidder) continue;
                        model.earnTokens(player, -2);
                    }
                }
                break;
            // Сбор на молоководной
            case massingOnTheMilkwater:
                if (isNightWatchWon) {
                    model.returnAllCards(exclusiveBidder);
                } else {
                    // Низшая ставка
                    model.loseHighestCards(exclusiveBidder);
                    // Прочие ставки
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = model.getTrackPlayerOnPlace(0, place);
                        if (player == exclusiveBidder || player == preemptiveRaidCheater ||
                                model.getNumActiveCardsOfPlayer(player) == 1) continue;
                        HouseCard chosenCard = null;
                        for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                            int card = playerInterface[player].massingOnTheMilkwaterLoseDecision();
                            if (card >= 0 && card < NUM_HOUSE_CARDS &&
                                    model.getHouseCardOfPlayer(player, card).isActive()) {
                                controlPoint(HOUSE[player] + LOSES_ONE_CARD);
                                if (Thread.currentThread().isInterrupted()) {
                                    return;
                                }
                                chosenCard = model.getHouseCardOfPlayer(player, card);
                                break;
                            }
                        }
                        if (attempt >= MAX_TRIES_TO_GO) {
                            chosenCard = model.getFirstActiveHouseCard(player);
                        }
                        assert chosenCard != null;
                        model.loseCard(chosenCard);
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
                    if (model.getNumActiveCardsOfPlayer(exclusiveBidder) < NUM_HOUSE_CARDS) {
                        HouseCard chosenCard = null;
                        say(HOUSE[exclusiveBidder] + CAN_RETURN_CARD);
                        for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                            int card = playerInterface[exclusiveBidder].mammothRidersTopDecision();
                            // Если номер карты отрицательный, значит, игрок не хочет возвращать карту Дома
                            if (card < 0) {
                                break;
                            }
                            if (card >= 0 && card < NUM_HOUSE_CARDS &&
                                    !model.getHouseCardOfPlayer(exclusiveBidder, card).isActive()) {
                                chosenCard = model.getHouseCardOfPlayer(exclusiveBidder, card);
                                break;
                            }
                        }
                        if (attempt >= MAX_TRIES_TO_GO) {
                            say(HOUSE[exclusiveBidder] + FAILED_TO_WILD);
                        }
                        if (chosenCard != null) {
                            model.returnCard(chosenCard);
                        }
                    }
                } else {
                    // Низшая ставка
                    playDisband(exclusiveBidder, DisbandReason.mammothTreadDown);
                    // Прочие ставки
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = model.getTrackPlayerOnPlace(0, place);
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
                    controlPoint(HOUSE[exclusiveBidder] + ENLIGHTENS);
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    if (track != null) {
                        say(HOUSE[exclusiveBidder] + ENLIGHTENS_ON_TRACK + track.onTheTrack() + ".");
                        model.enlightenOnTrack(exclusiveBidder, track.getCode());
                    }
                } else {
                    // Низшая ставка
                    say(HOUSE[exclusiveBidder] + DESCENDS_ON_ALL_TRACKS);
                    for (int trackId = 0; trackId < NUM_TRACK; trackId++) {
                        model.pissOffOnTrack(exclusiveBidder, trackId);
                    }
                    // Прочие ставки
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = model.getTrackPlayerOnPlace(0, place);
                        if (player == exclusiveBidder || player == preemptiveRaidCheater) continue;
                        say(HOUSE[player] + MUST_DESCEND_ON_TRACK);
                        track = playerInterface[player].aKingBeyondTheWallLoseDecision();
                        controlPoint(HOUSE[player] + IS_PISSED_UP);
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        if (track == null || track == TrackType.ironThrone) {
                            track = TrackType.getTrack(random.nextInt(2) + 1);
                        }
                        assert (track != null);
                        say(HOUSE[player] + DESCENDS_ON_TRACK + track.onTheTrack() + ".");
                        model.pissOffOnTrack(player, track.getCode());
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
                    ArrayList<Integer> normCastles = model.getHordeVulnerableCastles(exclusiveBidder);
                    if (normCastles.size() == 0) {
                        playDisband(exclusiveBidder, DisbandReason.wildlingCommonDisband);
                    } else if (normCastles.size() == 1 && model.getArmyInArea(normCastles.get(0)).getSize() == 2) {
                        model.killAllUnitsInArea(normCastles.get(0));
                    } else {
                        playDisband(exclusiveBidder, DisbandReason.hordeCastle);
                    }
                    // Прочие ставки
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = model.getTrackPlayerOnPlace(0, place);
                        if (player == exclusiveBidder || player == preemptiveRaidCheater) continue;
                        playDisband(player, DisbandReason.hordeBite);
                    }
                }
                break;
            // Убийцы ворон
            case crowKillers:
                if (isNightWatchWon) {
                    // Высшая ставка
                    int numAlivePawns = model.getNumAliveUnits(exclusiveBidder, UnitType.pawn);
                    int numRestingKnight = model.getRestingUnits(exclusiveBidder, UnitType.knight);
                    int maxPawnsToUpgrade = Math.min(Math.min(2, numRestingKnight), numAlivePawns);
                    if (maxPawnsToUpgrade > 0) {
                        upgradeSomePawns(exclusiveBidder, maxPawnsToUpgrade);
                    }
                } else {
                    // Низшая ставка
                    int numRestingPawns = model.getRestingUnits(exclusiveBidder, UnitType.pawn);
                    int numAliveKnights = model.getNumAliveUnits(exclusiveBidder, UnitType.knight);
                    int numKnightsToKill = Math.max(0, numAliveKnights - numRestingPawns);
                    // Убиваем коней, которых нельзя превратить в пешки
                    if (numKnightsToKill > 0) {
                        killSomeKnights(exclusiveBidder, numKnightsToKill, numAliveKnights);
                        break;
                    }
                    // Спешиваем всех коней
                    model.dismountAllKnights(exclusiveBidder);
                    // Прочие ставки
                    for (int place = 0; place < NUM_PLAYER; place++) {
                        player = model.getTrackPlayerOnPlace(0, place);
                        if (player == exclusiveBidder || player == preemptiveRaidCheater) continue;
                        numRestingPawns = model.getRestingUnits(player, UnitType.pawn);
                        numAliveKnights = model.getNumAliveUnits(player, UnitType.knight);
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
                    model.setWildlingsStrength(PREEMPTIVE_RAID_WILDLINGS_STRENGTH);
                    model.setPreemptiveRaidCheater(exclusiveBidder);
                    playWildlingAttack();
                    model.setPreemptiveRaidCheater(-1);
                } else {
                    // Низшая ставка
                    int bestPlace = NUM_PLAYER;
                    for (int trackId = 0; trackId < NUM_TRACK; trackId++) {
                        if (bestPlace > model.getTrackPlaceForPlayer(trackId, exclusiveBidder)) {
                            bestPlace = model.getTrackPlaceForPlayer(trackId, exclusiveBidder);
                        }
                    }
                    for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                        Object decision = playerInterface[exclusiveBidder].preemptiveRaidBottomDecision();
                        controlPoint(HOUSE[exclusiveBidder] + PREEMPTIVE_DECIDES);
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        if (decision instanceof TrackType) {
                            if (model.getTrackPlaceForPlayer(((TrackType) decision).getCode(), exclusiveBidder) == bestPlace) {
                                say(HOUSE[exclusiveBidder] + PREEMPTIVE_RAID_TRACK + ((TrackType) decision).onTheTrack());
                                model.descendOnTrack(exclusiveBidder, ((TrackType) decision).getCode(), 2);
                                break;
                            }
                        }
                        if (decision instanceof DisbandPlayed) {
                            if (model.validateDisband(((DisbandPlayed) decision), exclusiveBidder,
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
                            if (model.getTrackPlaceForPlayer(trackId, exclusiveBidder) == bestPlace) {
                                model.descendOnTrack(exclusiveBidder, trackId, 2);
                                break;
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     * Метод уничтожает всех юнитов игрока
     * @param player номер игрока
     */
    private void killAllUnits(int player, KillingReason reason) {
        for (int area: model.getAreasWithTroopsOfPlayer(player)) {
            model.getArmyInArea(area).killAllUnits(reason, model);
            tryToRenewEmptiedCastle(area, player);
        }
        model.getAreasWithTroopsOfPlayer(player).clear();
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
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
            if (model.validatePawnsToUpgrade(pawnUpgradeVariant, player, maxPawnsToUpgrade)) {
                controlPoint(HOUSE[player] + UPGRADES_PAWNS);
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                model.executePawnUpgrade(pawnUpgradeVariant, player);
                break;
            }
        }
        if (attempt >= MAX_TRIES_TO_GO) {
            say(HOUSE[player] + FAILED_TO_WILD);
        }
    }

    /**
     * Метод убивает часть рыцарей определённого игрока. Может вызваться при победе карты "Убийцы ворон"
     * @param player           номер игрока
     * @param numKnightsToKill количество рыцарей, которых нужно убить
     * @param numAliveKnights  количество живых рыцарей игрока
     */
    private void killSomeKnights(int player, int numKnightsToKill, int numAliveKnights) {
        int attempt;
        if (numKnightsToKill == numAliveKnights) {
            model.killAllKnights(player);
        } else {
            // Спрашиваем у игрока, каких коней убить
            say(HOUSE[player] + MUST_DISBAND_SOME_KNIGHTS);
            for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                UnitExecutionPlayed variantToKillKnights = playerInterface[player].
                        crowKillersKillKnights(numKnightsToKill);
                if (model.validateKnightsToDo(variantToKillKnights, player, numKnightsToKill)) {
                    controlPoint(HOUSE[player] + KILLS_KNIGHTS);
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    // Уничтожаем выбранных коней
                    model.executeKnightKilling(variantToKillKnights, player);
                    break;
                }
            }
            if (attempt >= MAX_TRIES_TO_GO) {
                // Если игрок не смог выбрать нескольких коней для умерщвления, то убиваем их всех!
                say(HOUSE[player] + FAILED_TO_WILD);
                model.killAllKnights(player);
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
            model.dismountAllKnights(player);
        } else {
            // Если выбор есть, то спешиваем нужное число коней, обратившись к игроку
            say(HOUSE[player] + MUST_DOWNGRADE_SOME_KNIGHTS);
            int attempt;
            for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
                UnitExecutionPlayed executionVariant = playerInterface[player].
                        crowKillersLoseDecision(numKnightToDowngrade);
                if (model.validateKnightsToDo(executionVariant, player, numKnightToDowngrade)) {
                    controlPoint(HOUSE[player] + DOWNGRADES_KNIGHTS);
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    model.executeKnightDismounting(executionVariant, player);
                    break;
                }
            }
            if (attempt >= MAX_TRIES_TO_GO) {
                // Если игрок не шмог, то спешиваем первых попавшихся коней
                say(HOUSE[player] + FAILED_TO_WILD);
                model.genericKnightDismount(player, numKnightToDowngrade);
            }
        }
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
            int numUnits = model.getTotalNumberOfUnits(player);
            if (numDisbandUnits >= numUnits) {
                killAllUnits(player, KillingReason.wildlings);
                return;
            }
        }
        // Иначе он сам решает, какие войска распустить.
        say(HOUSE[player] + MUST_DISBAND_TROOPS);
        int attempt;
        for (attempt = 0; attempt < MAX_TRIES_TO_GO; attempt++) {
            DisbandPlayed disbandVariant = playerInterface[player].disband(reason);
            if (model.validateDisband(disbandVariant, player, reason)) {
                controlPoint(HOUSE[player] + DISBANDS);
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                executeDisband(disbandVariant, player);
                break;
            }
        }
        if (attempt >= MAX_TRIES_TO_GO) {
            say(HOUSE[player] + FAILED_TO_DISBAND);
            if (reason == DisbandReason.supply) {
                model.genericSupplyDisband(player);
            } else {
                if (reason == DisbandReason.hordeCastle) {
                    int area = model.genericHordeCastleDisband(player);
                    tryToRenewEmptiedCastle(area, player);
                } else {
                    for (int numDisbands = reason.getNumDisbands(); numDisbands > 0; numDisbands--) {
                        int area = model.genericOneOtherDisband(player);
                        tryToRenewEmptiedCastle(area, player);
                    }
                }
            }
        }
    }

    private void tryToRenewEmptiedCastle(int area, int exOwner) {
        if (map.getNumCastle(area) > 0 && model.getArmyInArea(area).isEmpty()) {
            int areaOwner = model.getAreaOwner(area);
            if (areaOwner != exOwner) {
                model.loseVictoryPoints(exOwner);
                if (areaOwner < 0) {
                    tryToDestroyNeutralShips(area);
                } else {
                    model.adjustVictoryPoints();
                    tryToCaptureShips(area, areaOwner);
                }
            }
        }
        model.renewArea(area, exOwner);
    }

    private void executeDisband(DisbandPlayed disbandVariant, int player) {
        HashMap<Integer, ArrayList<UnitType>> disbands = disbandVariant.getDisbandUnits();
        for (int area: disbands.keySet()) {
            for (UnitType unitType: disbands.get(area)) {
                model.getArmyInArea(area).killUnitOfType(unitType, model);
            }
            tryToRenewEmptiedCastle(area, player);
        }
        if (!Settings.getInstance().isPassByRegime()) {
            houseTabPanel.repaintHouse(player);
        }
    }

    public void playEvents() {
        say(ROUND_NUMBER + Controller.getInstance().getTime() + ". " + GamePhase.westerosPhase);
        model.nullifyOrdersAndVariables();
        model.chooseNewEvents();
        controlPoint(EVENTS + model.getEvent(1).getName() + "; " +
                model.getEvent(2).getName() + "; " + model.getEvent(3).getName());
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        playNewEvents();
    }

    public GameOfThronesMap getMap() {
        return model.getMap();
    }

    public void receiveComponents(MapPanel mapPanel, LeftTabPanel tabPanel) {
        this.mapPanel = mapPanel;
        this.tabPanel = tabPanel;
        this.chat = tabPanel.getChatTab().getChat();
        this.eventTabPanel = tabPanel.getEventTab();
        this.fightTabPanel = tabPanel.getFightTab();
        this.houseTabPanel = tabPanel.getHouseTab();
        model.receiveComponents(mapPanel, tabPanel);
    }

    /**
     * Метод пишет в чат определённый текст.
     * @param text текст, который должен быть написан.
     */
    private void say(String text) {
        if (!Settings.getInstance().isPassByRegime()) {
            chat.append(text + "\n");
        } else {
            System.out.println(text);
        }
    }

    private void controlPoint(String text) {
        Controller.getInstance().pause(text);
    }

    public GameModel getModel() {
        return model;
    }
}
