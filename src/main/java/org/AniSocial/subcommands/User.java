package org.AniSocial.subcommands;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.AniSocial.interfaces.SubCommandInterface;
import org.AniSocial.util.AniList.AniListQueryType;
import org.AniSocial.util.DatabaseHandler;
import org.AniSocial.util.AniList.AniListQueryHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class User implements SubCommandInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(User.class);
    private static User user = null;

    @Override
    public void autoComplete(@NonNull CommandAutoCompleteInteractionEvent event) {

    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) {
        event.deferReply(true).complete();

        // Require event
        String msg = "Could not process command or find user";
        String user = Objects.requireNonNull(event.getOption("user")).getAsString();

        try {
            DatabaseHandler database = DatabaseHandler.getInstance();
            switch (event.getInteraction().getName().toLowerCase()) {
                case "add":
                    JSONObject variable = new JSONObject();
                    variable.put("user", user);

                    JSONObject userData = Objects.requireNonNull(AniListQueryHandler.query(AniListQueryType.USER, variable)).getJSONObject("User");
                    long userId = userData.getLong("id");
                    String name = userData.getString("name");
                    String siteurl = userData.getString("siteUrl");
                    if (database.addUser(userId, name, siteurl, event) > 0) {
                        msg = String.format("Added [%s](%s) user to <#%d>", user, siteurl, event.getChannelIdLong());
                    } else {
                        msg = String.format("Failed to add [%s](%s) user to <#%d>", user, siteurl, event.getChannelIdLong());
                    }
                    break;

                case "remove":
                    if (database.removeUser(user, event.getChannelIdLong()) > 0) {
                        msg = String.format("Removed %s user", user);
                    } else {
                        msg = String.format("%s user was not found", user);
                    }
                    break;
            }
        } catch (SQLException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }

        event.getHook().editOriginal(msg).queue();
    }

    @NonNull
    @Override
    public SubcommandData getSubcommandData() {
        return new SubcommandData("user", "user")
                .addOption(OptionType.STRING, "user", "AniList Username or Anilist Profile Link", true);
    }

    @NonNull
    synchronized public static User getInstance() {
        if (user == null) {
            user = new User();
        }
        return user;
    }
}
