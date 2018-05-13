package com.memories_of_war.bot;

import com.memories_of_war.bot.database.Squad;
import com.memories_of_war.bot.database.SquadRepository;
import com.memories_of_war.bot.database.Unit;
import com.memories_of_war.bot.database.UnitRepository;
import com.memories_of_war.bot.services.SquadService;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
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

    @Autowired
    private SquadService squadService;

    private IMessage LOBBY_MESSAGE = null;

    @Scheduled(fixedRate = 5000)
    public void refresh() {

        if(Objects.isNull(LOBBY_MESSAGE)) {
            LOGGER.warn("Lobby message identifier not initialized.");
        } else {

            EmbedBuilder builder = new EmbedBuilder();

            builder.withColor(Color.black);

            builder.withAuthorIcon(Flags.FO);
            builder.withAuthorName("SQUADS");
            builder.withAuthorUrl(Flags.FO);

            builder.withTitle("Lobby");
            builder.withDescription("Type ?squad to see the squad options.");

            int index = 1;
            Iterable<Squad> squads = squadRepository.findBySquadStateNot(SquadState.CLOSED);
            for(Squad squad : squads) {

                if (this.isSquadIdleForMoreThanFiveMinutes(squad.getLastModified()) && squad.getSquadState().equals(SquadState.WAITING)) {
                    LOGGER.info("Disbanding squad {} due to inactivity.", squad.getId());
                    squadService.disbandSquad(squad.getId());
                } else {
                    List<Unit> units = unitRepository.findBySquad(squad);
                    builder.appendField("SQUAD " + squad.getId(), this.getSquadContent(squad, units), true);
                    index++;
                }
            }

            try {
                if (index == 1) {
                    LOBBY_MESSAGE.edit("```There are no active squads. You can start a new one by using the \"?squad new\" command.```");
                } else {
                    LOBBY_MESSAGE.edit(builder.build());
                }
            } catch (RateLimitException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    private boolean isSquadIdleForMoreThanFiveMinutes(Timestamp lastModfiied) {
        return lastModfiied.toLocalDateTime().isBefore(LocalDateTime.now().minusMinutes(5));
    }

    private String getSquadContent(Squad squad, List<Unit> units) {
        String content = "";
        switch (squad.getSquadState()){
            case IN_MOVEMENT:
                content = "Moving towards [" + squad.getFormattedDestination() + "]";
                break;
            case IN_COMBAT:
                content = "Engaging enemy at [" + squad.getFormattedDestination() + "]";
                break;
            case WAITING:
                content = "Waiting in lobby [" + units.size() + "/" + MAXIMUM_NUMBER_OF_SQUAD_MEMBERS + "] - Destination: [" + squad.getFormattedDestination() + "]";
                break;
            case CLOSED:
                break;
            default:
                break;
        }

        content += "\n\n";
        content += this.getFormattedSquadComponents(units);
        return content;
    }

    private String getFormattedSquadComponents(List<Unit> units) {
        return String.join("\n", units.stream().map(Unit::getUnitNameWithFaction).collect(Collectors.toList()));
    }

    public void initializeLobby(IMessage message) {
        for(int index = 1; index <= MAXIMUM_NUMBER_OF_SQUADS; index++) {
            Squad squad = new Squad(index);
            squadRepository.save(squad);

        }

        this.LOBBY_MESSAGE = message;

        LOGGER.info("Lobby initialized.");
    }
}
