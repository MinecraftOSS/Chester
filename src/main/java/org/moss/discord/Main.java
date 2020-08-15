package org.moss.discord;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.moss.discord.commands.AvatarCommand;
import org.moss.discord.commands.BStatsCommand;
import org.moss.discord.commands.ColorsCommand;
import org.moss.discord.commands.CommandsCommand;
import org.moss.discord.commands.DatabaseCommand;
import org.moss.discord.commands.EightBallCommand;
import org.moss.discord.commands.EssentialsXCommand;
import org.moss.discord.commands.ProfileCommand;
import org.moss.discord.commands.RoleReactionCommand;
import org.moss.discord.commands.EmbedCommand;
import org.moss.discord.commands.SayCommand;
import org.moss.discord.commands.SpaceXCommand;
import org.moss.discord.commands.UserTagCommand;
import org.moss.discord.commands.VerifyCommand;
import org.moss.discord.commands.WolframAlphaCommand;
import org.moss.discord.commands.XkcdCommand;
import org.moss.discord.listeners.AntiHoistingListener;
import org.moss.discord.listeners.StarboardListener;
import org.moss.discord.commands.GithubCommand;
import org.moss.discord.commands.MojangCommand;
import org.moss.discord.commands.RoleCheckCommand;
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
import org.moss.discord.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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

        new DatabaseUtil(api).initSqlite(new File("chester.db"));

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
        commandHandler.registerCommand(new RoleCheckCommand());
        commandHandler.registerCommand(new PresenceCommand());
        commandHandler.registerCommand(new NicknameCommand());
        commandHandler.registerCommand(new AvatarCommand());
        commandHandler.registerCommand(new SpigetCommand());
        commandHandler.registerCommand(new CommandsCommand(commandHandler));
        commandHandler.registerCommand(new EssentialsXCommand());
        commandHandler.registerCommand(new RoleReactionCommand(api));
        commandHandler.registerCommand(new EmbedCommand());
        commandHandler.registerCommand(new SayCommand());
        commandHandler.registerCommand(new UserTagCommand(api));
        commandHandler.registerCommand(new SpaceXCommand());
        commandHandler.registerCommand(new XkcdCommand());
        commandHandler.registerCommand(new WolframAlphaCommand());
        commandHandler.registerCommand(new EightBallCommand());
        commandHandler.registerCommand(new ColorsCommand());
        commandHandler.registerCommand(new VerifyCommand());
        commandHandler.registerCommand(new ProfileCommand());
        commandHandler.registerCommand(new DatabaseCommand());

        // Register listeners
        api.addListener(new ModLogListeners(api));
        api.addListener(new AutoModListeners(api, commandHandler));
        api.addListener(new PrivateListener(api));
        api.addReactionAddListener(new StarboardListener(api));
        api.addListener(new AntiHoistingListener(api));

    }

}
