package org.moss.discord.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.javacord.api.entity.message.embed.EmbedBuilder;
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
}
