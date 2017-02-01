package com.alexeus.graph;

import com.alexeus.graph.util.ImageLoader;
import com.alexeus.logic.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static com.alexeus.graph.constants.Constants.*;
import static com.alexeus.logic.constants.MainConstants.*;

/**
 * Created by alexeus on 28.01.2017.
 * Панель отображает результаты игры.
 */
public class EndGamePanel extends JPanel {

    private BufferedImage[] victoryImage = new BufferedImage[NUM_PLAYER];

    private BufferedImage[] emblemImage = new BufferedImage[NUM_PLAYER];

    private BufferedImage[] supplyImage = new BufferedImage[NUM_PLAYER];

    private BufferedImage[] tokenImage = new BufferedImage[NUM_PLAYER];

    private BufferedImage throneImage, fortressImage;

    private int emblemTrueWidth, supplyTrueWidth, victoryTrueWidth, fortressTrueWidth, tokenTrueWidth, throneTrueWidth;

    private int headerWidth, totalWidth, totalHeight;

    private int maxVictoryWidth = 0, maxFortressWidth = 0, maxSupplyWidth = 0, maxTokenWidth = 0, maxThroneWidth = 0;

    private String headerString;

    private Font headerFont, commonFont;

    /**
     * Показывает, сколько данных о результатах игрока на определённом месте нужно показывать
     */
    private int[] numPositionsToShowOnPlace;

    /**
     * Максимальное значение позиции, которое нужно отображать
     */
    private int maxPositionToShow;

    /**
     * Показывает, какой игрок находится на данном месте
     */
    private int[] playerOnPlace;

    public EndGamePanel() {
        loadPics();
        fillTrueWidths();
        fillInfo();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Game game = Game.getInstance();
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int curHeight = ENDSPIEL_MAIN_TEXT_Y_INDENT;
        g2d.setFont(headerFont);
        g2d.drawString(headerString, (totalWidth - headerWidth) / 2, curHeight);
        curHeight = ENDSPIEL_TABLE_Y;
        g2d.setFont(commonFont);
        FontMetrics metrics = g2d.getFontMetrics(commonFont);
        for (int place = 0; place < NUM_PLAYER; place++) {
            int curX = ENDSPIEL_INDENT;
            g2d.drawString((place + 1) + ". ", curX, curHeight + (ENDSPIEL_HEIGHT + ENDSPIEL_TEXT_Y_INDENT) / 2);
            curX += ENDSPIEL_TEXT_WIDTH;
            Image emblem = emblemImage[playerOnPlace[place]];
            int trueWidth = (int) (1f * emblem.getWidth(null) * ENDSPIEL_HEIGHT / emblemImage[3].getHeight(null));
            g2d.drawImage(emblem, curX + (emblemTrueWidth - trueWidth) / 2, curHeight, trueWidth, ENDSPIEL_HEIGHT, null);
            curX += emblemTrueWidth + ENDSPIEL_EMBLEM_INDENT;
            g2d.drawImage(victoryImage[playerOnPlace[place]], curX, curHeight, victoryTrueWidth, ENDSPIEL_HEIGHT, null);
            curX += victoryTrueWidth + ENDSPIEL_INDENT;
            String stringValue = String.valueOf(game.getVictoryPoints(playerOnPlace[place]));
            g2d.drawString(stringValue, curX + (maxVictoryWidth - metrics.stringWidth(stringValue)) / 2,
                    curHeight + (ENDSPIEL_HEIGHT + ENDSPIEL_TEXT_Y_INDENT) / 2);
            curX += maxVictoryWidth + ENDSPIEL_GROUP_INDENT;
            if (numPositionsToShowOnPlace[place] > 0) {
                g2d.drawImage(fortressImage, curX, curHeight, fortressTrueWidth, ENDSPIEL_HEIGHT, null);
                curX += fortressTrueWidth + ENDSPIEL_INDENT;
                stringValue = String.valueOf(game.getNumFortress(playerOnPlace[place]));
                g2d.drawString(stringValue, curX + (maxFortressWidth - metrics.stringWidth(stringValue)) / 2,
                        curHeight + (ENDSPIEL_HEIGHT + ENDSPIEL_TEXT_Y_INDENT) / 2);
                curX += maxFortressWidth + ENDSPIEL_GROUP_INDENT;
            }
            if (numPositionsToShowOnPlace[place] > 1) {
                g2d.drawImage(supplyImage[playerOnPlace[place]], curX, curHeight, supplyTrueWidth, ENDSPIEL_HEIGHT, null);
                curX += supplyTrueWidth + ENDSPIEL_INDENT;
                stringValue = String.valueOf(game.getSupply(playerOnPlace[place]));
                g2d.drawString(stringValue, curX + (maxSupplyWidth - metrics.stringWidth(stringValue)) / 2,
                        curHeight + (ENDSPIEL_HEIGHT + ENDSPIEL_TEXT_Y_INDENT) / 2);
                curX += maxSupplyWidth + ENDSPIEL_GROUP_INDENT;
            }
            if (numPositionsToShowOnPlace[place] > 2) {
                curX += ENDSPIEL_INDENT;
                g2d.drawImage(tokenImage[playerOnPlace[place]], curX, curHeight, tokenTrueWidth, ENDSPIEL_HEIGHT, null);
                curX += tokenTrueWidth + ENDSPIEL_INDENT;
                stringValue = String.valueOf(game.getNumPowerTokensHouse(playerOnPlace[place]));
                g2d.drawString(stringValue, curX + (maxTokenWidth - metrics.stringWidth(stringValue)) / 2,
                        curHeight + (ENDSPIEL_HEIGHT + ENDSPIEL_TEXT_Y_INDENT) / 2);
                curX += maxTokenWidth + ENDSPIEL_GROUP_INDENT;
            }
            if (numPositionsToShowOnPlace[place] > 3) {
                g2d.drawImage(throneImage, curX, curHeight, throneTrueWidth, ENDSPIEL_HEIGHT, null);
                curX += throneTrueWidth + ENDSPIEL_INDENT;
                stringValue = String.valueOf(1 + game.getInfluenceTrackPlaceForPlayer(0, playerOnPlace[place]));
                g2d.drawString(stringValue, curX + (maxThroneWidth - metrics.stringWidth(stringValue)) / 2,
                        curHeight + (ENDSPIEL_HEIGHT + ENDSPIEL_TEXT_Y_INDENT) / 2);
            }
            curHeight += ENDSPIEL_HEIGHT + ENDSPIEL_INDENT;
        }
    }

    private void fillInfo() {
        Game game = Game.getInstance();
        playerOnPlace = new int[NUM_PLAYER];
        numPositionsToShowOnPlace = new int[NUM_PLAYER];
        long[] valueOfPlayer = new long[NUM_PLAYER];
        for (int player = 0; player < NUM_PLAYER; player++) {
            valueOfPlayer[player] = 5 - game.getInfluenceTrackPlaceForPlayer(0, player) +
                    NUM_PLAYER * (game.getNumPowerTokensHouse(player) + MAX_TOKENS * (game.getSupply(player) +
                            MAX_SUPPLY * (game.getNumFortress(player) + NUM_CASTLES_TO_WIN * game.getVictoryPoints(player))));
        }
        boolean[] isPlayerCounted = new boolean[NUM_PLAYER];
        for (int i = 0; i < NUM_PLAYER; i++) {
            isPlayerCounted[i] = false;
        }
        long curValue;
        int curPlayer;
        for (int place = 0; place < NUM_PLAYER; place++) {
            curValue = -1;
            curPlayer = -1;
            for (int player = 0; player < NUM_PLAYER; player++) {
                if (!isPlayerCounted[player] && valueOfPlayer[player] > curValue) {
                    curValue = valueOfPlayer[player];
                    curPlayer = player;
                }
            }
            playerOnPlace[place] = curPlayer;
            isPlayerCounted[curPlayer] = true;
        }
        int prevPositionsToShow = 0;
        int nextPositionsToShow;
        maxPositionToShow = 0;
        for (int place = 0; place < NUM_PLAYER; place++) {
            int player = playerOnPlace[place];
            if (place < NUM_PLAYER - 1) {
                int nextPlayer = playerOnPlace[place + 1];
                nextPositionsToShow = game.getVictoryPoints(player) != game.getVictoryPoints(nextPlayer) ? 0 :
                        game.getNumFortress(player) != game.getNumFortress(nextPlayer) ? 1 :
                        game.getSupply(player) != game.getSupply(nextPlayer) ? 2 :
                        game.getNumPowerTokensHouse(player) != game.getNumPowerTokensHouse(nextPlayer) ? 3 : 4;
            } else {
                nextPositionsToShow = 0;
            }
            numPositionsToShowOnPlace[place] = Math.max(prevPositionsToShow, nextPositionsToShow);
            if (numPositionsToShowOnPlace[place] > maxPositionToShow) {
                maxPositionToShow = numPositionsToShowOnPlace[place];
            }
            prevPositionsToShow = nextPositionsToShow;
        }

        Graphics2D g2d = throneImage.createGraphics();
        headerFont = new Font("Times New Roman", Font.BOLD, ENDSPIEL_MAIN_TEXT_SIZE);
        commonFont = new Font("Times New Roman", Font.BOLD, ENDSPIEL_TEXT_SIZE);
        FontMetrics metrics = g2d.getFontMetrics(headerFont);
        headerString = "Итог игры: победил " + HOUSE[playerOnPlace[0]] + "!";
        headerWidth = metrics.stringWidth(headerString);
        metrics = g2d.getFontMetrics(commonFont);
        for (int place = 0; place < NUM_PLAYER; place++) {
            int player = playerOnPlace[place];
            if (metrics.stringWidth(String.valueOf(game.getVictoryPoints(player))) > maxVictoryWidth) {
                maxVictoryWidth = metrics.stringWidth(String.valueOf(game.getVictoryPoints(player)));
            }
            if (metrics.stringWidth(String.valueOf(game.getNumFortress(player))) > maxFortressWidth &&
                    numPositionsToShowOnPlace[place] > 0) {
                maxFortressWidth = metrics.stringWidth(String.valueOf(game.getNumFortress(player)));
            }
            if (metrics.stringWidth(String.valueOf(game.getSupply(player))) > maxSupplyWidth &&
                    numPositionsToShowOnPlace[place] > 1) {
                maxSupplyWidth = metrics.stringWidth(String.valueOf(game.getSupply(player)));
            }
            if (metrics.stringWidth(String.valueOf(game.getNumPowerTokensHouse(player))) > maxTokenWidth &&
                    numPositionsToShowOnPlace[place] > 2) {
                maxTokenWidth = metrics.stringWidth(String.valueOf(game.getNumPowerTokensHouse(player)));
            }
            if (metrics.stringWidth(String.valueOf(game.getInfluenceTrackPlaceForPlayer(0, player))) > maxThroneWidth &&
                    numPositionsToShowOnPlace[place] > 3) {
                maxThroneWidth = metrics.stringWidth(String.valueOf(game.getInfluenceTrackPlaceForPlayer(0, player)));
            }
        }

        totalWidth = Math.max(headerWidth, ENDSPIEL_TEXT_WIDTH + emblemTrueWidth + ENDSPIEL_EMBLEM_INDENT +
                victoryTrueWidth + (maxPositionToShow > 0 ? fortressTrueWidth : 0) +
                (maxPositionToShow > 1 ? supplyTrueWidth : 0) + (maxPositionToShow > 2 ? tokenTrueWidth : 0) +
                (maxPositionToShow > 3 ? throneTrueWidth : 0) +
                (maxPositionToShow + 2) * ENDSPIEL_INDENT + (maxPositionToShow + 1) * ENDSPIEL_GROUP_INDENT +
                maxVictoryWidth + maxFortressWidth + maxSupplyWidth + maxTokenWidth + maxThroneWidth);
        totalHeight = ENDSPIEL_TABLE_Y + ENDSPIEL_INDENT + NUM_PLAYER * (ENDSPIEL_HEIGHT + ENDSPIEL_INDENT);
        setPreferredSize(new Dimension(totalWidth, totalHeight));
    }

    private void loadPics() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        fortressImage = imageLoader.getImage(FORTRESS);
        throneImage = imageLoader.getImage(THRONE);
        for (int player = 0; player < NUM_PLAYER; player++) {
            tokenImage[player] = imageLoader.getImage(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + POWER);
            victoryImage[player] = imageLoader.getImage(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + VICTORY);
            supplyImage[player] = imageLoader.getImage(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + SUPPLY);
            emblemImage[player] = imageLoader.getImage(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + EMBLEM);
        }
    }

    private void fillTrueWidths() {
        emblemTrueWidth = (int) (1f * emblemImage[3].getWidth() * ENDSPIEL_HEIGHT / emblemImage[0].getHeight());
        supplyTrueWidth = (int) (1f * supplyImage[0].getWidth() * ENDSPIEL_HEIGHT / supplyImage[0].getHeight());
        victoryTrueWidth = (int) (1f * victoryImage[0].getWidth() * ENDSPIEL_HEIGHT / victoryImage[0].getHeight());
        fortressTrueWidth = (int) (1f * fortressImage.getWidth() * ENDSPIEL_HEIGHT / fortressImage.getHeight());
        tokenTrueWidth = (int) (1f * tokenImage[0].getWidth() * ENDSPIEL_HEIGHT / tokenImage[0].getHeight());
        throneTrueWidth = (int) (1f * throneImage.getWidth() * ENDSPIEL_HEIGHT / throneImage.getHeight());
    }
}
