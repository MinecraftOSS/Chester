package org.moss.discord.listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.moss.discord.Constants;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoModListeners implements MessageCreateListener {

    DiscordApi api;
    List<String> blacklistedFiles;
    List<String> censoredWords;
    private Optional<TextChannel> modChannel;
    private Map<Long, Instant> active = new HashMap<>();
    String[] donts = {
            "Do not",
            "Don't",
            "Stop",
            "Huge Mistake",
            "Bad Idea",
            "No no no",
            "apologize",
            "Say sorry",
            "You will now be terminated",
            "Prepare for your termination",
            "We're coming for you",
            "You've been added to the naughty list",
            "EXTERMINATE!",
            "The ban-hammer awaits you",
            "Hell will now be unleashed"
    };

    String[] nopes = {
            "https://i.imgur.com/z8UBrh5.png",
            "https://i.imgur.com/J015ZK5.png",
            "https://i.imgur.com/RJAtyAg.png",
            "https://i.imgur.com/aPVrnKl.gif",
            "https://media0.giphy.com/media/rFvtiIevmj0zu/giphy.gif",
            "https://i.imgur.com/dTMWtkp.png",
            "https://i.imgur.com/KbsSxlR.gif",
            "https://i.imgur.com/8r5mriG.jpg",
            "https://i.imgur.com/ced9xSC.gif",
            "https://i.imgur.com/HFRKIpb.gif",
            "https://i.imgur.com/ebngidh.gif",
            "https://i.imgur.com/7h0YLxS.gif"
    };

    List<String> nodonts = new ArrayList<>(Arrays.asList("430125651559710720", "430125667062120449", "442304736587415574", "479919881702342676", "479919848177139713"));

    public AutoModListeners(DiscordApi api) {
        this.api = api;
        modChannel = api.getTextChannelById(Constants.CHANNEL_MODLOG);
        blacklistedFiles = new ArrayList<>(Arrays.asList(".jar", ".exe", ".zip")); //TODO configurable
        censoredWords = new ArrayList<>(Arrays.asList("discord.gg", "discordapp.com/invite", "blackspigot", "amazingsexdating.com", "whatsappx.com", "bestoffersx.com", "kidsearncash.com", "l10n\\_2\\.x", "38875", "getbukkit"));
    }

    @Override
    public void onMessageCreate(MessageCreateEvent ev) {
        if (ev.getMessage().getAuthor().isYourself() || ev.getMessage().getAuthor().canKickUsersFromServer()) {
            active.put(ev.getMessage().getAuthor().getId(), Instant.now());
            return;
        }

        parsePings(ev.getMessage());

        if (!ev.getMessage().getEmbeds().isEmpty()) {
            for (Embed embed : ev.getMessage().getEmbeds()) {
                String title = embed.getTitle().orElse("");
                System.out.println(title);
                for (String pattern : censoredWords) {
                    if (title.toLowerCase().contains(pattern)) {
                        ev.getMessage().delete("Pattern trigger: " + pattern);
                        logCensorMessage(ev.getMessage().getUserAuthor(), pattern, ev.getChannel().getIdAsString());
                        return;
                    }
                }
            }
        }

        for (MessageAttachment messageAttachment : ev.getMessage().getAttachments()) {
            String fileName = messageAttachment.getFileName();
            if (blacklistedFiles.contains(fileName.substring(fileName.lastIndexOf('.')))) {
                ev.getMessage().delete("Blacklisted File: " + fileName);
                logFileMessage(ev.getMessage().getUserAuthor(), fileName, ev.getChannel().getIdAsString());
                return;
            }
        }

        String message = ev.getMessage().getContent();
        for (String pattern : censoredWords) {
            Matcher mat = Pattern.compile(pattern).matcher(message.toLowerCase());
            if (mat.find()) {
                ev.getMessage().delete("Pattern trigger: " + pattern);
                logCensorMessage(ev.getMessage().getUserAuthor(), pattern, ev.getChannel().getIdAsString());
                return;
            }
        }
    }

    public void parsePings(Message message) {
        if (message.getMentionedUsers().size() >= 1 && !nodonts.contains(message.getChannel().getIdAsString())) {
            for (User user : message.getMentionedUsers()) {
                if (message.getServer().get().canKickUsers(user) && !userIsActive(user.getId())) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setColor(Color.RED);
                    embed.setImage(nopes[ThreadLocalRandom.current().nextInt(nopes.length)]);
                    embed.setDescription("It looks like you're trying to randomly ping a staff");
                    embed.setFooter(donts[ThreadLocalRandom.current().nextInt(donts.length)]);
                    message.getChannel().sendMessage(message.getUserAuthor().get().getMentionTag(),embed).thenAcceptAsync(msg -> api.getThreadPool().getScheduler().schedule((Callable<CompletableFuture<Void>>) msg::delete, 30, TimeUnit.MINUTES));
                    message.getUserAuthor().get().sendMessage(embed);
                    break;
                }
            }
        }
    }

    private boolean userIsActive(long userId) {
        if (!active.keySet().contains(userId)) {
            return false;
        }
        return (Duration.between(active.get(userId), Instant.now()).toMinutes() <= 15);
    }

    public void logCensorMessage(Optional<User> user, String pattern, String chanId) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor("CENSOR");
        embed.setColor(Color.CYAN);
        embed.setThumbnail("https://i.imgur.com/bYGnGCp.png");

        embed.addInlineField("Author", user.get().getMentionTag());
        embed.addInlineField("Channel", String.format("<#%s>", chanId));

        embed.addField("Pattern", String.format("```%s```", pattern));

        embed.setFooter(user.get().getIdAsString());
        embed.setTimestamp(Instant.now());
        modChannel.get().sendMessage(embed);
    }

    public void logFileMessage(Optional<User> user, String fileName, String chanId) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor("FILE");
        embed.setColor(Color.CYAN);
        embed.setThumbnail("https://i.imgur.com/bYGnGCp.png");

        embed.addInlineField("Author", user.get().getMentionTag());
        embed.addInlineField("Channel", String.format("<#%s>", chanId));

        embed.addField("File", String.format("```%s```", fileName));

        embed.setFooter(user.get().getIdAsString());
        embed.setTimestamp(Instant.now());

        modChannel.get().sendMessage(embed);
    }
}