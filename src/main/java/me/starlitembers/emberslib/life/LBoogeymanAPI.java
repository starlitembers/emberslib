package me.starlitembers.emberslib.life;

import me.starlitembers.emberslib.Util;
import me.starlitembers.emberslib.command.BaseCommand;
import me.starlitembers.emberslib.command.CommandAPI;
import me.starlitembers.emberslib.command.SubCommand;
import me.starlitembers.emberslib.command.TabCompletionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Why do people like this feature I hate it
 */
public class LBoogeymanAPI implements Listener {
    final LRoleAPI roleAPI;
    final LivesAPI livesAPI;
    final CommandAPI commandAPI;
    final LColorAPI colorAPI;
    final LTimeAPI timeAPI;
    LColorAPI.Mode mode = LColorAPI.Mode.LIVES;
    final Plugin p;
    int minBoogey = 1;
    int maxBoogey = 6;
    int minLives = 2;
    Random random;
    Sound tick = Sound.UI_BUTTON_CLICK;
    float[] tickSettings = new float[]{1f, 1f};
    Sound thunder = Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
    float[] thunderSettings = new float[]{1f, 0.5f};
    Sound suspense = Sound.BLOCK_BEACON_POWER_SELECT;
    float[] suspenseSettings = new float[]{1f, 0.5f};
    Sound boogey = Sound.BLOCK_BEACON_DEACTIVATE;
    float[] boogeySettings = new float[]{1f, 0.5f};
    Sound notBoogey = Sound.BLOCK_BEACON_ACTIVATE;
    float[] notBoogeySettings = new float[]{1f, 0.5f};
    public int getMinLivesForBoogey(){
        return minLives;
    }
    public void setMinLivesForBoogey(int lives){
        minLives = lives;
    }
    public void setTickSound(Sound sound, float volume, float pitch){
        tick = sound;
        tickSettings[0] = volume;
        tickSettings[1] = pitch;
    }
    public void setThunderSound(Sound sound, float volume, float pitch){
        thunder = sound;
        thunderSettings[0] = volume;
        thunderSettings[1] = pitch;
    }
    public void setSuspenseSound(Sound sound, float volume, float pitch){
        suspense = sound;
        suspenseSettings[0] = volume;
        suspenseSettings[1] = pitch;
    }
    public void setBoogeySound(Sound sound, float volume, float pitch){
        boogey = sound;
        boogeySettings[0] = volume;
        boogeySettings[1] = pitch;
    }
    public void setNotBoogeySound(Sound sound, float volume, float pitch){
        notBoogey = sound;
        notBoogeySettings[0] = volume;
        notBoogeySettings[1] = pitch;
    }
    /**
     * @param roleAPI Required for saving data and tracking boogeymen
     * @param livesAPI Required for failing boogeyman if the Color API color mode is Lives.
     * @param timeAPI Required for failing boogeyman if the Color API color mode is Time.
     * @param colorAPI Required for failing boogeyman.
     * @param commandAPI Required for creating the boogeyman command.
     */
    public LBoogeymanAPI(LRoleAPI roleAPI, LivesAPI livesAPI, LTimeAPI timeAPI, LColorAPI colorAPI, CommandAPI commandAPI){
        this.roleAPI = roleAPI;
        this.livesAPI = livesAPI;
        this.commandAPI = commandAPI;
        this.colorAPI = colorAPI;
        this.timeAPI = timeAPI;
        random = new Random();
        p = roleAPI.p;
        createBoogeymanRole();
    }
    private void createBoogeymanRole(){
        roleAPI.addRoleIfAbsent(roleAPI.createRole("boogeyman", null));
    }

    /**
     * Creates the boogeyman command.
     * @param baseCommand command to add boogeyman subcommand to
     */
    public void registerBoogeymanCommand(BaseCommand baseCommand){
        SubCommand boogeyCommand = baseCommand.createSubCommand("boogeyman");

        boogeyCommand.setTabCompletionType(TabCompletionType.SUB_COMMANDS);
        boogeyCommand.createSubCommand("roll");
        boogeyCommand.createSubCommand("get");
        boogeyCommand.createSubCommand("fail");
        boogeyCommand.createSubCommand("cure");

        SubCommand boogeyGetCommand = boogeyCommand.getSubCommand("get");
        boogeyGetCommand.createSubCommand("list");
        boogeyGetCommand.createSubCommand("amount");
        boogeyGetCommand.createSubCommand("test");

        boogeyGetCommand.setTabCompletionType(TabCompletionType.SUB_COMMANDS);
        boogeyGetCommand.getSubCommand("list").setTabCompletionType(TabCompletionType.EMPTY);
        boogeyGetCommand.getSubCommand("amount").setTabCompletionType(TabCompletionType.EMPTY);
        boogeyGetCommand.getSubCommand("test").setTabCompletionType(TabCompletionType.ONLINE_PLAYERS);

        boogeyCommand.getSubCommand("cure").setTabCompletionType(TabCompletionType.ONLINE_PLAYERS);
        boogeyCommand.getSubCommand("roll").setTabCompletionType(TabCompletionType.EMPTY);
        boogeyCommand.getSubCommand("fail").setTabCompletionType(TabCompletionType.ONLINE_PLAYERS);

        boogeyGetCommand.setFunction(info -> info.sender.sendMessage(ChatColor.RED+"Usage: /lastlife boogeyman get <amount/list/test>"));

        boogeyGetCommand.getSubCommand("list").setFunction(info -> {
            info.sender.sendMessage(ChatColor.GOLD+"Boogeymen:");
            for(UUID u : roleAPI.getRole("boogeyman").getPlayers()){
                OfflinePlayer player = Bukkit.getOfflinePlayer(u);
                ChatColor color = Bukkit.getPlayer(u) != null ? ChatColor.GREEN : ChatColor.RED;
                info.sender.sendMessage(color+player.getName());
            }
        });

        boogeyGetCommand.getSubCommand("amount").setFunction(info -> info.sender.sendMessage(ChatColor.RED+"There are "+roleAPI.getRole("boogeyman").getPlayers().size()+" boogeymen..."));
        String cmd = baseCommand.getName();
        boogeyGetCommand.getSubCommand("test").setFunction(info -> {
            if(info.args.length < 1){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" boogeyman get test <player>");
                return;
            }
            Player p = Bukkit.getPlayer(info.args[0]);
            if(p == null){
                info.sender.sendMessage(ChatColor.RED+"That player is not online!");
            }
            String boogey = roleAPI.playerHasRole(p, roleAPI.getRole("boogeyman")) ? ChatColor.RED+"The Boogeyman." : ChatColor.GREEN+"NOT the Boogeyman.";
            info.sender.sendMessage(ChatColor.GOLD+p.getName()+ChatColor.RESET+" is "+boogey);
        });

        boogeyCommand.setFunction(info -> info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" boogeyman <roll/get/fail>"));
        boogeyCommand.getSubCommand("roll").setFunction(info -> {
            if(info.args.length < 2){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" boogeyman roll <min> <max> [instant]");
                return;
            }
            try {
                int min = Integer.parseInt(info.args[0]);
                int max = Integer.parseInt(info.args[1]);
                rollBoogeyman(min, max, info.args.length > 2);
            } catch (Exception e){
                info.sender.sendMessage(ChatColor.RED+"Usage: /"+cmd+" boogeyman roll <min> <max>");
            }
        });
        boogeyCommand.getSubCommand("fail").setFunction(info -> {
            if(info.args.length < 1){
                List<UUID> playersToFail = new ArrayList<>(roleAPI.getRole("boogeyman").getPlayers());
                playersToFail.forEach(uuid -> {
                    Player p = Bukkit.getPlayer(uuid);
                    if(p != null) fail(p);
                    else fail(uuid);
                });
                info.sender.sendMessage(ChatColor.RED+"Failed all boogeymen");
                return;
            }
            Player p = Bukkit.getPlayer(info.args[0]);
            if(p == null){
                info.sender.sendMessage(ChatColor.RED+"That player is not online!");
                return;
            }
            if(!roleAPI.getRole("boogeyman").hasRole(p)){
                info.sender.sendMessage(ChatColor.RED+"That player is not a boogeyman!");
                return;
            }
            info.sender.sendMessage(ChatColor.RED+"Failed player "+p.getName());
            fail(p);
        });
        boogeyCommand.getSubCommand("cure").setFunction(info -> {
            if(info.args.length < 1){
                List<UUID> playersToFail = new ArrayList<>(roleAPI.getRole("boogeyman").getPlayers());
                playersToFail.forEach(uuid -> {
                    Player p = Bukkit.getPlayer(uuid);
                    if(p != null) cure(p);
                    else cure(uuid);
                });
                info.sender.sendMessage(ChatColor.GREEN+"Cured all boogeymen");
                return;
            }
            Player p = Bukkit.getPlayer(info.args[0]);
            if(p == null){
                info.sender.sendMessage(ChatColor.RED+"That player is not online!");
                return;
            }
            if(!roleAPI.getRole("boogeyman").hasRole(p)){
                info.sender.sendMessage(ChatColor.RED+"That player is not a boogeyman!");
                return;
            }
            info.sender.sendMessage(ChatColor.GREEN+"Cured player "+p.getName());
            cure(p);
        });
    }

    void enable(){
        p.getServer().getPluginManager().registerEvents(this, p);
    }

    @EventHandler
    void onDeath(PlayerDeathEvent event){
        Player v = event.getEntity();
        Player k = event.getEntity().getKiller();
        if(k != null && roleAPI.playerHasRole(k.getUniqueId(), roleAPI.getRole("boogeyman"))){
            cure(k);
        }
    }

    /**
     * Fails boogeyman with animation. (Does not work when player is offline)
     * @param player Player who failed boogeyman.
     */
    public void fail(Player player){
        if(roleAPI.getRole("boogeyman").hasRole(player) && livesAPI.getPlayerLives(player) > 0){
            if(mode == LColorAPI.Mode.LIVES){
                livesAPI.setPlayerLives(player, 1);
                livesAPI.updateColor(player);
            } else if(mode == LColorAPI.Mode.TIME) {
                timeAPI.getTimer("global").removeTime(player, Util.timeToTicks(8, 0, 0, 0));
                timeAPI.updateColor(player, "global", colorAPI);
            }

            player.sendTitle(ChatColor.RED+"You have failed.", "", 20, 60, 20);
            player.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 1f);
            roleAPI.getRole("boogeyman").removePlayer(player);
            Bukkit.getScheduler().runTaskLater(p, () -> player.sendMessage(ChatColor.GRAY+"You failed to kill a green or yellow name last session as the boogeyman. As punishment, you have dropped to you "+ChatColor.RED+"Last Life"+ChatColor.GRAY+". All alliances are severed and you are now hostile to all players. You may team with others on their Last Life if you wish."), 20);
        }
    }

    /**
     * Fails boogeyman without animation. (Works when player is offline)
     * @param uuid UUID of player who failed boogeyman.
     */
    public void fail(UUID uuid){
        if(roleAPI.getRole("boogeyman").hasRole(uuid) && livesAPI.getPlayerLives(uuid) > 0){
            livesAPI.setPlayerLives(uuid, 1);
            roleAPI.getRole("boogeyman").removePlayer(uuid);
        }
    }

    /**
     * Cures boogeyman with animation. (Does not work when player is offline)
     * @param player Player who succeeded as boogeyman.
     */
    public void cure(Player player){
        player.sendTitle(ChatColor.GREEN+"You are cured!", "", 20, 60, 20);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1, true, true, true));
        player.playSound(player, Sound.ENTITY_PLAYER_BREATH, 1f, 1f);
        roleAPI.getRole("boogeyman").removePlayer(player);
    }

    /**
     * Cures boogeyman without animation. (Works when player is offline)
     * @param uuid UUID of player who succeeded as boogeyman.
     */
    public void cure(UUID uuid){
        roleAPI.getRole("boogeyman").removePlayer(uuid);
    }
    void rollBoogeyman$0(){
        Bukkit.broadcastMessage(ChatColor.RED+"The boogeyman is being chosen in 5 minutes.");
        for(Player pl : Bukkit.getOnlinePlayers()){
            pl.playSound(pl, thunder, thunderSettings[0], thunderSettings[1]);
        }
        Bukkit.getScheduler().runTaskLater(p, this::rollBoogeyman$1, 4800);
    }
    void rollBoogeyman$1(){
        Bukkit.broadcastMessage(ChatColor.RED+"The boogeyman is being chosen in 1 minute.");
        for(Player pl : Bukkit.getOnlinePlayers()){
            pl.playSound(pl, thunder, thunderSettings[0], thunderSettings[1]);
        }
        Bukkit.getScheduler().runTaskLater(p, this::rollBoogeyman$2, 1200);
    }
    void rollBoogeyman$2(){
        Bukkit.broadcastMessage(ChatColor.RED+"The boogeyman is about to be chosen.");
        Bukkit.getScheduler().runTaskLater(p, this::rollBoogeyman$3, 200);
    }
    void rollBoogeyman$3(){
        Bukkit.getScheduler().runTaskLater(p, this::rollBoogeyman$distribute, 199);
        for(Player pl : Bukkit.getOnlinePlayers()){
            pl.sendTitle(ChatColor.GREEN+"3", "", 20, 40, 20);
            pl.playSound(pl, tick, tickSettings[0], tickSettings[1]);
        }
        Bukkit.getScheduler().runTaskLater(p, this::rollBoogeyman$4, 40);
    }
    void rollBoogeyman$4(){
        for(Player pl : Bukkit.getOnlinePlayers()){
            pl.sendTitle(ChatColor.YELLOW + "2", "", 20, 40, 20);
            pl.playSound(pl, tick, tickSettings[0], tickSettings[1]);
        }
        Bukkit.getScheduler().runTaskLater(p, this::rollBoogeyman$5, 40);
    }
    void rollBoogeyman$5(){
        for(Player pl : Bukkit.getOnlinePlayers()){
            pl.sendTitle(ChatColor.RED+"1", "", 20, 40, 20);
            pl.playSound(pl, tick, tickSettings[0], tickSettings[1]);
        }
        Bukkit.getScheduler().runTaskLater(p, this::rollBoogeyman$6, 40);
    }
    void rollBoogeyman$6(){
        for(Player pl : Bukkit.getOnlinePlayers()){
            pl.sendTitle(ChatColor.YELLOW+"You are...", "", 20, 40, 20);
            pl.playSound(pl, suspense, suspenseSettings[0], suspenseSettings[1]);
        }
        Bukkit.getScheduler().runTaskLater(p, this::rollBoogeyman$7, 80);
    }
    void rollBoogeyman$7(){
        for(Player pl : Bukkit.getOnlinePlayers()){
            boolean boogey = roleAPI.playerHasRole(pl, roleAPI.getRole("boogeyman"));
            if(boogey){
                pl.sendTitle(ChatColor.RED+"The Boogeyman", "", 20, 60, 20);
                pl.playSound(pl, this.boogey, boogeySettings[0], boogeySettings[1]);
                Bukkit.getScheduler().runTaskLater(p, () -> rollBoogeyman$boogeyInfo(pl), 80);
            } else {
                pl.sendTitle(ChatColor.GREEN+"NOT the Boogeyman", "", 20, 60, 20);
                pl.playSound(pl, notBoogey, notBoogeySettings[0], notBoogeySettings[1]);
            }
        }
    }
    void rollBoogeyman$boogeyInfo(Player player){
        player.sendMessage(ChatColor.GRAY+"You are the boogeyman. You must by any means necessary kill a "+ChatColor.GREEN+"green"+ChatColor.GRAY+" or "+ChatColor.YELLOW+"yellow"+ChatColor.GRAY+" name by direct action to be cured of the curse. If you fail, next session you will become a "+ChatColor.RED+"red name"+ChatColor.GRAY+". All loyalties and friendships are removed while you are the boogeyman.");
    }
    void rollBoogeyman$distribute(){
        roleAPI.distributeRole(new ArrayList<>(Bukkit.getOnlinePlayers()), random.nextInt(minBoogey, maxBoogey+1), minLives, Integer.MAX_VALUE-1, false, false, "boogeyman", livesAPI);
    }

    /**
     * Rolls boogeymen for all online players with animation.
     * @param min Minimum amount of boogeymen
     * @param max Maximum amount of boogeymen
     * @param instant If the 5-minute countdown before the reveal should be skipped.
     */
    public void rollBoogeyman(int min, int max, boolean instant){
        minBoogey = min;
        maxBoogey = max;
        if(instant) rollBoogeyman$2();
        else rollBoogeyman$0();
    }
}
