package mapd.solvers.Interface;

import graph.Node;

/**
 * IAgent
 */
public interface IAgent {
    public String ID();
    public boolean hasTask();
    public boolean requestTask();
    public void prepare();
    public void move();
    public void transact();
    public boolean updateGoal();
    public boolean isArrived();
    public void elapseTime();
    public void confirmDelivery();
    public Node currentNode();
    public Node nextNode();
    public Node goal();
}
