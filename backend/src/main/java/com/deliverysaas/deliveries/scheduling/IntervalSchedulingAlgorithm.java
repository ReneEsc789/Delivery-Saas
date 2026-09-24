package com.deliverysaas.deliveries.scheduling;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class IntervalSchedulingAlgorithm {

    public static List<TimeWindow> selectCompatible(List<TimeWindow> windows) {
        List<TimeWindow> sorted = new ArrayList<>(windows);
        sorted.sort(Comparator.comparing(TimeWindow::end));

        List<TimeWindow> selected = new ArrayList<>();
        Instant lastEnd = null;
        for (TimeWindow window : sorted) {
            if (lastEnd == null || !window.start().isBefore(lastEnd)) {
                selected.add(window);
                lastEnd = window.end();
            }
        }
        return selected;
    }
}
