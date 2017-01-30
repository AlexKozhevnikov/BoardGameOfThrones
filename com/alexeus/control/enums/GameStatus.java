package com.alexeus.control.enums;

/**
 * Created by alexeus on 30.01.2017.
 * Статус текущей партии с точки зрения контроллера
 */
public enum GameStatus {
    // Игра не началась или уже закончилась
    none,
    // Игра идёт в данный момент
    running,
    // Игра прервана и не будет продолжена
    interrupted;
}
