package org.moss.discord.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.moss.discord.Constants;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class ModLogListeners extends ListenerAdapter {

    private final TextChannel modChannel;

    private final Cache<String, Message> messageCache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .build();

    public ModLogListeners(JDA api) {
        modChannel = api.getTextChannelById(Constants.CHANNEL_MODLOG);
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        messageCache.put(event.getMessageId(), event.getMessage());
    }

    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent ev) {
        Message message = messageCache.getIfPresent(ev.getMessageId());

        //Check if the message isn't cached
        if (message == null) {
            return;
        }

        ev.getGuild().retrieveAuditLogs().type(ActionType.MESSAGE_DELETE).queue(auditLogEntries -> {
            String deletedBy = "Self";
            for (AuditLogEntry entry : auditLogEntries) {
                if (entry.getTargetId().equals(ev.getMessageId()) && entry.getTimeCreated().toInstant().isAfter(Instant.now().minus(Duration.ofMinutes(1)))) {
                    deletedBy = entry.getUser().getAsMention();
                }
            }
            EmbedBuilder embed = new EmbedBuilder();

            embed.setAuthor("DELETE");
            embed.setColor(Color.RED);
            embed.setThumbnail("https://i.imgur.com/bYGnGCp.png");

            embed.addField("Author", message.getAuthor().getAsMention(), true);
            embed.addField("Channel", String.format("<#%s>", ev.getChannel().getId()), true);
            embed.addField("Deleted by", deletedBy, true);

            embed.addField("Message", "```"+stripGrave(message.getContentStripped())+"```", false);

            embed.setFooter(message.getAuthor().getId());
            embed.setTimestamp(Instant.now());

            modChannel.sendMessage(embed.build()).queue();
            messageCache.invalidate(ev.getMessageId());
        });
    }

    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent ev) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor("EDIT");
        embed.setColor(Color.YELLOW);
        embed.setThumbnail("https://i.imgur.com/bYGnGCp.png");

        embed.addField("Author", ev.getMessage().getAuthor().getAsMention(), true);
        embed.addField("Channel", String.format("<#%s>", ev.getChannel().getId()), true);

        Message oldMessage = messageCache.getIfPresent(ev.getMessageId());
        embed.addField("Was", (oldMessage == null ? "Unknown" : "```"+stripGrave(oldMessage.getContentRaw())+"```"), false);
        embed.addField("Now", "```"+stripGrave(ev.getMessage().getContentRaw())+"```", false);
        messageCache.put(ev.getMessageId(), ev.getMessage());

        embed.setFooter(ev.getMessage().getAuthor().getId());
        embed.setTimestamp(Instant.now());

        modChannel.sendMessage(embed.build()).queue();
    }

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent ev) {
        ev.getGuild().retrieveAuditLogs().type(ActionType.BAN).queue(auditLogEntries -> {
            String bannedBy = "Unknown";
            String reason = "Because";
            for (AuditLogEntry entry : auditLogEntries) {
                if (entry.getTargetId().equals(ev.getUser().getId())) {
                    bannedBy = entry.getUser().getAsMention();
                    reason = entry.getReason();
                    break;
                }
            }
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor(ev.getUser().getName());
            embed.setColor(Color.PINK);
            embed.setThumbnail("https://i.imgur.com/8WJqz7B.png");

            embed.addField("Banned By: ", bannedBy, true);
            embed.addField("ID", ev.getUser().getId(), true);
            embed.addField("Reason", reason, false);

            embed.setFooter(ev.getUser().getId());
            embed.setTimestamp(Instant.now());

            modChannel.sendMessage(embed.build()).queue();
        });
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent ev) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor(ev.getUser().getName());
        embed.setTitle("Joined the server");
        embed.setColor(Color.GREEN);

        embed.addField("Created", Date.from(ev.getUser().getTimeCreated().toInstant()).toString(), false);

        embed.setFooter(ev.getUser().getId());
        embed.setTimestamp(Instant.now());

        modChannel.sendMessage(embed.build()).queue();
    }


    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent ev) {
        ev.getGuild().retrieveAuditLogs().type(ActionType.KICK).queue(auditLogEntries -> {
            String kickedBy = "Unknown";
            String kickReason = "Because";
            for (AuditLogEntry entry : auditLogEntries) {
                if (entry.getTargetId().equals(ev.getUser().getId())) {
                    kickedBy = entry.getUser().getAsMention();
                    kickReason = entry.getReason();
                    break;
                }
            }
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor(ev.getUser().getName());

            if (kickedBy.equalsIgnoreCase("unknown")) {
                embed.setTitle("Left the server");
                embed.setColor(Color.RED);
            } else {
                embed.setColor(Color.PINK);
                embed.setThumbnail("https://i.imgur.com/6TqbP9o.png");

                embed.addField("Kicked By: ", kickedBy, true);
                embed.addField("Reason", kickReason, false);
            }

            embed.setFooter(ev.getUser().getId());
            embed.setTimestamp(Instant.now());
            modChannel.sendMessage(embed.build()).queue();
        });
    }

    @Override
    public void onUserUpdateName(@Nonnull UserUpdateNameEvent ev) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor("NAME CHANGE");
        embed.setColor(Color.YELLOW);
        embed.setThumbnail("https://i.imgur.com/bYGnGCp.png");

        embed.addField("Old", ev.getOldName(), true);
        embed.addField("New", ev.getNewName(), true);

        embed.setFooter(ev.getUser().getId());
        embed.setTimestamp(Instant.now());

        modChannel.sendMessage(embed.build()).queue();
    }

    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent ev) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor("NICK CHANGE");
        embed.setColor(Color.YELLOW);
        embed.setThumbnail("https://i.imgur.com/bYGnGCp.png");

        embed.addField("Old", ev.getOldNickname(), true);
        embed.addField("New", ev.getNewNickname(), true);
        embed.addField("ID", ev.getUser().getId(), false);

        embed.setFooter(ev.getUser().getId());
        embed.setTimestamp(Instant.now());

        modChannel.sendMessage(embed.build()).queue();
    }

    public String stripGrave(String string) {
        return string.replace("`", "");
    }
}
