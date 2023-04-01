package solution;

/**
 * This class represents an edge of the digraph, contains both source and destination nodes (tasks) as well as the weight
 * of the edge.
 */
public class Edge {
    private Task source;
    private Task destination;
    private int weight;

    public Edge(Task from, Task to, int weight) {
        this.source = from;
        this.destination = to;
        this.weight = weight;
    }

    public Task getSource() {
        return source;
    }

    public void setSource(Task source) {
        this.source = source;
    }

    public Task getDestination() {
        return destination;
    }

    public void setDestination(Task destination) {
        this.destination = destination;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}