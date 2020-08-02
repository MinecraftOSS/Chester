package org.moss.discord;

import com.google.common.reflect.ClassPath;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.moss.chesterapi.ChesterPlugin;
import org.moss.chesterapi.Settings;
import org.moss.discord.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Chester implements CommandExecutor {

    // The logger for this class.
    private static final Logger logger = LoggerFactory.getLogger(Chester.class);
    private static DiscordApi api;
    private static CommandHandler commandHandler;

    public static void main(String[] args) {
        Settings.load(new File("chester.properties"));

        api = new DiscordApiBuilder().setToken(Settings.getBotToken()).login().join();
        logger.info("Logged in to Discord account {}", api.getYourself().getName());

        new DatabaseUtil().initSqlite(new File("chester.db"));

        // Create command handler
        commandHandler = new JavacordHandler(api);

        // Give bot owner all permissions.
        commandHandler.addPermission(String.valueOf(api.getOwnerId()), "*");

        //Load all plugins
        try {
            ClassPath classPath = ClassPath.from(Chester.class.getClassLoader());
            for (ClassPath.ClassInfo classinfo : classPath.getTopLevelClasses("org.moss.discord.plugins.test")) {
                logger.info("Loading {}", classinfo.getSimpleName());
                Class<?> classy = classinfo.load();
                ChesterPlugin plugin = (ChesterPlugin) classy.newInstance();
                plugin.init();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DiscordApi getDiscordApi() {
        return api;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

}
