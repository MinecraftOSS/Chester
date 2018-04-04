package org.moss.discord.listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.event.server.member.ServerMemberBanEvent;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.server.member.ServerMemberBanListener;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;
import org.moss.discord.Constants;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;

public class ModLogListeners implements MessageEditListener, MessageDeleteListener, ServerMemberBanListener, ServerMemberJoinListener, ServerMemberLeaveListener {

    private DiscordApi api;
    private Optional<TextChannel> modChannel;

    /*
    ++ User Join/Leave
    +- User Ban/Kick
    ++ User Edit/Delete message
     */

    public ModLogListeners(DiscordApi api) {
        modChannel = api.getTextChannelById(Constants.CHANNEL_MODLOG);
        this.api = api;
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent ev) {
        try {
            Message message = ev.getMessage().orElseGet(null); //TODO
        } catch (Exception e) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor("DELETE");
        embed.setColor(Color.RED);
        embed.setThumbnail("https://i.imgur.com/bYGnGCp.png");

        embed.addInlineField("Author", ev.getMessage().get().getAuthor().getDiscriminatedName());
        embed.addInlineField("Channel", String.format("<#%s>", ev.getChannel().getId()));

        embed.addField("Message", "```"+ev.getMessage().get().getContent()+"```");

        embed.setFooter("Time");
        embed.setTimestamp(ev.getMessage().get().getCreationTimestamp());

        modChannel.get().sendMessage(embed);
    }

    @Override
    public void onMessageEdit(MessageEditEvent ev) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor("EDIT");
        embed.setColor(Color.YELLOW);
        embed.setThumbnail("https://i.imgur.com/bYGnGCp.png");

        embed.addInlineField("Author", ev.getMessage().get().getAuthor().getDiscriminatedName());
        embed.addInlineField("Channel", String.format("<#%s>", ev.getChannel().getId()));

        embed.addField("Was", "```"+ev.getOldContent().get()+"```");
        embed.addField("Now", "```"+ev.getNewContent()+"```");

        embed.setFooter("Time");
        embed.setTimestamp(ev.getMessage().get().getCreationTimestamp());

        modChannel.get().sendMessage(embed);
    }

    @Override
    public void onServerMemberBan(ServerMemberBanEvent ev) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor(ev.getUser());
        embed.setColor(Color.YELLOW);
        embed.setThumbnail("https://i.imgur.com/8WJqz7B.png");

        embed.addInlineField("Banned By: ", "Chester"); //TODO
        embed.addInlineField("ID", ev.getUser().getIdAsString());
        embed.addField("Reason", "Audit Log");

        embed.setFooter("Banned");
        embed.setTimestamp(Instant.now());

        modChannel.get().sendMessage(embed);
    }

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent ev) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor(ev.getUser());
        embed.setTitle("Joined the server");
        embed.setColor(Color.GREEN);

        modChannel.get().sendMessage(embed);
    }

    @Override
    public void onServerMemberLeave(ServerMemberLeaveEvent ev) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor(ev.getUser());
        embed.setTitle("Left the server");
        embed.setColor(Color.RED);

        modChannel.get().sendMessage(embed);
    }
}
