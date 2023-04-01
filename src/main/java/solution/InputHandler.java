package solution;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * This class takes in the input arguments and stores them in the respective fields. If invalid arguments have been entered,
 * an exception will be thrown
 */
public class InputHandler {

    public static String[] args;
    private static boolean validInput;
    public static Graph inputGraph;
    private static boolean validFileName;
    public static String fileName;
    private static boolean validProcessors;
    public static String outputFileName;
    private static boolean validOptionalArguments;
    public static int numberOfProcessors;
    public static boolean usingParallelExecution = false;
    public static int numberOfCores = 0;
    public static boolean usingVisualization = false;
    public static boolean usingOnlyVisualization = false;

    public static boolean usingCustomOutputName = false;
    public static ArrayList<String> terminalInput;
    public static String absoluteFilePath = null;

    /**
     * Checking each field in the input arguments
     * @param input
     * @return
     */
    public static Graph parseInput(String[] input) {
        args = input;
        usingOnlyVisualization = checkForOnlyVisualization();
        if (usingOnlyVisualization) {
            return new Graph();
        }
        validInput = checkNumberOfArguments();
        if (validInput) {
            validFileName = checkFileName();
            if (validFileName) {
                fileName = getFileName();
                outputFileName = getDefaultOutputFileName();
                validProcessors = checkNumberOfProcessors();
                if (validProcessors) {
                    numberOfProcessors = getNumberOfProcessors();
                    validOptionalArguments = checkAndSetOptionalArguments();
                    if (validOptionalArguments) {
                        printHeaders();
                        printInputGraph();
                        Graph graph = new Graph();
                        graph = buildGraph();
                        inputGraph = graph;
                        return graph;
                    } else {
                        throw new IllegalArgumentException();
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static boolean checkForOnlyVisualization() {
        if (args.length == 1 && args[0].toLowerCase().equals("-v")) {
            usingVisualization = true;
            return true;
        }
        return false;
    }

    private static boolean checkNumberOfArguments() {
        if (args.length < 2) {
            System.err.println("INVALID INPUT: Please provide at least the file name and number of processors!");
            return false;
        } else if (args.length > 7) {
            System.err.println("INVALID INPUT: Too many arguments!");
            return false;
        }
        else {
            return true;
        }
    }

    private static boolean checkFileName() {
        String fileName = args[0];
        //boolean dotFile;
        boolean fileExists;


        // Checking if file is a .dot file
//        if (fileName.length() <= 4) {
//            dotFile = false;
//        } else {
//            if (!fileName.substring(fileName.length() - 4).toLowerCase().equals(".dot")) {
//                dotFile = false;
//            }
//            else {
//                dotFile = true;
//            }
//        }

        // Checking if file exists
        File f = new File(fileName);
        //absoluteFilePath = f.getAbsolutePath();
        if(!f.isDirectory()) {
            fileExists = true;
        } else {
            fileExists = false;
        }

        if (fileExists) {
            return true;
        } else {
            System.err.println("INVALID INPUT: File provided does not exist or not in correct format!");
            return false;
        }
    }

    private static String getFileName() {
        return args[0];
    }

    private static String getDefaultOutputFileName() {
        return args[0].replace(".dot", "") + "-output.dot";
    }

    private static boolean checkNumberOfProcessors() {
        String processorInput = args[1];
        if (!isInteger(processorInput)) {
            System.err.println("INVALID INPUT: Number of processors needs to be a valid integer!");
            return false;
        } else if (Integer.valueOf(processorInput) < 1) {
            System.err.println("INVALID INPUT: Number of processors needs to be at least 1!");
            return false;
        } else {
            return true;
        }
    }

    private static boolean isInteger(String processorInput) {
        try {
            Integer.parseInt(processorInput);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    private static int getNumberOfProcessors() {
        return Integer.valueOf(args[1]);
    }

    private static boolean checkAndSetOptionalArguments() {
        // If no optional arguments, return true as this is fine
        if (args.length < 3) {
            return true;
        } else {
            String[] optionalArgs = Arrays.copyOfRange(args, 2, args.length);
            for (int i = 0; i < optionalArgs.length; i++) {
                if (optionalArgs[i].toLowerCase().equals("-p")) {
                    if (optionalArgs.length >= i+1) {
                        if (isInteger(optionalArgs[i + 1])) {
                            usingParallelExecution = true;
                            numberOfCores = Integer.valueOf(optionalArgs[i+1]);
                            i++;
                        } else {
                            System.err.println("INVALID INPUT: -p must be followed by an integer!");
                            return false;
                        }
                    } else {
                        System.err.println("INVALID INPUT: -p must be followed by an integer!");
                        return false;
                    }
                } else if (optionalArgs[i].toLowerCase().equals("-v")) {
                    usingVisualization = true;
                } else if (optionalArgs[i].toLowerCase().equals("-o")) {
                    if (optionalArgs.length >= i+1) {
                        String[] illegalCharacters = new String[]{
                                "#", "%", "&", "{", "}", "\\\\", "<", ">", "*", "?", "/", "$", "!", "'",   "\"", ":",
                                "@", "+", "`", "|", "="};

                        for (String illegalCharacter : illegalCharacters) {
                            if (optionalArgs[i + 1].contains(illegalCharacter)) {
                                System.err.println("INVALID INPUT: Filename cannot contain illegal characters!");
                                return false;
                            }
                        }
                        usingCustomOutputName = true;
                        outputFileName = optionalArgs[i+1];
                        i++;
                    } else {
                        System.err.println("INVALID INPUT: -o must be followed by an output name!");
                        return false;
                    }
                } else {
                    System.err.println("INVALID INPUT: Optional parameters are invalid!");
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Printing the headers into the terminal
     */
    private static void printHeaders() {
        ArrayList<String> inputString = new ArrayList<>(Arrays.asList("=========================",
                "TEAM 3 OPTIMAL SOLVER",
                "=========================",
                "INPUT PARAMETERS: ",
                "Input graph name: " + fileName,
                "Output graph name: " + outputFileName,
                "Number of processors: " + numberOfProcessors
                ));

        System.out.println("=========================");
        System.out.println("TEAM 3 OPTIMAL SOLVER");
        System.out.println("=========================");
        System.out.println("INPUT PARAMETERS: ");
        System.out.println("Input graph name: " + fileName);
        System.out.println("Output graph name: " + outputFileName);
        System.out.println("Number of processors: " + numberOfProcessors);
        if (usingParallelExecution) {
            System.out.println("Using parallel execution: True (with " + numberOfCores + " number of cores)");
            inputString.add("Using parallel execution: True (with " + numberOfCores + " number of cores)");
        } else {
            System.out.println("Using parallel execution: False");
            inputString.add("Using parallel execution: False");
        }
        if (usingVisualization) {
            System.out.println("Using visualization: True");
            inputString.add("Using visualization: True");
        } else {
            System.out.println("Using visualization: False");
            inputString.add("Using visualization: False");
        }
        if (usingCustomOutputName) {
            System.out.println("Using custom output name: True (custom name is " + outputFileName + ")");
            inputString.add("Using custom output name: True (custom name is " + outputFileName + ")");
        } else {
            System.out.println("Using custom output name: False (default name is " + outputFileName + ")");
            inputString.add("Using custom output name: False (default name is \" + outputFileName + \")");
        }
        inputString.add("=========================");
        System.out.println("=========================");
        terminalInput = inputString;
    }

    private static void printInputGraph() {

        terminalInput.add("Input graph:");
        terminalInput.add("");

        System.out.println("Input graph:");
        System.out.println();
        StringBuilder sb = new StringBuilder();
        try {
            if (usingOnlyVisualization) {
                fileName = absoluteFilePath;
            }

            File dotFile;

            if (absoluteFilePath != null) {
                dotFile = new File(absoluteFilePath);
            } else {
                dotFile = new File(fileName);
            }

            Scanner s = new Scanner(dotFile);
            s.nextLine();
            while (s.hasNextLine()) {
                String line = s.nextLine();
                if (line.equals("}")) {
                    break;
                }
                line = line.replace("[", "").replace("]", "").replace(";", "");
                sb.append(line).append("\n");
            }
            s.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(sb);
        terminalInput.add(sb.toString());
        System.out.println();
    }

    /**
     * Converts the input digraph file into a graph structure that is then used to generate the optimal schedule.
     * @return
     */
    private static Graph buildGraph() {
        Graph graph = new Graph();
        graph.setGraphName(outputFileName);
        StringBuilder sb = new StringBuilder();

        try {
            if (usingOnlyVisualization) {
                fileName = absoluteFilePath;
            }

            File dotFile;

            if (absoluteFilePath != null) {
                dotFile = new File(absoluteFilePath);
            } else {
                dotFile = new File(fileName);
            }

            Scanner s = new Scanner(dotFile);
            s.nextLine();
            while (s.hasNextLine()) {
                try {
                    String lineOrigin = s.nextLine();
                    if (lineOrigin.equals("}")) {
                        break;
                    }

                    String line = lineOrigin.replace("[", "").replace("]", "").replace(";", "").replace("=", " ");

                    String[] splitLine = line.split("\\s+");

                    Task task = graph.getTaskByName(splitLine[1]);

                    if (lineOrigin.indexOf('[') > 0) {
                        if (splitLine.length == 4) {
                            int taskWeight = Integer.parseInt(splitLine[3]);
                            if (task == null) {
                                task = new Task(splitLine[1], taskWeight);
                                graph.addTask(task);
                            } else{
                                task.setWeight(taskWeight);
                            }
                        } else {
                            if (task == null) {
                                task = new Task(splitLine[1], 0);
                                graph.addTask(task);
                            }

                            Task to = graph.getTaskByName(splitLine[3]);
                            if (to == null) {
                                to = new Task(splitLine[3], 0);
                                graph.addTask(to);
                            }
                            task.incrementOutDegree();
                            to.incrementInDegree();
                            Edge edge = new Edge(task, to, Integer.parseInt(splitLine[5]));
                            graph.addEdge(task, edge);
                            graph.addReversedEdge(to, new Edge(to, task, Integer.parseInt(splitLine[5])));
                        }
                    }
                    sb.append(line).append("\n");
                }
                catch (NumberFormatException e) {
                }
                catch (ArrayIndexOutOfBoundsException e) {
                }
            }
            s.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return graph;
    }

}