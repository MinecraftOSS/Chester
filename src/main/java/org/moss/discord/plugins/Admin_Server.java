package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.apache.commons.lang.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.ExceptionLogger;
import org.moss.discord.Chester;
import org.moss.chesterapi.ChesterPlugin;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Admin_Server extends Chester implements ChesterPlugin {

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
    }


    @Command(aliases = {"!ban"}, usage = "!ban <User> <Reason>", description = "Bans a chosen user.")
    public void onBan(TextChannel channel, String[] args, Message message, MessageAuthor author) {
        if (author.canBanUsersFromServer()) {
            if (args.length >= 1 && message.getMentionedUsers().size() >= 1) {
                User user = message.getMentionedUsers().get(0);
                String reason = args.length >= 2 ? String.join(" ", args).substring(args[0].length()) : "Banned by " + author.getName();
                message.getServer().ifPresent(server -> server.banUser(user, 1, reason));
                channel.sendMessage(String.format("Successfully banned %s Reason: %s", user.getDiscriminatedName(), reason));
            }
        } else message.addReaction("\uD83D\uDC4E");
    }

    @Command(aliases = {"!softban"}, usage = "!softban <username> <days>", description = "Ban then unbans a user and deleting <days> of messages.")
    public void onSoftBan(TextChannel channel, String[] args, Message message, MessageAuthor author) {
        if (author.canBanUsersFromServer()) {
            if (args.length == 2 && StringUtils.isNumeric(args[1])) {
                User user = message.getMentionedUsers().get(0);
                message.getServer().ifPresent(server -> server.banUser(user, Integer.valueOf(args[1]), "Softban"));

                channel.sendMessage("Soft-banned " + user.getMentionTag() + " successfully!");

                message.getServer().ifPresent(server -> server.unbanUser(user));
            } else {
                channel.sendMessage("!softban <username> <days>");
            }
        } else message.addReaction("\uD83D\uDC4E");
    }

    @Command(aliases = {"!kick"}, usage = "!kick <username> <reason>", description = "Kicks a chosen user.")
    public void onKick(TextChannel channel, String[] args, Message message, MessageAuthor author) {
        if (author.canKickUsersFromServer()) {
            if (args.length >= 1 && message.getMentionedUsers().size() >= 1) {
                User user = message.getMentionedUsers().get(0);
                String reason = args.length >= 2 ? String.join(" ", args).substring(args[0].length()) : "Kicked by " + author.getName();
                message.getServer().ifPresent(server -> server.kickUser(user, reason));
                channel.sendMessage(String.format("Successfully kicked %s Reason: %s", user.getDiscriminatedName(), reason));
            }
        } else message.addReaction("\uD83D\uDC4E");
    }

    @Command(aliases = {"!prune"}, usage = "!prune <amount>", description = "Prunes a certain amount of messages (between 2 and 100)")
    public void onPrune(TextChannel channel, String[] args, MessageAuthor author, Message message) {
        if (author.canManageMessagesInTextChannel() && StringUtils.isNumeric(args[0])) {
            int amount = Integer.parseInt(args[0])+1;
            channel.getMessages(amount).thenCompose(MessageSet::deleteAll).exceptionally(ExceptionLogger.get());
            channel.sendMessage("Deleted " + amount + " messages.").thenAcceptAsync(message1 -> message1.getApi().getThreadPool().getScheduler().schedule(() -> message1.delete(), 5, TimeUnit.SECONDS));
        } else message.addReaction("\uD83D\uDC4E");
    }

    @Command(aliases = {"!say"}, usage = "!say <channel> <words>", description = "Say stuff")
    public void onSay(TextChannel channel, String[] args, MessageAuthor author, Message message) {
        if (author.canKickUsersFromServer()) {
            if (message.getMentionedChannels().size() >= 1 && channel.canManageMessages(author.asUser().orElse(null))) {
                message.getMentionedChannels().get(0).sendMessage(String.join(" ", args).substring(args[0].length()));
            } else {
                channel.sendMessage(String.join(" ", args));
            }
        }
    }

    @Command(aliases = {"!setnick", ".setnick"}, usage = "!setnick <name>", description = "Sets the nickname of the bot")
    public void onCommand(DiscordApi api, String[] args, Server server, MessageAuthor author, TextChannel channel) {
        if (author.canManageNicknamesOnServer()) {
            if (args.length >= 1) {
                String nick = String.join(" ", args);
                api.getYourself().updateNickname(server, nick);
                channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("Successfully set nick to " + nick));
            } else {
                api.getYourself().updateNickname(server, api.getYourself().getName());
            }
        }
    }
}
