package com.alexeus.logic.enums;

/**
 * Created by Пользователь on 08.01.2017.
 * Перечисление типов соседства областей
 */
public enum AdjacencyType {
    noAdjacency,
    landToLand,
    seaToSea,
    seaToLand,
    landToSea,
    portToSea,
    seaToPort,
    portOfCastle,
    castleWithPort
}
