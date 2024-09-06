package me.starlitembers.emberslib.life;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Colored names in the TabList/Chat/NameTag that change depending on lives, time, or some custom value.
 */
public class LColorAPI implements Listener {
    /**
     * Color Rule types.
     */
    public enum Mode{
        LIVES,
        TIME,
        CUSTOM
    }
    final Plugin p;
    TreeMap<Integer, ChatColor> livesColorRule;
    TreeMap<Integer, ChatColor> timeColorRule;
    TreeMap<Integer, ChatColor> customColorRule;
    Mode mode;

    /**
     * @param plugin Plugin to register listeners for
     */
    public LColorAPI(Plugin plugin){
        livesColorRule = new TreeMap<>();
        timeColorRule = new TreeMap<>();
        customColorRule = new TreeMap<>();
        p = plugin;
        mode = Mode.CUSTOM;
    }
    void enable(LColorAPI colorAPI){
        p.getServer().getPluginManager().registerEvents(colorAPI, p);
    }
    /**
     * Gets the current Color Rule type.
     * @return The current Color Rule type.
     */
    public Mode getMode(){
        return mode;
    }

    /**
     * Changes the current Color Rule type.
     * @param mode The Color Rule type.
     */
    public void setMode(Mode mode){
        this.mode = mode;
    }

    /**
     * Gets the Team of a player, and creates one if non-existent.
     * @param uuid UUID of player
     * @param add If the player should be added to the team
     * @return The team of the player
     */
    public Team getOrCreateTeam(UUID uuid, boolean add){
        String s = uuid.toString();
        if(Bukkit.getScoreboardManager() == null) return null;
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Team t = sb.getTeam(s) != null ? sb.getTeam(s) : sb.registerNewTeam(s);
        if(add) t.addEntry(Bukkit.getPlayer(uuid).getName());
        return t;
    }

    /**
     * Changes a players name color.
     * @param uuid UUID of player
     * @param color Color to set to
     */
    public void setColor(UUID uuid, ChatColor color){
        getOrCreateTeam(uuid, false).setColor(color);
    }

    /**
     * Gets a players name color.
     * @param uuid UUID of player
     * @return The player's name color
     */
    public ChatColor getColor(UUID uuid){
        return getOrCreateTeam(uuid, false).getColor();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onJoin(PlayerJoinEvent e){
        getOrCreateTeam(e.getPlayer().getUniqueId(), true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onQuit(PlayerQuitEvent e){
        getOrCreateTeam(e.getPlayer().getUniqueId(), false).unregister();
    }

    /**
     * Updates the color of the player's name based on life count and current Color Rule.
     * @param uuid UUID of player
     * @param lives API to get lives from
     */
    public void updatePlayerColor(UUID uuid, LivesAPI lives) {
        if(mode != Mode.LIVES) return;
        AtomicInteger colorIndex = new AtomicInteger(0);
        AtomicInteger playerLives = new AtomicInteger(lives.getPlayerLives(uuid));
        AtomicBoolean colChosen = new AtomicBoolean(false);
        livesColorRule.forEach((k, v)->{
            if(playerLives.get() <= k && !colChosen.get()){
                colorIndex.set(k);
                colChosen.set(true);
            }
        });
        ChatColor color;
        if(!colChosen.get()) color = livesColorRule.get(livesColorRule.lastKey());
        else color = livesColorRule.get(colorIndex.get());
        if(color == null){
            color = livesColorRule.get(livesColorRule.firstKey());
        }
        setColor(uuid, color);
    }

    /**
     * Updates the color of the player's name based on life count and current Color Rule.
     * @param player Player to change
     * @param lives API to get lives from
     */
    public void updatePlayerColor(Player player, LivesAPI lives) {
        updatePlayerColor(player.getUniqueId(), lives);
    }

    /**
     * Updates the color of the player's name based on time and current Color Rule.
     * @param uuid UUID of player
     * @param timer ID of timer
     * @param time API to get time from
     */
    public void updatePlayerColor(UUID uuid, String timer, LTimeAPI time){
        if(mode != Mode.TIME) return;
        AtomicInteger colorIndex = new AtomicInteger(0);
        AtomicInteger playerTime = new AtomicInteger(time.getPlayerTime(uuid, timer));
        AtomicBoolean colChosen = new AtomicBoolean(false);
        timeColorRule.forEach((k, v)->{
            if(playerTime.get() <= k && !colChosen.get()){
                colorIndex.set(k);
                colChosen.set(true);
            } else {
            }
        });
        ChatColor color;
        if(!colChosen.get()) color = timeColorRule.get(timeColorRule.lastKey());
        else color = timeColorRule.get(colorIndex.get());
        if(color == null){
            color = timeColorRule.get(timeColorRule.firstKey());
        }
        setColor(uuid, color);
    }

    /**
     * Gets a color based on the current Color Rule.
     * @param value Input
     * @return Color from rule using input
     */
    public ChatColor getColorByRule(int value){
        switch (mode){
            case LIVES -> {
                AtomicInteger colorIndex = new AtomicInteger(0);
                AtomicBoolean colChosen = new AtomicBoolean(false);
                AtomicInteger playerLives = new AtomicInteger(value);
                livesColorRule.forEach((k, v)->{
                    if(playerLives.get() <= k && !colChosen.get()){
                        colorIndex.set(k);
                        colChosen.set(true);
                    }
                });
                ChatColor color;
                if(!colChosen.get()) color = livesColorRule.get(livesColorRule.lastKey());
                else color = livesColorRule.get(colorIndex.get());
                if(color == null){
                    color = livesColorRule.get(livesColorRule.firstKey());
                }
                return color;
            }
            case TIME -> {
                AtomicInteger colorIndex = new AtomicInteger(0);
                AtomicInteger playerTime = new AtomicInteger(value);
                AtomicBoolean colChosen = new AtomicBoolean(false);
                timeColorRule.forEach((k, v)->{
                    if(playerTime.get() <= k && !colChosen.get()){
                        colorIndex.set(k);
                        colChosen.set(true);
                    }
                });
                ChatColor color;
                if(!colChosen.get()) color = timeColorRule.get(timeColorRule.lastKey());
                else color = timeColorRule.get(colorIndex.get());
                if(color == null){
                    color = timeColorRule.get(timeColorRule.firstKey());
                }
                return color;
            }
            case CUSTOM -> {
                AtomicInteger colorIndex = new AtomicInteger(0);
                AtomicInteger playerValue = new AtomicInteger(value);
                AtomicBoolean colChosen = new AtomicBoolean(false);
                customColorRule.forEach((k, v) -> {
                    if(playerValue.get() >= k && !colChosen.get()){
                        colorIndex.set(k);
                        colChosen.set(true);
                    }
                });
                ChatColor color;
                if(!colChosen.get()) color = customColorRule.get(customColorRule.lastKey());
                else color = customColorRule.get(colorIndex.get());
                if(color == null){
                    color = customColorRule.get(customColorRule.firstKey());
                }
                return color;
            }
        }
        return null;
    }

    /**
     * Updates the color of the player's name based on time and current Color Rule.
     * @param player Player to change
     * @param timer ID of timer
     * @param time API to get time from
     */
    public void updatePlayerColor(Player player, String timer, LTimeAPI time){
        updatePlayerColor(player.getUniqueId(), timer, time);
    }

    /**
     * Updates the color of the player's name from the current Color Rule using an arbitrary value.
     * @param uuid UUID of player
     * @param colorValue Input for Color Rule
     */
    public void updatePlayerColor(UUID uuid, int colorValue){
        if(mode != Mode.CUSTOM) return;
        AtomicInteger colorIndex = new AtomicInteger(0);
        AtomicInteger playerValue = new AtomicInteger(colorValue);
        AtomicBoolean colChosen = new AtomicBoolean(false);
        customColorRule.forEach((k, v) -> {
            if(playerValue.get() <= k && !colChosen.get()){
                colorIndex.set(k);
                colChosen.set(true);
            }
        });
        ChatColor color = customColorRule.get(colorIndex.get());
        if(color == null){
            color = ChatColor.WHITE;
        }
        setColor(uuid, color);
    }

    /**
     * Updates the color of the player's name from the current Color Rule using an arbitrary value.
     * @param player Player to change
     * @param colorValue Input for Color Rule
     */
    public void updatePlayerColor(Player player, int colorValue){
        updatePlayerColor(player.getUniqueId(), colorValue);
    }

    /**
     * Adds a new rule to the current Color Rule.
     * @param amount Value for rule
     * @param color Color for rule
     */
    public void addColorRule(int amount, ChatColor color){
        switch (mode){
            case CUSTOM -> customColorRule.put(amount, color);
            case LIVES -> livesColorRule.put(amount, color);
            case TIME -> timeColorRule.put(amount, color);
        }
    }
}
