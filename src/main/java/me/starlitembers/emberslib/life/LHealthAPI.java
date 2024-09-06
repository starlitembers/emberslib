package me.starlitembers.emberslib.life;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;

import java.util.function.BiFunction;
import java.util.function.Function;

public class LHealthAPI implements Listener {
    final Plugin p;
    boolean naturalRegen = true;
    boolean secretMode = false;
    double startingMaxHealth = 20;
    Function<PlayerRespawnEvent, Double> respawnFunction = (e) -> startingMaxHealth;
    BiFunction<EntityDamageEvent, Double, Double> secretDamageFunction = null;
    public LHealthAPI(Plugin plugin){
        p = plugin;
    }
    public double getStartingMaxHealth(){
        return startingMaxHealth;
    }
    public void setRespawnFunction(Function<PlayerRespawnEvent, Double> function){
        respawnFunction = function;
    }
    @EventHandler
    void onRespawn(PlayerRespawnEvent event){
        if(event.getRespawnReason() == PlayerRespawnEvent.RespawnReason.DEATH){
            double val = respawnFunction.apply(event);
            setMaxHealth(event.getPlayer(), val);
            event.getPlayer().setHealth(val);
        }
    }
    @EventHandler
    void onJoin(PlayerJoinEvent event){
        OfflinePlayer p = Bukkit.getOfflinePlayer(event.getPlayer().getUniqueId());
        if(!p.hasPlayedBefore()) setMaxHealth(event.getPlayer(), getStartingMaxHealth());
    }

    @EventHandler
    void onLoad(WorldLoadEvent event){
        event.getWorld().setGameRule(GameRule.NATURAL_REGENERATION, naturalRegen);
    }

    @EventHandler
    void onRegen(EntityRegainHealthEvent event){
        if(secretMode && event.getEntity() instanceof Player) event.setCancelled(true);
    }
    public void setSecretDamageFunction(BiFunction<EntityDamageEvent, Double, Double> function){
        secretDamageFunction = function;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    void onTakeDamage(EntityDamageEvent event){
        if(event.isCancelled()) return;
        if(event.getEntity() instanceof Player player){
            if(secretMode && !naturalRegen){
                double finalDamage = event.getFinalDamage() - player.getAbsorptionAmount();
                if(event.getFinalDamage() > 0){
                    Bukkit.getScheduler().scheduleSyncDelayedTask(p, () -> {
                        double d = getMaxHealth(player) - finalDamage;
                        double health = secretDamageFunction != null ? secretDamageFunction.apply(event, d) : d;
                        setMaxHealth(player, health);
                    });
                }
            }
        }
    }

    public double getMaxHealth(Player player){
        return player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
    }

    public void setMaxHealth(Player player, double value){
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(value);
    }
    public void setStartingMaxHealth(double amount){
        startingMaxHealth = amount;
    }

    public void setSecretMode(boolean secretMode){
        this.secretMode = secretMode;
    }
    public boolean isSecretMode(){
        return secretMode;
    }

    public boolean isNaturalRegen(){
        return naturalRegen;
    }
    public void setNaturalRegen(boolean naturalRegen){
        this.naturalRegen = naturalRegen;
    }

    void enable() {
        p.getServer().getPluginManager().registerEvents(this, p);
    }
}
