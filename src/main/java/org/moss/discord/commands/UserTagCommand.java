package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.apache.commons.lang.ArrayUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.moss.discord.Constants;
import org.moss.discord.util.EmbedUtil;
import org.moss.discord.util.KeywordsUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class UserTagCommand implements CommandExecutor, MessageCreateListener {

    private Map<String, UserTag> tagMap = new HashMap<>();
    private EmbedUtil embedUtil = new EmbedUtil();
    private ObjectMapper mapper = new ObjectMapper();

    public UserTagCommand(DiscordApi api) {
        api.addListener(this);
        try {
            JsonNode jsonTags = mapper.readTree(new File("./user_tags.json")).get("tags");
            for (int i = 0; i < jsonTags.size(); i++) {
                UserTag userTag = mapper.readValue(jsonTags.get(i).toString(), UserTag.class);
                tagMap.put(userTag.name, userTag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Command(aliases = {"!utags"}, usage = "!utags [filter]", description = "List all currently enabled user tags.")
    public void onList(DiscordApi api, TextChannel channel, Server server, User user) {
        if (!server.canKickUsers(user)) return;
        String s = "";
        for (String key : tagMap.keySet()) {
            s += String.format("`%s` ", key);
        }
        channel.sendMessage(new EmbedBuilder().addField("Active Tags", s).setColor(Color.GREEN));
    }

    @Command(aliases = {"!utagset"}, usage = "!utagset <name> [message]", description = "Set a user tag")
    public void onSet(DiscordApi api, TextChannel channel, String[] args, User user, Server server) {
        String key = args[0].toLowerCase();
        if (!channel.getIdAsString().equals(Constants.CHANNEL_RANDOM) && !server.canKickUsers(user)) {
            return;
        }
        if (args.length >= 2 && tagMap.containsKey(key)) {
            if (!user.getIdAsString().equals(tagMap.get(key).owner)) {
                channel.sendMessage(new EmbedBuilder().setTitle("Tag already exist").setColor(Color.RED));
                return;
            }
        }
        if (args.length >= 2) {
            StringJoiner sb = new StringJoiner(" ");
            for(int i = 1; i < args.length; i++) {
                sb.add(args[i]);
            }
            UserTag newTag = new UserTag().setName(key).setContent(sb.toString()).setOwner(user.getIdAsString());
            tagMap.put(key, newTag);
            saveTags();
            channel.sendMessage(new EmbedBuilder().setTitle("Tag set!").setColor(Color.GREEN));
        }
    }

    @Command(aliases = {"!utagunset", "!tagrelease"}, usage = "!utagunset <name> [message]", description = "Unset a user tag")
    public void onUnset(DiscordApi api, TextChannel channel, String[] args, User user, Server server) {
        if (args.length >= 1 && tagMap.containsKey(args[0].toLowerCase())) {
            if (tagMap.get(args[0].toLowerCase()).owner.equals(user.getIdAsString()) || server.canKickUsers(user)) {
                tagMap.remove(args[0].toLowerCase());
                saveTags();
                channel.sendMessage(new EmbedBuilder().setTitle("Tag released!").setColor(Color.GREEN));
            } else {
                channel.sendMessage(new EmbedBuilder().setTitle("Tag is not yours!").setColor(Color.RED));
            }
        }
    }

    @Command(aliases = {"!utaginfo", "!uti"}, usage = "!utaginfo <name>", description = "Info of a user tag")
    public void onInfo(DiscordApi api, TextChannel channel, String[] args, User user, Server server) {
        if (!channel.getIdAsString().equals(Constants.CHANNEL_RANDOM) && !server.canKickUsers(user)) {
            return;
        }
        if (args.length >= 1 && tagMap.containsKey(args[0].toLowerCase())) {
            UserTag userTag = tagMap.get(args[0].toLowerCase());
            channel.sendMessage(user.getMentionTag(), new EmbedBuilder().setTitle(args[0] + " info").addInlineField("Created by", String.format("<@%s>", userTag.owner)).addInlineField("Modified on", Date.from(Instant.parse(userTag.modified)).toString()).addField("Content",String.format("```%s```", userTag.content)));
        }
    }

    public void saveTags() {
        try {
            mapper.writerWithDefaultPrettyPrinter().withRootName("tags").writeValue(new File("./user_tags.json"), tagMap.values());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageCreate(MessageCreateEvent ev) {
        String message = ev.getMessage().getContent();
        if (message.startsWith("??") && message.length() >= 2) {
            String[] args = message.split(" ");
            String tag = args[0].substring(2).toLowerCase();
            args = (String[]) ArrayUtils.remove(args, 0);
            if (tagMap.containsKey(tag)) {
                String factoid = tagMap.get(tag).content;
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

    static class UserTag {
        public String name;
        public String content;
        public String owner;
        public String modified;

        public UserTag setName(String name) {
            this.name = name;
            return this;
        }

        public UserTag setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public UserTag setContent(String content) {
            this.content = content;
            return this;
        }

        UserTag() {
            this.owner = "";
            this.name = "";
            this.content = "";
            this.modified = Instant.now().toString();
        }
    }

}
