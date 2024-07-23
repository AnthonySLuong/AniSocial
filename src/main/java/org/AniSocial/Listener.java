package org.AniSocial;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.AniSocial.interfaces.CommandInterface;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@RequiredArgsConstructor
public class Listener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
    private final Map<String, CommandInterface> commands;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        String login = String.format("%s Logged in!", event.getJDA().getSelfUser().getName());
        LOGGER.info(login);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commmandString = event.getCommandString();
        String username = event.getUser().getName();
        long userid = event.getUser().getIdLong();
        String logExecute = String.format("Command received => %s by %s (%d)", commmandString, username, userid);
        LOGGER.info(logExecute);

        String commandName = event.getInteraction().getName().toLowerCase();
        if (this.commands.containsKey(commandName)) {
            try {
                this.commands.get(commandName).execute(event);
                LOGGER.info(String.format("Command executed => %s by %s (%d)", commmandString, username, userid));
            } catch (Exception e) {
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal(String.format("Bot couldn't execute %s", commmandString)).queue();
                } else {
                    event.reply(String.format("Bot couldn't execute %s", commmandString)).setEphemeral(true).queue();
                }
                LOGGER.warn(String.format("Bot couldn't execute %s", commmandString), e);
            }
        } else {
            event.reply(String.format("Invalid command! %s", commmandString)).setEphemeral(true).queue();
            LOGGER.error(String.format("Invalid command! %s", commmandString));
        }
    }
}