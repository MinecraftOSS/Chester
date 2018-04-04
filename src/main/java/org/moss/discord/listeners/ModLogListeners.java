package org.moss.discord.listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
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

import java.util.Optional;

public class ModLogListeners implements MessageEditListener, MessageDeleteListener, ServerMemberBanListener, ServerMemberJoinListener, ServerMemberLeaveListener {

    private DiscordApi api;
    private Optional<TextChannel> modChannel;

    /*
    ++ User Join/Leave
    +- User Ban/Kick
    ++ User Edit/Delete message
    TODO: Make prettier with emojis?
     */

    public ModLogListeners(DiscordApi api) {
        modChannel = api.getTextChannelById(Constants.CHANNEL_MODLOG);
        this.api = api;
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent ev) { //TODO Ignore if the user is not present. = Banned & Fix channels
        String messageFormat = "**Delete** \n **Chan:** %s \n **Author** %s \n **Message:** %s";
        modChannel.get().sendMessage(String.format(messageFormat, ev.getMessage().get().getChannel(), ev.getMessage().get().getUserAuthor().get().getMentionTag(), ev.getMessage().get().getContent()));
    }

    @Override
    public void onMessageEdit(MessageEditEvent ev) {
        String messageFormat = "**Edit** \n **Chan:** %s \n **Author** %s \n **Was:** %s \n **Now:** %s";
        modChannel.get().sendMessage(String.format(messageFormat, ev.getChannel(), ev.getMessage().get().getUserAuthor().get().getMentionTag(), ev.getOldContent().get(), ev.getNewContent()));
    }

    @Override
    public void onServerMemberBan(ServerMemberBanEvent ev) { //TODO Get by/reason from audit log
        String banMessage = "**BAN** (%s) \n **User:** %s %s \n **Reason**: %s \n **By:** %s";
        modChannel.get().sendMessage(String.format(banMessage, ev.getUser().getIdAsString(), ev.getUser().getDiscriminatedName(), ev.getUser().getMentionTag(), "Reason", "Me"));
    }

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent ev) {
        modChannel.get().sendMessage("**Join** " + ev.getUser().getMentionTag());
    }

    @Override
    public void onServerMemberLeave(ServerMemberLeaveEvent ev) {
        modChannel.get().sendMessage("**Leave** " + ev.getUser().getMentionTag());
    }
}
