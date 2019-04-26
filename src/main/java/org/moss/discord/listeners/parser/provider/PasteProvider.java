package org.moss.discord.listeners.parser.provider;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.javacord.api.entity.message.Message;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasteProvider implements LogProvider {

    private static final OkHttpClient client = new OkHttpClient.Builder().build();
    // TODO: Add more paste providers
    private static final Pattern pattern = Pattern.compile("hasteb.in/[^ ]*");

    @Override
    public String provide(Message message) {
        Matcher matcher = pattern.matcher(message.getContent());

        if (matcher.find()) {
            Request request = new Request.Builder()
                    .url("https://" + matcher.group().replaceFirst("/", "/raw/"))
                    .build();

            try {
                return Objects.requireNonNull(client.newCall(request).execute().body()).string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
