package org.moss.discord.listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.moss.discord.Constants;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AutoModListeners implements MessageCreateListener {

    DiscordApi api;
    List<String> blacklistedFiles;
    private Optional<TextChannel> modChannel;

    /*
    TODO: Add role bypass
     */

    public AutoModListeners(DiscordApi api) {
        this.api = api;
        modChannel = api.getTextChannelById(Constants.CHANNEL_MODLOG);
        blacklistedFiles = new ArrayList<>(Arrays.asList(".jar", ".exe", ".zip")); //TODO configurable
    }

    @Override
    public void onMessageCreate(MessageCreateEvent ev) {
        for (MessageAttachment messageAttachment :  ev.getMessage().getAttachments()) {
            String fileName = messageAttachment.getFileName();
            if (blacklistedFiles.contains(fileName.substring(fileName.lastIndexOf('.')))) {
                ev.getMessage().delete("Blacklisted File: " + fileName);
                logMessage(ev.getMessage().getUserAuthor(), fileName, ev.getChannel().getIdAsString());
            }
        }
    }

    public void logMessage(Optional<User> user, String fileName, String chanId) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor("FILE");
        embed.setTitle("Attempted blacklist file post");
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
