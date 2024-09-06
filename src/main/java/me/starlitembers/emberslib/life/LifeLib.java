package me.starlitembers.emberslib.life;

import me.starlitembers.emberslib.EmbersLib;
import me.starlitembers.emberslib.CreditsCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.TreeMap;

/**
 * The Life Instance Registry
 */
public class LifeLib {
    private static EmbersLib lib;
    private static Plugin p;

    private static final TreeMap<String, LifeInstance> registry = new TreeMap<>();

    /**
     * @param sender The player or console who ran the /credits command.
     */
    public static void sendCreditsMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD+"Credits:");
        sender.sendMessage(ChatColor.BLUE+"EmbersLib ["+p.getDescription().getVersion()+"]"+ChatColor.RESET+": By "+ChatColor.LIGHT_PURPLE+"starlitembers");
        registry.forEach((s, lf) -> {
            ChatColor c = lf.isEnabled() ? ChatColor.GREEN : ChatColor.RED;
            sender.sendMessage(c+lf.getPlugin().getName()+" ["+lf.getVersion()+"]"+ChatColor.RESET+": "+lf.getCredits());
        });
    }

    public static void onLoad(EmbersLib lib){
        p = lib;
        LifeLib.lib = lib;
    }

    /**
     * Enables all life instances.
     */
    public static void onEnable(){
        registry.forEach((s, lf) -> {
            lf.enable();
            lf.setEnabled(true);
        });
        lib.getCommand("credits").setExecutor(new CreditsCommand());
    }

    /**
     * Disables all life instances
     */
    public static void onDisable(){
        registry.forEach((s, lf) -> {
            lf.disable();
            lf.setEnabled(false);
        });
    }

    /**
     * Removes a life instance from the registry. Does not disable the Life Plugin.
     * @param id ID of the Life Instance to remove
     */
    public static void removeLifeInstance(String id){
        registry.remove(id);
        p.getLogger().info("Unoaded Life Plugin: ["+id+"]");
    }

    /**
     * Adds a life instance to the registry.
     * <br>
     * Called automatically when instantiating a Life Instance.
     * @param id ID of the Life Instance
     * @param instance The Life Instance itself
     */
    public static void addLifeInstance(String id, LifeInstance instance) {
        registry.put(id, instance);
        p.getLogger().info("Loaded Life Plugin: ["+id+"]");
    }

    /**
     * Gets a life instance from the registry
     * @param id ID of the Life Instance
     * @return The Life Instance from the provided id, null if it can't find it.
     */
    public static LifeInstance getLifeInstance(String id){
        return registry.get(id);
    }
}
