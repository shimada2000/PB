package mapd.problem;

import java.util.HashMap;
import java.util.LinkedList;

import graph.Graph;
import graph.Node;
import util.record.TimeNodePair;

public class Map extends Graph{

    private java.util.Map<Node, java.util.Map<Node, LinkedList<Node>>> map;

    public Map(String mapPath, String wallSymbol){
        super(mapPath, wallSymbol);
        map = new HashMap<>();
    }

    private LinkedList<Node> getPath(Node start, Node goal){
        if(map.containsKey(start)){
            if(!map.get(start).containsKey(goal)){
                (map.get(start)).put(goal, Astar(start, goal));
            }
        }else{
            map.put(start, new HashMap<>());
            (map.get(start)).put(goal, Astar(start, goal));
        }
        return (map.get(start)).get(goal);
    }

    public LinkedList<TimeNodePair> Astar(TimeNodePair start, Node goal){
        LinkedList<Node> path = getPath(start.node(), goal);
        LinkedList<TimeNodePair> pairPath = new LinkedList<>();
        int time = start.time() + 1;
        for(Node node: path){
            pairPath.addLast(new TimeNodePair(time, node));
            time++;
        }
        return pairPath;
    }

    public int AstarNum(){
        int num = 0;
        for(Node start: map.keySet()){
            num += (map.get(start)).size();
        }
        return num;
    }
}
