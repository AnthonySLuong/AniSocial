package org.AniSocial.interfaces;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public interface SubCommandInterface {
    void autoComplete(@NonNull CommandAutoCompleteInteractionEvent event) throws Exception;
    void execute(@NonNull SlashCommandInteractionEvent event) throws Exception;
    @NonNull SubcommandData getSubcommandData();
}
