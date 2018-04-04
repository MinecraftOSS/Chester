package org.moss.discord.listeners;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.moss.discord.Constants;
import org.moss.discord.storage.StarboardStorage;

public class StarboardListener implements ReactionAddListener {

    private DiscordApi api;
    private static Collection<Character> starEmojis = Arrays.asList((char) 0x1F31F, (char) 0x2B50, (char) 0x1F954);

    private StarboardStorage storage = new StarboardStorage();

    public StarboardListener(DiscordApi dApi) {
        api = dApi;
    }

	@Override
	public void onReactionAdd(ReactionAddEvent event) {
        if (storage.isStarred(event.getMessageId()) || storage.isStarboardMessage(event.getMessageId())) return;

        Optional<Message> mOptional = event.getMessage();
        Message message = mOptional.orElseGet(() -> 
            api.getMessageById(event.getMessageId(), event.getChannel()).join());

        int totalStars = message.getReactions().stream()
            .filter(r -> r.getEmoji().isUnicodeEmoji())
            .filter(r -> starEmojis.contains(r.getEmoji().asUnicodeEmoji().get().charAt(0)))
            .mapToInt(Reaction::getCount)
            .sum();
        
        System.out.println("Total stars: " + totalStars);

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

        embed.setTitle(author + " in " + channel);
        embed.setAuthor(message.getAuthor());
        embed.setColor(new Color(16763904));
        embed.setDescription("```markdown\n" + content + "\n```");
        embed.setTimestamp(message.getCreationTimestamp());

        Message starMessage = starboardChannel.get().sendMessage(embed).join();
        storage.set(message.getIdAsString(), starMessage.getIdAsString());
    }

}