package org.moss.discord.listeners.log;

import org.javacord.api.entity.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LogData {

    private String log;
    private Message message;

    private String version;
    private String serverSoftware;

    private List<Plugin> plugins = new ArrayList<>();

    public LogData(String log, Message message) {
        this.log = log;
        this.message = message;
    }

    public Scanner getLog() {
        return new Scanner(log);
    }

    public Message getMessage() {
        return message;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getServerSoftware() {
        return serverSoftware;
    }

    public void setServerSoftware(String serverSoftware) {
        this.serverSoftware = serverSoftware;
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }

    public void addPlugin(Plugin plugin) {
        plugins.add(plugin);
    }

    public static class Plugin {
        private String name;
        private String version;

        public Plugin(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }
    }

}
