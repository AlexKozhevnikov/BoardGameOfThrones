package com.alexeus.ai.math;

import com.alexeus.logic.Constants;

import java.util.Random;

/**
 * Created by alexeus on 04.01.2017.
 * Класс реализует методы, решающие задачи оптимальных стратегий на торгах с несколькими участниками.
 */
public class BrownRobinsonSolver {

    private static Random random = new Random();
    private static final int N_ITERATIONS = 10000;
    private static final int PERCENT_ITERATIONS = N_ITERATIONS / 100;
    // Во сколько раз должно быть значение стратегии при обрезании меньше, чем CUT_ITERATION, чтобы она попала под обрезание
    private static final int CUT_MULTIPLIER = 200;
    // На какой итерации алгоритма (и кратных ей) выполнеяется обрезание.
    private static final int CUT_ITERATION = CUT_MULTIPLIER;
    //private static final int N_EXAMPLE = 5;

    private static final int nPlayer = Constants.NUM_PLAYER;

    private static boolean shutUp = false;

    // Переменные, нужные для расчётов цен оставшихся жетонов после ставок на трон
    private static float[] priceOfFutureGame = new float[nPlayer];

    // Выдаёт дом, который находится на данном месте по порядку симпатийпри данном короле
    static int[][] kingOrdersPlayerOnPlace = new int[Constants.NUM_PLAYER][Constants.NUM_PLAYER];
    // Выдаёт номер дома по порядку симпатий при данном короле. "Обратный" к kingOrdersPlayerOnPlace и высчитывается вместе с ним
    static int[][] kingOrdersPlaceForPlayer = new int[Constants.NUM_PLAYER][Constants.NUM_PLAYER];

    private static int king;

    /*
     * Метод для определения оптимальных стратегий ставок на Трон, в которых участвуют шесть, ШЕСТЬ игроков!!!
     * @param money[]                 массив с количествами жетонов власти у игроков
     * @param priceFirstOnThrone[][]  массив с ценами для игроков, что другой (или тот же) игрок будет королём
     * @param priceHigherOnThrone[][] массив с ценами для игроков, что другой игрок будет выше по треку трона
     * @param priceFirstOnSword[][]   массив с ценами для игроков, что другой (или тот же) игрок будет меченосцем
     * @param priceHigherOnSword[][]  массив с ценами для игроков, что другой игрок будет выше по треку меча
     * @param priceStars[][]          массив с ценами для игроков мест на треке ворона
     * @param priceToken[][]          массив с ценами оставшихся жетонов после всех трёх торгов жетонов
     * @returns              массив с вероятностями использования игроками своих статегий
     */
    public static float[][] sixPlayersBiddingThrone(int[] money, float[][] priceFirstOnThrone, float[][] priceHigherOnThrone,
                                               float[][] priceFirstOnSword, float[][] priceHigherOnSword,
                                               float[][] priceStars, float[][] priceToken) {

        int bid[] = new int[nPlayer];

        // Обрезание лишних жетонов у самого богатого игрока: ему бессмысленно их тратить.
        int trueMoney[];
        int curMaxMoney = money[0];
        int curMoneyBag = 0;
        for (int player = 1; player < nPlayer; player++) {
            if (curMaxMoney < money[player]) {
                curMaxMoney = money[player];
                curMoneyBag = player;
            } else if (curMaxMoney == money[player]) {
                curMoneyBag = -1;
            }
        }
        if (curMoneyBag >= 0) {
            int curSecondMaxMoney = Integer.MIN_VALUE;
            for (int player = 0; player < nPlayer; player++) {
                if (player != curMoneyBag && money[player] >= curSecondMaxMoney) {
                    curSecondMaxMoney = (kingOrdersPlaceForPlayer[king][player] < kingOrdersPlaceForPlayer[king][curMoneyBag]) ? money[player] + 1 : money[player];
                }
            }
            if (curSecondMaxMoney == money[curMoneyBag]) {
                trueMoney = money;
            } else {
                trueMoney = new int[nPlayer];
                for (int player = 0; player < nPlayer; player++) {
                    trueMoney[player] = player == curMoneyBag ? curSecondMaxMoney : money[player];
                }
            }
        } else {
            trueMoney = money;
        }

        //Контроль: вывод обрезанных денег
        if (!shutUp) {
            for (int player = 0; player < nPlayer; player++) {
                System.out.println("Игрок № " + player + ". Деньги: " + trueMoney[player]);
            }
        }

        /*
         * Рассчитываем массив profit
         */
        float profit[][][][][][][] = new float[nPlayer][trueMoney[0] + 1][trueMoney[1] + 1][trueMoney[2] + 1][trueMoney[3] + 1][trueMoney[4] + 1][trueMoney[5] + 1];
        int curMaxBid;
        int curMaxBidder;
        boolean playerIsHigherThanOpponent;
        int restMoney[] = new int[nPlayer];
        float estimatedSSProfit[];
        fillFutureGamePrice(priceFirstOnSword, priceHigherOnSword, priceStars);

        for (bid[0] = 0; bid[0] <= trueMoney[0]; bid[0]++) {
            for (bid[1] = 0; bid[1] <= trueMoney[1]; bid[1]++) {
                for (bid[2] = 0; bid[2] <= trueMoney[2]; bid[2]++) {
                    for (bid[3] = 0; bid[3] <= trueMoney[3]; bid[3]++) {
                        for (bid[4] = 0; bid[4] <= trueMoney[4]; bid[4]++) {
                            for (bid[5] = 0; bid[5] <= trueMoney[5]; bid[5]++) {
                                curMaxBid = bid[0];
                                curMaxBidder = 0;
                                // Определяем первое место
                                for (int player = 1; player < nPlayer; player++) {
                                    if (bid[player] > curMaxBid || bid[player] == curMaxBid &&
                                            kingOrdersPlaceForPlayer[king][player] < kingOrdersPlaceForPlayer[king][curMaxBidder]) {
                                        curMaxBid = bid[player];
                                        curMaxBidder = player;
                                    }
                                }

                                for (int player = 0; player < nPlayer; player++) {
                                    profit[player][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] += priceFirstOnThrone[player][curMaxBidder];
                                    for (int opponent = player + 1; opponent < nPlayer; opponent++) {
                                        playerIsHigherThanOpponent = bid[player] > bid[opponent] ||
                                                bid[player] == bid[opponent] && kingOrdersPlaceForPlayer[king][player] < kingOrdersPlaceForPlayer[king][opponent];
                                        profit[player][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] +=
                                                playerIsHigherThanOpponent ? priceHigherOnThrone[player][opponent] : -priceHigherOnThrone[player][opponent];
                                        profit[opponent][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] +=
                                                playerIsHigherThanOpponent ? -priceHigherOnThrone[opponent][player] : priceHigherOnThrone[opponent][player];
                                    }
                                    restMoney[player] = money[player] - bid[player];
                                }

                                // добавка к оценке из-за голосования оставшимися жетонами за меч и звёзды
                                estimatedSSProfit = primitiveThroneRestTokensEvaluation(restMoney, priceToken, curMaxBidder);
                                for (int player = 0; player < nPlayer; player++) {
                                    profit[player][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] += estimatedSSProfit[player];
                                }
                            }
                        }
                    }
                }
            }
        }

        // После того, как мы подготовили огромный массив profit, мы передаём его в основной этап расчёта - итерации
        return sixPlayersBiddingCommon(trueMoney, profit);
    }

    /*
     * Метод для определения оптимальных стратегий ставок на Меч, в которых участвуют шесть, ШЕСТЬ игроков!!!
     * @param money[][]               массив с количествами жетонов власти
     * @param priceFirstOnSword[][]   массив с ценами для игроков, что другой (или тот же) игрок будет меченосцем
     * @param priceHigherOnSword[][]  массив с ценами для игроков, что другой игрок будет выше по треку меча
     * @param priceStars[][]          массив с ценами для игроков мест на треке ворона
     * @param priceToken[][]          массив с ценами оставшихся жетонов после всех трёх торгов жетонов
     * @returns              массив с вероятностями использования игроками своих статегий
     */
    public static float[][] sixPlayersBiddingSword(int[] money, float[][] priceFirstOnSword, float[][] priceHigherOnSword,
                                              float[][] priceStars, float[][] priceToken) {

        int bid[] = new int[nPlayer];

        // Обрезание лишних жетонов у самого богатого игрока: ему бессмысленно их тратить.
        int trueMoney[];
        int curMaxMoney = money[0];
        int curMoneyBag = 0;
        for (int player = 1; player < nPlayer; player++) {
            if (curMaxMoney < money[player]) {
                curMaxMoney = money[player];
                curMoneyBag = player;
            } else if (curMaxMoney == money[player]) {
                curMoneyBag = -1;
            }
        }
        if (curMoneyBag >= 0) {
            int curSecondMaxMoney = Integer.MIN_VALUE;
            for (int player = 0; player < nPlayer; player++) {
                if (player != curMoneyBag && money[player] >= curSecondMaxMoney) {
                    curSecondMaxMoney = (kingOrdersPlaceForPlayer[king][player] < kingOrdersPlaceForPlayer[king][curMoneyBag]) ? money[player] + 1 : money[player];
                }
            }
            if (curSecondMaxMoney == money[curMoneyBag]) {
                trueMoney = money;
            } else {
                trueMoney = new int[nPlayer];
                for (int player = 0; player < nPlayer; player++) {
                    trueMoney[player] = player == curMoneyBag ? curSecondMaxMoney : money[player];
                }
            }
        } else {
            trueMoney = money;
        }

        //Контроль: вывод обрезанных денег
        if (!shutUp) {
            for (int player = 0; player < nPlayer; player++) {
                System.out.println("Игрок № " + player + ". Деньги: " + trueMoney[player]);
            }
        }

        /*
         * Рассчитываем массив profit
         */
        float profit[][][][][][][] = new float[nPlayer][trueMoney[0] + 1][trueMoney[1] + 1][trueMoney[2] + 1][trueMoney[3] + 1][trueMoney[4] + 1][trueMoney[5] + 1];
        int curMaxBid;
        int curMaxBidder;
        boolean playerIsHigherThanOpponent;
        int restMoney[] = new int[nPlayer];
        float estimatedStarsProfit[];
        for (bid[0] = 0; bid[0] <= trueMoney[0]; bid[0]++) {
            for (bid[1] = 0; bid[1] <= trueMoney[1]; bid[1]++) {
                for (bid[2] = 0; bid[2] <= trueMoney[2]; bid[2]++) {
                    for (bid[3] = 0; bid[3] <= trueMoney[3]; bid[3]++) {
                        for (bid[4] = 0; bid[4] <= trueMoney[4]; bid[4]++) {
                            for (bid[5] = 0; bid[5] <= trueMoney[5]; bid[5]++) {
                                curMaxBid = bid[0];
                                curMaxBidder = 0;
                                // Определяем первое место
                                for (int player = 1; player < nPlayer; player++) {
                                    if (bid[player] > curMaxBid || bid[player] == curMaxBid &&
                                            kingOrdersPlaceForPlayer[king][player] < kingOrdersPlaceForPlayer[king][curMaxBidder]) {
                                        curMaxBid = bid[player];
                                        curMaxBidder = player;
                                    }
                                }

                                for (int player = 0; player < nPlayer; player++) {
                                    profit[player][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] += priceFirstOnSword[player][curMaxBidder];
                                    for (int opponent = player + 1; opponent < nPlayer; opponent++) {
                                        playerIsHigherThanOpponent = bid[player] > bid[opponent] ||
                                                bid[player] == bid[opponent] && kingOrdersPlaceForPlayer[king][player] < kingOrdersPlaceForPlayer[king][opponent];
                                        profit[player][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] +=
                                                playerIsHigherThanOpponent ? priceHigherOnSword[player][opponent] : -priceHigherOnSword[player][opponent];
                                        profit[opponent][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] +=
                                                playerIsHigherThanOpponent ? -priceHigherOnSword[opponent][player] : priceHigherOnSword[opponent][player];
                                    }
                                    restMoney[player] = money[player] - bid[player];
                                }

                                // добавка к оценке из-за голосования оставшимися жетона за звёзды
                                estimatedStarsProfit = primitiveSwordRestTokensEvaluation(restMoney, priceStars, priceToken);
                                for (int player = 0; player < nPlayer; player++) {
                                    profit[player][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] += estimatedStarsProfit[player];
                                }
                            }
                        }
                    }
                }
            }
        }

        // После того, как мы подготовили огромный массив profit, мы передаём его в основной этап расчёта - итерации
        return sixPlayersBiddingCommon(trueMoney, profit);
    }

    /*
     * Метод для определения оптимальных стратегий ставок на ворона, в которых участвуют шесть, ШЕСТЬ игроков!!!
     * @param money[]        массив с количествами жетонов власти у игроков
     * @param priceStars[][] массив со стоимостями определённого места на треке ворона
     * @param priceToken[][] массив со стоимостями оставшихся жетонов
     * @returns              массив с вероятностями использования игроками своих статегий
     */
    public static float[][] sixPlayersBiddingRaven(int[] money, float[][] priceStars, float[][] priceToken) {

        int place[] = new int[nPlayer];
        int bid[] = new int[nPlayer];

        // Обрезание лишних жетонов у самого богатого игрока: ему бессмысленно их тратить.
        int trueMoney[];
        int curMaxMoney = money[0];
        int curMoneyBag = 0;
        for (int player = 1; player < nPlayer; player++) {
            if (curMaxMoney < money[player]) {
                curMaxMoney = money[player];
                curMoneyBag = player;
            } else if (curMaxMoney == money[player]) {
                curMoneyBag = -1;
            }
        }
        if (curMoneyBag >= 0) {
            int curSecondMaxMoney = Integer.MIN_VALUE;
            for (int player = 0; player < nPlayer; player++) {
                if (player != curMoneyBag && money[player] >= curSecondMaxMoney) {
                    curSecondMaxMoney = (kingOrdersPlaceForPlayer[king][player] < kingOrdersPlaceForPlayer[king][curMoneyBag]) ? money[player] + 1 : money[player];
                }
            }
            if (curSecondMaxMoney == money[curMoneyBag]) {
                trueMoney = money;
            } else {
                trueMoney = new int[nPlayer];
                for (int player = 0; player < nPlayer; player++) {
                    trueMoney[player] = player == curMoneyBag ? curSecondMaxMoney : money[player];
                }
            }
        } else {
            trueMoney = money;
        }

        //Контроль: вывод обрезанных денег
        if (!shutUp) {
            for (int player = 0; player < nPlayer; player++) {
                System.out.println("Игрок № " + player + ". Деньги: " + trueMoney[player]);
            }
        }

        /*
         * Рассчитываем массив profit
         */
        float profit[][][][][][][] = new float[nPlayer][trueMoney[0] + 1][trueMoney[1] + 1][trueMoney[2] + 1][trueMoney[3] + 1][trueMoney[4] + 1][trueMoney[5] + 1];
        for (bid[0] = 0; bid[0] <= trueMoney[0]; bid[0]++) {
            for (bid[1] = 0; bid[1] <= trueMoney[1]; bid[1]++) {
                for (bid[2] = 0; bid[2] <= trueMoney[2]; bid[2]++) {
                    for (bid[3] = 0; bid[3] <= trueMoney[3]; bid[3]++) {
                        for (bid[4] = 0; bid[4] <= trueMoney[4]; bid[4]++) {
                            for (bid[5] = 0; bid[5] <= trueMoney[5]; bid[5]++) {
                                for (int player = 0; player < nPlayer; player++) {
                                    place[player] = -1;
                                }
                                // присваиваем места игрокам
                                for (int curPlace = 1; curPlace <= nPlayer; curPlace++) {
                                    int curMax = Integer.MIN_VALUE;
                                    int curMaxer = -1;
                                    for (int player = 0; player < nPlayer; player++) {
                                        if (place[player] < 0 && (curMax < bid[player] || curMax == bid[player] &&
                                                kingOrdersPlaceForPlayer[king][player] < kingOrdersPlaceForPlayer[king][curMaxer])) {
                                            curMax = bid[player];
                                            curMaxer = player;
                                        }
                                    }
                                    place[curMaxer] = curPlace;
                                }

                                for (int player = 0; player < nPlayer; player++) {
                                    profit[player][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] =
                                            priceStars[player][place[player] - 1] + priceToken[player][money[player] - bid[player]];
                                }
                            }
                        }
                    }
                }
            }
        }

        return sixPlayersBiddingCommon(trueMoney, profit);
    }

    /*
     * Основной этап расчёта торгов с 6 игроками, используется при битве за трон, меч и ворона
     * @param money[]              массив с количествами жетонов власти у игроков
     * @param profit[][][][][][][] массив с выгодами от определённой конфигурации ставок, заранее расчитанный
     *                             в зависимости от трека, по которому производится голосование
     * @param priceRestTokens[][]  массив с выгодами от оставшихся жетонов власти
     * @returns                    массив с вероятностями выбора игроками своих стратегий
     */
    private static float[][] sixPlayersBiddingCommon(int[] commonMoney, float[][][][][][][] biddingProfit) {

        int bid[] = new int[nPlayer];

        // здесь хранятся числа, сколько раз была выбрана та или иная стратегия игрока
        int nStrategyChoices[][] = new int[nPlayer][];

        /*
         * здесь хранится информация для итераций - текущая цена каждой из стратегий игроков.
         * ВНИМАНИЕ: с целью повышения производительности она не делится на пятую степень числа итераций, поэтому
         * чтобы получить реальную цену игры для игрока, нужно разделить эти величины на пятую степень числа итераций.
         */
        float curStrategyProfit[][] = new float[nPlayer][];

        for (int player = 0; player < nPlayer; player++) {
            nStrategyChoices[player] = new int[commonMoney[player] + 1];
            curStrategyProfit[player] = new float[commonMoney[player] + 1];
        }

        /*
         * Расставляем начальные значения nStrategyChoices
         */
        /*for (int player = 0; player < nPlayer; player++) {
            nStrategyChoices[player][0]++;
        }*/
        int minMoney = Integer.MAX_VALUE;
        for (int player = 0; player < nPlayer; player++) {
            if (minMoney > commonMoney[player]) minMoney = commonMoney[player];
        }
        if (minMoney == 0) {
            minMoney = 1;
        }
        for (int player = 0; player < nPlayer; player++) {
            nStrategyChoices[player][commonMoney[player] == 0 ? 0 : minMoney]++;
        }

        /*
         * Собственно, итерации алгоритма
         */
        long startTime = System.currentTimeMillis();
        long time = startTime;
        for (int nIterations = 1; nIterations <= N_ITERATIONS; nIterations++) {
            // обнуляем величины nStrategyProfit
            for (int player = 0; player < nPlayer; player++) {
                for (int b = 0; b <= commonMoney[player]; b++) {
                    curStrategyProfit[player][b] = 0f;
                }
            }

            /*
             * Следующие 6 расчётов сделаны отдельными для повышения эффективности при отсеивании стратегий с нулевыми шансами.
             * Проверено: если их слить в один, то итерации работают в разы медленнее.
             */
            // расчёт профита для 1-ого игрока
            for (bid[1] = 0; bid[1] <= commonMoney[1]; bid[1]++) {
                if (nStrategyChoices[1][bid[1]] == 0) continue;
                for (bid[2] = 0; bid[2] <= commonMoney[2]; bid[2]++) {
                    if (nStrategyChoices[2][bid[2]] == 0) continue;
                    for (bid[3] = 0; bid[3] <= commonMoney[3]; bid[3]++) {
                        if (nStrategyChoices[3][bid[3]] == 0) continue;
                        for (bid[4] = 0; bid[4] <= commonMoney[4]; bid[4]++) {
                            if (nStrategyChoices[4][bid[4]] == 0) continue;
                            for (bid[5] = 0; bid[5] <= commonMoney[5]; bid[5]++) {
                                if (nStrategyChoices[5][bid[5]] == 0) continue;
                                for (bid[0] = 0; bid[0] <= commonMoney[0]; bid[0]++) {
                                    float increment = biddingProfit[0][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] *
                                            nStrategyChoices[1][bid[1]] * nStrategyChoices[2][bid[2]] * nStrategyChoices[3][bid[3]] *
                                            nStrategyChoices[4][bid[4]] * nStrategyChoices[5][bid[5]];
                                    curStrategyProfit[0][bid[0]] += increment;
                                }
                            }
                        }
                    }
                }
            }

            // расчёт профита для 2-ого игрока
            for (bid[0] = 0; bid[0] <= commonMoney[0]; bid[0]++) {
                if (nStrategyChoices[0][bid[0]] == 0) continue;
                for (bid[2] = 0; bid[2] <= commonMoney[2]; bid[2]++) {
                    if (nStrategyChoices[2][bid[2]] == 0) continue;
                    for (bid[3] = 0; bid[3] <= commonMoney[3]; bid[3]++) {
                        if (nStrategyChoices[3][bid[3]] == 0) continue;
                        for (bid[4] = 0; bid[4] <= commonMoney[4]; bid[4]++) {
                            if (nStrategyChoices[4][bid[4]] == 0) continue;
                            for (bid[5] = 0; bid[5] <= commonMoney[5]; bid[5]++) {
                                if (nStrategyChoices[5][bid[5]] == 0) continue;
                                for (bid[1] = 0; bid[1] <= commonMoney[1]; bid[1]++) {
                                    float increment = biddingProfit[1][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] *
                                            nStrategyChoices[0][bid[0]] * nStrategyChoices[2][bid[2]] * nStrategyChoices[3][bid[3]] *
                                            nStrategyChoices[4][bid[4]] * nStrategyChoices[5][bid[5]];
                                    curStrategyProfit[1][bid[1]] += increment;
                                }
                            }
                        }
                    }
                }
            }

            // расчёт профита для 3-ого игрока
            for (bid[0] = 0; bid[0] <= commonMoney[0]; bid[0]++) {
                if (nStrategyChoices[0][bid[0]] == 0) continue;
                for (bid[1] = 0; bid[1] <= commonMoney[1]; bid[1]++) {
                    if (nStrategyChoices[1][bid[1]] == 0) continue;
                    for (bid[3] = 0; bid[3] <= commonMoney[3]; bid[3]++) {
                        if (nStrategyChoices[3][bid[3]] == 0) continue;
                        for (bid[4] = 0; bid[4] <= commonMoney[4]; bid[4]++) {
                            if (nStrategyChoices[4][bid[4]] == 0) continue;
                            for (bid[5] = 0; bid[5] <= commonMoney[5]; bid[5]++) {
                                if (nStrategyChoices[5][bid[5]] == 0) continue;
                                for (bid[2] = 0; bid[2] <= commonMoney[2]; bid[2]++) {
                                    float increment = biddingProfit[2][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] *
                                            nStrategyChoices[0][bid[0]] * nStrategyChoices[1][bid[1]] * nStrategyChoices[3][bid[3]] *
                                            nStrategyChoices[4][bid[4]] * nStrategyChoices[5][bid[5]];
                                    curStrategyProfit[2][bid[2]] += increment;
                                }
                            }
                        }
                    }
                }
            }

            // расчёт профита для 4-ого игрока
            for (bid[0] = 0; bid[0] <= commonMoney[0]; bid[0]++) {
                if (nStrategyChoices[0][bid[0]] == 0) continue;
                for (bid[1] = 0; bid[1] <= commonMoney[1]; bid[1]++) {
                    if (nStrategyChoices[1][bid[1]] == 0) continue;
                    for (bid[2] = 0; bid[2] <= commonMoney[2]; bid[2]++) {
                        if (nStrategyChoices[2][bid[2]] == 0) continue;
                        for (bid[4] = 0; bid[4] <= commonMoney[4]; bid[4]++) {
                            if (nStrategyChoices[4][bid[4]] == 0) continue;
                            for (bid[5] = 0; bid[5] <= commonMoney[5]; bid[5]++) {
                                if (nStrategyChoices[5][bid[5]] == 0) continue;
                                for (bid[3] = 0; bid[3] <= commonMoney[3]; bid[3]++) {
                                    float increment = biddingProfit[3][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] *
                                            nStrategyChoices[0][bid[0]] * nStrategyChoices[1][bid[1]] * nStrategyChoices[2][bid[2]] *
                                            nStrategyChoices[4][bid[4]] * nStrategyChoices[5][bid[5]];
                                    curStrategyProfit[3][bid[3]] += increment;
                                }
                            }
                        }
                    }
                }
            }

            // расчёт профита для 5-ого игрока
            for (bid[0] = 0; bid[0] <= commonMoney[0]; bid[0]++) {
                if (nStrategyChoices[0][bid[0]] == 0) continue;
                for (bid[1] = 0; bid[1] <= commonMoney[1]; bid[1]++) {
                    if (nStrategyChoices[1][bid[1]] == 0) continue;
                    for (bid[2] = 0; bid[2] <= commonMoney[2]; bid[2]++) {
                        if (nStrategyChoices[2][bid[2]] == 0) continue;
                        for (bid[3] = 0; bid[3] <= commonMoney[3]; bid[3]++) {
                            if (nStrategyChoices[3][bid[3]] == 0) continue;
                            for (bid[5] = 0; bid[5] <= commonMoney[5]; bid[5]++) {
                                if (nStrategyChoices[5][bid[5]] == 0) continue;
                                for (bid[4] = 0; bid[4] <= commonMoney[4]; bid[4]++) {
                                    float increment = biddingProfit[4][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] *
                                            nStrategyChoices[0][bid[0]] * nStrategyChoices[1][bid[1]] * nStrategyChoices[2][bid[2]] *
                                            nStrategyChoices[3][bid[3]] * nStrategyChoices[5][bid[5]];
                                    curStrategyProfit[4][bid[4]] += increment;
                                }
                            }
                        }
                    }
                }
            }

            // расчёт профита для 6-ого игрока
            for (bid[0] = 0; bid[0] <= commonMoney[0]; bid[0]++) {
                if (nStrategyChoices[0][bid[0]] == 0) continue;
                for (bid[1] = 0; bid[1] <= commonMoney[1]; bid[1]++) {
                    if (nStrategyChoices[1][bid[1]] == 0) continue;
                    for (bid[2] = 0; bid[2] <= commonMoney[2]; bid[2]++) {
                        if (nStrategyChoices[2][bid[2]] == 0) continue;
                        for (bid[3] = 0; bid[3] <= commonMoney[3]; bid[3]++) {
                            if (nStrategyChoices[3][bid[3]] == 0) continue;
                            for (bid[4] = 0; bid[4] <= commonMoney[4]; bid[4]++) {
                                if (nStrategyChoices[4][bid[4]] == 0) continue;
                                for (bid[5] = 0; bid[5] <= commonMoney[5]; bid[5]++) {
                                    float increment = biddingProfit[5][bid[0]][bid[1]][bid[2]][bid[3]][bid[4]][bid[5]] *
                                            nStrategyChoices[0][bid[0]] * nStrategyChoices[1][bid[1]] * nStrategyChoices[2][bid[2]] *
                                            nStrategyChoices[3][bid[3]] * nStrategyChoices[4][bid[4]];
                                    curStrategyProfit[5][bid[5]] += increment;
                                }
                            }
                        }
                    }
                }
            }

            // Выбираем наилучную стратегию для каждого игрока. На последней итерации не нужно, потому что тогда нужно
            // просто рассчитать ожидаемую выгоду игроков.
            if (nIterations < N_ITERATIONS) {
                for (int player = 0; player < nPlayer; player++) {
                    float curMax = curStrategyProfit[player][0];
                    int curBestStrategy = 0;
                    for (int b = 1; b <= commonMoney[player]; b++) {
                        if (curMax < curStrategyProfit[player][b]) {
                            curBestStrategy = b;
                            curMax = curStrategyProfit[player][b];
                        }
                    }
                    nStrategyChoices[player][curBestStrategy]++;
                }
            }

            /*
             * ПОДРЕЗКА - для оптимизации поиска величины вроде 6E-4 сокращаются до нуля, и после этого итерации
             * работают заметно быстрее (иногда в разы), потому что стратегии других игроков с нулевыми шансами
             * пропускаются при подсчёте ожидаемой выгоды.
             */
            if (nIterations % CUT_ITERATION == 0 && nIterations != N_ITERATIONS) {
                for (int player = 0; player < nPlayer; player++) {
                    int nNormalStrategies = 0;
                    int[] normalStrategies = new int[Constants.MAX_TOKENS + 1];
                    int cut_sum = 0;
                    for (int strat = 0; strat <= commonMoney[player]; strat++) {
                        if (nStrategyChoices[player][strat] * CUT_MULTIPLIER <= nIterations) {
                            cut_sum += nStrategyChoices[player][strat];
                            nStrategyChoices[player][strat] = 0;
                        } else {
                            normalStrategies[nNormalStrategies] = strat;
                            nNormalStrategies++;
                        }
                    }
                    int curNormStr = 0;
                    for ( ; cut_sum > 0; cut_sum--) {
                        nStrategyChoices[player][normalStrategies[curNormStr]]++;
                        curNormStr++;
                        if (curNormStr >= nNormalStrategies) {
                            curNormStr = 0;
                        }
                    }
                }
            }

            // если данная итерация была "процентной", то выводим статистику
            if (nIterations % PERCENT_ITERATIONS == 0 && !shutUp) {
                float nIterationsFifth = 1f * nIterations * nIterations * nIterations * nIterations * nIterations;
                System.out.printf("%3s", nIterations / PERCENT_ITERATIONS);
                System.out.print("%  ");
                for (int player = 0; player < nPlayer; player++) {
                    float expectedProfit = 0;
                    for (int b = 0; b <= commonMoney[player]; b++) {
                        expectedProfit += curStrategyProfit[player][b] * nStrategyChoices[player][b] / nIterations;
                    }
                    System.out.printf("%3.1f  ", expectedProfit / nIterationsFifth);
                }
                long newTime = System.currentTimeMillis();
                System.out.println(" время: " + (newTime - time) / 1000f + "c");
                time = newTime;
            }
        }

        time = System.currentTimeMillis();
        /*
         * выводим полное время работы итераций
         */
        if (!shutUp) {
            System.out.println("Время работы итераций: " + (time - startTime) / 1000 + " секунд.");
        }

        float[][] results = new float[nPlayer][];
        for (int player = 0; player < nPlayer; player++) {
            results[player] = new float[commonMoney[player] + 1];
            for (int strat = 0; strat <= commonMoney[player]; strat++) {
                results[player][strat] = 1f * nStrategyChoices[player][strat] / N_ITERATIONS;
            }
        }
        return results;
    }

    /*
     * Метод ПРИМИТИВНО оценивает профит от жетонов, оставшихся после голосования за трон
     * @param restMoney[]    массив с оставшимися после голосования за трон количествами жетонов власти у игроков
     * @param priceToken[][] массив со стоимостями оставшихся жетонов
     * @param newKing        номер игрока, который станет новым королём при таких ставках
     */
    // TODO  Проверить, в каких пределах и насколько данная оценка точна
    public static float[] primitiveThroneRestTokensEvaluation(int[] restMoney, float[][] priceToken, int newKing) {
        float[] effectiveMoney = new float[nPlayer];
        float fraction;
        float[] finalProfit = new float[nPlayer];
        float KING_EFFECTIVE_TOKENS_REWARD = 2f;
        // Поправка числа эффективных жетонов из-за королевских прихотей
        float effectiveSumTokens = KING_EFFECTIVE_TOKENS_REWARD * 3;
        for (int player = 0; player < nPlayer; player++) {
            effectiveSumTokens += restMoney[player];
            effectiveMoney[player] = restMoney[player];
        }
        // Поправка к эффективным деньгам из-за королевских привилегий
        for (int place = 0; place < nPlayer; place++) {
            effectiveMoney[kingOrdersPlayerOnPlace[newKing][place]] += KING_EFFECTIVE_TOKENS_REWARD * (1f - place / (nPlayer - 1f));
        }
        for (int player = 0; player < nPlayer; player++) {
            // доля жетонов игрока из эффективного общего числа жетонов
            fraction = 1f * effectiveMoney[player] / effectiveSumTokens;
            if (fraction > 1) {
                finalProfit[player] = priceOfFutureGame[player] + priceToken[player][restMoney[player]];
            } else {
                finalProfit[player] = priceOfFutureGame[player] * 3 * fraction + priceToken[player][restMoney[player]];
            }
        }

        return finalProfit;
    }

    /*
     * Метод заполняет величины цен дальнейших ставок на меч и ворона после ставок на трон
     * @param priceFirstOnSword[][]   массив с ценами для игроков, что другой (или тот же) игрок будет меченосцем
     * @param priceHigherOnSword[][]  массив с ценами для игроков, что другой игрок будет выше по треку меча
     * @param priceStars[][]          массив со стоимостями определённого места на треке ворона
     */
    public static void fillFutureGamePrice(float[][] priceFirstOnSword, float[][] priceHigherOnSword,
                                     float[][] priceStars) {
        float maxSwordOwnPrice[] = new float[nPlayer];
        float minSwordOwnPrice[] = new float[nPlayer];
        for (int player = 0; player < nPlayer; player++) {
            priceOfFutureGame[player] = 0f;
            maxSwordOwnPrice[player] = Float.MIN_VALUE;
            minSwordOwnPrice[player] = Float.MAX_VALUE;
            for (int opponent = 0; opponent < nPlayer; opponent++) {
                if (priceFirstOnSword[player][opponent] > maxSwordOwnPrice[player]) {
                    maxSwordOwnPrice[player] = priceFirstOnSword[player][opponent];
                }
                if (priceFirstOnSword[player][opponent] < minSwordOwnPrice[player]) {
                    minSwordOwnPrice[player] = priceFirstOnSword[player][opponent];
                }
                if (opponent != player) {
                    priceOfFutureGame[player] += priceHigherOnSword[player][opponent];
                    priceOfFutureGame[player] += priceHigherOnSword[opponent][player];
                }
            }
            priceOfFutureGame[player] += maxSwordOwnPrice[player];
            priceOfFutureGame[player] -= minSwordOwnPrice[player];
            priceOfFutureGame[player] += priceStars[player][0];
        }
    }

    /*
     * Метод устанавливает новые королевские порядки игроков
     * @param newKingOrders массив с порядками игроков при короле i
     */
    public static void setKingOrders(int[][] newKingOrders) {
        kingOrdersPlayerOnPlace = newKingOrders;
        for (int king = 0; king < nPlayer; king++) {
            for (int place = 0; place < nPlayer; place++) {
                kingOrdersPlaceForPlayer[king][kingOrdersPlayerOnPlace[king][place]] = place;
            }
        }
    }

    /*
     * Метод примитивно, но довольно точно оценивает профит от жетонов, оставшихся после голосования за меч, сложность O(n^2)
     * @param restMoney[]    массив с оставшимися после голосования за меч количествами жетонов власти у игроков
     * @param priceStars[][] массив со стоимостями определённого места на треке ворона
     * @param priceToken[][] массив со стоимостями оставшихся жетонов
     */
    public static float[] primitiveSwordRestTokensEvaluation(int[] restMoney, float[][] priceStars, float[][] priceToken) {
        int place[] = new int[nPlayer];
        int playerOnPlace[] = new int[nPlayer];
        float finalProfit[] = new float[nPlayer];
        int restRestTokens;
        int spentTokens[] = new int[nPlayer];
        //инициализация
        for (int player = 0; player < nPlayer; player++) {
            place[player] = -1;
            spentTokens[player] = 0;
            // начинаем подсчёт выгоды для игроков с ситуации, когда они не тратят жетонов на звёзды
            finalProfit[player] = priceToken[player][restMoney[player]];
        }
        // присваиваем места игрокам
        for (int curPlace = 1; curPlace <= nPlayer; curPlace++) {
            int curMax = Integer.MIN_VALUE;
            int curMaxer = -1;
            for (int player = 0; player < nPlayer; player++) {
                if (place[player] < 0 && (curMax < restMoney[player] || curMax == restMoney[player] &&
                        kingOrdersPlaceForPlayer[king][player] < kingOrdersPlaceForPlayer[king][curMaxer])) {
                    curMax = restMoney[player];
                    curMaxer = player;
                }
            }
            place[curMaxer] = curPlace;
            playerOnPlace[curPlace - 1] = curMaxer;
        }

        // Звёзды и снисхождения
        for (int player = 0; player < nPlayer; player++) {
            for (int opponent = 0; opponent < nPlayer; opponent++) {
                if (kingOrdersPlaceForPlayer[king][player] >= kingOrdersPlaceForPlayer[king][opponent]) continue;
                // Проверяем, выгодно ли одному игроку снизойти до другого, и если выгодно, то обновляем finalProfit
                if (place[player] < place[opponent]) {
                    restRestTokens = restMoney[player] - restMoney[opponent];
                    if (priceStars[player][place[opponent] - 2] + priceToken[player][restRestTokens] > finalProfit[player]) {
                        finalProfit[player] = priceStars[player][place[opponent] - 2] + priceToken[player][restRestTokens];
                        spentTokens[player] = restMoney[player] - restRestTokens;
                    }
                } else {
                    restRestTokens = restMoney[opponent] - restMoney[player] - 1;
                    if (priceStars[opponent][place[player] - 2] + priceToken[opponent][restRestTokens] > finalProfit[opponent]) {
                        finalProfit[opponent] = priceStars[opponent][place[player] - 2] + priceToken[opponent][restRestTokens];
                        spentTokens[opponent] = restMoney[opponent] - restRestTokens;
                    }
                }
            }
        }
        if (spentTokens[playerOnPlace[5]] == 0 && spentTokens[playerOnPlace[4]] == 0) {
            if (kingOrdersPlaceForPlayer[king][playerOnPlace[5]] < kingOrdersPlaceForPlayer[king][playerOnPlace[4]]) {
                finalProfit[playerOnPlace[5]] += priceStars[playerOnPlace[5]][4];
                System.out.println("Чек!");
            } else {
                finalProfit[playerOnPlace[4]] += priceStars[playerOnPlace[4]][4];
                System.out.println("Чек2!");
            }
        }

        // коррекция профитов из-за эффекта каскадного снисхождения
        float descent, oldDescent = 0f;
        float cascadeDescentCorrection[] = new float[nPlayer];
        float[] asymptothicTokenPrice = new float[nPlayer];
        for (int player = 0; player < nPlayer; player++) {
            asymptothicTokenPrice[player] = 2f * (priceToken[player][Constants.MAX_TOKENS] - priceToken[player][Constants.MAX_TOKENS / 2]) / Constants.MAX_TOKENS;
        }
        for (int curPlace = 3; curPlace >= 0; curPlace--) {
            descent = oldDescent + (kingOrdersPlaceForPlayer[king][playerOnPlace[curPlace + 1]] >
                    kingOrdersPlaceForPlayer[king][playerOnPlace[curPlace + 2]] ?
                    restMoney[playerOnPlace[curPlace + 1]] - restMoney[playerOnPlace[curPlace + 2]] - 1f:
                    restMoney[playerOnPlace[curPlace + 1]] - restMoney[playerOnPlace[curPlace + 2]]);
            cascadeDescentCorrection[playerOnPlace[curPlace]] += descent * asymptothicTokenPrice[playerOnPlace[curPlace]] / 2f;
            oldDescent = descent / 2;
        }

        if (!shutUp) {
            for (int i = 0; i < nPlayer; i++) {
                System.out.println("Выгода " + Constants.HOUSE_GENITIVE[i] + ": " + finalProfit[i] + " + " +
                        cascadeDescentCorrection[i] + ". Деньги: " + restMoney[i] + ". Потрачено: " + spentTokens[i]);
            }
        }
        for (int player = 0; player < nPlayer; player++) {
            finalProfit[player] += cascadeDescentCorrection[player];
        }
        return finalProfit;
    }

    public static void setShutUp(boolean b) {
        shutUp = b;
    }

    public static void setKing(int i) {
        king = i;
    }

    public static int getKing(int i) {
        return king;
    }
}
