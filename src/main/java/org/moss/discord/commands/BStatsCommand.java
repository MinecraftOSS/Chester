package org.moss.discord.commands;

import com.fasterxml.jackson.databind.JsonNode;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;
import org.moss.discord.util.BStatsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class BStatsCommand implements CommandExecutor {

    /**
     * The logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(BStatsCommand.class);

    @Command(aliases = {"!bStats"}, usage = "!bStats <pluginName>", description = "Shows some stats about the given plugin.")
    public void onCommand(DiscordApi api, TextChannel channel, String[] args) {
        BStatsUtil bStatsUtil = new BStatsUtil(api);

        if (args.length >= 1) {
            EmbedBuilder embed = new EmbedBuilder();
            bStatsUtil.getPlugin(String.join(" ", args)).thenAcceptAsync(plugin -> {
                // Check if a plugin with this name exists
                if (plugin == null) {
                    channel.sendMessage(new EmbedBuilder().setTitle("Unknown plugin!").setColor(Color.RED)).join();
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
                InputStream signatureAsStream = convertSignatureImageToPng(
                        "https://bstats.org/signatures/" + softwareUrl + "/" + name + ".svg");
                int serverCount = bStatsUtil.getLineChartData(id, "servers").join();
                int playerCount = bStatsUtil.getLineChartData(id, "players").join();

                // Create the embed
                embed.setTitle(name + " by " + owner);
                embed.setDescription("[View on bStats](https://bstats.org/plugin/" + softwareUrl + "/" + name + ")");
                embed.addInlineField("\uD83D\uDD0C Servers", "```\n" + String.valueOf(serverCount) + "\n```");
                embed.addInlineField("\uD83D\uDC76 Players", "```\n" + String.valueOf(playerCount) + "\n```");
                embed.addInlineField("⚙ Software", "```\n" + softwareName + "\n```");
                embed.setImage(signatureAsStream, "jpg");
                embed.setTimestamp();

                // Send the embed
                channel.sendMessage(embed).join();
            }).exceptionally(ExceptionLogger.get(MissingPermissionsException.class));
        }

        // No plugin name provided. Send help message.
        if (args.length == 0) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setDescription("**Usage**: !bStats <pluginName>")
                    .setColor(Color.RED);
            channel.sendMessage(embed)
                    .exceptionally(ExceptionLogger.get(MissingPermissionsException.class));
        }
    }

    /**
     * Converts a svg bStats signature image to a jpeg image.
     *
     * @param url The url to the signature image.
     * @return An input stream for the jpeg image.
     */
    private InputStream convertSignatureImageToPng(String url) {
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
