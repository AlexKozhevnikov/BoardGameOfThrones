package com.alexeus.logic.enums;

/**
 * Created by alexeus on 13.01.2017.
 * Класс представляет собой апгрейд пехотинца до рыцаря или осадной башни. Используется при сборе войск
 */
public enum PawnPromotion implements Musterable {
    pawnToKnight,
    pawnToSiege;

    public UnitType getTargetType() {
        return this == pawnToKnight ? UnitType.knight: UnitType.siegeEngine;
    }

    @Override
    public int getNumMusterPoints() {
        return 1;
    }

    @Override
    public String getActionString() {
        return this == pawnToKnight ? " улучшает пехотинца до рыцаря " : " улучшает пехотинца до осадной башни ";
    }

    @Override
    public int getCode() {
        return this == pawnToKnight ? 4: 5;
    }
}
