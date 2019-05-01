package org.moss.discord.listeners.log.parsers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.moss.discord.listeners.log.LogData;
import org.moss.discord.listeners.log.LogParser;

import java.awt.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class TruAntiLagParser implements LogParser {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient.Builder().build();

    private JsonNode laggers;

    public TruAntiLagParser() {
        Request request = new Request.Builder()
                .url("https://raw.githubusercontent.com/LaxWasHere/TruAntiLag/master/laggers.json")
                .build();

        try {
            laggers = mapper.readTree(Objects.requireNonNull(client.newCall(request).execute().body()).string()).get("name");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void evaluate(LogData log) {
        boolean found = false;
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("**TruAntiLag xPerience Alert!**");
        embed.setColor(Color.RED);

        Scanner scanner = log.getLog();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Iterator<Map.Entry<String, JsonNode>> iterator = laggers.fields();

            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> lagger = iterator.next();
                if (line.contains(lagger.getKey())) {
                    found = true;
                    embed.addField(lagger.getKey(), lagger.getValue().get("description").asText());
                }
            }
        }

        if (found) {
            log.getMessage().getChannel().sendMessage(embed);
        }
    }

}
