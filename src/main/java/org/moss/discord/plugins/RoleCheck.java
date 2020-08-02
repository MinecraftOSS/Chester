package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.moss.discord.Chester;
import org.moss.chesterapi.ChesterPlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoleCheck extends Chester implements ChesterPlugin {

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
    }


    @Command(aliases = {"!rolecheck", ".rolecheck"}, usage = "!rolecheck <User>", description = "Checks users' role")
    public void onCommand(TextChannel channel, String[] args, Message message, Server server) {
        if (args.length >= 1) {
            User user = null;
            if (message.getMentionedUsers().size() >= 1) {
                user = message.getMentionedUsers().get(0);
            } else {
                user = findUser(args[0], server);
            }
            if (user != null) {
                channel.sendMessage(createList(user, server));
            }
        }
    }

    private EmbedBuilder createList(User user, Server server) {
        EmbedBuilder builder = new EmbedBuilder().setColor(Color.GREEN).setDescription("List of roles for " + user.getMentionTag());
        String roles = user.getRoles(server).stream().map(Role::getMentionTag).collect(Collectors.joining(" "));
        builder.addField("Roles", roles);
        return builder;
    }

    private User findUser(String name, Server server) {
        List<User> users = new ArrayList<>(server.getMembersByNameIgnoreCase(name));
        return users.isEmpty() ? null : users.get(0);
    }
}
