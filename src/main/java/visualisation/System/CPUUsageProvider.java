package visualisation.System;

import javafx.scene.chart.AreaChart;

public class CPUUsageProvider extends UsageProvider {
    
    public CPUUsageProvider(AreaChart<String, Number> graph, String title, GraphTimeProvider gtp) {
        super(graph,title, gtp);
    }

    @Override
    public double getData() {
        return this.bean.getProcessCpuLoad() * 100;
    }

    @Override
    public double getUpperBound() {
        return 100;
    }

}
