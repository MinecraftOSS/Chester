package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class BlacklistCommand implements CommandExecutor {

    @Command(aliases = {"!blacklist"}, usage = "!blacklist <server IP>", description = "Checks if a server IP is blacklisted from Mojang")
    public void onCommand(DiscordApi api, Message message, TextChannel channel, String[] args) {
        if (args.length != 1) return;
        EmbedBuilder builder = new EmbedBuilder();
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url("https://use.gameapis.net/mc/extra/blockedservers/check/" + args[0]).build();
            Response response = client.newCall(request).execute();
            String jsonData = Objects.requireNonNull(response.body()).string();
            JSONObject json = new JSONObject(jsonData);
            response.close();
            JSONArray array = json.getJSONArray(args[0]);
            int length = array.length();
            builder.setTitle(args[0] + " Blacklist Information");
            for (int i = 0; i < length; i++) {
                String emoji;
                String check = String.valueOf(array.getJSONObject(i).getBoolean("blocked"));
                if (check.equalsIgnoreCase("false")) {
                    emoji = "✅";
                } else {
                    emoji = "\uD83D\uDEAB";
                }
                builder.addField(array.getJSONObject(i).getString("domain"), "Status: " + emoji, true);
            }
            builder.setFooter("Requested by " + message.getAuthor().getDisplayName(), message.getAuthor().getAvatar());
            channel.sendMessage(builder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
