package org.AniSocial;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.AniSocial.util.Database;

import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws SQLException {
        String token = System.getenv("TOKEN");
        String url = System.getenv("URL");
        String username = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");

        if (token == null || url == null || username == null || password == null) {
            throw new RuntimeException("Missing required environment variables");
        }

        if (!Database.getInstance().init(url, username, password).connect().isValid()) {
            throw new SQLException("Couldn't connect to database");
        }

        JDA api = JDABuilder.createDefault(token)
                .addEventListeners(new AniSocial())
                .build();
    }
}
