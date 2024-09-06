package me.starlitembers.emberslib.life;

import com.google.gson.reflect.TypeToken;
import me.starlitembers.emberslib.EmbersLib;
import me.starlitembers.emberslib.Util;
import me.starlitembers.emberslib.data.DataAPI;
import me.starlitembers.emberslib.data.SerializableData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class LTimeAPI implements Listener {
    final Plugin p;
    final DataAPI data;
    BiFunction<PlayerDeathEvent, Integer, Integer> onDeath;
    boolean shortenFinalHour = true;
    boolean shortenFinalMinute = false;
    public boolean getFinalHourShortened(){
        return shortenFinalHour;
    }
    public void setFinalHourShortened(boolean shortened){
        shortenFinalHour = shortened;
    }
    public boolean getFinalMinuteShortened(){
        return shortenFinalMinute;
    }
    public void setFinalMinuteShortened(boolean shortened){
        shortenFinalMinute = shortened;
    }
    public LTimeAPI(Plugin plugin, DataAPI data){
        this.p = plugin;
        this.data = data;
        File f = data.unsafe().createIfNull(new File(p.getDataFolder(), "timers.json"));
        boolean exists = f.exists();
        if(!exists){
            data.unsafe().writeData(f, data.GSON.toJson(timerHashMap));
        }
        timerHashMap = data.GSON.fromJson(data.unsafe().readData(f), new TypeToken<HashMap<String, LTimer>>(){}.getType());
        if(timerHashMap == null){
            timerHashMap = new HashMap<>();
        }
        forEach((k, v) -> {
            v.initIfNull();
            v.deserialize();
        });
    }
    public void setDeathFunction(BiFunction<PlayerDeathEvent, Integer, Integer> bifunction){
        this.onDeath = bifunction;
    }
    public void registerGlobalTimer(int startTime){
        createTimerIfAbsent("global", startTime, true, null);
    }
    public Optional<LTimer> getGlobalTimer(){
        return Optional.ofNullable(getTimer("global"));
    }
    void enable(){
        p.getServer().getPluginManager().registerEvents(this, p);
    }
    @EventHandler
    void onDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        if(onDeath != null) getGlobalTimer().ifPresent(t -> t.setFinalTime(player, onDeath.apply(event, getTimer("global").getFinalTime(player))));
    }
    public void save(){
        forEach(((string, lTimer) -> lTimer.serialize()));
        File f = new File(p.getDataFolder(), "timers.json");
        data.unsafe().writeData(f, data.GSON.toJson(timerHashMap, new TypeToken<Map<String, LTimer>>(){}.getType()));
    }
    HashMap<String, LTimer> timerHashMap = new HashMap<>();
    public int getPlayerTime(UUID player, String id) {
        return getTimer(id).getFinalTime(player);
    }
    public int getPlayerTime(Player player, String id) {
        return getPlayerTime(player.getUniqueId(), id);
    }
    public LTimer getTimer(String id){
        return timerHashMap.get(id);
    }
    public <T extends SerializableData> void createTimer(String id, int startTicks, boolean paused, T data){
        timerHashMap.put(id, new LTimer(id, startTicks, paused, data));
    }
    public <T extends SerializableData> void createTimerIfAbsent(String id, int startTicks, boolean paused, T data){
        if(!timerHashMap.containsKey(id)) createTimer(id, startTicks, paused, data);
    }
    public void updateColor(UUID player, String id, LColorAPI color){
        color.updatePlayerColor(player, id, this);
    }
    public void updateColor(Player player, String id, LColorAPI color){
        updateColor(player.getUniqueId(), id, color);
    }
    public BukkitTask registerTimer(String id, boolean positive, long delay, long period, Consumer<LTimer> consumer){
        if(getTimer(id) == null) return null;
        return Bukkit.getScheduler().runTaskTimer(EmbersLib.plugin(), () -> {
            LTimer timer = getTimer(id);
            if(timer.paused) return;
            consumer.accept(timer);
            if(positive){
                timer.ticks++;
            } else {
                timer.ticks--;
            }
        }, delay, period);
    }
    public String formatTime(int ticks, boolean colored, LColorAPI LFColorAPI){
        final int SECONDS_IN_MINUTE = 60;
        final int MINUTES_IN_HOUR = 60;
        final int TICKS_IN_SECOND = 20;

        int seconds = ticks / TICKS_IN_SECOND;
        int minutes = seconds / SECONDS_IN_MINUTE;
        int hours = minutes / MINUTES_IN_HOUR;

        StringBuilder finalString = new StringBuilder();

        if(colored){
            String color = LFColorAPI.getColorByRule(ticks).toString();
            finalString.append(color);
        }

        String temp = hours > 9 ? hours+"" : "0"+hours;

        if(ticks >= 72000 || !shortenFinalHour){
            finalString.append(temp);
            finalString.append(":");
        }
        temp = minutes % 60 > 9 % 60 ? minutes % 60+"" : "0"+minutes % 60;

        if(ticks > 1200 || !shortenFinalMinute){
            finalString.append(temp);
            finalString.append(":");
        }

        temp = seconds % 60 > 9 ? seconds % 60+"" : "0"+seconds % 60;

        finalString.append(temp);

        return finalString.toString();
    }
    public void forEach(BiConsumer<String, LTimer> consumer){
        timerHashMap.forEach(consumer);
    }
}
