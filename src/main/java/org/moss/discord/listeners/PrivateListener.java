package org.moss.discord.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.moss.discord.Constants;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;

public class PrivateListener extends ListenerAdapter {

    JDA api;
    private final PrivateChannel privateChannel;

    public PrivateListener(JDA api) {
        this.api = api;
        privateChannel = api.getPrivateChannelById(Constants.CHANNEL_PRIVATE);
    }

    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent ev) {
        String message = ev.getMessage().getContentRaw();
        if (message.toLowerCase().startsWith("private")) {
            EmbedBuilder embed = new EmbedBuilder();
            StringBuilder attachments = new StringBuilder();

            for (Message.Attachment attachment : ev.getMessage().getAttachments()) {
                attachments.append("**Name:** ").append(attachment.getFileName()).append("\n").append(attachment.getUrl()).append("\n");
            }

            embed.setAuthor(ev.getMessage().getAuthor().getName());
            embed.setColor(Color.CYAN);
            embed.setThumbnail("https://i.imgur.com/zY8VBQ8.png");

            embed.addField("Author", ev.getMessage().getAuthor().getAsMention(), true);
            embed.addField("Channel", String.format("<#%s>", ev.getChannel().getId()), true);

            embed.addField("Message", "```"+message.replace("`", "")+"```", false);
            embed.addField("Attachments", (attachments.length() == 0) ? "None" : attachments.toString(),false);

            embed.setTimestamp(Instant.now());

            privateChannel.sendMessage(embed.build()).queue();

            ev.getMessage().delete().reason("Private").queue();
        }
    }

}
