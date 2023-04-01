package solution;

import java.util.*;

/**
 * This class houses a valid schedule
 */
public class Schedule implements Comparable<Schedule> {

    private int currentIdleTime;
    private int[][] schedule;
    private int currentEndTime;
    private int lowerBound;
    private int[] processorEndTimes;
    private final byte numProcessors;
    private byte tasksCompleted;
    private Graph graph;
    private int[] inDegreeMap;
    private int estimatedTime;
    ArrayList<Task>[] processorTasksMap;
    ArrayList<Idle> idles;

    public Schedule(byte numProcessors, Graph graph, int[]inDegreeMap) {
        currentIdleTime = 0;
        schedule = new int[graph.getNumOfTasks()][2];
        for (int i=0; i<schedule.length; i++) {
            schedule[i] = new int[]{-1, -1};
        }
        currentEndTime = 0;
        lowerBound = Integer.MAX_VALUE;
        processorEndTimes = new int[numProcessors];
        idles = new ArrayList<>();
        this.numProcessors = numProcessors;
        tasksCompleted = 0;
        this.graph = graph;
        processorTasksMap = new ArrayList[numProcessors];
        this.inDegreeMap = inDegreeMap;
        for (int i=0; i<numProcessors; i++) {
            processorTasksMap[i] = new ArrayList<>();
        }
    }

    public Schedule(int currentIdleTime,
                    int[][] schedule,
                    int currentEndTime,
                    int lowerBound,
                    int[] processorEndTimes,
                    byte numProcessors,
                    byte tasksCompleted,
                    Graph graph, int[] inDegreeMap, int estimatedTime, ArrayList<Task>[] processorTasksMap, ArrayList<Idle> idles) {

        this.currentIdleTime = currentIdleTime;
        this.schedule = schedule;
        this.currentEndTime = currentEndTime;
        this.lowerBound = lowerBound;
        this.processorEndTimes = processorEndTimes;
        this.numProcessors = numProcessors;
        this.tasksCompleted = tasksCompleted;
        this.graph = graph;
        this.inDegreeMap = inDegreeMap;
        this.estimatedTime = estimatedTime;
        this.processorTasksMap = processorTasksMap;
        this.idles = idles;
    }

    public int getCurrentIdleTime() {
        return currentIdleTime;
    }

    public int[][] getSchedule() {
        return schedule;
    }

    public int getCurrentEndTime() {
        return currentEndTime;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int[] getProcessorEndTimes() {
        return processorEndTimes;
    }

    public int getNumProcessors() {
        return numProcessors;
    }

    public byte getTasksCompleted() {
        return tasksCompleted;
    }

    /**
     * Adding a task to the current schedule
     * @param task
     * @param processor
     * @param ALL_TASKS
     */
    public void addTask(Task task, int processor, Task[] ALL_TASKS) {
        int startTime = calculateStartTime(task, processor);
        tasksCompleted++;
        int idleTime = startTime - processorEndTimes[processor-1];
        List<Task> singleProcessorTaskMap = processorTasksMap[processor-1];
        if (idleTime >= graph.getMinWeight()){
            if (singleProcessorTaskMap.size()==0) {
                idles.add(new Idle(idleTime, null, task, processor));
            }
            else {
                idles.add(new Idle(idleTime,singleProcessorTaskMap.get(singleProcessorTaskMap.size()-1),task,processor));
            }
        }

        currentIdleTime += idleTime;

        processorEndTimes[processor-1] = startTime + task.getWeight();
        estimatedTime = Math.max(estimatedTime, startTime + AStar.longestPathMap.get(task));
        lowerBound = Math.max((graph.getTotalWeight() + currentIdleTime) / numProcessors,estimatedTime);

        inDegreeMap[task.getIndex()] = -1;
        for(Edge e: graph.getGraph().get(task)){
            Task to = e.getDestination();
            inDegreeMap[to.getIndex()]--;
        }

        currentEndTime = Math.max(processorEndTimes[processor-1], currentEndTime);

        processorTasksMap[processor-1].add(task);

        int[] taskInfo = schedule[task.getIndex()];

        taskInfo[0] = processor;
        taskInfo[1] = startTime;
        lookFurther(ALL_TASKS);

    }

    /**
     * Scan into the tree graph to update the lower bound once a node has been updated
     * @param ALL_TASKS
     */
    private void lookFurther(Task[] ALL_TASKS) {
        int bestStartTime=Integer.MAX_VALUE;
        for (byte t : getNextValidTasks()) {
            for(int i=1; i<=numProcessors; i++) {
                int startTime = calculateStartTime(ALL_TASKS[t],i);
                if (startTime < bestStartTime) {
                    bestStartTime = startTime;
                }
            }
            lowerBound = Math.max(lowerBound, bestStartTime + AStar.longestPathMap.get(ALL_TASKS[t]));
        }
        estimatedTime = lowerBound;
    }

    /**
     * Calculating the earlier start time a task can be scheduled on a given processor
     * @param task
     * @param processor
     * @return
     */
    public int calculateStartTime(Task task, int processor) {
        int earliest = processorEndTimes[processor-1];
        for (Edge e : graph.getReversedGraph().get(task)) {
            Task parentTask = e.getDestination();
            int[] taskInfo = schedule[parentTask.getIndex()];

            int parentTaskFinishTime = taskInfo[1] +parentTask.getWeight();
            if (taskInfo[0] == processor) {
                earliest = Math.max(earliest, parentTaskFinishTime);
            } else {
                earliest = Math.max(earliest, parentTaskFinishTime + e.getWeight());
            }
        }
        return earliest;
    }

    /**
     * A deep clone of the current schedule
     * @return
     */
    public Schedule clone() {
        int[][] scheduleCopy = new int[schedule.length][];
        for (int i = 0; i < schedule.length; i++) {
            scheduleCopy[i] = Arrays.copyOf(schedule[i], schedule[i].length);
        }
        return new Schedule(currentIdleTime,
                scheduleCopy,
                currentEndTime,
                lowerBound,
                processorEndTimes.clone(),
                numProcessors,
                tasksCompleted,
                graph,
                inDegreeMap.clone(),
                estimatedTime, deepCloneProcessorTaskMap(),
                (ArrayList<Idle>) idles.clone());
    }

    public int[] getInDegreeMap() {
        return inDegreeMap;
    }

    public ArrayList<Task>[] getProcessorTasksMap() {
        return processorTasksMap;
    }

    public void setInDegreeMap(int[] inDegreeMap) {
        this.inDegreeMap = inDegreeMap;
    }


    @Override
    public int compareTo(Schedule o) {

        for (int i = 0; i < schedule.length; i++) {
            int taskStart1 = schedule[i][1];
            if (taskStart1 != -1) {
                int ts = o.getSchedule()[i][1];
                if (taskStart1 != ts)
                    return taskStart1 -ts;
            }
        }
        return 0;
    }

    public int getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(int estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    /**
     * Get a queue of tasks that can be next executed in the schedule. A task is valid if all parent nodes have been
     * completed
     * @return
     */
    public Queue<Byte> getNextValidTasks() {
        Queue<Byte> nextValidTasks = new LinkedList<>();
        for (byte t = 0; t < inDegreeMap.length; t++) {
            if (inDegreeMap[t] == 0) {
                nextValidTasks.add(t);
            }
        }
        return nextValidTasks;
    }

    public void setLowerBound(int lowerBound) {
        this.lowerBound = lowerBound;
    }

    public List<Task> getSingleProcessorTasksMap(int processor) {
        return processorTasksMap[processor-1];
    }

    /**
     * Deep clone of the processor task map
     * @return
     */
    public ArrayList<Task>[] deepCloneProcessorTaskMap() {
        ArrayList<Task>[] clone = new ArrayList[numProcessors];
        for (int i=0; i<numProcessors; i++) {
            clone[i] = new ArrayList<>(processorTasksMap[i]);
        }
        return clone;
    }

    public ArrayList<Idle> getIdles() {
        return idles;
    }
}
