package org.AniSocial.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatabaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHandler.class);
    private static final String addChannelQuery = "INSERT INTO channels VALUES (?, ?, ?, ?, NOW(), ?)";
    private static final String deleteChannelQuery = "DELETE FROM channels WHERE channel_id = ?";
    private static final String addUserQuery = "INSERT INTO users VALUES (?, ?, ?, ?, ?, NOW())";
    private static final String deleteUserQuery = "DELETE FROM users WHERE anilist_name = ?";
    private static DatabaseHandler databaseHandler = null;

    private Connection con;
    private String url, username, password;

    public DatabaseHandler init(String url, String username, String password) {
        if (url == null || url.isEmpty() ||
                username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        this.url = url;
        this.username = username;
        this.password = password;
        return this;
    }

    public DatabaseHandler connect() throws SQLException {
        if (this.url == null || this.username == null || this.password == null) {
            throw new SQLException("Database not initialized");
        }
        this.con = DriverManager.getConnection(url, username, password);
        LOGGER.info(String.format("Connected to database: %s", url));
        return this;
    }

    public boolean isValid() throws SQLException {
        return this.con.isValid(0);
    }

//    public int[] queryUser() throws SQLException {
//        try (PreparedStatement statement = this.con.prepareStatement("SELECT * FROM USERS")) {
//            ResultSet result = statement.executeQuery();
//
//        }
//    }

    public int addChannelId(SlashCommandInteractionEvent event) throws SQLException {
        try (PreparedStatement statement = this.con.prepareStatement(addChannelQuery)) {
            statement.setLong(1, event.getChannelIdLong());
            statement.setString(2, event.getChannel().getName());
            statement.setLong(3, event.getGuild().getIdLong());
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

    public int addUser(long id, String name, String siteUrl, SlashCommandInteractionEvent event) throws SQLException {
        try (PreparedStatement statement = this.con.prepareStatement(addUserQuery)) {
            statement.setLong(1, id);
            statement.setString(2, name.toLowerCase());
            statement.setString(3, siteUrl);
            statement.setLong(4, event.getChannelIdLong());
            statement.setLong(5, event.getInteraction().getUser().getIdLong());

            return statement.executeUpdate();
        }
    }

    public int removeUser(String user) throws SQLException {
        try (PreparedStatement statement = this.con.prepareStatement(deleteUserQuery)) {
            statement.setString(1, user.toLowerCase());
            return statement.executeUpdate();
        }
    }

    public static synchronized DatabaseHandler getInstance() throws SQLException {
        if (databaseHandler == null) {
            databaseHandler = new DatabaseHandler();
        }
        return databaseHandler;
    }
}
