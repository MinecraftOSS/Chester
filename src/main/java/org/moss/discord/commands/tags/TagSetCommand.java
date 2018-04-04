package org.moss.discord.commands.tags;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class TagSetCommand implements CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TagSetCommand.class);

    @Command(aliases = {"!tagset"}, usage = "!tagset <name> [message]", description = "Set a new tag for this channel.")
    public void onCommand(DiscordApi api, TextChannel channel, String[] args) {
        
    }

}
