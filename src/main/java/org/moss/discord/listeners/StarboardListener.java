package org.moss.discord.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.moss.discord.Constants;
import org.moss.discord.storage.StarboardStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class StarboardListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(StarboardListener.class);
    private static final Collection<String> starEmojis = Arrays.asList(Constants.EMOJI_STARS_UNICODE);

    private final JDA api;
    private final StarboardStorage storage = new StarboardStorage();

    public StarboardListener(JDA dApi) {
        api = dApi;
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if (storage.isStarred(event.getMessageIdLong()) || storage.isStarboardMessage(event.getMessageIdLong())) return;

        event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
            long totalStars = countStars(message.getReactions());

            if (totalStars >= Constants.STARS_MINIMUM) {
                postStarboard(message);
            }
        });
    }

    private long countStars(List<MessageReaction> reactions) {
        return reactions.stream()
            .filter(r -> isStar(r.getReactionEmote()))
            .map(MessageReaction::retrieveUsers)
            .distinct()
            .count();
    }
    
    private void postStarboard(Message message) {
        TextChannel starboardChannel = api.getTextChannelById(Constants.CHANNEL_STARBOARD);
        if (starboardChannel == null) return; // Starboard is disabled

        EmbedBuilder embed = new EmbedBuilder();

        String author = message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator();
        String channel = message.getTextChannel().getAsMention();
        String content = message.getContentRaw();

        embed.setAuthor(message.getAuthor().getName());
        embed.setColor(Constants.COLOR_STARBOARD);
        embed.setDescription("```markdown\n" + content + "\n```");
        embed.setTimestamp(message.getTimeCreated());
        embed.setThumbnail("https://cdn.discordapp.com/attachments/397536210236604427/431107224308547604/ecMd5Gecn.png");
        embed.addField("Author", author, true);
        embed.addField("Channel", channel, true);
        embed.setFooter("Posted");

        starboardChannel.sendMessage(embed.build()).queue(starMessage -> {
            storage.set(message.getId(), starMessage.getId());
            logger.info("Posted {} to starboard as {}", message.getId(), starMessage.getId());
        });
    }

    private static boolean isStar(MessageReaction.ReactionEmote emoji) {
        if (emoji.isEmoji()) {
            return starEmojis.contains(emoji.getEmoji());
        }
        return starEmojis.contains(emoji.getEmote().getName());
    }

}