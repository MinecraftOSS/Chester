package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.moss.discord.Constants;

import java.awt.*;
import java.util.List;

public class RoleCommand implements CommandExecutor {

    @Command(aliases = {"!role", ".role"}, usage = "!role <give/take> <role> <user>", description = "Give user a role")
    public void onRole(TextChannel channel, User user, Server server, Message message, String[] args) {
        if (args.length >= 3 && server.isAdmin(user)) {
            User target = message.getMentionedUsers().get(0);
            Role role = server.getRolesByNameIgnoreCase(args[1]).get(0);
            if (target != null && role != null) {
                if (args[0].equalsIgnoreCase("give")) {
                    target.addRole(role, "Role given by " + user.getName());
                    channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle("Role given"));
                }
                if (args[0].equalsIgnoreCase("take")) {
                    target.removeRole(role, "Role taken by " + user.getName());
                    channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle("Role Taken"));
                }
            } else {
                channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("No user/role found"));
            }
        }
    }

    @Command(aliases = {"!contributor", ".contributor"}, usage = ".contributor <user>", description = "Allows project leads to give contributor roles.")
    public void onContributor(TextChannel channel, User user, Server server, Message message, String[] args) {
        if (args.length >= 1 && hasRole(user.getRoles(server), "Project Lead")) {
            User target = message.getMentionedUsers().get(0);
            if (target != null) {
                    target.addRole(server.getRoleById(Constants.ROLE_CONTRIBUTOR).get(), "Role given by " + user.getName());
                    channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle("Role given"));
            } else {
                channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("No user found"));
            }
        }
    }

    @Command(aliases = {"!supporter", "!donor"}, usage = "!supporter <user>", description = "Allows project leads to give donor roles.")
    public void onSupporter(TextChannel channel, User user, Server server, Message message, String[] args) {
        if (args.length >= 1 && hasRole(user.getRoles(server), "Project Lead")) {
            User target = message.getMentionedUsers().get(0);
            if (target != null) {
                target.addRole(server.getRoleById(Constants.ROLE_SUPPORTER).get(), "Role given by " + user.getName());
                channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle("Role given"));
            } else {
                channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("No user found"));
            }
        }
    }

    public Boolean hasRole(List<Role> roles, String role) { //TODO 1 class
        for (Role roll : roles) {
            if (roll.getName().equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

}
