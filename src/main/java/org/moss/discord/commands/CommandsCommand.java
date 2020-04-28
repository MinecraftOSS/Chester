package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.moss.discord.util.PagedEmbed;

import java.awt.*;

public class CommandsCommand implements CommandExecutor {

    CommandHandler handler;

    public CommandsCommand(CommandHandler commandHandler) {
        this.handler = commandHandler;
    }

    @Command(aliases = {"!commands", ".commands"}, usage = "!commands", description = "shows all commands")
    public void onCommand(JDA api, TextChannel channel, String[] args) {
        EmbedBuilder embed = new EmbedBuilder().setTitle("M.O.S.S Commands").setColor(Color.ORANGE);
        PagedEmbed pagedEmbed = new PagedEmbed(channel, embed);
        for (CommandHandler.SimpleCommand command : handler.getCommands()) {
            pagedEmbed.addField(command.getCommandAnnotation().usage(), "```"+command.getCommandAnnotation().description()+"```");
        }
        pagedEmbed.build().join();
    }
}
