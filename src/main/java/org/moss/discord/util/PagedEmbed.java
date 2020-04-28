package org.moss.discord.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
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

public class PagedEmbed extends ListenerAdapter {

    private final static int FIELD_MAX_CHARS = 1024;
    private final static int MAX_CHARS_PER_PAGE = 4500;
    private int MAX_FIELDS_PER_PAGE = 8;
    private final static int DELETE_EMOJIS_TIME = 15;
    private final static TimeUnit DELETE_EMOJIS_UNIT = TimeUnit.MINUTES;
    private final static String PREV_PAGE_EMOJI = "\u2b05";
    private final static String NEXT_PAGE_EMOJI = "\u27a1";

    private final PagedEmbed instance = this;
    private final MessageChannel messageable;
    private final EmbedBuilder embed;
    private final User user;

    private final ConcurrentHashMap<Integer, List<Field>> pages = new ConcurrentHashMap<>();
    private final List<Field> fields = new ArrayList<>();
    private int page;
    private final AtomicReference<Message> sentMessage = new AtomicReference<>();

    /**
     * Creates a new PagedEmbed object.
     *
     * @param messageable The Messageable in which the embed should be sent.
     * @param embed       An EmbedBuilder the sent embed should be based on.
     */
    public PagedEmbed(MessageChannel messageable, EmbedBuilder embed) {
        this.messageable = messageable;
        this.embed = embed;
        this.user = null;
    }

    /**
     * Creates a new PagedEmbed object and locks it to a user.
     *
     * @param messageable The Messageable in which the embed should be sent.
     * @param embed       An EmbedBuilder the sent embed should be based on.
     * @param user        A user to lock the embed with
     */
    public PagedEmbed(MessageChannel messageable, EmbedBuilder embed, User user) {
        this.messageable = messageable;
        this.embed = embed;
        this.user = user;
    }

    /**
     * Adds a new non-inline field to the paged embed.
     *
     * @param title The title of the field.
     * @param text  The text of the field.
     * @return The new, modified PagedEmbed object.
     */
    public PagedEmbed addField(String title, String text) {
        return addField(title, text, false);
    }

    /**
     * Adds a new field to the pages embed.
     *
     * @param title  The title of the field.
     * @param text   The text of the field.
     * @param inline If the field should be inline.
     * @return The new, modified PageEmbed object.
     */
    public PagedEmbed addField(String title, String text, boolean inline) {
        fields.add(
            new Field(
                title,
                text,
                inline
            )
        );

        return this;
    }

    /**
     * Builds & sends the PagedEmbed.
     *
     * @return A {@code CompletableFuture} that will contain the sent message.
     */
    public CompletableFuture<Message> build() {
        page = 1;
        refreshPages();


        CompletableFuture<Message> future = new CompletableFuture<>();
        messageable.sendMessage(embed.build()).queue(message -> {
            sentMessage.set(message);
            if (pages.size() != 1) {
                message.addReaction(PREV_PAGE_EMOJI).queue();
                message.addReaction(NEXT_PAGE_EMOJI).queue();
                message.getJDA().addEventListener(instance);
            }
            future.complete(message);
        });
        return future;
    }

    /**
     * Re-creates the map containing the pages.
     */
    private void refreshPages() {
        int fieldCount = 0, pageChars = 0, totalChars = 0, thisPage = 1;
        pages.clear();

        for (Field field : fields) {
            pages.putIfAbsent(thisPage, new ArrayList<>());

            if (fieldCount <= MAX_FIELDS_PER_PAGE-1 &&
                    pageChars <= FIELD_MAX_CHARS * fieldCount &&
                    totalChars < MAX_CHARS_PER_PAGE) {
                pages.get(thisPage)
                        .add(field);

                fieldCount++;
                pageChars = pageChars + field.getTotalChars();
                totalChars = totalChars + field.getTotalChars();
            } else {
                thisPage++;
                pages.putIfAbsent(thisPage, new ArrayList<>());

                pages.get(thisPage)
                        .add(field);

                fieldCount = 1;
                pageChars = field.getTotalChars();
                totalChars = field.getTotalChars();
            }
        }

        refreshMessage();
    }

    /**
     * Updates the embed in the message according to the page number.
     */
    private void refreshMessage() {
        // Refresh the embed to the current page
        embed.clearFields();

        pages.get(page)
                .forEach(field -> {
                    embed.addField(
                            field.getTitle(),
                            field.getText(),
                            field.getInline()
                    );
                });
        if (pages.size() > 1)
            embed.setFooter("Page " + page + " of " + pages.size());

        // Edit sent message
        if (sentMessage.get() != null) {
            sentMessage.get().editMessage(embed.build()).queue();
        }
    }

    private void onReactionClick(GenericGuildMessageReactionEvent event) {
        if (event.getReactionEmote().isEmoji()) {
            if (!event.getUser().getId().equals(event.getJDA().getSelfUser().getId())) {
                if (user != null && !event.getUser().equals(user)) return;
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

    public EmbedBuilder getEmbed() {
        return embed;
    }

    /**
     * Sets the amount of fields per page.
     * @param amount  The max amount of field per page.
     */
    public void setMaxFieldsPerPage(int amount) {
        MAX_FIELDS_PER_PAGE = amount;
    }

    /**
     * This subclass represents an embed field for the PagedEmbed.
     */
    static class Field {
        private final String title;
        private final String text;
        private final boolean inline;

        Field(String title, String text, boolean inline) {
            this.title = title;
            this.text = text;
            this.inline = inline;
        }

        /**
         * Gets the title of the field.
         *
         * @return The title of the field.
         */
        String getTitle() {
            return title;
        }

        /**
         * Gets the text of the field.
         *
         * @return The text of the field.
         */
        String getText() {
            return text;
        }

        /**
         * Gets if the field is inline.
         *
         * @return If the field is inline.
         */
        boolean getInline() {
            return inline;
        }

        /**
         * Returns the total characters of the field.
         *
         * @return The total characters of the field.
         */
        int getTotalChars() {
            return title.length() + text.length();
        }
    }
}