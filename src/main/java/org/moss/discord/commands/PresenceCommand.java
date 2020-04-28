package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import org.moss.discord.Constants;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PresenceCommand implements CommandExecutor {

    @Command(aliases = {"!presence", ".presence"}, usage = "!presence <Sstatus>", description = "Sets the status presence of the bot")
    public void onCommand(JDA api, String[] args, TextChannel channel, Member user, Guild server) {
        if (hasPermission(user.getRoles()) && args.length >= 2) {
            String type = args[0].toUpperCase();
            String url = args[args.length-1];
            LinkedList<String> status = new LinkedList<>(Arrays.asList(args));
            status.removeFirst();
            try {
                if (type.equalsIgnoreCase("streaming")) {
                    status.removeLast();
                    api.getPresence().setActivity(Activity.streaming(String.join(" ", status), url));
                } else {
                    api.getPresence().setActivity(Activity.of(Activity.ActivityType.valueOf(type), String.join(" ", status)));
                }
            } catch (Exception e) {
                channel.sendMessage("Invalid activity use: " + Arrays.toString(Activity.ActivityType.values())).queue();
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
