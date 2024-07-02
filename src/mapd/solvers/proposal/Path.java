package mapd.solvers.proposal;

import util.record.TimeNodePair;

import java.util.*;

public class Path{
    private LinkedList<TimeNodePair> decidedPath;
    private LinkedList<TimeNodePair> planningPath;
    private LinkedList<TimeNodePair> entirePath;

    public Path(TimeNodePair initPair){
        decidedPath = new LinkedList<>();
        planningPath = new LinkedList<>();
        decidedPath.addLast(initPair);
        entirePath = new LinkedList<>(decidedPath);
    }

    public void stay(){
        plan(getDecidedLast().stayingPair());
        decide();
    }

    //about decidedPath

    public void decide(){
        TimeNodePair planningHead = planningPath.removeFirst();
        decidedPath.addLast(planningHead);
    }

    public TimeNodePair getDecidedFirst(){
        return decidedPath.getFirst();
    }

    public TimeNodePair getDecidedLast(){
        return decidedPath.getLast();
    }

    public TimeNodePair removeDecidedFirst(){
        decidedPath.removeFirst();
        return entirePath.removeFirst();
    }

    public TimeNodePair nextPair(){
        return decidedPath.get(1);
    }

    //about planningPath

    public void plan(TimeNodePair pair){
        if(pair.time() != getPlanningLast().time() + 1){
            System.err.println("Planning error!!");
        }
        planningPath.addLast(pair);
        entirePath.addLast(pair);
    }

    public TimeNodePair getPlanningFirst(){
        return planningPath.getFirst();
    }

    public TimeNodePair getPlanningLast(){
        return entirePath.getLast();
    }

    public void revokePlanning(){
        planningPath = new LinkedList<>();
        entirePath = new LinkedList<>(decidedPath);
    }

    public boolean hasNoPlan(){
        return planningPath.isEmpty();
    }

    // removePlanningPath from time ~~
    public void relinquish(int time){
        while(true){
            if(getPlanningLast().time() < time) break;
            planningPath.removeLast();
            entirePath.removeLast();
        }
    }

    public LinkedList<TimeNodePair> getPlanningPath(){
        return new LinkedList<>(planningPath);
    }

    //about entirePath

    public boolean isBooking(TimeNodePair pair){
        return entirePath.contains(pair);
    }

    public boolean isSlippingThrough(TimeNodePair current, TimeNodePair next){
        boolean before = isBooking(new TimeNodePair(current.time(), next.node()));
        boolean after = isBooking(new TimeNodePair(next.time(), current.node()));
        return (before && after);
    }

    public boolean hasConflict(TimeNodePair current, TimeNodePair next){
        return (isSlippingThrough(current, next) || isBooking(next));
    }

    public String planningString(){
        StringBuffer sb = new StringBuffer("<planning>: ");
        for(TimeNodePair pair: this.planningPath){sb.append(pair);}
        return sb.toString();
    }

    public String toString(){
        StringBuffer sb = new StringBuffer("[path]: ");
        for(TimeNodePair pair: this.decidedPath){sb.append(pair);}
        return sb.toString();
    }
}