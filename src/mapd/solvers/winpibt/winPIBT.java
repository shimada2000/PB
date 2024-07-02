package mapd.solvers.winpibt;

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

public class winPIBT extends AbstractSolver<Agent>{

    private int windowSize;

    public winPIBT(TaskManager taskManager, Map map, int simLimit, String option, int _windowSize){
        super(taskManager, map, simLimit, option);
        windowSize = _windowSize;
    }

    @Override
    public String toString() {
        return String.format("\"winPIBT(W = %d)\"", windowSize);
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
        int kappa = 0;
        Collections.sort(agentList, (ai, aj) -> (ai.isPrior(aj) ? -1 : 1));
        for(int rank = 0; rank < agentList.size(); rank++){
            Agent ai = agentList.get(rank);
            if(ai.getDecidedLast().time() <= time){
                if(rank == 0){
                    search(ai, (time + windowSize), new HashSet<>());
                }else{
                    search(ai, Math.min(time + windowSize, kappa), new HashSet<>());
                }
            }
            TimeNodePair last_i = ai.getDecidedLast();
            kappa = Math.min(kappa, last_i.time());
            if(rank == 0){
                kappa = last_i.time();
            }
        }
        return kappa;
    }

    private Agent getPotentialBlocker(Agent ai, TimeNodePair planning_i){
        TimeNodePair last_i = ai.getDecidedLast();
        for(Agent a: this.agentList){
            TimeNodePair last_j = a.getDecidedLast();
            if(last_j.time() < last_i.time()){
                if(last_j.node() == planning_i.node()){
                    return a;
                }
            }
        }
        return null;
    }

    private Agent getFacingBlocker(Agent ai, TimeNodePair planning_i, HashSet<Agent> R){
        TimeNodePair last_i = ai.getDecidedLast();
        for(Agent a: this.agentList){
            if(R.contains(a)) continue;
            TimeNodePair last_j = a.getDecidedLast();
            if(last_j.time() == last_i.time()){
                if(last_j.node() == planning_i.node()){
                    return a;
                }
            }
        }
        return null;
    }

    private boolean search(Agent ai, int alpha, HashSet<Agent> R){
        TimeNodePair last_i = ai.getDecidedLast();
        if(last_i.time() >= alpha){
            return true;
        }
        int beta = alpha;
        for(Agent other: (this.agentList)){
            beta = Math.max(beta, other.getPlanningLast().time());
        }
        if(!existValidPath(ai, alpha, beta)){
            ai.stayUntil(alpha);
            return false;
        }
        R.add(ai);
        while(last_i.time() < alpha){
            //alphaに対してplanの長さが十分でない場合に
            if(ai.hasNoPlan()) break;
            TimeNodePair planning_i = ai.getPlanningFirst();
            Agent aj = null;
            while((aj = getPotentialBlocker(ai, planning_i)) != null){
                search(aj, (aj.getDecidedLast()).time() + 1, new HashSet<>(R));
            }
            if((aj = getFacingBlocker(ai, planning_i, R)) != null){
                if(!search(aj, (aj.getDecidedLast().time() + 1), new HashSet<>(R))){
                    ai.revokePlanning();
                    if(!existValidPath(ai, alpha, beta)){
                        ai.stayUntil(alpha);
                        return false;
                    }else{
                        continue;
                    }
                }
            }
            ai.decide();
            last_i = ai.getDecidedLast();
        }
        return true;
    }

    // ゴールに辿り着く経路計画ができるかを判定、ついでに予約
    private boolean existValidPath(Agent ai, int alpha, int beta){
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
            if(beta < record.pair.time()) break;
            if(isDisentangledPath(record)){
                LinkedList<TimeNodePair> fullPath = record.backTrack();
                for(TimeNodePair pair: fullPath){
                    if(pair.time() > alpha) break;
                    ai.plan(pair);
                }
                return true;
            }
            for(TimeNodePair nextPair: record.pair().nextPairs()){
                if(close.contains(nextPair)) continue;
                if(!isDisentangled(record.pair(), nextPair)) continue;
                open.add(new SearchRecord(record, nextPair, map.Astar(nextPair, goal)));
            }
        }
        return false;
    }

    private boolean isDisentangled(TimeNodePair current, TimeNodePair next){
        for(Agent a: agentList){
            if(!a.isIsolated(current, next)) return false;
        }
        return true;
    }

    private boolean isDisentangledPath(SearchRecord record){
        TimeNodePair current = record.pair();
        for(TimeNodePair pair: record.path()){
            if(!isDisentangled(current, pair)) return false;
            current = pair;
        }
        return true;
    }

    private boolean copeStay(Agent ai, TimeNodePair last_i){
        if(isDisentangled(last_i, last_i.stayingPair())){
            ai.plan(last_i.stayingPair());
            return true;
        }
        List<TimeNodePair> list = new ArrayList<>(last_i.adjacentPairs());
        Collections.shuffle(list, rand);
        for(TimeNodePair pair: list){
            if(isDisentangled(last_i, pair)){
                ai.plan(pair);
                return true;
            }
        }
        return false;
    }

    record SearchRecord(SearchRecord parent, TimeNodePair pair, LinkedList<TimeNodePair> path){

        public double cost(TimeNodePair start){
            double cost = start.durationTo(pair) + path.size();
            if(parent.pair() == pair) cost -= 0.01;
            return cost;
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
