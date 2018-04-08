package org.moss.discord.commands.moderation;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class BanCommand implements CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(BanCommand.class);

    @Command(aliases = {"!ban"}, usage = "!ban <username> <reason>", description = "Bans a chosen user.")
    public void onCommand(DiscordApi api, TextChannel channel, String[] args) {
        // some code will go here I guess
    }

}