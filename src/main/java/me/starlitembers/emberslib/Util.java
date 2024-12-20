package me.starlitembers.emberslib;

public class Util {
    public static long timeToTicks(int hours, int minutes, int seconds, int ticks){
        return (((hours * 60L) * 60) * 20) + ((minutes * 60L) * 20) + (seconds * 20L) + ticks;
    }
}
