package me.starlitembers.emberslib.command;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CommandAPI {
    final Plugin p;
    public CommandAPI(Plugin p){
        this.p = p;
    }
    public BaseCommand create(String name){
        return new BaseCommand(name);
    }
    public void register(BaseCommand command){
        try {
            final Server server = Bukkit.getServer();
            Field cmdField = server.getClass().getDeclaredField("commandMap");
            cmdField.setAccessible(true);
            final CommandMap commandMap = (CommandMap) cmdField.get(server);
            Constructor<PluginCommand> pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            pluginCommandConstructor.setAccessible(true);
            PluginCommand cmd = pluginCommandConstructor.newInstance(command.getName(), p);
            cmd.setPermission(command.getPermission());
            cmd.setPermissionMessage(command.getPermissionMessage());
            cmd.setAliases(command.getAliases());
            cmd.setDescription(command.getDescription());
            commandMap.register(p.getName().toLowerCase(), cmd);
            cmd.setExecutor(command::run);
            cmd.setTabCompleter((sender, cmnd, label, args) -> {
                SubCommand currentCmd = null;
                boolean ended = false;
                SubCommand temp = null;
                for (String arg : args) {
                    if (ended) continue;
                    currentCmd = currentCmd == null ? command.getSubCommand(arg) : currentCmd.getSubCommand(arg);
                    if (currentCmd == null) {
                        ended = true;
                        currentCmd = temp;
                    }
                    temp = currentCmd;
                }
                if(currentCmd != null){
                    Set<String> cmds = currentCmd.getSubCommandsNames();
                    Set<String> players = new HashSet<>();
                    List<String> finalCmds = new ArrayList<>();
                    List<String> finalPlayers = new ArrayList<>();
                    Bukkit.getOnlinePlayers().forEach((p) -> players.add(p.getName()));
                    StringUtil.copyPartialMatches(args[args.length-1], cmds, finalCmds);
                    StringUtil.copyPartialMatches(args[args.length-1], players, finalPlayers);
                    return switch (currentCmd.getTabCompletionType()){
                        case ONLINE_PLAYERS -> finalPlayers;
                        case SUB_COMMANDS -> finalCmds;
                        case EMPTY -> new ArrayList<>();
                        case CUSTOM -> currentCmd.getCustomTabCompletion().apply(new CommandInfo(sender, cmnd, label, args));
                    };
                } else {
                    if(args.length < 2){
                        Set<String> cmds = command.getSubCommandsNames();
                        Set<String> players = new HashSet<>();
                        List<String> finalCmds = new ArrayList<>();
                        List<String> finalPlayers = new ArrayList<>();
                        Bukkit.getOnlinePlayers().forEach((p) -> players.add(p.getName()));
                        StringUtil.copyPartialMatches(args[args.length-1], cmds, finalCmds);
                        StringUtil.copyPartialMatches(args[args.length-1], players, finalPlayers);
                        return switch (command.getTabCompletionType()){
                            case ONLINE_PLAYERS -> finalPlayers;
                            case SUB_COMMANDS -> finalCmds;
                            case EMPTY -> new ArrayList<>();
                            case CUSTOM -> command.getCustomTabCompletionFunction().apply(new CommandInfo(sender, cmnd, label, args));
                        };
                    }
                }
                return new ArrayList<>();
            });
        } catch (NoSuchFieldException | InvocationTargetException
                 | IllegalAccessException | NoSuchMethodException | InstantiationException e){
            p.getLogger().severe("Could not create command "+command+": "+e.getMessage());
            e.printStackTrace();
        }
    }
    public void sync(){
        try{
            final Server server = Bukkit.getServer();
            final Method method = server.getClass().getDeclaredMethod("syncCommands");
            method.setAccessible(true);
            method.invoke(Bukkit.getServer());
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            p.getLogger().severe("Could not sync commands: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
