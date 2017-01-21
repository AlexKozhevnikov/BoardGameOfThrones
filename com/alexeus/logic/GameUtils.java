package com.alexeus.logic;

import java.util.HashMap;
import java.util.Map;

import static com.alexeus.logic.constants.MainConstants.MAX_TROOPS_IN_AREA;
import static com.alexeus.logic.constants.MainConstants.SUPPLY_NUM_GROUPS;

/**
 * Created by alexeus on 21.01.2017.
 * Класс содержит некоторые полезные для игры функции, которые можно абстрагировать от переменных конкретной партии.
 * Создан для того, чтобы рагрузить основной класс игры Game.
 */
class GameUtils {

    /**
     * Метод проверяет, нарушается ли предел снабжения при данном расположении войск игрока
     * @param areasWithTroops карта с парами область-количество юнитов
     * @param supplyLevel     уровень снабжения игрока
     * @return true, если предел снабжения не нарушается
     */
    public static boolean supplyTest(HashMap<Integer, Integer> areasWithTroops, int supplyLevel) {
        int[] nFreeGroups = new int[MAX_TROOPS_IN_AREA - 1];
        System.arraycopy(SUPPLY_NUM_GROUPS[supplyLevel], 0, nFreeGroups, 0, SUPPLY_NUM_GROUPS[supplyLevel].length);
        for (Map.Entry<Integer, Integer> entry: areasWithTroops.entrySet()) {
            int armySize = entry.getValue();
            // Если это армия, то занимаем свободную ячейку. Если это не удаётся сделать, значит предел снабжения превышен.
            if (armySize > 1) {
                boolean isThereFreeCell = false;
                for (int cellSize = armySize - 2; cellSize < MAX_TROOPS_IN_AREA - 1; cellSize++) {
                    if (nFreeGroups[cellSize] > 0) {
                        isThereFreeCell = true;
                        nFreeGroups[cellSize]--;
                        break;
                    }
                }
                if (!isThereFreeCell) {
                    return false;
                }
            }
        }
        return true;
    }
}
