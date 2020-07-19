package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.moss.discord.Chester;
import org.moss.discord.Constants;

import java.util.List;

public class Nickname extends Chester {

    public Nickname() {
        getCommandHandler().registerCommand(this);
    }

    @Command(aliases = {"!setnick", ".setnick"}, usage = "!setnick <name>", description = "Sets the nickname of the bot")
    public void onCommand(DiscordApi api, String[] args, User user, Server server) {
        if (hasPermission(user.getRoles(server))) {
            if (args.length == 1) {
                api.getYourself().updateNickname(server, args[0]);
            } else {
                api.getYourself().updateNickname(server, api.getYourself().getName());
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
