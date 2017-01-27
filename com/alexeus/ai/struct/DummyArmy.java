package com.alexeus.ai.struct;

import com.alexeus.logic.constants.TextInfo;
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

    public UnitType getWeakestUnit() {
        boolean hasSiegeEngines = false;
        for (UnitType unit : unitTypes) {
            if (unit.getStrength() == 1) {
                return unit;
            }
            if (unit == UnitType.siegeEngine) {
                hasSiegeEngines = true;
            }
        }
        return hasSiegeEngines ? UnitType.siegeEngine : UnitType.knight;
    }

    public int getStrength(boolean isAttackCastle) {
        int strength = 0;
        for (UnitType unit: unitTypes) {
            if (isAttackCastle || unit != UnitType.siegeEngine) {
                strength += unit.getStrength();
            }
        }
        return strength;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int n = unitTypes.size();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                if (i == n - 1 && i != 0) {
                    sb.append(" и ");
                } else if (i != 0) {
                    sb.append(", ");
                }
                sb.append(unitTypes.get(i));
            }
        } else {
            sb.append(TextInfo.NOBODY);
        }
        return sb.toString();
    }
}
