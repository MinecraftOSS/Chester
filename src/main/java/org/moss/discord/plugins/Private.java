package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.moss.chesterapi.ChesterPlugin;
import org.moss.discord.Chester;
import org.moss.discord.Constants;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;

public class Private extends Chester implements ChesterPlugin, MessageCreateListener {

    private Optional<TextChannel> privateChannel;

    @Override
    public void init() {
        getDiscordApi().addListener(this);
        getCommandHandler().registerCommand(this);
        privateChannel = getDiscordApi().getTextChannelById(Constants.CHANNEL_PRIVATE);
    }

    @Command(aliases = {"!private"}, usage = "!private", description = "Private stuff")
    public void onCommand(TextChannel channel, Message msg) {
        handleMessage(msg, channel.getType());
    }

    @Override
    public void onMessageCreate(MessageCreateEvent ev) {
        if (ev.getMessageAuthor().isYourself()) {
            return;
        }

        if (ev.getChannel().getType() == ChannelType.PRIVATE_CHANNEL) {
            handleMessage(ev.getMessage(), ev.getChannel().getType());
        }
    }


    public void handleMessage(Message msg, ChannelType channelType) {
        String message = msg.getReadableContent();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.CYAN);

        String attachments = "";

        for (MessageAttachment attachment : msg.getAttachments()) {
            attachments += "**Name:** " + attachment.getFileName() + "\n" + attachment.getUrl()+"\n";
        }

        embed.setAuthor(msg.getAuthor());
        embed.setThumbnail("https://i.imgur.com/zY8VBQ8.png");

        embed.addInlineField("Author", msg.getUserAuthor().get().getMentionTag());
        embed.addInlineField("Channel", channelType == ChannelType.PRIVATE_CHANNEL ? "Direct Message" : String.format("<#%s>", msg.getChannel().getId()));

        embed.addField("Message", message);

        embed.addField("Attachments", attachments.isEmpty() ? "None" : attachments);

        embed.setFooter(msg.getAuthor().getIdAsString());
        embed.setTimestamp(Instant.now());

        privateChannel.get().sendMessage(embed);
        msg.delete("Private");
    }

}
