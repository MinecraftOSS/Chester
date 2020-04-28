package org.moss.discord.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.moss.discord.Constants;
import org.moss.discord.storage.RolePollStorage;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class RoleReactionCommand extends ListenerAdapter implements CommandExecutor {

    RolePollStorage storage = new RolePollStorage();
    Map<String, String> roleMap = new LinkedHashMap<>();

    public RoleReactionCommand(JDA api) {
        api.addEventListener(this);
        roleMap.put("\uD83C\uDF4D", Constants.ROLE_ESSX_UPDATES);
        roleMap.put("\uD83C\uDF6A", Constants.ROLE_FUUID_UPDATES);
        roleMap.put("\uD83C\uDF54", Constants.ROLE_PVX_UPDATES);
        roleMap.put("\ud83C\uDF2F", Constants.ROLE_MLWC_UPDATES);
        roleMap.put("\uD83C\uDF7A", Constants.ROLE_NVTFR_UPDATES);
        roleMap.put("\uD83D\uDDDD", Constants.ROLE_PEX_UPDATES);
        roleMap.put("\uD83E\uDD5A", Constants.ROLE_EGG_UPDATES);
        roleMap.put("\uD83C\uDFB2", Constants.ROLE_MNGMES_UPDATES);
        roleMap.put("\uD83C\uDF08", Constants.ROLE_PRISM_UPDATES);
        roleMap.put("\uD83E\uDDCA", Constants.ROLE_PSTONES_UPDATES);
    }

    @Command(aliases = {"!rolepoll", ".rolepoll"}, usage = "!rolepoll", description = "Polls users for update roles")
    public void onCommand(JDA api, TextChannel channel, Member user, Guild server, Message cmd) {
        if (user.hasPermission(Permission.ADMINISTRATOR)) {
            cmd.delete().queue();
            try {
                Message msg = channel.sendMessage(createPoll().build()).complete();
                roleMap.keySet().forEach(s -> msg.addReaction(s).queue());
                storage.set(msg.getId(), msg.getChannel().getId());
            } catch (Exception ignored) {}
        }
    }

    @Command(aliases = {"!rpupdate", ".rpupdate"}, usage = "!rpupdate", description = "Updates all roll polls.")
    public void onRPUpdate(JDA api, TextChannel channel, Member user, Guild server, Message cmd) {
        if (user.hasPermission(Permission.ADMINISTRATOR)) {
            cmd.delete().queue();
            try {
                for (String key : storage.getMap().keySet()) {
                    api.getTextChannelById(storage.getChannel(key)).retrieveMessageById(key).queue(msg -> {
                        msg.editMessage(createPoll().build()).queue();
                        roleMap.keySet().forEach(s -> msg.addReaction(s).queue());
                    });
                    api.getTextChannelById(storage.getChannel(key)).retrieveMessageById(key).queue(msg -> {
                        msg.editMessage(createPoll().build()).queue();
                    });
                }
            } catch (Exception e) {
                channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Unable to update polls").build()).queue();
            }
        }
    }

    /*
    TODO: Map the channels instead.
     */
    @Command(aliases = {"!update", ".update"}, usage = "!update", description = "Polls users for update roles")
    public void onUpdate(JDA api, TextChannel channel, Member user, Guild server, String[] args, Message cmd) {
        if (user.hasPermission(Permission.KICK_MEMBERS)) {
            cmd.delete().queue();
            try {
                switch (channel.getId()) {
                    case "426460619277991936": //essx
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_ESSX_UPDATES));
                        break;
                    case "426460663498407948": //fuuid
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_FUUID_UPDATES));
                        break;
                    case "426460690136694795": //pvx
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_PVX_UPDATES));
                        break;
                    case "479919913067216897": //LWCX
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_MLWC_UPDATES));
                        break;
                    case "430125681645453325": //NVTFR
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_NVTFR_UPDATES));
                        break;
                    case "632427764707753994": //PEX
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_PEX_UPDATES));
                        break;
                    case "673969910585491466": //EGG
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_EGG_UPDATES));
                        break;
                    case "675063189934964748": //MNGMES
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_MNGMES_UPDATES));
                        break;
                    case "675838377198747678": //PRISM
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_PRISM_UPDATES));
                        break;
                    case "676271306714382385": //PSTONES
                        broadcast(String.join(" ", args), channel, server.getRoleById(Constants.ROLE_PSTONES_UPDATES));
                        break;
                    case "397536210236604427": //TEST
                        broadcast(String.join(" ", args), channel, server.getRoleById("585793006611726346"));
                        break;
                    default:
                        channel.sendMessage(new MessageBuilder().setContent(user.getAsMention()).setEmbed(new EmbedBuilder().setTitle("Invalid update channel").setColor(Color.RED).build()).build()).queue();
                }
            } catch (Exception e) {
                channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Failed").build()).queue();
                e.printStackTrace();
            }
        }
    }

    private void broadcast(String payload, TextChannel channel, Role role) {
        try {
            role.getManager().setMentionable(true).reason("Update command").complete();
            channel.sendMessage(role.getAsMention() + " " + payload).complete();
            role.getManager().setMentionable(false).complete();
        } catch (Exception e) {
            e.printStackTrace();
            role.getManager().setMentionable(false).complete();
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
                        "\nClick the \uD83E\uDDCA to subscribe to ProtectionStones" +
                        "\nClick the \uD83C\uDF08 to subscribe to Prism```", false);
        return embed;
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }
        if (!event.getReaction().getReactionEmote().isEmoji()) {
            event.getReaction().removeReaction().queue();
            return;
        }
        if (storage.ispoll(event.getMessageIdLong())) {
            if (event.getReaction().retrieveUsers().complete().contains(event.getJDA().getSelfUser())) {
                updateRole(event.getMember(), roleMap.get(event.getReaction().getReactionEmote().getEmoji()), event.getGuild(), "add");
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        if (event.getUser().isBot()) {
            return;
        }
        if (storage.ispoll(event.getMessageIdLong()) && event.getReaction().getReactionEmote().isEmoji() && event.getReaction().retrieveUsers().complete().contains(event.getJDA().getSelfUser())) {
            updateRole(event.getMember(), roleMap.get(event.getReactionEmote().getEmoji()), event.getGuild(), "remove");
        }
    }

    public void updateRole(Member user, String role, Guild server, String type) { //TODO
        Role target = server.getRoleById(role);
        if (user.getRoles().stream().anyMatch(role1 -> role1.getId().equalsIgnoreCase(role)) && type.equalsIgnoreCase("remove")) {
            server.removeRoleFromMember(user, target).reason("Role Poll").queue();
        } else {
            if (type.equalsIgnoreCase("add")) {
                server.addRoleToMember(user, target).reason("Role Poll").queue();
            }
        }
    }

}
