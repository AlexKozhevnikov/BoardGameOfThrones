package com.alexeus.util;

/**
 * Created by alexeus on 12.01.2017.
 * Данный класс содержит всякие мелочи, которые бывают полезны в разных классах
 */
public class LittleThings {

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
}
