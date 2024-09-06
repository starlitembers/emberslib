package me.starlitembers.emberslib.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface RunnableCommand {
    boolean run(CommandSender sender, Command command, String label, String[] args);
}
