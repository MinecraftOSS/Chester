package org.moss.discord.listeners;

import java.awt.Color;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.moss.discord.Constants;

public class StarboardListener implements ReactionAddListener {

    // TODO: Store this permanently, obviously this isn't going to work in-memory
    private Set<Long> starredMessages = new HashSet<Long>();

	@Override
	public void onReactionAdd(ReactionAddEvent event) {
        if (!(event.getEmoji().equalsEmoji("🌟") || event.getEmoji().equalsEmoji("⭐"))) return;
        if (starredMessages.contains(event.getMessageId())) return;

        int totalStars = event.getMessage().get().getReactions().stream()
            .filter(r -> r.getEmoji().equalsEmoji("🌟") || r.getEmoji().equalsEmoji("⭐"))
            .mapToInt(Reaction::getCount)
            .sum();

        if (totalStars >= Constants.STARS_MINIMUM) {
            postStarboard(event.getMessage().get());
        }
    }
    
    private void postStarboard(Message message) {
        Optional<ServerChannel> starboardChannel = message.getServer().get().getChannelById(Constants.CHANNEL_STARBOARD);
        if (!starboardChannel.isPresent()) return; // Starboard is disabled

        EmbedBuilder embed = new EmbedBuilder();

        String author = message.getAuthor().getName();
        String channel = message.getServerTextChannel().get().getName();
        String content = message.getContent();

        embed.setTitle(author + " in " + channel);
        embed.setAuthor(message.getAuthor());
        embed.setColor(Color.ORANGE);
        embed.setDescription(content);

        starboardChannel.get().asTextChannel().get().sendMessage(embed).join();
    }

}