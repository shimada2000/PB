package mapd.solvers.winpibt;

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

    public void stayUntil(int time){
        TimeNodePair last = getDecidedLast();
        while(last.time() != time){
            plan(last.stayingPair());
            decide();
            last = getDecidedLast();
        }
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

    public boolean isIsolated(TimeNodePair current, TimeNodePair next){
        if(isBooking(next)) return false;
        if(isSlippingThrough(current, next)) return false;
        for(int time = next.time() + 1; time <= getDecidedLast().time(); time++){
            if(isBooking(new TimeNodePair(time, next.node()))) return false;
        }
        return true;
    }


    //about planningPath

    public void plan(TimeNodePair pair){
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

    //about entirePath

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