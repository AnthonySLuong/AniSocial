package org.AniSocial.util;

import org.json.JSONObject;

public class Query {
    public static final String URL = "https://graphql.anilist.co";
    public static final String QUERY =
        """
        query ($ids: [Int], $pages: Int, $seconds: Int) {
             Page(page: $pages) {
                 pageInfo {
                     lastPage
                 }
                 activities(userId_in: $ids, type: MEDIA_LIST, sort: ID_DESC, createdAt_greater: $seconds) {
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
        """.trim().replaceAll("([\\n\\t]| {2,})", " ");

    public static JSONObject toPayload(Variable variable) {
        JSONObject payload = new JSONObject();
        payload.put("query", QUERY);
        payload.put("variables", variable.toJson());
        return payload;
    }
}
