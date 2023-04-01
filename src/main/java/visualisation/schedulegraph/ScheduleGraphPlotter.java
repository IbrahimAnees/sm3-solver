package visualisation.schedulegraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import solution.Schedule;
import solution.Task;

/**
 * This class initialises the gantt chart that is displayed on the visualisation. As well as updating the data within
 * the graph when the schedules are being produced.
 * Base code taken from: https://stackoverflow.com/questions/27975898/gantt-chart-from-scratch
 */
public class ScheduleGraphPlotter {
    private static NumberAxis xAxis;
    private static CategoryAxis yAxis;
    private static ScheduleGraph<Number,String> chart;
    private ObservableList<XYChart.Series<Number, String>> processorSeries;
    private ArrayList<String> yAxisLabels;

    /**
     * Initialise the gantt chart
     * @param numProcessors
     */
    public ScheduleGraphPlotter(int numProcessors) {
        xAxis = new NumberAxis();
        yAxis = new CategoryAxis();
        chart = new ScheduleGraph<Number,String>(xAxis, yAxis);

        xAxis.setLabel("Time (s)");
        xAxis.setTickLabelFill(Color.WHITE);
        xAxis.setTickLabelGap(10);
        xAxis.setMinorTickCount(1);

        yAxis.setLabel("Processor");
        yAxis.setTickLabelFill(Color.WHITE);
        yAxis.setTickLabelGap(10);
        yAxis.lookup(".axis-label").setStyle("-fx-text-fill: white;");

        yAxisLabels = new ArrayList<>();

        for (int i = 1; i <= numProcessors; i++) {
            yAxisLabels.add(String.valueOf(i));
        }
        yAxis.setCategories(FXCollections.<String>observableArrayList(yAxisLabels));

        chart.setLegendVisible(false);
        chart.setBlockHeight( 50);

        processorSeries = FXCollections.observableArrayList();

        for (int i = 0; i < numProcessors; i++) {
            processorSeries.add(new XYChart.Series());
        }

        chart.getXAxis().setTickLabelGap(1);

        chart.setAnimated(false);
        chart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
        chart.lookup(".axis-label").setStyle("-fx-text-fill: white;");

        chart.setVerticalGridLinesVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setData(processorSeries);
    }

    /**
     * Updating the data in the gantt chart with a newly produced schedule
     * @param s
     */
    public void updateChart(Schedule s) {
        int[][] schedule = s.getSchedule();
        for (ArrayList<Task> a : s.getProcessorTasksMap()) {
            for (Task t : a) {
            }
        }

        // Clear series before updating.
        for (XYChart.Series series : processorSeries) {
            series.getData().clear();
        }
        
        for (int i = 1; i <= s.getNumProcessors(); i++) {

            List<Task> tasks = s.getSingleProcessorTasksMap(i);

            for (int j = 0; j < tasks.size(); j++) {
                Task currentTask = tasks.get(j);
                int startTime = schedule[currentTask.getIndex()][1];
                if (startTime == -1) {
                    continue;
                }

                int length = currentTask.getWeight();

                XYChart.Data<Number, String> data = new XYChart.Data(
                        startTime, yAxisLabels.get(i-1),
                        new ScheduleGraph.ExtraData(length, "status-pink", currentTask.getName()));

                XYChart.Series series = processorSeries.get(i - 1);
                series.getData().add(data);
            }
        }

        chart.setData(processorSeries);
    }

    /**
     * Return the current chart
     * @return
     */
    public ScheduleGraph getChart() {
        return chart;
    }
}
