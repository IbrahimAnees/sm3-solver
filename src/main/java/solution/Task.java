package solution;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents an indiviudal task object, or a node in the digraph
 */
public class Task {
    private String name;
    private int weight;
    private int start;
    private int end;
    private int processor;
    private int inDegree;
    private int distance = Integer.MAX_VALUE;
    private int outDegree;
    private int idleTime;
    private Byte index;

    private Set<Task> parents;

    private Set<Task> children;

    private int order;

    public Task (String name, int weight) {
        this.name = name;
        this.weight = weight;
        parents = new HashSet<>();
        children = new HashSet<>();
    }

    /**
     * Set the start time of the task in the schedule. ALso sets the corresponding end time depending on the task weight
     * @param start
     */
    public void setStart(int start) {
        this.start = start;
        this.end = start + weight;
    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void incrementInDegree() {
        inDegree++;
    }

    public int getInDegree() {
        return inDegree;
    }
    public void incrementOutDegree () { outDegree++; }
    public int getOutDegree() { return outDegree; }

    public int getProcessor() {
        return processor;
    }

    public void setProcessor(int processor) {
        this.processor = processor;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Set<Task> getChildren() {
        return children;
    }

    public Set<Task> getParents() {
        return parents;
    }

    public void setChildren(Set<Task> children) {
        this.children = children;
    }

    public void setParents(Set<Task> parents) {
        this.parents = parents;
    }

    public void addChildren(Task task) {
        children.add(task);
    }

    public void addParents(Task task) {
        parents.add(task);
    }
}