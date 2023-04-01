package visualisation;

import com.groupdocs.conversion.Converter;
import com.groupdocs.conversion.filetypes.FileType;
import com.groupdocs.conversion.options.convert.ConvertOptions;
import com.groupdocs.conversion.options.convert.ImageConvertOptions;
import com.groupdocs.conversion.options.convert.JpegOptions;
//import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.*;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import solution.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import solution.AStar;

import visualisation.schedulegraph.ScheduleGraphPlotter;
import java.awt.image.BufferedImage;
import java.io.*;
import solution.InputHandler;
import solution.Schedule;
import visualisation.System.CPUUsageProvider;
import visualisation.System.GraphTimeProvider;
import visualisation.System.RAMUsageProvider;
import visualisation.schedulegraph.ScheduleGraphPlotter;

import javax.swing.plaf.ColorUIResource;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class VisualisationController {


    @FXML private Text ngUnpressed;
    @FXML private Text ramUnpressed;
    @FXML private Text statUnpressed;
    @FXML private Text termUnpressed;
    @FXML private Text ngPressed;
    @FXML private Text ramPressed;
    @FXML private Text statPressed;
    @FXML private Text termPressed;
    @FXML private BorderPane ng;
    @FXML private AnchorPane ram;
    @FXML private AnchorPane stats;
    @FXML private AnchorPane running;
    @FXML private AnchorPane complete;
    @FXML private Text runningText;
    @FXML private AnchorPane term;
    @FXML private BorderPane currentGraph;
    @FXML private Text homeTextHover;
    @FXML private Rectangle homeRectHover;
    @FXML private TextArea terminalText;
    @FXML private Text s1, s2, s3, s4, s5, s6, s7, s8, s9, s10;
    @FXML private AreaChart<String, Number> cpuGraph;
    @FXML private AreaChart<String, Number> ramGraph;
    private Stage stage;
    private Parent root;
    private GraphTimeProvider timeProvider;
    private CPUUsageProvider cpuUsageProvider;
    private RAMUsageProvider ramUsageProvider;
    Timeline timelineSchedule;
    Timeline timelineStats;


    public void initialize() {

        String inputTemp = InputHandler.fileName;
        if (inputTemp.contains(File.separator)) {
            inputTemp = InputHandler.fileName.replaceAll("^.*[\\/\\\\]", "");
        }

        String outputTemp = InputHandler.outputFileName;
        if (outputTemp.contains(File.separator)) {
            outputTemp = InputHandler.outputFileName.replaceAll("^.*[\\/\\\\]", "");
        }

        s6.setText(inputTemp);
        s7.setText(outputTemp);
        s8.setText(String.valueOf(InputHandler.numberOfProcessors));
        s9.setText(String.valueOf(InputHandler.usingParallelExecution));
        s10.setText(String.valueOf(InputHandler.numberOfCores));
        running.setVisible(true);
        complete.setVisible(false);


        for (String s : InputHandler.terminalInput) {
            terminalText.appendText(s + " \n");
        }


    }





    public void onNgPressed(MouseEvent event) {
        unpressAll();
        ngPressed.setVisible(true);
        ngUnpressed.setVisible(false);
        ng.setVisible(true);
    }

    public void onRamPressed(MouseEvent event) {
        unpressAll();
        ramPressed.setVisible(true);
        ramUnpressed.setVisible(false);
        ram.setVisible(true);
    }

    public void onStatPressed(MouseEvent event) {
        unpressAll();
        statPressed.setVisible(true);
        statUnpressed.setVisible(false);
        stats.setVisible(true);
    }

    public void onTermPressed(MouseEvent event) {
        unpressAll();
        termPressed.setVisible(true);
        termUnpressed.setVisible(false);
        term.setVisible(true);
    }

    public void unpressAll() {
        ngUnpressed.setVisible(true);
        ramUnpressed.setVisible(true);
        statUnpressed.setVisible(true);
        termUnpressed.setVisible(true);

        ngPressed.setVisible(false);
        ramPressed.setVisible(false);
        statPressed.setVisible(false);
        termPressed.setVisible(false);

        ng.setVisible(false);
        ram.setVisible(false);
        stats.setVisible(false);
        term.setVisible(false);
    }

    public void homeButtonHover(MouseEvent Event) {
        homeTextHover.setVisible(true);
        homeRectHover.setVisible(true);
    }

    public void homeButtonDefault(MouseEvent Event) {
        homeTextHover.setVisible(false);
        homeRectHover.setVisible(false);
    }

    public void returnToMain(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("mainScreen.fxml"));
        root = loader.load();
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void startScheduleGraph(AStar scheduler, int numProcessors, Timeline timeline) {
        ScheduleGraphPlotter sgp = new ScheduleGraphPlotter(numProcessors);
        AtomicInteger taskCompleted = new AtomicInteger(0);
        AtomicBoolean addScheduleP = new AtomicBoolean(false);
        EventHandler<ActionEvent> scheduleUpdater = event -> {
            Schedule s = null;
            if (InputHandler.usingParallelExecution) {
                s = getCurrentBestParallelSchedule((AStarParallel) scheduler);

                if (s !=null) {

                    if (taskCompleted.get() <= s.getTasksCompleted()) {
                        addScheduleP.set(true);
                        taskCompleted.set(s.getTasksCompleted());
                    }
                    else {
                        addScheduleP.set(false);
                    }
                }

            } else {
                s = scheduler.getCurrentBestDisplay();
            }
            if (addScheduleP.get() || !InputHandler.usingParallelExecution) {
                if(s != null)
                    sgp.updateChart(s);
//                System.out.println(Arrays.deepToString(s.getSchedule()));
            }
            this.currentGraph.setCenter(sgp.getChart());
        };

        timelineSchedule = new Timeline(new KeyFrame(Duration.millis(500), scheduleUpdater));
        timelineSchedule.setCycleCount(Timeline.INDEFINITE);
        timelineSchedule.play();

    }

    public void updateFinalGraph(Schedule s, int numProcessors) {
        ScheduleGraphPlotter sgp = new ScheduleGraphPlotter(numProcessors);
        sgp.updateChart(s);
        this.currentGraph.setCenter(sgp.getChart());
    }

    boolean timerRunning = true;

    public void setEndTime(long timeTaken) {
        runningText.setText(String.valueOf(timeTaken) + " ms");
        s1.setText(String.valueOf(timeTaken) + " ms");
    }

    public void startStatsRamCpu(AStar scheduler) {
        AtomicInteger maxCompletedTasks = new AtomicInteger();
        s1.setText("0");
        EventHandler<ActionEvent> scheduleUpdater = event -> {
            Schedule s = null;
            if (InputHandler.usingParallelExecution) {
                s = getCurrentBestParallelSchedule((AStarParallel) scheduler);
            } else {
                s = scheduler.getCurrentBestDisplay();
            }
            if (s!=null) {
                s2.setText(s.getEstimatedTime() + " s");
                s3.setText(String.valueOf(scheduler.getTotalScheduleCount()));
                s4.setText(String.valueOf(scheduler.getDiscarded()));
                maxCompletedTasks.set(Math.max(maxCompletedTasks.get(), s.getTasksCompleted()));
                s5.setText(Integer.toString(maxCompletedTasks.get()));

                if (timerRunning) {
                    double timeRn = Double.valueOf(s1.getText().replace(" s", ""));
                    timeRn = timeRn + 0.01;

                    DecimalFormat df = new DecimalFormat("#.00");
                    String twoDp = df.format(timeRn);


                    s1.setText(twoDp + " s");
                }
            }




        };

        timelineStats = new Timeline(new KeyFrame(Duration.millis(10), scheduleUpdater));
        timelineStats.setCycleCount(Timeline.INDEFINITE);
        timelineStats.play();

        timeProvider = GraphTimeProvider.getInstance();

        cpuUsageProvider = new CPUUsageProvider(cpuGraph, "CPU Usage", timeProvider);
        cpuUsageProvider.startTracking();

        NumberAxis cpuYAxis = (NumberAxis) cpuGraph.getYAxis();
        Axis cpuXAxis = cpuGraph.getXAxis();
        cpuGraph.getXAxis().setLabel("Time (s)");
        cpuGraph.getXAxis().lookup(".axis-label").setStyle("-fx-text-fill: white");
        cpuYAxis.setLabel("CPU Usage (%)");
        cpuYAxis.setUpperBound(cpuUsageProvider.getUpperBound());
        cpuYAxis.lookup(".axis-label").setStyle("-fx-text-fill: white;");
        cpuYAxis.setTickLabelFill(Color.WHITE);
        cpuXAxis.setTickLabelFill(Color.WHITE);
        cpuGraph.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
        cpuGraph.setVerticalGridLinesVisible(false);
        cpuGraph.setHorizontalGridLinesVisible(false);

        ramUsageProvider = new RAMUsageProvider(ramGraph, "RAM Usage", timeProvider);
        ramUsageProvider.startTracking();

        NumberAxis ramYAxis = (NumberAxis) ramGraph.getYAxis();
        Axis ramXAxis = ramGraph.getXAxis();
        ramGraph.getXAxis().setLabel("Time (s)");
        ramGraph.getXAxis().lookup(".axis-label").setStyle("-fx-text-fill: white");
        ramYAxis.setLabel("RAM Usage (GB)");
        ramYAxis.setUpperBound(ramUsageProvider.getUpperBound());
        ramYAxis.lookup(".axis-label").setStyle("-fx-text-fill: white;");
        ramYAxis.setTickLabelFill(Color.WHITE);
        ramXAxis.setTickLabelFill(Color.WHITE);
        ramGraph.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
        ramGraph.setVerticalGridLinesVisible(false);
        ramGraph.setHorizontalGridLinesVisible(false);


    }

    public void stopTimer() throws InterruptedException {
        timerRunning = false;
        terminalText.appendText(" \n Solution completed!");
/*        timelineSchedule.stop();
        timelineStats.stop();
        ParametersController.timeline.stop();
        Thread.sleep(1000);*/
        complete.setVisible(true);
        running.setVisible(false);
        Thread.sleep(1000);
        timelineStats.stop();
        timelineSchedule.stop();
        ParametersController.timeline.stop();




    }

    public void setNodeGraph(String filename) {
        try (InputStream inputDotFile = new FileInputStream(filename)) {
            MutableGraph mg = new Parser().read(inputDotFile);
            mg.graphAttrs().add(guru.nidi.graphviz.attribute.Color.TRANSPARENT.background());
            mg.linkAttrs().add(guru.nidi.graphviz.attribute.Color.WHITE);
            mg.nodeAttrs().add(guru.nidi.graphviz.attribute.Color.WHITE);
            mg.nodeAttrs().add(guru.nidi.graphviz.attribute.Color.WHITE.font());
            BufferedImage bi = Graphviz.fromGraph(mg).render(Format.SVG).toImage();
            ImageView iv = new ImageView(SwingFXUtils.toFXImage(bi, null));
            iv.prefHeight(1000);
            iv.prefWidth(1000);
            ng.setCenter(iv);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Schedule getCurrentBestParallelSchedule(AStarParallel s) {
        int fastest = Integer.MAX_VALUE;
        Schedule schedule = null;
        AStarParallel.ThreadWorker[] threadworkers = s.getThreadWorker();

        for (AStarParallel.ThreadWorker t : threadworkers) {
            Schedule currentS = t.getCurrentBestDisplay();
            if (currentS != null) {
                int currentTime = currentS.getLowerBound();
                if (currentTime < fastest) {
                    fastest = currentTime;
                    schedule = currentS;
                }
            }
        }

        return schedule;
    }

}