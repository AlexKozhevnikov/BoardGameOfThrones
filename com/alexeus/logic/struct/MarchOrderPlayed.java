package com.alexeus.logic.struct;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexeus on 10.01.2017.
 * Класс описывает определённый вариант розыгрыша одного приказа похода
 */
public class MarchOrderPlayed {

    // Код области, поход в которой разыгрывается
    private int areaFrom;
    // Карта с областями, в которые направляются юниты, и юнитами, которые направляются в области. Может быть пустой.
    private HashMap<Integer, Army> destinationsOfMarch;
    // Оставляем ли жетон власти на покидаемой области
    private boolean isLeaveToken;

    public MarchOrderPlayed() {
        destinationsOfMarch = new HashMap<>();
    }

    public MarchOrderPlayed(int from, HashMap<Integer, Army> destinationsOfMarch, boolean isLeaveToken) {
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

    public HashMap<Integer, Army> getDestinationsOfMarch() {
        return destinationsOfMarch;
    }

    public void deleteDestinations() {
        destinationsOfMarch.clear();
    }

    public void addDestination(int area, Army units) {
        destinationsOfMarch.put(area, units);
    }

    public void setDestinationsOfMarch(HashMap<Integer, Army> destinationsOfMarch) {
        this.destinationsOfMarch = destinationsOfMarch;
    }

    public boolean isLeaveToken() {
        return isLeaveToken;
    }

    public void setLeaveToken(boolean leaveToken) {
        isLeaveToken = leaveToken;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Поход: (").append(areaFrom);
        for (Map.Entry<Integer, Army> entry: destinationsOfMarch.entrySet()) {
            sb.append(", ").append(entry.getValue()).append(" -> ").append(entry.getKey());
        }
        sb.append(")");
        return sb.toString();
    }
}
