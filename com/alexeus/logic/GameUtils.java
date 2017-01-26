package com.alexeus.logic;

import com.alexeus.logic.constants.TextInfo;
import com.alexeus.logic.enums.UnitType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.alexeus.logic.constants.MainConstants.MAX_TROOPS_IN_AREA;
import static com.alexeus.logic.constants.MainConstants.SUPPLY_NUM_GROUPS;

/**
 * Created by alexeus on 21.01.2017.
 * Класс содержит некоторые полезные для игры функции, которые можно абстрагировать от переменных конкретной партии.
 * Создан для того, чтобы рагрузить основной класс игры Game.
 */
public class GameUtils {

    /**
     * Метод проверяет, нарушается ли предел снабжения при данном расположении войск игрока
     * @param areasWithTroops карта с парами область-количество юнитов
     * @param supplyLevel     уровень снабжения игрока
     * @return true, если предел снабжения не нарушается
     */
    public static boolean supplyTest(Map<Integer, Integer> areasWithTroops, int supplyLevel) {
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

    /**
     * Метод возвращает размер армии, который нарушает предел снабжения для данных областей с юнитами
     * @param areasWithTroops карта с областями и числами юнитов в них
     * @param supplyLevel     уровень снабжения игрока
     * @return размер армии
     */
    public static int getBreakingArmySize(Map<Integer, Integer> areasWithTroops, int supplyLevel) {
        int[] nFreeGroups = new int[MAX_TROOPS_IN_AREA - 1];
        System.arraycopy(SUPPLY_NUM_GROUPS[supplyLevel], 0, nFreeGroups, 0, SUPPLY_NUM_GROUPS[supplyLevel].length);
        for (int armySize = MAX_TROOPS_IN_AREA; armySize >= 2; armySize--) {
            for (Map.Entry<Integer, Integer> entry: areasWithTroops.entrySet()) {
                if (entry.getValue() != armySize) continue;
                // Если это армия, пробуем занять свободную ячейку. Если это не удаётся сделать, значит предел снабжения превышен.
                boolean isThereFreeCell = false;
                for (int cellSize = armySize - 2; cellSize < MAX_TROOPS_IN_AREA - 1; cellSize++) {
                    if (nFreeGroups[cellSize] > 0) {
                        isThereFreeCell = true;
                        nFreeGroups[cellSize]--;
                        break;
                    }
                }
                if (!isThereFreeCell) {
                    return armySize;
                }
            }
        }
        return -1;
    }

    /**
     * Метод возвращает самый большой свободный размер армии при данном уровне снабжения
     * @param areasWithTroops карта с областями и числами юнитов в них
     * @param supplyLevel     уровень снабжения игрока
     * @return размер армии
     */
    public static int getBiggestFreeArmySize(Map<Integer, Integer> areasWithTroops, int supplyLevel) {
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
                    return -1;
                }
            }
        }
        for (int cellSize = 2; cellSize >= 0; cellSize--) {
            if (nFreeGroups[cellSize] > 0) {
                return cellSize + 2;
            }
        }
        return 1;
    }

    /**
     * Метод составляет и возвращает описание группы юнитов в виде кратких сокращений
     * @param unitTypes юниты
     * @return короткая строка
     */
    static String getUnitsShortString(ArrayList<UnitType> unitTypes) {
        StringBuilder sb = new StringBuilder();
        int n = unitTypes.size();
        if (n > 0) {
            for (UnitType type : unitTypes) {
                sb.append(type.getLetter());
            }
        } else {
            sb.append(TextInfo.NOBODY);
        }
        return sb.toString();
    }
}
