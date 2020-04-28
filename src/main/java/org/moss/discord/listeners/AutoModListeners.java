package org.moss.discord.listeners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.moss.discord.Constants;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoModListeners extends ListenerAdapter implements CommandExecutor {

    JDA api;
    private ModerationData data;
    private final TextChannel modChannel;
    private final Map<Long, Instant> active = new HashMap<>();
    private final Map<Long, MessageTracker> trackers = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<String> protectedRoles = new ArrayList<String>() {
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

    public AutoModListeners(JDA api, CommandHandler commandHandler) {
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
    public void onOk(JDA api, String[] args, TextChannel channel, Member user, Message message, Guild server) {
        if (user == null) {
            return;
        }
        if (!(server.getMember(api.getSelfUser()).canInteract(user) || hasProtectedRole(user.getRoles()))) {
            return;
        }
        try {
            if (data.exempts.get(user.getIdLong()).contains(channel.getId())) {
                data.exempts.get(user.getIdLong()).remove(channel.getId());
                message.addReaction("\ud83d\ude08").queue();
            } else {
                data.exempts.computeIfAbsent(user.getIdLong(), daList -> new TreeSet<>()).add(channel.getId());
                message.addReaction("\ud83d\ude01").queue();
            }
        } catch (Exception e) {
            data.exempts.computeIfAbsent(user.getIdLong(), daList -> new TreeSet<>()).add(channel.getId());
            message.addReaction("\ud83d\ude01").queue();
        }
        saveModData();
    }

    @Command(aliases = {"!fileblacklist", ".fileblacklist"}, usage = "!fileblacklist <Extension>", description = "Adds a file blacklist")
    public void addBlackList(JDA api, String[] args, TextChannel channel, Member author, Message message) {
        if (author == null) {
            return;
        }
        if (author.hasPermission(Permission.BAN_MEMBERS) && args.length >= 1) {
            if (args[0].startsWith(".")) {
                if (data.blacklistedFiles.contains(args[0])) {
                    data.blacklistedFiles.remove(args[0].toLowerCase());
                    message.addReaction("\uD83D\uDC4E").queue();
                } else {
                    data.blacklistedFiles.add(args[0].toLowerCase());
                    message.addReaction("\uD83D\uDC4D").queue();
                }
                saveModData();
            } else {
                channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Invalid file extension").build()).queue();
            }
        }
    }

    @Command(aliases = {"!addcensor", ".addcensor"}, usage = "!addcensor <Word/Regex>", description = "Adds the words to the censor list")
    public void addCensor(JDA api, String[] args, TextChannel channel, Member author, Message message) {
        if (author == null) {
            return;
        }
        if (author.hasPermission(Permission.BAN_MEMBERS) && args.length >= 1) {
            if (data.censoredWords.contains(args[0])) {
                data.censoredWords.remove(args[0].toLowerCase());
                message.addReaction("\uD83D\uDC4E").queue();
            } else {
                data.censoredWords.add(args[0].toLowerCase());
                message.addReaction("\uD83D\uDC4D").queue();
            }
            saveModData();
        }
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent ev) {
        if (ev.getMessage().getAuthor().getId().equals(ev.getJDA().getSelfUser().getId()) || ev.getMessage().getMember().hasPermission(Permission.KICK_MEMBERS)) {
            active.put(ev.getMessage().getAuthor().getIdLong(), Instant.now());
            return;
        }

        parsePings(ev.getMessage());

        if (!ev.getMessage().getEmbeds().isEmpty()) {
            for (MessageEmbed embed : ev.getMessage().getEmbeds()) {
                String title = embed.getTitle() == null ? "" : embed.getTitle();
                System.out.println(title);
                for (String pattern : data.censoredWords) {
                    if (title.toLowerCase().contains(pattern)) {
                        ev.getMessage().delete().reason("Pattern trigger: " + pattern).queue();
                        logCensorMessage(ev.getMessage().getAuthor(), pattern, ev.getChannel().getId());
                        return;
                    }
                }
            }
        }

        for (Message.Attachment messageAttachment : ev.getMessage().getAttachments()) {
            String fileName = messageAttachment.getFileName();
            if (data.blacklistedFiles.contains(fileName.substring(fileName.lastIndexOf('.')))) {
                ev.getMessage().delete().reason("Blacklisted File: " + fileName).queue();
                logFileMessage(ev.getMessage().getAuthor(), fileName, ev.getChannel().getId());
                return;
            }
        }

        String message = ev.getMessage().getContentRaw();
        for (String pattern : data.censoredWords) {
            Matcher mat = Pattern.compile(pattern).matcher(message.toLowerCase());
            if (mat.find()) {
                ev.getMessage().delete().reason("Pattern trigger: " + pattern).queue();
                logCensorMessage(ev.getMessage().getAuthor(), mat.group(), ev.getChannel().getId());
                return;
            }
        }
    }

    public void parsePings(Message message) {
        if (message.getMentionedUsers().size() >= 1) {
            Member perp = message.getMember();
            MessageTracker tracker = getTrackedUser(perp.getIdLong());
            boolean warn = false;
            for (Member user : message.getMentionedMembers()) {
                if ((perp.hasPermission(Permission.KICK_MEMBERS) || hasProtectedRole(user.getRoles())) && !userIsActive(user.getIdLong())) {
                    if (data.exempts.containsKey(user.getIdLong()) && data.exempts.get(user.getIdLong()).contains(message.getChannel().getId())) {
                        continue;
                    }
                    tracker.updatePings();
                    warn = true;
                }
                if (tracker.getCount() > 3) { //4th ping will ban the user.
                    message.getGuild().ban(perp, 0, "Mass ping").queue();
                    message.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(String.format("User %s has been permanently banned for mass ping.", perp.getAsMention())).build()).queue();
                    return;
                }
            }
            if (warn) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.RED);
                embed.setImage(nopes[ThreadLocalRandom.current().nextInt(nopes.length)]);
                embed.setDescription("It looks like you're trying to randomly ping a staff");
                embed.setFooter(String.format(" Warning %d/3", tracker.getCount()) + " | " + donts[ThreadLocalRandom.current().nextInt(donts.length)] );
                message.getChannel().sendMessage(new MessageBuilder().setContent(perp.getAsMention()).setEmbed(embed.build()).build()).queue(msg -> api.getRateLimitPool().schedule(() -> msg.delete().queue(), 30, TimeUnit.MINUTES));
                perp.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(embed.build()).queue());
            }
        }
    }

    private boolean userIsActive(long userId) {
        if (!active.containsKey(userId)) {
            return false;
        }
        return (Duration.between(active.get(userId), Instant.now()).toMinutes() <= 15);
    }

    public void logCensorMessage(User user, String pattern, String chanId) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor("CENSOR");
        embed.setColor(Color.CYAN);
        embed.setThumbnail("https://i.imgur.com/bYGnGCp.png");

        embed.addField("Author", user.getAsMention(), true);
        embed.addField("Channel", String.format("<#%s>", chanId), true);

        embed.addField("Pattern", String.format("```%s```", pattern), false);

        embed.setFooter(user.getId());
        embed.setTimestamp(Instant.now());
        modChannel.sendMessage(embed.build()).queue();
    }

    public void logFileMessage(User user, String fileName, String chanId) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setAuthor("FILE");
        embed.setColor(Color.CYAN);
        embed.setThumbnail("https://i.imgur.com/bYGnGCp.png");

        embed.addField("Author", user.getAsMention(), true);
        embed.addField("Channel", String.format("<#%s>", chanId), true);

        embed.addField("File", String.format("```%s```", fileName), false);

        embed.setFooter(user.getId());
        embed.setTimestamp(Instant.now());

        modChannel.sendMessage(embed.build()).queue();
    }

    private void saveModData() {
        try {
            mapper.writerWithDefaultPrettyPrinter().withRootName("data").writeValue(new File("./moddata.json"), data);
        } catch (Throwable rock) {
            rock.printStackTrace();
        }
    }

    private MessageTracker getTrackedUser(long id) {
        if (!trackers.containsKey(id)) {
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
            if (protectedRoles.contains(role.getId())) {
                return true;
            }
        }
        return false;
    }

    class MessageTracker {

        private final AtomicInteger count;
        private String lastMessage;

        public int updatePings () {
            return count.incrementAndGet();
        }

        public int getCount() {
            return count.get();
        }

        MessageTracker() {
            this.count = new AtomicInteger(0);
            api.getRateLimitPool().scheduleAtFixedRate(() -> {
                if (count.get() > 0) {
                    count.decrementAndGet();
                }
            }, 0,5, TimeUnit.MINUTES);
        }
    }
}