package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.apache.commons.lang.StringUtils;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.moss.discord.Chester;
import org.moss.chesterapi.ChesterPlugin;
import java.awt.*;

public class Box extends Chester implements ChesterPlugin {

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
    }

    @Command(aliases = {"!box", "!mboxter"}, usage = "!box text", description = "Makes a box")
    public void onBox(TextChannel channel, String[] args, MessageAuthor author) {
        if (args.length < 1 || !author.canKickUsersFromServer()) {
            return;
        }
        String text = String.join(" ", args);
        String out = "";
        for (int i = 0; i < text.length(); i++) {
            String output = "";
            if (i == 0) {
                for (char c : text.toCharArray()) {
                    output = output.concat(c+" ");
                }
            }
            else if (i == text.length() -1) {
                for (char c : StringUtils.reverse(text).toCharArray()) {
                    output = output.concat(c+" ");
                }
            } else {
                String x = Character.toString(text.toCharArray()[i]);
                output = x+StringUtils.repeat(" ", (text.length() *2)-3)+StringUtils.reverse(text).toCharArray()[i];
            }
            out += output+"\n";
        }
        channel.sendMessage(String.format("```%s```", out));
    }

    @Command(aliases = {"!cube"}, usage = "!cube text", description = "Makes a cube")
    //Source https://github.com/NNTin/Cubify-Reddit
    public void onCube(TextChannel channel, String[] args, MessageAuthor author) {
        if (args.length < 1 || !author.canKickUsersFromServer()) {
            return;
        }

        char[] text = String.join(" ", args).toCharArray();
        if (text[0]!= text[text.length-1]) {
            channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Can't cubify that!"));
            return;
        }
        String out = "";

        int size = (text.length / 2 + text.length) +1;
        int gap = text.length / 2;
        char filler = '/';

        char[][] matrix = new char[size][size];
        int correction = 0;

        for (int i = 0; i < text.length; i++) {
            matrix[0][gap+i] = text[i];
            matrix[gap][i] = text[i];
            matrix[2*gap - correction][gap + i] = text[i];
            matrix[3*gap - correction][i] = text[i];

            matrix[gap + i][0] = text[i];
            matrix[i][gap] = text[i];
            matrix[i][3*gap - correction] = text[i];
            matrix[gap + i][2*gap - correction] = text[i];

        }
        for (int i = 1; i < gap; i++){
            matrix[gap - i][i] = filler;
            matrix[gap - i][2 * gap + i - correction] = filler;
            matrix[3 * gap - i - correction][i] = filler;
            matrix[3 * gap - i - correction][2 * gap + i - correction] = filler;
        }
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                out += matrix[i][j]+" ";
            }
            out += "\n";
        }
        channel.sendMessage(String.format("```%s```", out));
    }

}
