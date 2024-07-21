package org.AniSocial.util.AniList;

import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.AniSocial.Listener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor()
public class AniListRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);

    public static void run(JDA api) {
        try (ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1)) {
            executorService.scheduleAtFixedRate(() -> task(api), 0, 15, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static void task(JDA api) {
        JSONObject variable = getVariables();
        JSONObject response = AniListQueryHandler.query(AniListQueryType.LIST, variable);

        if (response != null) {
            JSONArray activities = response.getJSONArray("activities");
            JSONObject pages = response.getJSONObject("pageInfo");
            int lastPage = pages.getInt("lastPage");

            for (int i = 0; i < lastPage; i++) {
                for (int j = 0; j < activities.length(); j++) {
                    // TODO: Database for channelId
                    MessageEmbed msg = buildMsg(activities.getJSONObject(j));
                    TextChannel channel = api.getTextChannelById("1261222335701323776");
                    if (channel != null) {
                        channel.sendMessageEmbeds(msg).completeAfter(500, TimeUnit.MILLISECONDS);
                    }
                }

                if (i < lastPage - 1) {
                    variable.increment("page");
                    do {
                        response = AniListQueryHandler.query(AniListQueryType.LIST, variable);
                    } while (response == null);
                }
            }
        }
    }

    private static JSONObject getVariables() {
        // TODO: Query Database for UsersID
        int[] userId = new int[] {295061};
        JSONObject variable = new JSONObject();
        variable.put("userids", userId);
        variable.put("page", 1);
        variable.put("time", Instant.now().getEpochSecond() - 15);
        return variable;
    }

    private static MessageEmbed buildMsg(JSONObject msg) {
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

    private static String capitalizeEachWord(String input) {
        if (input == null || input.isEmpty()) {
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
