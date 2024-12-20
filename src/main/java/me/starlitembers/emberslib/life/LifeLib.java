package me.starlitembers.emberslib.life;

import me.starlitembers.emberslib.EmbersLib;
import me.starlitembers.emberslib.CreditsCommand;
import me.starlitembers.emberslib.PluginCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
 * The Life Instance Registry
 */
public class LifeLib {
    private static EmbersLib lib;
    private static Plugin p;

    public static void enable(String id){
        try {
            Plugin p = Bukkit.getPluginManager().loadPlugin(getLifeInstance(id).file);
            p.onLoad();
            getLifeInstance(id).setEnabled(true);
            Bukkit.getPluginManager().enablePlugin(p);
            getLifeInstance(id).enable();
        } catch (InvalidPluginException | InvalidDescriptionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void disable(String id){
        getLifeInstance(id).setEnabled(false);
        getLifeInstance(id).getTimeAPI().forEach((s, lTimer) -> lTimer.setPaused(true));
        Bukkit.getScheduler().cancelTasks(getLifeInstance(id).getPlugin());
        getLifeInstance(id).disable();
        getLifeInstance(id).getCommandAPI().clear();
        getLifeInstance(id).getCommandAPI().sync();
        Bukkit.getPluginManager().disablePlugin(getLifeInstance(id).getPlugin());
    }

    private static final TreeMap<String, LifeInstance> registry = new TreeMap<>();

    /**
     * @param sender The player or console who ran the /credits command.
     */
    public static void sendCreditsMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD+"Credits:");
        sender.sendMessage(ChatColor.BLUE+"EmbersLib ["+p.getDescription().getVersion()+"]"+ChatColor.RESET+": By "+ChatColor.LIGHT_PURPLE+"AshlikeDreams");
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
        p.saveDefaultConfig();
        boolean enableByDefault = p.getConfig().getBoolean("enabled-on-startup");
        registry.forEach((s, lf) -> {
            lf.enable();
            lf.setEnabled(true);
        });
        if(!enableByDefault){
            Bukkit.getScheduler().runTaskLater(p, () -> registry.forEach((s, lf) -> {
                lf.disable();
                lf.setEnabled(false);
                disable(s);
            }), 1);
        }
        lib.getCommand("credits").setExecutor(new CreditsCommand());
        lib.getCommand("plugin").setExecutor(new PluginCommand());
        lib.getCommand("plugin").setTabCompleter(new PluginCommand());
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

    public static Collection<LifeInstance> getInstances(){
        Collection<LifeInstance> list = new ArrayList<>();
        registry.forEach((s, l) -> list.add(l));
        return list;
    }

    public static Collection<String> getInstanceNames(){
        Collection<String> list = new ArrayList<>();
        registry.forEach((s, l) -> list.add(s));
        return list;
    }
}
