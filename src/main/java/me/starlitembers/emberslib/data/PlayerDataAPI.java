package me.starlitembers.emberslib.data;

import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class PlayerDataAPI {
    final Plugin p;
    DataAPI data;
    HashMap<UUID, PlayerData> playerData;
    public PlayerDataAPI(Plugin plugin, DataAPI data){
        p = plugin;
        this.data = data;
        File f = data.unsafe().createIfNull(new File(p.getDataFolder(), "playerdata.json"));
        boolean exists = f.exists();
        if(!exists){
            data.unsafe().writeData(f, data.GSON.toJson(playerData));
        }
        playerData = data.GSON.fromJson(data.unsafe().readData(f), new TypeToken<HashMap<UUID, PlayerData>>(){}.getType());
        if(playerData == null){
            playerData = new HashMap<>();
        }
        playerData.forEach((k, v) -> {
            v.initIfNull();
            v.deserialize();
        });
    }
    public void save(){
        playerData.forEach((k, v) -> v.serialize());
        File f = new File(p.getDataFolder(), "playerdata.json");
        data.unsafe().writeData(f, data.GSON.toJson(playerData, new TypeToken<Map<UUID, PlayerData>>(){}.getType()));
    }
    public <T extends SerializableData> PlayerData create(UUID player, T data){
        playerData.put(player, new PlayerData(data));
        return playerData.get(player);
    }
    public <T extends SerializableData> PlayerData create(Player player, T data){
        return create(player.getUniqueId(), data);
    }
    public <T extends SerializableData> PlayerData createIfAbsent(UUID player, T data){
        if(!playerData.containsKey(player)) return create(player, data);
        else return playerData.get(player);
    }
    public <T extends SerializableData> PlayerData createIfAbsent(Player player, T data){
        return createIfAbsent(player.getUniqueId(), data);
    }
    public <T extends SerializableData> T getPlayerData(UUID player, Class<T> clazz){
        if(!playerData.containsKey(player)) return null;
        return playerData.get(player).getData(clazz);
    }
    public <T extends SerializableData> T getPlayerData(Player player, Class<T> clazz){
        return getPlayerData(player.getUniqueId(), clazz);
    }
    public PlayerData getPlayerDataRaw(UUID player){
        return playerData.get(player);
    }
    public PlayerData getPlayerDataRaw(Player player){
        return getPlayerDataRaw(player.getUniqueId());
    }
    public void forEach(BiConsumer<UUID, PlayerData> consumer){
        playerData.forEach(consumer);
    }
}
