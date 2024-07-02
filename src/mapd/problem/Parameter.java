package mapd.problem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import util.parser.ConfigParser;

public class Parameter extends ConfigParser{

    public Parameter(String filePath){
        super(filePath, "#");
    }

    private List<Integer> splitIntegerList(String str){
        return Arrays.asList(str.split("\s*,\s*"))
                .stream()
                .map(i -> Integer.parseInt(i))
                .collect(Collectors.toList());
    }

    public String mapPath(){
        return getConfig("map", "MAP_PATH");
    }

    public String wallSymbol(){
        return getConfig("map", "WALL_SYMBOL");
    }

    public List<Integer> agentNumList(){
        return splitIntegerList(getConfig("agent", "AGENT_NUM"));
    }

    public List<Integer> windowSize(){
        return splitIntegerList(getConfig("agent", "WINDOW_SIZE"));
    }

    public List<String> ruleList(){
        String rule = getConfig("task", "RULE");
        return Arrays.asList(rule.split("\s*,\s*"));
    }

    public List<Integer> taskNumList(){
        return splitIntegerList(getConfig("task", "TASK_NUM"));
    }

    public int iteration(){
        return Integer.parseInt(getConfig("experiment", "ITERATION"));
    }

    public int period(){
        return Integer.parseInt(getConfig("experiment", "PERIOD"));
    }

    public long seed(){
        return Long.parseLong(getConfig("experiment", "SEED"));
    }
}
