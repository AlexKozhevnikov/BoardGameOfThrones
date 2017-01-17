package com.alexeus.logic.enums;

import com.alexeus.logic.constants.TextErrors;

/**
 * Created by alexeus on 03.01.2017.
 * Перечисление событий второй колоды
 */
public enum Deck2Cards implements Happenable {
    clashOfKings,
    gameOfThrones,
    darkWingsDarkWords,
    lastDayOfSummer2,
    winterIsComing2;

    @Override
    public String getName() {
        switch(this) {
            case clashOfKings:
                return "Битва королей";
            case gameOfThrones:
                return "Игра престолов";
            case darkWingsDarkWords:
                return "Чёрные крылья, чёрные слова";
            case lastDayOfSummer2:
                return "Последние дни лета";
            case winterIsComing2:
                return "Зима близко";
        }
        return TextErrors.UNKNOWN_EVENT_ERROR;
    }

    @Override
    public boolean isWild() {
        return (this == darkWingsDarkWords || this == lastDayOfSummer2);
    }

    @Override
    public int getNumOfCards() {
        switch(this) {
            case clashOfKings:
            case gameOfThrones:
                return 3;
            case darkWingsDarkWords:
                return 2;
        }
        return 1;
    }

    @Override
    public int getDeckNumber() {
        return 2;
    }
}
