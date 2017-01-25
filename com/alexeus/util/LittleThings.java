package com.alexeus.util;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.Random;
import java.util.Set;

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
