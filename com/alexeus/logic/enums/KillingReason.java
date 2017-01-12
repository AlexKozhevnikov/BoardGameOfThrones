package com.alexeus.logic.enums;

import com.alexeus.logic.constants.TextErrors;

/**
 * Created by alexeus on 11.01.2017.
 * У всего есть свои причины. У убийства - тоже
 */
public enum KillingReason {
    sword,
    supplyLimit,
    noAreaToRetreat,
    mace;

    @Override
    public String toString() {
        switch (this) {
            case sword:
                return "знак меча";
            case supplyLimit:
                return "ограничение по снабжению";
            case noAreaToRetreat:
                return "некуда отступать";
            case mace:
                return "аппетит Мейса Тирелла";
        }
        return TextErrors.UNKNOWN_UFO_ERROR;
    }
}
