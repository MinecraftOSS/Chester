package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.apache.commons.lang.ArrayUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.moss.discord.Constants;
import org.moss.discord.storage.FactoidStorage;
import org.moss.discord.util.EmbedUtil;
import org.moss.discord.util.KeywordsUtil;

import java.awt.*;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.StringJoiner;

public class TagCommand implements CommandExecutor, MessageCreateListener {

    private FactoidStorage storage = new FactoidStorage();
    EmbedUtil util = new EmbedUtil();

    public TagCommand(DiscordApi api) {
        api.addListener(this);
    }

//    @Command(aliases = {"!tag", "?tag", "?"}, usage = "!tag <name>", description = "Send the tag message to the channel.")
//    public void onTag(DiscordApi api, TextChannel channel, String[] args) {
//        if (args.length >= 1  && storage.isFactoid(args[0].toLowerCase())) {
//            if (args.length > 1) {
//                String[] tagArgs = Arrays.copyOfRange(args, 1, args.length);
//                try {
//                    channel.sendMessage(String.format(getFactoid(args[0]), (Object[]) tagArgs));
//                } catch (IllegalFormatException e) {
//                    // Some Joker -_- passed in some weird arguments
//                    return;
//                }
//                return;
//            }
//            channel.sendMessage(getFactoid(args[0]));
//        }
//    }

    @Command(aliases = {"!tagraw", "?tagraw"}, usage = "!tagraw <name>", description = "Send the raw tag message to the channel.")
    public void onTagRaw(DiscordApi api, TextChannel channel, String[] args, User user, Server server) {
        if (args.length >= 1  && storage.isFactoid(args[0].toLowerCase()) && server.canKickUsers(user)) {
            channel.sendMessage("```"+getFactoid(args[0])+"```");
        }
    }

    @Command(aliases = {"!tags", "?tags"}, usage = "!tags [filter]", description = "List all currently enabled tags.")
    public void onList(DiscordApi api, TextChannel channel, Server server, User user) {
        if (!server.canKickUsers(user)) return;
        String s = "";
        for (String key : storage.getMap().keySet()) {
            s += String.format("`%s` ", key);
        }
        channel.sendMessage(new EmbedBuilder().addField("Active Tags", s).setColor(Color.GREEN));
    }

    @Command(aliases = {"!tagset", "?tagset"}, usage = "!tagset <name> [message]", description = "Set a new tag")
    public void onSet(DiscordApi api, TextChannel channel, String[] args, User user, Server server) {
        if (args.length >= 2 && server.canKickUsers(user)) {
            StringJoiner sb = new StringJoiner(" ");
            for(int i = 1; i < args.length; i++) {
                sb.add(args[i]);
            }
            storage.set(args[0].toLowerCase(), sb.toString());
            channel.sendMessage(new EmbedBuilder().setTitle("Tag set!").setColor(Color.GREEN));
        }
    }

    @Command(aliases = {"!tagunset", "?tagunset"}, usage = "!tagunset <name> [message]", description = "Unset a tag")
    public void onUnset(DiscordApi api, TextChannel channel, String[] args, User user, Server server) {
        if (args.length >= 1 && server.canKickUsers(user)) {
            storage.unset(args[0].toLowerCase());
            channel.sendMessage(new EmbedBuilder().setTitle("Tag removed!").setColor(Color.GREEN));
        }
    }

    @Override
    public void onMessageCreate(MessageCreateEvent ev) {
        String message = ev.getMessage().getContent();
        if (message.startsWith("?") && message.length() >= 2) {
            String[] args = message.split(" ");
            String tag = args[0].substring(1).toLowerCase();
            args = (String[]) ArrayUtils.remove(args, 0);
            if (storage.isFactoid(tag)) {
                String factoid = getFactoid(tag);
                if (factoid.startsWith("<staff>")) {
                    factoid = factoid.substring(7);
                    if (!ev.getMessageAuthor().canKickUsersFromServer()) return;
                }
                if (factoid.startsWith("<del>")) {
                    ev.getMessage().delete();
                    factoid = factoid.substring(5);
                }
                if (factoid.startsWith("<embed>") || factoid.startsWith("<json>")) {
                    ev.getChannel().sendMessage(util.parseString(factoid, ev.getMessage().getUserAuthor().get(), ev.getServer().get(), args));
                } else {
                    ev.getChannel().sendMessage(new KeywordsUtil(factoid, ev.getMessage().getUserAuthor().get(), ev.getServer().get(), args).replace());
                }
            }
        }
    }

    public String getFactoid(String tag) {
        String taag = storage.getTag(tag.toLowerCase());
        return taag.startsWith("?") ? storage.getTag(taag.split(" ")[0].substring(1).toLowerCase()) : taag;
    }

}
