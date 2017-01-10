package com.alexeus.logic.struct;

import com.alexeus.logic.Constants;
import com.alexeus.logic.enums.UnitType;

import java.util.ArrayList;

/**
 * Created by alexeus on 08.01.2017.
 * Класс описывает войско из юнитов, находящихся в одной области
 */
public class Army {

    private ArrayList<Unit> units;

    public Army() {
        units = new ArrayList<>();
    }

    /**
     * Возвращает всех юнитов армии
     * @return все юниты армии
     */
    public ArrayList<Unit> getUnits() {
        return units;
    }

    /**
     * Метод возвращает количество юнитов в данной армии
     * @return число юнитов в армии
     */
    public int getNumUnits() {
        return units.size();
    }

    /**
     * Метод возвращает номер дома-владельца армии
     * @return номер дома, или -1, если армия пуста
     */
    public int getOwner() {
        return units.size() == 0 ? -1 : units.get(0).getHouse();
    }

    /**
     * Метод удаляет из армии одного юнита определённого типа
     * @param unitType тип юнита, которого нужно удалить
     * @return true, если удалось успешно удалить
     */
    public boolean deleteUnit(UnitType unitType) {
        for (Unit curUnit: units) {
            if (curUnit.getUnitType().equals(unitType)) {
                assert(units.remove(curUnit));
                return true;
            }
        }
        System.out.println(Constants.DELETE_TROOP_ERROR);
        return false;
    }

    /**
     * Метод удаляет из армии несколько юнитов
     * @param subArmy армия из юнитов, которох нужно удалить
     * @return true,  если удалось успешно удалить
     */
    public boolean deleteSubArmy(Army subArmy) {
        boolean success = true;
        for (Unit curUnit: subArmy.getUnits()) {
            if (!deleteUnit(curUnit.getUnitType())) {
                success = false;
            }
        }
        if (!success) {
            System.out.println(Constants.DELETE_TROOP_ERROR);
        }
        return success;
    }

    /**
     * Метод удаляет всех юнитов из области
     */
    public void deleteAllUnits() {
        units.clear();
    }

    /**
     * Добавляет в армию новых юнитов
     * @param subArmy армия из добавляемых юнитов
     */
    public void addSubArmy(Army subArmy) {
        if (units.size() + subArmy.getSize() > Constants.MAX_TROOPS_IN_AREA) {
            System.out.println(Constants.TOO_BIG_ARMY_ERROR);
        } else if (subArmy.isEmpty()) {
            System.out.println(Constants.TRYING_TO_ADD_NULL_ARMY_ERROR);
        } else if (getOwner() >= 0 && subArmy.getOwner() != getOwner()) {
            System.out.println(Constants.TRYING_TO_ADD_ARMY_OF_OTHER_PLAYER_ERROR);
        } else {
            units.addAll(subArmy.getUnits());
        }
    }

    /**
     * Добавляет юнита в армию по его типу и Дому, за который он сражается
     * @param unitType   тип юнита
     * @param lordFamily номер Дома
     */
    public void addUnit(UnitType unitType, int lordFamily) {
        if (units.size() == Constants.MAX_TROOPS_IN_AREA) {
            System.out.println(Constants.TOO_BIG_ARMY_ERROR);
        } else if (getOwner() >= 0 && getOwner() != lordFamily) {
            System.out.println(Constants.ALREADY_OCCUPIED_BY_OTHER_HOUSE_ERROR);
        } else {
            units.add(new Unit(unitType, lordFamily));
        }
    }

    /**
     * Добавляет нового юнита в армию
     * @param unit добавляемый юнит
     */
    public void addUnit(Unit unit) {
        if (units.size() == Constants.MAX_TROOPS_IN_AREA) {
            System.out.println(Constants.TOO_BIG_ARMY_ERROR);
        } else if (getOwner() >= 0 && getOwner() != unit.getHouse()) {
            System.out.println(Constants.ALREADY_OCCUPIED_BY_OTHER_HOUSE_ERROR);
        } else {
            units.add(unit);
        }
    }

    /**
     * Метод проверяет, пуста ли армия
     * @return true, если пуста
     */
    public boolean isEmpty() {
        return units.size() == 0;
    }

    /**
     * Метод возращает количество юнитов в армии
     * @return количество юнитов
     */
    public int getSize() {
        return units.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (units.size() > 0) {
            boolean firstFlag = true;
            for (Unit unit: units) {
                if (firstFlag) {
                    firstFlag = false;
                } else {
                    sb.append(", ");
                }
                sb.append(unit.getUnitType());
            }
            sb.append(" ").append(Constants.HOUSE_GENITIVE[units.get(0).getHouse()]);
        } else {
            sb.append(Constants.NOBODY);
        }
        return sb.toString();
    }
}
