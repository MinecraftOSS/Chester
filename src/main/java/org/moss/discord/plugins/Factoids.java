package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.apache.commons.lang.ArrayUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.moss.chesterapi.ChesterPlugin;
import org.moss.chesterapi.Factoids.Factoid;
import org.moss.discord.Chester;
import org.moss.discord.storage.FactoidStorage;
import org.moss.discord.util.EmbedUtil;
import org.moss.discord.util.KeywordsUtil;

import java.awt.*;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringJoiner;

public class Factoids extends Chester implements ChesterPlugin, MessageCreateListener {

    EmbedUtil util = new EmbedUtil();
    FactoidStorage storage = new FactoidStorage();

    @Override
    public void init() {
        getDiscordApi().addListener(this);
        getCommandHandler().registerCommand(this);
    }

    @Command(aliases = {"!tagraw", "?tagraw"}, usage = "!tagraw <name>", description = "Sends the raw tag message to the channel.")
    public void onTagRaw(TextChannel channel, String[] args, User user, Server server) {
        if (args.length >= 1 && storage.getFactoid(server, args[0]) != null && server.canKickUsers(user)) {
            Factoid factoid = storage.getFactoid(server, args[0]);
            if (factoid == null) {
                channel.sendMessage(user.getMentionTag(), new EmbedBuilder().setTitle("Unable to find tag").setColor(Color.RED));
                return;
            }
            channel.sendMessage(user.getMentionTag(), new EmbedBuilder().setTitle(args[0] + " info").addInlineField("Created by", String.format("<@%s>", factoid.getOwner())).addInlineField("Modified on", Date.from(Instant.parse(factoid.getModified())).toString()).addField("Content",String.format("```%s```", factoid.getContent())));
        }
    }

    @Command(aliases = {"!tags", "?tags"}, usage = "!tags", description = "List all currently enabled tags.")
    public void onList(TextChannel channel, Server server, User user) {
        if (!server.canKickUsers(user)) return;
        ArrayList<String> tagsList = new ArrayList<>(storage.getFactoidsFromServer(server).keySet());
        Collections.sort(tagsList);
        channel.sendMessage(new EmbedBuilder().setTitle(String.format("%s tags (%d)", server.getName(), tagsList.size())).setDescription(String.format("```%s```", String.join(", ",  tagsList))).setColor(Color.GREEN));
    }

    @Command(aliases = {"!tagset", "?tagset"}, usage = "!tagset <name> [message]", description = "Set a new tag")
    public void onSet(TextChannel channel, String[] args, User user, Server server) {
        if (args.length >= 2 && server.canKickUsers(user)) {
            String key = args[0].toLowerCase();
            if (key.matches("\\s|\\n")) {
                channel.sendMessage("Invalid factoid name!");
                return;
            }
            StringJoiner sb = new StringJoiner(" ");
            for(int i = 1; i < args.length; i++) {
                sb.add(args[i]);
            }
            Factoid newTag = new Factoid().setName(key).setContent(sb.toString()).setOwner(user.getIdAsString());
            storage.setFactoid(server, newTag);
            channel.sendMessage(new EmbedBuilder().setTitle("Tag set!").setColor(Color.GREEN));
        }
    }

    @Command(aliases = {"!tagunset", "?tagunset"}, usage = "!tagunset <name> [message]", description = "Unset a tag")
    public void onUnset(TextChannel channel, String[] args, User user, Server server) {
        if (args.length >= 1 && server.canKickUsers(user)) {
            if (storage.deleteFactoid(server, args[0].toLowerCase())) {
                channel.sendMessage(new EmbedBuilder().setTitle("Tag removed!").setColor(Color.GREEN));
            } else {
                channel.sendMessage(new EmbedBuilder().setDescription("Unable to remove tag, does it exist?").setColor(Color.RED));
            }
        }
    }

    @Override
    public void onMessageCreate(MessageCreateEvent ev) {
        Message message = ev.getMessage();
        String msgString = ev.getReadableMessageContent();
        if (msgString.startsWith("?") && msgString.length() >= 2) {
            String[] args = msgString.split(" ");
            String tag = args[0].substring(1).toLowerCase();
            args = (String[]) ArrayUtils.remove(args, 0);
            Factoid factoid = storage.getFactoid(message.getServer().orElse(null), tag);
            if (factoid != null) {
                String content = factoid.getContent().startsWith("?") ? getFactoid(message.getServer().get(), factoid) : factoid.getContent();
                if (content.startsWith("<staff>")) {
                    content = content.substring(7);
                    if (!ev.getMessageAuthor().canKickUsersFromServer()) return;
                }
                if (content.startsWith("<del>")) {
                    ev.getMessage().delete();
                    content = content.substring(5);
                }
                if (content.startsWith("<embed>")) {
                    ev.getChannel().sendMessage(util.parseString(content, message.getUserAuthor().orElse(null), ev.getServer().orElse(null), args));
                } else {
                    ev.getChannel().sendMessage(new KeywordsUtil(content, message.getUserAuthor().orElse(null), ev.getServer().orElse(null), args).replace());
                }
            }
        }
    }

    public String getFactoid(Server server, Factoid tag) {
        String taag = tag.getContent();
        return storage.getFactoid(server, taag.split(" ")[0].substring(1).toLowerCase()).getContent();
    }

}
