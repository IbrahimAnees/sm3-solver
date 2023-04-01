package solution;

import java.util.*;

/**
 * This class creates an optimal schedule using the branch and bound algorithm
 */
public class OptimisedScheduler extends Scheduler{

    private int lowerBound = Integer.MAX_VALUE;
    private LinkedList<Task> schedule;
    private LinkedList<Integer> endTime;
    private Map<Integer,Integer> processors;
    private Graph graph;
    private int minEndTime = Integer.MAX_VALUE;
    private LinkedList<Task> optimalSchedule;
    private Stack<Integer> lastEndTime = new Stack<>();
    private int idleTime;

    public OptimisedScheduler(Graph graph, int numProcessors) {
        super(graph, numProcessors);
        schedule = new LinkedList<>();
        endTime = new LinkedList<>();
        processors = new HashMap<>();
        this.graph = graph;
        Schedule s = generateListSchedule();
        optimalSchedule = new LinkedList<>();
        for (byte i=0; i<s.getSchedule().length; i++) {
            optimalSchedule.add(ALL_TASKS[i]);
        }

        minEndTime = generateListSchedule().getCurrentEndTime();
    }

    /**
     * Scheduling a task at the earliest possible time given a processor
     * @param processor
     * @param task
     */
    private void addTaskToSchedule(int processor, Task task) {
        int start = processors.get(processor);
        lastEndTime.add(processors.get(processor));

        List<Edge> backArcs = graph.getReversedGraph().get(task);
        if (backArcs != null) {
            int startDifferentThread;
            //calculate start time according to processor numbers
            for(Edge e : graph.getReversedGraph().get(task)) {
                Task t = e.getDestination();
                if (t.getProcessor() != processor) {
                    startDifferentThread = t.getEnd() + e.getWeight();
                    if (startDifferentThread > start)
                        start = startDifferentThread;

                }
            }
        }

        task.setStart(start);
        int taskIdleTime = start - processors.get(processor);
        idleTime += taskIdleTime;
        task.setIdleTime(taskIdleTime);
        task.setProcessor(processor);
        processors.put(processor,task.getEnd());
        endTime.add(task.getEnd());
        schedule.add(task);
    }

    /**
     * Removing a specific node from the schedule
     * @param task
     */
    private void deleteTaskToSchedule(Task task) {
        task.setStart(0);
        endTime.removeLast();

        processors.put(task.getProcessor(), lastEndTime.pop());
        idleTime -= task.getIdleTime();
        task.setIdleTime(0);

        task.setProcessor(0);
        schedule.removeLast();
    }

    public Graph branchAndBound() {
        //start with source nodes
        Map<Task,Integer> inDegreeMap = graph.getInDegreeMap();
        int numberOfProcessors = numOfProcessors;
        LinkedList<Task> nextValidTasks = findSourceNodes(inDegreeMap);
        for (int i=1; i<=numberOfProcessors; i++) {
            processors.put(i,0);
        }

        LinkedList<Task> inplaceTasks = (LinkedList<Task>)nextValidTasks.clone();

        //Iterator list_Iter = inplaceTasks.iterator();
        //recursive function to build the schedule tree
        branchAndBoundUntil(inDegreeMap, nextValidTasks, numberOfProcessors, inplaceTasks);

        return graph;
    }

    private void branchAndBoundUntil(Map<Task,Integer> inDegreeMap, LinkedList<Task> nextValidTasks, int numberOfProcessors, LinkedList<Task> inplaceTasks) {
        if (isExceedLowerBound(numberOfProcessors))
            return;
            if (nextValidTasks.isEmpty()) {
                int scheduleEndTime = Collections.max(endTime);
                if (scheduleEndTime < minEndTime) {
                    minEndTime = scheduleEndTime;
                    optimalSchedule = deepCopySchedule(schedule);
                }
                return;
            }

            for (int t=0; t<nextValidTasks.size(); t++){
                for (int i=1; i<=numberOfProcessors; i++) {

                    addTaskToSchedule(i, nextValidTasks.get(t));


                    Task removedTask = nextValidTasks.remove(t);


                    List<Edge> currentNodeEdges = graph.getGraph().get(removedTask);

                    for(Edge e : currentNodeEdges) {
                        Task nextTask = e.getDestination();
                        int inDegree = inDegreeMap.get(nextTask);
                        inDegree--;
                        if (inDegree == 0)
                            nextValidTasks.add(nextTask);
                        inDegreeMap.put(nextTask,inDegree);
                    }

                    branchAndBoundUntil(inDegreeMap, nextValidTasks, numberOfProcessors, inplaceTasks);

                    for(Edge e : currentNodeEdges) {
                        Task nextTask = e.getDestination();
                        int inDegree = inDegreeMap.get(nextTask);
                        inDegree++;
                        if (inDegree == 1)
                            nextValidTasks.remove(nextTask);
                        inDegreeMap.put(nextTask,inDegree);
                    }
                    nextValidTasks.add(t,removedTask);
                    deleteTaskToSchedule(removedTask);
                }
            }
        return;
    }

    private LinkedList<Task> deepCopySchedule(LinkedList<Task> sampleSchedule) {
        LinkedList<Task> scheduleCopy = new LinkedList<>();

        for (Task t : sampleSchedule) {
            Task taskCopy = new Task(t.getName(), t.getWeight());
            taskCopy.setStart(t.getStart());
            taskCopy.setProcessor(t.getProcessor());
            scheduleCopy.add(taskCopy);
        }

        return scheduleCopy;
    }

    private boolean isExceedLowerBound(int numberOfProcessors) {
        int lowerBound = (graph.getTotalWeight() + idleTime) / numberOfProcessors;

        if (lowerBound < minEndTime || endTime.getLast() < minEndTime)
            return false;

        return true;
    }
}
