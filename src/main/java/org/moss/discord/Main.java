package org.moss.discord;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JDA4Handler;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.moss.discord.commands.*;
import org.moss.discord.commands.moderation.BanCommand;
import org.moss.discord.commands.moderation.KickCommand;
import org.moss.discord.commands.moderation.PruneCommand;
import org.moss.discord.listeners.AutoModListeners;
import org.moss.discord.listeners.ModLogListeners;
import org.moss.discord.listeners.PrivateListener;
import org.moss.discord.listeners.StarboardListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class Main {

    // The logger for this class.
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws LoginException, InterruptedException {

        if (args.length != 1) {
            logger.error("Invalid amount of arguments provided!");
            return;
        }

        JDA api = new JDABuilder(AccountType.BOT).setToken(args[0]).build().awaitReady();
        logger.info("Logged in to Discord account {}", api.getSelfUser().getName());

        // Create command handler
        CommandHandler commandHandler = new JDA4Handler(api);

        // Give bot owner all permissions.
        commandHandler.addPermission(api.getSelfUser().getId(), "*");

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

        // Register listeners
        api.addEventListener(new ModLogListeners(api));
        api.addEventListener(new AutoModListeners(api, commandHandler));
        api.addEventListener(new PrivateListener(api));
        api.addEventListener(new StarboardListener(api));

    }

}
