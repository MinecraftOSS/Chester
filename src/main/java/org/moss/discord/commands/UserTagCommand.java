package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang.ArrayUtils;
import org.moss.discord.Constants;
import org.moss.discord.util.EmbedUtil;
import org.moss.discord.util.KeywordsUtil;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.*;

public class UserTagCommand extends ListenerAdapter implements CommandExecutor {

    private UserTagData data = new UserTagData();
    private final EmbedUtil embedUtil = new EmbedUtil();
    private final ObjectMapper mapper = new ObjectMapper();

    public UserTagCommand(JDA api) {
        api.addEventListener(this);
        try {
            JsonNode jsonTags = mapper.readTree(new File("./user_tags.json")).get("data");
            data = mapper.readValue(jsonTags.toString(), UserTagData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Command(aliases = {"!utags"}, usage = "!utags [filter]", description = "List all currently enabled user tags.")
    public void onList(JDA api, TextChannel channel, Guild server, Member user) {
        if (!user.hasPermission(Permission.KICK_MEMBERS)) return;
        StringBuilder s = new StringBuilder();
        ArrayList<String> tagsList = new ArrayList<>(data.tagMap.keySet());
        Collections.sort(tagsList);
        for (String key : tagsList) {
            s.append(String.format("`%s` ", key));
        }
        channel.sendMessage(new EmbedBuilder().addField("Active User Tags", s.toString(), false).setColor(Color.GREEN).setFooter(String.format("%d users whitelisted", data.whitelist.size())).build()).queue();
    }


    @Command(aliases = {"!utagset"}, usage = "!utagset <name> [message]", description = "Set a user tag")
    public void onSet(JDA api, TextChannel channel, String[] args, Member user, Guild server) {
        String key = args[0].toLowerCase();
        if (!channel.getId().equals(Constants.CHANNEL_RANDOM) && !user.hasPermission(Permission.KICK_MEMBERS)) {
            return;
        }
        if (key.contains("\\n")) {
            channel.sendMessage("Please do not be a DoNotSpamPls, pls").queue();
            return;
        }
        if (args.length >= 2 && data.tagMap.containsKey(key)) {
            if (!user.getId().equals(data.tagMap.get(key).owner)) {
                channel.sendMessage(new EmbedBuilder().setTitle("Tag already exist").setColor(Color.RED).build()).queue();
                return;
            }
        }
        if (args.length >= 2) {
            StringJoiner sb = new StringJoiner(" ");
            for(int i = 1; i < args.length; i++) {
                sb.add(args[i]);
            }
            UserTag newTag = new UserTag().setName(key).setContent(sb.toString()).setOwner(user.getId());
            data.tagMap.put(key, newTag);
            saveTags();
            channel.sendMessage(new EmbedBuilder().setTitle("User Tag set!").setColor(Color.GREEN).build()).queue();
        }
    }

    @Command(aliases = {"!utagunset", "!tagrelease"}, usage = "!utagunset <name> [message]", description = "Unset a user tag")
    public void onUnset(JDA api, TextChannel channel, String[] args, Member user, Guild server) {
        if (args.length >= 1 && data.tagMap.containsKey(args[0].toLowerCase())) {
            if (data.tagMap.get(args[0].toLowerCase()).owner.equals(user.getId()) || user.hasPermission(Permission.KICK_MEMBERS)) {
                data.tagMap.remove(args[0].toLowerCase());
                saveTags();
                channel.sendMessage(new EmbedBuilder().setTitle("Tag released!").setColor(Color.GREEN).build()).queue();
            } else {
                channel.sendMessage(new EmbedBuilder().setTitle("Tag is not yours!").setColor(Color.RED).build()).queue();
            }
        }
    }

    @Command(aliases = {"!utaginfo", "!uti"}, usage = "!utaginfo <name>", description = "Info of a user tag")
    public void onInfo(JDA api, TextChannel channel, String[] args, Member user, Guild server) {
        if (!channel.getId().equals(Constants.CHANNEL_RANDOM) && !user.hasPermission(Permission.KICK_MEMBERS)) {
            return;
        }
        if (args.length >= 1 && data.tagMap.containsKey(args[0].toLowerCase())) {
            UserTag userTag = data.tagMap.get(args[0].toLowerCase());
            channel.sendMessage(new MessageBuilder().setContent(user.getAsMention()).setEmbed(new EmbedBuilder().setTitle(args[0] + " info").addField("Created by", String.format("<@%s>", userTag.owner), true).addField("Modified on", Date.from(Instant.parse(userTag.modified)).toString(), true).addField("Content",String.format("```%s```", userTag.content), false).build()).build()).queue();
        }
    }

    @Command(aliases = {"!utw"}, usage = "!utw", description = "Adds user to user tag whitelist.")
    public void onadd(JDA api, TextChannel channel, Guild server, Member author, Message message) {
        if (author.hasPermission(Permission.KICK_MEMBERS) && message.getMentionedUsers().size() >= 1) {
            for (User target : message.getMentionedUsers()) {
                String id = target.getId();
                if (data.whitelist.contains(id)) {
                    data.whitelist.remove(id);
                } else {
                    data.whitelist.add(id);
                }
            }
            saveTags();
            channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setTitle("User(s) added/removed").build()).queue();
        }
    }

    public void saveTags() {
        try {
            mapper.writerWithDefaultPrettyPrinter().withRootName("data").writeValue(new File("./user_tags.json"), data);
            //mapper.writerWithDefaultPrettyPrinter().withRootName("whitelist").writeValue(new File("./user_tags.json"), whitelist);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent ev) {
        String message = ev.getMessage().getContentRaw();
        if (message.startsWith("??")) {
            String[] args = message.split(" ");
            String tag = args[0].substring(2).toLowerCase();
            args = (String[]) ArrayUtils.remove(args, 0);
            if (data.tagMap.containsKey(tag)) {
                String factoid = data.tagMap.get(tag).content;
                if (factoid.startsWith("<del>")) {
                    ev.getMessage().delete().queue();
                    factoid = factoid.substring(5);
                }
                if (factoid.startsWith("<embed>") || factoid.startsWith("<json>")) {
                    ev.getChannel().sendMessage(embedUtil.parseString(factoid, ev.getMessage().getMember(), ev.getGuild(), args).build()).queue();
                } else {
                    ev.getChannel().sendMessage(new KeywordsUtil(factoid, ev.getMessage().getMember(), ev.getGuild(), args).replace()).queue();
                }
            }
        }
    }

    static class UserTagData {
        public Map<String, UserTag> tagMap = new HashMap<>();
        public Set<String> whitelist = new TreeSet<>();
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
