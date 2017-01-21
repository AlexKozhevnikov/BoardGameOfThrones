package com.alexeus.logic.struct;

import com.alexeus.logic.enums.UnitType;

/**
 * Created by alexeus on 09.01.2017.
 * Класс представляет собой нормального, вещественного юнита на карте Вестероса
 */
public class Unit {

    /**
     * Тип юнита
     */
    private UnitType unitType;
    /**
     * Номер Дома, за который сражается юнит
     */
    private int house;
    /**
     * Показатель ранености юнита (раненые имеют силу 0 и погибают, если проигрывают битву)
     */
    private boolean isWounded;

    public Unit(UnitType type, int lordFamily) {
        unitType = type;
        house = lordFamily;
        isWounded = false;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public int getHouse() {
        return house;
    }

    public void setHouse(int house) {
        this.house = house;
    }

    public void setWounded(boolean isWounded) {
        this.isWounded = isWounded;
    }

    public boolean isWounded() {
        return isWounded;
    }

    public int getStrength() {
        return this.getUnitType().getStrength();
    }
}
