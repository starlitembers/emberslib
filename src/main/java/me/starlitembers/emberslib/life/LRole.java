package me.starlitembers.emberslib.life;

import me.starlitembers.emberslib.EmbersLib;
import me.starlitembers.emberslib.data.SerializableData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LRole {
    String id;
    String displayname;
    String description;
    String plugin;
    List<UUID> playersWithRole;
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
    private LRole(LRole lRole){
        this.id = lRole.id+"_copy";
        this.displayname = lRole.displayname;
        this.description = lRole.description;
        this.playersWithRole = lRole.playersWithRole;
        this.plugin = lRole.plugin;
        this.data = lRole.data;
        this.dataTypeRaw = lRole.dataTypeRaw;
        this.dataType = lRole.dataType;
    }
    <T extends SerializableData> LRole(String roleId, String roleName, String roleDescription, Plugin plugin, T data){
        this.plugin = plugin.getName();
        this.id = roleId;
        this.displayname = roleName;
        this.description = roleDescription;
        playersWithRole = new ArrayList<>();
        this.data = data;
        if(data == null) dataTypeRaw = null;
        else this.dataTypeRaw = data.getClass();
    }
    void initIfNull(){
        if(this.id == null) this.id = "MISSING_ID";
        if(this.displayname == null) this.displayname = ChatColor.LIGHT_PURPLE+"[ROLE NAME]";
        if(this.description == null) this.description = ChatColor.LIGHT_PURPLE+"[ROLE DESCRIPTION]";
        if(this.playersWithRole == null) playersWithRole = new ArrayList<>();
        if(this.plugin == null) this.plugin = EmbersLib.plugin().getName();
    }
    public void addPlayer(UUID player){
        if(!playersWithRole.contains(player)) playersWithRole.add(player);
    }
    public void addPlayer(Player player){
        addPlayer(player.getUniqueId());
    }
    public void removePlayer(UUID player){
        playersWithRole.remove(player);
    }
    public void removePlayer(Player player){
        removePlayer(player.getUniqueId());
    }
    public void copyPlayersFromRole(LRole lRoleToCopy){
        this.playersWithRole = lRoleToCopy.playersWithRole;
    }
    public void copyDataFromRole(LRole lRoleToCopy){
        this.data = lRoleToCopy.data;
    }
    public void copyNameFromRole(LRole lRoleToCopy){
        this.displayname = lRoleToCopy.displayname;
    }
    public void copyDescriptionFromRole(LRole lRoleToCopy){
        this.description = lRoleToCopy.description;
    }
    public boolean hasRole(UUID player){
        return playersWithRole.contains(player);
    }
    public boolean hasRole(Player player){
        return playersWithRole.contains(player.getUniqueId());
    }
    public String getId(){
        return id;
    }
    public String getName(){
        return displayname;
    }
    public String getDescription(){
        return description;
    }
    public List<UUID> getPlayers(){
        return playersWithRole;
    }
    public Plugin getPlugin(){
        return Bukkit.getPluginManager().getPlugin(plugin);
    }
    public void setName(String name){
        this.displayname = name;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public LRole copy(){
        return new LRole(this);
    }
}
