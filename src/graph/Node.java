package graph;

import java.util.*;

public class Node {
    private final int row, column;
    private final int nodeID;
    private final String description;
    private Set<Node> adjacents;

    public Node(int nodeID, int row, int column){
        this.row = row;
        this.column = column;
        this.nodeID = nodeID;
        description = String.format("%d", nodeID);
        adjacents = new HashSet<>();
    }

    public void addAdjacent(Node node){
        adjacents.add(node);
    }

    public void linkUndirectly(Node node){
        addAdjacent(node);
        node.addAdjacent(this);
    }

    public double Manhattan(Node node){
        double d_row = Math.abs(row - node.row);
        double d_column = Math.abs(column - node.column);
        return d_row + d_column;
    }

    public double euclidean(Node node){
        double d_row = Math.abs(row - node.row);
        double d_column = Math.abs(column - node.column);
        return Math.sqrt(Math.pow(d_row, 2) + Math.pow(d_column, 2));
    }


    public boolean isOP(){
        return (adjacents.size() <= 2);
    }

    @Override
    public String toString() {
        return description;
    }

    public int row(){return row;}
    public int column(){return column;}
    
    public int ID(){return nodeID;}
    public Set<Node> adjacents(){return new HashSet<>(adjacents);}
}