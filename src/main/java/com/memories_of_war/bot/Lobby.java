package com.memories_of_war.bot;

import com.memories_of_war.bot.database.Squad;
import com.memories_of_war.bot.database.SquadRepository;
import com.memories_of_war.bot.database.Unit;
import com.memories_of_war.bot.database.UnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.obj.IMessage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class Lobby {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lobby.class);

    @Autowired
    private SquadRepository squadRepository;

    @Autowired
    private UnitRepository unitRepository;

    private IMessage LOBBY_MESSAGE = null;

    @Scheduled(fixedRate = 2000)
    public void refresh() {

        if(Objects.isNull(LOBBY_MESSAGE)) {
            LOGGER.warn("Lobby message identifier not initialized.");
        } else {

            String lobbyMessageContent = "";
            Iterable<Squad> squads = squadRepository.findAll();
            for(Squad squad : squads) {
                lobbyMessageContent += this.getFormattedSquadComponents(squad) + '\n';
            }

            if(lobbyMessageContent.isEmpty()) {
                LOBBY_MESSAGE.edit("```There are no squads registered. You can start a new one by using the \"?squad new\" command.```");
            } else {
                LOBBY_MESSAGE.edit("**SQUADS ASSEMBLING:**\n\n**ID        MEMBERS**\n" + lobbyMessageContent);
            }
        }
    }

    private String getFormattedSquadComponents(Squad squad) {
        List<Unit> units = unitRepository.findBySquad(squad);
        String names = String.join("   ", units.stream().map(unit -> unit.getUnitNameWithFaction()).collect(Collectors.toList()));
        return "**"+ squad.getId() + "**        " + names;
    }

    public void initializeLobbyMessage(IMessage message) {
        this.LOBBY_MESSAGE = message;
    }
}
