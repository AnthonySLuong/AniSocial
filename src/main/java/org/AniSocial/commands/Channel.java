package org.AniSocial.commands;

import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.AniSocial.interfaces.Command;
import org.AniSocial.util.DatabaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
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
    public void executeSlashCommand() {
        if (this.slashEvent == null) {
            throw new IllegalStateException("slashCommand is null");
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
    public void executeButtonInteraction() {
        if (this.buttonEvent == null) {
            throw new IllegalStateException("buttonEvent is null");
        }

        MessageEditBuilder editData = MessageEditBuilder
                .fromMessage(this.buttonEvent.getMessage())
                .setComponents();
        try {
            DatabaseHandler database = DatabaseHandler.getInstance();
            if (this.slashEvent.getSubcommandName().equalsIgnoreCase("remove") &&
                this.buttonEvent.getComponent().getLabel().equalsIgnoreCase("yes")) {
                database.removeChannelId(this.slashEvent.getChannelIdLong());
                editData.setContent(
                        String.format("Removed <#%s> as a notification channel", this.buttonEvent.getChannelId())
                );
            } else {
                editData.setContent(
                        String.format("Did not remove <#%s> as a notification channel", this.buttonEvent.getChannelId())
                );
            }
        } catch (SQLException e) {
            editData.setContent(
                    String.format("Could not remove <#%s> as a notification channel", this.buttonEvent.getChannelId())
            );
            LOGGER.error(e.getLocalizedMessage(), e);
        }

        this.buttonEvent.editMessage(editData.build()).queue();
    }

    // ============================
    // Private Helper Methods
    // ============================

    private void addChannel() {
        MessageCreateBuilder msg = new MessageCreateBuilder()
                .setSuppressedNotifications(true)
                .setContent("Invalid Command");

        boolean suppress = this.slashEvent.getOption("suppress") != null
                ? this.slashEvent.getOption("suppress").getAsBoolean()
                : false; // default

        try {
            DatabaseHandler database = DatabaseHandler.getInstance();
            if (database.containChannelId(this.slashEvent.getChannelIdLong())) {
                throw new SQLException("Channel Id already exist", "23505");
            }

            database.addChannelId(
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

            LOGGER.info("Added {} ({}) as a notification channel", this.slashEvent.getChannel().getName(), this.slashEvent.getChannelId());
        } catch (SQLException e) {
            if (e.getSQLState().equalsIgnoreCase("23505")) {
                msg.setContent(
                        String.format("<#%s> already added as a notification channel", this.slashEvent.getChannelId())
                );
            } else {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }

        this.slashEvent.reply(msg.build()).queue();
    }

    private void removeChannel() {
        //TODO: Add embedd paging of users
        MessageCreateBuilder msg = new MessageCreateBuilder()
                .setSuppressedNotifications(true)
                .setContent("Invalid Command");

        List<String> list;
        try {
            DatabaseHandler databaseHandler = DatabaseHandler.getInstance();
            list = databaseHandler.listofUserIds(this.slashEvent.getChannelIdLong());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (list != null) {
            msg.setContent(
                    String.format("Are you sure you want to remove <#%s> as a notification channel?\n" +
                                    "%d user(s) will be remove from this channel too",
                            this.slashEvent.getChannelId(),
                            list.size()
                    )
            );
        }

        msg.addActionRow(
                Button.success("yes-" + this.slashEvent.getId(), "Yes"),
                Button.danger("no-" + this.slashEvent.getId(), "No")
        );

        this.slashEvent.reply(msg.build()).queue();
    }
}