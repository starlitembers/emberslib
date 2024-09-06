package me.starlitembers.emberslib;

public class Util {
    public static int timeToTicks(int hours, int minutes, int seconds, int ticks){
        return (((hours * 60) * 60) * 20) + ((minutes * 60) * 20) + (seconds * 20) + ticks;
    }
}
