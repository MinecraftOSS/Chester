package org.moss.discord.plugins.test;

import de.btobastian.sdcf4j.Command;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;
import org.moss.chesterapi.ChesterPlugin;
import org.moss.discord.Chester;

public class test extends Chester implements ChesterPlugin {

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
        System.out.println("Test initialized!");
    }

    @Command(aliases = {"!ping"})
    public void onPing(TextChannel channel, User user) {
        System.out.println("Called!");
        channel.sendMessage("Pong");
    }

}
