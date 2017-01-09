package com.alexeus.ai;

import com.alexeus.logic.enums.DisbandReason;
import com.alexeus.logic.enums.Order;
import com.alexeus.logic.struct.MarchOrderPlayed;
import com.alexeus.logic.struct.RaidOrderPlayed;
import com.alexeus.logic.enums.WildlingCard;

import java.util.HashMap;

/**
 * Created by alexeus on 03.01.2017.
 * Интерфейс игрока для общения с игрой
 */
public interface GotPlayerInterface {

    // Представиться
    public String nameYourself();

    /**
     * Расстановка приказов
     * @return Карта с парами область-приказ. Не обязательно указывать все приказы, можно даже вернуть пустую карту.
     */
    public HashMap<Integer, Order> giveOrders();

    /**
     * Как использовать посыльного ворона при вскрытии приказов
     * @return Строку с описанием решения.
     *         "w" означает просмотр карты одичалых;
     *         Пара значений int x, int y означает замену приказа в области x на приказ с кодом y. Примеры: "2, 0", "54, 7".
     *         Всё остальное, включая пустую строку, означает отказ от использования ворона.
     */
    public String useRaven();

    /**
     * Метод служит для оповещения игрока о верхней карте одичалых вследствие использования посыльного ворона
     * @param card Карта одичалых
     * @return true, если хотим оставить карту одичалых наверху, false - если хотим закопать
     */
    public boolean leaveWildlingCardOnTop(WildlingCard card);

    // Розыгрыш приказов
    /**
     * Разыграть один свой приказ набега
     * @return вариант розыгрыша набега
     */
    public RaidOrderPlayed playRaid();

    /*
     * Разыграть один свой приказ похода
     */
    public MarchOrderPlayed playMarch();

    /*
     * Разыграть свой приказ сбора власти со звездой
     */
    public String playConsolidatePower();

    /*
     * Разыграть один свой приказ сбора войск
     */
    public String muster();

    /*
     * Использовать ли валирийский меч в битве
     */
    public boolean useSword();

    /*
     * Какое событие выбрать (1, 2, 3).
     * Аргумент deckNumber принимает следующие значения:
     * 1 - Трон из клинков
     * 2 - Чёрные крылья, чёрные слова
     * 3 - Преданы мечу
     */
    public int eventToChoose(int deckNumber);

    // карты дома
    /*
     * Выбирает в бою нужную карту своего дома
     */
    public int playHouseCard();

    /*
     * Использовать ли свойство карты "Ренли Баратеон", если есть такая возможность
     */
    public boolean useRenly();

    /*
     * Выбор карты дома противника для сброса после боя, в котором был сыгран Пестряк
     */
    public int chooseCardPatchface();

    /*
     * Использовать ли свойство карты "Тирион Ланнистер" во время боя
     */
    public boolean useTyrion();

    /*
     * Выбрать область, из которой удалить приказ Серсеей после боя. Вернуть -1 для отмены
     */
    public int chooseAreaCercei();

    /*
     * По какому из треков скидывать оппонента Дораном. Возможные варианты:
     * 0 - по трону
     * 1 - по мечу
     * 2 - по ворону
     * -1 - не скидывать
     */
    public int chooseInfluenceTrackDoran();

    /*
     * Использовать ли свойство карты "Эйерон Грейджой" в бою
     */
    public boolean useAeron();

    /*
     * Выбрать область, из которой удалить приказ Королевой Шипов после боя. Вернуть -1 для отмены
     */
    public boolean chooseAreaQueenOfThorns();

    // ставки
    /*
     * Возвращает число жетонов власти, которое игрок ставит на торгах за треки влияния.
     * Допустимое значение - от 0 до текущего максимума числа жетонов.
     * Аргумент track имеет следующие значения:
     * 0 - трек трона
     * 1 - трек вотчин
     * 2 - трек королевского двора
     */
    public int bid(int track);

    /*
     * Возвращает число жетонов власти, которое игрок ставит на ночной дозор.
     * Допустимое значение - от 0 до текущего максимума числа жетонов.
     */
    public int wildlingBid();

    // Королевские выборы
    /*
     * Возвращает номер дома, который признаётся высший ставкой при нашествии одичалых, если мы - король.
     */
    public int kingChoiceTop(int pretenders);

    /*
     * Возвращает номер дома, который признаётся низшей ставкой при нашествии одичалых, если мы - король.
     */
    public int kingChoiceBottom(int pretenders);

    /*
     * Возвращает новую расстановку на треке влияния после того, как ставки были сделаны, если мы = король.
     * Аргумент track имеет следующие значения:
     * 0 - трек трона
     * 1 - трек вотчин
     * 2 - трек королевского двора
     */
    public String kingChoiceInfluenceTrack(int track, int[] bids);

    // Действия одичалых
    public String disbanding(DisbandReason reason);

    public String crowKillersLoseDecision();

    public String crowKillersTopDecision();

    public String massingOnTheMilkwaterTopDecision();

    public String massingOnTheMilkwaterLoseDecision();

    public String aKingBeyondTheWallTopDecision();

    public String aKingBeyondTheWallLoseDecision();

    public String preemptiveRaidBottomDecision();
}
