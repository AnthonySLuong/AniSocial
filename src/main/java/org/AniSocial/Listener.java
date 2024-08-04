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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Listener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
    private final Cache<String, Command> cache = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledTask;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        LOGGER.info("{} is ready!", event.getJDA().getSelfUser().getName());

        this.scheduledTask = this.executor.scheduleAtFixedRate(new AniListTask(event.getJDA()), 0, 15, TimeUnit.SECONDS);
        LOGGER.info("Started AniList Task");
    }

    @Override
    public void onSessionDisconnect(@NotNull SessionDisconnectEvent event) {
        if (this.scheduledTask != null && !this.scheduledTask.isDone()) {
            this.scheduledTask.cancel(true);
            LOGGER.info("Stopped AniList Task");
        }
    }

    @Override
    public void onSessionRecreate(@NotNull SessionRecreateEvent event) {
        if (this.scheduledTask != null && this.scheduledTask.isDone()) {
            this.scheduledTask = this.executor.scheduleAtFixedRate(new AniListTask(event.getJDA()), 0, 15, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onSessionResume(@NotNull SessionResumeEvent event) {
        if (this.scheduledTask != null && this.scheduledTask.isDone()) {
            this.scheduledTask = this.executor.scheduleAtFixedRate(new AniListTask(event.getJDA()), 0, 15, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        LOGGER.info("Command => {} by {}", event.getCommandString(), event.getUser().getName());

        String commandName = event.getInteraction().getName();
        commandName = commandName.substring(0, 1).toUpperCase() + commandName.substring(1);

        try {
            Class<?> command = Class.forName("org.AniSocial.commands." + commandName);
            Command instance = (Command) command.getDeclaredConstructor()
                    .newInstance();

            instance.setSlashEvent(event)
                    .executeSlashCommand();

            this.cache.put(event.getId(), instance);
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
        LOGGER.info("Button => {} by {}", event.getComponentId(), event.getUser().getName());

        MessageEditBuilder editData = MessageEditBuilder
                .fromMessage(event.getMessage())
                .clear();

        String[] split = event.getComponentId().trim().split("[\\s-]+");
        Command command = this.cache.getIfPresent(split[2].trim());
        if (command != null) {
            try {
                command.setButtonEvent(event).executeButtonInteraction();
            } catch (Exception e) {
                editData.setContent("Bot couldn't execute Interaction");
                event.editMessage(editData.build()).queue();
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            editData.setContent("Interaction Timed out");
            event.editMessage(editData.build()).queue();
            LOGGER.warn("Button not in cache {}", split[1].trim());
        }
    }
}