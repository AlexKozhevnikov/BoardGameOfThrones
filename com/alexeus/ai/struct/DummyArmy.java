package com.alexeus.ai.struct;

import com.alexeus.logic.enums.UnitType;

import java.util.ArrayList;

/**
 * Created by alexeus on 23.01.2017.
 * "Макеты армии" используются компьютерными игроками для генерации вариантов роспуска войск
 */
public class DummyArmy {

    private ArrayList<UnitType> unitTypes;

    public DummyArmy() {
        unitTypes = new ArrayList<>();
    }

    public boolean addUnit(UnitType unitType) {
        return unitTypes.add(unitType);
    }

    public boolean removeUnit(UnitType unitType) {
        return unitTypes.remove(unitType);
    }

    public ArrayList<UnitType> getUnits() {
        return unitTypes;
    }

    public int getSize() {
        return unitTypes.size();
    }

    public boolean hasUnitOfType(UnitType type) {
        for (UnitType unit: unitTypes) {
            if (unit == type) return true;
        }
        return false;
    }

    public int getNumUnitsOfType(UnitType type) {
        int n = 0;
        for (UnitType unit: unitTypes) {
            if (unit == type) n++;
        }
        return n;
    }

    /**
     * Метод вытаскивает из армии одного юнита и удаляет его из армии
     * @return вытащенный юнит
     */
    public UnitType pollNextUnit() {
        UnitType unit = unitTypes.get(unitTypes.size() - 1);
        unitTypes.remove(unitTypes.size() - 1);
        return unit;
    }

    public void setUnits(ArrayList<UnitType> unitTypes) {
        this.unitTypes = unitTypes;
    }
}
