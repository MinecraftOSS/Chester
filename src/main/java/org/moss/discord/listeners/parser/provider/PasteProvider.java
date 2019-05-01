package org.moss.discord.listeners.parser.provider;

import okhttp3.*;
import org.javacord.api.entity.message.Message;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasteProvider implements LogProvider {

    private static final OkHttpClient client = new OkHttpClient.Builder().build();
    private static final Pattern pattern = Pattern.compile("(?:(?:https?|http)://)?[\\w/\\-?=%.]+\\.[\\w/\\-?=%.]+");

    @Override
    public CompletableFuture<String> provide(Message message) {
        CompletableFuture<String> log = new CompletableFuture<>();

        Matcher matcher = pattern.matcher(message.getContent());
        URL raw;
        try {
            if (!matcher.find()) {
                log.cancel(true);
                return log;
            }
            URL url = new URL(matcher.group());
            Sites site = Sites.fromUrl(url);
            raw = site.getRaw(url);
        } catch (MalformedURLException e) {
            log.cancel(true);
            return log;
        }

        Request request = new Request.Builder()
                .url(raw)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.cancel(true);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    log.complete(response.body().string());
                } else {
                    log.cancel(true);
                }
            }
        });

        return log;
    }

    private enum Sites {
        HASTEBIN(url -> {
            return new URL(url.getProtocol(), url.getHost(), "/raw" + url.getFile());
        }),
        GIST(url -> {
            return new URL(url.getProtocol(), "gist.githubusercontent.com", url.getFile() + "/raw");
        });

        private PasteSupplier supplier;

        Sites(PasteSupplier supplier) {
            this.supplier = supplier;
        }

        URL getRaw(URL url) throws MalformedURLException {
            return supplier.getRaw(url);
        }

        static Sites fromUrl(URL url) throws MalformedURLException {
            if (url.getFile().equals("/")) {
                throw new MalformedURLException();
            }
            switch (url.getHost()) {
                case "hasteb.in":
                case "paste.md-5.net":
                case "pastebin.com":
                    return HASTEBIN;
                case "gist.github.com":
                    return GIST;
            }
            throw new MalformedURLException();
        }

    }

    private interface PasteSupplier {
        URL getRaw(URL url) throws MalformedURLException;
    }

}
