package me.starlitembers.emberslib.data;

import me.starlitembers.emberslib.EmbersLib;

public class PlayerData {
    private SerializableData data;
    <T extends SerializableData> PlayerData(T data){
        this.data = data;
    }
    void serialize(){
        if(data == null) return;
        data.onSerialize();
    }
    void deserialize(){
        if(data == null) return;
        data.onDeserialize();
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
    void initIfNull(){

    }
}
