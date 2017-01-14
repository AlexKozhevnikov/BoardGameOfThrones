package com.alexeus.logic.enums;

import com.alexeus.logic.constants.TextErrors;

/**
 * Created by alexeus on 03.01.2017.
 * Перечисление событий первой колоды
 */
public enum Deck1Cards {
    muster,
    supply,
    throneOfSwords,
    lastDayOfSummer1,
    winterIsComing1;

    public String getName() {
        switch(this) {
            case muster:
                return "Сбор войск";
            case supply:
                return "Снабжение";
            case throneOfSwords:
                return "Трон из клинков";
            case lastDayOfSummer1:
                return "Последние дни лета";
            case winterIsComing1:
                return "Зима близко";
        }
        return TextErrors.UNKNOWN_EVENT_ERROR;
    }

    public boolean isWild() {
        return (this == throneOfSwords || this == lastDayOfSummer1);
    }

    public int getNumOfCards() {
        switch(this) {
            case muster:
            case supply:
                return 3;
            case throneOfSwords:
                return 2;
        }
        return 1;
    }
}
