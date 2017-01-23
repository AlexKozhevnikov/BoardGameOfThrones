package com.alexeus.logic.enums;

import com.alexeus.logic.constants.TextErrors;

/**
 * Created by alexeus on 05.01.2017.
 * Перечисление всевозможных типов треков влияния (на самом деле их 3)
 */
public enum TrackType {
    ironThrone,
    valyrianSword,
    raven;

    @Override
    public String toString() {
        switch (this) {
            case ironThrone:
                return "Железный трон";
            case valyrianSword:
                return "Валирийский меч";
            case raven:
                return "Посыльный ворон";
        }
        return TextErrors.UNKNOWN_UFO_ERROR;
    }

    public String onTheTrack() {
        switch (this) {
            case ironThrone:
                return " по треку трона";
            case valyrianSword:
                return " по треку меча";
            case raven:
                return " по треку ворона";
        }
        return TextErrors.UNKNOWN_UFO_ERROR;
    }

    public int getCode() {
        switch (this) {
            case ironThrone:
                return 0;
            case valyrianSword:
                return 1;
            case raven:
                return 2;
        }
        return -1;
    }

    public static TrackType getTrack(int index) {
        switch (index) {
            case 0:
                return ironThrone;
            case 1:
                return valyrianSword;
            case 2:
                return raven;
        }
        return null;
    }
}
