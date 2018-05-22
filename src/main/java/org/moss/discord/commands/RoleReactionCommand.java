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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleReactionCommand implements CommandExecutor, ReactionAddListener, ReactionRemoveListener {

    RolePollStorage storage = new RolePollStorage();
    Map<String, String> messageMap = new HashMap<>();

    public RoleReactionCommand(DiscordApi api) {
        api.addListener(this);
        messageMap.put("Click the pineapple to subscribe to EssentialsX", Constants.ROLE_ESSX_UPDATES+":\uD83C\uDF4D");
        messageMap.put("Click the cookie to subscribe to FactionsUUID", Constants.ROLE_FUUID_UPDATES+":\uD83C\uDF6A");
        messageMap.put("Click the burger to subscribe to PlayerVaults", Constants.ROLE_PVX_UPDATES+":\uD83C\uDF54");
    }

    @Command(aliases = {"!rolepoll", ".rolepoll"}, usage = "!rolepoll", description = "Polls users for update roles")
    public void onCommand(DiscordApi api, TextChannel channel, User user, Server server, Message cmd) {
        if (hasPermission(user.getRoles(server))) {
            cmd.delete();
            try {
                for (String message : messageMap.keySet()) {
                    Message msg = channel.sendMessage(new EmbedBuilder().setTitle(message).setColor(Color.GREEN)).get();
                    msg.addReaction(messageMap.get(message).split(":")[1]);
                    storage.set(msg.getIdAsString(), messageMap.get(message).split(":")[0]);
                }
            } catch (Exception e) {}
        }
    }

    public void onReactionAdd(ReactionAddEvent event) {
        if (event.getUser().isYourself()) {
            return;
        }
        if (!event.getReaction().isPresent()) {
            event.removeReaction();
        }
        if (storage.ispoll(event.getMessageId())) {
            if (event.getReaction().get().containsYou()) {
                updateRole(event.getUser(), storage.getRole(event.getMessageId()), event.getServer().get(), "add");
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
            updateRole(event.getUser(), storage.getRole(event.getMessageId()), event.getServer().get(), "remove");
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

    public Boolean hasPermission(List<Role> roles) { //TODO
        for (Role role : roles) {
            String roleId = role.getIdAsString();
            if ((roleId.equals(Constants.ROLE_MODERATOR) || roleId.equals(Constants.ROLE_ADMIN))) {
                return true;
            }
        }
        return false;
    }
}
