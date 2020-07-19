package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.moss.discord.Chester;

import java.util.stream.Collectors;

public class RoleCheck extends Chester {

    public RoleCheck() {
        getCommandHandler().registerCommand(this);
    }

    @Command(aliases = {"!rolecheck", ".rolecheck"}, usage = "!rolecheck <User>", description = "Checks users' role")
    public void onCommand(TextChannel channel, String[] args, Message message, Server server) {
        if (args.length >= 1) {
            String string = "User Roles```";
            if (message.getMentionedUsers().size() >= 1) {
                for (User user : message.getMentionedUsers()) {
                    channel.sendMessage("User Roles for " + user.getName() + String.format("```%s```", user.getRoles(server).stream().map(Role::getName).collect(Collectors.joining(", "))));
                }
                return;
            }
            for (User user : server.getMembers()) {
                if (user.getName().equalsIgnoreCase(args[0])) {
                    channel.sendMessage("User Roles for " + user.getName() + String.format("```%s```", user.getRoles(server).stream().map(Role::getName).collect(Collectors.joining(", "))));
                    break;
                }
            }
        }
    }
}
