package com.alexeus;

import javax.swing.*;

/**
 * Created by alexeus on 25.01.2017.
 * Окно приложения
 */
public class GotFrame extends JFrame {

    private static GotFrame instance;

    private GotFrame() {
        super("Board Game of Thrones");
    }

    public static GotFrame getInstance() {
        if (instance == null) {
            instance = new GotFrame();
        }
        return instance;
    }
}
