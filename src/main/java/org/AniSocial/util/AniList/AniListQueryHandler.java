package org.AniSocial.util.AniList;

import lombok.*;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AniListQueryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AniListQueryHandler.class);
    private static final String URL = "https://graphql.anilist.co";
    private static final OkHttpClient CLIENT = new OkHttpClient();

    public static JSONObject query(@NonNull AniListQueryType type, @NonNull JSONObject variable) {
        String payload = toPayload(type, variable).toString();
        RequestBody body = RequestBody.create(payload, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        LOGGER.info(String.format("Sending Request of %s", payload));
        try (Response response = CLIENT.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return new JSONObject(response.body().string()).getJSONObject("data");
            }
            LOGGER.warn(String.format("Received response but not valid: %s", response));
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

        return null;
    }

    @NonNull
    private static JSONObject toPayload(@NonNull AniListQueryType query, @NonNull JSONObject variable) {
        switch (query) {
            case LIST:
                if (!variable.has("userids") && !variable.has("page") && !variable.has("time")) {
                    throw new IllegalStateException("variable must contain userids, page and time");
                }
                break;

            case USER:
                if (!variable.has("user")) {
                    throw new IllegalStateException("variable must contain user");
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported query type");
        }

        JSONObject payload = new JSONObject();
        payload.put("query", query.getQuery());
        payload.put("variables", variable);
        return payload;
    }
}
