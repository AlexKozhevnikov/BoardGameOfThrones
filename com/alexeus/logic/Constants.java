package com.alexeus.logic;

/**
 * Created by alexeus on 03.01.2017.
 * Все значимые текстовые, численные и массивные константы собраны здесь.
 */
public class Constants {

    // примитивные текстовые константы
    public static final String AND = " и ";
    public static final String COMMA = ", ";
    public static final String SPACER = "; ";
    public static final String SPACER2 = ", ";
    public static final String COLON = ": ";
    public static final String ONE = "1 ";
    public static final String ONE_M = "один";
    public static final String ONE_F = "одна";
    public static final String TWO_M = "два";
    public static final String TWO_F = "две";
    public static final String LINE_DELIMITER = "**************************************************";

    // информационные текстовые константы
    public static final String NEW_GAME_BEGINS = "Начинается новая партия в Игру престолов!";
    public static final String AREA_NUMBER = "Область №";
    public static final String IN_AREA_NUMBER = "В области №";
    public static final String GARRISON = "гарнизон";
    public static final String POWER_TOKEN = "жетон власти";
    public static final String CASTLE = "Замок";
    public static final String FORTRESS = "Крепость";
    public static final String BARREL = " бочка";
    public static final String BARRELS = " бочки";
    public static final String POWER_SIGN = " знак короны";
    public static final String POWER_SIGNS = " знака короны";
    public static final String BELONGS_TO = "Область принадлежит ";
    public static final String BELONGS_TO_NOBODY = "Область не принадлежит никому";
    public static final String PAWN = "пехотинец";
    public static final String PAWNS = "пехотинца";
    public static final String KNIGHT = "рыцарь";
    public static final String KNIGHTS = "рыцаря";
    public static final String TOWER = "осадная башня";
    public static final String TOWERS = "осадные башни";
    public static final String SHIP = "корабль";
    public static final String SHIPS = "корабля";
    public static final String NO_TROOPS = "нет отрядов";
    public static final String ADJACENT = "Граничит с областями: ";
    public static final String SUPPLY_OF = "Снабжение ";
    public static final String EQUALS = " равно ";
    public static final String OF_CASTLE = " замок";
    public static final String OF_CASTLA = " замка";
    public static final String OF_CASTLES = " замков";
    public static final String NO_TROOPS_OF = " нет войск ";
    public static final String NOBODY = "Никто.";
    public static final String CHANGES_ORDER = " меняет приказ";
    public static final String SEES_WILDLINGS_CARD = " смотрит верхнюю карту одичалых...";
    public static final String AND_LEAVES = "... и оставляет её наверху.";
    public static final String AND_BURIES = "... и закапывает её.";
    public static final String RAIDS_FROM = " совершает набег из ";
    public static final String RAIDS_TO = " на ";
    public static final String DELETES_RAID_FROM = " удаляет набег из ";
    public static final String FAILED_TO_PLAY_RAID = " не смог разыграть набег, поэтому все его набеги удаляются с карты.";
    public static final String FAILED_TO_PLAY_MARCH = " не смог разыграть поход, поэтому один его поход удаляется с карты.";
    public static final String DELETES_MARCH_FROM = " удаляет поход из ";
    public static final String PLAYS_MARCH_FROM = " совершает поход из ";
    public static final String MOVE_TO = " перемещаются в ";
    public static final String MOVES_TO = " перемещается в ";
    public static final String GARRISON_IS_DEFEATED = "Нейтральный лорд побеждён: сила войск ";
    public static final String GARRISON_STRENGTH_IS = ", сила нейтрального лорда - ";
    public static final String AND_FIGHTS = " и начинает бой";
    public static final String BATTLE_BEGINS_IN = "Начинается бой за ";
    public static final String BETWEEN = " между ";
    public static final String CAN_SUPPORT_SOMEBODY = "должны определиться, кому они оказывают подмогу.";
    public static final String SUPPORTS = " поддерживает ";
    public static final String SUPPORTS_NOBODY = " отказывается от поддержки.";
    public static final String HOUSES_CHOOSE_CARDS = "Воюющие дома выбирают карты.";
    public static final String RELATION_OF_FORCES_IS = "Соотношение сил: ";
    public static final String VERSUS = " vs ";
    public static final String PLAYS_HOUSE_CARD = " играет карту Дома ";
    public static final String CAN_USE_SPECIAL_PROPERTY_OF_CARD = " может использовать специально свойство карты ";
    public static final String NO_EFFECT = ": нет эффекта";
    public static final String TYRION_CANCELS = "Тирион Ланнистер блокирует карту противника, теперь тот должен выбрать другую карту.";
    public static final String DORAN_ABUSES = "Доран Мартелл опускает ";
    public static final String QUEEN_OF_THORNS_REMOVES_ORDER_FROM_AREA = "Королева шипов удаляет приказ из области ";
    public static final String MACE_EATS_MAN_IN = "Мейс Тирелл уничтожает пехотинца в области ";
    public static final String AERON_RUNS_AWAY = "Эйерон Мокровласый сбегает. ";
    public static final String MUST_CHOOSE_OTHER_CARD = " должен выбрать другую карту.";
    public static final String GETS_NEW_DECK = " обновляет колоду карт Дома.";

    public static final String WESTEROS_PHASE = "Фаза Вестероса";
    public static final String PLANNING_PHASE = "Фаза замыслов.";
    public static final String RAVEN_PHASE = "Фаза действий. Посыльный ворон.";
    public static final String RAID_PHASE = "Фаза действий. Набеги.";
    public static final String MARCH_PHASE = "Фаза действий. Походы.";
    public static final String CONSOLIDATE_POWER_PHASE = "Фаза действий. Сбор власти.";
    public static final String END_OF_GAME = "Конец игры";
    public static final String ROUND_NUMBER = "Раунд №";
    public static final String WILDLINGS_ATTACK = "Нашествие одичалых";
    public static final String WITH_STRENGTH = " с силой ";
    public static final String NIGHT_WATCH_VICTORY = "Победа ночного дозора!";
    public static final String NIGHT_WATCH_DEFEAT = "Победа одичалых!";

    // текстовые константы - коды
    public static final String RAVEN_SEES_WILDLINGS_CODE = "w";

    // текстовые константы - ошибки
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
    public static final String CANT_MARCH_FROM_SEA_TO_LAND_ERROR = "Нальзя перемещать корабли в сухопутные области!";
    public static final String CANT_MARCH_FROM_LAND_TO_SEA_ERROR = "Нальзя перемещать сухопутные юниты в морские области!";
    public static final String CANT_MARCH_IN_NOT_YOUR_PORT_ERROR = "Нельзя зайти в порт, который не принадлежит вам!";
    public static final String NO_WAY_MARCH_ERROR = "Из области источника похода нальзя попасть в область назначения!";
    public static final String CANT_MARCH_THERE_ERROR = "Поход в данную область невозможен!";
    public static final String EMPTY_ARMY_MARCH_ERROR = "Ошибка: попытка пойти пустой армией.";
    public static final String CANT_BEGIN_TWO_BATTLES_BY_ONE_MARCH_ERROR = "Нельзя начинать две битвы одним походом!";
    public static final String CANT_BEAT_NEUTRAL_GARRISON_ERROR = "Невозможно пробить нейтральный гарнизон!";
    public static final String LACK_OF_UNITS_ERROR = "В области похода не хватает войск!";
    public static final String NO_POWER_TOKENS_TO_LEAVE_ERROR = "Нет жетонов власти, чтобы оставить один на покидаемой области!";
    public static final String TOO_BIG_ARMY_ERROR = "Ошибка, слишком большая армия!";
    public static final String DELETE_TROOP_ERROR = "Ошибка при удалении отряда, потому что его нету!!!";
    public static final String DELETE_ARMY_ERROR = "Ошибка при удалении армии из боя!";
    public static final String ALREADY_OCCUPIED_BY_OTHER_HOUSE_ERROR = "Ошибка добавления отряда: область занята другим игроком";
    public static final String TRYING_TO_ADD_NULL_ARMY_ERROR = "Ошибка! Попытка добавить пустую армию!";
    public static final String TRYING_TO_ADD_ARMY_OF_OTHER_PLAYER_ERROR = "Ошибка! Попытка добавить армию другого игрока!";
    public static final String NO_DEFENDER_ERROR = "Ошибка! Попытка начать бой в области, где нет защитника!";
    public static final String CANT_SUPPORT_AGAINST_YOURSELF_ERROR = "Ошибка! Нельзя оказывать поддержку против самого себя!";

    // текстовые константы - игроки
    public static final String HOUSE[] = {"Баратеон", "Ланнистер", "Старк", "Мартелл", "Грейджой", "Тирелл"};
    public static final String HOUSE_GENITIVE[] = {"Баратеона", "Ланнистера", "Старка", "Мартелла", "Грейджоя", "Тирелла"};
    public static final String HOUSE_DATIVE[] = {"Баратеону", "Ланнистеру", "Старку", "Мартеллу", "Грейджою", "Тиреллу"};
    public static final String HOUSE_ABLATIVE[] = {"Баратеоном", "Ланнистером", "Старком", "Мартеллом", "Грейджоем", "Тиреллом"};
    public static final String NOBODY_GENITIVE = "никого";
    public static final String PLAYERS = "Участники партии:";

    // числовые константы
    public static final int NUM_PLAYER = 6;
    public static final int LAST_TURN = 10;
    public static final int MAX_SUPPLY = 6;
    public static final int NUM_CASTLES_TO_WIN = 7;
    public static final int NUM_HOUSE_CARDS = 7;
    public static final int WILDLING_STRENGTH_INCREMENT = 2;
    public static final int MAX_WILDLING_STRENGTH = 12;
    public static final int MAX_TRIES_TO_GO = 3;
    public static final int INITIAL_TOKENS = 5;
    public static final int MAX_TOKENS = 20;
    public static final int NUM_DIFFERENT_ORDERS = 11;
    public static final int MAX_TROOPS_IN_AREA = 4;

    public static final int[][] INITIAL_INFLUENCE_TRACKS_PLAYER_ON_PLACE = {{0, 1, 2, 3, 4, 5},
                                                                            {4, 5, 3, 2, 0, 1},
                                                                            {1, 2, 3, 0, 5, 4}};
    public static final int[] NUM_OF_STARS_ON_PLACE = {3, 3, 2, 1, 0, 0};
}
