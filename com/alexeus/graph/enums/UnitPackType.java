package com.alexeus.graph.enums;

/**
 * Created by alexeus on 15.01.2017.
 * Перечисление типов расположения юнитов в области при отрисовке на карте
 */
public enum UnitPackType {
    // Все юниты располагаются в линию
    line,
    // Корабли в порту
    port,
    // Три юнита располагаются в линию, а четыре - квадратом
    line3square4,
    // Юниты располагаются по вертикали
    vertical,
    // Юниты располагаются по горизонтали
    horizontal,
    // Максимально компактное расположение юнитов
    triangleSquare,
    // Расположение юнитов под углом 45 грудусов
    degree
}
