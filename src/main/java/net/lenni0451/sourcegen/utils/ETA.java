package net.lenni0451.sourcegen.utils;

import java.util.ArrayList;
import java.util.List;

public class ETA {

    public static String format(final long ms) {
        if (ms < 0) return "~";
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }


    private final List<Long> lastDurations = new ArrayList<>();
    private long start;

    public boolean canEstimate() {
        return this.lastDurations.size() >= 2;
    }

    public void start() {
        this.start = System.nanoTime();
    }

    public void stop() {
        long nanos = System.nanoTime() - this.start;
        this.lastDurations.add(nanos / 1_000_000);
    }

    public long getLastDuration() {
        return this.lastDurations.get(this.lastDurations.size() - 1);
    }

    public long getEstimation(final int count) {
        ETA copy = new ETA();
        copy.lastDurations.addAll(this.lastDurations);
        for (int i = 0; i < count; i++) {
            copy.lastDurations.add(copy.getNextEstimation());
        }
        long sum = 0;
        for (int i = this.lastDurations.size(); i < copy.lastDurations.size(); i++) {
            sum += copy.lastDurations.get(i);
        }
        return sum;
    }

    public long getNextEstimation() {
        int n = this.lastDurations.size();
        if (n == 0) return 0;

        long sumX = 0;
        long sumY = 0;
        long sumXY = 0;
        long sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += this.lastDurations.get(i);
            sumXY += i * this.lastDurations.get(i);
            sumX2 += (long) i * i;
        }

        double slope = (n * sumXY - sumX * sumY) / (double) (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / (double) n;

        long estimation = Math.round(slope * n + intercept);
        return Math.max(estimation, 0);
    }

}
