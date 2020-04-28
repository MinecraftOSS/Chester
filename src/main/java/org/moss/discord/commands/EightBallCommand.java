package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class EightBallCommand implements CommandExecutor {

    List<String> responses = new ArrayList<>();
    String img = "https://i.imgur.com/nBRPBMf.gif";

    public EightBallCommand() {
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
    public void onCommand(JDA api, TextChannel channel, Member user, String[] args) {
        if (args.length == 0) {
            channel.sendMessage(user.getAsMention() + " `!8ball <question>`").queue();
            return;
        }
        channel.sendMessage(new EmbedBuilder().setTitle("Chester shakes the magic 8ball..").setImage(img).build()).queue(message -> {
            String[] answer = responses.get(ThreadLocalRandom.current().nextInt(responses.size() -1)).split("\\|");
            EmbedBuilder builder = new EmbedBuilder().setColor(Color.decode(answer[0])).setTitle(answer[1]);
            message.getJDA().getRateLimitPool().schedule(() -> message.editMessage(new MessageBuilder().setContent(user.getAsMention()).setEmbed(builder.build()).build()).queue(),4, TimeUnit.SECONDS);
        });

    }

}
