package com.alexeus.ai.struct;

import com.alexeus.logic.enums.Order;

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
        voteInAreaForOrder.get(area).put(order, votes);
        float prevVotes = numVotesInArea.containsKey(area) ? numVotesInArea.get(area) : 0;
        numVotesInArea.put(area, votes + prevVotes);
    }

    public float getOrderVotesInArea(int area, Order order) {
        return voteInAreaForOrder.get(area).get(order);
    }

    public float getTotalVotesInArea(int area) {
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

    public boolean removeArea(int area) {
        if (voteInAreaForOrder.containsKey(area)) {
            voteInAreaForOrder.remove(area);
            numVotesInArea.remove(area);
            return true;
        } else {
            return false;
        }
    }

    public Order giveCommand(int area) {
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
}
