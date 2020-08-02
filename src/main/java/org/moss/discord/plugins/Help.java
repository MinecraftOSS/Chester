package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.moss.discord.Chester;
import org.moss.chesterapi.ChesterPlugin;
import org.moss.discord.util.PagedEmbed;

import java.awt.*;

public class Help extends Chester implements ChesterPlugin {

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
    }

    @Command(aliases = {"!commands"}, usage = "!commands", description = "shows all commands")
    public void onCommand(TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder().setTitle("Chester Commands").setColor(Color.ORANGE);
        PagedEmbed pagedEmbed = new PagedEmbed(channel, embed);
        for (CommandHandler.SimpleCommand command : getCommandHandler().getCommands()) {
            pagedEmbed.addField(command.getCommandAnnotation().usage(), "```"+command.getCommandAnnotation().description()+"```");
        }
        pagedEmbed.build().join();
    }
}
