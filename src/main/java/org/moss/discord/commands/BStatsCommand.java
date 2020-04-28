package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.moss.discord.util.BStatsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;

public class BStatsCommand implements CommandExecutor {

    /**
     * The logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(BStatsCommand.class);

    @Command(aliases = {"!bStats"}, usage = "!bStats <pluginName>", description = "Shows some stats about the given plugin.")
    public void onCommand(JDA api, TextChannel channel, String[] args) {
        BStatsUtil bStatsUtil = new BStatsUtil(api);

        if (args.length >= 1) {
            EmbedBuilder embed = new EmbedBuilder();
            bStatsUtil.getPlugin(String.join(" ", args)).thenAcceptAsync(plugin -> {
                // Check if a plugin with this name exists
                if (plugin == null) {
                    channel.sendMessage(new EmbedBuilder().setTitle("Unknown plugin!").setColor(Color.RED).build()).queue();
                    return;
                }

                // Get some general data
                int id = plugin.get("id").asInt();
                String name = plugin.get("name").asText();
                String owner = plugin.get("owner").get("name").asText();
                int softwareId = plugin.get("software").get("id").asInt();
                JsonNode software = bStatsUtil.getSoftwareById(softwareId).join();
                String softwareName = software.get("name").asText();
                String softwareUrl = software.get("url").asText();
                InputStream signatureAsStream = convertSignatureImageToJpeg(
                        "https://bstats.org/signatures/" + softwareUrl + "/" + name + ".svg");
                int serverCount = bStatsUtil.getLineChartData(id, "servers").join();
                int playerCount = bStatsUtil.getLineChartData(id, "players").join();

                // Create the embed
                embed.setTitle(name + " by " + owner);
                embed.setDescription("[View on bStats](https://bstats.org/plugin/" + softwareUrl + "/" + name + ")");
                embed.addField("\uD83D\uDD0C Servers", "```\n" + serverCount + "\n```", true);
                embed.addField("\uD83D\uDC76 Players", "```\n" + playerCount + "\n```", true);
                embed.addField("⚙ Software", "```\n" + softwareName + "\n```", true);
                embed.setImage("attachment://photo.jpg");
                embed.setTimestamp(Instant.now());

                // Send the embed
                channel.sendFile(signatureAsStream, "photo.jpg").embed(embed.build()).queue();
            });
        }

        // No plugin name provided. Send help message.
        if (args.length == 0) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setDescription("**Usage**: !bStats <pluginName>")
                    .setColor(Color.RED);
            channel.sendMessage(embed.build()).queue();
        }
    }

    /**
     * Converts a svg bStats signature image to a jpeg image.
     *
     * @param url The url to the signature image.
     * @return An input stream for the jpeg image.
     */
    private InputStream convertSignatureImageToJpeg(String url) {
        // Create a JPEG transcoder
        JPEGTranscoder t = new JPEGTranscoder();


        // Set the transcoding hints.
        t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, 0.98f);

        // Create the transcoder input.
        TranscoderInput input = new TranscoderInput(url);

        // Create the transcoder output.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(outputStream);

        // Write the image to the output stream
        try {
            t.transcode(input, output);
        } catch (TranscoderException e) {
            logger.error("Failed to convert signature image to jpeg!", e);
        }

        return new ByteArrayInputStream(outputStream.toByteArray());
    }



}
