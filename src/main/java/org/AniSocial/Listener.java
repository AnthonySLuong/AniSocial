package org.AniSocial;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
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
        LOGGER.info("{} Logged in!", event.getJDA().getSelfUser().getName());
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commmandString = event.getCommandString();
        String username = event.getUser().getName();
        long userid = event.getUser().getIdLong();
        LOGGER.info("Command received => {} by {} ({})", commmandString, username, userid);

        String commandName = event.getInteraction().getName().toLowerCase();
        if (this.commands.containsKey(commandName)) {
            try {
                this.commands.get(commandName).execute(event);
                LOGGER.info("Command executed => {} by {} ({})", commmandString, username, userid);
            } catch (Exception e) {
                if (event.isAcknowledged()) {
                    event.getHook().editOriginalFormat("Bot couldn't execute %s", commmandString).queue();
                } else {
                    event.replyFormat("Bot couldn't execute %s", commmandString).setEphemeral(true).queue();
                }
                LOGGER.warn(e.getMessage(), e);
            }
        } else {
            event.replyFormat("Invalid command! %s", commmandString).setEphemeral(true).queue();
            LOGGER.error("Invalid command! {}", commmandString);
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        throw new UnsupportedOperationException();
    }
}