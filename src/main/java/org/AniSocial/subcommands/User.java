package org.AniSocial.subcommands;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import okhttp3.*;
import org.AniSocial.interfaces.SubCommandInterface;
import org.AniSocial.util.AniList.AniListQueryType;
import org.AniSocial.util.DatabaseHandler;
import org.AniSocial.util.AniList.AniListQueryHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class User implements SubCommandInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(User.class);
    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static User user = null;

    @Override
    public void autoComplete(CommandAutoCompleteInteractionEvent event) {
        return;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply(true).complete();

        // Require event
        String msg = "Could not process command or find user";
        String user = event.getOption("user").getAsString();

        try {
            DatabaseHandler database = DatabaseHandler.getInstance();
            switch (event.getInteraction().getName().toLowerCase()) {
                case "add":
                    JSONObject variable = new JSONObject();
                    variable.put("user", user);

                    JSONObject userData = AniListQueryHandler.query(AniListQueryType.USER, variable).getJSONObject("User");
                    long userId = userData.getLong("id");
                    String name = userData.getString("name");
                    String siteurl = userData.getString("siteUrl");
                    if (database.addUser(userId, name, siteurl, event) > 0) {
                        msg = msg = String.format("Added [%s](%s) user", user, siteurl);
                    } else {
                        msg = msg = String.format("Failed to add [%s](%s) user", user, siteurl);
                    }
                    break;

                case "remove":
                    if (database.removeUser(user) > 0) {
                        msg = String.format("Removed %s user(s)", user);
                    } else {
                        msg = String.format("%s user(s) was not found", user);
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

    @Override
    public SubcommandData getSubcommandData() {
        return new SubcommandData("user", "user")
                .addOption(OptionType.STRING, "user", "user", true);
    }

    synchronized public static User getInstance() {
        if (user == null) {
            user = new User();
        }
        return user;
    }
}
