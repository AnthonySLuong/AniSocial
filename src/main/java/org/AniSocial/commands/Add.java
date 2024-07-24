package org.AniSocial.commands;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.AniSocial.interfaces.CommandInterface;
import org.AniSocial.subcommands.Channel;
import org.AniSocial.subcommands.User;

@NoArgsConstructor()
public class Add implements CommandInterface {

    @Override
    public void autoComplete(@NonNull CommandAutoCompleteInteractionEvent event) {
        return;
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) {
        switch (event.getSubcommandName().toLowerCase()) {
            case "channel":
                if (event.getMember().hasPermission(Permission.ADMINISTRATOR, Permission.MANAGE_CHANNEL)) {
                    Channel.getInstance().execute(event);
                } else {
                    event.reply("You do not have permission to use this command!").setEphemeral(true).queue();
                }
                break;

            case "user":
                User.getInstance().execute(event);
                break;
        }
    }

    @NonNull
    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("add", "Adds")
                .addSubcommands(
                        Channel.getInstance().getSubcommandData(),
                        User.getInstance().getSubcommandData()
                )
                .setGuildOnly(true);
    }
}
