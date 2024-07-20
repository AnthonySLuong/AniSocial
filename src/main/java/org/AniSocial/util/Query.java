package org.AniSocial.util;

import lombok.*;
import org.json.JSONObject;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Query {
    public static final String URL = "https://graphql.anilist.co";

    @Getter
    @RequiredArgsConstructor
    public enum QueryString {
        LIST("""
            query ($userids: [Int], $page: Int, time: Int) {
                Page(page: $page) {
                    pageInfo {
                        lastPage
                        }
                    activities(userId_in: $userids, type: MEDIA_LIST, sort: ID, createdAt_greater: time) {
                        ... on ListActivity {
                            media {
                                title {
                                    romaji
                                    english
                                    native
                                }
                                coverImage {
                                    medium
                                }
                                siteUrl
                                isAdult
                            }
                            user {
                                name
                                avatar {
                                    medium
                                }
                                siteUrl
                            }
                            status
                            progress
                            createdAt
                        }
                    }
                }
            }
            """.trim().replaceAll("([\\n\\t]| {2,})", " ")),
        USER("""
            query ($name) {
                User (search: $name) {
                    name
                    id
                    siteUrl
                }
            }
            """.trim().replaceAll("([\\n\\t]| {2,})", " "));

        private final String query;
    }

    @NonNull
    public static JSONObject toPayload(@NonNull QueryString query, @NonNull JSONObject variable) {
        JSONObject payload = new JSONObject();
        payload.put("query", query.getQuery());

        switch (query) {
            case LIST:
                if (!variable.has("$userids") && !variable.has("$page") && !variable.has("$time")) {
                    throw new IllegalArgumentException("variable must contain userids, page and time");
                }
                break;

            case USER:
                if (!variable.has("name")) {
                    throw new IllegalArgumentException("variable must contain name");
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported query type");
        }

        payload.put("variables", variable);
        return payload;
    }
}
