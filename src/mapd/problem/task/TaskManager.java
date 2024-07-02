package mapd.problem.task;

import java.util.*;

import graph.Node;
import mapd.problem.Map;

public class TaskManager{
    private Map map;
    private List<String> ruleList;
    private List<Integer> taskNumList;

    private Random rand;
    private LinkedList<Task> taskList;
    private List<Integer> unAssignedID;

    public TaskManager(Map _map, List<String> _ruleList, List<Integer> _taskNumList){
        if(_ruleList.size() != _taskNumList.size()){
            throw new IllegalArgumentException("The number of elements in ruleList and taskNumList do not correspond!");
        }
        map = _map;
        ruleList = _ruleList;
        taskNumList = _taskNumList;
    }

    public void initialize(Random _rand){
        rand = _rand;
        taskList = new LinkedList<>();
        unAssignedID = new ArrayList<>();
        int taskID = 0;
        for(int ix = 0; ix < ruleList.size(); ix++){
            List<String> symbolList = Arrays.asList(ruleList.get(ix).split("\s*->\s*"));
            for(int taskNum = 0; taskNum < taskNumList.get(ix); taskNum++){
                Task task = generateTask(symbolList);
                taskList.add(task);
                unAssignedID.add(taskID);
                taskID++;
            }
        }
    }
    private Task generateTask(List<String> symbolList){
        LinkedList<Node> destList = new LinkedList<>();
        for(String symbol: symbolList){
            List<Node> destinationList = map.getSpecifiedNodes(symbol);
            Node dest = destinationList.remove(rand.nextInt(destinationList.size()));
            while(destList.contains(dest)){
                dest = destinationList.remove(rand.nextInt(destinationList.size()));
            }
            destList.add(dest);
        }
        return new Task(destList);
    }

    public Task assignTask(Node node){
        if(unAssignedID.isEmpty()) return null;
        Collections.shuffle(unAssignedID, rand);
        for(int i: unAssignedID){
            Task task = taskList.get(i);
            if(!node.equals(task.getFirst())){
                unAssignedID.remove((Integer) i);
                return task;
            }
        }
        return null;
    }

    public boolean allAssigned(){
        return unAssignedID.isEmpty();
    }

    public void reset(){
        int taskID = 0;
        unAssignedID = new ArrayList<>();
        for(Task task: taskList){
            task.reset();
            unAssignedID.add(taskID);
            taskID++;
        }
    }

    public void printTask(){
        System.out.println(taskList);
    }
}
