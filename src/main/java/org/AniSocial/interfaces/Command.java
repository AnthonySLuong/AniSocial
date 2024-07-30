package org.AniSocial.interfaces;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@Setter
@Accessors(chain = true)
@NoArgsConstructor
public abstract class Command {
    protected CommandAutoCompleteInteractionEvent autoCompleteEvent;
    protected SlashCommandInteractionEvent slashEvent;
    protected ButtonInteractionEvent buttonEvent;

    abstract public void executeCommandAutoComplete() throws Exception;
    abstract public void executeSlashCommand() throws Exception;
    abstract public void executeButtonInteraction() throws Exception;
}
