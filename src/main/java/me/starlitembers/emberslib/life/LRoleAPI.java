package me.starlitembers.emberslib.life;

import com.google.gson.reflect.TypeToken;
import me.starlitembers.emberslib.data.DataAPI;
import me.starlitembers.emberslib.data.SerializableData;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class LRoleAPI {
    final Plugin p;
    final DataAPI data;
    HashMap<String, LRole> roles;
    public LRoleAPI(Plugin plugin, DataAPI data){
        this.p = plugin;
        this.data = data;
        File f = data.unsafe().createIfNull(new File(p.getDataFolder(), "roles.json"));
        boolean exists = f.exists();
        if(!exists){
            data.unsafe().writeData(f, data.GSON.toJson(roles));
        }
        roles = data.GSON.fromJson(data.unsafe().readData(f), new TypeToken<HashMap<String, LRole>>(){}.getType());
        if(roles == null){
            roles = new HashMap<>();
        }
        forEach((k, v) -> {
            v.initIfNull();
            v.deserialize();
        });
    }
    public void save(){
        forEach(((string, lRole) -> lRole.serialize()));
        File f = new File(p.getDataFolder(), "roles.json");
        data.unsafe().writeData(f, data.GSON.toJson(roles, new TypeToken<Map<String, LRole>>(){}.getType()));
    }
    public void distributeRole(List<Player> playersToRoll, int amount, boolean allowSpectators, boolean allowDuplicates, String roleId){
        Random random = new Random();
        for(int i = 0; i < amount; i++){
            boolean chosen = false;
            int tries = 0;
            Player chosenPlayer;
            LRole role = getRole(roleId);
            while(!chosen){
                int rng = random.nextInt(0, playersToRoll.size());
                chosenPlayer = playersToRoll.get(rng);
                if(!(role.hasRole(chosenPlayer) && !allowDuplicates)){
                    if(!(chosenPlayer.getGameMode() == GameMode.SPECTATOR && !allowSpectators)){
                        chosen = true;
                        if(!allowDuplicates) playersToRoll.remove(rng);
                        role.addPlayer(chosenPlayer);
                    }
                }
                if(tries > 100) chosen = true;
                tries++;
            }
        }
    }
    public void distributeRole(List<Player> playersToRoll, int amount, int minLives, int maxLives, boolean allowSpectators, boolean allowDuplicates, String roleId, LivesAPI lives){
        Random random = new Random();
        for(int i = 0; i < amount; i++){
            boolean chosen = false;
            int tries = 0;
            Player chosenPlayer;
            LRole role = getRole(roleId);
            while(!chosen){
                if(playersToRoll.isEmpty()){
                    return;
                }
                int rng = random.nextInt(0, playersToRoll.size());
                chosenPlayer = playersToRoll.get(rng);
                int lifeCount = lives.getPlayerLives(chosenPlayer);
                if(lifeCount >= minLives && lifeCount <= maxLives){
                    if(!(role.hasRole(chosenPlayer) && !allowDuplicates)){
                        if(!(chosenPlayer.getGameMode() == GameMode.SPECTATOR && !allowSpectators)){
                            chosen = true;
                            if(!allowDuplicates) playersToRoll.remove(rng);
                            role.addPlayer(chosenPlayer);
                        }
                    }
                }
                if(tries > 100) chosen = true;
                tries++;
            }
        }
    }
    public void distributeRoles(List<Player> playersToRoll, boolean allowDuplicates, boolean allowMultiple, String... roleIds){
        Random random = new Random();
        int amount = roleIds.length;
        for(int i = 0; i < amount; i++){
            boolean chosen = false;
            int tries = 0;
            LRole role = getRole(roleIds[i]);
            Player chosenPlayer = null;
            while(!chosen){
                int rng = random.nextInt(0, playersToRoll.size());
                chosenPlayer = playersToRoll.get(rng);
                if(!(role.hasRole(chosenPlayer) && !allowDuplicates)){
                    if(!(playerHasAnyRole(chosenPlayer) && !allowMultiple)){
                        chosen = true;
                        if(!allowDuplicates && !allowMultiple) playersToRoll.remove(rng);
                        role.addPlayer(chosenPlayer);
                    }
                }
                if(tries > 100) chosen = true;
                tries++;
            }
        }
    }
    public LRole getRole(String id){
        return roles.get(id);
    }
    public void addRoleIfAbsent(LRole lRole){
        if(!roles.containsKey(lRole.id)){
            addRole(lRole);
        }
    }
    public void addRolesIfAbsent(LRole... lRoles){
        for(LRole role : lRoles){
            addRoleIfAbsent(role);
        }
    }
    public void addRole(LRole lRole){
        roles.put(lRole.id, lRole);
    }
    public void addRoles(LRole... lRoles){
        for (LRole role : lRoles) {
            addRole(role);
        }
    }
    public void removeRole(String id){
        roles.remove(id);
    }
    @Deprecated
    public <T extends SerializableData> LRole createRole(String id, String name, String description, T data){
        return new LRole(id, name, description, p, data);
    }
    @Deprecated
    public <T extends SerializableData> LRole createRole(String id, String name, T data){
        return new LRole(id, name, ChatColor.LIGHT_PURPLE+"[DESCRIPTION]", p, data);
    }
    public <T extends SerializableData> LRole createRole(String id, T data){
        return new LRole(id, ChatColor.LIGHT_PURPLE+"[NAME]", ChatColor.LIGHT_PURPLE+"[DESCRIPTION]", p, data);
    }
    public void removePlayerRoles(UUID player, LRole... lRoles){
        for(LRole role : lRoles){
            role.removePlayer(player);
        }
    }
    public void removePlayerRoles(Player player, LRole... lRoles){
        for(LRole role : lRoles){
            role.removePlayer(player);
        }
    }
    public void clearPlayerRoles(UUID player){
        forEach((k, v) -> v.removePlayer(player));
    }
    public void clearPlayerRoles(Player player){
        forEach((k, v) -> v.removePlayer(player));
    }
    public boolean playerHasRole(UUID player, LRole... lRoles){
        for (LRole role : lRoles) {
            if (role.hasRole(player)) return true;
        }
        return false;
    }
    public boolean playerHasRole(Player player, LRole... lRoles){
        for (LRole role : lRoles) {
            if (role.hasRole(player)) return true;
        }
        return false;
    }
    public boolean playerHasAnyRole(UUID player){
        AtomicBoolean returnValue = new AtomicBoolean(false);
        forEach((k,v)->{
            if(v.hasRole(player)) returnValue.set(true);
        });
        return returnValue.get();
    }
    public boolean playerHasAnyRole(Player player){
        AtomicBoolean returnValue = new AtomicBoolean(false);
        forEach((k,v)->{
            if(v.hasRole(player)) returnValue.set(true);
        });
        return returnValue.get();
    }
    public void forEach(BiConsumer<String, LRole> consumer){
        roles.forEach(consumer);
    }
}
