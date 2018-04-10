package org.moss.discord.commands.moderation;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class KickCommand implements CommandExecutor {

    @Command(aliases = {"!kick"}, usage = "!kick <username> <reason>", description = "Kicks a chosen user.")
    public void onCommand(TextChannel channel, String[] args, Message message) {
        if (args.length == 2) {
            User user = message.getMentionedUsers().get(0);
            String reason = args[1];
            Server server = message.getServer().get();

            server.kickUser(user, reason);
            channel.sendMessage("Kicked " + user.getMentionTag() + " successfully!");
        } else if (args.length == 1) {
            channel.sendMessage("You need to specify a reason to kick this user!");
        } else if (args.length == 0) {
            channel.sendMessage("You need to mention a user and specify a reason to kick!");
        }
    }

}
