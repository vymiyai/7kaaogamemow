package com.memories_of_war.bot;

import com.memories_of_war.bot.commands.IBotCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.DiscordException;

import java.util.HashMap;
import java.util.List;

@Component
public class CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

    @Value("${discord.TARGET_CHANNEL}")
    private String targetChannel;

    private String botUserName = "Arenagma";

    private HashMap<String, IBotCommand> basicCommands;

    private String placeholder = "*resolving...*";

    @Autowired
    private Lobby lobby;

    @Autowired
    public void setBasicCommands(List<IBotCommand> injectedBasicCommands) {
        this.basicCommands = new HashMap<String, IBotCommand>();

        for (IBotCommand command : injectedBasicCommands)
            this.basicCommands.put(command.getCommandName(), command);
    }

    private String[] tokenize(String messageString) {

        return messageString.split(" ");
    }

    private boolean isDefinedCommand(String commandToken) {
        return this.basicCommands.containsKey(commandToken);
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {

        String messageString = event.getMessage().getContent();
        String[] tokenizedMessage = this.tokenize(messageString);
        String commandToken = tokenizedMessage[0].toLowerCase();

        if (isDefinedCommand(commandToken)) {
            IBotCommand command = this.basicCommands.get(commandToken);
            command.execute(tokenizedMessage, event);
        }

        // do nothing if there is no command match.
    }

    /*
    @EventSubscriber
    public void onUserJoined(UserJoinEvent event) {
        IUser user = event.getUser();
        IRole selectFaction = event.getGuild().getRolesByName("Select Faction").get(0);
        user.addRole(selectFaction);

        String response = String.format(this.getWelcomeMessage(), user.mention());

        try {
            event.getGuild().getDefaultChannel().sendMessage(response);
        } catch (DiscordException e) {
            log.error(this.errorMessage, e);
        }
    }*/

    /*
    @EventSubscriber
    public void onSelfJoined(ReadyEvent event) {
        event.getClient().getGuilds().forEach((guild) -> {
            try {

                IDiscordClient client = event.getClient();
                client.changeUsername(botUserName);
                client.changePresence(StatusType.ONLINE, ActivityType.LISTENING, "?help");
                LOGGER.info(this.botUserName + " now online.");

                LOGGER.info("Trying to access channel " + targetChannel + "...");
                List<IChannel> channels = guild.getChannelsByName(targetChannel);

                for(IChannel channel : channels) {
                    try {
                        channel.bulkDelete();
                    } catch (DiscordException de) {
                        LOGGER.info("No messages to bulk delete in channel {}.", channel.getName());
                    }

                    channel.sendMessage(placeholder);
                }

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }*/


    @EventSubscriber
    public void onSelfJoined(ReadyEvent event) {
        event.getClient().getGuilds().forEach((guild) -> {
            try {

                LOGGER.info("Initializing {}...", this.botUserName);
                IDiscordClient client = event.getClient();
                client.changeUsername(botUserName);
                client.changePresence(StatusType.ONLINE, ActivityType.LISTENING, "?help");

                LOGGER.info("Trying to access channel " + targetChannel + "...");
                List<IChannel> channels = guild.getChannelsByName(targetChannel);

                for (IChannel channel : channels) {
                    try {
                        channel.bulkDelete();
                    } catch (DiscordException de) {
                        LOGGER.info("No messages to bulk delete in channel {}.", channel.getName());
                    }

                    lobby.initializeLobbyMessage(channel.sendMessage(placeholder));
                }

                LOGGER.info("{} is now online.", this.botUserName);

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }
}
