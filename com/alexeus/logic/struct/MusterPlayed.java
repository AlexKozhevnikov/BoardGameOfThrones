package com.alexeus.logic.struct;

/**
 * Created by alexeus on 13.01.2017.
 * Класс представляет собой вариант сбора войск в одной области
 */
public class MusterPlayed {

    private int castleArea;

    private int[] area;

    private Musterable[] musterUnits;

    int numberMusterUnits;

    public MusterPlayed(int area) {
        castleArea = area;
        numberMusterUnits = 0;
        musterUnits = new Musterable[2];
        this.area = new int[2];
    }

    public void addNewMusterUnit(int area, Musterable musterUnit) {
        musterUnits[numberMusterUnits] = musterUnit;
        this.area[numberMusterUnits] = area;
        numberMusterUnits++;
    }

    public int getCastleArea() {
        return castleArea;
    }

    public int getArea(int index) {
        return area[index];
    }

    public Musterable getMusterUnit(int index) {
        return musterUnits[index];
    }

    public int getNumberMusterUnits() {
        return numberMusterUnits;
    }
}
