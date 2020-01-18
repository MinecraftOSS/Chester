package org.moss.discord.listeners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.moss.discord.Constants;

import java.awt.*;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoModListeners implements MessageCreateListener, CommandExecutor {

    DiscordApi api;
    private ModerationData data;
    private Optional<TextChannel> modChannel;
    private Map<Long, Instant> active = new HashMap<>();
    private Map<Long, MessageTracker> trackers = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();
    private List<String> protectedRoles = new ArrayList<String>() {
        {
            add("391029845225766912");
            add("425708417663893505");
        }
    };

    String[] donts = {
            "Do not",
            "Don't",
            "Stop",
            "Huge Mistake",
            "Bad Idea",
            "No no no",
            "apologize",
            "Say sorry",
            "You will now be terminated",
            "Prepare for your termination",
            "We're coming for you",
            "You've been added to the naughty list",
            "EXTERMINATE!",
            "The ban-hammer awaits you",
            "Hell will now be unleashed",
            "Every day we stray further from god."
    };

    String[] nopes = {
            "https://i.imgur.com/z8UBrh5.png",
            "https://i.imgur.com/J015ZK5.png",
            "https://i.imgur.com/RJAtyAg.png",
            "https://i.imgur.com/aPVrnKl.gif",
            "https://media0.giphy.com/media/rFvtiIevmj0zu/giphy.gif",
            "https://i.imgur.com/dTMWtkp.png",
            "https://i.imgur.com/KbsSxlR.gif",
            "https://i.imgur.com/8r5mriG.jpg",
            "https://i.imgur.com/ced9xSC.gif",
            "https://i.imgur.com/HFRKIpb.gif",
            "https://i.imgur.com/ebngidh.gif",
            "https://i.imgur.com/7h0YLxS.gif",
            "https://i.imgur.com/hI6plU6.jpg",
            "https://i.imgur.com/pWBhrwI.gif",
            "https://i.imgur.com/g639DXN.gif",
            "https://i.imgur.com/u4Wixvd.gif",
            "https://i.imgur.com/ThMKJ7P.gif",
            "https://i.imgur.com/cdsdmYA.gif",
            "https://i.imgur.com/cVvUkWK.gif",
            "https://i.imgur.com/soBdcGC.gif"
    };

    public AutoModListeners(DiscordApi api, CommandHandler commandHandler) {
        this.api = api;
        commandHandler.registerCommand(this);
        modChannel = api.getTextChannelById(Constants.CHANNEL_MODLOG);
        data = new ModerationData();
        try {
            JsonNode jsonNode = mapper.readTree(new File("./moddata.json")).get("data");
            data = mapper.readValue(jsonNode.toString(), ModerationData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Command(aliases = {"!pingok", ".pingok"}, usage = "!pingok", description = "Pings on the channel are ok")
    public void onOk(DiscordApi api, String[] args, TextChannel channel, User user, Message message, Server server) {
        if (!(server.canKickUsers(user) || hasProtectedRole(user.getRoles(server)))) {
            return;
        }
        try {
            if (data.exempts.get(user.getId()).contains(channel.getIdAsString())) {
                data.exempts.get(user.getId()).remove(channel.getIdAsString());
                message.addReaction("\ud83d\ude08");
            } else {
                data.exempts.computeIfAbsent(user.getId(), daList -> new TreeSet<>()).add(channel.getIdAsString());
                message.addReaction("\ud83d\ude01");
            }
        } catch (Exception e) {
            data.exempts.computeIfAbsent(user.getId(), daList -> new TreeSet<>()).add(channel.getIdAsString());
            message.addReaction("\ud83d\ude01");
        }
        saveModData();
    }

    @Command(aliases = {"!fileblacklist", ".fileblacklist"}, usage = "!fileblacklist <Extension>", description = "Adds a file blacklist")
    public void addBlackList(DiscordApi api, String[] args, TextChannel channel, MessageAuthor author, Message message) {
        if (author.canBanUsersFromServer() && args.length >= 1) {
            if (args[0].startsWith(".")) {
                if (data.blacklistedFiles.contains(args[0])) {
                    data.blacklistedFiles.remove(args[0].toLowerCase());
                    message.addReaction("\uD83D\uDC4E");
                } else {
                    data.blacklistedFiles.add(args[0].toLowerCase());
                    message.addReaction("\uD83D\uDC4D");
                }
                saveModData();
            } else {
                channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Invalid file extension"));
            }
        }
    }

    @Command(aliases = {"!addcensor", ".addcensor"}, usage = "!addcensor <Word/Regex>", description = "Adds the words to the censor list")
    public void addCensor(DiscordApi api, String[] args, TextChannel channel, MessageAuthor author, Message message) {
        if (author.canBanUsersFromServer() && args.length >= 1) {
            if (data.censoredWords.contains(args[0])) {
                data.censoredWords.remove(args[0].toLowerCase());
                message.addReaction("\uD83D\uDC4E");
            } else {
                data.censoredWords.add(args[0].toLowerCase());
                message.addReaction("\uD83D\uDC4D");
            }
            saveModData();
        }
    }


    @Override
    public void onMessageCreate(MessageCreateEvent ev) {
        if (ev.getMessage().getAuthor().isYourself() || ev.getMessage().getAuthor().canKickUsersFromServer()) {
            active.put(ev.getMessage().getAuthor().getId(), Instant.now());
            return;
        }

        parsePings(ev.getMessage());

        if (!ev.getMessage().getEmbeds().isEmpty()) {
            for (Embed embed : ev.getMessage().getEmbeds()) {
                String title = embed.getTitle().orElse("");
                System.out.println(title);
                for (String pattern : data.censoredWords) {
                    if (title.toLowerCase().contains(pattern)) {
                        ev.getMessage().delete("Pattern trigger: " + pattern);
                        logCensorMessage(ev.getMessage().getUserAuthor(), pattern, ev.getChannel().getIdAsString());
                        return;
                    }
                }
            }
        }

        for (MessageAttachment messageAttachment : ev.getMessage().getAttachments()) {
            String fileName = messageAttachment.getFileName();
            if (data.blacklistedFiles.contains(fileName.substring(fileName.lastIndexOf('.')))) {
                ev.getMessage().delete("Blacklisted File: " + fileName);
                logFileMessage(ev.getMessage().getUserAuthor(), fileName, ev.getChannel().getIdAsString());
                return;
            }
        }

        String message = ev.getMessage().getContent();
        for (String pattern : data.censoredWords) {
            Matcher mat = Pattern.compile(pattern).matcher(message.toLowerCase());
            if (mat.find()) {
                ev.getMessage().delete("Pattern trigger: " + pattern);
                logCensorMessage(ev.getMessage().getUserAuthor(), mat.group(), ev.getChannel().getIdAsString());
                return;
            }
        }
    }

    public void parsePings(Message message) {
        if (message.getMentionedUsers().size() >= 1) {
            User perp = message.getUserAuthor().get();
            Server server = message.getServer().get();
            MessageTracker tracker = getTrackedUser(perp.getId());
            boolean warn = false;
            for (User user : message.getMentionedUsers()) {
                if ((server.canKickUsers(user) || hasProtectedRole(user.getRoles(server))) && !userIsActive(user.getId())) {
                    if (data.exempts.containsKey(user.getId()) && data.exempts.get(user.getId()).contains(message.getChannel().getIdAsString())) {
                        continue;
                    }
                    tracker.updatePings();
                    warn = true;
                }
                if (tracker.getCount() > 3) { //4th ping will ban the user.
                    message.getServer().get().banUser(perp, 0, "Mass ping");
                    message.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(String.format("User %s has been permanently banned for mass ping.", perp.getMentionTag())));
                    return;
                }
            }
            if (warn) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.RED);
                embed.setImage(nopes[ThreadLocalRandom.current().nextInt(nopes.length)]);
                embed.setDescription("It looks like you're trying to randomly ping a staff");
                embed.setFooter(String.format(" Warning %d/3", tracker.getCount()) + " | " + donts[ThreadLocalRandom.current().nextInt(donts.length)] );
                message.getChannel().sendMessage(perp.getMentionTag(),embed).thenAcceptAsync(msg -> api.getThreadPool().getScheduler().schedule((Callable<CompletableFuture<Void>>) msg::delete, 30, TimeUnit.MINUTES));
                perp.sendMessage(embed);
            }
        }
    }

    private boolean userIsActive(long userId) {
        if (!active.keySet().contains(userId)) {
            return false;
        }
        return (Duration.between(active.get(userId), Instant.now()).toMinutes() <= 15);
    }

    public void logCensorMessage(Optional<User> user, String pattern, String chanId) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor("CENSOR");
        embed.setColor(Color.CYAN);
        embed.setThumbnail("https://i.imgur.com/bYGnGCp.png");

        embed.addInlineField("Author", user.get().getMentionTag());
        embed.addInlineField("Channel", String.format("<#%s>", chanId));

        embed.addField("Pattern", String.format("```%s```", pattern));

        embed.setFooter(user.get().getIdAsString());
        embed.setTimestamp(Instant.now());
        modChannel.get().sendMessage(embed);
    }

    public void logFileMessage(Optional<User> user, String fileName, String chanId) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor("FILE");
        embed.setColor(Color.CYAN);
        embed.setThumbnail("https://i.imgur.com/bYGnGCp.png");

        embed.addInlineField("Author", user.get().getMentionTag());
        embed.addInlineField("Channel", String.format("<#%s>", chanId));

        embed.addField("File", String.format("```%s```", fileName));

        embed.setFooter(user.get().getIdAsString());
        embed.setTimestamp(Instant.now());

        modChannel.get().sendMessage(embed);
    }

    private void saveModData() {
        try {
            mapper.writerWithDefaultPrettyPrinter().withRootName("data").writeValue(new File("./moddata.json"), data);
        } catch (Throwable rock) {
            rock.printStackTrace();
        }
    }

    private MessageTracker getTrackedUser(long id) {
        if (!trackers.keySet().contains(id)) {
            trackers.put(id, new MessageTracker());
        }
        return trackers.get(id);
    }

    static class ModerationData {
        public Map<Long, Set<String>> exempts = new HashMap<>();
        public List<String> blacklistedFiles;
        public List<String> censoredWords;
    }

    private boolean hasProtectedRole(List<Role> roles) {
        for (Role role : roles) {
            if (protectedRoles.contains(role.getIdAsString())) {
                return true;
            }
        }
        return false;
    }

    class MessageTracker {

        private AtomicInteger count;
        private String lastMessage;

        public int updatePings () {
            return count.incrementAndGet();
        }

        public int getCount() {
            return count.get();
        }

        MessageTracker() {
            this.count = new AtomicInteger(0);
            api.getThreadPool().getScheduler().scheduleAtFixedRate(() -> {
                if (count.get() > 0) {
                    count.decrementAndGet();
                }
            }, 0,5, TimeUnit.MINUTES);
        }
    }
}