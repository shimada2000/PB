package mapd.solvers.proposal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import graph.Node;
import mapd.problem.Map;
import mapd.problem.task.TaskManager;
import mapd.solvers.proto.AbstractSolver;
import util.record.TimeNodePair;

public class Proposal extends AbstractSolver<Agent>{
    private int depthLimit;

    public Proposal(TaskManager taskManager, Map map, int simLimit, String option){
        super(taskManager, map, simLimit, option);
        depthLimit = 20;
    }

    @Override
    public String toString() {
        return "\"proposal\"";
    }

    @Override
    protected void generateAgentList() {
        List<Node> nodes = map.getNodes();
        for(int id = 0; id < agentNum; id++){
            Node spawn = nodes.remove(rand.nextInt(nodes.size()));
            agentList.add(new Agent(id, spawn, this));
        }
    }

    @Override
    public int solve(int time){
        Collections.sort(agentList, (ai, aj) -> (ai.isPrior(aj) ? -1 : 1));
        for(Agent ai: agentList){
            if(ai.getDecidedLast().time() < time + 1){
                ai.revokePlanning();
                search(ai, null, time);
            }
        }
        for(Agent ai: agentList) ai.updatePriority();
        return 0;
    }

    private Agent getBlocker(Agent ai){
        TimeNodePair last_i = ai.getDecidedLast();
        for(Agent a: agentList){
            if(a.priority() == ai.priority()) continue;
            TimeNodePair last_j = a.getDecidedLast();
            if(last_j.time() == last_i.time()){
                if(last_j.node() == ai.getPlanningFirst().node()){
                    return a;
                }
            }
        }
        return null;
    }

    private boolean search(Agent ai, Agent aj, int time){
        if(aj != null) ai.inheritFrom(aj);
        if(ai.hasNoPlan()) existValidPath(ai, aj);
        // System.out.println(ai.ID() +" "+ai.planningString()+" priority: "+ai.priority());
        while(!ai.hasNoPlan()){
            Agent ak = getBlocker(ai);
            if(ak != null){
                ak.revokePlanning();;
                // System.out.println(String.format("PI: %s -> %s", ai.ID(), ak.ID()));
                if(!search(ak, ai, time)){
                    ai.revokePlanning();
                    existValidPath(ai, aj);
                    // System.out.println(ai.ID() +" "+ai.planningString()+" priority: "+ai.priority());
                    continue;
                }
            }
            ai.decide();
            if((aj == null) && !(ai.goal().equals(ai.getDecidedLast().node()))){
                return addPreemptiveNodes(ai);
            }
            ai.revokePlanning();
            return true;
        }
        ai.stay();
        return false;
    }

    private Agent getPreemptiveBlocker(Agent ai, TimeNodePair current, TimeNodePair next){
        for(Agent a: agentList){
            if(ai.priority() <= a.priority()) continue;
            if(a.hasConflict(current, next)) return a;
        }
        return null;
    }

    private boolean addPreemptiveNodes(Agent ai){
        for(TimeNodePair preemption: ai.getPlanningPath()){
            if(ai.getDecidedLast().node() == ai.goal()){
                ai.revokePlanning();
                break;
            }
            if(!(preemption.node().isOP())){
                ai.relinquish(preemption.time());
                break;
            }
            Agent ak;
            if((ak = getPreemptiveBlocker(ai, ai.getDecidedLast(), preemption)) != null){
                ak.relinquish(ai.getDecidedLast().time());

            }
        }
        return true;
    }

    // ゴールに辿り着く経路計画ができるかを判定、ついでに予約
    private boolean existValidPath(Agent ai, Agent aj){
        TimeNodePair last_i = ai.getDecidedLast();
        Node goal = ai.goal();
        LinkedList<TimeNodePair> path = map.Astar(last_i, goal);
        if(path.isEmpty()) return copeStay(ai, last_i);
        Queue<SearchRecord> open = new PriorityQueue<>((x, y) -> (x.cost(last_i) < y.cost(last_i) ? -1 : 1));
        open.add(new SearchRecord(null, last_i, path));
        Set<TimeNodePair> close = new HashSet<>();
        while(!(open.isEmpty())){
            SearchRecord record = open.poll();
            close.add(record.pair());
            // System.out.println(record.path());
            if(depthLimit < last_i.durationTo(record.pair())) break;
            if(isCollisionFreePath(ai, record)){
                LinkedList<TimeNodePair> fullPath = record.backTrack();
                for(TimeNodePair pair: fullPath){
                    ai.plan(pair);
                    if(aj != null) break;
                }
                return true;
            }
            for(TimeNodePair nextPair: record.pair().nextPairs()){
                if(close.contains(nextPair)) continue;
                if(!isCollisionFree(ai, record.pair(), nextPair)) continue;
                open.add(new SearchRecord(record, nextPair, map.Astar(nextPair, goal)));
            }
        }
        return false;
    }

    private boolean isCollisionFree(Agent ai, TimeNodePair current, TimeNodePair next){
        for(Agent a: agentList){
            if(a.priority() < ai.priority()) continue;
            if(a.hasConflict(current, next)) return false;
        }
        return true;
    }

    private boolean isCollisionFreePath(Agent ai, SearchRecord record){
        TimeNodePair current = record.pair();
        for(TimeNodePair pair: record.path()){
            if(!isCollisionFree(ai, current, pair)) return false;
            current = pair;
        }
        return true;
    }

    private boolean copeStay(Agent ai, TimeNodePair last_i){
        if(isCollisionFree(ai, last_i, last_i.stayingPair())){
            ai.plan(last_i.stayingPair());
            return true;
        }
        List<TimeNodePair> list = new ArrayList<>(last_i.adjacentPairs());
        Collections.shuffle(list, rand);
        for(TimeNodePair pair: list){
            if(isCollisionFree(ai, last_i, pair)){
                ai.plan(pair);
                return true;
            }
        }
        return false;
    }

    record SearchRecord(SearchRecord parent, TimeNodePair pair, LinkedList<TimeNodePair> path){
        
        public double cost(TimeNodePair start){
            return start.durationTo(pair) + path.size();
        }
    
        //start node not included
        public LinkedList<TimeNodePair> backTrack(){
            SearchRecord record = this;
            LinkedList<TimeNodePair> route = new LinkedList<>(path);
            while(record.parent() != null){
                route.addFirst(record.pair());
                record = record.parent();
            }
            return route;
        }
    
        @Override
        public final String toString() {
            if(parent == null){
                return String.format("(null -> %s, path = %s)", pair, path);
            }
            return String.format("(%s -> %s, path = %s)", parent.pair(), pair, path);
        }
    }
}
