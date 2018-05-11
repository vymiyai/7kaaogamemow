package com.memories_of_war.bot;

import com.memories_of_war.bot.database.*;
import com.memories_of_war.bot.utils.SquadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RateLimitException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class Lobby {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lobby.class);

    @Value("${discord.MAXIMUM_NUMBER_OF_SQUADS}")
    private int MAXIMUM_NUMBER_OF_SQUADS;

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
            Iterable<Squad> squads = squadRepository.findBySquadStateNot(SquadState.CLOSED);
            for(Squad squad : squads) {
                lobbyMessageContent += this.getFormattedSquadComponents(squad) + '\n';
            }

            try {
                if (lobbyMessageContent.isEmpty()) {
                    LOBBY_MESSAGE.edit("```There are no squads registered. You can start a new one by using the \"?squad new\" command.```");
                } else {
                    LOBBY_MESSAGE.edit("```SQUADS ASSEMBLING:```\n\n`ID MEMBERS`\n" + lobbyMessageContent);
                }
            } catch (RateLimitException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    private String getFormattedSquadComponents(Squad squad) {
        List<Unit> units = unitRepository.findBySquad(squad);
        String names = String.join("   ", units.stream().map(unit -> unit.getUnitNameWithFaction()).collect(Collectors.toList()));
        return "`" + squad.getId() + "`        " + names;
    }

    public void initializeLobby(IMessage message) {
        for(int i = 1; i <= MAXIMUM_NUMBER_OF_SQUADS; i++) {
            Squad squad = new Squad(i);
            squadRepository.save(squad);
        }

        this.LOBBY_MESSAGE = message;

        LOGGER.info("Lobby initialized.");
    }
}
