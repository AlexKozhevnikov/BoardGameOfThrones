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
    private PlayRegimeType playRegime;

    /**
     * Если да, то левая панель автоматически будет переключать вкладки при игре
     * (начался бой -> вкладка "Бой", новые события -> вкладка "События", и т. д.)
     */
    private boolean autoSwitchTabs;

    /**
     * Количество миллисекунд - задержка в режиме автоматического хода партии
     */
    private int timeoutMillis;

    private Settings() {
        // TODO загрузка настроек по умолчанию из файла
        playRegime = PlayRegimeType.none;
        autoSwitchTabs = true;
        timeoutMillis = 2000;
    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public boolean isTrueAutoSwitchTabs() {
        return autoSwitchTabs && playRegime != PlayRegimeType.playEnd;
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

    public PlayRegimeType getPlayRegime() {
        return playRegime;
    }

    public void setPlayRegime(PlayRegimeType playRegime) {
        this.playRegime = playRegime;
    }

    public boolean isAutoSwitchTabs() {
        return autoSwitchTabs;
    }

    public void setAutoSwitchTabs(boolean autoSwitchTabs) {
        this.autoSwitchTabs = autoSwitchTabs;
    }
}
