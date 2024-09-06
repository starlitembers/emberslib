package me.starlitembers.emberslib.life;

import me.starlitembers.emberslib.EmbersLib;
import me.starlitembers.emberslib.data.SerializableData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.util.UUID;

public class LPair {
    UUID player1;
    UUID player2;
    final UUID pairId;
    int selectedPlayer = 0;
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
    private LPair(){
        pairId = UUID.randomUUID();
    }
    public LPair copy(boolean newId){
        LPair pair = new LPair(player1, player2, newId ? UUID.randomUUID() : pairId, (SerializableData) data);
        return pair;
    }
    <T extends SerializableData> LPair(UUID player1, UUID player2, UUID pairId, T data){
        this.player1 = player1;
        this.player2 = player2;
        this.pairId = pairId;
        this.data = data;
        if(data == null) dataTypeRaw = null;
        else this.dataTypeRaw = data.getClass();
    }
    public <T extends SerializableData> T getData(Class<T> clazz){
        try {
            return clazz.cast(data);
        } catch (ClassCastException e){
            EmbersLib.plugin().getLogger().severe("Class Cast Exception! ["+ data.getClass().getName()+" <- "+clazz.getName()+"]");
            return null;
        }
    }
    void initIfNull(){

    }
    public UUID getId(){
        return pairId;
    }
    public UUID getPlayer1Id(){
        return player1;
    }
    public UUID getPlayer2Id(){
        return player2;
    }
    public Player getPlayer1(){
        return Bukkit.getPlayer(player1);
    }
    public Player getPlayer2(){
        return Bukkit.getPlayer(player2);
    }
    public boolean hasPlayer(UUID player){
        return player.equals(player1) || player.equals(player2);
    }
    public boolean hasPlayer(Player player){
        return hasPlayer(player.getUniqueId());
    }
    public UUID getOtherPlayerId(UUID player){
        if(hasPlayer(player)){
            return player.equals(player1) ? player2 : player1;
        }
        return null;
    }
    public UUID getOtherPlayerId(Player player){
        return getOtherPlayerId(player.getUniqueId());
    }
    public Player getOtherPlayer(UUID player){
        return Bukkit.getPlayer(getOtherPlayerId(player));
    }
    public Player getOtherPlayer(Player player){
        return Bukkit.getPlayer(getOtherPlayerId(player));
    }
    public void cycleSelectedPlayer(){
        selectedPlayer = selectedPlayer == 0 ? 1 : 0;
    }
    public UUID getSelectedPlayerId(){
        return selectedPlayer == 0 ? player1 : player2;
    }
    public Player getSelectedPlayer(){
        return Bukkit.getPlayer(getSelectedPlayerId());
    }
}
