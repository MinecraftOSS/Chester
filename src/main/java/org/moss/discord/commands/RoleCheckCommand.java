package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.stream.Collectors;

public class RoleCheckCommand implements CommandExecutor {

    @Command(aliases = {"!rolecheck", ".rolecheck"}, usage = "!rolecheck <User>", description = "Checks users' role")
    public void onCheck(TextChannel channel, String[] args, Message message, Server server) {
        if (args.length >= 1) {
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

    @Command(aliases = {"!rolelist"}, usage = "Role List", description = "Role List")
    public void onList(TextChannel channel, User user, Server server) {
        if (server.canBanUsers(user)) {
            String roles = server.getRoles().stream().map(role -> role.getName() + " = " + role.getId()).collect(Collectors.joining("\n"));
            channel.sendMessage(new EmbedBuilder().setTitle("Roles").setColor(Color.GREEN).addField("Name | ID", roles));
        }
    }

}
