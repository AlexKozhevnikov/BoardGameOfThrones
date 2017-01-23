package com.alexeus.logic.enums;

import com.alexeus.logic.constants.TextErrors;

/**
 * Created by alexeus on 03.01.2017.
 * Перечисление всех карт одичалых
 */
public enum WildlingCard {
    silenceAtTheWall,
    rattleShirtRaiders,
    preemptiveRaid,
    massingOnTheMilkwater,
    shapechangerScout,
    crowKillers,
    hordeDescends,
    mammothRiders,
    aKingBeyondTheWall;

    @Override
    public String toString() {
        switch(this) {
            case silenceAtTheWall:
                return "Тишина за стеной";
            case rattleShirtRaiders:
                return "Разбойники гремучей рубашки";
            case preemptiveRaid:
                return "Передовой отряд";
            case massingOnTheMilkwater:
                return "Сбор на Молоководной";
            case shapechangerScout:
                return "Разведчик-оборотень";
            case crowKillers:
                return "Убийцы ворон";
            case hordeDescends:
                return "Нашествие орды";
            case mammothRiders:
                return "Наездники на мамонтах";
            case aKingBeyondTheWall:
                return "Король за стеной";
        }
        return TextErrors.UNKNOWN_EVENT_ERROR;
    }

    public int getCode() {
        switch(this) {
            case silenceAtTheWall:
                return 0;
            case rattleShirtRaiders:
                return 1;
            case preemptiveRaid:
                return 2;
            case massingOnTheMilkwater:
                return 3;
            case shapechangerScout:
                return 4;
            case crowKillers:
                return 5;
            case hordeDescends:
                return 6;
            case mammothRiders:
                return 7;
            case aKingBeyondTheWall:
                return 8;
        }
        return -1;
    }
}
