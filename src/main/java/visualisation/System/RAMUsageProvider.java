package visualisation.System;

import javafx.scene.chart.AreaChart;

public class RAMUsageProvider extends UsageProvider {

    public RAMUsageProvider(AreaChart<String, Number> graph, String title, GraphTimeProvider gtp) {
        super(graph, title, gtp);
    }

    @Override
    public double getData() {
        double memoryOfProcess;
        double memoryOfProcessGB;

        memoryOfProcess = getUpperBound() - memoryToGB(super.bean.getFreePhysicalMemorySize());

        return memoryOfProcess;
    }

    @Override
    public double getUpperBound() {
        double totalMemory;
        double totalMemoryGB;

        totalMemory = super.bean.getTotalPhysicalMemorySize();
        totalMemoryGB = memoryToGB(totalMemory);

        return totalMemoryGB;
    }

    public double memoryToGB(double memory) {
        double GB;
//        GB = memory;
        GB = memory / Math.pow(10, 9);

        return GB;
    }

}
