package org.moss.discord.commands.moderation;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.user.User;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class BanCommand implements CommandExecutor {

    @Command(aliases = {"!ban"}, usage = "!ban <username> <reason>", description = "Bans a chosen user.")
    public void onCommand(TextChannel channel, String[] args, Message message, MessageAuthor author) {
        if (author.canBanUsersFromServer()) {
            if (args.length == 2) {
                User user = message.getMentionedUsers().get(0);
                String reason = args[1];

                message.getServer().ifPresent(server -> server.banUser(user, 1, reason));

                channel.sendMessage("Banned " + user.getMentionTag() + " successfully!");
            } else if (args.length == 1) {
                channel.sendMessage("You need to specify a reason to ban this user!");
            } else if (args.length == 0) {
                channel.sendMessage("You need to mention a user and specify a reason to ban!");
            }
        } else message.react("👎");
    }

}