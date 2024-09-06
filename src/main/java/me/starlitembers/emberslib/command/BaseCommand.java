package me.starlitembers.emberslib.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class BaseCommand implements RunnableCommand {
    private final String name;
    private final Set<SubCommand> commands;
    private TabCompletionType tab;
    private Consumer<CommandInfo> function;
    private Function<CommandInfo, List<String>> tabFunction;
    private String permission;
    private String permissionMessage;
    private List<String> aliases;
    private String description;

    private BaseCommand(){
        name = "";
        commands = new HashSet<>();
        throw new UnsupportedOperationException();
    }

    BaseCommand(String name){
        this.name = name;
        commands = new HashSet<>();
        tab = TabCompletionType.SUB_COMMANDS;
        tabFunction = (info) -> null;
        function = (info) -> {};
        permission = "emberlifelib.commandapi.admin";
        permissionMessage = ChatColor.RED+"You do not have permission to use this command.";
        aliases = new ArrayList<>();
        description = "A command created using the Ember Life Library.";
    }
    public boolean hasPermission(Player player){
        return player.hasPermission(permission);
    }
    public TabCompletionType getTabCompletionType(){
        return tab;
    }
    public void setTabCompletionType(TabCompletionType type){
        tab = type;
    }
    public Function<CommandInfo, List<String>> getCustomTabCompletionFunction(){
        return tabFunction;
    }
    public void setCustomTabCompletionFunction(Function<CommandInfo, List<String>> function){
        tabFunction = function;
    }
    public Set<SubCommand> getSubCommands(){
        return commands;
    }
    public Set<String> getSubCommandsNames(){
        Set<String> set = new HashSet<>();
        for(SubCommand c : commands){
            set.add(c.getName());
        }
        return set;
    }
    public SubCommand createSubCommand(String name){
        SubCommand c = new SubCommand(name);
        commands.add(c);
        return c;
    }
    public SubCommand getSubCommand(String name){
        return getFromName(name);
    }
    public String getName(){
        return name;
    }
    public void setFunction(Consumer<CommandInfo> function){
        this.function = function;
    }
    public Consumer<CommandInfo> getFunction(){
        return function;
    }
    private SubCommand getFromName(String name){
        for(SubCommand c : commands){
            if(c.toString().equalsIgnoreCase(name)) return c;
        }
        return null;
    }
    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args){
        if(!sender.hasPermission(permission)){
            sender.sendMessage(ChatColor.RED+"You do not have permission to run this command");
            return true;
        }
        if(args.length < 1) {
            function.accept(new CommandInfo(sender, command, label, args));
            return true;
        }
        SubCommand cmd = getFromName(args[0]);
        if(cmd == null) {
            function.accept(new CommandInfo(sender, command, label, args));
            return true;
        }
        List<String> tempArgs = new LinkedList<>(Arrays.asList(args));
        tempArgs.remove(0);
        String[] newArgs = tempArgs.toArray(new String[0]);
        return cmd.run(sender, command, cmd.getName(), newArgs);
    }
    @Override
    public String toString(){
        return name;
    }

    public String getPermission() {
        return permission;
    }
    public void setPermission(String permission){
        this.permission = permission;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }
    public void setPermissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
    }
    
    public List<String> getAliases() {
        return aliases;
    }
    public void setAliases(List<String> aliases){
        this.aliases = aliases;
    }
    public void addAliases(String... aliases){
        this.aliases.addAll(Arrays.asList(aliases));
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
}
