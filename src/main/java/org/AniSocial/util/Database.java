package org.AniSocial.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Database {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);
    private static final String addChannelQuery = "INSERT INTO channels VALUES (?, ?, ?, ?, NOW(), ?)";
    private static final String deleteChannelQuery = "DELETE FROM channels WHERE channel_id = ?";
    private static Database database = null;

    private Connection con;
    private String url, username, password;

    public Database init(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        return this;
    }

    public Database connect() throws SQLException {
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

    public static synchronized Database getInstance() throws SQLException {
        if (database == null) {
            database = new Database();
        }
        return database;
    }
}
