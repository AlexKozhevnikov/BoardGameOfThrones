package com.alexeus.logic.struct;

import com.alexeus.logic.enums.UnitType;

/**
 * Created by alexeus on 13.01.2017.
 * Класс представляет собой апгрейд пехотинца до рыцаря или осадной башни. Используется при сборе войск
 */
public class PawnPromotion implements Musterable {

    private UnitType targetType;

    public PawnPromotion(UnitType targetType) {
        assert (targetType == UnitType.knight || targetType == UnitType.siegeEngine);
        this.targetType = targetType;
    }

    public UnitType getTargetType() {
        return targetType;
    }

    @Override
    public int getNumMusterPoints() {
        return 1;
    }

    public String getActionString() {
        return targetType == UnitType.siegeEngine ? " улучшает пехотинца до осадной башни" : " улучшает пехотинца до рыцаря";
    }
}
