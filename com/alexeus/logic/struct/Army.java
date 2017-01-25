package com.alexeus.logic.struct;

import com.alexeus.logic.Game;
import com.alexeus.logic.constants.MainConstants;
import com.alexeus.logic.constants.TextErrors;
import com.alexeus.logic.constants.TextInfo;
import com.alexeus.logic.enums.KillingReason;
import com.alexeus.logic.enums.UnitType;

import java.util.ArrayList;

/**
 * Created by alexeus on 08.01.2017.
 * Класс описывает войско из юнитов, находящихся в одной области
 */
public class Army {

    private ArrayList<Unit> units;

    private Game game;

    public Army(Game game) {
        units = new ArrayList<>();
        this.game = game;
    }

    public Army(Game game, ArrayList<UnitType> unitTypes, int player) {
        this.game = game;
        units = new ArrayList<>();
        for (UnitType type: unitTypes) {
            units.add(new Unit(type, player));
        }
    }

    /**
     * Возвращает всех юнитов армии
     * @return все юниты армии
     */
    public ArrayList<Unit> getUnits() {
        return units;
    }

    /**
     * Возвращает всех юнитов армии
     * @return все юниты армии
     */
    public ArrayList<Unit> getHealthyUnits() {
        ArrayList<Unit> healthyUnits = new ArrayList<>();
        for (Unit unit: units) {
            if (!unit.isWounded()) {
                healthyUnits.add(unit);
            }
        }
        return healthyUnits;
    }

    /**
     * Метод возвращает номер дома-владельца армии
     * @return номер дома, или -1, если армия пуста
     */
    public int getOwner() {
        return units.size() == 0 ? -1 : units.get(0).getHouse();
    }

    /**
     * Метод ищет в армии юнита определённого типа и возвращает его, если находит, или null, если не находит
     * @param unitType тип юнита
     * @return юнит, если нашли такого, или null, если не нашли
     */
    public Unit getUnitOfType(UnitType unitType) {
        for (Unit curUnit: units) {
            if (curUnit.getUnitType().equals(unitType)) {
                return curUnit;
            }
        }
        return null;
    }

    /**
     * Метод удаляет из армии одного юнита определённого типа
     * @param unitType тип юнита, которого нужно удалить
     * @return true, если удалось успешно удалить
     */
    public boolean deleteUnit(UnitType unitType) {
        for (Unit curUnit: units) {
            if (curUnit.getUnitType() == unitType) {
                return units.remove(curUnit);
            }
        }
        System.out.println(TextErrors.DELETE_TROOP_ERROR);
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
            System.out.println(TextErrors.DELETE_TROOP_ERROR);
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
        if (units.size() + subArmy.getSize() > MainConstants.MAX_TROOPS_IN_AREA) {
            System.out.println(TextErrors.TOO_BIG_ARMY_ERROR);
        } else if (subArmy.isEmpty()) {
            System.out.println(TextErrors.TRYING_TO_ADD_NULL_ARMY_ERROR);
        } else if (getOwner() >= 0 && subArmy.getOwner() != getOwner()) {
            System.out.println(TextErrors.TRYING_TO_ADD_ARMY_OF_OTHER_PLAYER_ERROR);
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
        if (units.size() == MainConstants.MAX_TROOPS_IN_AREA) {
            System.out.println(TextErrors.TOO_BIG_ARMY_ERROR);
        } else if (getOwner() >= 0 && getOwner() != lordFamily) {
            System.out.println(TextErrors.ALREADY_OCCUPIED_BY_OTHER_HOUSE_ERROR);
        } else {
            units.add(new Unit(unitType, lordFamily));
        }
    }

    /**
     * Добавляет нового юнита в армию
     * @param unit добавляемый юнит
     */
    public void addUnit(Unit unit) {
        if (units.size() == MainConstants.MAX_TROOPS_IN_AREA) {
            System.out.println(TextErrors.TOO_BIG_ARMY_ERROR);
        } else if (getOwner() >= 0 && getOwner() != unit.getHouse()) {
            System.out.println(TextErrors.ALREADY_OCCUPIED_BY_OTHER_HOUSE_ERROR);
        } else {
            units.add(unit);
        }
    }

    /**
     * Вызывается при проигрыше данной армии. Осадные башни уничтожаются сразу.
     * Определённое число юнитов погибает, остальные становятся ранеными.
     * @param numDoomedTroops количество юнитов, которые должны быть уничтожены
     * @param reason          причина уничтожения
     */
    public void woundAndKillTroops(int numDoomedTroops, KillingReason reason) {
        int nKilled = 0;
        ArrayList<Unit> unitsToKill = new ArrayList<>();
        for (Unit unit: units) {
            // Осадные башни и раненые уничтожаются сразу
            if (unit.getUnitType() == UnitType.siegeEngine || unit.isWounded()) {
                unitsToKill.add(unit);
            } else if (nKilled < numDoomedTroops && (unit.getUnitType() == UnitType.pawn || unit.getUnitType() == UnitType.ship)) {
                unitsToKill.add(unit);
                nKilled++;
            }
        }
        // Если не хватило убийств пешек, принимаемся за рыцарей
        if (nKilled < numDoomedTroops) {
            for (Unit unit: units) {
                if (nKilled < numDoomedTroops && unit.getUnitType() == UnitType.knight) {
                    unitsToKill.add(unit);
                    nKilled++;
                }
            }
        }
        for (Unit doomedUnit: unitsToKill) {
            killUnit(doomedUnit, reason);
        }
        // Оставшиеся в живых войска становятся ранеными
        for (Unit unit: units) {
            unit.setWounded(true);
            game.say(unit.getUnitType() + TextInfo.IS_WOUNDED);
        }
    }

    /**
     * Метод убивает всех юнитов в армии
     * @param reason причина убийства
     */
    public void killAllUnits(KillingReason reason) {
        ArrayList<Unit> unitsToKill = new ArrayList<>();
        unitsToKill.addAll(units);
        for (Unit unit: unitsToKill) {
            killUnit(unit, reason);
        }
    }

    /**
     * Метод убивает одного юнита в армии
     * @param unit   юнит, который должен быть уничтожен
     * @param reason причина убийства
     * @return true, если удалось уничтожить юнита
     */
    public boolean killUnit(Unit unit, KillingReason reason) {
        game.say(unit.getUnitType() + (unit.isWounded() ? TextInfo.IS_FINISHED :
                (unit.getUnitType() == UnitType.siegeEngine ? TextInfo.IS_DEFEATED_F : TextInfo.IS_DEFEATED_M +
                " (" + reason + ")")));
        game.unitStoreIncreased(unit.getUnitType(), unit.getHouse());
        return units.remove(unit);
    }

    /**
     * Метод убивает несколько юнитов в армии из-за снабжения
     * @param numDoomedTroops количество юнитов, которые должны быть уничтожены
     */
    public void killSomeUnits(int numDoomedTroops, KillingReason reason) {
        int nKilled = 0;
        ArrayList<Unit> unitsToKill = new ArrayList<>();
        unitsToKill.addAll(units);
        for (Unit unit: units) {
            if (nKilled < numDoomedTroops && (unit.getUnitType() == UnitType.pawn || unit.getUnitType() == UnitType.ship)) {
                unitsToKill.add(unit);
                nKilled++;
            }
        }
        // Если не хватило убийств пешек, принимаемся за рыцарей
        if (nKilled < numDoomedTroops) {
            for (Unit unit: units) {
                if (nKilled < numDoomedTroops && unit.getUnitType() == UnitType.knight) {
                    unitsToKill.add(unit);
                    nKilled++;
                }
            }
        }
        for (Unit unit: unitsToKill) {
            killUnit(unit, reason);
        }
    }

    /**
     * Метод убивает в армии одного юнита определённого типа
     * @param type тип юнита, который должен быть уничтожен
     * @return true, если удалось успешно уничтожить юнит
     */
    public boolean killUnitOfType(UnitType type) {
        Unit unitToKill = null;
        for (Unit unit: units) {
            if (unit.getUnitType() == type) {
                unitToKill = unit;
                break;
            }
        }
        if (unitToKill == null) {
            System.out.println(TextErrors.NO_TROOP_TO_KILL);
            return false;
        } else {
            return killUnit(unitToKill, KillingReason.wildlings);
        }
    }

    /**
     * Метод убивает в армии самого слабого юнита. Считается, что осадная башня слабее рыцаря, но сильнее пехотинца
     * @return true, если удалось успешно уничтожить юнит
     */
    public boolean killWeakestUnit() {
        if (getSize() == 0) {
            return false;
        } else {
            boolean hasSiegeEngines = false;
            for (Unit unit : units) {
                if (unit.getStrength() == 1) {
                    return killUnitOfType(unit.getUnitType());
                }
                if (unit.getUnitType() == UnitType.siegeEngine) {
                    hasSiegeEngines = true;
                }
            }
            return killUnitOfType(hasSiegeEngines ? UnitType.siegeEngine : UnitType.knight);
        }
    }

    /**
     * Исцелить всех юнитов в армии. Должен вызываться в начале каждого раунда.
     */
    public void healAllUnits() {
        for (Unit unit: units) {
            unit.setWounded(false);
        }
    }

    /**
     * Метод отвечает, есть ли в армии юнит определённого типа
     * @param unitType тип юнита
     * @return true, если есть
     */
    public boolean hasUnitOfType(UnitType unitType) {
        for (Unit unit: units) {
            if (unit.getUnitType() == unitType) {
                return true;
            }
        }
        return false;
    }

    /**
     * Метод отвечает, сколько в армии юнитов определённого типа
     * @param unitType тип юнита
     * @return количество таких юнитов
     */
    public int getNumUnitOfType(UnitType unitType) {
        int n = 0;
        for (Unit unit: units) {
            if (unit.getUnitType() == unitType) {
                n++;
            }
        }
        return n;
    }

    /**
     * Метод меняет тип одного юнита в армии на другой
     * @param typeFrom тип юнита, которого нужно поменять
     * @param typeTo   тип юнита, на который нужно поменять
     * @return true, если успешно удалось это сделать
     */
    public boolean changeType(UnitType typeFrom, UnitType typeTo) {
        if (typeFrom == typeTo) {
            System.out.println(TextErrors.SAME_TYPES_ERROR);
            return false;
        }
        for (Unit unit: units) {
            if (unit.getUnitType() == typeFrom) {
                unit.setUnitType(typeTo);
                return true;
            }
        }
        return false;
    }

    /**
     * Метод устанавливает владельца армии. Используется при переходе кораблей в порту от одного игрока к другому
     * @param player новый владелец
     */
    public void setOwner(int player) {
        for (Unit unit: units) {
            unit.setHouse(player);
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
        int n = units.size();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                if (i == n - 1 && i != 0) {
                    sb.append(" и ");
                } else if (i != 0) {
                    sb.append(", ");
                }
                sb.append(units.get(i).getUnitType());
            }
            sb.append(" ").append(MainConstants.HOUSE_GENITIVE[units.get(0).getHouse()]);
        } else {
            sb.append(TextInfo.NOBODY);
        }
        return sb.toString();
    }
}
