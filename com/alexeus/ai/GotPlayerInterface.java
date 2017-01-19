package com.alexeus.ai;

import com.alexeus.logic.enums.*;
import com.alexeus.logic.struct.*;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by alexeus on 03.01.2017.
 * Интерфейс игрока для общения с игрой
 */
public interface GotPlayerInterface {

    // Представиться
    void nameYourself();

    /**
     * Расстановка приказов
     * @return Карта с парами область-приказ. Не обязательно указывать все приказы, можно даже вернуть пустую карту.
     */
    HashMap<Integer, Order> giveOrders();

    /**
     * Как использовать посыльного ворона при вскрытии приказов
     * @return Строку с описанием решения.
     *         "w" означает просмотр карты одичалых;
     *         Пара значений int x, int y означает замену приказа в области x на приказ с кодом y. Примеры: "2, 0", "54, 7".
     *         Всё остальное, включая пустую строку, означает отказ от использования ворона.
     */
    String useRaven();

    /**
     * Метод служит для оповещения игрока о верхней карте одичалых вследствие использования посыльного ворона
     * @param card Карта одичалых
     * @return true, если хотим оставить карту одичалых наверху, false - если хотим закопать
     */
    boolean leaveWildlingCardOnTop(WildlingCard card);

    // Розыгрыш приказов
    /**
     * Разыграть один свой приказ набега
     * @return вариант розыгрыша набега
     */
    RaidOrderPlayed playRaid();

    /*
     * Разыграть один свой приказ похода
     */
    MarchOrderPlayed playMarch();

    /**
     * Поддержать одну из воюющих сторон
     * @param battleInfo информация о сражении
     * @return сторона, которую поддерживает игрок
     */
    SideOfBattle sideToSupport(final BattleInfo battleInfo);

    // *************************** КАРТЫ ДОМА ********************************
    /**
     * Выбрать в бою нужную карту своего дома
     * @param battleInfo информация о сражении
     * @return номер карты, которую выбрал игрок в своей колоде
     */
    int playHouseCard(final BattleInfo battleInfo);

    /**
     * Использовать ли свойство карты "Тирион Ланнистер" во время боя
     * @return true, если использовать
     */
    boolean useTyrion(final BattleInfo battleInfo, final HouseCard opponentCard);

    /**
     * По какому из треков скидывать оппонента Дораном.
     * @return трек, по которому Доран сбрасывает оппонента
     */
    TrackType chooseInfluenceTrackDoran(final BattleInfo battleInfo);

    /**
     * Использовать ли свойство карты "Эйерон Грейджой" в бою
     * @return true, если мы хотим заплатить 2 жетона и выбрать другую карту (оставив бомжа в сбросе)
     */
    boolean useAeron(final BattleInfo battleInfo);

    /**
     * Выбрать область, из которой удалить приказ Королевой Шипов после боя.
     * @param possibleVariants возможные варианты
     * @return номер области, из которой игрок хочет удалить приказ. Вернуть -1 для отмены
     */
    int chooseAreaQueenOfThorns(HashSet<Integer> possibleVariants);

    /**
     * Использовать ли валирийский меч в битве
     * @return true, если игрок хочет использовать валирийский меч в этой битве
     */
    boolean useSword(final BattleInfo battleInfo);

    /**
     * Выбор области для отступления
     * @param retreatingArmy отступающая армия
     * @param possibleAreas  множество из возможных вариантов
     * @return номер области для отступления
     */
    int chooseAreaToRetreat(final Army retreatingArmy, final HashSet<Integer> possibleAreas);

    /**
     * Выбор области, из которой удалить приказ Серсеей после боя
     * @return номер области, из которой игрок хочет удалить приказ. Вернуть -1 для отмены
     */
    int chooseAreaCerseiLannister(final HashSet<Integer> possibleAreas);

    /**
     * В какой области использовать свойство карты "Ренли Баратеон", если есть такая возможность
     * @param possibleAreas Возможные номера областей, в которых можно посвятить в рыцари
     * @return номер области карты, в которой посвятить пехотинца до рыцаря, или -1, если отказываемся
     */
    int areaToUseRenly(final HashSet<Integer> possibleAreas);

    /**
     * Выбор карты дома противника для сброса после боя, в котором был сыгран Пестряк
     * @param enemy номер дома, у которого можно сбросить карту
     * @return номер карты, которую выбрал игрок в колоде противника
     */
    int chooseCardPatchface(int enemy);

    /**
     * Разыграть свой приказ сбора власти со звездой
     * @param area номер области, где игрок поставил приказ сбора власти со звездой
     * @return Вариант сбора войск; если ни одного войска не собирается, то вместо этого собираются жетоны власти
     */
    MusterPlayed playConsolidatePowerS(int area);

    /**
     * Разыграть сбор войск в одном из своих замков
     * @param areas номера областей с замками, где игрок ещё не провёл сбор войск
     * @return Вариант сбора войск
     */
    MusterPlayed muster(HashSet<Integer> areas);

    /*
     * Какое событие выбрать (1, 2, 3).
     * Аргумент deckNumber принимает следующие значения:
     * 1 - Трон из клинков
     * 2 - Чёрные крылья, чёрные слова
     * 3 - Преданы мечу
     */
    int eventToChoose(int deckNumber);

    // ставки
    /*
     * Возвращает число жетонов власти, которое игрок ставит на торгах за треки влияния.
     * Допустимое значение - от 0 до текущего максимума числа жетонов.
     * Аргумент track имеет следующие значения:
     * 0 - трек трона
     * 1 - трек вотчин
     * 2 - трек королевского двора
     */
    int bid(int track);

    /*
     * Возвращает число жетонов власти, которое игрок ставит на ночной дозор.
     * Допустимое значение - от 0 до текущего максимума числа жетонов.
     */
    int wildlingBid();

    // Королевские выборы
    /*
     * Возвращает номер дома, который признаётся высший ставкой при нашествии одичалых, если мы - король.
     */
    int kingChoiceTop(int pretenders);

    /*
     * Возвращает номер дома, который признаётся низшей ставкой при нашествии одичалых, если мы - король.
     */
    int kingChoiceBottom(int pretenders);

    /*
     * Возвращает новую расстановку на треке влияния после того, как ставки были сделаны, если мы = король.
     * Аргумент track имеет следующие значения:
     * 0 - трек трона
     * 1 - трек вотчин
     * 2 - трек королевского двора
     */
    String kingChoiceInfluenceTrack(int track, int[] bids);

    // Действия одичалых
    String disbanding(DisbandReason reason);

    String crowKillersLoseDecision();

    String crowKillersTopDecision();

    String massingOnTheMilkwaterTopDecision();

    String massingOnTheMilkwaterLoseDecision();

    String aKingBeyondTheWallTopDecision();

    String aKingBeyondTheWallLoseDecision();

    String preemptiveRaidBottomDecision();
}
