package org.AniSocial.util;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;

public class CommandData {
    public static List<SlashCommandData> getCommandData() {
        return new ArrayList<SlashCommandData>(List.of(
                Commands.slash("channel", "Add or remove text channels where the bot is allowed to send updates")
                        .addSubcommands(
                                new SubcommandData("add", "Add text channels where the bot is allowed to send updates")
                                        .addOption(OptionType.BOOLEAN, "suppress","Whether to send notification silently"),
                                new SubcommandData("remove", "Remove text channels where the bot is allowed to send updates")
                        ),

                Commands.slash("user", "Add or remove text channels where the bot is allowed to send updates")
                    .addSubcommands(
                            new SubcommandData("add", "Add user")
                                    .addOption(OptionType.STRING, "user", "AniList Username", true),
                            new SubcommandData("remove", "Remove user")
                                    .addOption(OptionType.STRING, "user", "AniList Username", true)
                    )
        ));
    }
}
