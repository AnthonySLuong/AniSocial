package org.AniSocial.interfaces;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.sql.SQLException;

public interface SubCommandInterface {
    void autoComplete(CommandAutoCompleteInteractionEvent event) throws Exception;
    void execute(SlashCommandInteractionEvent event) throws Exception;
    SubcommandData getSubcommandData();
}
