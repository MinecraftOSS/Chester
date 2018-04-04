package org.moss.discord.listeners;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.moss.discord.Constants;
import org.moss.discord.storage.StarboardStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StarboardListener implements ReactionAddListener {

    private static final Logger logger = LoggerFactory.getLogger(StarboardListener.class);
    private static Collection<String> starEmojis = Arrays.asList(Constants.EMOJI_STARS_UNICODE);

    private DiscordApi api;
    private StarboardStorage storage = new StarboardStorage();

    public StarboardListener(DiscordApi dApi) {
        api = dApi;
    }

    @Override
    public void onReactionAdd(ReactionAddEvent event) {
        if (storage.isStarred(event.getMessageId()) || storage.isStarboardMessage(event.getMessageId())) return;

        Message message = event.requestMessage().join();
        
        int totalStars = message.getReactions().stream()
            .filter(r -> r.getEmoji().isUnicodeEmoji())
            .filter(r -> isStar(r.getEmoji()))
            .mapToInt(Reaction::getCount)
            .sum();

        if (totalStars >= Constants.STARS_MINIMUM) {
            postStarboard(message);
        }
    }
    
    private void postStarboard(Message message) {
        Optional<TextChannel> starboardChannel = api.getTextChannelById(Constants.CHANNEL_STARBOARD);
        if (!starboardChannel.isPresent()) return; // Starboard is disabled

        EmbedBuilder embed = new EmbedBuilder();

        String author = message.getAuthor().getDiscriminatedName();
        String channel = message.getServerTextChannel().get().getMentionTag();
        String content = message.getContent();

        embed.setAuthor(message.getAuthor());
        embed.setColor(new Color(16763904));
        embed.setDescription("```markdown\n" + content + "\n```");
        embed.setTimestamp(message.getCreationTimestamp());
        embed.setThumbnail("https://cdn.discordapp.com/attachments/397536210236604427/431107224308547604/ecMd5Gecn.png");
        embed.addInlineField("Author", author);
        embed.addInlineField("Channel", channel);
        embed.setFooter("Posted");

        starboardChannel.get().sendMessage(embed).thenAcceptAsync(starMessage -> {
            storage.set(message.getIdAsString(), starMessage.getIdAsString());
            logger.info("Posted {} to starboard as {}", message.getId(), starMessage.getId());
        }).exceptionally(t -> {
            logger.warn("Failed to post to starboard", t);
            return null;
        });
    }

    private static boolean isStar(Emoji emoji) {
        return starEmojis.contains(emoji.asUnicodeEmoji().orElseGet(() -> emoji.asCustomEmoji().get().getName()));
    }

}