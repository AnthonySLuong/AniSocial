package org.AniSocial.util.AniList;

import lombok.*;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AniListQueryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AniListQueryHandler.class);
    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final String URL = "https://graphql.anilist.co";

    /**
     * GraphQL query to AniList
     * @param type Query Type (List, User)
     * @param variable Variable Payload
     * @return JSONObject of Response
     */
    public static JSONObject query(@NonNull AniListQueryType type, @NonNull JSONObject variable) {
        String payload = toPayload(type, variable).toString();
        RequestBody body = RequestBody.create(payload, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Sending Request of {}", payload);
        }

        try (Response response = CLIENT.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return new JSONObject(response.body().string()).getJSONObject("data");
            }
            LOGGER.warn("Received response but not valid: {}", response);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

        return null;
    }

    // ============================
    // Private Helper Methods
    // ============================

    /**
     * Convert
     * @param query Query Type (List, User)
     * @param variables Variable PayLoad
     * @return JSONObject of {query: *, variables: *}
     */
    @NonNull
    private static JSONObject toPayload(@NonNull AniListQueryType query, @NonNull JSONObject variables) {
        switch (query) {
            case LIST:
                if (!variables.has("userids") && !variables.has("page") && !variables.has("time")) {
                    throw new IllegalStateException("variable must contain userids, page and time");
                }
                break;

            case USER:
                if (!variables.has("user")) {
                    throw new IllegalStateException("variable must contain user");
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported query type");
        }

        JSONObject payload = new JSONObject();
        payload.put("query", query.getQuery());
        payload.put("variables", variables);
        return payload;
    }
}
