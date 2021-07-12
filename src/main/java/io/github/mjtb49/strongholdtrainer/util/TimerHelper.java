package io.github.mjtb49.strongholdtrainer.util;

import java.text.DecimalFormat;

public class TimerHelper {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("00.00");

    public static String ticksToTime(int ticks) {
        float s = ticks / 20.0f;
        return (int) (s / 60) + ":" + DECIMAL_FORMAT.format((s % 60));
    }

    public static String millisToTime(long ms) {
        float s = ms / 1000f;
        return (int) (s / 60) + ":" + DECIMAL_FORMAT.format((s % 60));
    }
}
