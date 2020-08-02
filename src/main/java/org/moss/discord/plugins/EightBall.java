package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.moss.chesterapi.ChesterPlugin;
import org.moss.discord.Chester;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class EightBall extends Chester implements ChesterPlugin {

    List<String> responses = new ArrayList<>();
    String img = "https://i.imgur.com/nBRPBMf.gif";

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
        try {
            File file = new File("data/8ball_responses.txt");
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                responses.add(sc.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Command(aliases = {"!8ball"}, usage = "!8ball", description = "Asks the all knowing 8ball")
    public void onCommand(TextChannel channel, User user, String[] args) {
        if (args.length == 0) {
            channel.sendMessage(user.getMentionTag() + " `!8ball <question>`");
            return;
        }
        channel.sendMessage(new EmbedBuilder().setTitle("Chester shakes the magic 8ball..").setImage(img)).thenAcceptAsync(message -> {
            String[] answer = responses.get(ThreadLocalRandom.current().nextInt(responses.size() -1)).split("\\|");
            EmbedBuilder builder = new EmbedBuilder().setColor(Color.decode(answer[0])).setTitle(answer[1]);
            message.getApi().getThreadPool().getScheduler().schedule(() -> message.edit(user.getMentionTag(), builder),4, TimeUnit.SECONDS);
        });

    }

}
