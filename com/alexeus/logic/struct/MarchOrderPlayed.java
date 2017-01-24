package com.alexeus.logic.struct;

import com.alexeus.logic.Game;
import com.alexeus.logic.enums.UnitType;
import com.alexeus.map.GameOfThronesMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.alexeus.logic.constants.MainConstants.MAX_TROOPS_IN_AREA;
import static com.alexeus.logic.constants.MainConstants.NUM_UNIT_TYPES;
import static com.alexeus.logic.constants.MainConstants.TROOPS_HASH_MULTIPLYER;
import static com.alexeus.map.GameOfThronesMap.NUM_AREA;

/**
 * Created by alexeus on 10.01.2017.
 * Класс описывает определённый вариант розыгрыша одного приказа похода
 */
public class MarchOrderPlayed {

    // Код области, поход в которой разыгрывается
    private int areaFrom;
    // Карта с областями, в которые направляются юниты, и юнитами, которые направляются в области. Может быть пустой.
    private HashMap<Integer, ArrayList<UnitType>> destinationsOfMarch;
    // Оставляем ли жетон власти на покидаемой области
    private boolean isLeaveToken;

    public MarchOrderPlayed() {
        destinationsOfMarch = new HashMap<>();
    }

    public MarchOrderPlayed(int from, HashMap<Integer, ArrayList<UnitType>> destinationsOfMarch, boolean isLeaveToken) {
        setAreaFrom(from);
        setDestinationsOfMarch(destinationsOfMarch);
        setLeaveToken(isLeaveToken);
    }

    public int getAreaFrom() {
        return areaFrom;
    }

    public void setAreaFrom(int areaFrom) {
        this.areaFrom = areaFrom;
    }

    public HashMap<Integer, ArrayList<UnitType>> getDestinationsOfMarch() {
        return destinationsOfMarch;
    }

    public void deleteDestinations() {
        destinationsOfMarch.clear();
    }

    public void addDestination(int area, ArrayList<UnitType> units) {
        destinationsOfMarch.put(area, units);
    }

    public void setDestinationsOfMarch(HashMap<Integer, ArrayList<UnitType>> destinationsOfMarch) {
        this.destinationsOfMarch = destinationsOfMarch;
    }

    public boolean getIsLeaveToken() {
        return isLeaveToken;
    }

    public void setLeaveToken(boolean leaveToken) {
        isLeaveToken = leaveToken;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        GameOfThronesMap map = Game.getInstance().getMap();
        sb.append("Поход: (").append(map.getAreaNm(areaFrom));
        for (Map.Entry<Integer, ArrayList<UnitType>> entry: destinationsOfMarch.entrySet()) {
            sb.append(", ").append(entry.getValue()).append(" -> ").append(map.getAreaNm(entry.getKey()));
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MarchOrderPlayed)) {
            return false;
        }
        MarchOrderPlayed otherMarch = (MarchOrderPlayed) obj;
        if (areaFrom != otherMarch.getAreaFrom() || isLeaveToken != otherMarch.getIsLeaveToken()) {
            return false;
        }
        HashMap<Integer, ArrayList<UnitType>> otherDestinations = otherMarch.getDestinationsOfMarch();
        for (Map.Entry<Integer, ArrayList<UnitType>> entry: destinationsOfMarch.entrySet()) {
            if (!otherDestinations.containsKey(entry.getKey())) {
                return false;
            }
            int[] typeDifference = new int[NUM_UNIT_TYPES];
            for (UnitType type: entry.getValue()) {
                typeDifference[type.getCode()]++;
            }
            for (UnitType type: otherDestinations.get(entry.getKey())) {
                typeDifference[type.getCode()]--;
            }
            for (int i = 0; i < NUM_UNIT_TYPES; i++) {
                if (typeDifference[i] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode;
        int multiplier = NUM_AREA + 1;
        // Для морских походов хэш-код отрицательный, для сухопутных - положительный или нулевой
        if (Game.getInstance().getMap().getAreaType(areaFrom).isNaval()) {
            hashCode = -areaFrom;
            // Возможные юниты - только корабли, поэтому можно просто считать количество этих кораблей
            for (Map.Entry<Integer, ArrayList<UnitType>> entry : destinationsOfMarch.entrySet()) {
                hashCode -= entry.getKey() * entry.getValue().size() * multiplier;
            }
        } else {
            hashCode = areaFrom * 2 + (isLeaveToken ? 1 : 0);
            multiplier = NUM_AREA * 2 + 1;
            int numPawns = 0, numKnights = 0, numSiegeEngines = 0;
            for (Map.Entry<Integer, ArrayList<UnitType>> entry : destinationsOfMarch.entrySet()) {
                for (UnitType type : entry.getValue()) {
                    switch (type) {
                        case pawn:
                            numPawns++;
                            break;
                        case knight:
                            numKnights++;
                            break;
                        case siegeEngine:
                            numSiegeEngines++;
                            break;
                    }
                }
                int unitsHash = numPawns + TROOPS_HASH_MULTIPLYER * (numKnights + TROOPS_HASH_MULTIPLYER * numSiegeEngines);
                hashCode += unitsHash * entry.getKey() * multiplier;
            }
        }
        return hashCode;
    }
}
