package com.alexeus.logic.enums;

/**
 * Created by alexeus on 10.01.2017.
 * Перечисление участников боя
 */
public enum SideOfBattle {
    attacker,
    defender,
    neutral;

    /**
     * Метод возвращает код стороны
     * @return 0, если нападающий, 1, если защищающийся, и -1 в противном случае
     */
    public int getCode() {
        switch (this) {
            case attacker:
                return 0;
            case defender:
                return 1;
        }
        return -1;
    }
}
