package com.alexeus.graph.constants;

import java.awt.*;

/**
 * Created by alexeus on 13.01.2017.
 * Сюда были вынесены все константы, имеющие отношение к отрисовке
 */
public class Constants {
    public static final String WAY = "C:\\Users\\Пользователь\\IdeaProjects\\BoardGameOfThrones\\img\\";
    public static final String MAP_FILE = "map.jpg";
    public static final String TABS = "TabMenuItems\\";
    public static final String HOUSE = "card.png";
    public static final String EVENT = "event.png";
    public static final String CHAT = "chat.png";
    public static final String FIGHT = "fight.png";
    public static final String PLAY = "play.gif";
    public static final String PLAY_END = "playEnd.png";
    public static final String COLLAPSE = "collapse.png";
    public static final String RETURN = "return.png";
    public static final String POWER = "_power.png";
    public static final String SUPPLY = "_supply.png";
    public static final String INFLUENCE = "_influence.png";
    public static final String CARD_BACK = "_back.png";
    public static final String VICTORY = "_victory.png";
    public static final String TIME = "turn-marker.png";
    public static final String DEFENCE = "defence\\";
    public static final String ORDER = "order\\";
    public static final String PNG = ".png";
    public static final String AREA = "area\\";
    public static final String WESTEROS = "WesterosCard\\";
    public static final String HOUSE_CARD = "HouseCard\\";
    public static final String WILDLING_TOKEN = "wildlingToken.png";

    public static final Color[] HOUSE_COLOR = {Color.YELLOW, Color.RED, Color.WHITE,
            new Color(255, 128, 0), new Color(0x10, 0x10, 0x10), new Color(0, 180, 0)};
    public static final Color HOUSE_BACKGROUND_COLOR = new Color(0x90, 0x90, 0x90);

    public static final float[][] TOKEN_INDENT_X = {{}, {0}, {-0.4f, 0.4f}, {-0.4f, 0.4f, 0}, {-0.4f, 0.4f, -0.4f, 0.4f},
            {-0.4f, 0.4f, -0.4f, 0.4f, 0f}, {-0.8f, 0, 0.8f, -0.8f, 0, 0.8f}};
    public static final float[][] TOKEN_INDENT_Y= {{}, {0}, {0, 0}, {0.35f, 0.35f, -0.35f}, {0.4f, 0.4f, -0.4f, -0.4f},
            {0.4f, 0.4f, -0.4f, -0.4f, 0}, {0.4f, 0.4f, 0.4f, -0.4f, -0.4f, -0.4f}};
    public static final float[][] PORT_X_KOEF = {{}, {0.1f}, {-0.1f, 0.5f}, {-0.15f, 0.1f, 0.85f}};
    public static final float[][] PORT_Y_KOEF = {{}, {0.1f}, {0.5f, -0.1f}, {0.8f, 0.1f, -0.2f}};
    public static final int PORT_SIZE = 134;

    public static final int FRAME_WIDTH = 1360;
    public static final int FRAME_HEIGHT = 670;
    public static final int MAP_WIDTH = 900;
    public static final int MAP_HEIGHT = FRAME_HEIGHT;
    public static final int BUTTON_PANEL_WIDTH = 300;
    public static final int BUTTON_PANEL_HEIGHT = 100;
    public static final int HOUSE_TAB_WIDTH = 650;
    public static final int FIGHT_TAB_WIDTH = 500;
    public static final int OTHER_TAB_WIDTH = 350;
    public static final int TAB_PANEL_HEIGHT = FRAME_HEIGHT - BUTTON_PANEL_HEIGHT + 30;
    public static final int BUTTON_ICON_SIZE = (int) (BUTTON_PANEL_HEIGHT * 0.8);
    public static final int TAB_ICON_SIZE = 60;
    //public static final int COLLAPSE_ICON_SIZE = 100;
    public static final int EVENT_CARD_HEIGHT = TAB_PANEL_HEIGHT / 3 - 24;
    public static final int EVENT_CARD_WIDTH = (int) (EVENT_CARD_HEIGHT * 1.57);
    public static final int EVENT_TEXT_HEIGHT = 15;
    public static final int HOUSE_TEXT_HEIGHT = 23;
    public static final int HOUSE_CARD_HEAD_FROM = 109;
    public static final int HOUSE_CARD_HEAD_TO = 323;
    public static final int TEXT_HOUSE_X_INDENT = 3;
    public static final int TEXT_HOUSE_Y_INDENT = 17;
    public static final int TEXT_HOUSE_UNITS_X_INDENT = 3;
    public static final int TEXT_HOUSE_UNITS_Y_INDENT = 17;
    public static final int HOUSE_GROUP_X_INDENT = 80;
    public static final int HOUSE_NAME_X_INDENT = 10;
}
