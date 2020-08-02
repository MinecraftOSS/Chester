package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.apache.commons.lang.ArrayUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.moss.chesterapi.ChesterPlugin;
import org.moss.chesterapi.Factoids.Factoid;
import org.moss.discord.Chester;
import org.moss.discord.storage.UserFactoidStorage;
import org.moss.discord.util.EmbedUtil;
import org.moss.discord.util.KeywordsUtil;

import java.awt.*;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringJoiner;

public class UserFactoids extends Chester implements ChesterPlugin, MessageCreateListener {

    private EmbedUtil embedUtil = new EmbedUtil();
    private UserFactoidStorage storage = new UserFactoidStorage();

    @Override
    public void init() {
        getDiscordApi().addListener(this);
        getCommandHandler().registerCommand(this);
    }

    @Command(aliases = {"!utagraw"}, usage = "!utagraw <name>", description = "Info of a user tag")
    public void onInfo(DiscordApi api, TextChannel channel, String[] args, User user, Server server) {
        if (args.length >= 1 && storage.getFactoid(server, args[0]) != null) {
            Factoid userTag = storage.getFactoid(server, args[0]);
            channel.sendMessage(user.getMentionTag(), new EmbedBuilder().setTitle(args[0] + " info").addInlineField("Created by", String.format("<@%s>", userTag.getOwner())).addInlineField("Modified on", Date.from(Instant.parse(userTag.getModified())).toString()).addField("Content",String.format("```%s```", userTag.getContent())));
        }
    }

    @Command(aliases = {"!utags"}, usage = "!utags [filter]", description = "List all currently enabled user tags.")
    public void onList(DiscordApi api, TextChannel channel, Server server, User user) {
        String s = "";
        ArrayList<String> tagsList = new ArrayList<>(storage.getFactoidsFromServer(server).keySet());
        Collections.sort(tagsList);
        for (String key : tagsList) {
            s += String.format("`%s` ", key);
        }
        channel.sendMessage(new EmbedBuilder().addField("Active User Tags", s).setColor(Color.GREEN)); //.setFooter(String.format("%d users whitelisted", data.whitelist.size())));
    }


    @Command(aliases = {"!utagset"}, usage = "!utagset <name> [message]", description = "Set a user tag")
    public void onSet(DiscordApi api, TextChannel channel, String[] args, User user, Server server) {
        String key = args[0].toLowerCase();
//        if (!channel.getIdAsString().equals(Constants.CHANNEL_RANDOM) && !server.canKickUsers(user)) {
//            return;
//        }
        if (key.matches("\\s|\\n")) {
            channel.sendMessage("Invalid factoid name!");
            return;
        }
        if (args.length >= 2 && storage.getFactoid(server, key) != null) {
            if (!user.getIdAsString().equals(storage.getFactoid(server, key).getOwner())) {
                channel.sendMessage(new EmbedBuilder().setTitle("Tag already exist").setColor(Color.RED));
                return;
            }
        }
        if (args.length >= 2) {
            StringJoiner sb = new StringJoiner(" ");
            for(int i = 1; i < args.length; i++) {
                sb.add(args[i]);
            }
            Factoid newTag = new Factoid().setName(key).setContent(sb.toString()).setOwner(user.getIdAsString());
            storage.setFactoid(server, newTag);
            channel.sendMessage(new EmbedBuilder().setTitle("User Tag set!").setColor(Color.GREEN));
        }
    }

    @Command(aliases = {"!utagunset", "!tagrelease"}, usage = "!utagunset <name> [message]", description = "Unset a user tag")
    public void onUnset(DiscordApi api, TextChannel channel, String[] args, User user, Server server) {
        if (args.length >= 1 && storage.getFactoid(server, args[0]) != null) {
            if (storage.getFactoid(server, args[0]).getOwner().equals(user.getIdAsString()) || server.canBanUsers(user)) {
                storage.deleteFactoid(server, args[0]);
                channel.sendMessage(new EmbedBuilder().setTitle("Tag released!").setColor(Color.GREEN));
            } else {
                channel.sendMessage(new EmbedBuilder().setTitle("Tag is not yours!").setColor(Color.RED));
            }
        }
    }

//    @Command(aliases = {"!utw"}, usage = "!utw", description = "Adds user to user tag whitelist.")
//    public void onadd(DiscordApi api, TextChannel channel, Server server, MessageAuthor author, Message message) {
//        if (author.canKickUsersFromServer() && message.getMentionedUsers().size() >= 1) {
//            for (User target : message.getMentionedUsers()) {
//                String id = target.getIdAsString();
//                if (data.whitelist.contains(id)) {
//                    data.whitelist.remove(id);
//                } else {
//                    data.whitelist.add(id);
//                }
//            }
//            saveTags();
//            channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle("User(s) added/removed"));
//        }
//    }

    @Override
    public void onMessageCreate(MessageCreateEvent ev) {
        String message = ev.getMessage().getContent();
        if (message.startsWith("??") && message.length() >= 2) {
            String[] args = message.split(" ");
            String tag = args[0].substring(2).toLowerCase();
            args = (String[]) ArrayUtils.remove(args, 0);
            Factoid ufactoid = storage.getFactoid(ev.getServer().orElse(null), tag);
            if (ufactoid != null) {
                String factoid = ufactoid.getContent();
                if (factoid.startsWith("<del>")) {
                    ev.getMessage().delete();
                    factoid = factoid.substring(5);
                }
                if (factoid.startsWith("<embed>") || factoid.startsWith("<json>")) {
                    ev.getChannel().sendMessage(embedUtil.parseString(factoid, ev.getMessage().getUserAuthor().get(), ev.getServer().get(), args));
                } else {
                    ev.getChannel().sendMessage(new KeywordsUtil(factoid, ev.getMessage().getUserAuthor().get(), ev.getServer().get(), args).replace());
                }
            }
        }
    }

}
