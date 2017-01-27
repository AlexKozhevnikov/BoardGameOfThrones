package com.alexeus.ai.struct;

import com.alexeus.logic.enums.Order;
import com.alexeus.map.GameOfThronesMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by alexeus on 24.01.2017.
 * Данная структура используется примитивными игроками для определения вероятностей приказов определённого типа
 * в областях, где они должны расставить приказы.
 */
public class VotesForOrderInArea {

    HashMap<Integer, HashMap<Order, Float>> voteInAreaForOrder;

    HashMap<Integer, Float> numVotesInArea;

    Random random;

    public VotesForOrderInArea() {
        voteInAreaForOrder = new HashMap<>();
        numVotesInArea = new HashMap<>();
        random = new Random();
    }

    public void addArea(int area) {
        voteInAreaForOrder.put(area, new HashMap<>());
    }

    public void setOrderVotesInArea(int area, Order order, float votes) {
        float prevOrderVotes = voteInAreaForOrder.get(area).containsKey(order) ? voteInAreaForOrder.get(area).get(order) : 0;
        voteInAreaForOrder.get(area).put(order, votes);
        float prevNumVotes = numVotesInArea.containsKey(area) ? numVotesInArea.get(area) : 0;
        numVotesInArea.put(area, prevNumVotes + votes - prevOrderVotes);
    }

    public float getOrderVotesInArea(int area, Order order) {
        return voteInAreaForOrder.get(area).containsKey(order) ? voteInAreaForOrder.get(area).get(order) : 0;
    }

    public float getTotalVotesInArea(int area) {
        if (!numVotesInArea.containsKey(area)) {
            return 0;
        }
        return numVotesInArea.get(area);
    }

    public void removeOrderFromArea(int area, Order order) {
        float votesForRemovedOrder = voteInAreaForOrder.get(area).get(order);
        voteInAreaForOrder.get(area).remove(order);
        numVotesInArea.put(area, numVotesInArea.get(area) - votesForRemovedOrder);
    }

    public void forbidOrder(Order order) {
        for (Map.Entry<Integer, HashMap<Order, Float>> entry: voteInAreaForOrder.entrySet()) {
            if (entry.getValue().containsKey(order)) {
                removeOrderFromArea(entry.getKey(), order);
            }
        }
    }

    public Order giveCommand(int area) {
        if (!numVotesInArea.containsKey(area)) {
            return null;
        }
        if (numVotesInArea.get(area) <= 0) {
            return null;
        }
        float trueVote = random.nextFloat() * numVotesInArea.get(area);
        float curVote = 0;
        for (Map.Entry<Order, Float> entry: voteInAreaForOrder.get(area).entrySet()) {
            curVote += entry.getValue();
            if (curVote >= trueVote) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean firstFlag = true;
        for (Map.Entry<Integer, HashMap<Order, Float>> entry: voteInAreaForOrder.entrySet()) {
            if (!firstFlag) {
                sb.append("; ");
            }
            sb.append(entry.getKey()).append(": ");
            firstFlag = true;
            for (Map.Entry<Order, Float> voteEntry: entry.getValue().entrySet()) {
                if (!firstFlag) {
                    sb.append(", ");
                } else {
                    firstFlag = false;
                }
                sb.append(voteEntry.getKey()).append(" - ").append(voteEntry.getValue());
            }
            sb.append(", sum=").append(numVotesInArea.get(entry.getKey()));
            firstFlag = false;
        }
        return sb.toString();
    }
}
