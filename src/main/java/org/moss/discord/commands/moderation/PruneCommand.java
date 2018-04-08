package org.moss.discord.commands.moderation;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class PruneCommand implements CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(PruneCommand.class);

    @Command(aliases = {"!prune"}, usage = "!prune <amount>", description = "Prunes a certain amount of messages (between 2 and 100)")
    public void onCommand(DiscordApi api, TextChannel channel, String[] args) {
        // some code will go here I guess
    }

}