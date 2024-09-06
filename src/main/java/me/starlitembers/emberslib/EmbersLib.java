package me.starlitembers.emberslib;

import me.starlitembers.emberslib.life.LifeLib;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class EmbersLib extends JavaPlugin {
    private static Plugin p;
    private static EmbersLib inst;

    public static EmbersLib instance(){
        return inst;
    }

    public static Plugin plugin(){
        return p;
    }

    @Override
    public void onLoad(){
        p = this;

        LifeLib.onLoad(this);
    }
    @Override
    public void onEnable(){
        LifeLib.onEnable();
    }
    @Override
    public void onDisable(){
        LifeLib.onDisable();
    }
}
