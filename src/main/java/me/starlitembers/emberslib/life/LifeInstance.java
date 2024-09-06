package me.starlitembers.emberslib.life;

import com.google.gson.Gson;
import me.starlitembers.emberslib.EmbersLib;
import me.starlitembers.emberslib.command.CommandAPI;
import me.starlitembers.emberslib.data.DataAPI;
import me.starlitembers.emberslib.data.PlayerDataAPI;
import me.starlitembers.emberslib.recipe.RecipeAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * The core of any fan-made Life Series twists made using this library.
 */
public abstract class LifeInstance {

    /**
     * Should be created on the plugins onLoad method.
     * @param id The ID of this instance.
     * @param plugin The plugin this instance belongs to.
     */
    public LifeInstance(String id, Plugin plugin){
        this.plugin = plugin;
        LifeLib.addLifeInstance(id, this);
    }

    private final Plugin plugin;
    private boolean enabled = false;
    private LColorAPI colorAPI = null;
    private DataAPI dataAPI = null;
    private LivesAPI livesAPI = null;
    private LTimeAPI timeAPI = null;
    private LRoleAPI roleAPI = null;
    private LPairAPI pairAPI = null;
    private LTaskAPI taskAPI = null;
    private LHealthAPI healthAPI = null;
    private RecipeAPI recipeAPI = null;
    private PlayerDataAPI playerDataAPI = null;
    private CommandAPI commandAPI = null;
    private LBoogeymanAPI boogeymanAPI = null;

    /**
     * Gets the plugin that owns this instance.
     * @return The plugin that owns this instance.
     */
    public Plugin getPlugin(){
        return plugin;
    }

    /**
     * Called when enabling the instance.
     */
    public abstract void onEnable();

    /**
     * Called when disabling the instance.
     */
    public abstract void onDisable();

    /**
     * Gets the credits of this instance
     * @return The author[s] of this instance.
     */
    public abstract String getCredits();

    void disable(){
        if(livesAPI != null) {
            livesAPI.save();
        }
        if(timeAPI != null){
            timeAPI.save();
        }
        if(roleAPI != null){
            roleAPI.save();
        }
        if(pairAPI != null){
            pairAPI.save();
        }
        if(playerDataAPI != null){
            playerDataAPI.save();
        }
        if(taskAPI != null){
            taskAPI.save();
        }
        onDisable();
    }
    void enable(){
        onEnable();
        Bukkit.getScheduler().scheduleSyncDelayedTask(EmbersLib.plugin(), () -> {
            if(colorAPI != null) colorAPI.enable(colorAPI);
            if(livesAPI != null) livesAPI.enable();
            if(healthAPI != null) healthAPI.enable();
            if(boogeymanAPI != null) boogeymanAPI.enable();
            if(timeAPI != null) timeAPI.enable();
        });
    }
    /**
     * Gets if the instance is enabled
     * @return If this instance and owning plugin are enabled.
     */
    public boolean isEnabled(){
        return enabled && getPlugin().isEnabled();
    }


    /**
     * Changes if this instance should be enabled
     * @param enabled
     * If this instance should be enabled.
     */
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
        if(enabled){
            onEnable();
        } else {
            onDisable();
        }
    }


    /**
     * Gets the Color API of this instance.
     * @return The Color API.
     * <br>
     * If null, it creates a new one.
     */
    public LColorAPI getColorAPI() {
        if(colorAPI == null) colorAPI = new LColorAPI(getPlugin());
        return colorAPI;
    }

    /**
     * Gets the Data API of this instance.
     * @return The Data API.
     * <br>
     * If null, it creates a new one.
     */
    public DataAPI getDataAPI() {
        if(dataAPI == null) dataAPI = new DataAPI(getPlugin(), new Gson());
        return dataAPI;
    }

    /**
     * Gets the Lives API of this instance.
     * @return The Lives API.
     * <br>
     * If null, it creates a new one.
     */
    public LivesAPI getLivesAPI() {
        if(livesAPI == null) livesAPI = new LivesAPI(getPlugin(), getDataAPI(), getColorAPI());
        return livesAPI;
    }

    /**
     * Gets the Time API of this instance.
     * @return The Time API.
     * <br>
     * If null, it creates a new one.
     */
    public LTimeAPI getTimeAPI() {
        if(timeAPI == null) timeAPI = new LTimeAPI(getPlugin(), getDataAPI());
        return timeAPI;
    }

    /**
     * Gets the Role API of this instance.
     * @return The Role API.
     * <br>
     * If null, it creates a new one.
     */
    public LRoleAPI getRoleAPI() {
        if(roleAPI == null) roleAPI = new LRoleAPI(getPlugin(), getDataAPI());
        return roleAPI;
    }

    /**
     * Gets the Pair API of this instance.
     * @return The Pair API.
     * <br>
     * If null, it creates a new one.
     */
    public LPairAPI getPairAPI() {
        if(pairAPI == null) pairAPI = new LPairAPI(getPlugin(), getDataAPI());
        return pairAPI;
    }

    /**
     * Gets the Task API of this instance.
     * @return The Task API.
     * <br>
     * If null, it creates a new one.
     */
    public LTaskAPI getTaskAPI(){
        if(taskAPI == null) taskAPI = new LTaskAPI(getPlugin(), getDataAPI());
        return taskAPI;
    }

    /**
     * Gets the Health API of this instance.
     * @return The Health API.
     * <br>
     * If null, it creates a new one.
     */
    public LHealthAPI getHealthAPI(){
        if(healthAPI == null) healthAPI = new LHealthAPI(getPlugin());
        return healthAPI;
    }

    /**
     * Gets the Recipe API of this instance.
     * @return The Recipe API.
     * <br>
     * If null, it creates a new one.
     */
    public RecipeAPI getRecipeAPI() {
        if(recipeAPI == null) recipeAPI = new RecipeAPI(plugin);
        return recipeAPI;
    }

    /**
     * Gets the Player Data API of this instance.
     * @return The Player Data API.
     * <br>
     * If null, it creates a new one.
     */
    public PlayerDataAPI getPlayerDataAPI(){
        if(playerDataAPI == null) playerDataAPI = new PlayerDataAPI(getPlugin(), getDataAPI());
        return playerDataAPI;
    }

    /**
     * Gets the Command API of this instance.
     * @return The Command API.
     * <br>
     * If null, it creates a new one.
     */
    public CommandAPI getCommandAPI(){
        if(commandAPI == null) commandAPI = new CommandAPI(getPlugin());
        return commandAPI;
    }

    /**
     * Gets the Boogeyman API of this instance.
     * @return The Boogeyman API.
     * <br>
     * If null, it creates a new one.
     */
    public LBoogeymanAPI getBoogeymanAPI(){
        if(boogeymanAPI == null) boogeymanAPI = new LBoogeymanAPI(getRoleAPI(), getLivesAPI(), getTimeAPI(), getColorAPI(), getCommandAPI());
        return boogeymanAPI;
    }

    /**
     * Gets the version of this instance.
     * @return The version of this instance.
     */
    public String getVersion(){
        return getPlugin().getDescription().getVersion();
    }
}
