package org.AniSocial;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.AniSocial.interfaces.Command;
import org.AniSocial.util.AniList.AniListTask;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Listener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
    private final Cache<String, Command> cache = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        LOGGER.info("{} is ready!", event.getJDA().getSelfUser().getName());

        try (ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1)) {
            executorService.scheduleAtFixedRate(new AniListTask(event.getJDA()), 0, 15, TimeUnit.SECONDS);
            LOGGER.info("Started AniList Task");
        }
    }

    @Override
    public void onSessionDisconnect(@NotNull SessionDisconnectEvent event) {

    }

    @Override
    public void onSessionRecreate(@NotNull SessionRecreateEvent event) {

    }

    @Override
    public void onSessionResume(@NotNull SessionResumeEvent event) {

    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        LOGGER.info("Received Command => {} by {}", event.getCommandString(), event.getUser().getName());

        String commandName = event.getInteraction().getName();
        commandName = commandName.substring(0, 1).toUpperCase() + commandName.substring(1);

        try {
            Class<?> command = Class.forName("org.AniSocial.commands." + commandName);
            Command instance = (Command) command.getDeclaredConstructor()
                    .newInstance();

            instance.setSlashEvent(event)
                    .executeSlashCommand();

            this.cache.put(event.getId(), instance);
            LOGGER.info("Executed Command => {} by {}", event.getCommandString(), event.getUser().getName());
        } catch (Exception e) {
            if (event.isAcknowledged()) {
                event.getHook().editOriginalFormat("Bot couldn't execute %s", event.getCommandString()).queue();
            } else {
                event.replyFormat("Bot couldn't execute %s", event.getCommandString()).setEphemeral(true).queue();
            }
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        LOGGER.info("Received Button => by {}", event.getUser().getName());

        MessageEditBuilder editData = MessageEditBuilder
                .fromMessage(event.getMessage())
                .clear()
                .setContent("Interaction Timed out");

        String[] split = event.getComponentId().trim().split("[\\s-]+");
        Command c = this.cache.getIfPresent(split[1].trim());
        if (c != null) {
            try {
                c.setButtonEvent(event).executeButtonInteraction();
            } catch (Exception e) {
                event.editMessage(editData.build()).queue();
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            event.editMessage(editData.build()).queue();
            LOGGER.warn("Button not in cache {}", split[1].trim());
        }
    }
}