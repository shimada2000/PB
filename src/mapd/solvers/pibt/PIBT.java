package mapd.solvers.pibt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import graph.Node;
import mapd.problem.Map;
import mapd.problem.task.TaskManager;
import mapd.solvers.proto.AbstractSolver;
import util.record.TimeNodePair;

public class PIBT extends AbstractSolver<Agent>{

    private List<Agent> undecided;
    private Set<Node> occupied;

    public PIBT(TaskManager taskManager, Map map, int simLimit, String option){
        super(taskManager, map, simLimit, option);
    }

    @Override
    public String toString() {
        return "\"PIBT\"";
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
    public int solve(int time) {
        Collections.sort(agentList, (ai, aj) -> (ai.isPrior(aj) ? -1 : 1));
        undecided = new ArrayList<>(agentList);
        occupied = new HashSet<>();
        while(!undecided.isEmpty()){
            search(undecided.get(0), null);
        }
        return 0;
    }

    private PriorityQueue<TimeNodePair> getCandidate(Agent ai, Agent aj){
        Node goal = ai.goal();
        Node ban = null;
        if(aj != null) ban = aj.getDecidedLast().node();
        Set<TimeNodePair> nextPairs = (ai.getDecidedLast()).nextPairs();
        PriorityQueue<TimeNodePair> candidate = new PriorityQueue<>((x, y) -> (map.Astar(x, goal).size() < map.Astar(y, goal).size() ? -1 : 1));
        for(TimeNodePair pair: nextPairs){
            if((occupied.contains(pair.node()) || pair.node().equals(ban))){
                continue;
            }
            candidate.add(pair);
        }
        return candidate;
    }

    private Agent getBlocker(Node next){
        for(Agent a: undecided){
            if(next.equals(a.getDecidedLast().node())) return a;
        }
        return null;
    }

    private boolean search(Agent ai, Agent aj){
        undecided.remove(ai);
        PriorityQueue<TimeNodePair> candidate = getCandidate(ai, aj);
        while(!candidate.isEmpty()){
            TimeNodePair pair = candidate.poll();
            occupied.add(pair.node());
            Agent ak = null;
            if((ak = getBlocker(pair.node())) != null){
                // System.out.println(String.format("PI: %s -> %s", ai.ID(), ak.ID()));
                if(search(ak, ai)){
                    ai.decide(pair);
                    return true;
                }
            }else{
                ai.decide(pair);
                return true;
            }
        }
        ai.decide(ai.getDecidedLast().stayingPair());
        return false;
    }
}
