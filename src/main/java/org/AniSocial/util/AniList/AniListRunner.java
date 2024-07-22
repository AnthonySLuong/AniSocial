package org.AniSocial.util.AniList;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.AniSocial.util.DatabaseHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AniListRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AniListRunner.class);
    private static AniListRunner aniListRunner = null;

    public void run(@NonNull JDA api) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            try {
                JSONObject variable = getVariables();
                JSONObject response = AniListQueryHandler.query(AniListQueryType.LIST, variable).getJSONObject("Page");

                if (response != null) {
                    JSONArray activities = response.getJSONArray("activities");
                    JSONObject pages = response.getJSONObject("pageInfo");
                    int lastPage = pages.getInt("lastPage");

                    for (int i = 0; i < lastPage; i++) {
                        for (int j = 0; j < activities.length(); j++) {
                            // TODO: Database for channelId
                            long anilistId = activities.getJSONObject(j).getJSONObject("user").getLong("id");
                            List<Long> channelid = DatabaseHandler.getInstance().queryChannel(anilistId);
                            for (long id : channelid) {
                                MessageEmbed msg = buildMsg(activities.getJSONObject(j));
                                TextChannel channel = api.getTextChannelById(id);
                                if (channel != null) {
                                    channel.sendMessageEmbeds(msg).completeAfter(500, TimeUnit.MILLISECONDS);
                                }
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
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        };

        executorService.scheduleAtFixedRate(task, 0, 15, TimeUnit.SECONDS);
        LOGGER.info("Starting AniSocialRunner");
    }

    @NonNull
    private static JSONObject getVariables() throws SQLException {
        List<Long> userId = DatabaseHandler.getInstance().queryUser();
        JSONObject variable = new JSONObject();
        variable.put("userids", userId);
        variable.put("page", 1);
        variable.put("time", Instant.now().getEpochSecond() - 15);
        return variable;
    }

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

    @NonNull
    public static AniListRunner getInstance() {
        if (aniListRunner == null) {
            aniListRunner = new AniListRunner();
        }
        return new AniListRunner();
    }
}
