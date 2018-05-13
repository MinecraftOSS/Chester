package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class RoleCheckCommand implements CommandExecutor {

    @Command(aliases = {"!rolecheck", ".rolecheck"}, usage = "!rolecheck <User>", description = "Checks users' role")
    public void onCommand(TextChannel channel, String[] args, Message message, Server server) {
        if (args.length >= 1) {
            String string = "User Roles```";
            for (User user : server.getMembers()) {
                if (user.getName().equalsIgnoreCase(args[0])) {
                    for (Role role : user.getRoles(server)) {
                        string += role.getIdAsString() + " " + role.getName() + "\n";
                    }
                    break;
                }
            }
            string += "```";
            channel.sendMessage(string);
        }
    }
}
