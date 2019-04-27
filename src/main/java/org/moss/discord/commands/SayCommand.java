package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;

public class SayCommand implements CommandExecutor {

    @Command(aliases = {"!say", ".say"}, usage = "!say <channel> <words>", description = "Say stuff")
    public void onSay(TextChannel channel, String[] args, MessageAuthor author, Message message) {
        if (author.canKickUsersFromServer()) {
            if (message.getMentionedChannels().size() >= 1) {
                message.getMentionedChannels().get(0).sendMessage(String.join(" ", args).substring(args[0].length()));
            } else {
                 channel.sendMessage(String.join(" ", args));
            }
        }
    }
}
