package org.AniSocial.subcommands;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.AniSocial.interfaces.SubCommandInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class User implements SubCommandInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(User.class);
    private static User user = null;

    @Override
    public void autoComplete(CommandAutoCompleteInteractionEvent event) {
        return;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        return;
    }

    @Override
    public SubcommandData getSubcommandData() {
        return new SubcommandData("user", "user")
                .addOption(OptionType.USER, "username", "username", true);
    }

    synchronized public static User getInstance() {
        if (user == null) {
            user = new User();
        }
        return user;
    }
}
