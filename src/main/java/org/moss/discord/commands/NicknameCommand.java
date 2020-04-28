package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.moss.discord.Constants;

import java.util.List;

public class NicknameCommand implements CommandExecutor {

    @Command(aliases = {"!setnick", ".setnick"}, usage = "!setnick <name>", description = "Sets the nickname of the bot")
    public void onCommand(JDA api, String[] args, Member user, Guild server) {
        if (hasPermission(user.getRoles())) {
            if (args.length == 1) {
                server.getMember(api.getSelfUser()).modifyNickname(args[0]).queue();
            } else {
                server.getMember(api.getSelfUser()).modifyNickname(null).queue();
            }
        }
    }

    public Boolean hasPermission(List<Role> roles) { //TODO one class to rule them all
        for (Role role : roles) {
            String roleId = role.getId();
            if ((roleId.equals(Constants.ROLE_MODERATOR) || roleId.equals(Constants.ROLE_ADMIN))) {
                return true;
            }
        }
        return false;
    }
}
