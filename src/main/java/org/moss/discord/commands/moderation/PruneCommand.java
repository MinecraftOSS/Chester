package org.moss.discord.commands.moderation;

import org.javacord.api.entity.channel.TextChannel;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.util.logging.ExceptionLogger;

public class PruneCommand implements CommandExecutor {

    @Command(aliases = {"!prune"}, usage = "!prune <amount>", description = "Prunes a certain amount of messages (between 2 and 100)")
    public void onCommand(TextChannel channel, String[] args) {
        int amount = Integer.parseInt(args[0]);

        channel.getMessages(amount).thenCompose(MessageSet::deleteAll).exceptionally(ExceptionLogger.get());
        channel.sendMessage("Deleted " + amount + " messages.");
    }

}