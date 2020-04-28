package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.moss.discord.Constants;
import org.moss.discord.util.EmbedUtil;

import java.awt.*;

public class EmbedCommand implements CommandExecutor {

    EmbedUtil embedUtil = new EmbedUtil();

    @Command(aliases = {"!embed", ".embed"}, usage = "!embed <url>", description = "Makes an embed from a json text")
    public void onCommand(String[] args, Member user, TextChannel channel, Guild server) {
        if (args.length == 0) {
            channel.sendMessage(new EmbedBuilder().setTitle("Invalid URL").setColor(Color.RED).build()).queue();
            return;
        }
        if (user.hasPermission(Permission.KICK_MEMBERS)) {
            channel.sendMessage(new MessageBuilder().setContent(user.getAsMention()).setEmbed(embedUtil.parseString(String.join(" ", args), user, server).build()).build()).queue();
        } else if (channel.getId().equals(Constants.CHANNEL_RANDOM)) {
            channel.sendMessage(new MessageBuilder().setContent(user.getAsMention()).setEmbed(embedUtil.parseString(String.join(" ", args), user, server).build()).build()).queue();
        }
    }

}
