package org.moss.discord.listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;

import static org.moss.discord.Constants.*;

public class RoleReactionListeners implements ReactionAddListener, ReactionRemoveListener {

    private DiscordApi api;

    public RoleReactionListeners(DiscordApi api) {
        this.api = api;
    }

    public void onReactionAdd(ReactionAddEvent event) {
        switch (event.requestMessage().join().getIdAsString()) {
            case ESSX_ROLE_MSG: {
                Role role = api.getRoleById(ROLE_ESSX_UPDATES).get();
                event.getUser().addRole(role);
                break;
            }
            case PVX_ROLE_MSG: {
                Role role = api.getRoleById(ROLE_PVX_UPDATES).get();
                event.getUser().addRole(role);
                break;
            }
            case FUUID_ROLE_MSG: {
                Role role = api.getRoleById(ROLE_FUUID_UPDATES).get();
                event.getUser().addRole(role);
                break;
            }
        }
    }

    public void onReactionRemove(ReactionRemoveEvent event) {
        switch (event.requestMessage().join().getIdAsString()) {
            case ESSX_ROLE_MSG: {
                Role role = api.getRoleById(ROLE_ESSX_UPDATES).get();
                event.getUser().removeRole(role);
                break;
            }
            case PVX_ROLE_MSG: {
                Role role = api.getRoleById(ROLE_PVX_UPDATES).get();
                event.getUser().removeRole(role);
                break;
            }
            case FUUID_ROLE_MSG: {
                Role role = api.getRoleById(ROLE_FUUID_UPDATES).get();
                event.getUser().removeRole(role);
                break;
            }
        }
    }

}
