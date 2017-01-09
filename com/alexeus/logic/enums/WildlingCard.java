package com.alexeus.logic.enums;

import com.alexeus.logic.Constants;

/**
 * Created by alexeus on 03.01.2017.
 * Перечисление всех карт одичалых
 */
public enum WildlingCard {
    silenceAtTheWall,
    rattleShirtRaiders,
    preemptiveRaid,
    massingOnTheMilkwater,
    skinchangerScout,
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
            case skinchangerScout:
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
        return Constants.UNKNOWN_EVENT;
    }
}
