package org.moss.discord.plugins;

import de.btobastian.sdcf4j.Command;
import org.apache.commons.lang.StringUtils;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.moss.discord.Chester;
import org.moss.discord.ChesterPlugin;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Colors extends Chester implements ChesterPlugin {

    @Override
    public void init() {
        getCommandHandler().registerCommand(this);
    }


    @Command(aliases = {"!color"}, usage = "!color <rgb,hex>", description = "Parses colours")
    public void onCommand(User user, TextChannel channel, String[] args) {
        if (args.length <= 0) return;
        Color color = parseColor(String.join("", args));
        if (color != null) {
            BufferedImage img = new BufferedImage(150, 100, BufferedImage.TYPE_INT_RGB);
            Graphics2D gfx = img.createGraphics();
            gfx.setColor(color);
            gfx.fillRect(0,0,150,100);
            gfx.dispose();
            channel.sendMessage(user.getMentionTag(), new EmbedBuilder()
                    .setColor(color)
                    .setImage(img)
                    .setDescription("[[View Color]](https://www.thecolorapi.com/id?rgb={RGB}&format=html)\n[[Explore Color]](https://www.thecolorapi.com/scheme?rgb={RGB}&format=html)".replace("{RGB}", getRGB(color))));
        }
    }

    private Color parseColor(String colorTarget) {
        Color c = null;
        if (StringUtils.isNumeric(colorTarget.replace(",",""))) {
            int[] splt = Arrays.stream(colorTarget.split(",")).mapToInt(Integer::parseInt).toArray();
            c = splt.length == 3 ? new Color(splt[0],splt[1],splt[2]) : null;
        } else if (colorTarget.startsWith("#")) {
            c = Color.decode(colorTarget);
        }
        return c;
    }

    private String getRGB(Color color) {
        return String.format("%d,%d,%d", color.getRed(),color.getGreen(),color.getBlue());
    }
}