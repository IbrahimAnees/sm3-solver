package solution;

import java.util.*;

public class AStar extends Scheduler {

    protected PriorityQueue<Schedule> schedules;
    protected TreeSet<Schedule> visitedSchedules;
    protected volatile Schedule currentBest;
    protected volatile Schedule currentBestDisplay;
    protected int bestEndTime;
    private int totalScheduleCount = 0;
    private int discarded;


    public int getDiscarded() {
        return discarded;
    }

    public int getTotalScheduleCount() {
        return totalScheduleCount;
    }

    public AStar(Graph graph, int numProcessors) {
        super(graph, numProcessors);
        this.graph = graph;
        this.numOfProcessors = numProcessors;
    }

    /**
     * Executes the A* algorithm on the input graph using the optimisticTimeToSink() method to determine which task to
     * add to the schedule next.
     * @return
     */
    public Schedule aStar() {
        currentBest = generateListSchedule();
        currentBestDisplay = currentBest;
        bestEndTime = currentBest.getCurrentEndTime();

        //priority queue for storing all the schedules
        schedules = new PriorityQueue<>((s1, s2) -> {
            if (s1.getLowerBound() == s2.getLowerBound()) {
                return s2.getTasksCompleted()-s1.getTasksCompleted();
            }
            return s1.getLowerBound() - s2.getLowerBound();
        });

        //Tree set to check visited schedules
        visitedSchedules = new TreeSet<>();

        //generate initial schedules with source nodes
        generateInitialSchedules();


        while(!schedules.isEmpty()) {
            Schedule s = schedules.poll();
            currentBestDisplay = s;

            if (s.getTasksCompleted() == graph.getNumOfTasks()) {
                currentBest = s;
                return s;
            }
            else {
                Queue<Byte> nextValidTasks = s.getNextValidTasks();

                while(!nextValidTasks.isEmpty()) {
                    Task t = ALL_TASKS[nextValidTasks.poll()];
                    for (int i=1; i<=numOfProcessors; i++) {
                        Schedule newSchedule = s.clone();
                        newSchedule.addTask(t, i, ALL_TASKS);

                        if (newSchedule.getLowerBound() < bestEndTime
                               && !visitedSchedules.contains(newSchedule)
                                && !isBetterSwapWithPrevious(newSchedule,i,t)
                                && !isInsertable(newSchedule,t)
                        ) {
                            schedules.add(newSchedule);
                            totalScheduleCount++;
                        }
                        else {
                            discarded++;
                        }

                    }
                }
            }
            visitedSchedules.add(s);
        }

        currentBestDisplay = currentBest;
        return currentBest;
    }

    /**
     * A heuristic method which determines which nodes will be searched first in our A* algorithm.
     * It uses Dijkstra's algorithm starting from the input task and searches for a valid sink task.
     * @param graph     The graph to search.
     * @param task      The task/node to get the heuristic value of.
     * @return          An integer representing an optimistic underestimate of the minimum amount of
     *                  time required to reach and complete a sink task from the input task.
     */
    private static int optimisticTimeToSink(Graph graph, Task task, List<Task> sinkNodes) {
        if (sinkNodes.contains(task)) {
            return 0;
        }

        Stack<Task> stack = new Stack<>();
        stack.add(task);
        task.setDistance(task.getWeight());
        List<Task> validSinkNodes = new ArrayList<>();
        List<Task> seen = new ArrayList<>();

        while (!stack.isEmpty()) {
            Task current = stack.pop();

            for (Edge e : graph.getGraph().get(current)) {
                Task neighbour = e.getDestination();
                if (!seen.contains(neighbour) && sinkNodes.contains(neighbour)) {
                    seen.add(neighbour);
                    neighbour.setDistance(Integer.MAX_VALUE);
                } else if (!seen.contains(neighbour)) {
                    seen.add(neighbour);
                    neighbour.setDistance(neighbour.getWeight());
                }

                stack.add(neighbour);

                if (current.getDistance() + neighbour.getWeight() < neighbour.getDistance()) {
                    neighbour.setDistance(current.getDistance() + neighbour.getWeight());
                }

                if (sinkNodes.contains(neighbour)) {
                    validSinkNodes.add(neighbour);
                }
            }
        }

        int minCost = Integer.MAX_VALUE;
        for (Task s : validSinkNodes) {
            if (s.getDistance() < minCost) {
                minCost = s.getDistance();
            }
        }

        return minCost;
    }

    private static Task getFastestTask(Map<Task, Integer> cheapestToSink, Set<Task> openSet) {
        Task t = null;
        int time = Integer.MAX_VALUE;

        for (Task task : openSet) {
            int taskTime = cheapestToSink.get(task);
            if (taskTime < time) {
                time = taskTime;
                t = task;
            }
        }

        return t;
    }


    /**
     * generates a trial schedule using list scheduling
     */
    protected void generateInitialSchedules() {

        for (byte b : sourceNodes) {
            Schedule s = new Schedule((byte) numOfProcessors, graph, inDegreeMap);
            int[] newDegree = inDegreeMap.clone();
            newDegree[b] = -1;

            Task t = ALL_TASKS[b];
            s.setInDegreeMap(newDegree);

            HashSet<Task> nextAvailableTasks = (HashSet<Task>) sourceNodes.clone();
            nextAvailableTasks.remove(t);

            s.addTask(t, 1, ALL_TASKS);

            s.setEstimatedTime(longestPathMap.get(t));
            s.setLowerBound(longestPathMap.get(t));

            if (s.getLowerBound() < bestEndTime)
                schedules.add(s);

        }
    }


    /**
     * @param schedule the new partial partial schedule from the queue with one more task added
     * @param processor the processor number the task is added onto
     * @param t1 the newly added task
     * @return true if better solutions are found, vice versa
     */
    public boolean isBetterSwapWithPrevious(Schedule schedule, int processor,Task t1) {
        List<Task> singleProcessorTasksMap = schedule.getSingleProcessorTasksMap(processor);

        int mapSize = singleProcessorTasksMap.size();
        if (mapSize<=1)
            return false;
        Task t2 = singleProcessorTasksMap.get(mapSize-2);

        if (t1.getOrder() > t2.getOrder())
            return false;

        int[][] taskSchedule = schedule.getSchedule();

        int processorEndTime = mapSize>=2? taskSchedule[singleProcessorTasksMap.get(mapSize-2).getIndex()][1]:0;

        int t1EndTime = calculateStartTime(t1, processorEndTime, taskSchedule, processor) + t1.getWeight();

        int t2EndTime = calculateStartTime(t2,t1EndTime, taskSchedule, processor) + t2.getWeight();

        int originEndTime = taskSchedule[t1.getIndex()][1]+ t1.getWeight();

        //equivalent schedule exists, then drop the one that has bigger lexicographical order
        if (originEndTime == t2EndTime){
            if (t1.getName().compareTo(t2.getName()) > 0) {
                return false;
            }
        }

        else if((originEndTime < t2EndTime) || isTaskAffected(t2,t2EndTime,taskSchedule))
            return false;

        return true;

    }

    /**
     * @param t1 newly added task
     * @param taskEndTime latest end time of the task
     * @param taskSchedule schedule containing start time and processor for each task
     * @return true, if child is affected by the swap, false if not affected
     */
    public boolean isTaskAffected(Task t1, int taskEndTime, int[][] taskSchedule) {
        for(Edge e: graph.getGraph().get(t1)) {
            int taskStart = taskSchedule[e.getDestination().getIndex()][1];
            if (taskStart == -1)
                return true;
            if (taskStart<(taskEndTime+e.getWeight())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param schedule newly added schedule
     * @param task newly added task
     * @return yes, if the task can be inserted to idle blocks, false otherwise
     */
    public boolean isInsertable(Schedule schedule, Task task) {

        ArrayList<Idle> idles = schedule.getIdles();
        int[][] taskSchedule = schedule.getSchedule();
        int[] middleTaskInfo = taskSchedule[task.getIndex()];
        int startTimeLowerBound = calculateStartTimeLowerBound(task, taskSchedule);

        for (Idle idle : idles) {

            Task topTask = idle.getFrom();
            int idleStartTime;
            if (topTask !=null) {
                idleStartTime = taskSchedule[topTask.getIndex()][1]+topTask.getWeight();
            }
            else {
                idleStartTime = 0;
            }

            //check for correct topological order, start time lower bound and upper bound
            if (!task.getChildren().contains(topTask) && !task.getParents().contains(idle.getTo()) && idleStartTime < middleTaskInfo[1] && idleStartTime >= startTimeLowerBound) {
                int latestStartTimeOnProcessor = calculateStartTime(task,idleStartTime,taskSchedule,idle.getProcessor());

                if (taskSchedule[idle.getTo().getIndex()][1] >= latestStartTimeOnProcessor) {

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @param t1 newly added task
     * @param processorEndTime current latest end time of the processor
     * @param taskSchedule schedule containing start time and processor for each task
     * @param processor processor number
     * @return earliest time the task is allowed to start on the processor
     */
    public int calculateStartTime(Task t1, int processorEndTime, int[][] taskSchedule, int processor) {

        int maxStartTime = 0;
        for(Edge e : graph.getReversedGraph().get(t1)) {
            int currentStart = processorEndTime;
            Task parentTask = e.getDestination();
            int[] parentTaskInfo = taskSchedule[parentTask.getIndex()];
            if (parentTaskInfo[0]!=processor) {
                currentStart = Math.max(processorEndTime,parentTaskInfo[1] + e.getWeight());
            }
            maxStartTime = Math.max(maxStartTime, currentStart);
        }

        return maxStartTime;
    }

    /**
     * @param t newly added task
     * @param taskSchedule schedule containing start time and processor for each task
     * @return earliest start time allowed considering all parent tasks
     */
    public int calculateStartTimeLowerBound(Task t, int[][] taskSchedule) {
        int startTimeLowerBound = 0;

        for (Edge e : graph.getReversedGraph().get(t)) {
            Task parentTask = e.getDestination();
            int parentTaskInfo = taskSchedule[parentTask.getIndex()][1];
            if (parentTaskInfo != -1) {
                int parentTaskEndTime = parentTaskInfo+parentTask.getWeight();

                startTimeLowerBound = Math.max(startTimeLowerBound, parentTaskEndTime);

            }
        }

        return startTimeLowerBound;
    }


    /**
     * update task fields for the final solution
     * @param schedule finished schedule
     */
    public void updateTasks(Schedule schedule) {
        int [][] currentBestSchedule = schedule.getSchedule();
        for (byte i=0; i<currentBestSchedule.length; i++) {
            int[] currentBestScheduleInfo = currentBestSchedule[i];
            ALL_TASKS[i].setStart(currentBestScheduleInfo[1]);
            ALL_TASKS[i].setProcessor(currentBestScheduleInfo[0]);
        }
    }

    public Schedule getCurrentBestDisplay() {
        return currentBestDisplay;
    }

}
