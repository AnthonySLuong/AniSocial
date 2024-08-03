package org.AniSocial.util.AniList;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.AniSocial.util.DatabaseHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class AniListTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AniListTask.class);
    private final Map<Long, Collection<MessageEmbed>> allMsg = new HashMap<>();
    private final JDA api;

    @Override
    public void run() {
        try {
            JSONObject variable = getUsers();
            JSONObject response = AniListQueryHandler.query(AniListQueryType.LIST, variable);

            if (response != null) {
                JSONObject data = response.getJSONObject("Page");
                JSONArray activities = data.getJSONArray("activities");
                iterateSinglePage(this.allMsg, activities);

                while (data.getJSONObject("pageInfo").getBoolean("hasNextPage")) {
                    variable.increment("page");

                    do {
                        response = AniListQueryHandler.query(AniListQueryType.LIST, variable);
                    } while (response == null);

                    iterateSinglePage(this.allMsg, activities);
                }
            }

            for (long id: this.allMsg.keySet()) {
                TextChannel channel = this.api.getTextChannelById(id);
                if (channel != null) {
                    MessageCreateBuilder newMsg = new MessageCreateBuilder()
                            .setEmbeds(this.allMsg.get(id))
                            //TODO: Check for Suppressed Notifications
                            .setSuppressedNotifications(true);

                    channel.sendMessage(newMsg.build()).completeAfter(500, TimeUnit.MILLISECONDS);
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    // ============================
    // Private Helper Methods
    // ============================

    /**
     * Query List of User added to database
     * @return JSON of Variable for graphQL
     * @throws SQLException Any SQLException
     */
    @NonNull
    private static JSONObject getUsers() throws SQLException {
        List<Long> userId = DatabaseHandler.getInstance().listofUserIds();
        JSONObject variable = new JSONObject();
        variable.put("userids", userId);
        variable.put("page", 1);
        variable.put("time", Instant.now().getEpochSecond() - 15);
        return variable;
    }

    /**
     * Iterate a single activity page and build each message
     * @param allMsg Collection of message grouped by channel ID
     * @param activities Single page of Activity
     * @throws SQLException Any SQLException
     */
    private static void iterateSinglePage(@NonNull Map<Long, Collection<MessageEmbed>> allMsg,
                                          @NonNull JSONArray activities) throws SQLException {
        for (int i = 0; i < activities.length(); i++) {
            long anilistId = activities.getJSONObject(i).getJSONObject("user").getLong("id");
            List<Long> channelid = DatabaseHandler.getInstance().channelIdsOfUser(anilistId);
            for (long id : channelid) {
                // TODO: Better Check
                if (!activities.getJSONObject(i).getJSONObject("media").getBoolean("isAdult")) {
                    MessageEmbed msg = buildMsg(activities.getJSONObject(i));
                    allMsg.computeIfAbsent(id, k -> new ArrayList<>()).add(msg);
                }
            }
        }
    }

    /**
     * Build each Activity Update into Embedded Message
     * @param msg Message
     * @return Embedded Message
     */
    @NonNull
    private static MessageEmbed buildMsg(@NonNull JSONObject msg) {
        JSONObject user =  msg.getJSONObject("user");
        JSONObject media =  msg.getJSONObject("media");
        JSONObject title = media.getJSONObject("title");
        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(
                        user.getString("name"),
                        user.getString("siteUrl"),
                        user.getJSONObject("avatar").getString("medium"))
                .setTitle(
                        title.getString("romaji"),
                        media.getString("siteUrl"))
                .setThumbnail(media.getJSONObject("coverImage").getString("medium"))
                .setTimestamp(Instant.ofEpochSecond(msg.getLong("createdAt")));

        StringBuilder extraTitle = new StringBuilder();
        for (String key : title.keySet()) {
            if (!title.isNull(key)) {
                extraTitle.append(title.getString(key)).append("\n");
            }
        }
        embed.addField("Titles", extraTitle.toString(), true);

        if (msg.isNull("progress")) {
            embed.addField(capitalizeEachWord(msg.getString("status")), " ", true);
        } else {
            embed.addField(capitalizeEachWord(msg.getString("status")), msg.getString("progress"), true);
        }

        return embed.build();
    }

    /**
     * Capitalized Each Word, Split by whitespace
     * @param input String
     * @return String where each word is capitalized
     */
    @NonNull
    private static String capitalizeEachWord(@NonNull String input) {
        if (input.isEmpty()) {
            return input;
        }

        String[] words = input.split("\\s+");
        StringBuilder capitalized = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return capitalized.toString().trim();
    }
}
