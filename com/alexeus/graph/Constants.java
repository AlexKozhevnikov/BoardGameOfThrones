package com.alexeus.graph;

import java.awt.*;

/**
 * Created by alexeus on 13.01.2017.
 * Сюда были вынесены все константы, имеющие отношение к отрисовке
 */
public class Constants {
    static final String WAY = "C:\\Users\\Пользователь\\IdeaProjects\\BoardGameOfThrones\\img\\";
    static final String MAP_FILE = "map.jpg";
    static final String TABS = "TabMenuItems\\";
    static final String HOUSE = "card.png";
    static final String EVENT = "event.png";
    static final String CHAT = "chat.png";
    static final String FIGHT = "fight.png";
    static final String PLAY = "play.gif";
    static final String PLAY_END = "playEnd.png";
    static final String COLLAPSE = "collapse.png";
    static final String RETURN = "return.png";
    static final String POWER = "_power.png";
    static final String SUPPLY = "_supply.png";
    static final String INFLUENCE = "_influence.png";
    static final String CARD_BACK = "_back.png";
    static final String VICTORY = "_victory.png";
    static final String TIME = "turn-marker.png";
    static final String DEFENCE = "defence\\";
    static final String ORDER = "order\\";
    static final String PNG = ".png";
    static final String AREA = "area\\";
    static final String WESTEROS = "WesterosCard\\";
    static final String WILDLING_TOKEN = "wildlingToken.png";

    static final Color[] HOUSE_COLOR = {Color.YELLOW, Color.RED, Color.WHITE,
            new Color(255, 128, 0), new Color(0x10, 0x10, 0x10), new Color(0, 180, 0)};

    static final float[][] TOKEN_INDENT_X = {{}, {0}, {-0.4f, 0.4f}, {-0.4f, 0.4f, 0}, {-0.4f, 0.4f, -0.4f, 0.4f},
            {-0.4f, 0.4f, -0.4f, 0.4f, 0f}, {-0.8f, 0, 0.8f, -0.8f, 0, 0.8f}};
    static final float[][] TOKEN_INDENT_Y= {{}, {0}, {0, 0}, {0.3f, 0.3f, -0.4f}, {0.4f, 0.4f, -0.4f, -0.4f},
            {0.4f, 0.4f, -0.4f, -0.4f, 0}, {0.4f, 0.4f, 0.4f, -0.4f, -0.4f, -0.4f}};
    static final float[][] PORT_X_KOEF = {{}, {0.1f}, {-0.1f, 0.5f}, {-0.15f, 0.1f, 0.85f}};
    static final float[][] PORT_Y_KOEF = {{}, {0.1f}, {0.5f, -0.1f}, {0.8f, 0.1f, -0.2f}};
    static final int PORT_SIZE = 134;
}
