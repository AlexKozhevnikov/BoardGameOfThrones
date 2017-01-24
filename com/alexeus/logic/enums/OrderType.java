package com.alexeus.logic.enums;

/**
 * Created by alexeus on 03.01.2017.
 * Перечисление всех 5 типов приказов.
 */
public enum OrderType {
    raid,
    march,
    consolidatePower,
    support,
    defence;

    public Order mainVariant() {
        switch (this) {
            case raid:
                return Order.raid;
            case march:
                return Order.march;
            case consolidatePower:
                return Order.consolidatePower;
            case support:
                return Order.support;
            case defence:
                return Order.defence;
        }
        return null;
    }
}
