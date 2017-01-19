package com.alexeus.graph.tab;

import com.alexeus.graph.util.ImageLoader;
import com.alexeus.graph.util.PictureTormentor;
import com.alexeus.logic.Game;
import com.alexeus.logic.constants.MainConstants;
import com.alexeus.logic.enums.HouseCard;
import com.alexeus.logic.enums.UnitType;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static com.alexeus.graph.constants.Constants.*;
import static com.alexeus.logic.constants.MainConstants.*;
import static com.alexeus.logic.constants.MainConstants.HOUSE_ENG;

/**
 * Created by alexeus on 13.01.2017.
 * Закладка с картами домов, оставшимися жетонами и войсками
 */
public class HouseTabPanel extends JPanel {

    private int CARD_WIDTH, CARD_HEIGHT, TRUE_HOLE_IMAGE_HEIGHT, TRUE_TOKEN_WIDTH;

    BufferedImage[][] houseCardImage = new BufferedImage[NUM_PLAYER][NUM_HOUSE_CARDS];
    BufferedImage[][] greyHouseCardImage = new BufferedImage[NUM_PLAYER][NUM_HOUSE_CARDS];
    BufferedImage[][] unitImage = new BufferedImage[NUM_PLAYER][NUM_UNIT_TYPES];
    BufferedImage[] tokenImage = new BufferedImage[NUM_PLAYER];

    public HouseTabPanel() {
        loadPics();
        loadVariables();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Game game = Game.getInstance();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int width = getWidth();
        int height = getHeight();
        int cardHeight = (int) (1f * height / NUM_PLAYER - HOUSE_TEXT_HEIGHT);
        g2d.setColor(HOUSE_BACKGROUND_COLOR);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Liberation Mono", Font.BOLD, 20));
        for (int player = 0; player < NUM_PLAYER; player++) {
            for (int cardIndex = 0; cardIndex < NUM_HOUSE_CARDS; cardIndex++) {
                g2d.drawImage(game.isCardActive(player, cardIndex) ?
                        houseCardImage[player][cardIndex] : greyHouseCardImage[player][cardIndex],
                        (int) (1f * width / NUM_HOUSE_CARDS * cardIndex),
                        (int) (1f * height / NUM_PLAYER * player),
                        (int) (1f * width / NUM_HOUSE_CARDS * (cardIndex + 1) - 1),
                        (int) (1f * height / NUM_PLAYER * player + cardHeight),
                        0, HOUSE_CARD_HEAD_FROM, CARD_WIDTH, HOUSE_CARD_HEAD_TO, null);
                g2d.drawString(String.valueOf(CARD_STRENGTH_ON_INDEX[cardIndex]),
                        (int) (1f * width / NUM_HOUSE_CARDS * cardIndex + TEXT_HOUSE_X_INDENT),
                        (int) (1f * height / NUM_PLAYER * player + TEXT_HOUSE_Y_INDENT));
            }
        }
        g2d.setFont(new Font("Serif", Font.BOLD, 22));
        g2d.setColor(Color.BLACK);
        for (int player = 0; player < NUM_PLAYER; player++) {
            g2d.drawImage(tokenImage[player],
                    width - HOUSE_GROUP_X_INDENT,
                    (int) (1f * height / NUM_PLAYER * player + cardHeight) + 1,
                    TRUE_TOKEN_WIDTH, TRUE_HOLE_IMAGE_HEIGHT, null);
            g2d.drawString(game.getTokensOfPlayer(player) + "/" + game.getMaxTokensOfPlayer(player),
                    width - HOUSE_GROUP_X_INDENT + TRUE_TOKEN_WIDTH + TEXT_HOUSE_UNITS_X_INDENT,
                    (int) (1f * height / NUM_PLAYER * player + cardHeight) + 1 + TEXT_HOUSE_UNITS_Y_INDENT);
            g2d.drawString(MainConstants.HOUSE[player], HOUSE_NAME_X_INDENT,
                    (int) (1f * height / NUM_PLAYER * player + cardHeight) + 1 + TEXT_HOUSE_UNITS_Y_INDENT);
            for (UnitType type: UnitType.values()) {
                //noinspection SuspiciousNameCombination
                g2d.drawImage(unitImage[player][type.getCode()],
                        width - HOUSE_GROUP_X_INDENT * (5 - type.getCode()),
                        (int) (1f * height / NUM_PLAYER * player + cardHeight) + 1,
                        TRUE_HOLE_IMAGE_HEIGHT, TRUE_HOLE_IMAGE_HEIGHT, null);
                g2d.drawString((MAX_NUM_OF_UNITS[type.getCode()] - game.getRestingUnitsOfType(player, type)) + "/" +
                                MAX_NUM_OF_UNITS[type.getCode()],
                        width - HOUSE_GROUP_X_INDENT * (5 - type.getCode()) + TRUE_HOLE_IMAGE_HEIGHT + TEXT_HOUSE_UNITS_X_INDENT,
                        (int) (1f * height / NUM_PLAYER * player + cardHeight) + 1 + TEXT_HOUSE_UNITS_Y_INDENT);
            }
        }
    }

    public void repaintHouse(int player) {
        int width = getWidth();
        int height = getHeight();
        repaint(new Rectangle(0, (int) (1f * height / NUM_PLAYER * player),
                width, (int) (1f * height / NUM_PLAYER)));
    }

    private void loadPics() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        int n = 0;
        for (HouseCard card: HouseCard.values()) {
            if (card.house() < 0) continue;
            int player = n / NUM_HOUSE_CARDS;
            int cardNumber = n % NUM_HOUSE_CARDS;
            String fileName = card.getFileName();
            houseCardImage[player][cardNumber] = imageLoader.getImage(HOUSE_CARD + fileName + PNG);
            greyHouseCardImage[player][cardNumber] = PictureTormentor.getGrayImage(houseCardImage[player][cardNumber]);
            n++;
        }
        for (int player = 0; player < NUM_PLAYER; player++) {
            for (UnitType unitType: UnitType.values()) {
                unitImage[player][unitType.getCode()] = imageLoader.getImage(HOUSE_ENG[player] + "\\" +
                        HOUSE_ENG[player] + "_" + unitType.engName() + PNG);
            }
            tokenImage[player] = imageLoader.getImage(HOUSE_ENG[player] + "\\" + HOUSE_ENG[player] + POWER);
        }
    }

    private void loadVariables() {
        CARD_WIDTH = houseCardImage[0][0].getWidth();
        CARD_HEIGHT = houseCardImage[0][0].getHeight();
        TRUE_HOLE_IMAGE_HEIGHT = HOUSE_TEXT_HEIGHT - 2;
        TRUE_TOKEN_WIDTH = (int) (1f * tokenImage[0].getWidth() / tokenImage[0].getHeight() * TRUE_HOLE_IMAGE_HEIGHT);
    }
}
