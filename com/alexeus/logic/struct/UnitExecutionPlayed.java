package com.alexeus.logic.struct;

import java.util.HashMap;

/**
 * Created by alexeus on 23.01.2017.
 * Класс представляет собой вариант экзекуции над юнитами в результате нападения одичалых
 */
public class UnitExecutionPlayed {

    private HashMap<Integer, Integer> numberOfUnitsInArea = new HashMap<>();

    private int numUnits;

    public UnitExecutionPlayed() {
        numUnits = 0;
    }

    public void addUnit(int area) {
        if (numberOfUnitsInArea.containsKey(area)) {
            numberOfUnitsInArea.put(area, numberOfUnitsInArea.get(area) + 1);
        } else {
            numberOfUnitsInArea.put(area, 1);
        }
        numUnits++;
    }

    public int getNumUnits() {
        return numUnits;
    }

    public HashMap<Integer, Integer> getNumberOfUnitsInArea() {
        return numberOfUnitsInArea;
    }
}
