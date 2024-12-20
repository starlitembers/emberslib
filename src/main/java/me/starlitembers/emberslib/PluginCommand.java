package me.starlitembers.emberslib;

import me.starlitembers.emberslib.life.LifeLib;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class PluginCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length < 2) return false;
        boolean enable = args[0].equalsIgnoreCase("enable");

        if(enable){
            LifeLib.enable(args[1]);
            commandSender.sendMessage(ChatColor.BLUE+"Enabled "+args[1]);
        } else {
            LifeLib.disable(args[1]);
            commandSender.sendMessage(ChatColor.BLUE+"Disabled "+args[1]);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> list;
        List<String> finalList = new ArrayList<>();
        switch (args.length){
            case 1:
                list = List.of("enable", "disable");
                StringUtil.copyPartialMatches(args[0], list, finalList);
                break;
            case 2:
                list = new ArrayList<>(LifeLib.getInstanceNames());
                StringUtil.copyPartialMatches(args[1], list, finalList);
                break;
            default:
                break;
        }

        return finalList;
    }
}
