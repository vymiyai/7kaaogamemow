package com.memories_of_war.bot;

import com.memories_of_war.bot.commands.IBotCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.DiscordException;

import java.util.HashMap;
import java.util.List;

@Component
public class CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    private String welcomeMessage = "NOT USED";
    private String readyMesssage = "*Test bot online.";
    private String playingText = "!help";
    private String botUserName = "Arenagma";
    private String errorMessage = "Message could not be sent with error: \n";

    private HashMap<String, IBotCommand> basicCommands;

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
    }
    */

    @EventSubscriber
    public void onSelfJoined(ReadyEvent event) {
        String response = this.readyMesssage;

        event.getClient().getGuilds().forEach((guild) -> {
            try {

                IDiscordClient client = event.getClient();
                client.changeUsername(botUserName);
                client.changePresence(StatusType.ONLINE, ActivityType.WATCHING, "?help");
                guild.getDefaultChannel().sendMessage(response);

            } catch (DiscordException e) {
                log.error(this.errorMessage, e);
            }
        });
    }

    private String getWelcomeMessage() {
        StringBuilder response = new StringBuilder();
        response.append(readyMesssage);
        return response.toString();
    }

}
