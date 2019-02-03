package org.moss.discord.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.core.entity.message.embed.EmbedImpl;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class EmbedUtil {

    /**
     * The mapper used to map json objects.
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * The http client which is used to execute rest calls.
     */
    private static final OkHttpClient client = new OkHttpClient.Builder().build();

    public EmbedBuilder fromJson(String url) {
        try {
            JsonNode jsonEmbed = makeRequest(url);
            return new EmbedImpl(jsonEmbed.get("embed")).toBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new EmbedBuilder().setColor(Color.RED).setTitle("Invalid embed");
    }

    public EmbedBuilder fromJson(String url, User user, Server server, String... args) {
        try {
            JsonNode jsonEmbed = makeRequest(url);
            return new EmbedImpl(jsonAsString(new KeywordsUtil(jsonEmbed.get("embed").toString(), user, server, args).replace())).toBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new EmbedBuilder().setColor(Color.RED).setTitle("Invalid embed");
    }

    public EmbedBuilder fromString(String json, User user, Server server, String... args) {
        try {
            return new EmbedImpl(jsonAsString(new KeywordsUtil(json, user, server, args).replace())).toBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new EmbedBuilder().setColor(Color.RED).setTitle("Invalid embed");
    }

    public EmbedBuilder parseString(String message, User user, Server server, String... args) {
        if (message.startsWith("<embed>")) {
            return fromJson(message.substring(7), user, server, args);
        }
        if (message.startsWith("<json>")) {
            return fromString(message.substring(6), user, server, args);
        }
        if (message.startsWith("http")) {
            return fromJson(message, user, server, args);
        }
        return new EmbedBuilder().setColor(Color.RED).setTitle("Invalid embed");
    }

    /**
     * Executes a blocking GET request to the given url.
     *
     * @param url The url.
     * @return A json node.
     * @throws IOException If something went wrong.
     */
    private JsonNode makeRequest(String url) throws IOException {
        System.out.println("Request: " + url);
        Request request = new Request.Builder()
                .url(url)
                .build();
        return mapper.readTree(Objects.requireNonNull(client.newCall(request).execute().body()).string());
    }

    private JsonNode jsonAsString(String jsonString) throws IOException {
        return mapper.readTree(Objects.requireNonNull(jsonString));
    }
}
