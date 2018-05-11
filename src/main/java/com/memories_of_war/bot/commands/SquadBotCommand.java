package com.memories_of_war.bot.commands;

import com.memories_of_war.bot.services.DiscordRoleService;
import com.memories_of_war.bot.services.SquadService;
import com.memories_of_war.bot.exceptions.UserInformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

@Component
public class SquadBotCommand implements IBotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(SquadBotCommand.class);

    @Value("${discord.MAXIMUM_NUMBER_OF_SQUADS}")
    private int MAXIMUM_NUMBER_OF_SQUADS;

    @Autowired
    private SquadService squadService;

    @Autowired
    private DiscordRoleService discordRoleService;

    @Override
    public void execute(String[] tokenizedMessage, MessageReceivedEvent event) {

        IUser author = event.getAuthor();
        String mention = author.mention();
        Long discordId = author.getLongID();

        try {
            if (tokenizedMessage.length == 1) {
                String manual = this.getManual();
                event.getChannel().sendMessage(mention + manual);
                return;
            }

            if (tokenizedMessage.length == 2) {
                if(tokenizedMessage[1].toLowerCase().equals("leave") || tokenizedMessage[1].toLowerCase().equals("-l")) {
                    squadService.leaveSquad(discordId);
                    discordRoleService.removeSquadRole(event.getGuild(), author);
                    event.getChannel().sendMessage(mention + ": left the current squad.");
                    return;
                }

                if(tokenizedMessage[1].toLowerCase().equals("new") || tokenizedMessage[1].toLowerCase().equals("-n")) {
                    squadService.newSquad(discordId);
                    discordRoleService.addSquadRole(event.getGuild(), author);
                    event.getChannel().sendMessage(mention + ": new squad raised.");
                    return;
                } else {
                    throw new UserInformationException(": unrecognized command option \"" + event.getMessage().toString() + "\"");
                }
            }

            if (tokenizedMessage.length == 3) {
                if(tokenizedMessage[1].toLowerCase().equals("join") || tokenizedMessage[1].toLowerCase().equals("-j")) {
                    Integer squadId;
                    try {
                        squadId = Integer.parseInt(tokenizedMessage[2]);
                    } catch (NumberFormatException e) {
                        throw new Exception(": join parameter <SQUAD_ID> must be an integer. e.g. `?squad join 1`");
                    }

                    squadService.joinSquad(squadId, discordId);
                    discordRoleService.addSquadRole(event.getGuild(), author);
                    event.getChannel().sendMessage(mention + ": joined squad " + squadId + ".");
                    return;
                } else {
                    throw new UserInformationException(": unrecognized command option \"" + event.getMessage().toString() + "\"");
                }
            }

            if(tokenizedMessage.length > 3 ){
                event.getChannel().sendMessage(mention + this.getManual());
            }

        } catch (UserInformationException e) {
            event.getChannel().sendMessage(mention + e.getMessage());
        } catch (Exception e) {
            event.getChannel().sendMessage(mention + e.getMessage());

            String errorMessage = String.format("User %s in channel %s:", author.getName(), event.getChannel().getName());
            LOGGER.error(errorMessage, e);
        }
    }

    private String getManual() {
        String manual = "```";
        manual += "Type \"?squad\" to see this manual.\n";
        manual += "Type \"?squad new\" or \"?squad -n\" to raise a new squad (maximum number of existing squads: " + MAXIMUM_NUMBER_OF_SQUADS + ").\n";
        manual += "Type \"?squad join <SQUAD_ID>\" or \"?squad -j <SQUAD_ID>\" to join the squad identified by <SQUAD_ID>.\n";
        manual += "Type \"?squad leave\" or \"?squad -l\" to leave your current squad.```";

        return manual;
    }

    @Override
    public String getCommandName() {
        return "?squad";
    }

    @Override
    public String getCommandDescription() {
        return "Type ?squad to see the squad options.";
    }

}
