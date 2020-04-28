package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang.ArrayUtils;
import org.moss.discord.util.EmbedUtil;
import org.moss.discord.util.KeywordsUtil;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.*;

public class TagCommand extends ListenerAdapter implements CommandExecutor {

    private final Map<String, Factoid> tagMap = new HashMap<>();
    EmbedUtil util = new EmbedUtil();
    private final ObjectMapper mapper = new ObjectMapper();

    public TagCommand(JDA api) {
        api.addEventListener(this);
        try {
            JsonNode jsonTags = mapper.readTree(new File("./factoids.json")).get("tags");
            for (int i = 0; i < jsonTags.size(); i++) {
                Factoid userTag = mapper.readValue(jsonTags.get(i).toString(), Factoid.class);
                tagMap.put(userTag.name, userTag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Command(aliases = {"!tagraw", "?tagraw"}, usage = "!tagraw <name>", description = "Send the raw tag message to the channel.")
    public void onTagRaw(JDA api, TextChannel channel, String[] args, Member user, Guild server) {
        if (user == null) {
            return;
        }
        if (args.length >= 1 && tagMap.containsKey(args[0].toLowerCase()) && user.hasPermission(Permission.KICK_MEMBERS)) {
            Factoid userTag = tagMap.get(args[0].toLowerCase());
            channel.sendMessage(new MessageBuilder().setContent(user.getAsMention()).setEmbed(new EmbedBuilder().setTitle(args[0] + " info").addField("Created by", String.format("<@%s>", userTag.owner), true).addField("Modified on", Date.from(Instant.parse(userTag.modified)).toString(), true).addField("Content",String.format("```%s```", userTag.content), false).build()).build()).queue();
        }
    }

    @Command(aliases = {"!tagfile", "?tagfile"}, usage = "!tagfile", description = "Gets the tag file")
    public void onTagFile(TextChannel channel, Member user, Guild server) {
        if (user.hasPermission(Permission.BAN_MEMBERS)) {
            channel.sendFile(new File("./factoids.json")).queue();
        }
    }

    @Command(aliases = {"!tags", "?tags"}, usage = "!tags [filter]", description = "List all currently enabled tags.")
    public void onList(JDA api, TextChannel channel, Guild server, Member user) {
        if (!user.hasPermission(Permission.KICK_MEMBERS)) return;
        StringBuilder s = new StringBuilder();
        ArrayList<String> tagsList = new ArrayList<>(tagMap.keySet());
        Collections.sort(tagsList);
        for (String key : tagsList) {
            s.append(String.format("%s  ", key));
        }
        channel.sendMessage(new EmbedBuilder().setTitle("Active Tags").setDescription(String.format("```%s```", s.toString())).setColor(Color.GREEN).build()).queue();
    }

    @Command(aliases = {"!tagset", "?tagset"}, usage = "!tagset <name> [message]", description = "Set a new tag")
    public void onSet(JDA api, TextChannel channel, String[] args, Member user, Guild server) {
        if (args.length >= 2 && user.hasPermission(Permission.KICK_MEMBERS)) {
            String key = args[0].toLowerCase();
            if (key.contains("\\n")) {
                channel.sendMessage("Please do not be a DoNotSpamPls, pls").queue();
                return;
            }
            StringJoiner sb = new StringJoiner(" ");
            for(int i = 1; i < args.length; i++) {
                sb.add(args[i]);
            }
            Factoid newTag = new Factoid().setName(key).setContent(sb.toString()).setOwner(user.getId());
            tagMap.put(key, newTag);
            saveTags();
            channel.sendMessage(new EmbedBuilder().setTitle("Tag set!").setColor(Color.GREEN).build()).queue();
        }
    }

    @Command(aliases = {"!tagunset", "?tagunset"}, usage = "!tagunset <name> [message]", description = "Unset a tag")
    public void onUnset(JDA api, TextChannel channel, String[] args, Member user, Guild server) {
        if (args.length >= 1 && user.hasPermission(Permission.KICK_MEMBERS)) {
            tagMap.remove(args[0].toLowerCase());
            saveTags();
            channel.sendMessage(new EmbedBuilder().setTitle("Tag removed!").setColor(Color.GREEN).build()).queue();
        }
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent ev) {
        String message = ev.getMessage().getContentRaw();
        if (message.startsWith("?") && message.length() >= 2) {
            String[] args = message.split(" ");
            String tag = args[0].substring(1).toLowerCase();
            args = (String[]) ArrayUtils.remove(args, 0);
            if (tagMap.containsKey(tag)) {
                String factoid = getFactoid(tag);
                if (factoid.startsWith("<staff>")) {
                    factoid = factoid.substring(7);
                    if (!ev.getMember().hasPermission(Permission.KICK_MEMBERS)) return;
                }
                if (factoid.startsWith("<del>")) {
                    ev.getMessage().delete().queue();
                    factoid = factoid.substring(5);
                }
                if (factoid.startsWith("<embed>") || factoid.startsWith("<json>")) {
                    ev.getChannel().sendMessage(util.parseString(factoid, ev.getMessage().getMember(), ev.getGuild(), args).build()).queue();
                } else {
                    ev.getChannel().sendMessage(new KeywordsUtil(factoid, ev.getMessage().getMember(), ev.getGuild(), args).replace()).queue();
                }
            }
        }
    }

    public String getFactoid(String tag) {
        String taag = tagMap.get(tag.toLowerCase()).content;
        return taag.startsWith("?") ? tagMap.get(taag.split(" ")[0].substring(1).toLowerCase()).content : taag;
    }

    public void saveTags() {
        try {
            mapper.writerWithDefaultPrettyPrinter().withRootName("tags").writeValue(new File("./factoids.json"), tagMap.values());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Factoid {
        public String name;
        public String content;
        public String owner;
        public String modified;

        public Factoid setName(String name) {
            this.name = name;
            return this;
        }

        public Factoid setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public Factoid setContent(String content) {
            this.content = content;
            return this;
        }

        Factoid() {
            this.owner = "Chester";
            this.name = "";
            this.content = "";
            this.modified = Instant.now().toString();
        }
    }

}
