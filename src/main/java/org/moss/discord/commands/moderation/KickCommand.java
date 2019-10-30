package org.moss.discord.commands.moderation;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.user.User;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class KickCommand implements CommandExecutor {

    @Command(aliases = {"!kick"}, usage = "!kick <username> <reason>", description = "Kicks a chosen user.")
    public void onCommand(TextChannel channel, String[] args, Message message, MessageAuthor author) {
        if (author.canKickUsersFromServer()) {
            if (args.length >= 2) {
                User user = message.getMentionedUsers().get(0);
                String reason = String.join(" ", args).substring(args[0].length());

                message.getServer().ifPresent(server -> server.kickUser(user, reason));

                channel.sendMessage("Succesfully kicked " + user.getMentionTag() + " for " + reason);
            } else {
                channel.sendMessage("You need to specify a user and reason to kick!");
            }
        } else message.addReaction("\uD83D\uDC4E");
    }

}
