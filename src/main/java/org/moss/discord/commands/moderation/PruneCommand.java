package org.moss.discord.commands.moderation;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang.StringUtils;

public class PruneCommand implements CommandExecutor {

    @Command(aliases = {"!prune"}, usage = "!prune <amount>", description = "Prunes a certain amount of messages (between 2 and 100)")
    public void onCommand(TextChannel channel, String[] args, Member author, Message message) {
        if (author.hasPermission(Permission.KICK_MEMBERS) && StringUtils.isNumeric(args[0])) {
            int amount = Integer.parseInt(args[0])+1;

            channel.getHistoryBefore(message, amount).queue(messageHistory -> {
                messageHistory.getRetrievedHistory().forEach(channel::purgeMessages);
                channel.sendMessage("Deleted " + amount + " messages.").queue();
            });
        } else message.addReaction("\uD83D\uDC4E").queue();
    }

}