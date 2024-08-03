package org.AniSocial;

import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import org.AniSocial.util.CommandData;
import org.AniSocial.util.DatabaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws SQLException, InterruptedException {
        @NonNull String token = System.getenv("TOKEN");
        @NonNull String url = System.getenv("URL");
        @NonNull String username = System.getenv("USERNAME");
        @NonNull String password = System.getenv("PASSWORD");
        boolean global = Boolean.parseBoolean(System.getenv("GLOBAL"));
        String guildid = System.getenv("GUILDID");

        DatabaseHandler.getInstance().init(url.trim(), username.trim(), password.trim()).connect();

        JDA api = JDABuilder.createDefault(token.trim())
                .setAutoReconnect(true)
                .setRequestTimeoutRetry(true)
                .addEventListeners(new Listener())
                .build()
                .awaitReady();

        if (guildid != null) {
            Guild guild = api.getGuildById(guildid.trim());
            if (guild != null) {
                guild.updateCommands().addCommands(CommandData.getCommandData()).queue();
                LOGGER.info("Updated guild Commands {}", guild.getName());
            } else {
                LOGGER.warn("Could not find guild {}", guildid);
            }
        }

        if (global) {
            api.updateCommands().addCommands(CommandData.getCommandData()).queue();
            LOGGER.info("Updated global Commands");
        }
    }
}
