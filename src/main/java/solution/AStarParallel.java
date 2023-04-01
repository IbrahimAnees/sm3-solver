package solution;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AStarParallel extends AStar{

    private ExecutorService executorService;
    private ThreadWorker[] threadWorker;
    private int threads;

    public AStarParallel(Graph graph, int numProcessors, int numCores) {
        super(graph, numProcessors);
        threads = numCores;

        if(threads < 1){
            throw new RuntimeException("Doesn't need to use parallelisation");
        }
        //Generates the number of threads as requested
        executorService = Executors.newFixedThreadPool(threads);
        threadWorker = new ThreadWorker[threads];
        for(int i = 0; i<threads; i++){
            threadWorker[i] = new ThreadWorker();
        }
    }

    @Override
    public Schedule aStar(){
        //TODO Refactor code in AStar constructor
        //priority queue for storing all the schedules
        schedules = new PriorityQueue<>((s1, s2) -> {
            if (s1.getLowerBound() == s2.getLowerBound()) {
                return s2.getTasksCompleted()-s1.getTasksCompleted();
            }
            return s1.getLowerBound() - s2.getLowerBound();
        });
        visitedSchedules = new TreeSet<>();

        currentBest = generateListSchedule();
        bestEndTime = currentBest.getCurrentEndTime();

        generateInitialSchedules();

        //Implement the logic here

        //Need a loop to expand the first few initial schedules
        //Once the queue has more than 20 potential schedules, then stop.
        while(!schedules.isEmpty() && schedules.size() < threads * 8){
            Schedule s = schedules.poll();
            if(s.getTasksCompleted() == graph.getNumOfTasks()){
                return s;
            }

            //TODO Make this a method in AStar
            //Finds all the possible schedules that can exist.
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
                    }


                }
                visitedSchedules.add(s);

            }

        }

        //Split the tasks for all the threads
        int i = 0;
        while (!schedules.isEmpty()){
            threadWorker[i%threads].addToThreadSchedule(schedules.poll());
            i++;
        }

        try {
            executorService.invokeAll(Arrays.asList(threadWorker));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return currentBest;
    }

    public ThreadWorker[] getThreadWorker() {
        return threadWorker;
    }

    public class ThreadWorker implements Callable<Schedule>{
        private Schedule currentBestDisplay;
        private PriorityQueue<Schedule> threadSchedules = new PriorityQueue<>((s1, s2) -> {
            if (s1.getLowerBound() == s2.getLowerBound()) {
                return s2.getTasksCompleted()-s1.getTasksCompleted();
            }
            return s1.getLowerBound() - s2.getLowerBound();
        });

        public void addToThreadSchedule(Schedule s){
            threadSchedules.add(s);
        }

        public Schedule getCurrentBestDisplay() {
            System.out.println(Arrays.deepToString(currentBest.getSchedule()));
            return currentBestDisplay==null?currentBest:currentBestDisplay;
        }


        @Override
        public Schedule call() {

            while(!threadSchedules.isEmpty()){
                //Expand
                Schedule s = threadSchedules.poll();
                currentBestDisplay = s;

                if(s.getEstimatedTime() >= currentBest.getCurrentEndTime()){
                    currentBestDisplay = currentBest;
                    break;
                }
                if(s.getTasksCompleted() == ALL_TASKS.length && s.getEstimatedTime() < currentBest.getEstimatedTime()){
                    currentBest = s;
                    currentBestDisplay = currentBest;
                }

                Queue<Byte> nextValidTasks = s.getNextValidTasks();
                while(nextValidTasks.size()>0) {
                    Task t = ALL_TASKS[nextValidTasks.poll()];
                    for (int i=1; i<=numOfProcessors; i++) {

                        Schedule newSchedule = s.clone();
                        newSchedule.addTask(t, i, ALL_TASKS);

                        if (newSchedule.getLowerBound() < bestEndTime
                                && !visitedSchedules.contains(newSchedule)
                                && !isBetterSwapWithPrevious(newSchedule,i,t)
                                && !isInsertable(newSchedule,t)
                        ) {
//                            System.out.println("TRUE & ADDED TO THING");
                            threadSchedules.add(newSchedule);
                        }
                    }
                    visitedSchedules.add(s);
                }
            }

            currentBestDisplay = currentBest;
            return null;
        }
    }

}