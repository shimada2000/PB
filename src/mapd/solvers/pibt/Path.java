package mapd.solvers.pibt;

import util.record.TimeNodePair;

import java.util.*;

public class Path{
    private LinkedList<TimeNodePair> decidedPath;

    public Path(TimeNodePair initPair){
        decidedPath = new LinkedList<>();
        decidedPath.addLast(initPair);
    }

    //about decidedPath

    public void decide(TimeNodePair pair){
        decidedPath.addLast(pair);
    }

    public TimeNodePair getDecidedFirst(){
        return decidedPath.getFirst();
    }

    public TimeNodePair getDecidedLast(){
        return decidedPath.getLast();
    }

    public TimeNodePair removeDecidedFirst(){
        return decidedPath.removeFirst();
    }
    
    public TimeNodePair nextPair(){
        return decidedPath.get(1);
    }

    public String toString(){
        StringBuffer sb = new StringBuffer("[path]: ");
        for(TimeNodePair pair: this.decidedPath){sb.append(pair);}
        return sb.toString();
    }
}