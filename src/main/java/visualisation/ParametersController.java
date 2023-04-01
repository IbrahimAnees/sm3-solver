package visualisation;


import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import solution.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;


public class ParametersController {

    private String fileName;
    private String filePath;
    private Stage stage;
    private Parent root;
    private boolean mainScreen = false;
    @FXML private Text inputText;
    @FXML private TextField outputText;
    @FXML private TextField processorsText;
    @FXML private CheckBox pCheckbox;
    @FXML private TextField coresText;
    @FXML private Text coresTitle;
    @FXML private Rectangle coreBorder;

    @FXML private Text startTextHover;
    @FXML private Rectangle startRectHover;
    static public Timeline timeline;


    public void initialize() {
        if (!VisualisationLauncher.onlyVisualisationStatic) {
            integerOnly(processorsText);
            integerOnly(coresText);
            inputText.setText(InputHandler.fileName);
            outputText.setText(InputHandler.outputFileName);
            processorsText.setText(String.valueOf(InputHandler.numberOfProcessors));
            pCheckbox.setSelected(InputHandler.usingParallelExecution);

            if (InputHandler.usingParallelExecution) {
                coresText.setText(String.valueOf(InputHandler.numberOfCores));
                coresText.setVisible(true);
                coresTitle.setVisible(true);
                coreBorder.setVisible(true);
            }
        }
    }
    public void setFileInfo(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
        inputText.setText(fileName);
        outputText.setText(fileName.replace(".dot", "") + "-output.dot");
        processorsText.setText("2");
        coresText.setText("2");
        pCheckbox.setSelected(false);
        coresText.setVisible(false);
        coresTitle.setVisible(false);
        coreBorder.setVisible(false);
        integerOnly(processorsText);
        integerOnly(coresText);
        InputHandler.fileName = filePath;
        mainScreen = true;
    }

    public void onCheckParallelization(ActionEvent event) {
        if (pCheckbox.isSelected()) {
            coresText.setVisible(true);
            coresTitle.setVisible(true);
            coreBorder.setVisible(true);
        } else {
            coresText.setVisible(false);
            coresTitle.setVisible(false);
            coreBorder.setVisible(false);
        }
    }

    public void startButtonHover(MouseEvent Event) {
        startTextHover.setVisible(true);
        startRectHover.setVisible(true);
    }

    public void startButtonDefault(MouseEvent Event) {
        startTextHover.setVisible(false);
        startRectHover.setVisible(false);
    }

    public void switchToVisualisation(ActionEvent event) throws IOException {


        ArrayList<String> argsTemp = new ArrayList<>();
        argsTemp.add(inputText.getText());
        argsTemp.add(processorsText.getText());
        if (pCheckbox.isSelected()) {
            argsTemp.add("-p");
            argsTemp.add(coresText.getText());
        }
        argsTemp.add("-o");
        argsTemp.add(outputText.getText());
        String[] args = argsTemp.toArray(new String[0]);

        if (!mainScreen) {
            InputHandler.absoluteFilePath = inputText.getText();
        } else {
            InputHandler.absoluteFilePath = filePath;
        }

        InputHandler.parseInput(args);

        InputHandler.numberOfProcessors = Integer.valueOf(processorsText.getText());
        InputHandler.usingParallelExecution = pCheckbox.isSelected();

        if (pCheckbox.isSelected()) {
            InputHandler.numberOfCores = Integer.valueOf(coresText.getText());
        }
        InputHandler.outputFileName = outputText.getText();



        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("visualisationScreen.fxml"));
        root = loader.load();

        VisualisationController vc = loader.getController();

        timeline = new Timeline();
        AStar scheduler = null;
        if (InputHandler.usingParallelExecution) {
            scheduler = new AStarParallel(InputHandler.inputGraph, InputHandler.numberOfProcessors, InputHandler.numberOfCores);
        } else {
            scheduler = new AStar(InputHandler.inputGraph, InputHandler.numberOfProcessors);
        }

        AStar finalScheduler = scheduler;
        vc.setNodeGraph(InputHandler.absoluteFilePath==null?InputHandler.fileName:InputHandler.absoluteFilePath);
        new Thread(() -> {
            try {
                startSolution(finalScheduler, timeline, vc);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();


//        startSolution(scheduler, timeline, vc);





        //VisualisationController vc = loader.getController();

        vc.startScheduleGraph(scheduler, InputHandler.numberOfProcessors, timeline);
        vc.startStatsRamCpu(scheduler);

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void returnToMain(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("mainScreen.fxml"));
        root = loader.load();
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void integerOnly(final TextField field) {
        field.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(
                    ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    field.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

    private void startSolution(AStar s, Timeline timeline, VisualisationController vc) throws InterruptedException {


        long start2 = System.currentTimeMillis();


        //AStar s = new AStar(graph, InputHandler.numberOfProcessors);
        Schedule op = s.aStar();

        long time = System.currentTimeMillis();
        vc.setEndTime(time - start2);
        vc.stopTimer();



        s.updateTasks(op);
        s.outPutGraph(s.getGraph());

        long finish2 = System.currentTimeMillis();
//        vc.startScheduleGraph(s, InputHandler.numberOfProcessors, timeline);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        timeline.stop();
//        vc.updateFinalGraph(op, InputHandler.numberOfProcessors);
        System.out.println(op.getCurrentEndTime());
        System.out.println("A* time taken: " + (finish2 - start2) + "ms");


    }
}