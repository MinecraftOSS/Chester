package org.moss.discord.commands.moderation;

import org.apache.commons.lang.StringUtils;
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
            if (args.length >= 2) {
                User user = message.getMentionedUsers().get(0);
                String reason = String.join(" ", args).substring(args[0].length());

                message.getServer().ifPresent(server -> server.banUser(user, 1, reason));

                channel.sendMessage("Succesfully banned " + user.getMentionTag() + " for " + reason);
            } else {
                channel.sendMessage("You need to specify a user and reason to ban!");
            }
        } else message.addReaction("\uD83D\uDC4E");
    }

    @Command(aliases = {"!softban"}, usage = "!ban <username> <days>", description = "Ban then unbans a user and deleting <days> of messages.")
    public void onSoftBan(TextChannel channel, String[] args, Message message, MessageAuthor author) {
        if (author.canBanUsersFromServer()) {
            if (args.length == 2 && StringUtils.isNumeric(args[1])) {
                User user = message.getMentionedUsers().get(0);
                message.getServer().ifPresent(server -> server.banUser(user, Integer.valueOf(args[1]), "Softban"));

                channel.sendMessage("Soft-banned " + user.getMentionTag() + " successfully!");

                message.getServer().ifPresent(server -> server.unbanUser(user));
            } else if (args.length == 1) {
                channel.sendMessage("You need an integer 1-7");
            } else if (args.length == 0) {
                channel.sendMessage("You need to mention a user and specify a reason to ban!");
            }
        } else message.addReaction("\uD83D\uDC4E");
    }

}