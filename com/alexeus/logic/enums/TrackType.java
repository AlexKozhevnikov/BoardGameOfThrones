package com.alexeus.logic.enums;

import com.alexeus.logic.Constants;

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
        return Constants.UNKNOWN_UFO_ERROR;
    }

    public String onTheTrack() {
        switch (this) {
            case ironThrone:
                return " по треку трона";
            case valyrianSword:
                return " по треку вотчин";
            case raven:
                return " по треку ворона";
        }
        return Constants.UNKNOWN_UFO_ERROR;
    }
}
