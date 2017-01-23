package com.alexeus.ai.util;

import com.alexeus.ai.struct.DummyArmy;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.UnitType;
import com.alexeus.logic.struct.Army;
import com.alexeus.logic.struct.Unit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by alexeus on 23.01.2017.
 * Класс реализует некоторые функции, полезные для компьютерных игроков
 */
public class PlayerUtils {

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

    public static HashMap<Integer, DummyArmy> makeDummyArmies(int player) {
        Game game = Game.getInstance();
        HashMap<Integer, DummyArmy> dummyArmyInArea = new HashMap<>();
        Set<Integer> areasWithTroops = game.getAreasWithTroopsOfPlayer(player);
        for (int area: areasWithTroops) {
            dummyArmyInArea.put(area, makeDummyArmyFromNormalArmy(game.getArmyInArea(area)));
        }
        return dummyArmyInArea;
    }

    private static DummyArmy makeDummyArmyFromNormalArmy(Army army) {
        DummyArmy dummyArmy = new DummyArmy();
        for (Unit unit: army.getUnits()) {
            dummyArmy.addUnit(unit.getUnitType());
        }
        return dummyArmy;
    }
}
