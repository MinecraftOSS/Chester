package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class SayCommand implements CommandExecutor {

    @Command(aliases = {"!say", ".say"}, usage = "!say <channel> <words>", description = "Say stuff")
    public void onSay(TextChannel channel, String[] args, Member author, Message message) {
        if (author.hasPermission(Permission.KICK_MEMBERS)) {
            if (message.getMentionedChannels().size() >= 1) {
                message.getMentionedChannels().get(0).sendMessage(String.join(" ", args).substring(args[0].length())).queue();
            } else {
                 channel.sendMessage(String.join(" ", args)).queue();
            }
        }
    }
}
