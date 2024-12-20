package me.starlitembers.emberslib.life;

import com.google.gson.reflect.TypeToken;
import me.starlitembers.emberslib.command.BaseCommand;
import me.starlitembers.emberslib.command.SubCommand;
import me.starlitembers.emberslib.command.TabCompletionType;
import me.starlitembers.emberslib.data.DataAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class LivesAPI implements Listener {
    final Plugin p;
    final DataAPI data;
    final LColorAPI colorAPI;
    BiFunction<PlayerDeathEvent, Integer, Integer> onDeath;
    HashMap<UUID, Integer> playerLivesHashmap = new HashMap<>();
    int startingLives;
    public LivesAPI(Plugin plugin, DataAPI data, LColorAPI colorAPI){
        p = plugin;
        this.data = data;
        this.colorAPI = colorAPI;
        File f = data.unsafe().createIfNull(new File(p.getDataFolder(), "lives.json"));
        boolean exists = f.exists();
        if(!exists){
            data.unsafe().writeData(f, data.GSON.toJson(playerLivesHashmap));
        }
        playerLivesHashmap = data.GSON.fromJson(data.unsafe().readData(f), new TypeToken<Map<UUID, Integer>>(){}.getType());
        if(playerLivesHashmap == null){
            playerLivesHashmap = new HashMap<>();
        }
        startingLives = 3;
    }
    public void registerLivesCommand(BaseCommand cmd){
        SubCommand livesc = cmd.createSubCommand("lives");
        livesc.setTabCompletionType(TabCompletionType.SUB_COMMANDS);
        livesc.setFunction(info -> info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" lives <add/remove/get/set>"));

        livesc.createSubCommand("add");
        livesc.createSubCommand("remove");
        livesc.createSubCommand("get");
        livesc.createSubCommand("set");

        SubCommand laddsc = livesc.getSubCommand("add");
        SubCommand lremovesc = livesc.getSubCommand("remove");
        SubCommand lgetsc = livesc.getSubCommand("get");
        SubCommand lsetsc = livesc.getSubCommand("set");

        laddsc.setTabCompletionType(TabCompletionType.ONLINE_PLAYERS);
        lremovesc.setTabCompletionType(TabCompletionType.ONLINE_PLAYERS);
        lgetsc.setTabCompletionType(TabCompletionType.ONLINE_PLAYERS);
        lsetsc.setTabCompletionType(TabCompletionType.ONLINE_PLAYERS);

        laddsc.setFunction(info -> {
            if(info.args.length != 2){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" lives add <player> <amount>");
                return;
            }
            String name = info.args[0];
            int amount;
            try {
                amount = Integer.parseInt(info.args[1]);
            } catch (Exception e){
                info.sender.sendMessage(ChatColor.RED+"Amount must be a number!");
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" lives add <player> <amount>");
                return;
            }
            Player p = Bukkit.getPlayer(name);
            if(p == null){
                info.sender.sendMessage(ChatColor.RED+name+" must be online in order to change their lives!");
                return;
            }
            setPlayerLives(p, getPlayerLives(p) + amount);
            updateColor(p);
            info.sender.sendMessage(ChatColor.GREEN+"Added "+amount+" lives to "+name+"!");
        });
        lremovesc.setFunction(info -> {
            if(info.args.length != 2){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" lives remove <player> <amount>");
                return;
            }
            String name = info.args[0];
            int amount;
            try {
                amount = Integer.parseInt(info.args[1]);
            } catch (Exception e){
                info.sender.sendMessage(ChatColor.RED+"Amount must be a number!");
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" lives remove <player> <amount>");
                return;
            }
            Player p = Bukkit.getPlayer(name);
            if(p == null){
                info.sender.sendMessage(ChatColor.RED+name+" must be online in order to change their lives!");
                return;
            }
            setPlayerLives(p, getPlayerLives(p) - amount);
            updateColor(p);
            info.sender.sendMessage(ChatColor.GREEN+"Removed "+amount+" lives from "+name+"!");
        });
        lgetsc.setFunction(info -> {
            if(info.args.length != 1){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" lives get <player>");
                return;
            }
            String name = info.args[0];
            Player p = Bukkit.getPlayer(name);
            if(p == null){
                info.sender.sendMessage(ChatColor.RED+name+" must be online in order to get their life count!");
                return;
            }
            info.sender.sendMessage(ChatColor.GREEN+name+" has "+getPlayerLives(p)+" lives!");
        });
        lsetsc.setFunction(info -> {
            if(info.args.length != 2){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" lives set <player> <amount>");
                return;
            }
            String name = info.args[0];
            int amount;
            try {
                amount = Integer.parseInt(info.args[1]);
            } catch (Exception e){
                info.sender.sendMessage(ChatColor.RED+"Amount must be a number!");
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" lives set <player> <amount>");
                return;
            }
            Player p = Bukkit.getPlayer(name);
            if(p == null){
                info.sender.sendMessage(ChatColor.RED+name+" must be online in order to change their lives!");
                return;
            }
            setPlayerLives(p, amount);
            updateColor(p);
            info.sender.sendMessage(ChatColor.GREEN+"Set "+name+" to "+amount+" lives!");
        });
    }
    @EventHandler
    void onDeath(PlayerDeathEvent event){
        Player p = event.getEntity();
        int lives = getPlayerLives(p);
        Optional.ofNullable(onDeath).ifPresent(bifunction -> setPlayerLives(p, bifunction.apply(event, lives)));
        if(colorAPI != null) updateColor(event.getEntity());
    }
    public void setDeathFunction(BiFunction<PlayerDeathEvent, Integer, Integer> function){
        this.onDeath = function;
    }
    public void updateColor(Player player){
        colorAPI.updatePlayerColor(player, this);
    }
    public void updateColor(UUID player){
        colorAPI.updatePlayerColor(player, this);
    }
    public void save(){
        File f = new File(p.getDataFolder(), "lives.json");
        data.unsafe().writeData(f, data.GSON.toJson(playerLivesHashmap));
    }
    public int getPlayerLives(Player player){
        if(!playerLivesHashmap.containsKey(player.getUniqueId())){
            setPlayerLives(player.getUniqueId(), startingLives);
        }
        return playerLivesHashmap.get(player.getUniqueId());
    }
    public int getPlayerLives(UUID player){
        if(!playerLivesHashmap.containsKey(player)){
            setPlayerLives(player, startingLives);
        }
        return playerLivesHashmap.get(player);
    }
    @EventHandler
    void onJoin(PlayerJoinEvent event){
        updateColor(event.getPlayer());
    }
    public void setPlayerLives(UUID player, int amount){
        playerLivesHashmap.put(player, amount);
    }
    public void setPlayerLives(Player player, int amount){
        setPlayerLives(player.getUniqueId(), amount);
    }
    public void addLife(Player player){
        setPlayerLives(player, getPlayerLives(player)+1);
    }
    public void addLife(UUID player){
        setPlayerLives(player, getPlayerLives(player)+1);
    }
    public void removeLife(Player player){
        setPlayerLives(player, getPlayerLives(player)-1);
    }
    public void removeLife(UUID player){
        setPlayerLives(player, getPlayerLives(player)-1);
    }
    public void addLives(Player player, int amount){
        setPlayerLives(player, getPlayerLives(player)+amount);
    }
    public void addLives(UUID player, int amount){
        setPlayerLives(player, getPlayerLives(player)+amount);
    }
    public void removeLives(Player player, int amount){
        setPlayerLives(player, getPlayerLives(player)-amount);
    }
    public void removeLives(UUID player, int amount){
        setPlayerLives(player, getPlayerLives(player)-amount);
    }
    public int getStartingLives(){
        return startingLives;
    }
    public void setStartingLives(int amount){
        startingLives = amount;
    }

    void enable() {
        p.getServer().getPluginManager().registerEvents(this, p);
    }
}
