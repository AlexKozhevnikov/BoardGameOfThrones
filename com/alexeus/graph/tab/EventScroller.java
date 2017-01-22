package com.alexeus.graph.tab;

import com.alexeus.graph.util.ImageLoader;
import com.alexeus.logic.Game;
import com.alexeus.logic.enums.Deck1Cards;
import com.alexeus.logic.enums.Deck2Cards;
import com.alexeus.logic.enums.Deck3Cards;
import com.alexeus.logic.enums.Happenable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import static com.alexeus.graph.constants.Constants.*;

/**
 * Created by alexeus on 17.01.2017.
 * Одна третья вкладки событий
 */
public class EventScroller extends JPanel {

    private HashMap<Happenable, BufferedImage> deckImages = new HashMap<>();

    private BufferedImage deckClosedImage;

    private int deckNumber;

    public EventScroller(int deckNumber) {
        deckImages = new HashMap<>();
        this.deckNumber = deckNumber;
        loadPics();
        setPreferredSize(new Dimension(getWidth(), EVENT_TEXT_HEIGHT * (1 +
                (deckNumber == 1 ? Deck1Cards.values().length : (deckNumber == 2 ? Deck2Cards.values().length :
                        Deck3Cards.values().length))) + EVENT_CARD_HEIGHT));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int curHeight = 0;
        Game game = Game.getInstance();
        int numRemainingCards;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        Happenable curEvent = game.getEvent(deckNumber);
        g.drawImage(curEvent != null ? deckImages.get(curEvent) : deckClosedImage
                , (getWidth() - EVENT_CARD_WIDTH) / 2, curHeight, EVENT_CARD_WIDTH, EVENT_CARD_HEIGHT, null);
        curHeight += EVENT_CARD_HEIGHT + EVENT_TEXT_HEIGHT;

        g2d.setColor(Color.WHITE);
        for (Happenable h : deckNumber == 1 ? Deck1Cards.values() :
                (deckNumber == 2 ? Deck2Cards.values() : Deck3Cards.values())) {
            numRemainingCards = game.getNumRemainingCards(h);
            if (numRemainingCards > 0) {
                g.drawString(h.getName() + (numRemainingCards > 1 ?
                        ": " + game.getNumRemainingCards(h) : ""), 0, curHeight);
                curHeight += EVENT_TEXT_HEIGHT;
            }
        }
    }

    private void loadPics() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        deckClosedImage = imageLoader.getImage(WESTEROS + deckNumber + PNG);
        for (Happenable h : deckNumber == 1 ? Deck1Cards.values() :
                (deckNumber == 2 ? Deck2Cards.values() : Deck3Cards.values())) {
            deckImages.put(h, imageLoader.getImage(WESTEROS + h + PNG));
        }
    }

    public void updatePreferredSize() {
        int numTextStrings = 0;
        Game game = Game.getInstance();
        for (Happenable h : deckNumber == 3 ? Deck3Cards.values() :
                (deckNumber == 2 ? Deck2Cards.values() : Deck1Cards.values())) {
            if (game.getNumRemainingCards(h) > 0) {
                numTextStrings++;
            }
        }
        setPreferredSize(new Dimension(getWidth(), EVENT_TEXT_HEIGHT * (1 + numTextStrings) + EVENT_CARD_HEIGHT));
    }
}
