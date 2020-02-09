package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.moss.discord.Constants;
import org.moss.discord.storage.RolePollStorage;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class RoleReactionCommand implements CommandExecutor, ReactionAddListener, ReactionRemoveListener {

    RolePollStorage storage = new RolePollStorage();
    Map<String, String> roleMap = new LinkedHashMap<>();

    public RoleReactionCommand(DiscordApi api) {
        api.addListener(this);
        roleMap.put("\uD83C\uDF4D", Constants.ROLE_ESSX_UPDATES);
        roleMap.put("\uD83C\uDF6A", Constants.ROLE_FUUID_UPDATES);
        roleMap.put("\uD83C\uDF54", Constants.ROLE_PVX_UPDATES);
        roleMap.put("\ud83C\uDF2F", Constants.ROLE_MLWC_UPDATES);
        roleMap.put("\uD83C\uDF7A", Constants.ROLE_NVTFR_UPDATES);
        roleMap.put("\uD83D\uDDDD", Constants.ROLE_PEX_UPDATES);
        roleMap.put("\uD83E\uDD5A", Constants.ROLE_EGG_UPDATES);
        roleMap.put("\uD83C\uDFB2", Constants.ROLE_MNGMES_UPDATES);
        roleMap.put("\uD83C\uDF08", Constants.ROLE_PRISM_UPDATES);
    }

    @Command(aliases = {"!rolepoll", ".rolepoll"}, usage = "!rolepoll", description = "Polls users for update roles")
    public void onCommand(DiscordApi api, TextChannel channel, User user, Server server, Message cmd) {
        if (server.isAdmin(user)) {
            cmd.delete();
            try {
                Message msg = channel.sendMessage(createPoll()).get();
                roleMap.keySet().forEach(msg::addReaction);
                storage.set(msg.getIdAsString(), msg.getChannel().getIdAsString());
            } catch (Exception e) {}
        }
    }

    @Command(aliases = {"!rpupdate", ".rpupdate"}, usage = "!rpupdate", description = "Updates all roll polls.")
    public void onRPUpdate(DiscordApi api, TextChannel channel, User user, Server server, Message cmd) {
        if (server.isAdmin(user)) {
            cmd.delete();
            try {
                for (String key : storage.getMap().keySet()) {
                    api.getMessageById(key, api.getTextChannelById(storage.getChannel(key)).get()).thenAcceptAsync(msg -> {
                        msg.edit(createPoll());
                        roleMap.keySet().forEach(msg::addReaction);
                    });
                }
            } catch (Exception e) {
                channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Unable to update polls"));
            }
        }
    }

    @Command(aliases = {"!update", ".update"}, usage = "!update", description = "Polls users for update roles")
    public void onUpdate(DiscordApi api, TextChannel channel, User user, Server server, String[] args, Message cmd) {
        if (server.canKickUsers(user)) {
            cmd.delete();
            try {
                switch (channel.getIdAsString()) {
                    case "426460619277991936": //essx
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_ESSX_UPDATES).get());
                        break;
                    case "426460663498407948": //fuuid
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_FUUID_UPDATES).get());
                        break;
                    case "426460690136694795": //pvx
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_PVX_UPDATES).get());
                        break;
                    case "479919913067216897": //LWCX
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_MLWC_UPDATES).get());
                        break;
                    case "430125681645453325": //NVTFR
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_NVTFR_UPDATES).get());
                        break;
                    case "632427764707753994": //PEX
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_PEX_UPDATES).get());
                        break;
                    case "673969910585491466": //EGG
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_EGG_UPDATES).get());
                        break;
                    case "675063189934964748": //MNGMES
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_MNGMES_UPDATES).get());
                        break;
                    case "675838377198747678": //PRISM
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_PRISM_UPDATES).get());
                        break;
                    case "397536210236604427": //TEST
                        broadcast(String.join(" ", args), channel, server.getRoleById("585793006611726346").get());
                        break;
                    default:
                        channel.sendMessage(user.getMentionTag(), new EmbedBuilder().setTitle("Invalid update channel").setColor(Color.RED));
                }
            } catch (Exception e) {
                channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Failed"));
                e.printStackTrace();
            }
        }
    }

    private void broadcast(String payload, TextChannel channel, Role role) {
        try {
            role.createUpdater().setMentionableFlag(true).setAuditLogReason("Update command").update();
            channel.sendMessage(role.getMentionTag() + " " + payload);
            role.createUpdater().setMentionableFlag(false).update();
        } catch (Exception e) {
            e.printStackTrace();
            role.createUpdater().setMentionableFlag(false).update();
        }
    }

    private EmbedBuilder createPoll() {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setColor(Color.GREEN);
        embed.addField("Subscribe to plugin updates",
                "```Click the \uD83C\uDF4D to subscribe to EssentialsX" +
                        "\nClick the \uD83C\uDF6A to subscribe to FactionsUUID" +
                        "\nClick the \uD83C\uDF54 to subscribe to PlayerVaults" +
                        "\nClick the \ud83C\uDF2F to subscribe to LWC Extended" +
                        "\nClick the \uD83C\uDF7A to subscribe to NuVotifier" +
                        "\nClick the \ud83d\udddd\ufe0f to subscribe to PermissionsEx" +
                        "\nClick the \uD83E\uDD5A to subscribe to Egg82's plugins" +
                        "\nClick the \uD83C\uDFB2 to subscribe to Minigames" +
                        "\nClick the \uD83C\uDF08 to subscribe to Prism```");
        return embed;
    }

    public void onReactionAdd(ReactionAddEvent event) {
        if (event.getUser().isYourself()) {
            return;
        }
        if (!event.getReaction().isPresent()) {
            event.removeReaction();
            return;
        }
        if (storage.ispoll(event.getMessageId())) {
            if (event.getReaction().get().containsYou()) {
                updateRole(event.getUser(), roleMap.get(event.getReaction().get().getEmoji().asUnicodeEmoji().get()), event.getServer().get(), "add");
            } else {
                event.removeReaction();
            }
        }
    }

    public void onReactionRemove(ReactionRemoveEvent event) {
        if (event.getUser().isYourself()) {
            return;
        }
        if (storage.ispoll(event.getMessageId()) && event.getReaction().isPresent() && event.getReaction().get().containsYou()) {
            updateRole(event.getUser(), roleMap.get(event.getReaction().get().getEmoji().asUnicodeEmoji().get()), event.getServer().get(), "remove");
        }
    }

    public void updateRole(User user, String role, Server server, String type) { //TODO
        Role target = server.getRoleById(role).get();
        if (user.getRoles(server).stream().anyMatch(role1 -> role1.getIdAsString().equalsIgnoreCase(role)) && type.equalsIgnoreCase("remove")) {
            user.removeRole(target, "Role Poll");
        } else {
            if (type.equalsIgnoreCase("add")) {
                user.addRole(target, "Role Poll");
            }
        }
    }

}
