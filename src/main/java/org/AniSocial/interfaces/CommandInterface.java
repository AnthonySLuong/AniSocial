package org.AniSocial.interfaces;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface CommandInterface {
    void autoComplete(CommandAutoCompleteInteractionEvent event);
    void execute(SlashCommandInteractionEvent event);
    SlashCommandData getSlashCommandData();
}
