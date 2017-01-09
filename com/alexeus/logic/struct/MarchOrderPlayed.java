package com.alexeus.logic.struct;

import java.util.ArrayList;
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
    private HashMap<Integer, ArrayList<Unit>> destinationsOfMarch;

    public MarchOrderPlayed() {
        destinationsOfMarch = new HashMap<>();
    }

    public MarchOrderPlayed(int from, HashMap<Integer, ArrayList<Unit>> destinationsOfMarch) {
        setAreaFrom(from);
        setDestinationsOfMarch(destinationsOfMarch);
    }

    public int getAreaFrom() {
        return areaFrom;
    }

    public void setAreaFrom(int areaFrom) {
        this.areaFrom = areaFrom;
    }

    public HashMap<Integer, ArrayList<Unit>> getDestinationsOfMarch() {
        return destinationsOfMarch;
    }

    public void deleteDestinations() {
        destinationsOfMarch.clear();
    }

    public void addDestination(int area, ArrayList<Unit> units) {
        destinationsOfMarch.put(area, units);
    }

    public void setDestinationsOfMarch(HashMap<Integer, ArrayList<Unit>> destinationsOfMarch) {
        this.destinationsOfMarch = destinationsOfMarch;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Поход: (").append(areaFrom);
        for (Map.Entry<Integer, ArrayList<Unit>> entry: destinationsOfMarch.entrySet()) {
            sb.append(", ").append(entry.getValue()).append(" -> ").append(entry.getKey());
        }
        sb.append(")");
        return sb.toString();
    }
}
