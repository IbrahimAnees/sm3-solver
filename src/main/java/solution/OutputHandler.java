package solution;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class OutputHandler {
    public static ArrayList<String> terminalOutput = new ArrayList<>();

    /**
     * Print the output to the terminal
     * @param outputFileName
     */
    public static void printOutput(String outputFileName) {
        terminalOutput.add("");
        terminalOutput.add("=========================");
        terminalOutput.add("Output graph:");
            StringBuilder sb = new StringBuilder();
            try {
                File dotFile = new File(outputFileName);
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
            terminalOutput.add(sb.toString());
    }
}
