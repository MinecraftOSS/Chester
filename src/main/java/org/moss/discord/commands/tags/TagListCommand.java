package org.moss.discord.commands.tags;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class TagListCommand implements CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TagListCommand.class);

    @Command(aliases = {"!tags"}, usage = "!tags [filter]", description = "List all currently enabled tags.")
    public void onCommand(DiscordApi api, TextChannel channel, String[] args) {
        
    }

}
