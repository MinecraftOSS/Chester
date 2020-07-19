package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.apache.commons.lang.StringUtils;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.ExceptionLogger;
import org.moss.discord.Chester;
import org.moss.discord.ChesterPlugin;

public class Admin_Server extends Chester implements ChesterPlugin {

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
    }


    @Command(aliases = {"!ban"}, usage = "!ban <username> <reason>", description = "Bans a chosen user.")
    public void onBan(TextChannel channel, String[] args, Message message, MessageAuthor author) {
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

    @Command(aliases = {"!kick"}, usage = "!kick <username> <reason>", description = "Kicks a chosen user.")
    public void onKick(TextChannel channel, String[] args, Message message, MessageAuthor author) {
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

    @Command(aliases = {"!prune"}, usage = "!prune <amount>", description = "Prunes a certain amount of messages (between 2 and 100)")
    public void onPrune(TextChannel channel, String[] args, MessageAuthor author, Message message) {
        if (author.canManageMessagesInTextChannel() && StringUtils.isNumeric(args[0])) {
            int amount = Integer.parseInt(args[0])+1;

            channel.getMessages(amount).thenCompose(MessageSet::deleteAll).exceptionally(ExceptionLogger.get());
            channel.sendMessage("Deleted " + amount + " messages.");
        } else message.addReaction("\uD83D\uDC4E");
    }

    @Command(aliases = {"!say", ".say"}, usage = "!say <channel> <words>", description = "Say stuff")
    public void onSay(TextChannel channel, String[] args, MessageAuthor author, Message message) {
        if (author.canKickUsersFromServer()) {
            if (message.getMentionedChannels().size() >= 1) {
                message.getMentionedChannels().get(0).sendMessage(String.join(" ", args).substring(args[0].length()));
            } else {
                channel.sendMessage(String.join(" ", args));
            }
        }
    }
}
