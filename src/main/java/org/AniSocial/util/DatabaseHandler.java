package org.AniSocial.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatabaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHandler.class);
    private static DatabaseHandler databaseHandler = null;

    private Connection con;
    private String url, username, password;

    /**
     * Set database instance url, username and password
     * @param url Database URL
     * @param username Database username
     * @param password Database password
     * @return instance of itself
     */
    @NonNull
    public DatabaseHandler init(@NonNull String url, @NonNull String username, @NonNull String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * Connects to Databases
     * @return instance of itself
     * @throws SQLException If url, username, and password not initialize first
     */
    @NonNull
    public DatabaseHandler connect() throws SQLException {
        if (this.url == null || this.username == null || this.password == null) {
            throw new SQLException("Database not initialized");
        }
        this.con = DriverManager.getConnection(url, username, password);
        LOGGER.info("Connected to database: {}", url);
        return this;
    }

    /**
     * Query a List of UserIds to check for updates
     * @return List of UserIds
     * @throws SQLException Any SQLException
     */
    @NonNull
    public List<Long> listofUserIds() throws SQLException {
        List<Long> id = new ArrayList<>();
        try (PreparedStatement statement = this.con.prepareStatement("SELECT DISTINCT anilist_id FROM USERS")) {
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                id.add(result.getLong(1));
            }
        }

        return id;
    }

    /**
     * Query a List of UserIds that belong to channel ID
     * @param channelId
     * @return List of UserIds
     * @throws SQLException Any SQLException
     */
    @NonNull
    public List<String> listofUserIds(long channelId) throws SQLException {
        List<String> id = new ArrayList<>();
        try (PreparedStatement statement = this.con.prepareStatement("SELECT anilist_name FROM USERS WHERE channel_id = ?")) {
            statement.setLong(1, channelId);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                id.add(result.getString(1));
            }
        }

        return id;
    }

    /**
     * Query a list of channel Ids that the userId belong in
     * @param userId UserId
     * @return List of channel Ids
     * @throws SQLException Any SQLException
     */
    @NonNull
    public List<Long> channelIdsOfUser(long userId) throws SQLException {
        List<Long> channelId = new ArrayList<>();

        try (PreparedStatement statement = this.con.prepareStatement("SELECT channel_id FROM USERS WHERE anilist_id = ?")) {
            statement.setLong(1, userId);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                channelId.add(result.getLong(1));
            }
        }
        return channelId;
    }

    /**
     * Check if channel ID is already added to database
     * @param channelId channel id
     * @return True if exist otherwise false
     * @throws SQLException Any SQLException
     */
    public boolean containChannelId(long channelId) throws SQLException {
        try (PreparedStatement statement = this.con.prepareStatement("SELECT COUNT(channel_id) FROM channels WHERE channel_id = ?")) {
            statement.setLong(1, channelId);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Add Channel Meta Data to Database
     * @param channelId Channel ID
     * @param channelName Channel Name
     * @param guildId Guild ID
     * @param guildName Guild Name
     * @param userId User ID
     * @param suppress Suppress Message sent to the channel
     * @return Number of row affected
     * @throws SQLException Any SQLException
     */
    public int addChannelId(long channelId, String channelName, long guildId, String guildName, long userId,
                            boolean suppress) throws SQLException {

        try (PreparedStatement statement = this.con.prepareStatement(
                "INSERT INTO channels VALUES (?, ?, ?, ?, NOW(), ?, ?)")) {
            statement.setLong(1, channelId);
            statement.setString(2, channelName);
            statement.setLong(3, guildId);
            statement.setString(4, guildName);
            statement.setLong(5, userId);
            statement.setBoolean(6, suppress);
            return statement.executeUpdate();
        }
    }

    public int removeChannelId(long channelId) throws SQLException {
        try (PreparedStatement statement = this.con.prepareStatement("DELETE FROM users WHERE channel_id = ?")) {
            statement.setLong(1, channelId);
            statement.executeUpdate();
        }

        try (PreparedStatement statement = this.con.prepareStatement("DELETE FROM channels WHERE channel_id = ?")) {
            statement.setLong(1, channelId);
            return statement.executeUpdate();
        }
    }

    /**
     * Add User metadata associated with channel ID
     * @param id AniList ID
     * @param name AniList Name
     * @param siteUrl AniList Site URL
     * @param channelID Channel ID
     * @param addedBy User ID
     * @return Number of row affected
     * @throws SQLException
     */
    public int addUser(long id, @NonNull String name, @NonNull String siteUrl, long channelID, long addedBy) throws SQLException {
        try (PreparedStatement statement = this.con.prepareStatement("INSERT INTO users VALUES (?, ?, ?, ?, ?, NOW())")) {
            statement.setLong(1, id);
            statement.setString(2, name.toLowerCase());
            statement.setString(3, siteUrl);
            statement.setLong(4, channelID);
            statement.setLong(5, addedBy);

            return statement.executeUpdate();
        }
    }

    public int removeUser(@NonNull String user, long channelId) throws SQLException {
        try (PreparedStatement statement = this.con.prepareStatement("DELETE FROM users WHERE anilist_name = ? AND channel_id = ?")) {
            statement.setString(1, user.toLowerCase());
            statement.setLong(2, channelId);
            return statement.executeUpdate();
        }
    }

    @NonNull
    public static synchronized DatabaseHandler getInstance() throws SQLException {
        if (databaseHandler == null) {
            databaseHandler = new DatabaseHandler();
        } else if (databaseHandler.con.isClosed()) {
            databaseHandler.connect();
        }
        return databaseHandler;
    }
}
