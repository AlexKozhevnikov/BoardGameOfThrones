package com.alexeus.logic.constants;

import com.alexeus.logic.enums.TrackType;

/**
 * Created by alexeus on 11.01.2017.
 * Текстовые константы, использующиеся в информационных сообщениях
 */
public class TextInfo {

    /************** Бессмысленная болтовня ******************/
    public static final String NEW_GAME_BEGINS = "Начинается новая партия в Игру престолов!";
    public static final String AREA_NUMBER = "Область №";
    public static final String IN_AREA_NUMBER = "В области №";
    public static final String CASTLE = "Замок";
    public static final String FORTRESS = "Крепость";
    public static final String BARREL = " бочка";
    public static final String BARRELS = " бочки";
    public static final String POWER_SIGN = " знак короны";
    public static final String POWER_SIGNS = " знака короны";
    public static final String ADJACENT = "Граничит с областями: ";
    public static final String SUPPLY_OF = "Снабжение ";
    public static final String EQUALS = " равно ";
    public static final String NO_TROOPS_OF = " нет войск ";
    public static final String AREAS_WITH_TROOPS_OF = "    Области с войсками ";
    public static final String ARMIES = "Армии ";
    public static final String NOBODY = "Никого";

    /************** Ворононосец принимает решение ******************/
    public static final String SEES_WILDLINGS_CARD = " смотрит верхнюю карту одичалых...";
    public static final String AND_LEAVES = "... и оставляет её наверху.";
    public static final String AND_BURIES = "... и закапывает её.";

    /************** Набеги ******************/
    public static final String RAIDS = " совершает набег ";
    public static final String TO = " на ";
    public static final String GAINS_POWER = " получает жетон власти.";
    public static final String LOSES_POWER = " теряет жетон власти.";
    public static final String DELETES_RAID = " удаляет приказ набега ";
    public static final String FAILED_TO_PLAY_RAID = " не смог разыграть набег, поэтому все его набеги удаляются с карты.";

    /************** Походы ******************/
    public static final String MARCH_VALIDATION = "Проверка корректности следующего похода: ";
    public static final String FAILED_TO_PLAY_MARCH = " не смог разыграть поход, поэтому один его поход удаляется с карты.";
    public static final String DELETES_MARCH = " удаляет приказ похода ";
    public static final String MOVE_TO = " перемещаются в ";
    public static final String MOVES_TO = " перемещается в ";
    public static final String LEAVES_POWER_TOKEN = " оставляет жетон власти ";
    public static final String GARRISON_IS_DEFEATED = "Нейтральный лорд побеждён: сила войск ";
    public static final String GARRISON_STRENGTH_IS = ", сила нейтрального лорда - ";
    public static final String CAN_CAPTURE_OR_DESTROY_SHIPS = " может захватить или уничтожить корабли в порту.";
    public static final String DESTROYS_ALL_SHIPS = " уничтожает все корабли в порту.";
    public static final String CAPTURES = " захватывает ";
    public static final String OF_SHIP = " корабль.";
    public static final String OF_SHIPA = " корабля.";
    public static final String AND_FIGHTS = " и начинает бой.";
    public static final String AND_FIGHT = " и начинают бой.";
    public static final String BATTLE_BEGINS_FOR = "Начинается бой за ";
    public static final String BETWEEN = " между ";
    public static final String CAN_SUPPORT_SOMEBODY = " должны определиться, кому они оказывают подмогу.";
    public static final String SUPPORTS = " поддерживает ";
    public static final String SUPPORTS_NOBODY = " отказывается от поддержки.";
    public static final String HOUSES_CHOOSE_CARDS = "Воюющие дома выбирают карты.";
    public static final String VERSUS = " vs ";
    public static final String PLAYS_HOUSE_CARD = " играет карту Дома \"";
    public static final String CAN_USE_SPECIAL_PROPERTY_OF_CARD = " может использовать специальное свойство карты ";
    public static final String NO_EFFECT = ": нет эффекта";
    public static final String TYRION_CANCELS = "Тирион Ланнистер блокирует карту противника, теперь тот должен выбрать другую карту.";
    public static final String DORAN_ABUSES = "Доран Мартелл опускает ";
    public static final String QUEEN_OF_THORNS_REMOVES_ORDER = "Королева шипов удаляет приказ ";
    public static final String MACE_EATS_MAN = "Мейс Тирелл уничтожает пехотинца ";
    public static final String AERON_RUNS_AWAY = "Эйерон Мокровласый сбегает. ";
    public static final String MUST_CHOOSE_OTHER_CARD = " должен выбрать другую карту.";
    public static final String USES_SWORD = " использует валирийский меч, получая +1 к битве!";
    public static final String WINS_THE_BATTLE = " выигрывает битву!";
    public static final String SWORDS_U = " мечей, у ";
    public static final String OF_TOWERS = " башен";
    public static final String LOSES = " теряет ";
    public static final String TROOP = " отряд.";
    public static final String TROOPS = " отряда.";
    public static final String HAS_NO_LOSSES = " не несёт потерь.";
    public static final String BLACKFISH_SAVES = "Чёрная Рыба спасает ";
    public static final String FROM_LOSSES = " от потерь.";
    public static final String GARRISON = "Гарнизон ";
    public static final String IS_DEFEATED_M = " уничтожен!";
    public static final String IS_DEFEATED_F = " уничтожена!";
    public static final String IS_FINISHED = " добит!";
    public static final String IS_WOUNDED = " ранен!";
    public static final String LORAS_RULES = "Лорас Тирелл перегруппировывает войска.";
    public static final String ARIANNA_RULES_AND_PUTS_BACK = "Арианна Мартелл отбрасывает атакующие войска назад ";
    public static final String MINIMAL_LOSSES = "Минимальные потери при отступлении: ";
    public static final String MUST_RETREAT = " должен отсупить побеждёнными войсками.";
    public static final String RETREATS_IN = " отступает в ";
    public static final String GETS_NEW_DECK = " обновляет колоду карт Дома.";
    public static final String RENLY_CAN_MAKE_KNIGHT = "Ренли Баратеон может посвятить пехотинца в рыцари.";
    public static final String RENLY_MAKES_KNIGHT = "Ренли Баратеон посвящает пехотинца в рыцари ";
    public static final String CERSEI_CAN_REMOVE_ANY_ORDER = "Серсея Ланнистер может удалить любой приказ противника.";
    public static final String CERSEI_REMOVES_ORDER = "Серсея Ланнистер снимает приказ ";
    public static final String PATCHPACE_CAN_DELETE_ANY_CARD = "Пестряк может сбросить карту противника.";
    public static final String PATCHPACE_DELETES_CARD = "Пестряк сбрасывает карту ";
    public static final String RETURNS_ALL_CARDS = " возвращает весь сброс карт Дома себе на руку.";
    public static final String RETURNS_CARD = " возвращает карту Дома ";

    /************** Сбор власти ******************/
    public static final String EARNS = " получает ";
    public static final String POWER_TOKEN = " жетон власти.";
    public static final String POWER_TOKENA = " жетона власти.";
    public static final String POWER_TOKENS = " жетонов власти.";
    public static final String NOW_HE_HAS = " Теперь у него ";
    public static final String CAN_MUSTER = " может собрать войска ";
    public static final String CAN_MUSTER_TROOPS = " может собрать войска.";
    public static final String FAILED_TO_PLAY_MUSTER = " не смог собрать войска, и его сбор войск завершается.";

    /************** События ******************/
    public static final String NEW_EVENTS = "Новые события: ";
    public static final String MUST_CHOOSE_EVENT = " должен выбрать событие!";
    public static final String MUSTER_HAPPENS = "Все дома собирают новые войска в подвластных замках.";
    public static final String SUPPLY_HAPPENS = "Все дома изменяют снабжение и состав войск.";
    public static final String CLASH_HAPPENS = "Все дома вступают в борьбу за влияние.";
    public static final String CLASH_FOR = "Битва за ";
    public static final String PLAYERS_MAKE_BIDS = ". Игроки делают свои ставки.";
    public static final String NEW_ORDER_ON_TRACK = "Новый порядок игроков на треке влияния ";
    public static final String FAILED_TO_BID = " не смог сделать ставку, поэтому она приравнивается к нулю.";
    public static final String FAILED_TO_KING = " не смог принять королевское решение.";
    public static final String GAME_OF_THRONES_HAPPENS = "Все дома собирают жетоны власти с подвластных земель.";
    public static final String RELAX_NOTHING_HAPPENS = "Расслабляемся, ничего не происходит.";
    public static final String SEA_OF_STORMS = "Запрещены приказы набегов.";
    public static final String RAIN_OF_AUTUMN = "Запрещены приказы похода +1.";
    public static final String FEAST_FOR_CROWS = "Запрещены приказы сбора власти.";
    public static final String WEB_OF_LIES = "Запрещены приказы подмоги.";
    public static final String STORM_OF_SWORDS = "Запрещены приказы обороны.";

    /************** Одичалые и роспуск войск ******************/
    public static final String WILDLINGS_ATTACK_WITH_STRENGTH = "Нашествие одичалых с силой ";
    public static final String BIDS_ARE = "Ставки игроков: ";
    public static final String NIGHT_WATCH_VICTORY = "Победа ночного дозора!";
    public static final String NIGHT_WATCH_DEFEAT = "Победа одичалых!";
    public static final String CHOOSES = " выбирает";
    public static final String TOP_BID_IS = "Высшая ставка - ";
    public static final String BOTTOM_BID_IS = "Низшая ставка - ";
    public static final String WILDLINGS_ARE = "Карта одичалых: ";
    public static final String SILENCE_AT_THE_WALL = "Ничего не происходит.";
    public static final String SKINCHANGER_SCOUT_WIN = " возвращает ставку, потраченную на отражение атаки одичалых.";
    public static final String LOSES_ALL_MONEY = " теряет все доступные жетоны власти.";
    public static final String LOSES_MONEY = " теряет ";
    public static final String LOSES_CARD = " теряет карту Дома ";
    public static final String CAN_RETURN_CARD = " может вернуть на руку одну карту Дома.";
    public static final String CAN_ENLIGHTEN_ON_TRACK = " может подняться на верхнюю позицию любого трека влияния!";
    public static final String ENLIGHTENS_ON_TRACK = " поднимается ";
    public static final String DESCENDS_ON_ALL_TRACKS = " опускается на нижние позиции по всем трекам влияния!";
    public static final String MUST_DESCEND_ON_TRACK = " должен опуститься на нижнюю позицию трека меча или ворона.";
    public static final String DESCENDS_ON_TRACK = " опускается ";
    public static final String CAN_MUSTER_IN_ONE_CASTLE = " может собрать войска в одном из своих замков/крепостей!";
    public static final String MUST_DISBAND_TROOPS = " должен распустить войска!";
    public static final String FAILED_TO_DISBAND = " не смог распустить войска, поэтому его войска будут распущены случайным образом.";
    public static final String MUST_DISBAND_SOME_KNIGHTS = " должен распустить часть своих рыцарей!";
    public static final String MUST_DOWNGRADE_SOME_KNIGHTS = " должен превратить часть своих рыцарей в пехотинцев!";
    public static final String DOESNT_TAKE_PART = " не участвует в отражении новой атаки одичалых!";
    public static final String PREEMPTIVE_RAID_TRACK = " решил опуститься на два деления ";
    public static final String PREEMPTIVE_RAID_DISBAND = " решил распустить войска.";
    public static final String FAILED_TO_WILD = " не смог принять одичальническое решение!";

    /*************** Фазы игры ****************/
    public static final String LINE_DELIMITER = "**************************************************";
    public static final String WESTEROS_PHASE = "Фаза Вестероса";
    public static final String PLANNING_PHASE = "Фаза замыслов.";
    public static final String RAVEN_PHASE = "Фаза действий. Посыльный ворон.";
    public static final String RAID_PHASE = "Фаза действий. Набеги.";
    public static final String MARCH_PHASE = "Фаза действий. Походы.";
    public static final String CONSOLIDATE_POWER_PHASE = "Фаза действий. Сбор власти.";
    public static final String END_OF_GAME = "Конец игры";
    public static final String ROUND_NUMBER = "Раунд №";
}
