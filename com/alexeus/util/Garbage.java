package com.alexeus.util;

import com.alexeus.ai.math.BrownRobinsonSolver;
import com.alexeus.logic.Game;
import com.alexeus.logic.constants.MainConstants;

import java.util.Random;

import static com.alexeus.logic.constants.MainConstants.NUM_PLAYER;

/**
 * Created by alexeus on 13.01.2017.
 * Нахуй!м
 */
public class Garbage {

    final static float SWORD_PROFIT = 30f;
    final static float THRONE_PROFIT = 40f;
    final static float SECOND = THRONE_PROFIT / 2;
    final static float THIRD = THRONE_PROFIT / 8 * 3;
    final static float FOURTH = THRONE_PROFIT / 4;
    final static float FIFTH = THRONE_PROFIT / 8;
    final static float LAST = 0;
    final static float ORDER = THRONE_PROFIT / 3;

    // Профит от обладания определённым местом по треку ворона
    static float[][] priceStars = new float[MainConstants.NUM_PLAYER][MainConstants.NUM_PLAYER];
    // Профит от обладания деньгами
    static float[][] priceToken = new float[MainConstants.NUM_PLAYER][MainConstants.MAX_TOKENS + 1];

    static Random random = new Random();

    private static void brownRobinsonTest() {

        // Массив, показывающий, в каком порядке приоритетов по трекам будут игроки при разных королях
        int[][] defaultKingOrderPlayerOnPlace = {{0, 1, 5, 3, 4, 2},
                {1, 5, 3, 0, 2, 4},
                {2, 1, 5, 3, 4, 0},
                {3, 1, 0, 2, 4, 5},
                {4, 5, 3, 0, 2, 1},
                {5, 1, 0, 2, 4, 3}};

        // Профит для i-го игрока, что j-тый игрок сидит на троне
        float[][] priceFirstOnThrone = {    {THRONE_PROFIT, FOURTH, LAST, FOURTH, FOURTH, THIRD},
                {SECOND, THRONE_PROFIT, SECOND, SECOND, LAST, SECOND},
                {LAST, FIFTH, THRONE_PROFIT, FOURTH, FIFTH, FOURTH},
                {FOURTH, THIRD, FOURTH, THRONE_PROFIT, THIRD, LAST},
                {FIFTH, LAST, FIFTH, FIFTH, THRONE_PROFIT, FIFTH},
                {THIRD, SECOND, THIRD, LAST, SECOND, THRONE_PROFIT}};

        // Профит для i-го игрока, что j-тый игрок ниже его по трону
        float[][] priceHigherOnThrone = {{0f, 0f, ORDER, 0f, 0f, 0f},
                {0f, 0f, 0f, 0f, ORDER, 0f},
                {ORDER, 0f, 0f, 0f, 0f, 0f},
                {0f, 0f, 0f, 0f, 0f, ORDER},
                {0f, ORDER, 0f, 0f, 0f, 0f},
                {0f, 0f, 0f, ORDER, 0f, 0f}};

        // Профит для i-го игрока быть выше по мечу, чем j-тый игрок
        //float[][] priceFirstOnSword = new float[MainConstants.NUM_PLAYER][MainConstants.NUM_PLAYER];
        float[][] priceFirstOnSword = {  {SWORD_PROFIT, 0f, -SWORD_PROFIT, 0f, 0f, 0f},
                {0f, SWORD_PROFIT, 0f, 0f, -SWORD_PROFIT, 0f},
                {-SWORD_PROFIT, 0f, SWORD_PROFIT, 0f, 0f, 0f},
                {0f, 0f, 0f, SWORD_PROFIT, 0f, -SWORD_PROFIT},
                {0f, -SWORD_PROFIT, 0f, 0f, SWORD_PROFIT, 0f},
                {0f, 0f, 0f, -SWORD_PROFIT, 0f, SWORD_PROFIT}};

        // Профит для i-го игрока быть выше по мечу, чем j-тый игрок
        //float[][] priceHigherOnSword = new float[MainConstants.NUM_PLAYER][MainConstants.NUM_PLAYER];
        float[][] priceHigherOnSword = {{0, 0f, SWORD_PROFIT / 2, 0f, 0f, 0f},
                {0f, 0, 0f, 0f, SWORD_PROFIT / 2, 0f},
                {SWORD_PROFIT / 2, 0f, 0f, 0f, 0f, 0f},
                {0f, 0f, 0f, 0f, 0f, SWORD_PROFIT / 2},
                {0f, SWORD_PROFIT / 2, 0f, 0f, 0f, 0f},
                {0f, 0f, 0f, SWORD_PROFIT / 2, 0f, 0f}};

        for (int player = 0; player < MainConstants.NUM_PLAYER; player++) {
            priceToken[player][0] = 0;
            priceStars[player][0] = 90f;
            priceStars[player][1] = 80f;
            priceStars[player][2] = 60f;
            priceStars[player][3] = 35f;
            priceStars[player][4] = 1f;
            priceStars[player][5] = 0f;

            /*for (int opponent = 0; opponent < MainConstants.NUM_PLAYER; opponent++) {
                if (player == opponent) {
                    priceFirstOnSword[player][opponent] = SWORD_PROFIT;
                    priceHigherOnSword[player][opponent] = 0;
                } else {
                    priceFirstOnSword[player][opponent] = -SWORD_PROFIT / 5;
                    priceHigherOnSword[player][opponent] = SWORD_PROFIT / 5;
                }
            }*/
        }
        float temp = 0f;
        for (int i = 1; i <= MainConstants.MAX_TOKENS; i++) {
            temp += i == 1 ? 9 : (i > 4 ? 4 : (8 - i));
            for (int player = 0; player < MainConstants.NUM_PLAYER; player++) {
                priceToken[player][i] = temp;
            }
        }
        /*for (int i = 1; i <= MainConstants.MAX_TOKENS; i++) {
            temp += i == 1 ? 11 : 9;
            for (int player = 0; player < MainConstants.NUM_PLAYER; player++) {
                priceToken[player][i] = temp;
            }
        }*/

        //int[] money = {random.nextInt(15) + 1, random.nextInt(15) + 1, random.nextInt(15) + 1, random.nextInt(15) + 1, random.nextInt(15) + 1, random.nextInt(15) + 1};
        int[] money = {8, 8, 8, 8, 1, 0};
        // Задаём королевские порядки для каждого короля
        BrownRobinsonSolver.setKingOrders(defaultKingOrderPlayerOnPlace);
        // Король - Рандом
        int king = 0;
        System.out.println("Король: " + MainConstants.HOUSE[king]);
        BrownRobinsonSolver.setKing(king);

        float[][] results;
        //results = BrownRobinsonSolver.sixPlayersBiddingThrone(money, priceFirstOnThrone, priceHigherOnThrone, priceFirstOnSword, priceHigherOnSword, priceStars, priceToken);
        //results = BrownRobinsonSolver.sixPlayersBiddingSword(money, priceFirstOnSword, priceHigherOnSword, priceStars, priceToken);
        results = BrownRobinsonSolver.sixPlayersBiddingRaven(money, priceStars, priceToken);

        //System.out.println("Настоящие профиты.");
        /*for (int i = 0; i < MainConstants.NUM_PLAYER; i++) {
            System.out.println("\n" + MainConstants.HOUSE[i] + ".");
            for (int b = 0; b < results[i].length; b++) {
                System.out.println("Ставка " + b + ": " + results[i][b]);
            }
        }*/

        /*BrownRobinsonSolver.fillFutureGamePrice(priceFirstOnSword, priceHigherOnSword, priceStars);
        float[] estimatedProfit = BrownRobinsonSolver.primitiveThroneRestTokensEvaluation(money, priceToken, king);
        System.out.println("Оценочные профиты.");
        for (int player = 0; player < MainConstants.NUM_PLAYER; player++) {
            System.out.println(MainConstants.HOUSE[player] + ": " + estimatedProfit[player]);
        }*/
        BrownRobinsonSolver.primitiveSwordRestTokensEvaluation(money, priceStars, priceToken);
    }

    // Начинаем новое дерьмо - для теста Контроллера
    /*public void startNewShit() {
        Thread t = new Thread() {
            @Override
            public void run() {
                Game game = Game.getInstance();
                int shittingPlayer = 0;
                game.prepareNewGame();
                game.prepareNewShit(shittingPlayer);
                eventTabPanel.displayNewEvents();
                while (true) {
                    synchronized (Game.getInstance()) {
                        try {
                            shittingPlayer++;
                            if (shittingPlayer >= NUM_PLAYER) {
                                shittingPlayer = 0;
                            }
                            game.prepareNewShit(shittingPlayer);
                            int area = random.nextInt(58);
                            chat.append("Обновляется " + game.getMap().getAreaNameRus(area) + "\n");
                            mapPanel.repaintArea(area);
                            //mapPanel.repaint();
                            houseTabPanel.repaintHouse(shittingPlayer);
                            game.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Игра престолов началась.
                //setNewGamePhase(GamePhase.planningPhase);
            }
        };
        t.start();
    }*/
}
