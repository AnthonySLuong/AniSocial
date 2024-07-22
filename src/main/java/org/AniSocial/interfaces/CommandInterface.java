package org.AniSocial.interfaces;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface CommandInterface {
    void autoComplete(@NonNull CommandAutoCompleteInteractionEvent event) throws Exception;
    void execute(@NonNull SlashCommandInteractionEvent event) throws Exception;
    @NonNull SlashCommandData getSlashCommandData();
}
