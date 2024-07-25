package org.AniSocial.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatabaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHandler.class);
    private static final String addChannelQuery = "INSERT INTO channels VALUES (?, ?, ?, ?, NOW(), ?)";
    private static final String deleteChannelQuery = "DELETE FROM channels WHERE channel_id = ?";
    private static final String addUserQuery = "INSERT INTO users VALUES (?, ?, ?, ?, ?, NOW())";
    private static final String deleteUserQuery = "DELETE FROM users WHERE anilist_name = ? AND channel_id = ?";
    private static final String anilistIdQuery = "SELECT DISTINCT anilist_id FROM USERS";
    private static final String channelIdQuery = "SELECT channel_id FROM USERS WHERE anilist_id = ?";

    private static DatabaseHandler databaseHandler = null;

    private Connection con;
    private String url, username, password;

    @NonNull
    public DatabaseHandler init(@NonNull String url, @NonNull String username, @NonNull String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        return this;
    }

    @NonNull
    public DatabaseHandler connect() throws SQLException {
        if (this.url == null || this.username == null || this.password == null) {
            throw new SQLException("Database not initialized");
        }
        this.con = DriverManager.getConnection(url, username, password);
        LOGGER.info("Connected to database: {}", url);
        return this;
    }

    public boolean isValid() throws SQLException {
        if (this.con == null) {
            return false;
        }
        return this.con.isValid(0);
    }

    @NonNull
    public List<Long> queryUser() throws SQLException {
        List<Long> id = new ArrayList<>();
        try (PreparedStatement statement = this.con.prepareStatement(anilistIdQuery)) {
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                id.add(result.getLong(1));
            }
        }

        return id;
    }

    @NonNull
    public List<Long> queryChannel(long userId) throws SQLException {
        List<Long> channelId = new ArrayList<>();

        try (PreparedStatement statement = this.con.prepareStatement(channelIdQuery)) {
            statement.setLong(1, userId);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                channelId.add(result.getLong(1));
            }
        }
        return channelId;
    }

    public int addChannelId(@NonNull SlashCommandInteractionEvent event) throws SQLException {
        try (PreparedStatement statement = this.con.prepareStatement(addChannelQuery)) {
            statement.setLong(1, event.getChannelIdLong());
            statement.setString(2, event.getChannel().getName());
            statement.setLong(3, Objects.requireNonNull(event.getGuild()).getIdLong());
            statement.setString(4, event.getGuild().getName());
            statement.setLong(5, event.getInteraction().getUser().getIdLong());
            return statement.executeUpdate();
        }
    }

    public int removeChannelId(long channelId) throws SQLException {
        try (PreparedStatement statement = this.con.prepareStatement(deleteChannelQuery)) {
            statement.setLong(1, channelId);
            return statement.executeUpdate();
        }
    }

    public int addUser(long id, @NonNull String name, @NonNull String siteUrl, @NonNull SlashCommandInteractionEvent event) throws SQLException {
        try (PreparedStatement statement = this.con.prepareStatement(addUserQuery)) {
            statement.setLong(1, id);
            statement.setString(2, name.toLowerCase());
            statement.setString(3, siteUrl);
            statement.setLong(4, event.getChannelIdLong());
            statement.setLong(5, event.getInteraction().getUser().getIdLong());

            return statement.executeUpdate();
        }
    }

    public int removeUser(@NonNull String user, long channelId) throws SQLException {
        try (PreparedStatement statement = this.con.prepareStatement(deleteUserQuery)) {
            statement.setString(1, user.toLowerCase());
            statement.setLong(2, channelId);
            return statement.executeUpdate();
        }
    }

    @NonNull
    public static synchronized DatabaseHandler getInstance() throws SQLException {
        if (databaseHandler == null) {
            databaseHandler = new DatabaseHandler();
        }
        return databaseHandler;
    }
}
