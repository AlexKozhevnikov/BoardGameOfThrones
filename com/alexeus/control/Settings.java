package com.alexeus.control;

/**
 * Created by alexeus on 18.01.2017.
 * Данный класс определяет настройки для игры, которые можно поменять в меню "Настройки".
 */
public class Settings {

    private static Settings instance;

    // Если да, то игра проходит без участия графического интерфейса, а её результаты сохраняются в файл
    // TODO написать этот самый режим
    private boolean passByRegime;

    /**
     * Если да, то включается автоматический ход партии: наблюдателю не нужно нажимать на next, ход будет автоматически
     * совершаться по происшествии timeoutMillis миллисекунд.
     */
    private boolean playRegime;

    /**
     * Количество миллисекунд - задержка в режиме автоматического хода партии
     */
    private int timeoutMillis;

    private Settings() {
        // TODO загрузка настроек по умолчанию из файла
        passByRegime = false;
        playRegime = false;
        timeoutMillis = 5000;
    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public boolean isPassByRegime() {
        return passByRegime;
    }

    public void setPassByRegime(boolean passByRegime) {
        this.passByRegime = passByRegime;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public boolean isPlayRegime() {
        return playRegime;
    }

    public void setPlayRegime(boolean playRegime) {
        this.playRegime = playRegime;
    }
}
