package org.moss.discord.listeners.parser.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.Color;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class FileProvider implements LogProvider {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient.Builder().build();

    @Override
    public CompletableFuture<String> provide(Message message) {
        CompletableFuture<String> log = new CompletableFuture<>();
        for (MessageAttachment attachment : message.getAttachments()) {
            if (attachment.getFileName().contains("log")) {
                attachment.downloadAsByteArray().thenAcceptAsync(bytes -> {
                    String logString = new String(bytes);
                    log.complete(logString);

                    RequestBody body = RequestBody.create(MediaType.parse("text/plain"), logString);
                    Request request = new Request.Builder()
                            .url("https://hasteb.in/documents")
                            .post(body)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {}

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            JsonNode node = mapper.readTree(Objects.requireNonNull(response.body()).string());
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setTitle("Hastebinify");
                            embed.setColor(Color.MAGENTA);
                            embed.setUrl("https://hasteb.in/" + node.get("key").asText());
                            message.getChannel().sendMessage(embed);
                        }
                    });
                });
            }
        }
        return log;
    }

}
