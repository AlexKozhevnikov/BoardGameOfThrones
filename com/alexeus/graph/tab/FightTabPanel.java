package com.alexeus.graph.tab;

import com.alexeus.graph.util.ImageLoader;
import com.alexeus.logic.Game;
import com.alexeus.logic.GameModel;
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

    private final static int MAX_NUM_LINES = 2;

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
    private int[] playerOnSide = new int[MAX_NUM_LINES];

    private Font headerFont, textFont;
    private int plusWidth;
    private int[] lineWidth;
    private int numLines;

    FightTabPanel() {
        loadPics();
        loadVariables();
    }

    @Override
    public void paintComponent(Graphics g) {
        GameModel model = Game.getInstance().getModel();
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        BattleInfo battleInfo = model.getBattleInfo();
        if (battleInfo != null && battleInfo.getAreaOfBattle() >= 0) {
            int width = getWidth();
            int trueCardWidth = (int)((width - 3 * FIGHT_INDENT) / 2f);
            int trueCardHeight = (int)(trueCardWidth * 1.57f);
            // Рисуем заголовок и карты домов
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(headerFont);
            FontMetrics metrics = g2d.getFontMetrics();
            String header = FIGHT_FOR + model.getMap().getAreaNameRusAccusative(battleInfo.getAreaOfBattle());
            g2d.drawString(header, (width - metrics.stringWidth(header)) / 2, FIGHT_TEXT_Y_INDENT);
            for (int side = 0; side < 2; side++) {
                card[side] = battleInfo.getCardOnSide(side);
                playerOnSide[side] = battleInfo.getPlayerOnSide(side);
            }
            g2d.drawImage(houseCardImage.containsKey(card[0]) ?
                          houseCardImage.get(card[0]) : houseCardBackImage[playerOnSide[0]],
                    FIGHT_INDENT, FIGHT_AFTER_TEXT_Y, trueCardWidth, trueCardHeight, null);
            g2d.drawImage(houseCardImage.containsKey(card[1]) ?
                            houseCardImage.get(card[1]) : houseCardBackImage[playerOnSide[1]],
                    (int) ((width + FIGHT_INDENT) / 2f), FIGHT_AFTER_TEXT_Y, trueCardWidth, trueCardHeight, null);
            if (battleInfo.getIsFightResolved()) {
                int failTrueHeight = (int) (1f * failImage.getHeight() * trueCardWidth / failImage.getWidth());
                int winTrueHeight = (int) (1f * winImage.getHeight() * trueCardWidth / winImage.getWidth());
                g2d.drawImage(winImage, FIGHT_INDENT + battleInfo.getWinnerSide() * (width / 2 + FIGHT_INDENT),
                        FIGHT_AFTER_TEXT_Y + (int) (trueCardHeight * CARD_TORSO_KOEF),
                        trueCardWidth, winTrueHeight, null);
                g2d.drawImage(failImage, FIGHT_INDENT + (1 - battleInfo.getWinnerSide()) * (width / 2 + FIGHT_INDENT),
                        FIGHT_AFTER_TEXT_Y + (int) (trueCardHeight * CARD_TORSO_KOEF),
                        trueCardWidth, failTrueHeight, null);
            }

            // Рисуем слагаемые боевой силы сторон
            g2d.setFont(textFont);
            metrics = g2d.getFontMetrics();
            plusWidth = metrics.stringWidth("+");
            int curLine = 0;
            for (int side = 0; side < 2; side++) {
                int curHeight = FIGHT_SPECIFICS_Y;
                boolean firstFlag = true;
                calculateWidth(battleInfo, side, metrics);
                if (numLines == 2) {
                    curHeight -= (int) (FIGHT_STRING_SIZE / 2f);
                }
                int lineBegin = (int) (side * width / 2f + width / 4f - lineWidth[curLine] / 2f);
                int lineShift = 0;
                // Модификатор приказа
                if (side == 0 && battleInfo.getMarchModifier() != 0) {
                    firstFlag = false;
                    g2d.drawImage(marchImage[battleInfo.getMarchModifier() + 1], lineBegin, curHeight,
                            FIGHT_STRING_SIZE, FIGHT_STRING_SIZE, null);
                    lineShift += FIGHT_STRING_SIZE + FIGHT_SMALL_X_INDENT;
                }
                // Сила гарнизона
                if (side == 1 && battleInfo.getGarrisonModifier() > 0) {
                    firstFlag = false;
                    trueGarrisonWidth = (int) (1f * garrisonWidth * FIGHT_STRING_SIZE / garrisonHeight);
                    g2d.drawImage(garrisonImage[battleInfo.getGarrisonModifier()], lineBegin, curHeight,
                            trueGarrisonWidth, FIGHT_STRING_SIZE, null);
                    lineShift += trueGarrisonWidth + FIGHT_SMALL_X_INDENT;
                }
                // Модификатор защиты
                if (side == 1 && battleInfo.getDefenceModifier() > 0) {
                    if (firstFlag) {
                        firstFlag = false;
                    } else {
                        g2d.drawString("+", lineBegin + lineShift, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                        lineShift += plusWidth + FIGHT_SMALL_X_INDENT;
                    }
                    g2d.drawImage(defenceImage[battleInfo.getDefenceModifier() - 1], lineBegin + lineShift, curHeight,
                            FIGHT_STRING_SIZE, FIGHT_STRING_SIZE, null);
                    lineShift += FIGHT_STRING_SIZE + FIGHT_SMALL_X_INDENT;
                }
                // Основной участник боя - атакующий или защищающийся
                if (!firstFlag) {
                    g2d.drawString("+", lineBegin + lineShift, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                    lineShift += plusWidth + FIGHT_SMALL_X_INDENT;
                }
                g2d.drawImage(houseEmblemImage[playerOnSide[side]], lineBegin + lineShift, curHeight,
                        houseEmblemImage[playerOnSide[side]].getWidth(), FIGHT_STRING_SIZE, null);
                lineShift += houseEmblemImage[playerOnSide[side]].getWidth() + FIGHT_SMALL_X_INDENT;
                String text = String.valueOf(battleInfo.getPlayerStrength(playerOnSide[side]));
                g2d.drawString(text, lineBegin + lineShift, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                lineShift += metrics.stringWidth(text) + FIGHT_SMALL_X_INDENT;
                // Остальные участники боя - помогающие
                for (int player = 0; player < NUM_PLAYER; player++) {
                    if (battleInfo.getSupportOfPlayer(player) == side && player != playerOnSide[side]) {
                        text = String.valueOf(battleInfo.getPlayerStrength(player));
                        if (lineShift >= lineWidth[curLine]) {
                            curLine++;
                            lineBegin = (int) (side * width / 2f + width / 4f - lineWidth[curLine] / 2f);
                            lineShift = 0;
                            curHeight += FIGHT_STRING_SIZE;
                        }
                        g2d.drawString("+", lineBegin + lineShift, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                        lineShift += plusWidth + FIGHT_SMALL_X_INDENT;
                        g2d.drawImage(houseEmblemImage[player], lineBegin + lineShift, curHeight,
                                houseEmblemImage[player].getWidth(), FIGHT_STRING_SIZE, null);
                        lineShift += houseEmblemImage[player].getWidth() + FIGHT_SMALL_X_INDENT;
                        g2d.drawString(text, lineBegin + lineShift, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                        lineShift += metrics.stringWidth(text) + FIGHT_SMALL_X_INDENT;
                    }
                }
                // Добавляем силу карт дома
                if (houseCardImage.containsKey(card[side]) && battleInfo.getCardStrengthOnSide(side) > 0) {
                    text = String.valueOf(battleInfo.getCardStrengthOnSide(side));
                    if (lineShift >= lineWidth[curLine]) {
                        curLine++;
                        lineBegin = (int) (side * width / 2f + width / 4f - lineWidth[curLine] / 2f);
                        lineShift = 0;
                        curHeight += FIGHT_STRING_SIZE;
                    }
                    g2d.drawString("+", lineBegin + lineShift, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                    lineShift += plusWidth + FIGHT_SMALL_X_INDENT;
                    g2d.drawImage(houseCardBackImage[playerOnSide[side]], lineBegin + lineShift, curHeight,
                            trueHouseCardBackImageWidth, FIGHT_STRING_SIZE, null);
                    lineShift += trueHouseCardBackImageWidth + FIGHT_SMALL_X_INDENT;
                    g2d.drawString(text, lineBegin + lineShift, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                    lineShift += metrics.stringWidth(text) + FIGHT_SMALL_X_INDENT;
                }
                // Добавляем бонусную силу карт дома
                if (battleInfo.getBonusStrengthOnSide(side) > 0) {
                    text = String.valueOf(battleInfo.getBonusStrengthOnSide(side));
                    if (lineShift >= lineWidth[curLine]) {
                        curLine++;
                        lineBegin = (int) (side * width / 2f + width / 4f - lineWidth[curLine] / 2f);
                        lineShift = 0;
                        curHeight += FIGHT_STRING_SIZE;
                    }
                    g2d.drawString("+", lineBegin + lineShift, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                    lineShift += plusWidth + FIGHT_SMALL_X_INDENT;
                    g2d.drawImage(bonusImage, lineBegin + lineShift, curHeight, FIGHT_STRING_SIZE, FIGHT_STRING_SIZE,
                            null);
                    lineShift += FIGHT_STRING_SIZE + FIGHT_SMALL_X_INDENT;
                    g2d.drawString(text, lineBegin + lineShift, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                    lineShift += metrics.stringWidth(text) + FIGHT_SMALL_X_INDENT;
                }
                // Добавляем валирийский меч
                if (battleInfo.getSideWhereSwordUsed() == side) {
                    if (lineShift >= lineWidth[curLine]) {
                        curLine++;
                        lineBegin = (int) (side * width / 2f + width / 4f - lineWidth[curLine] / 2f);
                        lineShift = 0;
                        curHeight += FIGHT_STRING_SIZE;
                    }
                    g2d.drawString("+", lineBegin + lineShift, curHeight + FIGHT_STRING_TEXT_Y_INDENT);
                    lineShift += plusWidth + FIGHT_SMALL_X_INDENT;
                    g2d.drawImage(swordImage, lineBegin + lineShift, curHeight, trueSwordImageWidth, FIGHT_STRING_SIZE, null);
                    lineShift += trueSwordImageWidth;
                }
            }
            // Рисуем финальное соотношение сил
            String text = String.valueOf(battleInfo.getStrengthOnSide(SideOfBattle.attacker));
            g2d.drawString(text, (int) (width / 4f - metrics.stringWidth(text) / 2f), FIGHT_FINAL_TEXT_Y);
            text = String.valueOf(battleInfo.getStrengthOnSide(SideOfBattle.defender));
            g2d.drawString(text, (int) (3f * width / 4f - metrics.stringWidth(text) / 2f), FIGHT_FINAL_TEXT_Y);
            // Иконка валирийского меча, если он ещё не использован в текущем раунде
            if (!model.getIsSwordUsed()) {
                int trueSwordWidth = (int) (1f * SWORD_ICON_HEIGHT * swordIconWidth / swordIconHeight);
                g2d.drawImage(swordImage, (int) ((width - trueSwordWidth) / 2f), FIGHT_FINAL_SWORD_Y,
                        trueSwordWidth, SWORD_ICON_HEIGHT, null);
            }
        }
        System.out.println();
    }

    private void calculateWidth(BattleInfo battleInfo, int side, FontMetrics metrics) {
        numLines = 1;
        for (int i = 0; i < MAX_NUM_LINES; i++) {
            lineWidth[i] = 0;
        }
        // Основной участник боя - атакующий или защищающийся (это слагаемое есть всегда)
        lineWidth[numLines - 1] = houseEmblemImage[playerOnSide[side]].getWidth() + FIGHT_SMALL_X_INDENT * 2 +
                metrics.stringWidth(String.valueOf(battleInfo.getPlayerStrength(playerOnSide[side])));
        // Модификатор приказа
        if (side == 0 && battleInfo.getMarchModifier() != 0) {
            lineWidth[numLines - 1] += FIGHT_STRING_SIZE + plusWidth + FIGHT_SMALL_X_INDENT * 2;
        }
        // Сила гарнизона
        if (side == 1 && battleInfo.getGarrisonModifier() > 0) {
            lineWidth[numLines - 1] += trueGarrisonWidth + plusWidth + FIGHT_SMALL_X_INDENT * 2;
        }
        // Модификатор защиты
        if (side == 1 && battleInfo.getDefenceModifier() > 0) {
            lineWidth[numLines - 1] += FIGHT_STRING_SIZE + plusWidth + FIGHT_SMALL_X_INDENT * 2;
        }
        // Остальные участники боя - помогающие
        for (int player = 0; player < NUM_PLAYER; player++) {
            if (battleInfo.getSupportOfPlayer(player) == side && player != playerOnSide[side]) {
                increaseLineWidth(houseEmblemImage[player].getWidth() + plusWidth + FIGHT_SMALL_X_INDENT * 3 +
                        metrics.stringWidth(String.valueOf(battleInfo.getPlayerStrength(player))));
            }
        }
        // Добавляем силу карт дома
        if (houseCardImage.containsKey(card[side]) && battleInfo.getCardStrengthOnSide(side) > 0) {
            increaseLineWidth(trueHouseCardBackImageWidth + plusWidth + FIGHT_SMALL_X_INDENT * 3 +
                    metrics.stringWidth(String.valueOf(battleInfo.getCardStrengthOnSide(side))));
        }
        // Добавляем бонусную силу карт дома
        if (battleInfo.getBonusStrengthOnSide(side) > 0) {
            increaseLineWidth(FIGHT_STRING_SIZE + plusWidth + FIGHT_SMALL_X_INDENT * 3 +
                    metrics.stringWidth(String.valueOf(battleInfo.getBonusStrengthOnSide(side))));
        }
        // Добавляем валирийский меч
        if (battleInfo.getSideWhereSwordUsed() == side) {
            increaseLineWidth(FIGHT_SYMBOL_INDENT + trueSwordImageWidth + FIGHT_SMALL_X_INDENT * 2);
        }
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
        trueHouseCardBackImageWidth = (int) (1f * houseCardBackImage[0].getWidth() *
                FIGHT_STRING_SIZE / houseCardBackImage[0].getHeight());
        headerFont = new Font("Serif", Font.BOLD, 28);
        textFont = new Font("Liberation Mono", Font.BOLD, 30);
        lineWidth = new int[2];
    }

    private void increaseLineWidth(int delta) {
        if (lineWidth[numLines - 1] + delta > getWidth() / 2) {
            numLines++;
        }
        lineWidth[numLines - 1] += delta;
    }
}
