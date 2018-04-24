package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.moss.discord.Constants;
import org.moss.discord.storage.FactoidStorage;

import java.awt.*;
import java.util.List;

public class TagCommand implements CommandExecutor, MessageCreateListener {

    private FactoidStorage storage = new FactoidStorage();

    public TagCommand(DiscordApi api) {
        api.addListener(this);
    }

    @Command(aliases = {"!tag", "?tag", "?"}, usage = "!tag <name>", description = "Send the tag message to the channel.")
    public void onTag(DiscordApi api, TextChannel channel, String[] args) {
        if (args.length >= 1  && storage.isFactoid(args[0].toLowerCase())) {
            channel.sendMessage(storage.getTag(args[0].toLowerCase()));
        }
    }

    @Command(aliases = {"!tags", "?tags"}, usage = "!tags [filter]", description = "List all currently enabled tags.")
    public void onList(DiscordApi api, TextChannel channel, String[] args) {
        String s = "";
        for (String key : storage.getMap().keySet()) {
            s += String.format("`%s` ", key);
        }
        channel.sendMessage(new EmbedBuilder().addField("Active Tags", s).setColor(Color.GREEN));
    }

    @Command(aliases = {"!tagset", "?tagset"}, usage = "!tagset <name> [message]", description = "Set a new tag for this channel.")
    public void onSet(DiscordApi api, TextChannel channel, String[] args, User user, Server server) {
        if (args.length >= 2 && hasPermission(user.getRoles(server))) {
            StringBuilder sb = new StringBuilder();
            for(int i = 1; i < args.length; i++) {
                sb.append(' ').append(args[i]);
            }
            storage.set(args[0].toLowerCase(), sb.toString());
            channel.sendMessage(new EmbedBuilder().setTitle("Tag set!").setColor(Color.GREEN));
        }
    }

    public Boolean hasPermission(List<Role> roles) {
        for (Role role : roles) {
            String roleId = role.getIdAsString();
            if ((roleId.equals(Constants.ROLE_MODERATOR) || roleId.equals(Constants.ROLE_ADMIN))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent ev) {
        String message = ev.getMessage().getContent();
        if (message.startsWith("?") && message.length() >= 2) {
            String tag = message.split(" ")[0].substring(1).toLowerCase();
            if (storage.isFactoid(tag)) {
                ev.getChannel().sendMessage(storage.getTag(tag));
            }
        }
    }
}
