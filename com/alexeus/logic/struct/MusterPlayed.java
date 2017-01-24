package com.alexeus.logic.struct;

import com.alexeus.logic.enums.Musterable;
import com.alexeus.logic.enums.PawnPromotion;
import com.alexeus.logic.enums.UnitType;

import static com.alexeus.logic.constants.TextErrors.TOO_MANY_TROOPS_TO_MUSTER_ERROR;
import static com.alexeus.map.GameOfThronesMap.NUM_AREA;

/**
 * Created by alexeus on 13.01.2017.
 * Класс представляет собой вариант сбора войск в одной области.
 * Для простоты кода рассчитан на сбор не более 2 юнитов в одном замке, как в стандартной ИП.
 * Для расширения стандартных правил придётся рефакторить!
 */
public class MusterPlayed {

    private int castleArea;

    private int[] area;

    private Musterable[] musterUnits;

    private int numberMusterUnits;

    // Основной конструктор
    public MusterPlayed(int area) {
        castleArea = area;
        numberMusterUnits = 0;
        this.area = new int[2];
        musterUnits = new Musterable[2];
    }

    // Конструктор копирования
    public MusterPlayed(MusterPlayed muster) {
        castleArea = muster.getCastleArea();
        numberMusterUnits = muster.getNumberMusterUnits();
        this.area = new int[2];
        musterUnits = new Musterable[2];
        for (int i = 0; i < numberMusterUnits; i++) {
            area[i] = muster.getArea(i);
            // Мы можем присваивать ссылку и не создавать новый юнит, потому что из всех вариантов сбора войск
            // настоящим окажется только один, и выхода на поле дважды одного юнита под разными ссылками не произойдёт
            musterUnits[i] = muster.getMusterUnit(i);
        }
    }

    /**
     * Добавляет юнит или улучшение юнита в данный вариант сбора войск
     * @param area          область, где производится юнит или улучшение юнита
     * @param newMusterable юнит или улучшение юнита
     */
    public void addNewMusterable(int area, Musterable newMusterable) {
        // повышение пехотинца + новый пехотинец = сразу повышенный юнит
        if (numberMusterUnits == 1 && musterUnits[0] instanceof PawnPromotion && newMusterable instanceof UnitType &&
                newMusterable == UnitType.pawn) {
            musterUnits[0] = ((PawnPromotion) musterUnits[0]).getTargetType();
        } else if (numberMusterUnits == 1 && newMusterable instanceof PawnPromotion &&
                musterUnits[0] instanceof UnitType && musterUnits[0] == UnitType.pawn) {
            musterUnits[0] = ((PawnPromotion) newMusterable).getTargetType();
        } else {
            musterUnits[numberMusterUnits] = newMusterable;
            this.area[numberMusterUnits] = area;
            numberMusterUnits++;
        }
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

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MusterPlayed)) {
            return false;
        }
        MusterPlayed m1 = (MusterPlayed) object;
        if (numberMusterUnits != m1.numberMusterUnits) {
            return false;
        }
        switch (numberMusterUnits) {
            case 0:
                return true;
            case 1:
                return musterUnits[0].equals(m1.getMusterUnit(0)) && area[0] == m1.getArea(0);
            case 2:
                return musterUnits[0].equals(m1.getMusterUnit(0)) && area[0] == m1.getArea(0) &&
                        musterUnits[1].equals(m1.getMusterUnit(1)) && area[1] == m1.getArea(1) ||
                        musterUnits[0].equals(m1.getMusterUnit(1)) && area[0] == m1.getArea(1) &&
                        musterUnits[1].equals(m1.getMusterUnit(0)) && area[1] == m1.getArea(0);
            default:
                System.err.println(TOO_MANY_TROOPS_TO_MUSTER_ERROR);
                return false;
        }
    }

    @Override
    public int hashCode() {
        int x = castleArea;
        for (int i = 0; i < numberMusterUnits; i++) {
            x += NUM_AREA * (area[i] + NUM_AREA * musterUnits[i].getCode());
        }
        return x;
    }
}
