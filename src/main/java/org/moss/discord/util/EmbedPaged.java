package org.moss.discord.util;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.SingleReactionEvent;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by burdoto (Kaleidox) https://github.com/burdoto
 * https://github.com/burdoto/javacord-utilities/blob/master/src/main/java/de/kaleidox/javacord/util/ui/messages/paging/PagedEmbed.java
 */

public class EmbedPaged {

    private final static int DELETE_EMOJIS_TIME = 15;
    private final static TimeUnit DELETE_EMOJIS_UNIT = TimeUnit.MINUTES;
    private final static String PREV_PAGE_EMOJI = "\u2b05";
    private final static String NEXT_PAGE_EMOJI = "\u27a1";

    private final Messageable messageable;
    private final User user;

    private ConcurrentHashMap<Integer, EmbedBuilder> pages = new ConcurrentHashMap<>();
    private int page;
    private AtomicReference<Message> sentMessage = new AtomicReference<>();

    public EmbedPaged(Messageable messageable, List<EmbedBuilder> embeds) {
        this.messageable = messageable;
        addPages(embeds);
        this.user = null;
    }


    public EmbedPaged(Messageable messageable, List<EmbedBuilder> embeds, User user) {
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


    /**
     * Builds & sends the PagedEmbed.
     *
     * @return A {@code CompletableFuture} that will contain the sent message.
     */
    @SuppressWarnings("Duplicates")
    public CompletableFuture<Message> build() {
        page = 1;

        CompletableFuture<Message> future = messageable.sendMessage(pages.get(page));

        future.thenAcceptAsync(message -> {
            sentMessage.set(message);
            if (pages.size() != 1) {
                message.addReaction(PREV_PAGE_EMOJI);
                message.addReaction(NEXT_PAGE_EMOJI);
                message.addReactionAddListener(this::onReactionClick);
                message.addReactionRemoveListener(this::onReactionClick);
            }

            message.addMessageDeleteListener(delete -> message.getMessageAttachableListeners()
                    .forEach((a, b) -> message.removeMessageAttachableListener(a)))
                    .removeAfter(DELETE_EMOJIS_TIME, DELETE_EMOJIS_UNIT)
                    .addRemoveHandler(() -> {
                        sentMessage.get()
                                .removeAllReactions();
                        sentMessage.get()
                                .getMessageAttachableListeners()
                                .forEach((a, b) -> message.removeMessageAttachableListener(a));
                    });
        }).exceptionally(ExceptionLogger.get());

        return future;
    }

    /**
     * Updates the embed in the message according to the page number.
     */
    private void refreshMessage() {
        if (sentMessage.get() != null) {
            sentMessage.get().edit(pages.get(page));
        }
    }

    private void onReactionClick(SingleReactionEvent event) {
        event.getEmoji().asUnicodeEmoji().ifPresent(emoji -> {
            if (!event.getUser().isYourself()) {
                if (user != null && !event.getUser().equals(user)) return;
                switch (emoji) {
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
        });
    }

}