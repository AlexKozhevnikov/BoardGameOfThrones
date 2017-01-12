package com.alexeus.logic.struct;

import com.alexeus.logic.constants.TextErrors;
import com.alexeus.logic.enums.SideOfBattle;
import com.alexeus.logic.enums.UnitType;

import java.util.ArrayList;

/**
 * Created by alexeus on 10.01.2017.
 * Класс представляет собой информацию об одной битве
 */
public class BattleInfo {

    private int areaOfBattle;

    private int attacker;

    private int defender;

    private int attackerStrength;

    private int defenderStrength;

    private boolean isThereACastle;

    // Атакующие юниты и им сочувствующие
    private ArrayList<Unit> attackerUnits;

    // Защищающиеся юниты и им сочувствующие
    private ArrayList<Unit> defenderUnits;

    public BattleInfo(int attacker, int defender, int areaOfBattle, boolean isThereACastle) {
        this.attacker = attacker;
        this.defender = defender;
        this.areaOfBattle = areaOfBattle;
        attackerStrength = 0;
        defenderStrength = 0;
        attackerUnits = new ArrayList<>();
        defenderUnits = new ArrayList<>();
        this.isThereACastle = isThereACastle;
    }

    /**
     * Метод добавляет боевую силу к одной из сражающихся сторон
     * @param side     сторона, которой добавляется боевая сила
     * @param strength боевая сила
     */
    public void addStrength(SideOfBattle side, int strength) {
        switch (side) {
            case attacker:
                attackerStrength += strength;
                break;
            case defender:
                defenderStrength += strength;
        }
    }

    /**
     * Метод добавляет армию из юнитов к одной из сражающихся сторон, попутно увеличивая соответствующую боевую силу
     * @param side сторона, которой добавляется армия
     * @param army армия
     */
    public void addArmyToSide (SideOfBattle side, Army army) {
        for (Unit unit: army.getUnits()) {
            if (!unit.isWounded()) {
                switch (side) {
                    case attacker:
                        attackerUnits.add(unit);
                        break;
                    case defender:
                        defenderUnits.add(unit);
                        break;
                }
                if (unit.getUnitType() != UnitType.siegeEngine || isThereACastle) {
                    switch (side) {
                        case attacker:
                            attackerStrength += unit.getStrength();
                            break;
                        case defender:
                            defenderStrength += unit.getStrength();
                            break;
                    }
                }
            }
        }
    }

    /**
     * Метод удаляет из армии одной из сторон одного юнита, попутно пересчитывая боевую силу
     * @param side сторона, у которой удаляется юнит
     * @param unit ссылка на юнита
     * @return true, если удалось удалить юнита
     */
    public boolean deleteUnit (SideOfBattle side, Unit unit) {
        boolean success = false;
        switch (side) {
            case attacker:
                success = attackerUnits.remove(unit);
                if (success) {
                    if (unit.getUnitType() != UnitType.siegeEngine || isThereACastle) {
                        attackerStrength -= unit.getStrength();
                    }
                } else {
                    System.out.println(TextErrors.DELETE_TROOP_ERROR);
                }
                break;
            case defender:
                success = defenderUnits.remove(unit);
                if (success) {
                    if (unit.getUnitType() != UnitType.siegeEngine) {
                        defenderStrength -= unit.getStrength();
                    }
                } else {
                    System.out.println(TextErrors.DELETE_TROOP_ERROR);
                }
                break;
        }
        return success;
    }

    /**
     * Метод удаляет из армии одной из сторон подармию, попутно пересчитывая боевую силу
     * @param side    сторона, у которой удаляется подармия
     * @param subArmy ссылка на подармию
     * @return true, если удалось удалить армию
     */
    public boolean deleteArmy (SideOfBattle side, Army subArmy) {
        boolean success = true;
        for (Unit unit: subArmy.getUnits()) {
            if (!deleteUnit(side, unit)) {
                success = false;
                System.out.println(TextErrors.DELETE_ARMY_ERROR);
            }
        }
        return success;
    }

    /**
     * Метод возвращает количество кораблей, не принадлежащих определённому игроку, на определённой стороне.
     * Нужен для учёта эффектов карты "Салладор Саан"
     * @param player игрок (базово - Баратеон, но всё-таки)
     * @param side   сторона боя
     * @return число кораблей
     */
    public int getNumEnemyShips (int player, SideOfBattle side) {
        if (side == SideOfBattle.neutral) {
            System.out.println(TextErrors.COUNT_UNITS_ON_NEUTRAL_SIDE_ERROR);
            return 0;
        }
        int nShips = 0;
        for (Unit unit : side == SideOfBattle.attacker ? attackerUnits : defenderUnits) {
            if (unit.getUnitType() == UnitType.ship && unit.getHouse() != player) {
                nShips++;
            }
        }
        return nShips;
    }

    /**
     * Метод возвращает количество юнитов определённого типа, принадлежащих определённому игроку определённой стороны.
     * Нужен для учёта эффектов карт "Виктарион Грейджой" и "Киван Ланнистер"
     * @param side     сторона боя
     * @param unitType тип юнита
     * @return число дружестенных юнитов такого типа
     */
    public int getNumFriendlyUnits (SideOfBattle side, UnitType unitType) {
        if (side == SideOfBattle.neutral) {
            System.out.println(TextErrors.COUNT_UNITS_ON_NEUTRAL_SIDE_ERROR);
            return 0;
        }
        int nUnits = 0;
        int player = side == SideOfBattle.attacker ? attacker : defender;
        for (Unit unit : side == SideOfBattle.attacker ? attackerUnits : defenderUnits) {
            if (unit.getUnitType() == unitType && unit.getHouse() == player) {
                nUnits++;
            }
        }
        return nUnits;
    }

    public int getAreaOfBattle() {
        return areaOfBattle;
    }

    public int getAttacker() {
        return attacker;
    }

    public int getDefender() {
        return defender;
    }

    public int getAttackerStrength() {
        return attackerStrength;
    }

    public int getDefenderStrength() {
        return defenderStrength;
    }
}
