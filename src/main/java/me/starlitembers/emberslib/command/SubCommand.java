package me.starlitembers.emberslib.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class SubCommand implements RunnableCommand {
    private final String name;
    private final Set<SubCommand> commands;
    private TabCompletionType tab;
    private Consumer<CommandInfo> function;
    private Function<CommandInfo, List<String>> tabFunction;
    private SubCommand(){
        name = "";
        commands = new HashSet<>();
        throw new UnsupportedOperationException();
    }
    SubCommand(String name) {
        this.name = name;
        tab = TabCompletionType.SUB_COMMANDS;
        tabFunction = (info) -> null;
        function = (info) -> {};
        commands = new HashSet<>();
    }
    public TabCompletionType getTabCompletionType(){
        return tab;
    }
    public void setTabCompletionType(TabCompletionType type){
        tab = type;
    }
    public Function<CommandInfo, List<String>> getCustomTabCompletion(){
        return tabFunction;
    }
    public void setCustomTabCompletion(Function<CommandInfo, List<String>> function){
        tabFunction = function;
    }
    public SubCommand createSubCommand(String name){
        SubCommand c = new SubCommand(name);
        commands.add(c);
        return c;
    }
    public SubCommand getSubCommand(String name){
        return getFromName(name);
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
    public String toString(){
        return name;
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args){
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
}
