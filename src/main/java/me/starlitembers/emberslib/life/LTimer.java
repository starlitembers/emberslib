package me.starlitembers.emberslib.life;

import me.starlitembers.emberslib.EmbersLib;
import me.starlitembers.emberslib.data.SerializableData;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;

public class LTimer {
    long ticks;
    boolean paused;
    final String id;
    transient Type dataTypeRaw;
    private String dataType;
    private SerializableData data;
    void serialize(){
        if(data == null) return;
        data.onSerialize();
        dataType = data.getClass().getName();
    }
    void deserialize(){
        if(dataType == null) return;
        if(data == null) return;
        try {
            dataTypeRaw = Class.forName(dataType);
            data.onDeserialize();
        } catch (ClassNotFoundException e) {
            EmbersLib.plugin().getLogger().severe("ClassNotFoundException! ["+ dataType +"]");
        }
    }
    public <T extends SerializableData> void setData(T data){
        this.data = data;
    }
    public <T extends SerializableData> T getData(Class<T> clazz){
        try {
            return clazz.cast(data);
        } catch (ClassCastException e){
            EmbersLib.plugin().getLogger().severe("Class Cast Exception! ["+ data.getClass().getName()+" <- "+clazz.getName()+"]");
            return null;
        }
    }
    public boolean isPaused(){
        return paused;
    }
    public void setPaused(boolean paused){
        this.paused = paused;
    }
    public String getId(){
        return id;
    }
    HashMap<UUID, Long> offset = new HashMap<>();
    <T extends SerializableData> LTimer(String id, int startTicks, boolean paused, T data){
        this.ticks = startTicks;
        this.paused = paused;
        this.id = id;
        this.data = data;
        if(data == null) dataTypeRaw = null;
        else this.dataTypeRaw = data.getClass();
    }
    void initIfNull(){
        if(offset == null) offset = new HashMap<>();
    }
    public long getGlobalTicks(){
        return ticks;
    }
    public void setGlobalTicks(long ticks){
        this.ticks = ticks;
    }
    public void resetTimer(long startTicks, boolean paused){
        this.ticks = startTicks;
        this.paused = paused;
        offset = new HashMap<>();
    }
    public long getOffset(UUID player){
        if(!offset.containsKey(player)) offset.put(player, 0L);
        return offset.get(player);
    }
    public long getFinalTime(UUID player){
        return ticks - getOffset(player);
    }
    public long getOffset(Player player){
        return getOffset(player.getUniqueId());
    }
    public long getFinalTime(Player player){
        return getFinalTime(player.getUniqueId());
    }
    public void setOffset(UUID player, long offset){
        this.offset.put(player, offset);
    }
    public void setFinalTime(UUID player, long ticks){
        setOffset(player, this.ticks - ticks);
    }
    public void setFinalTime(Player player, long ticks){
        setFinalTime(player.getUniqueId(), ticks);
    }
    public void setOffset(Player player, long offset){
        setOffset(player.getUniqueId(), offset);
    }
    public void addPlayer(UUID player, long offset){
        this.offset.put(player, offset);
    }
    public void removePlayer(UUID player){
        this.offset.remove(player);
    }
    public boolean hasPlayer(UUID player){
        return offset.containsKey(player);
    }
    public boolean hasPlayer(Player player){
        return hasPlayer(player.getUniqueId());
    }
    public void addPlayer(Player player, long offset){
        addPlayer(player.getUniqueId(), offset);
    }
    public void addTime(UUID player, long ticksToAdd){
        setOffset(player, getOffset(player) - ticksToAdd);
    }
    public void addTime(Player player, long ticksToAdd){
        addTime(player.getUniqueId(), ticksToAdd);
    }
    public void removeTime(UUID player, long ticksToRemove){
        setOffset(player, getOffset(player) + ticksToRemove);
    }
    public void removeTime(Player player, long ticksToRemove){
        removeTime(player.getUniqueId(), ticksToRemove);
    }
    public void forEach(BiConsumer<UUID, Long> consumer){
        this.offset.forEach(consumer);
    }
}
