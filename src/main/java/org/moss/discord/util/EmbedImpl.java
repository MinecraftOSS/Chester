package org.moss.discord.util;

import com.fasterxml.jackson.databind.JsonNode;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.OffsetDateTime;

public class EmbedImpl {

  private final EmbedBuilder embedBuilder = new EmbedBuilder();

  public EmbedImpl(JsonNode data) {
    embedBuilder.setTitle(data.has("title") ? data.get("title").asText() : null, data.has("url") ? data.get("url").asText() : null);
//    type = data.has("type") ? data.get("type").asText() : null;
    embedBuilder.setDescription(data.has("description") ? data.get("description").asText() : null);
    embedBuilder.setTimestamp(data.has("timestamp") ? OffsetDateTime.parse(data.get("timestamp").asText()).toInstant() : null);
    embedBuilder.setColor(data.has("color") ? new Color(data.get("color").asInt()) : null);
    JsonNode footer = data.get("footer");
    if (footer != null) {
      embedBuilder.setFooter(footer.has("text") ? footer.get("text").asText() : null, footer.has("icon_url") && !footer.get("icon_url").isNull() ? footer.get("icon_url").asText() : null);
    }
    JsonNode image = data.get("image");
    if (image != null) {
      embedBuilder.setImage(image.has("url") ? image.get("url").asText() : null);
    }
    JsonNode thumbnail = data.get("thumbnail");
    if (thumbnail != null) {
      embedBuilder.setThumbnail(thumbnail.has("url") ? thumbnail.get("url").asText() : null);
    }
//    video = data.has("video") ? new EmbedVideoImpl(data.get("video")) : null;
//    provider = data.has("provider") ? new EmbedProviderImpl(data.get("provider")) : null;
    JsonNode author = data.get("author");
    if (author != null) {
      embedBuilder.setAuthor(author.has("name") ? author.get("name").asText() : null, author.has("url") && !author.get("url").isNull() ? author.get("url").asText() : null, author.has("icon_url") && !author.get("icon_url").isNull() ? author.get("icon_url").asText() : null);
    }
    if (data.has("fields")) {
      for (JsonNode jsonField : data.get("fields")) {
        embedBuilder.addField(jsonField.has("name") ? jsonField.get("name").asText() : null, jsonField.has("value") ? jsonField.get("value").asText() : null, jsonField.has("inline") && jsonField.get("inline").asBoolean());
      }
    }
  }

  public EmbedBuilder toBuilder() {
    return embedBuilder;
  }
}
