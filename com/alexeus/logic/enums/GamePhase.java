package com.alexeus.logic.enums;

import com.alexeus.logic.constants.TextInfo;
import com.alexeus.logic.constants.TextErrors;

/**
 * Created by alexeus on 03.01.2017.
 * Перечисление всемозможных фаз игры
 */
public enum GamePhase {
    westerosPhase,
    planningPhase,
    ravenPhase,
    raidPhase,
    marchPhase,
    consolidatePowerPhase,
    end;

    @Override
    public String toString() {
        switch(this) {
            case westerosPhase:
                return TextInfo.WESTEROS_PHASE;
            case planningPhase:
                return TextInfo.PLANNING_PHASE;
            case ravenPhase:
                return TextInfo.RAVEN_PHASE;
            case raidPhase:
                return TextInfo.RAID_PHASE;
            case marchPhase:
                return TextInfo.MARCH_PHASE;
            case consolidatePowerPhase:
                return TextInfo.CONSOLIDATE_POWER_PHASE;
            case end:
                return TextInfo.END_OF_GAME;
        }
        return TextErrors.UNKNOWN_UFO_ERROR;
    }
}
