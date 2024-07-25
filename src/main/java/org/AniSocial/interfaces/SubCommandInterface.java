package org.AniSocial.interfaces;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public interface SubCommandInterface {
    void onCommandAutoCompleteInteraction(@NonNull CommandAutoCompleteInteractionEvent event) throws Exception;
    void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent event) throws Exception;
    void onButtonInteraction(@NonNull ButtonInteractionEvent event) throws Exception;
    @NonNull SubcommandData getSubcommandData();
}
