package me.starlitembers.emberslib.data;

import com.google.gson.Gson;
import me.starlitembers.emberslib.EmbersLib;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

public class DataAPI {
    final Plugin p;
    public Gson GSON;

    public DataAPI(Plugin plugin, Gson gson){
        p = plugin;
        GSON = gson.newBuilder().setPrettyPrinting().registerTypeAdapter(SerializableData.class, new SerializableDataTypeAdapter()).create();
    }

    public void registerTypeAdapter(Type type, Object typeAdapter){
        GSON = GSON.newBuilder().registerTypeAdapter(type, typeAdapter).create();
    }

    public <T extends SerializableData> T get(String name, Class<T> clazz){
        File file = unsafe().createIfNull(new File(p.getDataFolder(), name+".json"));
        T data = GSON.fromJson(unsafe().readData(file), clazz);
        if(data == null) {
            try {
                data = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                EmbersLib.plugin().getLogger().severe("SerializableData must have a public constructor with no args!");
                throw new RuntimeException(e);
            }
        } else {
            data.onDeserialize();
        }
        return data;
    }

    public <T extends SerializableData> void save(String name, T data){
        data.onSerialize();
        unsafe().writeData(unsafe().createIfNull(new File(p.getDataFolder(), name+".json")), GSON.toJson(data));
    }

    @Deprecated
    public Unsafe unsafe(){
        return new Unsafe();
    }

    @Deprecated
    public static class Unsafe{
        private Unsafe(){

        }
        public String readData(File file){
            try {
                FileReader fstream = new FileReader(file);
                BufferedReader reader = new BufferedReader(fstream);
                StringBuilder builder = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null){
                    builder.append(line);
                }
                reader.close();
                fstream.close();
                return builder.toString();
            } catch (IOException e){
                EmbersLib.plugin().getLogger().severe(e.getMessage());
                return null;
            }
        }
        public void writeData(File file, String data){
            try {
                FileWriter fstream = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fstream);
                writer.write(data);
                writer.close();
            } catch (IOException e){
                EmbersLib.plugin().getLogger().severe(e.getMessage());
            }
        }
        public File createIfNull(File f){
            if(!f.exists()){
                f.getParentFile().mkdirs();
            }
            return f;
        }
    }
}
