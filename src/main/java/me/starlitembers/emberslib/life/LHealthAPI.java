package me.starlitembers.emberslib.life;

import me.starlitembers.emberslib.command.BaseCommand;
import me.starlitembers.emberslib.command.SubCommand;
import me.starlitembers.emberslib.command.TabCompletionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        event.getPlayer().setHealth(getMaxHealth(event.getPlayer()));
    }

    public void registerHealthCommand(BaseCommand cmd){
        SubCommand heartsc = cmd.createSubCommand("health");
        heartsc.setTabCompletionType(TabCompletionType.SUB_COMMANDS);
        heartsc.setFunction(info -> info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" health <add/remove/get/set>"));

        heartsc.createSubCommand("add");
        heartsc.createSubCommand("remove");
        heartsc.createSubCommand("get");
        heartsc.createSubCommand("set");

        SubCommand haddsc = heartsc.getSubCommand("add");
        SubCommand hremovesc = heartsc.getSubCommand("remove");
        SubCommand hgetsc = heartsc.getSubCommand("get");
        SubCommand hsetsc = heartsc.getSubCommand("set");

        haddsc.setTabCompletionType(TabCompletionType.ONLINE_PLAYERS);
        hremovesc.setTabCompletionType(TabCompletionType.ONLINE_PLAYERS);
        hgetsc.setTabCompletionType(TabCompletionType.ONLINE_PLAYERS);
        hsetsc.setTabCompletionType(TabCompletionType.ONLINE_PLAYERS);

        haddsc.setFunction(info -> {
            if(info.args.length != 2){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" health add <player> <amount>");
                info.sender.sendMessage(ChatColor.YELLOW+"Note: Amount is in whole hearts.");
                return;
            }
            String name = info.args[0];
            double amount;
            try {
                amount = Double.parseDouble(info.args[1]);
            } catch (Exception e){
                info.sender.sendMessage(ChatColor.RED+"Amount must be a number!");
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" health add <player> <amount>");
                info.sender.sendMessage(ChatColor.YELLOW+"Note: Amount is in whole hearts.");
                return;
            }
            Player p = Bukkit.getPlayer(name);
            if(p == null){
                info.sender.sendMessage(ChatColor.RED+name+" must be online in order to change their health!");
                return;
            }
            setMaxHealth(p, getMaxHealth(p) + (amount * 2));
            p.setHealth(getMaxHealth(p));
            info.sender.sendMessage(ChatColor.GREEN+"Added "+amount+" heart(s) to "+name+"!");
        });
        hremovesc.setFunction(info -> {
            if(info.args.length != 2){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" health remove <player> <amount>");
                info.sender.sendMessage(ChatColor.YELLOW+"Note: Amount is in whole hearts.");
                return;
            }
            String name = info.args[0];
            double amount;
            try {
                amount = Double.parseDouble(info.args[1]);
            } catch (Exception e){
                info.sender.sendMessage(ChatColor.RED+"Amount must be a number!");
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" health remove <player> <amount>");
                info.sender.sendMessage(ChatColor.YELLOW+"Note: Amount is in whole hearts.");
                return;
            }
            Player p = Bukkit.getPlayer(name);
            if(p == null){
                info.sender.sendMessage(ChatColor.RED+name+" must be online in order to change their health!");
                return;
            }
            if(amount >= (getMaxHealth(p)/2)) {
                amount = getMaxHealth(p) - 1;
                info.sender.sendMessage(ChatColor.YELLOW+"Attempted to remove more health from "+name+" than they have! Setting them to half a heart.");
                setMaxHealth(p, getMaxHealth(p) - amount);
                info.sender.sendMessage(ChatColor.GREEN+"Removed "+(amount/2)+" heart(s) from "+name+"!");
                return;
            }
            setMaxHealth(p, getMaxHealth(p) - (amount * 2));
            p.setHealth(getMaxHealth(p));
            info.sender.sendMessage(ChatColor.GREEN+"Removed "+amount+" heart(s) from "+name+"!");
        });
        hgetsc.setFunction(info -> {
            if(info.args.length != 1){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" health get <player>");
                info.sender.sendMessage(ChatColor.YELLOW+"Note: Amount is in whole hearts.");
                return;
            }
            String name = info.args[0];
            Player p = Bukkit.getPlayer(name);
            if(p == null){
                info.sender.sendMessage(ChatColor.RED+name+" must be online in order to get their health!");
                return;
            }
            info.sender.sendMessage(ChatColor.GREEN+name+" has "+(getMaxHealth(p)/2)+" heart(s)!");
        });
        hsetsc.setFunction(info -> {
            if(info.args.length != 2){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" health set <player> <amount>");
                info.sender.sendMessage(ChatColor.YELLOW+"Note: Amount is in whole hearts.");
                return;
            }
            String name = info.args[0];
            double amount;
            try {
                amount = Double.parseDouble(info.args[1]);
            } catch (Exception e){
                info.sender.sendMessage(ChatColor.RED+"Amount must be a number!");
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" health set <player> <amount>");
                info.sender.sendMessage(ChatColor.YELLOW+"Note: Amount is in whole hearts.");
                return;
            }
            if(amount < 0.5){
                info.sender.sendMessage(ChatColor.RED+"Amount must be at least half a heart!");
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" health set <player> <amount>");
                info.sender.sendMessage(ChatColor.YELLOW+"Note: Amount is in whole hearts.");
                return;
            }
            Player p = Bukkit.getPlayer(name);
            if(p == null){
                info.sender.sendMessage(ChatColor.RED+name+" must be online in order to change their health!");
                return;
            }
            setMaxHealth(p, amount * 2);
            p.setHealth(getMaxHealth(p));
            info.sender.sendMessage(ChatColor.GREEN+"Set "+name+" to "+amount+" heart(s)!");
        });
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
        if(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() < 0) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(0.5);
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
