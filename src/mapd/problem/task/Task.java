package mapd.problem.task;

import java.util.LinkedList;

import graph.Node;

public class Task {

    private final LinkedList<Node> deliveryQueue;
    private int deliveryingID;
    private double deliveryTime;
    private int serviceTime;

    public Task(LinkedList<Node> initQueue){
        deliveryQueue = initQueue;
        deliveryingID = 0;
        deliveryTime = 0;
        serviceTime = 0;
    }

    public Node getFirst(){
        return deliveryQueue.getFirst();
    }

    public Node getDestination(){
        return deliveryQueue.get(deliveryingID);
    }

    public void elapseTime(){
        deliveryTime++;
        serviceTime++;
    }

    public double getTime(){
        return deliveryTime;
    }

    public void confirm(){
        deliveryingID++;
        deliveryTime = 0;
    }

    public boolean isCompleted(){
        return (deliveryingID == deliveryQueue.size());
    }

    public int serviceTime(){
        return serviceTime;
    }

    public void reset(){
        deliveryingID = 0;
        deliveryTime = 0;
        serviceTime = 0;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer("");
        sb.append(deliveryQueue.toString());
        return sb.toString();
    }
}