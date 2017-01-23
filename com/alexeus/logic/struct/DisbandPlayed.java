package com.alexeus.logic.struct;

import com.alexeus.logic.enums.UnitType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alexeus on 22.01.2017.
 * Класс представляет собой вариант роспуска войск.
 */
public class DisbandPlayed {

    private HashMap<Integer, ArrayList<UnitType>> disbandUnits;

    private int numDisbands;

    public DisbandPlayed() {
        numDisbands = 0;
    }

    public void addDisbandedUnit(int area, UnitType type) {
        if (disbandUnits.containsKey(area)) {
            disbandUnits.get(area).add(type);
        } else {
            ArrayList<UnitType> newArmy = new ArrayList<>();
            newArmy.add(type);
            disbandUnits.put(area, newArmy);
        }
        numDisbands++;
    }

    public int getNumberDisbands() {
        return numDisbands;
    }

    public HashMap<Integer, ArrayList<UnitType>> getDisbandUnits() {
        return disbandUnits;
    }
}
