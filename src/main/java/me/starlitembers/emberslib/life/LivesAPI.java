package me.starlitembers.emberslib.life;

import com.google.gson.reflect.TypeToken;
import me.starlitembers.emberslib.data.DataAPI;
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
