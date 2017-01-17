package com.alexeus.graph.tab;

import com.alexeus.logic.Game;
import com.alexeus.logic.struct.BattleInfo;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alexeus on 13.01.2017.
 * Закладка с текущим (или последним) сражением
 */
public class FightTabPanel extends JPanel {

    @Override
    public void paintComponent(Graphics g) {
        Game game = Game.getInstance();
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        BattleInfo battleInfo = game.getBattleInfo();
        // TODO дописать
    }
}
