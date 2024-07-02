package util.record;

import java.util.HashSet;
import java.util.Set;

import graph.Node;

public record TimeNodePair(int time, Node node) {
    
    public int durationTo(TimeNodePair pair){
        return (pair.time() - time);
    }

    public TimeNodePair stayingPair(){
        return new TimeNodePair(time + 1, node);
    }

    public Set<TimeNodePair> adjacentPairs(){
        Set<TimeNodePair> adjacents = new HashSet<>();
        for(Node adjacent: node.adjacents()){
            adjacents.add(new TimeNodePair(time + 1, adjacent));
        }
        return adjacents;
    }

    public Set<TimeNodePair> nextPairs(){
        Set<TimeNodePair> nextPairs = adjacentPairs();
        nextPairs.add(stayingPair());
        return nextPairs;
    }

    public boolean isOP(){
        return node.isOP();
    }

    @Override
    public final String toString() {
        return String.format("(t = %d, %s), ", time, node.toString());
    }
}
