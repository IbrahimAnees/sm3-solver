package solution;

import java.util.*;

/**
 * This class represents the graph object given an input digraph. This object consists of a list of tasks and their
 * respective edges.
 */
public class Graph {
    private HashMap<Task, List<Edge>> graph;
    private HashMap<Task,List<Edge>> reversedGraph;
    private int totalWeight;
    private int minWeight;
    private byte currentIndex=0;

    public Graph() {
        this.graph = new HashMap<>();
        this.reversedGraph = new HashMap<>();
        minWeight = Integer.MAX_VALUE;
    }

    public String graphName;

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public HashMap<Task, List<Edge>> getGraph() {
        return graph;
    }

    public HashMap<Task, List<Edge>> getReversedGraph() {
        return reversedGraph;
    }

    public void setGraph(HashMap<Task, List<Edge>> graph) {
        this.graph = graph;
    }

    /**
     * Adding a new task to the graph object
     * @param task
     */
    public void addTask(Task task) {
        task.setIndex(currentIndex);
        currentIndex++;
        minWeight = Math.min(minWeight,task.getWeight());
        totalWeight += task.getWeight();
        this.graph.put(task, new ArrayList<>());
        this.reversedGraph.put(task,new ArrayList<>());
    }

    public void addEdge(Task task, Edge edge) {
        this.graph.get(task).add(edge);
    }

    public void addReversedEdge(Task task, Edge edge) {
        this.reversedGraph.get(task).add(edge);
    }

    /**
     * A recurssive helper function to generate a topological sort of the input graph
     * @param v
     * @param visited
     * @param stack
     */
    void topologicalSortUtil(Task v, Map<Task,Boolean> visited,
                             Stack<Task> stack) {
        // Mark the current node as visited.
        visited.put(v,true);
        Edge i;

        // Recur for all the vertices adjacent
        // to this vertex
        Iterator<Edge> it = graph.get(v).iterator();
        while (it.hasNext()) {
            i = it.next();
            Task taskName = i.getDestination();
            if (!visited.get(taskName))
                topologicalSortUtil(taskName, visited, stack);
        }

        // Push current vertex to stack
        // which stores result
        stack.push(v);
    }

    /**
     * Generates a topological ordering of tasks. This uses the util method above.
     * @return
     */
    public ArrayList<Task> topologicalSort() {
        ArrayList<Task> output = new ArrayList<>();
        Stack<Task> stack = new Stack<>();

        // Mark all the vertices as not visited
        Map<Task,Boolean> visited = new HashMap<>();
        for (Task t : graph.keySet())
            visited.put(t,false);

        // Call the recursive helper
        // function to store
        // Topological Sort starting
        // from all vertices one by one
        for (Task t : graph.keySet())
            if (visited.get(t) == false)
                topologicalSortUtil(t, visited, stack);

        // Print contents of stack
        while (stack.empty() == false) {
//            System.out.print(stack.pop() + " ");
            output.add(stack.pop());
        }

        return output;
    }

    public Task getTaskByName(String name) {
        try {
            return this.graph.keySet().stream().filter(t -> t.getName().equals(name)).findFirst().get();
        }
        catch (NoSuchElementException e) {
            return null;
        }

    }

    public Set<Task> getAllTasks() {
        return graph.keySet();
    }

    /**
     * Produce a hashmap with nodes/tasks as the key and the corresponding in degree number as the values
     * @return
     */
    public HashMap<Task, Integer> getInDegreeMap() {
        HashMap<Task, Integer> inDegreeMap = new HashMap<>();

        for (Task t : getAllTasks()) {
            inDegreeMap.put(t,t.getInDegree());
        }

        return inDegreeMap;
    }

    public int[] getInDegreeIntMap() {
        int[] inDegreeMap = new int[getNumOfTasks()];

        for (Task t : getAllTasks()) {
            inDegreeMap[t.getIndex()] = t.getInDegree();
        }

        return inDegreeMap;
    }

    public Set<Task> getAllTasksReversed() {
        return reversedGraph.keySet();
    }

    /**
     * Produce a hashmap with nodes/tasks as the key and the corresponding out degree number as the values
     * @return
     */
    public Map<Task, Integer> getOutDegreeMap() {
        Map<Task, Integer> outDegreeMap = new HashMap<>();

        for (Task t : getAllTasks()) {
            outDegreeMap.put(t,t.getOutDegree());
        }

        return outDegreeMap;
    }

    public int getNumOfTasks() {
        return graph.size();
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    /**
     * Generate a topolical ordering of the input graph and create a queue of tasks using this
     * @param sourceNodes
     * @param ALL_TASKS
     */
    public void setTopologicalOrder(Set<Byte> sourceNodes, Task[] ALL_TASKS) {
        Queue<Byte> taskQueue = new LinkedList<>();
        for (Byte i : sourceNodes) {
            int nodeOrder = 0;
            taskQueue.add(i);
            while (!taskQueue.isEmpty()) {
                Task task = ALL_TASKS[taskQueue.poll()];
                List<Integer> orders = new ArrayList<>();
                for (Edge e: reversedGraph.get(task)) {
                    Task to = e.getDestination();
                    orders.add(to.getOrder());
                }
                for (Edge e: graph.get(task)) {
                    Task to = e.getDestination();
                    taskQueue.add(to.getIndex());
                }
                orders.add(nodeOrder);
                task.setOrder(Collections.max(orders)+1);

            }
        }
    }

    public int getMinWeight() {
        return minWeight;
    }

    public void incrementTotalWeight(int increment) {
        totalWeight+=increment;
    }
}