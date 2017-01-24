package com.alexeus.ai.util;

import com.alexeus.ai.struct.DummyArmy;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.UnitType;
import com.alexeus.logic.struct.Army;
import com.alexeus.logic.struct.Unit;
import com.alexeus.map.GameOfThronesMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.alexeus.logic.constants.MainConstants.NUM_PLAYER;
import static com.alexeus.map.GameOfThronesMap.NUM_AREA;

/**
 * Created by alexeus on 23.01.2017.
 * Класс реализует некоторые функции, полезные для компьютерных игроков
 */
public class PlayerUtils {

    private static PlayerUtils instance;

    private int[] numFightsCanBeginInArea;

    private int[] numFightsToSupportFrom;

    private int[] numAreasToRaidFrom;

    private int[] numAreasToRaid;

    private boolean isNumFightsActual;

    private Game game;

    // Время в игре. Нужно учитывать, чтобы вовремя заполнять вспомогательные массивы и коллекции
    private int time;

    private PlayerUtils() {
        numFightsCanBeginInArea = new int[NUM_AREA];
        numFightsToSupportFrom = new int[NUM_AREA];
        numAreasToRaidFrom = new int[NUM_AREA];
        numAreasToRaid = new int[NUM_AREA];
        isNumFightsActual = false;
        game = Game.getInstance();
        time = game.getTime();
    }

    public static PlayerUtils getInstance() {
        if (instance == null) {
            instance = new PlayerUtils();
        }
        return instance;
    }

    public static HashMap<Integer, Integer> getNumDisbandSwipe(HashMap<Integer, DummyArmy> armyInArea) {
        HashMap<Integer, Integer> numDisbandSwipe = new HashMap<>();
        int numSwipe;
        for (Map.Entry<Integer, DummyArmy> entry: armyInArea.entrySet()) {
            DummyArmy army = entry.getValue();
            numSwipe = army.hasUnitOfType(UnitType.knight) || army.hasUnitOfType(UnitType.siegeEngine) ?
                    army.getNumUnitsOfType(UnitType.pawn) : army.getSize() - 1;
            if (numSwipe > 0) {
                numDisbandSwipe.put(entry.getKey(), numSwipe);
            }
        }
        return numDisbandSwipe;
    }

    public HashMap<Integer, DummyArmy> makeDummyArmies(int player) {
        HashMap<Integer, DummyArmy> dummyArmyInArea = new HashMap<>();
        Set<Integer> areasWithTroops = game.getAreasWithTroopsOfPlayer(player);
        for (int area: areasWithTroops) {
            dummyArmyInArea.put(area, makeDummyArmyFromNormalArmy(game.getArmyInArea(area)));
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

    public int getNumFightsCanBeginInArea(int area) {
        if (game.getTime() != time) {
            isNumFightsActual = false;
            time = game.getTime();
        }
        if (!isNumFightsActual) {
            fillNumFights();
            fillNumFightsToSupport();
            fillNumAreasToRaid();
            isNumFightsActual = true;
        }
        return numFightsCanBeginInArea[area];
    }

    public int getNumFightsToSupportFrom(int area) {
        return numFightsToSupportFrom[area];
    }

    public int getNumAreasToRaidFrom(int area) {
        return numAreasToRaidFrom[area];
    }

    public int getNumAreasToRaid(int area) {
        return numAreasToRaid[area];
    }

    /**
     * Метод заполняет массив количеств потенциальных источников атаки
     */
    private void fillNumFights() {
        boolean[] doesPlayerFight = new boolean[NUM_PLAYER];
        boolean emptyAreaFlag;
        int areaOwner, marchAreaOwner;
        for (int area = 0; area < NUM_AREA; area++) {
            numFightsCanBeginInArea[area] = 0;
            areaOwner = game.getTroopsOrGarrisonOwner(area);
            emptyAreaFlag = areaOwner < 0 && game.getGarrisonInArea(area) == 0;
            HashSet<Integer> probableMarchAreas = game.getAreasWithProbableMarch(area);
            for (int marchArea: probableMarchAreas) {
                marchAreaOwner = game.getTroopsOwner(marchArea);
                if (marchAreaOwner != areaOwner) {
                    doesPlayerFight[marchAreaOwner] = true;
                }
            }
            for (int player = 0; player < NUM_PLAYER; player++) {
                if (doesPlayerFight[player]) {
                    doesPlayerFight[player] = false;
                    if (emptyAreaFlag) {
                        emptyAreaFlag = false;
                    } else {
                        numFightsCanBeginInArea[area]++;
                    }
                }
            }
        }
    }

    /**
     * Метод заполняет массив количеств областей для подмоги из определённой области
     */
    private void fillNumFightsToSupport() {
        int areaOwner;
        GameOfThronesMap map = game.getMap();
        for (int area = 0; area < NUM_AREA; area++) {
            numFightsToSupportFrom[area] = 0;
            areaOwner = game.getAreaOwner(area);
            if (areaOwner >= 0) {
                HashSet<Integer> adjacentAreas = map.getAdjacentAreas(area);
                for (int adjArea: adjacentAreas) {
                    if (map.getAdjacencyType(area, adjArea).supportOrRaidAvailable()) {
                        numFightsToSupportFrom[area] += numFightsCanBeginInArea[adjArea];
                    }
                }
            }
        }
    }

    private void fillNumAreasToRaid() {
        int areaOwner;
        GameOfThronesMap map = game.getMap();
        for (int area = 0; area < NUM_AREA; area++) {
            numAreasToRaidFrom[area] = 0;
            numAreasToRaid[area] = 0;
            areaOwner = game.getAreaOwner(area);
            if (areaOwner >= 0) {
                HashSet<Integer> adjacentAreas = map.getAdjacentAreas(area);
                for (int adjacentArea: adjacentAreas) {
                    int adjacentAreaOwner = game.getTroopsOwner(adjacentArea);
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
}
