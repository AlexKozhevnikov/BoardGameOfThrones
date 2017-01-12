package com.alexeus.logic.constants;

/**
 * Created by alexeus on 03.01.2017.
 * Основные текстовые, численные и массивные константы собраны здесь.
 */
public class MainConstants {

    // текстовые константы - коды
    public static final String RAVEN_SEES_WILDLINGS_CODE = "w";

    // текстовые константы - игроки
    public static final String HOUSE[] = {"Баратеон", "Ланнистер", "Старк", "Мартелл", "Грейджой", "Тирелл"};
    public static final String HOUSE_GENITIVE[] = {"Баратеона", "Ланнистера", "Старка", "Мартелла", "Грейджоя", "Тирелла"};
    public static final String HOUSE_DATIVE[] = {"Баратеону", "Ланнистеру", "Старку", "Мартеллу", "Грейджою", "Тиреллу"};
    public static final String HOUSE_ABLATIVE[] = {"Баратеоном", "Ланнистером", "Старком", "Мартеллом", "Грейджоем", "Тиреллом"};
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
    public static final int NUM_UNIT_TYPES = 4;

    public static final int[][] INITIAL_INFLUENCE_TRACKS_PLAYER_ON_PLACE = {{0, 1, 2, 3, 4, 5},
                                                                            {4, 5, 3, 2, 0, 1},
                                                                            {1, 2, 3, 0, 5, 4}};
    public static final int[] NUM_OF_STARS_ON_PLACE = {3, 3, 2, 1, 0, 0};

    public static final int[] MAX_NUM_OF_UNITS = {10, 5, 2, 6};
}