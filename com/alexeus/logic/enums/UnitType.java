package com.alexeus.logic.enums;

import com.alexeus.logic.constants.TextErrors;

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
        return TextErrors.UNKNOWN_UFO_ERROR;
    }

    public String engName() {
        switch(this) {
            case pawn:
                return "foot";
            case knight:
                return "knight";
            case siegeEngine:
                return "tower";
            case ship:
                return "ship";
        }
        return TextErrors.UNKNOWN_UFO_ERROR;
    }

    public String nameGenitive() {
        switch(this) {
            case pawn:
                return "пехотинца";
            case knight:
                return "рыцаря";
            case siegeEngine:
                return "осадную башню";
            case ship:
                return "корабль";
        }
        return TextErrors.UNKNOWN_UFO_ERROR;
    }

    /**
     * Возвращает код отряда
     * @return код отряда
     */
    public int getCode() {
        switch (this) {
            case pawn:
                return 0;
            case knight:
                return 1;
            case siegeEngine:
                return 2;
            case ship:
                return 3;
        }
        return -1;
    }

    /**
     * Возыращает символ отряда
     * @return символ отряда
     */
    public char getLetter() {
        switch (this) {
            case pawn:
                return 'п';
            case knight:
                return 'Р';
            case siegeEngine:
                return 'Б';
            case ship:
                return 'к';
        }
        return '?';
    }
}
