package com.alexeus.logic.enums;

import com.alexeus.logic.Constants;

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
                return Constants.WESTEROS_PHASE;
            case planningPhase:
                return Constants.PLANNING_PHASE;
            case ravenPhase:
                return Constants.RAVEN_PHASE;
            case raidPhase:
                return Constants.RAID_PHASE;
            case marchPhase:
                return Constants.MARCH_PHASE;
            case consolidatePowerPhase:
                return Constants.CONSOLIDATE_POWER_PHASE;
            case end:
                return Constants.END_OF_GAME;
        }
        return Constants.UNKNOWN_UFO;
    }
}
