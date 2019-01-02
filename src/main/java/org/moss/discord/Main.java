package org.moss.discord;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.moss.discord.commands.AvatarCommand;
import org.moss.discord.commands.BStatsCommand;
import org.moss.discord.listeners.StarboardListener;
import org.moss.discord.commands.GithubCommand;
import org.moss.discord.commands.MojangCommand;
import org.moss.discord.commands.NicknameCommand;
import org.moss.discord.commands.PresenceCommand;
import org.moss.discord.commands.TagCommand;
import org.moss.discord.commands.SpigetCommand;
import org.moss.discord.commands.moderation.BanCommand;
import org.moss.discord.commands.moderation.KickCommand;
import org.moss.discord.commands.moderation.PruneCommand;
import org.moss.discord.listeners.AutoModListeners;
import org.moss.discord.listeners.ModLogListeners;
import org.moss.discord.listeners.PrivateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    // The logger for this class.
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        if (args.length != 1) {
            logger.error("Invalid amount of arguments provided!");
            return;
        }

        DiscordApi api = new DiscordApiBuilder().setToken(args[0]).login().join();
        logger.info("Logged in to Discord account {}", api.getYourself().getName());

        // Create command handler
        CommandHandler commandHandler = new JavacordHandler(api);

        // Give bot owner all permissions.
        commandHandler.addPermission(String.valueOf(api.getOwnerId()), "*");

        // Register commands
        commandHandler.registerCommand(new BStatsCommand());
        commandHandler.registerCommand(new TagCommand(api));
        commandHandler.registerCommand(new GithubCommand());
        commandHandler.registerCommand(new BanCommand());
        commandHandler.registerCommand(new KickCommand());
        commandHandler.registerCommand(new PruneCommand());
        commandHandler.registerCommand(new MojangCommand());
        commandHandler.registerCommand(new PresenceCommand());
        commandHandler.registerCommand(new NicknameCommand());
        commandHandler.registerCommand(new AvatarCommand());
        commandHandler.registerCommand(new SpigetCommand());

        // Register listeners
        api.addListener(new ModLogListeners(api));
        api.addListener(new AutoModListeners(api));
        api.addListener(new PrivateListener(api));
        api.addReactionAddListener(new StarboardListener(api));

    }

}
