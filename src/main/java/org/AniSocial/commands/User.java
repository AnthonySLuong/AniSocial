package org.AniSocial.commands;

import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.AniSocial.interfaces.Command;
import org.AniSocial.util.AniList.AniListQueryType;
import org.AniSocial.util.DatabaseHandler;
import org.AniSocial.util.AniList.AniListQueryHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Objects;

public class User extends Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(User.class);
    private JSONObject user;

    @Override
    public void executeCommandAutoComplete() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void executeSlashCommand() {
        if (this.slashEvent == null) {
            throw new IllegalStateException("slashCommand is null");
        }

        switch (Objects.requireNonNull(this.slashEvent.getSubcommandName()).toLowerCase()) {
            case "add":
                addUser();
                break;

            case "remove":
                removeUser();
                break;
        }
    }

    @Override
    public void executeButtonInteraction() throws Exception {
        if (this.buttonEvent == null) {
            throw new IllegalStateException("buttonEvent is null");
        }

        MessageEditBuilder editData = MessageEditBuilder
                .fromMessage(this.buttonEvent.getMessage())
                .setComponents();

        if (this.slashEvent.getSubcommandName().equalsIgnoreCase("add") &&
                this.buttonEvent.getComponent().getLabel().equalsIgnoreCase("yes")) {
            try {
                DatabaseHandler db = DatabaseHandler.getInstance();
                db.addUser(
                        this.user.getLong("id"),
                        this.user.getString("name"),
                        this.user.getString("siteUrl"),
                        this.slashEvent.getChannelIdLong(),
                        this.slashEvent.getUser().getIdLong()
                );
                editData.setContent(
                        String.format("Added [%s](%s) user to <#%d>",
                                this.user.getString("name"),
                                this.user.getString("siteUrl"),
                                this.slashEvent.getChannelIdLong()
                        )
                );
            } catch (SQLException e) {
                editData.setContent("Bot could not process button");
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        } else {
            editData.setContent(
                    String.format("%s user was not added", this.user.getString("name"))
            );
        }

        this.buttonEvent.editMessage(editData.build()).queue();
    }

    // ============================
    // Private Helper Methods
    // ============================

    private void addUser() {
        String user = Objects.requireNonNull(this.slashEvent.getOption("user")).getAsString();
        JSONObject variable = new JSONObject();
        variable.put("user", user);

        this.user = Objects.requireNonNull(AniListQueryHandler.query(AniListQueryType.USER, variable));
        this.user = this.user.getJSONObject("User");

        String name = this.user.getString("name");
        String siteUrl = this.user.getString("siteUrl");

        MessageCreateBuilder msg = new MessageCreateBuilder()
                .setSuppressedNotifications(true)
                .addActionRow(
                        Button.success("yes-" + this.slashEvent.getId(), "Yes"),
                        Button.danger("no-" + this.slashEvent.getId(), "No")
                )
                .setContent(
                        String.format("Do you want to add [%s](%s) user to <#%d>?", name, siteUrl, this.slashEvent.getChannelIdLong())
                );

        this.slashEvent.reply(msg.build()).queue();
    }

    private void removeUser() {
        MessageCreateBuilder msg = new MessageCreateBuilder()
                .setSuppressedNotifications(true)
                .setContent("Could not process command or find user");

        String user = Objects.requireNonNull(this.slashEvent.getOption("user")).getAsString();

        try {
            DatabaseHandler database = DatabaseHandler.getInstance();
            if (database.removeUser(user, this.slashEvent.getChannelIdLong()) > 0) {
                msg.setContent(String.format("Removed %s user", user));
            } else {
                msg.setContent(String.format("%s user was not found", user));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        this.slashEvent.reply(msg.build()).queue();
    }
}
