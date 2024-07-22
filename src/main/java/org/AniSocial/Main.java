package org.AniSocial;

import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.AniSocial.commands.Add;
import org.AniSocial.commands.Remove;
import org.AniSocial.interfaces.CommandInterface;
import org.AniSocial.util.AniList.AniListRunner;
import org.AniSocial.util.DatabaseHandler;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws SQLException, InterruptedException {
        @NonNull String token = System.getenv("TOKEN");
        @NonNull String url = System.getenv("URL");
        @NonNull String username = System.getenv("USERNAME");
        @NonNull String password = System.getenv("PASSWORD");
        String guildid = System.getenv("GUILDID");

        if (!DatabaseHandler.getInstance().init(url, username, password).connect().isValid()) {
            throw new SQLException("Couldn't connect to database");
        }

        Map<String, Class<? extends CommandInterface>> commands = new HashMap<>();
        commands.put(Add.class.getSimpleName().toLowerCase(), Add.class);
        commands.put(Remove.class.getSimpleName().toLowerCase(), Remove.class);

        List<SlashCommandData> commandsData = commands.values().stream()
                .map(cmd -> {
                    try {
                        CommandInterface command = (CommandInterface) cmd.getMethod("getInstance").invoke(null);
                        return command.getSlashCommandData();

                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException("Error creating instance or calling method", e);
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));

        JDA api = JDABuilder.createDefault(token)
                .addEventListeners(new Listener(commands))
                .build()
                .awaitReady();

        if (guildid != null) {
            api.getGuildById(guildid).updateCommands().addCommands(commandsData).queue();
        }

        AniListRunner.getInstance().run(api);
    }
}
