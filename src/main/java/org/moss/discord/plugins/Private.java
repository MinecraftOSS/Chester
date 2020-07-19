package org.moss.discord.plugins;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.moss.discord.Chester;
import org.moss.discord.Constants;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;

public class Private extends Chester implements MessageCreateListener {

    private Optional<TextChannel> privateChannel;

    public Private() {
        getDiscordApi().addListener(this);
        privateChannel = getDiscordApi().getTextChannelById(Constants.CHANNEL_PRIVATE);
    }

    @Override
    public void onMessageCreate(MessageCreateEvent ev) {
        String message = ev.getMessage().getContent();
        if (message.toLowerCase().startsWith("private")) {
            EmbedBuilder embed = new EmbedBuilder();
            String attachments = "";

            for (MessageAttachment attachment : ev.getMessage().getAttachments()) {
                attachments += "**Name:** " + attachment.getFileName() + "\n" + attachment.getUrl()+"\n";
            }

            embed.setAuthor(ev.getMessage().getAuthor());
            embed.setColor(Color.CYAN);
            embed.setThumbnail("https://i.imgur.com/zY8VBQ8.png");

            embed.addInlineField("Author", ev.getMessage().getUserAuthor().get().getMentionTag());
            embed.addInlineField("Channel", String.format("<#%s>", ev.getChannel().getId()));

            embed.addField("Message", "```"+message.replace("`", "")+"```");
            embed.addField("Attachments", attachments.isEmpty() ? "None" : attachments);

            embed.setTimestamp(Instant.now());

            privateChannel.get().sendMessage(embed);

            ev.getMessage().delete("Private");
        }
    }
}
