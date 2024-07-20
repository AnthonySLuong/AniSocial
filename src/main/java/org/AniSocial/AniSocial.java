package org.AniSocial;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.AniSocial.commands.Add;
import org.AniSocial.commands.Remove;
import org.AniSocial.interfaces.CommandInterface;
import org.AniSocial.util.AniListRunner;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AniSocial extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AniSocial.class);
    private static final Map<String, Class<? extends CommandInterface>> commands = new HashMap<>();
    private static final List<SlashCommandData> commandsData = new ArrayList<>();

    static {
        commands.put(Add.class.getSimpleName().toLowerCase(), Add.class);
        commands.put(Remove.class.getSimpleName().toLowerCase(), Remove.class);

        for (Class<? extends CommandInterface> commandsClass : commands.values()) {
            CommandInterface instance = null;
            try {
                instance = (CommandInterface) commandsClass.getMethod("getInstance").invoke(null);
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage(), e);
                e.printStackTrace();
            }
            commandsData.add(instance.getSlashCommandData());
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        LOGGER.info(String.format("%s Logged in!", event.getJDA().getSelfUser().getName()));
        event.getJDA().getGuildById("1081902181617254410").updateCommands().addCommands(commandsData).queue();
        AniListRunner.run(event.getJDA());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        LOGGER.info(String.format("Command received => %s by %s", event.getCommandString(), event.getUser().getName()));

        String commandName = event.getInteraction().getName().toLowerCase();
        if (commands.containsKey(commandName)) {
            Class<? extends CommandInterface> commandClass = commands.get(commandName);
            try {
                CommandInterface instance = (CommandInterface) commandClass.getMethod("getInstance").invoke(null);
                instance.execute(event);
                LOGGER.info(String.format("Command executed => %s by %s", event.getCommandString(), event.getUser().getName()));
            } catch (Exception e) {
                event.reply(String.format("Bot couldn't execute %s", event.getCommandString())).setEphemeral(true).queue();
                LOGGER.warn(String.format("Bot couldn't execute %s", event.getCommandString()), e);
            }
        } else {
            event.reply(String.format("Invalid command! %s", event.getCommandString())).setEphemeral(true).queue();
            LOGGER.warn(String.format("Invalid command! %s", event.getCommandString()));
        }
    }
}