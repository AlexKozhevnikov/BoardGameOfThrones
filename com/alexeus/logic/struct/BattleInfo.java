package com.alexeus.logic.struct;

import com.alexeus.logic.Game;
import com.alexeus.logic.constants.TextErrors;
import com.alexeus.logic.enums.*;

import java.util.ArrayList;

import static com.alexeus.logic.constants.MainConstants.NUM_PLAYER;

/**
 * Created by alexeus on 10.01.2017.
 * Класс представляет собой информацию об одной битве
 */
public class BattleInfo {

    private int areaOfBattle;

    private boolean isThereACastle;

    private int marchModifier, defenceModifier, garrisonModifier;

    private SideOfBattle sideWhereSwordUsed;

    private int[] playerStrength, playerNumSupports;

    private SideOfBattle[] supportOfPlayer = new SideOfBattle[NUM_PLAYER];

    private int[] playerOnSide = new int[2];
    private int[] swordsOnSide = new int[2];
    private int[] towersOnSide = new int[2];
    private int[] cardStrengthOnSide = new int[2];
    private int[] bonusStrengthOnSide = new int[2];
    private HouseCard[] houseCardOfSide = new HouseCard[2];

    private boolean isFightResolved;
    private SideOfBattle winnerSide;

    // Атакующие юниты и им сочувствующие
    private ArrayList<Unit> attackerUnits = new ArrayList<>();

    // Защищающиеся юниты и им сочувствующие
    private ArrayList<Unit> defenderUnits = new ArrayList<>();

    public BattleInfo() {
    }

    public void setNewBattle(int attacker, int defender, int areaOfBattle, int marchModifier, boolean isThereACastle) {
        playerOnSide[0] = attacker;
        playerOnSide[1] = defender;
        this.areaOfBattle = areaOfBattle;
        marchModifier = 0;
        defenceModifier = 0;
        garrisonModifier = 0;
        attackerUnits.clear();
        defenderUnits.clear();
        this.isThereACastle = isThereACastle;
        for (int player = 0; player < NUM_PLAYER; player++) {
            playerStrength[player] = 0;
            supportOfPlayer[player] = null;
            playerNumSupports[player] = 0;
        }
        for (int side = 0; side < 2; side++) {
            swordsOnSide[side] = 0;
            towersOnSide[side] = 0;
            cardStrengthOnSide[side] = 0;
            bonusStrengthOnSide[side] = 0;
            houseCardOfSide[side] = null;
        }
        sideWhereSwordUsed = null;
        winnerSide = null;
        isFightResolved = false;
    }

    /**
     * Метод добавляет армию поддержки к одной из сражающихся сторон, попутно пересчитывая соответствующие переменные
     * @param side           сторона, которую поддерживают
     * @param supportingArmy поддерживающая армия
     */
    public void addSupportingArmyToSide(SideOfBattle side, Army supportingArmy) {
        int house = supportingArmy.getOwner();
        supportOfPlayer[house] = side;
        playerNumSupports[house]++;
        addArmyToSide(side, supportingArmy);
    }

    /**
     * Метод добавляет армию из юнитов к одной из сражающихся сторон, попутно увеличивая соответствующую боевую силу
     * @param side сторона, которой добавляется армия
     * @param army армия
     */
    public void addArmyToSide (SideOfBattle side, Army army) {
        int house = army.getOwner();
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
                if (unit.getUnitType() != UnitType.siegeEngine || side == SideOfBattle.attacker && isThereACastle) {
                    playerStrength[house] += unit.getStrength();
                    playerStrength[house] += unit.getStrength();
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
                        playerStrength[unit.getHouse()] -= unit.getStrength();
                    }
                } else {
                    System.out.println(TextErrors.DELETE_TROOP_ERROR);
                }
                break;
            case defender:
                success = defenderUnits.remove(unit);
                if (success) {
                    if (unit.getUnitType() != UnitType.siegeEngine) {
                        playerStrength[unit.getHouse()] -= unit.getStrength();
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
        int player = playerOnSide[side.getCode()];
        for (Unit unit : side == SideOfBattle.attacker ? attackerUnits : defenderUnits) {
            if (unit.getUnitType() == unitType && unit.getHouse() == player) {
                nUnits++;
            }
        }
        return nUnits;
    }

    /**
     * Метод рассчитывает стандартные переменные боя - сила карты, мечи, башни и бонусы к боевой силе из-за свойств карт
     */
    public void countBattleVariables() {
        for (int curSide = 0; curSide < 2; curSide++) {
            cardStrengthOnSide[curSide] = houseCardOfSide[curSide] == null ? 0 : houseCardOfSide[curSide].getStrength();
            swordsOnSide[curSide] = houseCardOfSide[curSide] == null ? 0 : houseCardOfSide[curSide].getNumSwords();
            towersOnSide[curSide] = houseCardOfSide[curSide] == null ? 0 : houseCardOfSide[curSide].getNumTowers();
            bonusStrengthOnSide[curSide] = 0;
        }

        Game game = Game.getInstance();
        for (int side = 0; side < 2; side++) {
            if (houseCardOfSide[side] == null) continue;
            if (houseCardOfSide[side].getCardInitiative() == CardInitiative.bonus) {
                switch (houseCardOfSide[side]) {
                    case stannisBaratheon:
                        if (!game.isHigherOnThrone(playerOnSide[side], playerOnSide[1 - side])) {
                            bonusStrengthOnSide[side] = 1;
                        }
                        break;
                    case serDavosSeaworth:
                        if (!game.isStannisActive()) {
                            bonusStrengthOnSide[side] = 1;
                            swordsOnSide[side] = 1;
                        }
                        break;
                    case salladhorSaan:
                        if (getNumSupportsOnSide(side) > 0) {
                            bonusStrengthOnSide[side] -= getNumEnemyShips(playerOnSide[side],
                                    side == 0 ? SideOfBattle.attacker : SideOfBattle.defender);
                            bonusStrengthOnSide[1 - side] -= getNumEnemyShips(playerOnSide[side],
                                    side == 0 ? SideOfBattle.defender : SideOfBattle.attacker);
                        }
                        break;
                    case serKevanLannister:
                        if (side == 0) {
                            bonusStrengthOnSide[side] += getNumFriendlyUnits(SideOfBattle.attacker, UnitType.pawn);
                        }
                        break;
                    case catelynStark:
                        int defenceBonus = game.getDefenceBonusInArea(areaOfBattle);
                        if (side == 1 && defenceBonus > 0) {
                            bonusStrengthOnSide[side] += defenceBonus;
                        }
                        break;
                    case nymeriaSand:
                        if (side == 0) {
                            swordsOnSide[side]++;
                        } else {
                            towersOnSide[side]++;
                        }
                        break;
                    case ashaGreyjoy:
                        if (getNumSupportsOnSide(side) == 0) {
                            swordsOnSide[side] += 2;
                            towersOnSide[side]++;
                        }
                        break;
                    case theonGreyjoy:
                        if (side == 1 && game.getMap().getNumCastle(areaOfBattle) > 0) {
                            bonusStrengthOnSide[side] = 1;
                            swordsOnSide[side] = 1;
                        }
                        break;
                    case victarionGreyjoy:
                        // Салладор Саан подавляет свойство Виктариона
                        if (houseCardOfSide[1 - side] == HouseCard.salladhorSaan) break;
                        if (side == 0) {
                            bonusStrengthOnSide[side] += getNumFriendlyUnits(SideOfBattle.attacker, UnitType.ship);
                        }
                        break;
                    case balonGreyjoy:
                        cardStrengthOnSide[1 - side] = 0;
                        break;
                }
            }
        }
    }

    public SideOfBattle resolveFight() {
        int attackerStrength = getStrengthOnSide(SideOfBattle.attacker);
        int defenderStrength = getStrengthOnSide(SideOfBattle.defender);
        winnerSide = attackerStrength > defenderStrength || attackerStrength == defenderStrength &&
                Game.getInstance().isHigherOnSword(playerOnSide[0], playerOnSide[1]) ?
                SideOfBattle.attacker : SideOfBattle.defender;
        isFightResolved = true;
        return winnerSide;
    }

    public int getNumSupportsOnSide(int side) {
        int n = 0;
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (supportOfPlayer[player].getCode() == side) {
                n += playerNumSupports[player];
            }
        }
        return n;
    }

    public int getAreaOfBattle() {
        return areaOfBattle;
    }

    public int getAttacker() {
        return playerOnSide[0];
    }

    public int getDefender() {
        return playerOnSide[1];
    }

    public int getStrengthOnSide(SideOfBattle sideOfBattle) {
        int strength = cardStrengthOnSide[sideOfBattle.getCode()] + bonusStrengthOnSide[sideOfBattle.getCode()];
        if (sideOfBattle == SideOfBattle.attacker) {
            strength += marchModifier;
        } else if (sideOfBattle == SideOfBattle.defender) {
            strength += defenceModifier + garrisonModifier;
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (supportOfPlayer[player] == sideOfBattle || player == playerOnSide[sideOfBattle.getCode()]) {
                strength += playerStrength[player];
            }
        }
        if (sideWhereSwordUsed == sideOfBattle) {
            strength++;
        }
        return strength;
    }

    public void useSwordOnSide(SideOfBattle sideOfBattle) {
        sideWhereSwordUsed = sideOfBattle;
    }

    public void setHouseCardForPlayer(int player, HouseCard card) {
        int side = player == playerOnSide[0] ? 0 : 1;
        houseCardOfSide[side] = card;
    }

    public void deleteSupportOfPlayer(int player) {
        playerNumSupports[player]--;
    }

    public int getMarchModifier() {
        return marchModifier;
    }

    public int getDefenceModifier() {
        return defenceModifier;
    }

    public void setDefenceModifier(int defenceModifier) {
        this.defenceModifier = defenceModifier;
    }

    public int getGarrisonModifier() {
        return garrisonModifier;
    }

    public void setGarrisonModifier(int garrisonModifier) {
        this.garrisonModifier = garrisonModifier;
    }

    public boolean getIsFightResolved() {
        return isFightResolved;
    }

    public int getSwordsOnSide(int side) {
        return swordsOnSide[side];
    }

    public int getTowersOnSide(int side) {
        return towersOnSide[side];
    }

    public int getNumKilled() {
        return Math.max(0, swordsOnSide[winnerSide.getCode()] - towersOnSide[1 - winnerSide.getCode()]);
    }
}
