package org.AniSocial.subcommands;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.AniSocial.interfaces.SubCommandInterface;
import org.AniSocial.util.DatabaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Channel implements SubCommandInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(Channel.class);
    private static Channel channel = null;

    @Override
    public void onCommandAutoCompleteInteraction(@NonNull CommandAutoCompleteInteractionEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent event) throws NullPointerException {
        event.deferReply(true).queue(hook -> {
            EmbedBuilder msg = new EmbedBuilder()
                    .setDescription("Bot could not process command");

            // Required Options, never null
            TextChannel channel = Objects.requireNonNull(event.getInteraction().getOption("channel")).getAsChannel().asTextChannel();
            try {
                DatabaseHandler databaseHandler = DatabaseHandler.getInstance();
                switch (event.getInteraction().getName().toLowerCase()) {
                    case "add":
                        if (databaseHandler.addChannelId(event) > 0) {
                            msg.setDescription(String.format("Added <#%s> as activity channel", channel.getIdLong()));
                            LOGGER.info("Added {} ({}) as activity channel", channel.getName(), channel.getIdLong());
                        }
                        break;

                    case "remove":
                        if (databaseHandler.removeChannelId(event.getChannelIdLong()) > 0) {
                            msg.setDescription(String.format("Removed <#%s> as activity channel", channel.getIdLong()));
                            LOGGER.info("Removed {} ({}) as activity channel", channel.getName(), channel.getIdLong());
                        } else {
                            msg.setDescription(String.format("<#%s> was never added as activity channel", channel.getIdLong()));
                        }
                        break;
                }
            } catch (SQLException e) {
                if (e.getSQLState().equalsIgnoreCase("23505")) {
                    msg.setDescription(String.format("<#%s> already added as activity channel", channel.getIdLong()));
                } else {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn(e.getLocalizedMessage(), e);
                    }
                }
            }
            hook.editOriginalEmbeds(msg.build()).queue();
        });
    }

    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @NonNull
    @Override
    public SubcommandData getSubcommandData() {
        return new SubcommandData("channel", "Add/Remove text channel where the bot can send updates")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Text Channel", true)
                        .setChannelTypes(ChannelType.TEXT));
    }

    @NonNull
    synchronized public static Channel getInstance() {
        if (channel == null) {
            channel = new Channel();
        }
        return channel;
    }
}