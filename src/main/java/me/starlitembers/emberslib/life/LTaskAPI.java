package me.starlitembers.emberslib.life;

import com.google.gson.reflect.TypeToken;
import me.starlitembers.emberslib.data.DataAPI;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;

public class LTaskAPI {
    final Plugin p;
    final DataAPI data;
    final Random random = new Random();
    private TreeMap<UUID, String> activeTasks;
    private TreeMap<String, LTask> allTasks;
    private BiConsumer<UUID, LTask> failLogic;
    private BiConsumer<UUID, LTask> winLogic;
    public LTaskAPI(Plugin plugin, DataAPI dataAPI){
        p = plugin;
        data = dataAPI;
        File f1 = data.unsafe().createIfNull(new File(p.getDataFolder(), "active_tasks.json"));
        File f2 = data.unsafe().createIfNull(new File(p.getDataFolder(), "tasks.json"));
        activeTasks = data.GSON.fromJson(data.unsafe().readData(f1), new TypeToken<TreeMap<UUID, String>>(){}.getType());
        allTasks = data.GSON.fromJson(data.unsafe().readData(f2), new TypeToken<TreeMap<String, LTask>>(){}.getType());
        if(activeTasks == null) activeTasks = new TreeMap<>();
        if(allTasks == null) allTasks = new TreeMap<>();
    }
    public void save(){
        File f1 = data.unsafe().createIfNull(new File(p.getDataFolder(), "active_tasks.json"));
        File f2 = data.unsafe().createIfNull(new File(p.getDataFolder(), "tasks.json"));
        data.unsafe().writeData(f1, data.GSON.toJson(activeTasks, new TypeToken<TreeMap<UUID, String>>(){}.getType()));
        data.unsafe().writeData(f2, data.GSON.toJson(allTasks, new TypeToken<TreeMap<String, LTask>>(){}.getType()));
    }
    public void setOnFail(BiConsumer<UUID, LTask> failLogic){
        this.failLogic = failLogic;
    }
    public void setOnSucceed(BiConsumer<UUID, LTask> winLogic){
        this.winLogic = winLogic;
    }
    public void fail(UUID uuid){
        Optional.ofNullable(failLogic).ifPresent(consumer -> getAssignedTask(uuid).ifPresent(task -> consumer.accept(uuid, task)));
    }
    public void succeed(UUID uuid){
        Optional.ofNullable(winLogic).ifPresent(consumer -> getAssignedTask(uuid).ifPresent(task -> consumer.accept(uuid, task)));
    }
    public Optional<LTask> getTask(String id){
        LTask task = allTasks.get(id);
        return Optional.ofNullable(task);
    }
    public List<LTask> getTasks(int value){
        List<LTask> tasks = new ArrayList<>();
        allTasks.forEach((s, task) -> {
            if(task.getMinValue() <= value && task.getMaxValue() >= value) tasks.add(task);
        });
        return tasks;
    }
    public List<LTask> getTasks(){
        List<LTask> tasks = new ArrayList<>();
        allTasks.forEach((s, task) -> tasks.add(task));
        return tasks;
    }
    public void addTask(LTask task){
        allTasks.put(task.getId(), task);
    }
    public void removeTask(String id){
        getTask(id).ifPresent(task -> allTasks.remove(id));
    }
    public void unassignTask(UUID player){
        activeTasks.remove(player);
    }
    public void assignTask(UUID player, int value){
        List<LTask> tasks = getTasks(value);
        int r = random.nextInt(0, tasks.size());
        LTask task = tasks.get(r);
        activeTasks.put(player, task.getId());
    }
    public void assignTask(UUID player, String id){
        Optional<LTask> task = getTask(id);
        task.ifPresent(t -> activeTasks.put(player, id));
    }
    public Optional<LTask> getAssignedTask(UUID uuid){
        return getTask(activeTasks.get(uuid));
    }
}
