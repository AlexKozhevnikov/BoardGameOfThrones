package com.alexeus.logic.struct;

import com.alexeus.logic.Constants;
import com.alexeus.logic.enums.SideOfBattle;
import com.alexeus.logic.enums.UnitType;

import java.util.ArrayList;
import java.util.HashMap;

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
                    System.out.println(Constants.DELETE_TROOP_ERROR);
                }
                break;
            case defender:
                success = defenderUnits.remove(unit);
                if (success) {
                    if (unit.getUnitType() != UnitType.siegeEngine) {
                        defenderStrength -= unit.getStrength();
                    }
                } else {
                    System.out.println(Constants.DELETE_TROOP_ERROR);
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
                System.out.println(Constants.DELETE_ARMY_ERROR);
            }
        }
        return success;
    }

    public int getAreaOfBattle() {
        return areaOfBattle;
    }

    public void setAreaOfBattle(int areaOfBattle) {
        this.areaOfBattle = areaOfBattle;
    }

    public int getAttacker() {
        return attacker;
    }

    public void setAttacker(int attacker) {
        this.attacker = attacker;
    }

    public int getDefender() {
        return defender;
    }

    public void setDefender(int defender) {
        this.defender = defender;
    }

    public int getAttackerStrength() {
        return attackerStrength;
    }

    public void setAttackerStrength(int attackerStrength) {
        this.attackerStrength = attackerStrength;
    }

    public int getDefenderStrength() {
        return defenderStrength;
    }

    public void setDefenderStrength(int defenderStrength) {
        this.defenderStrength = defenderStrength;
    }
}
