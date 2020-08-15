package org.moss.discord.listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.ServerUpdater;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.user.UserChangeNameEvent;
import org.javacord.api.event.user.UserChangeNicknameEvent;
import org.javacord.api.listener.server.ServerJoinListener;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.user.UserChangeNameListener;
import org.javacord.api.listener.user.UserChangeNicknameListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Prevent users from setting annoying nicknames or real names.
 */
public class AntiHoistingListener implements UserChangeNameListener, UserChangeNicknameListener, ServerMemberJoinListener, ServerJoinListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AntiHoistingListener.class);
    private static final Pattern FORBIDDEN_NAMES = Pattern.compile("^[!*.,'($-]");
    private static final String FORBIDDEN_NAME_REASON = "no self-hoisting :(";

    private final DiscordApi api;

    public AntiHoistingListener(DiscordApi api) {
        this.api = api;
        this.api.getServers().forEach(this::validateExistingServer);
    }

    // Event Listeners //

    @Override
    public void onServerJoin(ServerJoinEvent event) {
        LOGGER.info("Joining server {}", event.getServer().getName());
        validateExistingServer(event.getServer());
    }

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        testAntiHoist(event.getServer(), event.getUser(),
                event.getUser().getNickname(event.getServer()).orElse(event.getUser().getName()));
    }

    @Override
    public void onUserChangeName(UserChangeNameEvent event) {
        for (Server server : event.getUser().getMutualServers()) {
            testAntiHoist(server, event.getUser(), event.getNewName());
        }
    }

    @Override
    public void onUserChangeNickname(UserChangeNicknameEvent event) {
        final String nickname = event.getNewNickname().orElse(event.getUser().getName());
        testAntiHoist(event.getServer(), event.getUser(), nickname);
    }

    // Detection logic //

    private void validateExistingServer(final Server server) {
        LOGGER.info("Validating server {}", server.getName());
        ServerUpdater updater = null;
        int updateCount = 0;
        for (User user : server.getMembers()) {
            if (shouldPreventHoist(server, user) && canManageUser(server, user)) {
                if (updater == null) {
                    updater = server.createUpdater();
                    updater.setAuditLogReason(FORBIDDEN_NAME_REASON);
                }
                updater.setNickname(user, generateNickname(user));
                updateCount++;
            }
        }
        if (updater != null) {
            int finalUpdateCount = updateCount;
            updater.update().thenRun(() -> {
                LOGGER.info("Successfully corrected the annoying names of {} users in {}", finalUpdateCount, server.getName());
            }).exceptionally(err -> {
                LOGGER.error("Unable to update user nicknames in {}", server.getName(), err);
                return null;
            });
        }

    }

    private void testAntiHoist(final Server server, final User user, final String newNick) {
        if (server.hasPermission(server.getApi().getYourself(), PermissionType.MANAGE_NICKNAMES) // can we change nicknames
                && FORBIDDEN_NAMES.matcher(newNick).lookingAt()) { // whoops
            if (canManageUser(server, user)) {
                server.updateNickname(user, generateNickname(user), FORBIDDEN_NAME_REASON);
            }
            // TODO: maybe notify users?
        }
    }

    private boolean shouldPreventHoist(final Server server, final User user)  {
        final String nameOrNickname = user.getNickname(server).orElse(user.getName());
        return server.hasPermission(server.getApi().getYourself(), PermissionType.MANAGE_NICKNAMES) // can we change nicknames
                && FORBIDDEN_NAMES.matcher(nameOrNickname).lookingAt(); // is this a bad nick

    }

    /**
     * Get if the user has a role higher than the bot's highest role in the server.
     *
     * @param server server to check
     * @param user user to check
     * @return if things will go boom trying to do management things
     */
    private boolean canManageUser(final Server server, final User user) {
        int highestBotRole = -1;
        int highestUserRole = -1;
        final List<Role> roles = server.getRoles();
        for (int i = roles.size() - 1; i >= 0; i--) { // highest role comes first
            final Role role = roles.get(i);
            if (role.hasUser(api.getYourself())) {
                highestBotRole = Math.max(highestBotRole, role.getRawPosition());
            }
            if (role.hasUser(user)) {
                highestUserRole = Math.max(highestUserRole, role.getRawPosition());
            }

            if (highestBotRole != -1 && highestUserRole != -1) {
                break;
            }
        }
        return highestBotRole >= highestUserRole;
    }

    private String generateNickname(final User user) {
        return "zAnnoying name #" + Math.abs(user.getName().hashCode() % 10000);
    }
}
