package com.alexeus.logic.struct;

/**
 * Created by alexeus on 13.01.2017.
 * Интерфейс описывает то, что можно собрать при сборе войск
 */
public interface Musterable {

    /**
     * Метод отвечает на вопрос: сколько очков сбора нужно потратить, чтобы собрать это дерьмище?
     * @return количество очков сбора войск
     */
    int getNumMusterPoints();
}
