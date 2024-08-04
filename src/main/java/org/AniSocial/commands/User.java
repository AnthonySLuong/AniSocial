package org.AniSocial.commands;

import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.AniSocial.interfaces.Command;
import org.AniSocial.util.AniList.AniListQueryType;
import org.AniSocial.util.DBHandler;
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
    public void executeCommandAutoComplete() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void executeSlashCommand() throws SQLException {
        if (this.slashEvent == null) {
            throw new IllegalStateException("SlashCommand is null");
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
    public void executeButtonInteraction() throws SQLException {
        if (this.buttonEvent == null) {
            throw new IllegalStateException("ButtonEvent is null");
        }

        MessageEditBuilder editData = MessageEditBuilder
                .fromMessage(this.buttonEvent.getMessage())
                .setComponents();

        if (this.slashEvent.getSubcommandName().equalsIgnoreCase("add") &&
                this.buttonEvent.getComponent().getLabel().equalsIgnoreCase("add")) {
            DBHandler db = DBHandler.getInstance();
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

    private void addUser() throws SQLException {
        MessageCreateBuilder msg = new MessageCreateBuilder()
                .setSuppressedNotifications(true);

        DBHandler db = DBHandler.getInstance();
        if (db.containChannelID(this.slashEvent.getChannelIdLong())) {
            String user = Objects.requireNonNull(this.slashEvent.getOption("user")).getAsString();
            JSONObject variable = new JSONObject();
            variable.put("user", user);

            JSONObject data;
            do {
                data = AniListQueryHandler.query(AniListQueryType.USER, variable);
            } while (data == null);

            this.user = data.getJSONObject("User");
            String name = this.user.getString("name");
            String siteUrl = this.user.getString("siteUrl");

            msg.addActionRow(
                    Button.success("add-user-" + this.slashEvent.getId(), "Add"),
                    Button.secondary("cancel-user-" + this.slashEvent.getId(), "Cancel"),
                    Button.link(siteUrl, name)
            );
            msg.setContent(
                    String.format("Do you want to add [%s](%s) user to <#%d>?", name, siteUrl, this.slashEvent.getChannelIdLong())
            );
        } else {
            msg.setContent("You must added users to notification channels");
        }

        this.slashEvent.reply(msg.build()).queue();
    }

    private void removeUser() throws SQLException {
        MessageCreateBuilder msg = new MessageCreateBuilder()
                .setSuppressedNotifications(true);

        String user = Objects.requireNonNull(this.slashEvent.getOption("user")).getAsString();

        DBHandler database = DBHandler.getInstance();
        if (database.removeUser(user, this.slashEvent.getChannelIdLong()) > 0) {
            msg.setContent(String.format("Removed %s user", user));
        } else {
            msg.setContent(String.format("%s user was not found", user));
        }

        this.slashEvent.reply(msg.build()).queue();
    }
}
