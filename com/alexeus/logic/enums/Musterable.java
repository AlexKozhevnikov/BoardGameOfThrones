package com.alexeus.logic.enums;

/**
 * Created by alexeus on 13.01.2017.
 * Интерфейс описывает некое дерьмище, которое можно создать при сборе войск
 */
public interface Musterable {

    /**
     * Метод отвечает на вопрос: сколько очков сбора нужно потратить, чтобы собрать это дерьмище?
     * @return количество очков сбора войск
     */
    int getNumMusterPoints();

    /**
     * Метод возвращает текст, связанный с постройкой этого дерьмища
     */
    String getActionString();

    /**
     * Метод возвращает код производимого дерьмища (нужно для работы методов equals и hashcode для MusterPlayed)
     * @return код дерьмища
     */
    int getCode();
}
