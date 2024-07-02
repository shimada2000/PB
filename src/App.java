import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import mapd.problem.Map;
import mapd.problem.Parameter;
import mapd.problem.task.TaskManager;
import mapd.solvers.pibt.PIBT;
import mapd.solvers.proposal.Proposal;
import mapd.solvers.proto.AbstractSolver;
import mapd.solvers.winpibt.winPIBT;

public class App {
    public static void main(String[] args) throws Exception {
        Parameter params = new Parameter(args[0]);
        String option = args[1];
        if(option.equals("experiment")) experiment(params, option);
        else{
            Map map = new Map(params.mapPath(), params.wallSymbol());
            TaskManager taskManager = new TaskManager(map, params.ruleList(), params.taskNumList());
    
            Instance pibt = new Instance(new PIBT(taskManager, map, params.period(), option), taskManager);
            Instance proposal = new Instance(new Proposal(taskManager, map, params.period(), option), taskManager);

            int agentNum = params.agentNumList().get(0);
            Random rand = new Random(params.seed());
            taskManager.initialize(rand);

            // pibt.run(rand, agentNum);
            proposal.run(rand, agentNum);
        }
    }

    public static void experiment(Parameter params, String option){
        Map map = new Map(params.mapPath(), params.wallSymbol());
        TaskManager taskManager = new TaskManager(map, params.ruleList(), params.taskNumList());
        List<Instance> solvers = new ArrayList<>();

        Instance pibt = new Instance(new PIBT(taskManager, map, params.period(), option), taskManager);
        solvers.add(pibt);
        for(int windowSize: params.windowSize()){
            Instance winpibt = new Instance(new winPIBT(taskManager, map, params.period(), option, windowSize), taskManager);
            solvers.add(winpibt);
        }
        Instance proposal = new Instance(new Proposal(taskManager, map, params.period(), option), taskManager);
        solvers.add(proposal);
        
        params.commentedFile();        
        map.commentedFile();

        System.out.println(params.agentNumList());
        for(int agentNum: params.agentNumList()){
            Double [] makespan = new Double[solvers.size()];
            Arrays.fill(makespan, 0.0);
            Random baseRand = new Random(params.seed());
            int iterations = params.iteration();
            for(int itr = 0; itr < iterations; itr++){
                Random rand = new Random(baseRand.nextInt());
                taskManager.initialize(rand);
                for(Instance solver: solvers){
                    makespan[solver.id] += solver.run(rand, agentNum);
                }
            }
            for(int i = 0; i < makespan.length; i++) makespan[i] /= iterations;
            System.out.println(Arrays.asList(makespan) + ",");
        }
    }
}

class Instance{
    static int count = 0;
    public int id;
    
    private AbstractSolver<?> solver;
    private TaskManager taskManager;
    
    public Instance(AbstractSolver<?> _solver, TaskManager _taskManager){
        id = count++;
        solver = _solver;
        taskManager = _taskManager;
    }
    
    public int run(Random rand, int agentNum){
        taskManager.reset();
        solver.initialize(rand, agentNum);
        return solver.run();
    }
}
