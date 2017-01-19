package com.alexeus.graph.enums;

import static com.alexeus.logic.constants.TextErrors.UNKNOWN_UFO_ERROR;

/**
 * Created by alexeus on 19.01.2017.
 * Перечисление вкладок левой панели
 */
public enum TabEnum {
    event,
    house,
    fight,
    chat;

    public int getCode() {
        switch (this) {
            case event:
                return 0;
            case house:
                return 1;
            case fight:
                return 2;
            case chat:
                return 3;
        }
        System.out.println(UNKNOWN_UFO_ERROR);
        return -1;
    }
}
