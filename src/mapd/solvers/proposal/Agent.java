package mapd.solvers.proposal;

import graph.Node;
import mapd.problem.task.Task;
import mapd.solvers.Interface.IAgent;
import mapd.solvers.Interface.IPriority;
import mapd.solvers.Interface.IState;
import mapd.solvers.proto.AbstractSolver;
import mapd.solvers.proto.state.*;
import util.record.TimeNodePair;

public class Agent extends Path implements IAgent, IPriority{
    private final String description;
    private final AbstractSolver<Agent> solver;
    private IState state;
    private Task task;
    private Node goal;

    private final double uniquePriority;
    private double totalPriority;

    public Agent(int id, Node spawn, AbstractSolver<Agent> _solver){
        super(new TimeNodePair(0, spawn));
        description = String.format("%d", id);
        state = new OffTaskState();
        solver = _solver;
        task = null;
        goal = null;
        uniquePriority = (double) 1.0 / (id + 1);
        totalPriority = 0;
    }

    @Override
    public String ID(){
        return description;
    }

    @Override
    public boolean hasTask(){
        return (task != null);
    }

    @Override
    public boolean requestTask() {
        task = solver.assignTask(currentNode());
        return updateGoal();
    }

    @Override
    public void prepare(){
        state = state.prepare(this);
        updatePriority();
    }

    @Override
    public void move(){
        removeDecidedFirst();
        state = state.move(this);
    }

    @Override
    public void transact(){
        state = state.transact(this);
    }

    @Override
    public boolean updateGoal(){
        if(hasTask()){
            goal = task.getDestination();
        }else{ 
            goal = null;
        }
        return (goal != null);
    }

    @Override
    public boolean isArrived(){
        return (goal.equals(currentNode()));
    }

    @Override
    public void elapseTime(){
        task.elapseTime();
    }

    public void confirmDelivery(){
        task.confirm();
        if(task.isCompleted()){
            task = null;
        }
    }

    @Override
    public Node currentNode(){
        return (getDecidedFirst()).node();
    }

    @Override
    public Node nextNode() {
        return (super.nextPair()).node();
    }

    @Override
    public Node goal(){
        if(goal == null) return currentNode();
        else return goal;
    }

    @Override
    public double priority() {
        return totalPriority;
    }

    public void inheritFrom(Agent a){
        totalPriority = a.priority();
    }

    @Override
    public boolean isPrior(IPriority a){
        return (a.priority() < totalPriority);
    }

    @Override
    public void updatePriority(){
        totalPriority = uniquePriority;
        if(hasTask()){
            totalPriority += task.getTime();
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(String.format("A%s ", description));
        sb.append(String.format("[priority]: %.2f, ", priority()));
        sb.append(super.toString());
        sb.append(super.planningString());
        sb.append(String.format("[task]: %s, ", task));
        if(hasTask()) sb.append(String.format("[goal]: %s, ", goal()));
        return sb.toString();
    }
}
