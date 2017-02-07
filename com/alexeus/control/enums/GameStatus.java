package com.alexeus.control.enums;

/**
 * Created by alexeus on 30.01.2017.
 * Статус текущей партии с точки зрения контроллера
 */
public enum GameStatus {
    // Игра должна скоро начаться
    start,
    // Игра идёт в данный момент
    running,
    // Игра прервана и должна завершиться; новая игра будет начата
    interrupted,
    // Игра закончена, новой игры не ожидается
    end
}
