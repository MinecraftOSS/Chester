package org.moss.discord.commands.moderation;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class BanCommand implements CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(BanCommand.class);

    @Command(aliases = {"!ban"}, usage = "!ban <username> <reason>", description = "Bans a chosen user.")
    public void onCommand(TextChannel channel, String[] args, Message message) {
        if (args.length == 2) {
            User user = message.getMentionedUsers().get(0);
            String reason = args[1];
            Server server = message.getServer().get();

            server.banUser(user, 1, reason);
            channel.sendMessage("Banned " + user.getName() + " successfully!");
        } else if (args.length == 1) {
            channel.sendMessage("You need to specify a reason to ban this user!");
        } else if (args.length == 0) {
            channel.sendMessage("You need to mention a user and specify a reason to ban!");
        }
    }

}