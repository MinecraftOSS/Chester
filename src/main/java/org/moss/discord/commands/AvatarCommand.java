package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class AvatarCommand implements CommandExecutor {

    @Command(aliases = {"!avatar", ".avatar"}, usage = "!avatar <User>", description = "Shows the users' avatar")
    public void onCommand(DiscordApi api, String[] args, TextChannel channel, Server server) {
        if (args.length >= 1) {
            for (User user : server.getMembers()) {
                if (user.getName().equalsIgnoreCase(args[0])) {
                    channel.sendMessage(new EmbedBuilder().setImage(user.getAvatar())); //TODO LAAAARGER
                    return;
                }
            }
        }
    }
}
