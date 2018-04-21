package org.moss.discord.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.javacord.api.DiscordApi;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class BStatsUtil {

    /**
     * The mapper used to map json objects.
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * The http client which is used to execute rest calls.
     */
    private static final OkHttpClient client = new OkHttpClient.Builder().build();

    /**
     * The Discord api instance.
     */
    private final DiscordApi api;

    /**
     * Creates a new bStats util.
     *
     * @param api A Discord api instance.
     */
    public BStatsUtil(DiscordApi api) {
        this.api = api;
    }

    /**
     * Gets a list with all registered plugins.
     *
     * @return A json node which contains all plugins.
     */
    public CompletableFuture<JsonNode> getPluginList() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return makeRequest("https://bStats.org/api/v1/plugins/");
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, api.getThreadPool().getExecutorService());
    }

    /**
     * Gets a list with all supported software.
     *
     * @return A json node which contains all supported software.
     */
    public CompletableFuture<JsonNode> getSoftwareList() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return makeRequest("https://bStats.org/api/v1/software/");
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, api.getThreadPool().getExecutorService());
    }

    /**
     * Gets a software by its id.
     *
     * @param id The id of the software.
     * @return A json node which contains data about the software.
     */
    public CompletableFuture<JsonNode> getSoftwareById(int id) {
        return getSoftwareList()
                .thenApply(softwareList -> {
                    for (JsonNode software : softwareList) {
                        if (software.get("id").asInt() == id) {
                            return software;
                        }
                    }
                    return null;
                });
    }

    /**
     * Gets a plugin by its name.
     *
     * @param pluginName The name of the plugin.
     * @return A json node which contains data about the plugin.
     */
    public CompletableFuture<JsonNode> getPlugin(String pluginName) {
        return getPluginList()
                .thenApply(plugins -> {
                    for (JsonNode plugin : plugins) {
                        if (plugin.get("name").asText().equalsIgnoreCase(pluginName)) {
                            return plugin;
                        }
                    }
                    return null;
                });
    }

    /**
     * Gets the latest data from a line chart.
     *
     * @param pluginId The id of the plugin.
     * @param chartId The id of the chart.
     * @return The latest data of the line chart.
     */
    public CompletableFuture<Integer> getLineChartData(int pluginId, String chartId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return makeRequest("https://bStats.org/api/v1/plugins/" + pluginId + "/charts/" + chartId + "/data/?maxElements=1").get(0).get(1).asInt();
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, api.getThreadPool().getExecutorService());
    }

    /**
     * Executes a blocking GET request to the given url.
     *
     * @param url The url.
     * @return A json node.
     * @throws IOException If something went wrong.
     */
    public JsonNode makeRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return mapper.readTree(Objects.requireNonNull(client.newCall(request).execute().body()).string());
    }

}
