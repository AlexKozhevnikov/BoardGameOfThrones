package com.alexeus.util;

import com.alexeus.logic.enums.UnitType;
import com.alexeus.logic.struct.Unit;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import static com.alexeus.logic.constants.MainConstants.HOUSE_GENITIVE;
import static com.alexeus.logic.constants.TextInfo.NOBODY;

/**
 * Created by alexeus on 12.01.2017.
 * Данный класс содержит всякие мелочи, которые бывают полезны в разных классах
 */
public class LittleThings {

    private static Random random = new Random();

    /**
     * Метод печатает разделитель, если флаг первого элемента опущен
     * @param firstFlag флаг первого элемента
     * @return опущенный флаг
     */
    public static boolean printDelimiter(boolean firstFlag) {
        if (!firstFlag) {
            System.out.print(", ");
        }
        return false;
    }


    /**
     * Метод возвращает случайный элемент множества
     * @param set множество
     * @return случайный элемент
     */
    public static <T> T getRandomElementOfSet(Set<T> set) throws NullPointerException {
        if (set.isEmpty()) {
            throw new NullPointerException();
        }
        T chosenElement = set.iterator().next();
        int size = set.size();
        int curIndex = 0;
        int needIndex = random.nextInt(size);
        for (T element: set) {
            if (curIndex == needIndex) {
                chosenElement = element;
                break;
            } else {
                curIndex++;
            }
        }
        return chosenElement;
    }

    public static String unitsToString(ArrayList<Unit> units) {
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
            sb.append(" ").append(HOUSE_GENITIVE[units.get(0).getHouse()]);
        } else {
            sb.append(NOBODY);
        }
        return sb.toString();
    }

    public static String unitTypesToString(ArrayList<UnitType> units) {
        StringBuilder sb = new StringBuilder();
        int n = units.size();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                if (i == n - 1 && i != 0) {
                    sb.append(" и ");
                } else if (i != 0) {
                    sb.append(", ");
                }
                sb.append(units.get(i));
            }
        } else {
            sb.append(NOBODY);
        }
        return sb.toString();
    }

    /**
     * Метод считает число комбинаций из n по k
     * @param k кэ
     * @param n нэ
     * @return число комбинаций
     */
    public static double numCombinations(int k, int n){
        return k == n ? 1 : 1. * n / (n - k) * numCombinations(k, n - 1);
    }
}
