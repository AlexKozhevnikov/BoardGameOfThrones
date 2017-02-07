package com.alexeus.logic.enums;

import com.alexeus.logic.constants.TextErrors;

/**
 * Created by alexeus on 03.01.2017.
 * Перечисление всех 11 типов возможных приказов
 */
public enum Order {
    raid,
    raidS,
    marchB,
    march,
    marchS,
    consolidatePower,
    consolidatePowerS,
    support,
    supportS,
    defence,
    defenceS;

    public final static int[] maxNumOrdersWithCode = {2, 1,  1, 1, 1,  2, 1,  2, 1,  2, 1};

    @Override
    public String toString() {
        switch(this) {
            case raid:
                return "Набег";
            case raidS:
                return "Набег*";
            case marchB:
                return "Поход -1";
            case march:
                return "Поход +0";
            case marchS:
                return "Поход +1*";
            case consolidatePower:
                return "Усиление власти";
            case consolidatePowerS:
                return "Усиление власти*";
            case support:
                return "Подмога";
            case supportS:
                return "Подмога +1*";
            case defence:
                return "Оборона +1";
            case defenceS:
                return "Оборона +2*";
        }
        return TextErrors.UNKNOWN_ORDER_ERROR;
    }

    // Возвращает сокращённое обозначение приказа
    public String brief() {
        switch(this) {
            case raid:
                return "R";
            case raidS:
                return "R*";
            case marchB:
                return "M-1";
            case march:
                return "M+0";
            case marchS:
                return "M+1*";
            case consolidatePower:
                return "CP";
            case consolidatePowerS:
                return "CP*";
            case support:
                return "S";
            case supportS:
                return "S*";
            case defence:
                return "D";
            case defenceS:
                return "D*";
        }
        return TextErrors.UNKNOWN_ORDER_ERROR;
    }

    public OrderType orderType() {
        switch(this) {
            case raid:
            case raidS:
                return OrderType.raid;
            case marchB:
            case march:
            case marchS:
                return OrderType.march;
            case consolidatePower:
            case consolidatePowerS:
                return OrderType.consolidatePower;
            case support:
            case supportS:
                return OrderType.support;
            case defence:
            case defenceS:
                return OrderType.defence;
        }
        return null;
    }

    // Возвращает код приказа
    public int getCode() {
        switch(this) {
            case raid:
                return 0;
            case raidS:
                return 1;
            case marchB:
                return 2;
            case march:
                return 3;
            case marchS:
                return 4;
            case consolidatePower:
                return 5;
            case consolidatePowerS:
                return 6;
            case support:
                return 7;
            case supportS:
                return 8;
            case defence:
                return 9;
            case defenceS:
                return 10;
        }
        return -1;
    }

    // Возвращает приказ по его коду
    public static Order getOrderWithCode(int code) {
        switch(code) {
            case 0:
                return raid;
            case 1:
                return raidS;
            case 2:
                return marchB;
            case 3:
                return march;
            case 4:
                return marchS;
            case 5:
                return consolidatePower;
            case 6:
                return consolidatePowerS;
            case 7:
                return support;
            case 8:
                return supportS;
            case 9:
                return defence;
            case 10:
                return defenceS;
        }
        return null;
    }

    public boolean isStar() {
        return this == raidS || this == marchS || this == consolidatePowerS || this == supportS || this == defenceS;
    }

    /**
     * Возвращает модификатор приказа. Имеет смысл только для похода, подмоги и обороны.
     * @return модификатор приказа
     */
    public int getModifier() {
        switch(this) {
            case marchB:
                return -1;
            case march:
            case support:
                return 0;
            case defence:
            case marchS:
            case supportS:
                return 1;
            case defenceS:
                return 2;
        }
        return 0;
    }

    public static Order getMarchWithMod(int mod) {
        switch (mod) {
            case 1:
                return marchS;
            case 0:
                return march;
            case -1:
                return marchB;
        }
        return null;
    }
}
