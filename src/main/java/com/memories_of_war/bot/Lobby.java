package com.memories_of_war.bot;

import com.memories_of_war.bot.database.*;
import com.memories_of_war.bot.utils.Flags;
import com.memories_of_war.bot.utils.SquadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RateLimitException;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class Lobby {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lobby.class);

    @Value("${discord.MAXIMUM_NUMBER_OF_SQUADS}")
    private int MAXIMUM_NUMBER_OF_SQUADS;

    @Value("${discord.MAXIMUM_NUMBER_OF_SQUAD_MEMBERS}")
    private int MAXIMUM_NUMBER_OF_SQUAD_MEMBERS;

    @Autowired
    private SquadRepository squadRepository;

    @Autowired
    private UnitRepository unitRepository;

    private IMessage LOBBY_MESSAGE = null;

    @Scheduled(fixedRate = 5000)
    public void refresh() {

        if(Objects.isNull(LOBBY_MESSAGE)) {
            LOGGER.warn("Lobby message identifier not initialized.");
        } else {

            EmbedBuilder builder = new EmbedBuilder();

            builder.withColor(Color.black);

            builder.withAuthorIcon(Flags.FO);
            builder.withAuthorName("WAITING SQUADS");
            builder.withAuthorUrl(Flags.GI);

            builder.withTitle("Lobby");
            builder.withDescription("Type ?squad to see the squad options.");

            int index = 1;
            Iterable<Squad> squads = squadRepository.findBySquadStateNot(SquadState.CLOSED);
            for(Squad squad : squads) {
                List<Unit> units = unitRepository.findBySquad(squad);
                builder.appendField("[" + units.size() + "/" + MAXIMUM_NUMBER_OF_SQUAD_MEMBERS + "] SQUAD " + squad.getId(), this.getFormattedSquadComponents(units), true);
                index++;
            }

            try {
                if (index == 1) {
                    LOBBY_MESSAGE.edit("```There are no squads registered. You can start a new one by using the \"?squad new\" command.```");
                } else {
                    LOBBY_MESSAGE.edit(builder.build());
                }
            } catch (RateLimitException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    private String getFormattedSquadComponents(List<Unit> units) {
        return String.join("\n", units.stream().map(Unit::getUnitNameWithFaction).collect(Collectors.toList()));
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
