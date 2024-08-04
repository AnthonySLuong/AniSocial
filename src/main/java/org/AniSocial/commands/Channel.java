package org.AniSocial.commands;

import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.AniSocial.interfaces.Command;
import org.AniSocial.util.DBHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
public class Channel extends Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(Channel.class);

    @Override
    public void executeCommandAutoComplete() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void executeSlashCommand() throws SQLException {
        if (this.slashEvent == null) {
            throw new IllegalStateException("SlashCommand is null");
        }

        switch (Objects.requireNonNull(this.slashEvent.getSubcommandName()).toLowerCase()) {
            case "add":
                addChannel();
                break;

            case "remove":
                removeChannel();
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

        DBHandler database = DBHandler.getInstance();
        if (this.slashEvent.getSubcommandName().equalsIgnoreCase("remove") &&
                this.buttonEvent.getComponent().getLabel().equalsIgnoreCase("Remove")) {
            database.removeChannelID(this.slashEvent.getChannelIdLong());
            editData.setContent(
                    String.format("Removed <#%s> as a notification channel", this.buttonEvent.getChannelId())
            );
        } else {
            editData.setContent(
                    String.format("Cancelled the removal of <#%s>", this.buttonEvent.getChannelId())
            );
        }

        this.buttonEvent.editMessage(editData.build()).queue();
    }

    // ============================
    // Private Helper Methods
    // ============================

    private void addChannel() throws SQLException {
        MessageCreateBuilder msg = new MessageCreateBuilder()
                .setSuppressedNotifications(true);

        boolean suppress = this.slashEvent.getOption("suppress") != null
                ? this.slashEvent.getOption("suppress").getAsBoolean()
                : false; // default

        try {
            DBHandler db = DBHandler.getInstance();
            db.addChannelId(
                    this.slashEvent.getChannelIdLong(),
                    this.slashEvent.getChannel().getName(),
                    Objects.requireNonNull(this.slashEvent.getGuild()).getIdLong(),
                    Objects.requireNonNull(this.slashEvent.getGuild()).getName(),
                    this.slashEvent.getUser().getIdLong(),
                    suppress
            );

            msg.setContent(
                    String.format("Added <#%s> as a notification channel", this.slashEvent.getChannelId())
            );
        } catch (SQLException e) {
            if (e.getSQLState().equalsIgnoreCase("23505")) {
                msg.setContent(
                        String.format("<#%s> already added as a notification channel", this.slashEvent.getChannelId())
                );
            } else {
                throw e;
            }
        }

        this.slashEvent.reply(msg.build()).queue();
    }

    private void removeChannel() throws SQLException {
        // TODO: Add embed paging of users
        DBHandler db = DBHandler.getInstance();
        MessageCreateBuilder msg = new MessageCreateBuilder()
                .setSuppressedNotifications(true);

        if (!db.containChannelID(this.slashEvent.getChannelIdLong())) {
            msg.setContent(
                    String.format("<#%s> is not a notification channel", this.slashEvent.getChannelId())
            );
        } else {
            List<String> users = new ArrayList<>(db.listofUserIds(this.slashEvent.getChannelIdLong()));

            msg.setContent(
                    String.format("Are you sure you want to remove <#%s> as a notification channel?\n" +
                                    "%d user(s) will be remove from this channel too",
                            this.slashEvent.getChannelId(),
                            users.size()
                    )
            );

            msg.addActionRow(
                    Button.danger("remove-channel-" + this.slashEvent.getId(), "Remove"),
                    Button.secondary("cancel-channel-" + this.slashEvent.getId(), "Cancel")
            );
        }

        this.slashEvent.reply(msg.build()).queue();
    }
}