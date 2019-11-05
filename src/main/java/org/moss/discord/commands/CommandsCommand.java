package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.moss.discord.util.PagedEmbed;

import java.awt.*;

public class CommandsCommand implements CommandExecutor {

    CommandHandler handler;

    public CommandsCommand(CommandHandler commandHandler) {
        this.handler = commandHandler;
    }

    @Command(aliases = {"!commands", ".commands"}, usage = "!commands", description = "shows all commands")
    public void onCommand(DiscordApi api, TextChannel channel, String[] args) {
        EmbedBuilder embed = new EmbedBuilder().setTitle("M.O.S.S Commands").setColor(Color.ORANGE);
        PagedEmbed pagedEmbed = new PagedEmbed(channel, embed);
        for (CommandHandler.SimpleCommand command : handler.getCommands()) {
            pagedEmbed.addField(command.getCommandAnnotation().usage(), "```"+command.getCommandAnnotation().description()+"```");
        }
        pagedEmbed.build().join();
    }
}
