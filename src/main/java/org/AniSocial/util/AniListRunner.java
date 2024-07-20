package org.AniSocial.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import okhttp3.*;
import org.AniSocial.AniSocial;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AniListRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AniSocial.class);
    private static final OkHttpClient CLIENT = new OkHttpClient();

    public static void run(JDA api) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                JSONObject variable = getVariables();
                JSONObject response = checkListUpdate(variable);

                if (response != null) {
                    JSONObject data = unwrapData(response);
                    JSONArray activities = data.getJSONArray("activities");
                    JSONObject pages = data.getJSONObject("pageInfo");
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
                                response = checkListUpdate(variable);
                            } while (response == null);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("An error occurred during task execution", e);
            }
        };

        executorService.scheduleAtFixedRate(task, 0, 15, TimeUnit.SECONDS);
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

    private static JSONObject checkListUpdate(JSONObject variable) {
        String payload = Query.toPayload(Query.QueryString.LIST, variable).toString();
        RequestBody body = RequestBody.create(payload, MediaType.parse("application/json"));
        Request request = new Request.Builder()
            .url(Query.URL)
            .post(body)
            .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return new JSONObject(response.body().string());
            }
            LOGGER.error(String.format("Received response but not valid: %s", response));
        } catch (Exception e) {
            LOGGER.error(String.format("Execution caught during Post Request: %s", e.getMessage()));
        }

        return null;
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

    private static JSONObject unwrapData(JSONObject jsonObject) {
        return jsonObject.getJSONObject("data").getJSONObject("Page");
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
