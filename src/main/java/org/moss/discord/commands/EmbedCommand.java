package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.moss.discord.Constants;
import org.moss.discord.util.EmbedUtil;

import java.awt.*;

public class EmbedCommand implements CommandExecutor {

    EmbedUtil embedUtil = new EmbedUtil();

    @Command(aliases = {"!embed", ".embed"}, usage = "!embed <url>", description = "Makes an embed from a json text")
    public void onCommand(String[] args, User user, TextChannel channel, MessageAuthor messageAuthor, Server server) {
        if (args.length == 0) {
            channel.sendMessage(new EmbedBuilder().setTitle("Invalid URL").setColor(Color.RED));
            return;
        }
        if (messageAuthor.canKickUsersFromServer()) {
            channel.sendMessage(user.getMentionTag(), embedUtil.parseString(String.join(" ", args), user, server));
        } else if (channel.getIdAsString().equals(Constants.CHANNEL_RANDOM)) {
            channel.sendMessage(user.getMentionTag(), embedUtil.parseString(String.join(" ", args), user, server));
        }
    }

}
