package com.alexeus.logic.enums;

/**
 * Created by alexeus on 03.01.2017.
 * Перечисление причин роспуска войск
 */
public enum DisbandReason {
    // Превышение лимита по снабжению
    supply,
    // Низшая ставка при "Нашествии орды" должна распустить два отряда в одном из своих замков и крепостей, если может
    hordeCastle,
    // Прочие ставки при "Нашествии орды" должны распустить один любой отряд
    hordeBite,
    // Низшая ставка при "Наездниках на мамонтах" должна распустить три любых отряда
    mammothTreadDown,
    // "Стандартный" роспуск войск: два любых отряда
    wildlingCommonDisband;

    public int getNumDisbands() {
        switch (this) {
            case mammothTreadDown:
                return 3;
            case hordeCastle:
            case wildlingCommonDisband:
                return 2;
            case hordeBite:
                return 1;
        }
        return Integer.MAX_VALUE;
    }
}
