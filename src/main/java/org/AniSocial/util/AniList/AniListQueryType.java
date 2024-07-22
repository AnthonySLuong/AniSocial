package org.AniSocial.util.AniList;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AniListQueryType {
    LIST("""
            query ($userids: [Int], $page: Int, $time: Int) {
                Page(page: $page) {
                    pageInfo {
                        lastPage
                        }
                    activities(userId_in: $userids, type: MEDIA_LIST, sort: ID, createdAt_greater: $time) {
                        ... on ListActivity {
                            status
                            progress
                            createdAt
                            media {
                                siteUrl
                                isAdult
                                title {
                                    romaji
                                    english
                                    native
                                }
                                coverImage {
                                    medium
                                }
                            }
                            user {
                                name
                                id
                                siteUrl
                                avatar {
                                    medium
                                }
                            }
                        }
                    }
                }
            }
            """.trim().replaceAll("([\\n\\t]| {2,})", " ")),
    USER("""
            query ($user: String) {
                User (search: $user) {
                    name
                    id
                    siteUrl
                }
            }
            """.trim().replaceAll("([\\n\\t]| {2,})", " "));

    @NonNull private final String query;
}