package org.AniSocial;

import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.AniSocial.commands.Add;
import org.AniSocial.commands.Remove;
import org.AniSocial.interfaces.CommandInterface;
import org.AniSocial.util.AniList.AniListRunner;
import org.AniSocial.util.DatabaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws SQLException, InterruptedException {
        @NonNull String token = System.getenv("TOKEN");
        @NonNull String url = System.getenv("URL");
        @NonNull String username = System.getenv("USERNAME");
        @NonNull String password = System.getenv("PASSWORD");
        boolean global = Boolean.parseBoolean(System.getenv("GLOBAL"));
        String guildid = System.getenv("GUILDID");

        if (!DatabaseHandler.getInstance().init(url, username, password).connect().isValid()) {
            throw new SQLException("Couldn't connect to database");
        }

        Map<String, CommandInterface> commands = new HashMap<>();
        commands.put(Add.class.getSimpleName().toLowerCase(), new Add());
        commands.put(Remove.class.getSimpleName().toLowerCase(), new Remove());

        List<SlashCommandData> commandsData = commands.values().stream()
                .map(CommandInterface::getSlashCommandData)
                .collect(Collectors.toCollection(ArrayList::new));

        JDA api = JDABuilder.createDefault(token)
                .addEventListeners(new Listener(commands))
                .build()
                .awaitReady();

        if (guildid != null) {
            Guild guild = api.getGuildById(guildid);
            if (guild != null) {
                LOGGER.info("Updating guild Commands {}", guild.getName());
                guild.updateCommands().addCommands(commandsData).queue();
            } else {
                LOGGER.warn("Could not find guild {}}", guildid);
            }
        }

        if (global) {
            api.updateCommands().addCommands(commandsData).queue();
        }

        AniListRunner.getInstance().run(api);
    }
}
