package com.alexeus.logic.enums;

import com.alexeus.logic.Constants;

/**
 * Created by alexeus on 03.01.2017.
 * Перечисление событий третьей колоды
 */
public enum Deck3Cards {
    wildlingsAttack,
    devotedToSword,
    seaOfStorms,
    rainOfAutumn,
    feastForCrows,
    webOfLies,
    stormOfSwords;

    @Override
    public String toString() {
        switch(this) {
            case wildlingsAttack:
                return "Нашествие одичалых";
            case devotedToSword:
                return "Преданы мечу";
            case seaOfStorms:
                return "Море штормов";
            case rainOfAutumn:
                return "Дожди осени";
            case feastForCrows:
                return "Пир для ворон";
            case webOfLies:
                return "Паутина лжи";
            case stormOfSwords:
                return "Буря мечей";
        }
        return Constants.UNKNOWN_EVENT_ERROR;
    }

    public boolean isWild() {
        return (this != wildlingsAttack && this != devotedToSword);
    }
}
