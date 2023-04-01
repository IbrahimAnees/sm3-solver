package visualisation.System;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GraphTimeProvider {

    private static long time;
    private Timeline timeline;
    private List<Timeline> timelineList;
    private static GraphTimeProvider provider;

    private GraphTimeProvider() {
        time = 0;
        timelineList = new ArrayList<>();

        timeline = new Timeline(new KeyFrame(Duration.millis(1000), event -> time += 1000));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public static GraphTimeProvider getInstance() {
        if (provider == null) {
            provider = new GraphTimeProvider();
        }

        return provider;
    }

    public void stopTimers() {
        for (Timeline timeline : timelineList) {
            timeline.stop();
        }
    }

    public void registerTimeline(Timeline timeline) {
        timelineList.add(timeline);
    }

    public long getCurrentMilli() {
        return time % 1000;
    }

    public long getCurrentSec() {
        return time / 1000;
    }

}
