package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.moss.discord.Chester;
import org.moss.discord.ChesterPlugin;
import org.moss.discord.Constants;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Presence extends Chester implements ChesterPlugin {

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
    }


    @Command(aliases = {"!presence", ".presence"}, usage = "!presence <Sstatus>", description = "Sets the status presence of the bot")
    public void onCommand(DiscordApi api, String[] args, TextChannel channel, User user, Server server) {
        if (hasPermission(user.getRoles(server)) && args.length >= 2) {
            String type = args[0].toUpperCase();
            String url = args[args.length-1];
            LinkedList<String> status = new LinkedList(Arrays.asList(args));
            status.removeFirst();
            try {
                if (type.equalsIgnoreCase("streaming")) {
                    status.removeLast();
                    api.updateActivity(String.join(" ", status), url);
                } else {
                    api.updateActivity(ActivityType.valueOf(type), String.join(" ", status));
                }
            } catch (Exception e) {
                channel.sendMessage("Invalid activity use: " + Arrays.toString(ActivityType.values()));
            }
        }
    }

    public Boolean hasPermission(List<Role> roles) { //TODO one class to rule them all
        for (Role role : roles) {
            String roleId = role.getIdAsString();
            if ((roleId.equals(Constants.ROLE_MODERATOR) || roleId.equals(Constants.ROLE_ADMIN))) {
                return true;
            }
        }
        return false;
    }
}
