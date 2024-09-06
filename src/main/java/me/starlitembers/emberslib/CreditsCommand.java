package me.starlitembers.emberslib;

import me.starlitembers.emberslib.life.LifeLib;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CreditsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LifeLib.sendCreditsMessage(sender);
        return true;
    }
}
