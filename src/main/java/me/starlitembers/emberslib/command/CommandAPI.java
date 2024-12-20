package me.starlitembers.emberslib.command;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
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
    private List<BaseCommand> commands = new ArrayList<>();
    public CommandAPI(Plugin p){
        this.p = p;
    }
    public BaseCommand create(String name){
        return new BaseCommand(name);
    }
    public void register(BaseCommand command){
        commands.add(command);
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
                    List<String> list = currentCmd.getTabCompletionList();
                    List<String> finalList = new ArrayList<>();
                    List<String> finalCmds = new ArrayList<>();
                    List<String> finalPlayers = new ArrayList<>();
                    Bukkit.getOnlinePlayers().forEach((p) -> players.add(p.getName()));
                    StringUtil.copyPartialMatches(args[args.length-1], cmds, finalCmds);
                    StringUtil.copyPartialMatches(args[args.length-1], players, finalPlayers);
                    StringUtil.copyPartialMatches(args[args.length-1], list, finalList);
                    return switch (currentCmd.getTabCompletionType()){
                        case ONLINE_PLAYERS -> finalPlayers;
                        case SUB_COMMANDS -> finalCmds;
                        case LIST -> finalList;
                        case EMPTY -> new ArrayList<>();
                        case CUSTOM -> currentCmd.getCustomTabCompletion().apply(new CommandInfo(sender, cmnd, label, args));
                    };
                } else {
                    if(args.length < 2){
                        Set<String> cmds = command.getSubCommandsNames();
                        Set<String> players = new HashSet<>();
                        List<String> list = command.getTabCompletionList();
                        List<String> finalList = new ArrayList<>();
                        List<String> finalCmds = new ArrayList<>();
                        List<String> finalPlayers = new ArrayList<>();
                        Bukkit.getOnlinePlayers().forEach((p) -> players.add(p.getName()));
                        StringUtil.copyPartialMatches(args[args.length-1], cmds, finalCmds);
                        StringUtil.copyPartialMatches(args[args.length-1], players, finalPlayers);
                        StringUtil.copyPartialMatches(args[args.length-1], list, finalList);
                        return switch (command.getTabCompletionType()){
                            case ONLINE_PLAYERS -> finalPlayers;
                            case SUB_COMMANDS -> finalCmds;
                            case LIST -> finalList;
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

    public void remove(boolean minecraft, String... commands){
        String alias = minecraft ? "minecraft:" : p.getName().toLowerCase()+":";
        try {
            final Server server = Bukkit.getServer();
            Field cmdField = server.getClass().getDeclaredField("commandMap");
            cmdField.setAccessible(true);
            final CommandMap commandMap = (CommandMap) cmdField.get(server);
            Field knownCmd = commandMap.getClass().getSuperclass().getDeclaredField("knownCommands");
            knownCmd.setAccessible(true);
            HashMap<String, Command> knownCmds = (HashMap<String, Command>) knownCmd.get(commandMap);
            for(String command : commands){
                Command c = knownCmds.get(command);
                if(c != null) c.unregister(commandMap);
                knownCmds.remove(command);
                c = knownCmds.get(alias+command);
                if(c != null) c.unregister(commandMap);
                knownCmds.remove(alias+command);
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            p.getLogger().severe("Could not remove commands "+ Arrays.toString(commands) +": "+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        try {
            final Server server = Bukkit.getServer();
            Field cmdField = server.getClass().getDeclaredField("commandMap");
            cmdField.setAccessible(true);
            final CommandMap commandMap = (CommandMap) cmdField.get(server);
            Field knownCmd = commandMap.getClass().getSuperclass().getDeclaredField("knownCommands");
            knownCmd.setAccessible(true);
            HashMap<String, Command> knownCmds = (HashMap<String, Command>) knownCmd.get(commandMap);
            for(BaseCommand cmd : commands){
                Command c = knownCmds.get(cmd.getName());
                if(c != null) c.unregister(commandMap);
                knownCmds.remove(cmd.getName());
                c = knownCmds.get(p.getName().toLowerCase()+":"+cmd.getName());
                if(c != null) c.unregister(commandMap);
                knownCmds.remove(p.getName().toLowerCase()+":"+cmd.getName());
                for(String s : cmd.getAliases()){
                    c = knownCmds.get(s);
                    if(c != null) c.unregister(commandMap);
                    knownCmds.remove(s);
                    c = knownCmds.get(p.getName().toLowerCase()+":"+s);
                    if(c != null) c.unregister(commandMap);
                    knownCmds.remove(p.getName().toLowerCase()+":"+s);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            p.getLogger().severe("Could not clear commands: "+e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
