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
    private final JDA api;

    @Override
    public void run() {
        Map<Long, Collection<MessageEmbed>> msg = new HashMap<>();
        List<JSONObject> users = new ArrayList<>();

        try {
            JSONObject variable = getUsers();
            JSONObject response = AniListQueryHandler.query(AniListQueryType.LIST, variable);

            if (response != null) {
                JSONObject data = response.getJSONObject("Page");
                getUserActivities(users, data.getJSONArray("activities"));

                while (data.getJSONObject("pageInfo").getBoolean("hasNextPage")) {
                    variable.increment("page");

                    do {
                        response = AniListQueryHandler.query(AniListQueryType.LIST, variable);
                    } while (response == null);
                    data = response.getJSONObject("Page");
                    getUserActivities(users, data.getJSONArray("activities"));
                }
            }
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage(), e);
        }

        for (JSONObject user: users) {
            long anilistID = user.getJSONObject("user").getLong("id");
            try {
                List<Long> channelIDs = DatabaseHandler.getInstance().channelIdsOfUser(anilistID);
                for (long id : channelIDs) {
                    // TODO: Better Check
                    if (!user.getJSONObject("media").getBoolean("isAdult")) {
                        MessageEmbed embed = buildMsg(user);
                        msg.computeIfAbsent(id, k -> new ArrayList<>()).add(embed);
                    }
                }
            } catch (SQLException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }

        for (long id: msg.keySet()) {
            TextChannel channel = this.api.getTextChannelById(id);
            if (channel != null) {
                MessageCreateBuilder newMsg = new MessageCreateBuilder()
                        .setEmbeds(msg.get(id))
                        //TODO: Check for Suppressed Notifications
                        .setSuppressedNotifications(true);

                channel.sendMessage(newMsg.build()).completeAfter(500, TimeUnit.MILLISECONDS);
            }
        }
    }

    // ============================
    // Private Helper Methods
    // ============================

    /**
     * Put all of User Activities into a List
     * @param activities Array of all User Activities
     */
    private static void getUserActivities(@NonNull List<JSONObject> users, @NonNull JSONArray activities) {
        for (int i = 0; i < activities.length(); i++) {
            JSONObject activity = activities.getJSONObject(i);
            users.add(activity);
        }
    }

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
