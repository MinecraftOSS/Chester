package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.entities.*;

import java.util.stream.Collectors;

public class RoleCheckCommand implements CommandExecutor {

    @Command(aliases = {"!rolecheck", ".rolecheck"}, usage = "!rolecheck <User>", description = "Checks users' role")
    public void onCommand(TextChannel channel, String[] args, Message message, Guild server) {
        if (args.length >= 1) {
            String string = "User Roles```";
            if (message.getMentionedUsers().size() >= 1) {
                for (Member user : message.getMentionedMembers()) {
                    channel.sendMessage("User Roles for " + user.getUser().getName() + String.format("```%s```", user.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")))).queue();
                }
                return;
            }
            for (Member user : server.getMembers()) {
                if (user.getUser().getName().equalsIgnoreCase(args[0])) {
                    channel.sendMessage("User Roles for " + user.getUser().getName() + String.format("```%s```", user.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")))).queue();
                    break;
                }
            }
        }
    }
}
