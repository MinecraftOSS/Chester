package org.moss.discord.commands.tags;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class TagCommand implements CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TagCommand.class);

    @Command(aliases = {"!tag", "?"}, usage = "!tag <name>", description = "Send the tag message to the channel.")
    public void onCommand(DiscordApi api, TextChannel channel, String[] args) {

    }

}
