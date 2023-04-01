package visualisation.System;

import com.sun.management.OperatingSystemMXBean;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;

import java.lang.management.ManagementFactory;

public abstract class UsageProvider {

    private AreaChart<String, Number> graph;
    protected final OperatingSystemMXBean bean;
    private GraphTimeProvider timeProvider;

    public UsageProvider(AreaChart<String, Number> graph, String title, GraphTimeProvider timeProvider) {
        this.graph = graph;
        graph.setTitle(title);
        graph.setLegendVisible(false);
        bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.timeProvider = timeProvider;
    }

    public void startTracking() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        graph.setAnimated(false);
        graph.getYAxis().setAutoRanging(false);
        graph.getData().add(series);

        EventHandler<ActionEvent> graphUpdater = event -> {
            ObservableList currentData = series.getData();
            currentData.add(new XYChart.Data<>(String.valueOf(timeProvider.getCurrentSec()), getData()));
            if (currentData.size() == 20){
                currentData.remove(0);
            }
        };

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), graphUpdater));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

    }

    public abstract double getData();

    public abstract double getUpperBound();

}
