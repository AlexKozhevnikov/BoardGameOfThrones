package com.alexeus.logic.enums;

/**
 * Created by alexeus on 17.01.2017.
 * Интерфейс для колод событий
 */
public interface Happenable {
    String getName();
    boolean isWild();
    int getNumOfCards();
    int getDeckNumber();
}
