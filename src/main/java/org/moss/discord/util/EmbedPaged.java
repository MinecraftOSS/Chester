package org.moss.discord.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by burdoto (Kaleidox) https://github.com/burdoto
 * https://github.com/burdoto/javacord-utilities/blob/master/src/main/java/de/kaleidox/javacord/util/ui/messages/paging/PagedEmbed.java
 */

public class EmbedPaged extends ListenerAdapter {

    private final static int DELETE_EMOJIS_TIME = 15;
    private final static TimeUnit DELETE_EMOJIS_UNIT = TimeUnit.MINUTES;
    private final static String PREV_PAGE_EMOJI = "\u2b05";
    private final static String NEXT_PAGE_EMOJI = "\u27a1";

    private final MessageChannel messageable;
    private final User user;
    private final EmbedPaged instance = this;

    private boolean buttonPaged = false;
    private final ConcurrentHashMap<Integer, EmbedBuilder> pages = new ConcurrentHashMap<>();
    private int page;
    private final AtomicReference<Message> sentMessage = new AtomicReference<>();

    public EmbedPaged(MessageChannel messageable, List<EmbedBuilder> embeds) {
        this.messageable = messageable;
        addPages(embeds);
        this.user = null;
    }


    public EmbedPaged(MessageChannel messageable, List<EmbedBuilder> embeds, User user) {
        this.messageable = messageable;
        addPages(embeds);
        this.user = user;
    }

    private void addPages(List<EmbedBuilder> embeds) {
        int lePage = 1;
        for (EmbedBuilder ebs : embeds) {
            pages.putIfAbsent(lePage, ebs);
            lePage++;
        }
    }

    public EmbedPaged setPagedButtons(boolean bool) {
        buttonPaged = (bool && pages.size() <= 9);
        return this;
    }


    /**
     * Builds & sends the PagedEmbed.
     *
     * @return A {@code CompletableFuture} that will contain the sent message.
     */
    @SuppressWarnings("Duplicates")
    public CompletableFuture<Message> build() {
        page = 1;


        CompletableFuture<Message> future = new CompletableFuture<>();
            messageable.sendMessage(new MessageBuilder().setContent(getPageInfo()).setEmbed(pages.get(page).build()).build()).queue(message -> {
                sentMessage.set(message);
                if (pages.size() != 1) {
                    if (buttonPaged) {
                        for (int i = 1; i <= pages.size(); i++) {
                            message.addReaction(i+"\u20E3").queue();
                        }
                    } else {
                        message.addReaction(PREV_PAGE_EMOJI).queue();
                        message.addReaction(NEXT_PAGE_EMOJI).queue();
                    }
                    message.getJDA().addEventListener(instance);
                    future.complete(message);
                }
            });
        return future;
    }

    /**
     * Updates the embed in the message according to the page number.
     */
    private void refreshMessage() {
        if (sentMessage.get() != null) {
            sentMessage.get().editMessage(new MessageBuilder().setContent(getPageInfo()).setEmbed(pages.get(page).build()).build()).queue();
        }
    }

    private void onReactionClick(GenericGuildMessageReactionEvent event) {
        if (event.getReactionEmote().isEmoji()) {
            if (!event.getUser().getId().equals(event.getJDA().getSelfUser().getId())) {
                if (user != null && !event.getUser().equals(user)) return;
                if (buttonPaged) {
                    page = Integer.parseInt(event.getReactionEmote().getEmoji().replace("\u20E3", ""));
                    this.refreshMessage();
                    return;
                }
                switch (event.getReactionEmote().getEmoji()) {
                    case PREV_PAGE_EMOJI:
                        if (page > 1)
                            page--;
                        else if (page == 1)
                            page = pages.size();

                        this.refreshMessage();
                        break;
                    case NEXT_PAGE_EMOJI:
                        if (page < pages.size())
                            page++;
                        else if (page == pages.size())
                            page = 1;
                        this.refreshMessage();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if (event.getMessageId().equals(sentMessage.get().getId())) {
            onReactionClick(event);
        }
    }

    @Override
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        if (event.getMessageId().equals(sentMessage.get().getId())) {
            onReactionClick(event);
        }
    }

    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        if (event.getMessageId().equals(sentMessage.get().getId())) {
            event.getJDA().removeEventListener(instance);
        }
    }

    private String getPageInfo() {
        return String.format("Page %d of %d", page, pages.size());
    }
}