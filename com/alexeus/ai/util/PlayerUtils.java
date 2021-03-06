package com.alexeus.ai.util;

import com.alexeus.ai.struct.DummyArmy;
import com.alexeus.control.Controller;
import com.alexeus.logic.Game;
import com.alexeus.logic.GameModel;
import com.alexeus.logic.enums.UnitType;
import com.alexeus.logic.struct.Army;
import com.alexeus.logic.struct.Unit;
import com.alexeus.map.GameOfThronesMap;

import java.util.*;

import static com.alexeus.logic.constants.MainConstants.NUM_PLAYER;
import static com.alexeus.map.GameOfThronesMap.NUM_AREA;

/**
 * Created by alexeus on 23.01.2017.
 * Класс реализует некоторые функции, полезные для компьютерных игроков
 */
public class PlayerUtils {

    private static PlayerUtils instance;

    private int[] numPassiveFightsInArea;

    private int[] numPassiveFightsToSupportFrom;

    private int[] numAreasToRaidFrom;

    private int[] numAreasToRaid;

    private boolean isNumFightsActual;

    private GameModel model;

    // Время в игре. Нужно учитывать, чтобы вовремя заполнять вспомогательные массивы и коллекции
    private int time;

    private PlayerUtils() {
        numPassiveFightsInArea = new int[NUM_AREA];
        numPassiveFightsToSupportFrom = new int[NUM_AREA];
        numAreasToRaidFrom = new int[NUM_AREA];
        numAreasToRaid = new int[NUM_AREA];
        isNumFightsActual = false;
        model = Game.getInstance().getModel();
        time = Controller.getInstance().getTime();
    }

    public static PlayerUtils getInstance() {
        if (instance == null) {
            instance = new PlayerUtils();
        }
        return instance;
    }

    /**
     * Метод возвращает количество дешёвых юнитов, которых можно убрать с карты без потери области
     * @param armyInArea карта с армиями игрока
     * @return количество удалябельных юнитов
     */
    public static int getNumDisbandSwipe(HashMap<Integer, DummyArmy> armyInArea) {
        int numSwipe = 0;
        for (Map.Entry<Integer, DummyArmy> entry: armyInArea.entrySet()) {
            DummyArmy army = entry.getValue();
            numSwipe += (army.hasUnitOfType(UnitType.knight) || army.hasUnitOfType(UnitType.siegeEngine)) ?
                    army.getNumUnitsOfType(UnitType.pawn) : army.getSize() - 1;
        }
        return numSwipe;
    }

    /**
     * Метод возвращает карту с местонахождениями дешёвых юнитов, которых можно убрать с карты без потери области
     * @param armyInArea карта с армиями игрока
     * @return карта с местоположением всех удалябельных юнитов игрока
     */
    public static HashMap<Integer, Integer> getDisbandSwipes(HashMap<Integer, DummyArmy> armyInArea) {
        HashMap<Integer, Integer> numDisbandSwipe = new HashMap<>();
        int numSwipe;
        for (Map.Entry<Integer, DummyArmy> entry: armyInArea.entrySet()) {
            DummyArmy army = entry.getValue();
            numSwipe = (army.hasUnitOfType(UnitType.knight) || army.hasUnitOfType(UnitType.siegeEngine)) ?
                    army.getNumUnitsOfType(UnitType.pawn) : army.getSize() - 1;
            if (numSwipe > 0) {
                numDisbandSwipe.put(entry.getKey(), numSwipe);
            }
        }
        return numDisbandSwipe;
    }

    public HashMap<Integer, DummyArmy> makeDummyArmies(int player) {
        HashMap<Integer, DummyArmy> dummyArmyInArea = new HashMap<>();
        Set<Integer> areasWithTroops = model.getAreasWithTroopsOfPlayer(player);
        for (int area: areasWithTroops) {
            dummyArmyInArea.put(area, makeDummyArmyFromNormalArmy(model.getArmyInArea(area)));
        }
        return dummyArmyInArea;
    }

    private DummyArmy makeDummyArmyFromNormalArmy(Army army) {
        DummyArmy dummyArmy = new DummyArmy();
        for (Unit unit: army.getUnits()) {
            dummyArmy.addUnit(unit.getUnitType());
        }
        return dummyArmy;
    }

    public int getNumPassiveFightsInArea(int area) {
        renewIfNewRound();
        return numPassiveFightsInArea[area];
    }

    public int getNumPassiveFightsToSupportFrom(int area) {
        renewIfNewRound();
        return numPassiveFightsToSupportFrom[area];
    }

    public int getNumAllFightsToSupportFrom(int area, ArrayList<Integer> areasWithMarches) {
        renewIfNewRound();
        int numFightsToSupport = numPassiveFightsToSupportFrom[area];
        if (areasWithMarches != null) {
            for (int marchArea : areasWithMarches) {
                Set<Integer> accessibleAreas = model.getAccessibleAreas(marchArea, area);
                if (accessibleAreas.contains(area)) {
                    numFightsToSupport++;
                }
            }
        }
        return numFightsToSupport;
    }

    public int getNumAreasToRaidFrom(int area) {
        renewIfNewRound();
        return numAreasToRaidFrom[area];
    }

    public int getNumAreasToRaid(int area) {
        renewIfNewRound();
        return numAreasToRaid[area];
    }

    /**
     * Метод заполняет массив количеств потенциальных источников атаки
     * "Пассивность" боя означает то, что на игрока нападают, а не он нападает, т.е. он может случиться, даже
     * если он не поставил ни одного похода
     */
    private void fillNumPassiveFights() {
        boolean[] doesPlayerFight = new boolean[NUM_PLAYER];
        int areaOwner, marchAreaOwner;
        for (int area = 0; area < NUM_AREA; area++) {
            numPassiveFightsInArea[area] = 0;
            areaOwner = model.getTroopsOrGarrisonOwner(area);
            // Пассивные бои могут происходить только на чьей-то территории
            if (areaOwner < 0) continue;
            HashSet<Integer> probableMarchAreas = model.getAreasWithProbableMarch(area);
            for (int marchArea: probableMarchAreas) {
                marchAreaOwner = model.getTroopsOwner(marchArea);
                if (marchAreaOwner != areaOwner) {
                    doesPlayerFight[marchAreaOwner] = true;
                }
            }
            for (int player = 0; player < NUM_PLAYER; player++) {
                if (doesPlayerFight[player]) {
                    doesPlayerFight[player] = false;
                    numPassiveFightsInArea[area]++;
                }
            }
        }
    }

    /**
     * Метод заполняет массив количеств областей для подмоги из определённой области.
     * Учитываются только пассивные бои, происходящие на территории того же игрока, который оказывает подмогу
     */
    private void fillNumPassiveFightsToSupportFrom() {
        int areaOfSupportOwner;
        GameOfThronesMap map = model.getMap();
        for (int areaOfSupport = 0; areaOfSupport < NUM_AREA; areaOfSupport++) {
            numPassiveFightsToSupportFrom[areaOfSupport] = 0;
            areaOfSupportOwner = model.getTroopsOwner(areaOfSupport);
            if (areaOfSupportOwner >= 0) {
                HashSet<Integer> adjacentAreas = map.getAdjacentAreas(areaOfSupport);
                for (int adjArea: adjacentAreas) {
                    if (map.getAdjacencyType(areaOfSupport, adjArea).supportOrRaidAvailable() &&
                            areaOfSupportOwner == model.getTroopsOrGarrisonOwner(adjArea)) {
                        numPassiveFightsToSupportFrom[areaOfSupport] += numPassiveFightsInArea[adjArea];
                    }
                }
            }
        }
    }

    /**
     * Метод заполняет массивы количеств областей, доступных для набега из данной области, и количеств областей,
     * из которых можно набежать на данную область
     */
    private void fillNumAreasToRaid() {
        int areaOwner;
        GameOfThronesMap map = model.getMap();
        for (int area = 0; area < NUM_AREA; area++) {
            numAreasToRaidFrom[area] = 0;
            numAreasToRaid[area] = 0;
            areaOwner = model.getAreaOwner(area);
            if (areaOwner >= 0) {
                HashSet<Integer> adjacentAreas = map.getAdjacentAreas(area);
                for (int adjacentArea: adjacentAreas) {
                    int adjacentAreaOwner = model.getTroopsOwner(adjacentArea);
                    if (adjacentAreaOwner >= 0 && areaOwner != adjacentAreaOwner) {
                        if (map.getAdjacencyType(area, adjacentArea).supportOrRaidAvailable()) {
                            numAreasToRaidFrom[area]++;
                        }
                        if (map.getAdjacencyType(adjacentArea, area).supportOrRaidAvailable()) {
                            numAreasToRaid[area]++;
                        }
                    }
                }
            }
        }
    }

    private void renewIfNewRound() {
        if (Controller.getInstance().getTime() != time) {
            isNumFightsActual = false;
            time = Controller.getInstance().getTime();
        }
        if (!isNumFightsActual) {
            fillNumPassiveFights();
            fillNumPassiveFightsToSupportFrom();
            fillNumAreasToRaid();
            isNumFightsActual = true;
        }
    }
}
