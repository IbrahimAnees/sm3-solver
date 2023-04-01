package solution;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * This class implements the functionality of the trivial scheduler
 */
public class Scheduler {

    protected Queue<Schedule> schedules;
    protected volatile Schedule currentBest;
    protected int numOfProcessors;
    protected Graph graph;
    static Map<Task,Integer> longestPathMap;
    static Map<Task,Integer> longestPathMapWithEdges;
    protected final Task[] ALL_TASKS;
    protected int[] inDegreeMap;
    protected HashSet<Byte> sourceNodes;


    public Scheduler(Graph graph, int numProcessors) {
        this.graph = graph;
        this.numOfProcessors = numProcessors;
        longestPathMap = generateLongestPathMap();
        longestPathMapWithEdges = generateLongestPathWithEdgeMap();
        inDegreeMap = graph.getInDegreeIntMap();
        sourceNodes = findSourceNodesSet(inDegreeMap);

        ALL_TASKS = new Task[graph.getNumOfTasks()];
        for (Task t: graph.getAllTasks()) {
            ALL_TASKS[t.getIndex()] = t;
        }

        graph.setTopologicalOrder(sourceNodes, ALL_TASKS);
    }

    /**
     * This method implements a basic run of the alogrithm, giving a valid output schedule
     * @param graph
     */
    public static void runTrivialAlgorithm(Graph graph) {
        Map<Task, Integer> indegreeGraph = graph.getInDegreeMap();

        LinkedList<Task> soureNodes = findSourceNodes(indegreeGraph);

        HashSet<Task> visited = new HashSet<>();

        List<Task> queue = new LinkedList<>();

        //add all source nodes to the beginning of the schedule
        for (Task currentTask : soureNodes) {
            queue.add(currentTask);
        }

        int start = 0; //start time counter

        ArrayList<String> output = new ArrayList<>();

        while (!queue.isEmpty()) {
            Task node = queue.get(0);
            visited.add(node);

            output.add(node.getName() + " [Weight = " + node.getWeight() + ", Start = " + start + ", Processor = " + 1 + "]");

            //get all outgoing edges
            for (Edge currentEdge : graph.getGraph().get(node)) {

                Task adjacentTask = currentEdge.getDestination();

                List<Edge> ingoingEdges = graph.getReversedGraph().get(adjacentTask);

                int counter = 0;

                //check all ingoing edges are already completed or scheduled
                for (Edge edge : ingoingEdges) {
                    Task currentTask = edge.getDestination();
                    if (queue.contains(currentTask) || visited.contains(currentTask)) {
                        counter++;
                    }
                }

                //if all previous tasks are scheduled or completed
                if (counter == ingoingEdges.size()) {
                    if (!queue.contains(adjacentTask) && !visited.contains(adjacentTask)) {
                        //add to the queue
                        queue.add(adjacentTask);
                    }
                }
            }
            start = start + node.getWeight();
            queue.remove(0);
        }

        printEdges(graph, output);
        writeDot(output, graph.getGraphName());
    }

    /**
     * This method returns a valid list schedule for the corresponding input graph. The tasks are scheduled on a
     * specified number of processes.
     */
    public Schedule generateListSchedule() {
        Schedule schedule = new Schedule((byte) numOfProcessors, graph, inDegreeMap);

        Queue<Byte> tasksToSchedule = new PriorityQueue<>((t1, t2) -> longestPathMapWithEdges.get(ALL_TASKS[t2]) - longestPathMapWithEdges.get(ALL_TASKS[t1]));
        for (byte t : schedule.getNextValidTasks()) {
            tasksToSchedule.add(t);
        }

        int[] s1 = inDegreeMap.clone();

        schedule.setInDegreeMap(s1);


        while (tasksToSchedule.size() > 0) {
            Task t = ALL_TASKS[tasksToSchedule.poll()];

            int earliestStartTime = Integer.MAX_VALUE;
            int earliestStartProcessor=0;
            for (int i = 1; i <= numOfProcessors; i++) {
                int start = schedule.calculateStartTime(t,i);

                if (start < earliestStartTime) {
                    earliestStartTime = start;
                    earliestStartProcessor = i;
                }

            }

            schedule.addTask(t, earliestStartProcessor, ALL_TASKS);

            for (Edge e : graph.getGraph().get(t)) {
                Task to = e.getDestination();
                if (schedule.getInDegreeMap()[to.getIndex()] == 0) {
                    tasksToSchedule.add(to.getIndex());
                }
            }
        }

        return schedule;
    }

    /**
     * This method identifies the earliest start time of a task. The begin a task all parent tasks must be already finished.
     * @param task
     * @param processor
     * @param processorTimes
     * @param prevProcessingUnit
     * @param taskEndTimes
     * @return
     */
    protected int findEarliestStartTime(Task task, int processor, int[] processorTimes, HashMap<Integer,
            List<Task>> prevProcessingUnit, HashMap<Task, Integer> taskEndTimes) {
        int earliestTime = Integer.MAX_VALUE;

        if (graph.getReversedGraph().get(task).size() == 0 && processorTimes[processor] == 0) {
            return 0;
        }

        int lastestParentFinish = Integer.MIN_VALUE;
        for (Edge edge : graph.getReversedGraph().get(task)) {
            Task parent = edge.getDestination();
            lastestParentFinish = Math.max(lastestParentFinish, taskEndTimes.get(parent));
        }

        for (Edge edge : graph.getReversedGraph().get(task)) {

            Task parent = edge.getDestination();
            //if parent is in the same processor
            if (prevProcessingUnit.get(processor).contains(parent)) {
                int time = processorTimes[processor];
                earliestTime = Math.min(earliestTime, Math.max(time, lastestParentFinish));
            } else {

                int time = taskEndTimes.get(parent) + edge.getWeight();
                int processorTime = Math.max(processorTimes[processor], lastestParentFinish);

                earliestTime = Math.min(earliestTime, Math.max(time, processorTime));
            }
        }
        return earliestTime;
    }

    /**
     * Method that identifies all source nodes (nodes with no in degree) given a graph
     * @param inDegreeMap
     * @return List of all source nodes
     */
    protected static LinkedList<Task> findSourceNodes(Map<Task, Integer> inDegreeMap) {
        LinkedList<Task> sourceNodes = new LinkedList<>();

        for (Task node : inDegreeMap.keySet()) {
            if (inDegreeMap.get(node) == 0)
                sourceNodes.add(node);
        }

        return sourceNodes;
    }

    /**
     * Generate all source nodes given the graph. Source nodes are defined as nodes without any in degree edges.
     * @param inDegreeMap
     * @return
     */
    protected static HashSet<Byte> findSourceNodesSet(int[] inDegreeMap) {
        HashSet<Byte> sourceNodes = new HashSet<>();

        for (byte i=0; i<inDegreeMap.length; i++) {
            if (inDegreeMap[i] == 0)
                sourceNodes.add(i);
        }

        return sourceNodes;
    }

    /**
     * Helper method to print out all edges to the output
     * @param graph
     * @param output
     */
    private static void printEdges(Graph graph, ArrayList<String> output) {
        for (List<Edge> edges : graph.getGraph().values()) {
            for (Edge edge : edges) {
                output.add(edge.getSource().getName() + " -> " + edge.getDestination().getName() + " [Weight = " + edge.getWeight() + "]");
            }
        }
    }

    /**
     * Method to create output dot file and write to it
     * @param output
     * @param name
     */
    static void writeDot(ArrayList<String> output, String name){
        try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name)))){
            out.write("digraph \"" + name +"\" {");
            out.newLine();
            for(String line : output){
                out.write(line);
                out.newLine();
            }
            out.write("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Given an input outdegree map, will return a list of tasks that are sink nodes, i.e., tasks that have
     * an outdegree of zero and therefore lead to no other tasks. The return list is used to
     * check when a search should end since all sink nodes have been reached.
     * @param outDegreeMap  a map whose keys are tasks and values the number of tasks that this task has an edge to
     * @return a list of tasks that are sink nodes
     */
    protected static LinkedList<Task> findSinkNodes(Map<Task, Integer> outDegreeMap) {
        LinkedList<Task> sinkNodes = new LinkedList<>();

        for (Task node : outDegreeMap.keySet()) {
            if (outDegreeMap.get(node) == 0)
                sinkNodes.add(node);
        }

        return sinkNodes;
    }

    /**
     * This method generates the longest distance needed for each given node to traverse its way to the sink node
     * @return
     */
    protected Map<Task,Integer> generateLongestPathMap() {
        Map<Task, Integer> longestPathMap = new HashMap<>();

        for (Task t: graph.getGraph().keySet()) {
            if (graph.getGraph().get(t).size() ==0) {
                longestPathMap.put(t,t.getWeight());
            }
            Queue<Task> tasks = new LinkedList<>();
            tasks.add(t);

            while (!tasks.isEmpty()) {
                Task currentTask = tasks.poll();

                for (Edge backArc : graph.getReversedGraph().get(currentTask)) {
                    Task previousTask = backArc.getDestination();
                    Integer previousTaskCost = longestPathMap.get(previousTask);
                    previousTaskCost = previousTaskCost == null? 0:previousTaskCost;
                    Integer currentTaskCost = longestPathMap.get(currentTask);
                    currentTaskCost = currentTaskCost == null? 0:currentTaskCost;
                    Integer previousTaskWeight = previousTask.getWeight();

                    longestPathMap.put(previousTask, Math.max(previousTaskCost, currentTaskCost + previousTaskWeight));
                    tasks.add(previousTask);
                }
            }
        }

        return longestPathMap;
    }

    /**
     * This method generates the longest distance needed for each given node to traverse its way to the sink node. This
     * method takes into account weights of each edge.
     * @return
     */
    protected Map<Task,Integer> generateLongestPathWithEdgeMap() {
        Map<Task, Integer> longestPathMap = new HashMap<>();

        for (Task t: graph.getGraph().keySet()) {
            if (graph.getGraph().get(t).size() ==0) {
                longestPathMap.put(t,t.getWeight());
            }
            Queue<Task> tasks = new LinkedList<>();
            tasks.add(t);

            while (!tasks.isEmpty()) {
                Task currentTask = tasks.poll();

                for (Edge backArc : graph.getReversedGraph().get(currentTask)) {
                    Task previousTask = backArc.getDestination();
                    Integer previousTaskCost = longestPathMap.get(previousTask);
                    previousTaskCost = previousTaskCost == null? 0:previousTaskCost;
                    Integer currentTaskCost = longestPathMap.get(currentTask);
                    currentTaskCost = currentTaskCost == null? 0:currentTaskCost;
                    Integer previousTaskWeight = previousTask.getWeight();

                    longestPathMap.put(previousTask, Math.max(previousTaskCost, currentTaskCost + previousTaskWeight + backArc.getWeight()));
                    tasks.add(previousTask);
                }
            }
        }

        return longestPathMap;
    }

    /**
     * Writing the schedule to the output dot file
     * @param graph
     */
    public void outPutGraph(Graph graph) {
        ArrayList<String> output = new ArrayList<>();
        for (Task t: graph.getAllTasks()) {
            output.add(t.getName() + " [Weight = " + t.getWeight() + ", Start = " + t.getStart() + ", Processor = " + t.getProcessor() + "];");
        }
        for (Task t: graph.getAllTasks()) {
            for (Edge e : graph.getGraph().get(t)){
                output.add(e.getSource().getName() + " -> " + e.getDestination().getName() + " [Weight = " + e.getWeight() + "];");
            }
        }
        writeDot(output, graph.getGraphName());
    }

    public Graph getGraph() {
        return graph;
    }

    public Schedule getCurrentBestDisplay() {
        return currentBest;
    }

    public int getTotalScheduleCount() {

        return 0;
    }

    public int getDiscarded() {
        return 0;
    }
}