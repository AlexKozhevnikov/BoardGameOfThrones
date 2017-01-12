package com.alexeus.logic.enums;

import com.alexeus.logic.constants.TextErrors;

/**
 * Created by alexeus on 10.01.2017.
 * Перечиление карт домов
 */
public enum HouseCard {
    // Баратеоновые
    stannisBaratheon,
    renlyBaratheon,
    serDevosSeaworth,
    brienneOfTarth,
    salladhorSaan,
    melisandre,
    patchface,
    // Ланнистерские
    tywinLannister,
    serGregorClegane,
    serJaimeLannister,
    hound,
    serKevanLannister,
    tyrionLannister,
    cerseiLannister,
    // Старковские
    eddardStark,
    robbStark,
    greationUmber,
    rooseBolton,
    theBlackfish,
    serRoderickCassel,
    catylynStark,
    // Мартеллячьи
    theRedViper,
    areoHotah,
    darkstar,
    obaraSand,
    arianneMartell,
    nymeriaSand,
    doranMartell,
    // Серые радостные
    euronCrowsEye,
    victarionGreyjoy,
    balonGreyjoy,
    theonGreyjoy,
    dagmarCleftjaw,
    ashaGreyjoy,
    aeronDamphair,
    // Тиреллячьи
    maceTyrell,
    serLorasTyrell,
    serGarlanTyrell,
    randyllTarly,
    alesterFlorent,
    margaeryTyrell,
    queenOfThorns,
    // "пустая карта", нужная в том случае, когда Тирион отменил последнюю активную карту игрока
    none;

    private boolean isActive = true;

    @Override
    public String toString() {
        switch (this) {
            case aeronDamphair:
                return "Эйерон Мокровласый";
            case alesterFlorent:
                return "Алистер Флорент";
            case areoHotah:
                return "Арео Хота";
            case arianneMartell:
                return "Арианна Мартелл";
            case ashaGreyjoy:
                return "Аша Грейджой";
            case balonGreyjoy:
                return "Бейлон Грейджой";
            case brienneOfTarth:
                return "Бриенна Тартская";
            case catylynStark:
                return "Кейтилин Старк";
            case cerseiLannister:
                return "Серсея Ланнистер";
            case dagmarCleftjaw:
                return "Дагмер Битый Рот";
            case darkstar:
                return "Герольд Тёмная Звезда";
            case doranMartell:
                return "Доран Мартелл";
            case eddardStark:
                return "Эддард Старк";
            case euronCrowsEye:
                return "Эурон Вороний Глаз";
            case greationUmber:
                return "Большой Джон Амбер";
            case hound:
                return "Пёс";
            case maceTyrell:
                return "Мейс Тирелл";
            case margaeryTyrell:
                return "Маргери Тирелл";
            case melisandre:
                return "Мелисандра";
            case none:
                return "Никто";
            case nymeriaSand:
                return "Нимерия Сэнд";
            case obaraSand:
                return "Обара Сэнд";
            case patchface:
                return "Пестряк";
            case queenOfThorns:
                return "Королева Шипов";
            case randyllTarly:
                return "Рэндилл Тарли";
            case renlyBaratheon:
                return "Рэнли Баратеон";
            case robbStark:
                return "Робб Старк";
            case rooseBolton:
                return "Русе Болтон";
            case salladhorSaan:
                return "Салладор Саан";
            case serDevosSeaworth:
                return "Сер Давос";
            case serGarlanTyrell:
                return "Сер Гарлан Тирелл";
            case serGregorClegane:
                return "Сер Грегор Клиган";
            case serJaimeLannister:
                return "Сер Джейме Ланнистер";
            case serKevanLannister:
                return "Сер Киван Ланнистер";
            case serLorasTyrell:
                return "Сер Лорас Тирелл";
            case serRoderickCassel:
                return "Родерик Кассель";
            case stannisBaratheon:
                return "Станнис Баратеон";
            case theBlackfish:
                return "Чёрная Рыба";
            case theonGreyjoy:
                return "Теон Грейджой";
            case theRedViper:
                return "Оберин Красная Гадюка";
            case tyrionLannister:
                return "Тирион Ланнистер";
            case tywinLannister:
                return "Тайвин Ланнистер";
            case victarionGreyjoy:
                return "Виктарион Грейджой";
        }
        return TextErrors.UNKNOWN_HOUSE_CARD_ERROR;
    }

    /**
     * Метод возвращает дом, за который сражается данный полководец
     * @return номер дома
     */
    public int house() {
        switch (this) {
            case stannisBaratheon:
            case renlyBaratheon:
            case serDevosSeaworth:
            case brienneOfTarth:
            case salladhorSaan:
            case melisandre:
            case patchface:
                return 0;
            case tywinLannister:
            case serGregorClegane:
            case serJaimeLannister:
            case hound:
            case serKevanLannister:
            case tyrionLannister:
            case cerseiLannister:
                return 1;
            case eddardStark:
            case robbStark:
            case greationUmber:
            case rooseBolton:
            case theBlackfish:
            case serRoderickCassel:
            case catylynStark:
                return 2;
            case theRedViper:
            case areoHotah:
            case darkstar:
            case obaraSand:
            case arianneMartell:
            case nymeriaSand:
            case doranMartell:
                return 3;
            case euronCrowsEye:
            case victarionGreyjoy:
            case balonGreyjoy:
            case theonGreyjoy:
            case dagmarCleftjaw:
            case ashaGreyjoy:
            case aeronDamphair:
                return 4;
            case maceTyrell:
            case serLorasTyrell:
            case serGarlanTyrell:
            case randyllTarly:
            case alesterFlorent:
            case margaeryTyrell:
            case queenOfThorns:
                return 5;
            case none:
                return -1;
        }
        System.out.println(TextErrors.UNKNOWN_HOUSE_CARD_ERROR);
        return -1;
    }

    /**
     * Метод возвращает боевую силу, напечатанную на карте
     * @return номинальная боевая сила карты
     */
    public int getStrength() {
        switch (this) {
            case stannisBaratheon:
            case tywinLannister:
            case eddardStark:
            case theRedViper:
            case euronCrowsEye:
            case maceTyrell:
                return 4;
            case renlyBaratheon:
            case serGregorClegane:
            case robbStark:
            case areoHotah:
            case victarionGreyjoy:
            case serLorasTyrell:
                return 3;
            case serDevosSeaworth:
            case brienneOfTarth:
            case serJaimeLannister:
            case hound:
            case greationUmber:
            case rooseBolton:
            case darkstar:
            case obaraSand:
            case balonGreyjoy:
            case theonGreyjoy:
            case serGarlanTyrell:
            case randyllTarly:
                return 2;
            case salladhorSaan:
            case melisandre:
            case serKevanLannister:
            case tyrionLannister:
            case theBlackfish:
            case serRoderickCassel:
            case nymeriaSand:
            case arianneMartell:
            case dagmarCleftjaw:
            case ashaGreyjoy:
            case alesterFlorent:
            case margaeryTyrell:
                return 1;
            case patchface:
            case cerseiLannister:
            case catylynStark:
            case doranMartell:
            case aeronDamphair:
            case queenOfThorns:
            case none:
                return 0;
        }
        System.out.println(TextErrors.UNKNOWN_HOUSE_CARD_ERROR);
        return 0;
    }

    /**
     * Возвращает количество мечей на карте Дома
     * @return количество мечей
     */
    public int getNumSwords() {
        switch (this) {
            case serGregorClegane:
                return 3;
            case eddardStark:
            case theRedViper:
            case serGarlanTyrell:
                return 2;
            case brienneOfTarth:
            case melisandre:
            case serJaimeLannister:
            case greationUmber:
            case darkstar:
            case obaraSand:
            case euronCrowsEye:
            case dagmarCleftjaw:
            case randyllTarly:
                return 1;
            default:
                return 0;
        }
    }

    /**
     * Возвращает количество башен на карте Дома
     * @return количество башен
     */
    public int getNumTowers() {
        switch (this) {
            case hound:
            case serRoderickCassel:
                return 2;
            case brienneOfTarth:
            case theRedViper:
            case areoHotah:
            case dagmarCleftjaw:
            case alesterFlorent:
            case margaeryTyrell:
                return 1;
            default:
                return 0;
        }
    }

    /**
     * Метод определяет инициативу карты
     * @return инициатива карты
     */
    public CardInitiative getCardInitiative() {
        switch (this) {
            case tyrionLannister:
                return CardInitiative.cancel;
            case doranMartell:
            case aeronDamphair:
            case maceTyrell:
            case queenOfThorns:
                return CardInitiative.immediately;
            case stannisBaratheon:
            case serDevosSeaworth:
            case salladhorSaan:
            case serKevanLannister:
            case catylynStark:
            case nymeriaSand:
            case ashaGreyjoy:
            case theonGreyjoy:
            case victarionGreyjoy:
            case balonGreyjoy:
                return CardInitiative.bonus;
            case theBlackfish:
            case serLorasTyrell:
            case arianneMartell:
                return CardInitiative.passive;
            case robbStark:
                return CardInitiative.retreat;
            case renlyBaratheon:
            case tywinLannister:
            case cerseiLannister:
            case rooseBolton:
                return CardInitiative.afterFight;
            case patchface:
                return CardInitiative.patchface;
        }
        return CardInitiative.none;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
