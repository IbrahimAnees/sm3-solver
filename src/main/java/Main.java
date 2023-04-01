import solution.*;
import visualisation.VisualisationLauncher;

public class Main {

    public static void main (String[] args) {
        Graph graph = InputHandler.parseInput(args);

        // Useful parameters
        int numberOfProcessors = InputHandler.numberOfProcessors;
        boolean usingParallelExecution = InputHandler.usingParallelExecution;
        int numberOfCores = InputHandler.numberOfCores;
        boolean usingOnlyVisualisation = InputHandler.usingOnlyVisualization;
        boolean usingVisualisation = InputHandler.usingVisualization;
        String outputFileName = InputHandler.outputFileName;


        if (usingVisualisation && usingParallelExecution) {
            VisualisationLauncher.initiateLaunch(args, usingOnlyVisualisation, true, numberOfCores);
        } else if (usingVisualisation) {
            VisualisationLauncher.initiateLaunch(args, usingOnlyVisualisation, false, 0);
        } else {

/*            long start1 = System.currentTimeMillis();
            OptimisedScheduler b = new OptimisedScheduler(graph, numberOfProcessors);
            b.branchAndBound();
            long finish1 = System.currentTimeMillis();*/



            AStar s;
            Schedule op;

            System.out.println();

            if (usingParallelExecution) {
                long start3 = System.currentTimeMillis();
                s = new AStarParallel(graph, numberOfProcessors, numberOfCores);
                op = s.aStar();
                long finish3 = System.currentTimeMillis();
                System.out.println("A* Parallel time taken: " + (finish3 - start3) + "ms");
                System.out.println("Optimal schedule: "+op.getCurrentEndTime());
            }
            else {
                long start2 = System.currentTimeMillis();
                s = new AStar(graph, numberOfProcessors);
                op = s.aStar();
                long finish2 = System.currentTimeMillis();
                System.out.println("A* time taken: " + (finish2 - start2) + "ms");
                System.out.println("Optimal schedule:" + op.getCurrentEndTime());
            }

            s.updateTasks(op);
            s.outPutGraph(graph);
            System.exit(1);
            //System.out.println("Branch and Bound time taken: " + (finish1 - start1) + "ms");
        }

    }
}
