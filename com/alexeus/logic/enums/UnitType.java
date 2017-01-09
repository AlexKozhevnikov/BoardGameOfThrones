package com.alexeus.logic.enums;

import com.alexeus.logic.Constants;

/**
 * Created by alexeus on 08.01.2017.
 * Перечисление типов юнитов в игре.
 */
public enum UnitType {
    pawn,
    knight,
    siegeEngine,
    ship;

    public int getStrength() {
        switch(this) {
            case pawn:
            case ship:
                return 1;
            case knight:
                return 2;
            case siegeEngine:
                return 4;
        }
        return 0;
    }

    @Override
    public String toString() {
        switch(this) {
            case pawn:
                return "Пехотинец";
            case knight:
                return "Рыцарь";
            case siegeEngine:
                return "Осадная башня";
            case ship:
                return "Корабль";
        }
        return Constants.UNKNOWN_UFO;
    }
}
