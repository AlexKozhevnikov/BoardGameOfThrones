package com.alexeus.logic.struct;

/**
 * Created by alexeus on 08.01.2017.
 * Класс представляет собой вариант розыгрыша одного приказа набега.
 */
public class RaidOrderPlayed {

    // Код области, набег в которой разыгрывается
    private int areaFrom;
    // Код области, на которую совершается набег. Если она меньше нуля, или равна areaFrom, то набег просто удаляется
    private int areaTo;

    public RaidOrderPlayed() {
    }

    public RaidOrderPlayed(int from, int to) {
        setAreaFrom(from);
        setAreaTo(to);
    }

    public int getAreaFrom() {
        return areaFrom;
    }

    public void setAreaFrom(int areaFrom) {
        this.areaFrom = areaFrom;
    }

    public int getAreaTo() {
        return areaTo;
    }

    public void setAreaTo(int areaTo) {
        this.areaTo = areaTo;
    }

    public String toString() {
        return "(" + areaFrom + ", " + areaTo + ")";
    }
}
