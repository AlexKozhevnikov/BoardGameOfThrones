package com.alexeus.logic.enums;

/**
 * Created by alexeus on 10.01.2017.
 * Перечисление инициатив карт - стадий, на которых может разыгрываться карта
 */
public enum CardInitiative {
    cancel,
    immediately,
    bonus,
    passive,
    retreat,
    afterFight,
    // Пестряк разыгрывается после боя, но фактически для него нужна отдельная инициатива, потому что он играется после
    // всех других карт "после боя"
    patchface,
    none
}
