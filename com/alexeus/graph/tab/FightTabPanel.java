package com.alexeus.graph.tab;

import com.alexeus.graph.util.ImageLoader;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.HouseCard;
import com.alexeus.logic.enums.SideOfBattle;
import com.alexeus.logic.struct.BattleInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import static com.alexeus.graph.constants.Constants.*;
import static com.alexeus.logic.constants.MainConstants.*;

/**
 * Created by alexeus on 13.01.2017.
 * Закладка с текущим (или последним) сражением
 */
public class FightTabPanel extends JPanel {

    private HashMap<HouseCard, BufferedImage> houseCardImage = new HashMap<>();
    private BufferedImage[] houseCardBackImage = new BufferedImage[NUM_PLAYER];
    private BufferedImage[] houseEmblemImage = new BufferedImage[NUM_PLAYER];
    private BufferedImage[] marchImage = new BufferedImage[3];
    private BufferedImage[] defenceImage = new BufferedImage[2];
    private BufferedImage[] garrisonImage = new BufferedImage[MAX_DEFENCE + 1];
    private BufferedImage swordImage, winImage, failImage, bonusImage;

    private int swordIconHeight, swordIconWidth, garrisonWidth, garrisonHeight,
            trueHouseCardBackImageWidth, trueSwordImageWidth, trueGarrisonWidth;

    private HouseCard[] card = new HouseCard[2];
    private int[] playerOnSide = new int[2];

    FightTabPanel() {
        loadPics();
        loadVariables();
    }

    @Override
    public void paintComponent(Graphics g) {
        Game game = Game.getInstance();
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        BattleInfo battleInfo = game.getBattleInfo();
        if (battleInfo != null && battleInfo.getAreaOfBattle() >= 0) {
            int width = getWidth();
            int trueCardWidth = (int)((width - 3 * FIGHT_X_INDENT) / 2f);
            int trueCardHeight = (int)(trueCardWidth * 1.57f);
            // Рисуем заголовок и карты домов
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("Liberation Mono", Font.BOLD, 25));
            g2d.drawString(FIGHT_FOR + game.getMap().getAreaNameRusAccusative(battleInfo.getAreaOfBattle()),
                    FIGHT_X_INDENT, FIGHT_TEXT_Y_INDENT);
            for (int side = 0; side < 2; side++) {
                card[side] = battleInfo.getCardOnSide(side);
                playerOnSide[side] = battleInfo.getPlayerOnSide(side);
            }
            g2d.drawImage(houseCardImage.containsKey(card[0]) ?
                          houseCardImage.get(card[0]) : houseCardBackImage[playerOnSide[0]],
                    FIGHT_X_INDENT, FIGHT_AFTER_TEXT_Y, trueCardWidth, trueCardHeight, null);
            g2d.drawImage(houseCardImage.containsKey(card[1]) ?
                            houseCardImage.get(card[1]) : houseCardBackImage[playerOnSide[1]],
                    (int) ((width + FIGHT_X_INDENT) / 2f), FIGHT_AFTER_TEXT_Y, trueCardWidth, trueCardHeight, null);
            if (battleInfo.getIsFightResolved()) {
                int failTrueHeight = (int) (1f * failImage.getHeight() * trueCardWidth / failImage.getWidth());
                int winTrueHeight = (int) (1f * winImage.getHeight() * trueCardWidth / winImage.getWidth());
                g2d.drawImage(winImage, FIGHT_X_INDENT + battleInfo.getWinnerSide() * (width / 2 + FIGHT_X_INDENT),
                        FIGHT_AFTER_TEXT_Y + (int) (trueCardHeight * CARD_TORSO_KOEF),
                        trueCardWidth, winTrueHeight, null);
                g2d.drawImage(failImage, FIGHT_X_INDENT + (1 - battleInfo.getWinnerSide()) * (width / 2 + FIGHT_X_INDENT),
                        FIGHT_AFTER_TEXT_Y + (int) (trueCardHeight * CARD_TORSO_KOEF),
                        trueCardWidth, failTrueHeight, null);
            }
            int curHeight = FIGHT_AFTER_TEXT_Y + trueCardHeight;
            if (!game.getIsSwordUsed()) {
                int trueSwordWidth = (int) (1f * SWORD_ICON_HEIGHT * swordIconWidth / swordIconHeight);
                g2d.drawImage(swordImage, (int) ((width - trueSwordWidth) / 2f), curHeight + FIGHT_BEFORE_SWORD_Y_INDENT,
                        trueSwordWidth, SWORD_ICON_HEIGHT, null);
            }
            curHeight += FIGHT_AFTER_SWORD_Y_INDENT + FIGHT_BEFORE_SWORD_Y_INDENT + SWORD_ICON_HEIGHT;

            // Рисуем слагаемые боевой силы сторон
            // TODO реализовать более аккуратный вывод в 2 строчки, если позволяет место
            g2d.setFont(new Font("Liberation Mono", Font.BOLD, 30));
            for (int side = 0; side < 2; side++) {
                boolean firstFlag = true;
                int calculatedWidth = calculateWidth(battleInfo, side);
                float sizeMultiplier = 1f;
                if (calculatedWidth > width / 2) {
                    sizeMultiplier = width / 2f / calculatedWidth;
                    calculatedWidth = width / 2;
                }
                int pos = (int) (side * width / 2f + width / 4f - calculatedWidth / 2f);
                // Модификатор приказа
                if (side == 0 && battleInfo.getMarchModifier() != 0) {
                    firstFlag = false;
                    g2d.drawImage(marchImage[battleInfo.getMarchModifier() + 1], pos, curHeight,
                            (int) (FIGHT_STRING_SIZE * sizeMultiplier), (int) (FIGHT_STRING_SIZE * sizeMultiplier),
                            null);
                    pos += FIGHT_STRING_SIZE + FIGHT_X_INDENT;

                }
                // Сила гарнизона
                if (side == 1 && battleInfo.getGarrisonModifier() > 0) {
                    firstFlag = false;
                    trueGarrisonWidth = (int) (1f * garrisonWidth * FIGHT_STRING_SIZE / garrisonHeight);
                    g2d.drawImage(garrisonImage[battleInfo.getGarrisonModifier()], pos, curHeight,
                            (int) (trueGarrisonWidth * sizeMultiplier), (int) (FIGHT_STRING_SIZE * sizeMultiplier),
                            null);
                    pos += trueGarrisonWidth + FIGHT_X_INDENT;
                }
                // Модификатор защины
                if (side == 1 && battleInfo.getDefenceModifier() > 0) {
                    if (firstFlag) {
                        firstFlag = false;
                    } else {
                        g2d.drawString("+", pos, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                        pos += FIGHT_X_INDENT + FIGHT_SYMBOL_INDENT;
                    }
                    g2d.drawImage(defenceImage[battleInfo.getDefenceModifier() - 1], pos, curHeight,
                            (int) (FIGHT_STRING_SIZE * sizeMultiplier), (int) (FIGHT_STRING_SIZE * sizeMultiplier),
                            null);
                    pos += FIGHT_STRING_SIZE + FIGHT_X_INDENT;
                }
                // Основной участник боя - атакующий или защищающийся
                if (!firstFlag) {
                    g2d.drawString("+", pos, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                    pos += FIGHT_X_INDENT + FIGHT_SYMBOL_INDENT;
                }
                g2d.drawImage(houseEmblemImage[playerOnSide[side]], pos, curHeight,
                        (int) (houseEmblemImage[playerOnSide[side]].getWidth() * sizeMultiplier),
                        (int) (FIGHT_STRING_SIZE * sizeMultiplier), null);
                pos += houseEmblemImage[playerOnSide[side]].getWidth() + FIGHT_X_INDENT;
                g2d.drawString(String.valueOf(battleInfo.getPlayerStrength(playerOnSide[side])),
                        pos, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                pos += FIGHT_X_INDENT + FIGHT_SYMBOL_INDENT;
                // Остальные участники боя - помогающие
                for (int player = 0; player < NUM_PLAYER; player++) {
                    if (battleInfo.getSupportOfPlayer(player) == side && player != playerOnSide[side]) {
                        g2d.drawString("+", pos, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                        pos += FIGHT_X_INDENT + FIGHT_SYMBOL_INDENT;
                        g2d.drawImage(houseEmblemImage[player], pos, curHeight,
                                (int) (houseEmblemImage[player].getWidth() * sizeMultiplier),
                                (int) (FIGHT_STRING_SIZE * sizeMultiplier), null);
                        pos += houseEmblemImage[player].getWidth() + FIGHT_X_INDENT;
                        g2d.drawString(String.valueOf(battleInfo.getPlayerStrength(player)),
                                pos, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                        pos += FIGHT_X_INDENT + FIGHT_SYMBOL_INDENT;
                    }
                }
                // Добавляем силу карт дома
                if (houseCardImage.containsKey(card[side]) && battleInfo.getCardStrengthOnSide(side) > 0) {
                    g2d.drawString("+", pos, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                    pos += FIGHT_X_INDENT + FIGHT_SYMBOL_INDENT;
                    g2d.drawImage(houseCardBackImage[playerOnSide[side]], pos, curHeight,
                            (int) (trueHouseCardBackImageWidth * sizeMultiplier),
                            (int) (FIGHT_STRING_SIZE * sizeMultiplier), null);
                    pos += trueHouseCardBackImageWidth + FIGHT_X_INDENT;
                    g2d.drawString(String.valueOf(battleInfo.getCardStrengthOnSide(side)),
                            pos, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                    pos += FIGHT_X_INDENT + FIGHT_SYMBOL_INDENT;
                }
                // Добавляем бонусную силу карт дома
                if (battleInfo.getBonusStrengthOnSide(side) > 0) {
                    g2d.drawString("+", pos, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                    pos += FIGHT_X_INDENT + FIGHT_SYMBOL_INDENT;
                    g2d.drawImage(bonusImage, pos, curHeight,
                            (int) (FIGHT_STRING_SIZE * sizeMultiplier), (int) (FIGHT_STRING_SIZE * sizeMultiplier),
                            null);
                    pos += FIGHT_STRING_SIZE + FIGHT_X_INDENT;
                    g2d.drawString(String.valueOf(battleInfo.getBonusStrengthOnSide(side)),
                            pos, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                    pos += FIGHT_X_INDENT + FIGHT_SYMBOL_INDENT;
                }
                // Добавляем валирийский меч
                if (battleInfo.getSideWhereSwordUsed() == side) {
                    g2d.drawString("+", pos, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                    pos += FIGHT_X_INDENT + FIGHT_SYMBOL_INDENT;
                    g2d.drawImage(swordImage, pos, curHeight, (int) (trueSwordImageWidth * sizeMultiplier),
                            (int) (FIGHT_STRING_SIZE * sizeMultiplier), null);
                }
            }
            // Рисуем финальное соотношение сил
            curHeight += FIGHT_STRING_SIZE + 2 * FIGHT_AFTER_SWORD_Y_INDENT;
            g2d.drawString(String.valueOf(battleInfo.getStrengthOnSide(SideOfBattle.attacker)),
                    (int) (width / 4f - FIGHT_SYMBOL_INDENT / 2f), curHeight + FIGHT_FINAL_TEXT_Y_INDENT);
            g2d.drawString(String.valueOf(battleInfo.getStrengthOnSide(SideOfBattle.defender)),
                    (int) (3f * width / 4f - FIGHT_SYMBOL_INDENT / 2f), curHeight + FIGHT_FINAL_TEXT_Y_INDENT);
        }
    }

    private int calculateWidth(BattleInfo battleInfo, int side) {
        // Основной участник боя - атакующий или защищающийся (это слагаемое есть всегда)
        int curWidth = houseEmblemImage[playerOnSide[side]].getWidth() + FIGHT_X_INDENT;
        // Модификатор приказа
        if (side == 0 && battleInfo.getMarchModifier() != 0) {
            curWidth += FIGHT_STRING_SIZE + FIGHT_SYMBOL_INDENT + FIGHT_X_INDENT * 2;

        }
        // Сила гарнизона
        if (side == 1 && battleInfo.getGarrisonModifier() > 0) {
            curWidth += trueGarrisonWidth + FIGHT_SYMBOL_INDENT + FIGHT_X_INDENT * 2;
        }
        // Модификатор защины
        if (side == 1 && battleInfo.getDefenceModifier() > 0) {
            curWidth += FIGHT_STRING_SIZE + FIGHT_SYMBOL_INDENT + FIGHT_X_INDENT * 2;
        }
        // Остальные участники боя - помогающие
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (battleInfo.getSupportOfPlayer(player) == side && player != playerOnSide[side]) {
                curWidth += houseEmblemImage[player].getWidth() + FIGHT_SYMBOL_INDENT * 2 + FIGHT_X_INDENT * 3;
            }
        }
        // Добавляем силу карт дома
        if (houseCardImage.containsKey(card[side]) && battleInfo.getCardStrengthOnSide(side) > 0) {
            curWidth += trueHouseCardBackImageWidth + FIGHT_SYMBOL_INDENT * 2 + FIGHT_X_INDENT * 3;
        }
        // Добавляем бонусную силу карт дома
        if (battleInfo.getBonusStrengthOnSide(side) > 0) {
            curWidth += FIGHT_STRING_SIZE + FIGHT_SYMBOL_INDENT * 2 + FIGHT_X_INDENT * 3;
        }
        // Добавляем валирийский меч
        if (battleInfo.getSideWhereSwordUsed() == side) {
            curWidth += FIGHT_SYMBOL_INDENT + trueSwordImageWidth + FIGHT_X_INDENT * 2;
        }
        return curWidth;
    }

    private void loadPics() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        swordImage = imageLoader.getImage(BATTLE + SWORD);
        failImage = imageLoader.getImage(BATTLE + FAIL);
        winImage = imageLoader.getImage(BATTLE + WIN);
        bonusImage = imageLoader.getImage(BONUS);
        for (int i = 0; i < 3; i++) {
            marchImage[i] = imageLoader.getImage(ORDER + (i + 2) + PNG);
        }
        for (int i = 0; i < 2; i++) {
            defenceImage[i] = imageLoader.getImage(ORDER + (i + 9) + PNG);
        }
        for (int i = MIN_DEFENCE; i <= MAX_DEFENCE; i++) {
            garrisonImage[i] = imageLoader.getImage(DEFENCE + i + PNG);
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            houseCardBackImage[player] = imageLoader.getImage(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + CARD_BACK);
            houseEmblemImage[player] = imageLoader.getImage(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + EMBLEM);
        }
        for (HouseCard card: HouseCard.values()) {
            if (card.house() < 0) continue;
            String fileName = card.getFileName();
            houseCardImage.put(card, imageLoader.getImage(HOUSE_CARD + fileName + PNG));
        }
    }

    private void loadVariables() {
        swordIconHeight = swordImage.getHeight();
        swordIconWidth = swordImage.getWidth();
        trueSwordImageWidth = (int) (1f * swordIconWidth * FIGHT_STRING_SIZE / swordIconHeight);
        garrisonWidth = garrisonImage[MIN_DEFENCE].getWidth();
        garrisonHeight = garrisonImage[MIN_DEFENCE].getHeight();
        trueHouseCardBackImageWidth = (int) (1f * houseCardBackImage[0].getWidth() * FIGHT_STRING_SIZE / houseCardBackImage[0].getHeight());
    }
}
