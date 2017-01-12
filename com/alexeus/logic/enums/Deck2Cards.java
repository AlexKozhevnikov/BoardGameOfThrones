package com.alexeus.logic.enums;

import com.alexeus.logic.constants.TextErrors;

/**
 * Created by alexeus on 03.01.2017.
 * Перечисление событий второй колоды
 */
public enum Deck2Cards {
    clashOfKings,
    gameOfThrones,
    darkWingsDarkWords,
    lastDayOfSummer,
    winterIsComing;

    @Override
    public String toString() {
        switch(this) {
            case clashOfKings:
                return "Битва королей";
            case gameOfThrones:
                return "Игра престолов";
            case darkWingsDarkWords:
                return "Чёрные крылья, чёрные слова";
            case lastDayOfSummer:
                return "Последние дни лета";
            case winterIsComing:
                return "Зима близко";
        }
        return TextErrors.UNKNOWN_EVENT_ERROR;
    }

    public boolean isWild() {
        return (this == darkWingsDarkWords || this == lastDayOfSummer);
    }
}
