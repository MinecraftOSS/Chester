package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

public class CommandsCommand implements CommandExecutor {

    CommandHandler handler;

    public CommandsCommand(CommandHandler commandHandler) {
        this.handler = commandHandler;
    }

    @Command(aliases = {"!commands", ".commands"}, usage = "!commands", description = "shows all commands")
    public void onCommand(DiscordApi api, TextChannel channel, String[] args) {
        String message = "```";
        for (CommandHandler.SimpleCommand command : handler.getCommands()) {
            String cmd = String.format("%-35s %s", command.getCommandAnnotation().usage(), command.getCommandAnnotation().description());
            message += cmd+"\n";
        }
        message += "```";
        channel.sendMessage(message);
    }
}
