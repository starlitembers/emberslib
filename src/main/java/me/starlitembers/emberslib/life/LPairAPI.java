package me.starlitembers.emberslib.life;

import com.google.gson.reflect.TypeToken;
import me.starlitembers.emberslib.data.DataAPI;
import me.starlitembers.emberslib.data.SerializableData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class LPairAPI {
    final Plugin p;
    DataAPI data;
    HashMap<UUID, LPair> pairs;
    public LPairAPI(Plugin plugin, DataAPI data){
        this.p = plugin;
        this.data = data;
        File f = data.unsafe().createIfNull(new File(p.getDataFolder(), "pairs.json"));
        boolean exists = f.exists();
        if(!exists){
            data.unsafe().writeData(f, data.GSON.toJson(pairs));
        }
        pairs = data.GSON.fromJson(data.unsafe().readData(f), new TypeToken<HashMap<UUID, LPair>>(){}.getType());
        if(pairs == null){
            pairs = new HashMap<>();
        }
        forEach((k, v) -> {
            v.initIfNull();
            v.deserialize();
        });
    }
    public void save(){
        forEach((id, lPair) -> lPair.serialize());
        File f = new File(p.getDataFolder(), "pairs.json");
        data.unsafe().writeData(f, data.GSON.toJson(pairs, new TypeToken<HashMap<UUID, LPair>>(){}.getType()));
    }
    public LPair getPairById(UUID pairId){
        return pairs.get(pairId);
    }
    public LPair getPairByPlayer(UUID player){
        AtomicReference<UUID> chosen = new AtomicReference<>(null);
        forEach((uuid, lPair) -> {
            if(lPair.hasPlayer(player) && chosen.get() == null){
                chosen.set(uuid);
            }
        });
        return getPairById(chosen.get());
    }
    public <T extends SerializableData> void distributePairs(List<Player> playersToChoose, boolean allowDuplicates, Class<T> dataType){
        boolean assigning = true;
        Random random = new Random();
        if(!allowDuplicates){
            List<Player> toRemove = new ArrayList<>();
            playersToChoose.forEach((p) -> {
                if(getPairByPlayer(p) != null) toRemove.add(p);
            });
            toRemove.forEach(playersToChoose::remove);
        }
        while (assigning){
            if(playersToChoose.size() < 2){
                assigning = false;
                continue;
            }
            Player p1 = playersToChoose.get(random.nextInt(0, playersToChoose.size()));
            Player p2 = playersToChoose.get(random.nextInt(0, playersToChoose.size()));
            if(p1 == p2) continue;
            try {
                if(dataType != null) createPair(p1, p2, (T) dataType.getDeclaredConstructors()[0].newInstance());
                else createPair(p1, p2, null);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            playersToChoose.remove(p1);
            playersToChoose.remove(p2);
        }
    }
    public <T extends SerializableData> void distributePair(List<Player> playersToChoose, boolean allowDuplicates, T data){
        boolean choosing = true;
        Random random = new Random();
        if(!allowDuplicates){
            List<Player> toRemove = new ArrayList<>();
            playersToChoose.forEach((p) -> {
                if(getPairByPlayer(p) != null) toRemove.add(p);
            });
            toRemove.forEach(playersToChoose::remove);
        }
        while (choosing){
            if(playersToChoose.size() < 2){
                choosing = false;
                continue;
            }
            Player p1 = playersToChoose.get(random.nextInt(0, playersToChoose.size()));
            Player p2 = playersToChoose.get(random.nextInt(0, playersToChoose.size()));
            if(p1 == p2) continue;
            createPair(p1, p2, data);
            choosing = false;
        }
    }
    public LPair getPairByPlayer(Player player){
        return getPairByPlayer(player.getUniqueId());
    }
    public <T extends SerializableData> LPair createPair(UUID player1, UUID player2, T data){
        LPair pair = new <T>LPair(player1, player2, UUID.randomUUID(), data);
        pairs.put(pair.pairId, pair);
        return pair;
    }
    public <T extends SerializableData> LPair createPair(Player player1, Player player2, T data){
        return createPair(player1.getUniqueId(), player2.getUniqueId(), data);
    }
    public void removePairById(UUID pairId){
        pairs.remove(pairId);
    }
    public void removePairByPlayer(UUID player){
        AtomicReference<UUID> idToRemove = new AtomicReference<>(null);
        forEach((uuid, lPair) -> {
            if(lPair.hasPlayer(player)){
                idToRemove.set(uuid);
            }
        });
        removePairById(idToRemove.get());
    }
    public void removePairByPlayer(Player player){
        removePairByPlayer(player.getUniqueId());
    }
    public void forEach(BiConsumer<UUID, LPair> consumer){
        pairs.forEach(consumer);
    }
}
