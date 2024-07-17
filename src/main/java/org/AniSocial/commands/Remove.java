package org.AniSocial.commands;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.AniSocial.interfaces.CommandInterface;
import org.AniSocial.subcommands.Channel;
import org.AniSocial.subcommands.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Remove implements CommandInterface {
    private static Remove remove = null;

    @Override
    public void autoComplete(CommandAutoCompleteInteractionEvent event) {
        return;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        switch (event.getSubcommandName().toLowerCase()) {
            case "channel":
                Channel.getInstance().execute(event);
                break;

            case "user":
                break;
        }
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("remove", "Remove")
                .addSubcommands(
                        Channel.getInstance().getSubcommandData(),
                        User.getInstance().getSubcommandData()
                );
    }

    synchronized public static Remove getInstance() {
        if (remove == null) {
            remove = new Remove();
        }
        return remove;
    }
}
