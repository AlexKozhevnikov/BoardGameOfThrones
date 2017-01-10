package com.alexeus.logic.enums;

import com.alexeus.logic.Constants;

/**
 * Created by alexeus on 03.01.2017.
 * Перечисление событий первой колоды
 */
public enum Deck1Cards {
    muster,
    supply,
    throneOfSwords,
    lastDayOfSummer,
    winterIsComing;

    @Override
    public String toString() {
        switch(this) {
            case muster:
                return "Сбор войск";
            case supply:
                return "Снабжение";
            case throneOfSwords:
                return "Трон из клинков";
            case lastDayOfSummer:
                return "Последние дни лета";
            case winterIsComing:
                return "Зима близко";
        }
        return Constants.UNKNOWN_EVENT_ERROR;
    }

    public boolean isWild() {
        return (this == throneOfSwords || this == lastDayOfSummer);
    }
}
