package com.alexeus.logic.enums;

import com.alexeus.logic.constants.TextErrors;

/**
 * Created by alexeus on 03.01.2017.
 * Перечисление событий третьей колоды
 */
public enum Deck3Cards implements Happenable {
    wildlingsAttack,
    devotedToSword,
    seaOfStorms,
    rainOfAutumn,
    feastForCrows,
    webOfLies,
    stormOfSwords;

    @Override
    public String getName() {
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
        return TextErrors.UNKNOWN_EVENT_ERROR;
    }

    @Override
    public boolean isWild() {
        return (this != wildlingsAttack && this != devotedToSword);
    }

    @Override
    public int getNumOfCards() {
        switch(this) {
            case wildlingsAttack:
                return 3;
            case devotedToSword:
                return 2;
        }
        return 1;
    }

    @Override
    public int getDeckNumber() {
        return 3;
    }
}
