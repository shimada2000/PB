package mapd.solvers.proto;

import java.util.*;

import mapd.problem.Map;
import mapd.problem.task.*;
import mapd.solvers.Interface.IAgent;
import graph.*;

public abstract class AbstractSolver<AgentType extends IAgent>{
    protected TaskManager taskManager;
    protected Map map;
    protected Random rand;
    protected int agentNum;
    protected List<AgentType> agentList;

    private boolean debug;
    private boolean log;
    private int time;
    private int simLimit;
    private List<AgentType> ordered;

    public abstract String toString();
    protected abstract void generateAgentList();
    public abstract int solve(int time);

    public AbstractSolver(TaskManager _taskManager, Map _map, int _simLimit, String option){
        taskManager = _taskManager;
        map = _map;
        simLimit = _simLimit;

        debug = false;
        log = false;
        switch (option) {
            case "debug":
                debug = true;
                break;
            case "log":
                log = true;
                break;
            default:
                break;
        }
    }

    public void initialize(Random _rand, int _agentNum){
        time = 0;
        rand = _rand;
        agentNum = _agentNum;
        agentList = new ArrayList<>();
        generateAgentList();
        ordered = new ArrayList<>(agentList);
    }

    private boolean isFinished(int time){
        boolean isSolved = taskManager.allAssigned() & agentList.stream().allMatch(a -> !(a.hasTask()));
        boolean isTimeOver = (simLimit < time);
        return isSolved || isTimeOver;
    }

    public void step(){
        for(AgentType agent: agentList){agent.prepare();}
        printMap(time);
        solve(time);
        printAllAgents();
        printLog();
        for(AgentType agent: agentList){
            agent.move();
            agent.transact();
        }
    }

    public int run(){
        while(!isFinished(time)){
            step();
            time++;
        }
        printMap(time);
        printAllAgents();
        return time;
    }

    public Task assignTask(Node node){
        return taskManager.assignTask(node);
    }

    public void printAllAgents(){
        if(!debug) return;
        for(AgentType agent: agentList){
            System.out.println(agent);
        }
    }

    public void printMap(int time){
        if(!debug) return;
        String[] visual = map.getVisual();
        for(AgentType agent: agentList){
            Node node = agent.currentNode();
            visual[node.ID()] = agent.ID();
        }
        map.printVisual(time, visual);
    }

    public void printLog(){
        if(!log) return;
        for(AgentType agent: ordered){
            StringBuffer sb = new StringBuffer(String.format("%s,%s,%s", agent.currentNode(), agent.nextNode(), agent.goal()));
            System.out.println(sb);
        }
    }
}
