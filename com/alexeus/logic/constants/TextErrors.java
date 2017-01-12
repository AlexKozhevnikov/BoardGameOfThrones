package com.alexeus.logic.constants;

/**
 * Created by alexeus on 11.01.2017.
 * Текстовые константы, обозначающие ошибки, собраны здесь
 */
public class TextErrors {
    public static final String UNKNOWN_EVENT_ERROR = "Неизвестное событие";
    public static final String UNKNOWN_ORDER_ERROR = "Неизвестный приказ";
    public static final String UNKNOWN_HOUSE_CARD_ERROR = "Неизвестная карта Дома";
    public static final String UNKNOWN_UFO_ERROR = "Неизвестное нечто";
    public static final String ORDER_MISTAKES = "Ошибки приказов ";
    public static final String NO_CP_IN_SEA = " морская, там не может быть сбора власти";
    public static final String NO_DEFENCE_IN_PORT = " - порт, там не может быть обороны";
    public static final String PROHIBITED_ORDER = ": запрещённый приказ ";
    public static final String ORDERS = "Приказы ";
    public static final String OF_ORDERS = " приказов ";
    public static final String WHEN_MAX_OF_SUCH_ORDERS_IS = ", когда максимум таких приказов - ";
    public static final String STAR_NUMBER_VIOLENCE = "Нарушение числа доступных специальных приказов: звёзд - ";
    public static final String OF_STAR_ORDERS = ", специальных приказов - ";
    public static final String RAVEN_CHANGE_ORDER_FORMAT_ERROR = "Ошибка формата замены приказа с помощью посыльного ворона";
    public static final String RAVEN_CHANGE_ORDER_PARSE_ERROR = "Ошибка парсинга при замене приказа с помощью посыльного ворона";
    public static final String NO_RAID_ERROR = "В данной области нет вашего набега!";
    public static final String WRONG_AREAS_RAID_ERROR = "Неверные области для набега!";
    public static final String NO_ADJACENT_RAID_ERROR = "Области источника и назначения набега не соседствуют!";
    public static final String DONT_RAID_YOURSELF_ERROR = "Нельзя совершить набег на самого себя!";
    public static final String NO_ONE_TO_RAID_THERE_ERROR = "В целевой области набега нет приказов!";
    public static final String NO_RAID_FROM_LAND_TO_SEA_ERROR = "Нельзя совершить набег с суши на море!";
    public static final String CANT_RAID_THIS_ORDER_ERROR = "Этот приказ невозможно удалить данным набегом!";
    public static final String NO_MARCH_ERROR = "В данной области нет вашего похода!";
    public static final String WRONG_AREAS_MARCH_ERROR = "Неверные области для похода!";
    public static final String CANT_MARCH_FROM_SEA_TO_LAND_ERROR = "Нельзя перемещать корабли в сухопутные области!";
    public static final String CANT_MARCH_FROM_LAND_TO_SEA_ERROR = "Нельзя перемещать сухопутные юниты в морские области!";
    public static final String CANT_MARCH_IN_NOT_YOUR_PORT_ERROR = "Нельзя зайти в порт, который не принадлежит вам!";
    public static final String NO_WAY_MARCH_ERROR = "Из области источника похода нальзя попасть в область назначения!";
    public static final String CANT_MARCH_THERE_ERROR = "Поход в данную область невозможен!";
    public static final String EMPTY_ARMY_MARCH_ERROR = "Ошибка: попытка пойти пустой армией.";
    public static final String CANT_BEGIN_TWO_BATTLES_BY_ONE_MARCH_ERROR = "Нельзя начинать две битвы одним походом!";
    public static final String CANT_BEAT_NEUTRAL_GARRISON_ERROR_PLAYER_STRENGTH = "Невозможно пробить нейтральный гарнизон! Сила игрока: ";
    public static final String LACK_OF_UNITS_ERROR = "В области похода не хватает войск!";
    public static final String NO_POWER_TOKENS_TO_LEAVE_ERROR = "Нет жетонов власти, чтобы оставить один на покидаемой области!";
    public static final String CANT_LEAVE_POWER_TOKEN_IN_SEA_ERROR = "Нельзя оставить жетон власти в морской области!";
    public static final String TOO_BIG_ARMY_ERROR = "Ошибка, слишком большая армия!";
    public static final String DELETE_TROOP_ERROR = "Ошибка при удалении отряда, потому что его нету!!!";
    public static final String DELETE_ARMY_ERROR = "Ошибка при удалении армии из боя!";
    public static final String ALREADY_OCCUPIED_BY_OTHER_HOUSE_ERROR = "Ошибка добавления отряда: область занята другим игроком";
    public static final String TRYING_TO_ADD_NULL_ARMY_ERROR = "Ошибка! Попытка добавить пустую армию!";
    public static final String TRYING_TO_ADD_ARMY_OF_OTHER_PLAYER_ERROR = "Ошибка! Попытка добавить армию другого игрока!";
    public static final String NO_DEFENDER_ERROR = "Ошибка! Попытка начать бой в области, где нет защитника!";
    public static final String CANT_SUPPORT_AGAINST_YOURSELF_ERROR = "Ошибка! Нельзя оказывать поддержку против самого себя!";
    public static final String COUNЕ_UNITS_ON_NEUTRAL_SIDE_ERROR = "Ошибка! Попытка посчитать юниты на нейтральной стороне.";
    public static final String CANT_RETREAT_THERE_ERROR = "Нельзя отступить в эту область!";
    public static final String SAME_TYPES_ERROR = "Ошибка превращения одного юнита в другой: одинаковые типы!";
    public static final String INVALID_AREA_ERROR = "Ошибка: неподходящая область!";
    public static final String CARD_IS_NOT_ACTIVE_ERROR = "Ошибка: карта уже в сбросе!";
    public static final String CANT_CHANGE_UNIT_TYPE_ERROR = "Не удалось поменять тип юнита!";
}
