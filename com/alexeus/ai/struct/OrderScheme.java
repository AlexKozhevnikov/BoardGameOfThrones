package com.alexeus.ai.struct;

import com.alexeus.logic.Game;
import com.alexeus.logic.enums.Order;
import com.alexeus.logic.enums.OrderType;
import com.alexeus.map.GameOfThronesMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexeus on 24.01.2017.
 * Схема приказов игрока. Используется компьютерными игроками на этапе отдачи приказов
 */
public class OrderScheme {

    private HashMap<Integer, Order> orderInArea;

    public OrderScheme() {
        orderInArea = new HashMap<>();
    }

    /**
     * Метод добавляет приказ в схему приказов
     * @param area  номер области
     * @param order приказ
     */
    public void addOrderInArea(int area, Order order) {
        Order modifiedOrder = order;
        switch (order) {
            case raidS:
                modifiedOrder = Order.raid;
                break;
            case marchB:
            case marchS:
                modifiedOrder = Order.march;
                break;
            case defenceS:
                modifiedOrder = Order.defence;
                break;
            case supportS:
                modifiedOrder = Order.support;
                break;
        }
        orderInArea.put(area, modifiedOrder);
    }

    public boolean containsOrder(Order targetOrder) {
        for (Order order: orderInArea.values()) {
            if (order == targetOrder) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Integer> getAreasWithOrder (Order targetOrder) {
        ArrayList<Integer> areas = new ArrayList<>();
        for (Map.Entry<Integer, Order> entry: orderInArea.entrySet()) {
            if (entry.getValue() == targetOrder) {
                areas.add(entry.getKey());
            }
        }
        return areas;
    }

    public int getNumOfOrders(Order targetOrder) {
        int n = 0;
        for (Order order: orderInArea.values()) {
            if (order == targetOrder) {
                n++;
            }
        }
        return n;
    }

    public boolean hasArea(int area) {
        return orderInArea.containsKey(area);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        GameOfThronesMap map = Game.getInstance().getMap();
        sb.append("Схема приказов. ");
        boolean firstFlag = true;
        for (Map.Entry<Integer, Order> entry: orderInArea.entrySet()) {
            if (firstFlag) {
                firstFlag = false;
            } else {
                sb.append(", ");
            }
            sb.append(map.getAreaNameRus(entry.getKey())).append(": ").append(entry.getValue());
        }
        return sb.toString();
    }
}
