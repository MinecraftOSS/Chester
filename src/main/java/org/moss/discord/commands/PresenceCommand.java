package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.moss.discord.Constants;

import java.util.List;

public class PresenceCommand implements CommandExecutor {

    @Command(aliases = {"!presence", ".presence"}, usage = "!presence <Sstatus>", description = "Sets the status presence of the bot")
    public void onCommand(DiscordApi api, String[] args, User user, Server server) {
        if (hasPermission(user.getRoles(server))) {
            api.updateActivity(String.join(" ", args));
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
