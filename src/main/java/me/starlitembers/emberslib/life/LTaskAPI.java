package me.starlitembers.emberslib.life;

import com.google.gson.reflect.TypeToken;
import me.starlitembers.emberslib.command.BaseCommand;
import me.starlitembers.emberslib.command.SubCommand;
import me.starlitembers.emberslib.command.TabCompletionType;
import me.starlitembers.emberslib.data.DataAPI;
import me.starlitembers.emberslib.data.SerializableData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;

public class LTaskAPI implements Listener {
    final Plugin p;
    final DataAPI data;
    final Random random = new Random();
    private TreeMap<UUID, String> activeTasks;
    private TreeMap<String, LTask> allTasks;
    private BiConsumer<UUID, LTask> failLogic;
    private BiConsumer<UUID, LTask> winLogic;
    private BiConsumer<UUID, LTask> rerollLogic;
    private SKWrapper skWrapper = null;
    private boolean settingUpSK = false;
    private Player settingUpSKPlayer = null;
    private int settingUpSKPhase = 0;
    private boolean cd = false;
    public static class SKWrapper implements SerializableData {
        public SKWrapper(){

        }
        transient Location[] locations = new Location[]{null, null, null, null};
        private double[][] positions = new double[][]{new double[]{0, 0, 0}, new double[]{0, 0, 0}, new double[]{0, 0, 0}, new double[]{0, 0, 0}};
        private String[] worlds = new String[]{null, null, null, null};
        @Override
        public void onSerialize() {
            for(int i = 0; i < locations.length; i++){
                Location l = locations[i];
                if(l == null){
                    positions[i] = null;
                    worlds[i] = null;
                    continue;
                }
                positions[i] = new double[]{l.getX(), l.getY(), l.getZ()};
                worlds[i] = l.getWorld().getName();
            }
        }

        @Override
        public void onDeserialize() {
            for(int i = 0; i < positions.length; i++){
                double[] pos = positions[i];
                String name = worlds[i];
                if(pos == null || name == null){
                    locations[i] = null;
                    continue;
                }
                locations[i] = new Location(Bukkit.getWorld(name), pos[0], pos[1], pos[2]);
            }
        }
    }

    boolean useSecretKeeper = false;
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
    public void initSecretKeeper(){
        useSecretKeeper = true;
        skWrapper = data.get("secret_keeper", SKWrapper.class);
    }
    public Location getSucceedButton(){
        if(!useSecretKeeper) throw new UnsupportedOperationException("Can't get button locations if Secret Keeper is disabled!");
        return skWrapper.locations[0];
    }
    public Location getRerollButton(){
        if(!useSecretKeeper) throw new UnsupportedOperationException("Can't get button locations if Secret Keeper is disabled!");
        return skWrapper.locations[1];
    }
    public Location getFailButton(){
        if(!useSecretKeeper) throw new UnsupportedOperationException("Can't get button locations if Secret Keeper is disabled!");
        return skWrapper.locations[2];
    }
    public Location getRewardsLocation(){
        if(!useSecretKeeper) throw new UnsupportedOperationException("Can't get reward locations if Secret Keeper is disabled!");
        return skWrapper.locations[3];
    }
    public void setSucceedButton(Location button){
        skWrapper.locations[0] = button.clone();
    }
    public void setRerollButton(Location button){
        skWrapper.locations[1] = button.clone();
    }
    public void setFailButton(Location button){
        skWrapper.locations[2] = button.clone();
    }
    public void setRewardsLocation(Location location){
        skWrapper.locations[3] = location.clone();
    }
    public void registerTaskCommand(BaseCommand command){
        SubCommand sc = command.createSubCommand("task");
        String lc = useSecretKeeper ? "/setupsk" : "";
        sc.setFunction(info -> info.sender.sendMessage(ChatColor.RED+"Usage: /"+command.getName()+" task <add/remove/get/set"+lc+">"));
        SubCommand add, remove, get, set, setup;
        add = sc.createSubCommand("add");
        remove = sc.createSubCommand("remove");
        get = sc.createSubCommand("get");
        set = sc.createSubCommand("set");

        add.setTabCompletionType(TabCompletionType.EMPTY);
        add.setFunction(info -> {
            if(info.args.length < 3){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+command.getName()+" task add <name> <minVal> <maxVal> [hard]");
                info.sender.sendMessage(ChatColor.YELLOW+"Name cannot contain spaces");
                return;
            }
            Collection<Material> books = List.of(Material.WRITABLE_BOOK, Material.WRITTEN_BOOK);
            if(info.sender instanceof Player plr){
                if(!books.contains(plr.getInventory().getItemInMainHand().getType())){
                    plr.sendMessage(ChatColor.RED+"Must have a task (written book) in your main hand");
                }
                int min = Integer.parseInt(info.args[1]);
                int max = Integer.parseInt(info.args[2]);
                boolean hard = info.args.length > 3 && info.args[3].equalsIgnoreCase("hard");
                BookMeta m = (BookMeta) plr.getInventory().getItemInMainHand().getItemMeta();
                List<String> text = new ArrayList<>();
                if(m != null) text = m.getPages();
                else plr.sendMessage(ChatColor.RED+"Could not get contents of book! Task is empty");
                LTask task = new LTask(info.args[0], hard, min, max, text.toArray(new String[0]));
                addTask(task);
                task.addTaskMeta(plr.getInventory().getItemInMainHand(), null);
                String h = hard ? " hard" : "";
                info.sender.sendMessage(ChatColor.GREEN+"Successfully created"+h+" task "+info.args[0]+"!");
            }
        });
        remove.setTabCompletionType(TabCompletionType.CUSTOM);
        remove.setCustomTabCompletion(info -> {
            List<String> finalList = new ArrayList<>();
            List<String> list = getTaskIDs();
            StringUtil.copyPartialMatches(info.args[info.args.length-1], list, finalList);
            return finalList;
        });
        remove.setFunction(info -> {
            if(info.args.length < 1){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+command.getName()+" task remove <name>");
                return;
            }
            removeTask(info.args[0]);
            info.sender.sendMessage(ChatColor.GREEN+"Removed task "+info.args[0]);
        });
        get.setTabCompletionType(TabCompletionType.ONLINE_PLAYERS);
        get.setFunction(info -> {
            if(info.args.length < 1){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+command+" task get <player>");
                return;
            }
            String name = info.args[0];
            Player plr = Bukkit.getPlayer(name);
            if(plr == null){
                info.sender.sendMessage(ChatColor.RED+name+" must be online in order to get their task");
                return;
            }
            getAssignedTask(plr.getUniqueId()).ifPresent(task -> {
                Player pl = (Player) info.sender;
                ItemStack i = new ItemStack(Material.WRITTEN_BOOK);
                task.addTaskMeta(i, plr);
                pl.getInventory().addItem(i);
            });
        });
        set.setTabCompletionType(TabCompletionType.CUSTOM);
        set.setCustomTabCompletion(info -> {
            List<String> finalList = new ArrayList<>();
            List<String> list = new ArrayList<>();
            if(info.args.length == 3){
                List<String> fl = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(p -> fl.add(p.getName()));
                list = fl;
            } else if(info.args.length == 4) {
                list = getTaskIDs();
            }
            StringUtil.copyPartialMatches(info.args[info.args.length-1], list, finalList);
            return finalList;
        });
        set.setFunction(info -> {
            if(info.args.length < 2){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+command.getName()+" task set <player> <task>");
                return;
            }
            String name = info.args[0];
            String task = info.args[1];
            Player plr = Bukkit.getPlayer(name);
            if(plr == null) {
                info.sender.sendMessage(ChatColor.RED+name+" must be online in order to assign them a task.");
                return;
            }
            assignTask(plr.getUniqueId(), task);
            info.sender.sendMessage(ChatColor.GREEN+"Assigned task "+task+" to "+name+"!");
        });
        if(useSecretKeeper) {
            setup = sc.createSubCommand("setupsk");
            setup.setTabCompletionType(TabCompletionType.EMPTY);
            setup.setFunction(info -> {
                if(settingUpSK){
                    if(settingUpSKPhase == 4){
                        Location l = settingUpSKPlayer.getLocation().getBlock().getLocation();
                        l.add(0.5, 0.5, 0.5);
                        nextSKSetupStep(l);
                    } else {
                        info.sender.sendMessage(ChatColor.RED+settingUpSKPlayer.getName()+" is already setting up the Secret Keeper!");
                    }
                    return;
                }
                settingUpSK = true;
                settingUpSKPlayer = (Player) info.sender;
                settingUpSKPhase = 0;
                nextSKSetupStep(null);
            });
        }
    }

    private void nextSKSetupStep(Location location){
        switch (settingUpSKPhase){
            case 0 -> settingUpSKPlayer.sendMessage(ChatColor.YELLOW+"Punch the block that players interact with to "+ChatColor.GREEN+"succeed task");
            case 1 -> {
                setSucceedButton(location);
                settingUpSKPlayer.sendMessage(ChatColor.YELLOW+"Punch block that players interact with to "+ChatColor.GREEN+"reroll task");
            }
            case 2 -> {
                setRerollButton(location);
                settingUpSKPlayer.sendMessage(ChatColor.YELLOW+"Punch the block that players interact with to "+ChatColor.GREEN+"fail task");
            }
            case 3 -> {
                setFailButton(location);
                settingUpSKPlayer.sendMessage(ChatColor.YELLOW+"Rerun the setup command while standing at the place where you want rewards to spawn");
            }
            case 4 -> {
                setRewardsLocation(location);
                settingUpSKPhase = -1;
                settingUpSK = false;
                settingUpSKPlayer.sendMessage(ChatColor.GREEN+"Finished setting up Secret Keeper!");
                settingUpSKPlayer = null;
            }
        }
        settingUpSKPhase++;
    }

    void enable(LTaskAPI inst){
        p.getServer().getPluginManager().registerEvents(inst, p);
    }

    @EventHandler
    void interactEvent(PlayerInteractEvent e){
        if(e.getAction().equals(Action.LEFT_CLICK_BLOCK) && e.getPlayer().equals(settingUpSKPlayer) && settingUpSK && settingUpSKPhase < 4){
            e.setCancelled(true);
            nextSKSetupStep(e.getClickedBlock().getLocation());
        }
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !cd){
            if(e.getClickedBlock().getLocation().equals(getSucceedButton())){
                succeed(e.getPlayer().getUniqueId());
                cd = true;
                Bukkit.getScheduler().runTaskLater(p, () -> cd = false, 1);
            }
            if(e.getClickedBlock().getLocation().equals(getRerollButton())){
                reroll(e.getPlayer().getUniqueId());
                cd = true;
                Bukkit.getScheduler().runTaskLater(p, () -> cd = false, 1);
            }
            if(e.getClickedBlock().getLocation().equals(getFailButton())){
                fail(e.getPlayer().getUniqueId());
                cd = true;
                Bukkit.getScheduler().runTaskLater(p, () -> cd = false, 1);
            }
        }
    }

    public void save(){
        File f1 = data.unsafe().createIfNull(new File(p.getDataFolder(), "active_tasks.json"));
        File f2 = data.unsafe().createIfNull(new File(p.getDataFolder(), "tasks.json"));
        data.unsafe().writeData(f1, data.GSON.toJson(activeTasks, new TypeToken<TreeMap<UUID, String>>(){}.getType()));
        data.unsafe().writeData(f2, data.GSON.toJson(allTasks, new TypeToken<TreeMap<String, LTask>>(){}.getType()));
        if(useSecretKeeper){
            data.save("secret_keeper", skWrapper);
        }
    }
    public void setOnFail(BiConsumer<UUID, LTask> failLogic){
        this.failLogic = failLogic;
    }
    public void setOnSucceed(BiConsumer<UUID, LTask> winLogic){
        this.winLogic = winLogic;
    }
    public void setOnReroll(BiConsumer<UUID, LTask> rerollLogic){
        this.rerollLogic = rerollLogic;
    }
    public void fail(UUID uuid){
        if(getAssignedTask(uuid).isEmpty()) return;
        Optional.ofNullable(failLogic).ifPresent(consumer -> getAssignedTask(uuid).ifPresent(task -> consumer.accept(uuid, task)));
        activeTasks.remove(uuid);
    }
    public void succeed(UUID uuid){
        if(getAssignedTask(uuid).isEmpty()) return;
        Optional.ofNullable(winLogic).ifPresent(consumer -> getAssignedTask(uuid).ifPresent(task -> consumer.accept(uuid, task)));
        activeTasks.remove(uuid);
    }
    public void reroll(UUID uuid){
        if(getAssignedTask(uuid).isPresent()){
            LTask t = getAssignedTask(uuid).get();
            if(t.isHard()){
                Player p = Bukkit.getPlayer(uuid);
                if(p != null) p.sendMessage(ChatColor.RED+"You cannot reroll with a hard task!");
                return;
            }
        } else {
            return;
        }

        Optional.ofNullable(rerollLogic).ifPresent(consumer -> getAssignedTask(uuid).ifPresent(task -> consumer.accept(uuid, task)));
        activeTasks.remove(uuid);
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
    public List<String> getTaskIDs(){
        List<String> tasks = new ArrayList<>();
        allTasks.forEach((s, task) -> tasks.add(s));
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
    public LTask getTask(int value){
        List<LTask> tasks = getTasks(value);
        int r = random.nextInt(0, tasks.size());
        return tasks.get(r);
    }
    public void assignTask(UUID player, int value, boolean hard){
        List<LTask> tasks = new ArrayList<>();
        getTasks(value).forEach(task -> {
            if(task.isHard() == hard) tasks.add(task);
        });
        if(tasks.isEmpty()) {
            Player p = Bukkit.getPlayer(player);
            String t = hard ? "hard" : "non-hard";
            if(p != null) p.sendMessage(ChatColor.RED+"Cannot assign task as there are no "+t+" tasks!");
            unassignTask(player);
            return;
        }
        int r = random.nextInt(0, tasks.size());
        LTask task = tasks.get(r);
        activeTasks.put(player, task.getId());
    }
    public void assignTask(UUID player, String id){
        Optional<LTask> task = getTask(id);
        task.ifPresent(t -> activeTasks.put(player, id));
    }
    public Optional<LTask> getAssignedTask(UUID uuid){
        if(activeTasks.get(uuid) == null) return Optional.empty();
        return getTask(activeTasks.get(uuid));
    }
}
