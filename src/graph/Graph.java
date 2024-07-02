package graph;

import java.util.*;

import util.parser.CsvParser;

public class Graph extends CsvParser{

    private final String wallSymbol;
    private Node[][] nodes;
    
    protected String[] visualMap;
    protected Map<String, List<Node>> specifiedNodeMap;

    protected List<Node> nodeList;

    public Graph(String mapPath, String _wallSymbol){
        super(mapPath, "!");
        wallSymbol = _wallSymbol;
        nodes = new Node[height][width];
        
        visualMap = new String[height * width];
        nodeList = new ArrayList<>();
        specifiedNodeMap = new HashMap<>();
        setNodes();
    }

    private boolean isPassable(int row, int column){
        boolean isPassable = !(csv[row][column].equals(wallSymbol));
        if(isPassable && !specifiedNodeMap.containsKey(csv[row][column])){
            specifiedNodeMap.put(csv[row][column], new ArrayList<>());
        }
        return isPassable;
    }

    private void setNodes(){
        for(int row = 0; row < height; row++){
            for(int column = 0; column < width; column++){
                int nodeID = to1D(row, column);
                visualMap[nodeID] = csv[row][column];
                if(isPassable(row, column)){
                    nodes[row][column] = new Node(nodeID, row, column);
                    nodeList.add(nodes[row][column]);
                    specifiedNodeMap.get(csv[row][column]).add(nodes[row][column]);
                    linkUpLeft(row, column);
                }
            }
        }
    }

    private boolean isAdjacent(int row, int column){
        boolean row_check = ((0 <= row) && (row < height));
        boolean column_check = ((0 <= column) && (column < width));
        return (row_check && column_check && isPassable(row, column));
    }

    private void linkUpLeft(int row, int column){
        if(isAdjacent(row - 1, column)) nodes[row][column].linkUndirectly(nodes[row - 1][column]);
        if(isAdjacent(row, column - 1)) nodes[row][column].linkUndirectly(nodes[row][column - 1]);
    }

    record SearchRecord(SearchRecord parent, Node node, int depth){
        public double cost(Node goal){
            return depth + node.euclidean(goal);
        }

        public Set<Node> adjacents(){
            return node.adjacents();
        }

        //start node not included
        public LinkedList<Node> backTrack(){
            SearchRecord record = this;
            LinkedList<Node> path = new LinkedList<>();
            while(record.parent() != null){
                path.addFirst(record.node());
                record = record.parent();
            }
            return path;
        }
    
        @Override
        public final String toString() {
            return String.format("(%s -> %s, depth = %d)", parent.node(), node, depth);
        }
    }
    
    public LinkedList<Node> Astar(Node start, Node goal){
        LinkedList<Node> path = new LinkedList<>();
        PriorityQueue<SearchRecord> open = new PriorityQueue<>(
            (x, y) -> (x.cost(goal) < y.cost(goal) ? -1 : 1)
        );
        open.add(new SearchRecord(null, start, 0));
        Set<Node> close = new HashSet<>();
        while(!open.isEmpty()){
            SearchRecord record = open.poll();
            close.add(record.node());
            if(record.node() == goal) return record.backTrack();
            for(Node adjacent: record.adjacents()){
                if(!close.contains(adjacent)){
                    open.add(new SearchRecord(record, adjacent, record.depth() + 1));
                }
            }
        }
        return path;
    }

    public List<Node> getNodes(){
        return new ArrayList<>(nodeList);
    }

    public List<Node> getSpecifiedNodes(String symbol){
        if(symbol.equals("?")){
            return new ArrayList<>(nodeList);
        }
        if(!specifiedNodeMap.keySet().contains(symbol)){
            throw new IllegalArgumentException(String.format("Unexpected node symbol given!! : \"%s\"", symbol));
        }
        return new ArrayList<>(specifiedNodeMap.get(symbol));
    }

    public String[] getVisual(){
        String[] visual = new String[height * width];
        for(int i = 0; i < visualMap.length; i++){
            visual[i] = visualMap[i];
        }
        return visual;
    }

    public void printVisual(int time, String[] visual){
        StringBuffer sb = new StringBuffer(String.format("\n===== <t = %d> =====\n", time));
        for(int row = 0; row < height; row++){
            for(int column = 0; column < width; column++){
                sb.append(String.format("%s", visual[row * width + column]));
            }
            sb.append("\n");
        }
        System.out.println(sb);
    }
}