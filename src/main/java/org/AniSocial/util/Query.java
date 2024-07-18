package org.AniSocial.util;

import org.json.JSONObject;

public class Query {
    public static final String URL = "https://graphql.anilist.co";
    public static final String UPDATEQUERY =
        """
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
        """.trim().replaceAll("([\\n\\t]| {2,})", " ");

    public static final String USERQUERY = """
    query ($name) {
        User (search: $name) {
            name
            id
            siteUrl
        }
    }
    """.trim().replaceAll("([\\n\\t]| {2,})", " ");

    public static JSONObject toPayload(JSONObject variable) {
        JSONObject payload = new JSONObject();
        payload.put("query", UPDATEQUERY);
        payload.put("variables", variable);
        return payload;
    }
}
