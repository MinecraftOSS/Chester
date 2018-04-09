package org.moss.discord;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.moss.discord.commands.BStatsCommand;
import org.moss.discord.commands.moderation.BanCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    /**
     * The logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            logger.error("Invalid amount of arguments provided!");
            return;
        }

        // Logging in
        DiscordApi api = new DiscordApiBuilder().setToken(args[0]).login().join();
        logger.info("Logged in to Discord account {}", api.getYourself().getName());

        // Create command handler
        CommandHandler commandHandler = new JavacordHandler(api);

        // Give the bot owner all permissions
        commandHandler.addPermission(String.valueOf(api.getOwnerId()), "*");

        // Register commands
        commandHandler.registerCommand(new BStatsCommand());
        commandHandler.registerCommand(new BanCommand());
    }

}
