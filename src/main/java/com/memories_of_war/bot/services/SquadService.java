package com.memories_of_war.bot.services;

import com.memories_of_war.bot.Application;
import com.memories_of_war.bot.database.Squad;
import com.memories_of_war.bot.database.SquadRepository;
import com.memories_of_war.bot.database.Unit;
import com.memories_of_war.bot.database.UnitRepository;
import com.memories_of_war.bot.exceptions.UserInformationException;
import com.memories_of_war.bot.utils.Location;
import com.memories_of_war.bot.utils.SquadState;
import com.memories_of_war.bot.utils.UnitState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;
import java.util.Objects;

@Service
public class SquadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SquadService.class);

    @Value("${discord.MAXIMUM_NUMBER_OF_SQUAD_MEMBERS}")
    private int MAXIMUM_NUMBER_OF_SQUAD_MEMBERS;

    @Autowired
    private SquadRepository squadRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private DiscordRoleService discordRoleService;

    private void abortIfUnitAlreadyInASquad(Unit unit) throws UserInformationException {
        if(Objects.nonNull(unit.getSquad())) {
            throw new UserInformationException(": unit already in a squad. Use the `?squad leave` command to leave your current squad.");
        }
    }

    private void abortIfSquadAlreadySorteing(Squad squad) throws UserInformationException {
        if(squad.getSquadState().equals(SquadState.IN_COMBAT) || squad.getSquadState().equals(SquadState.IN_MOVEMENT)) {
            throw new UserInformationException(": command cannot be issued while the squad is sortieing.");
        }
    }

    @Transactional(readOnly = true)
    private Unit findUnitById(long unitId) throws UserInformationException {
        Unit unit = unitRepository.findOne(unitId);
        if(Objects.isNull(unit)) {
            throw new UserInformationException(": no unit registered. Use the `?enlist` command before using this one.");
        } else {
            return unit;
        }
    }

    @Transactional
    public void sortieSquad(long unitId) throws UserInformationException {
        Unit unit = this.findUnitById(unitId);
        Squad squad = unit.getSquad();

        abortIfSquadAlreadySorteing(squad);

        if(squad.getSquadState().equals(SquadState.WAITING)) {
            LOGGER.info("User [{}] has ordered squad [{}] to sortie.", unit.getId(), squad.getId());
            squad.setSquadState(SquadState.IN_MOVEMENT);
        } else {
            throw new UserInformationException(": only squads waiting in the lobby can sortie.");
        }
    }

    @Transactional
    public void disbandSquad(long squadId) {
        LOGGER.info("Disbanding squad [{}].", squadId);

        Squad squad = squadRepository.findOne(squadId);
        List<Unit> units = unitRepository.findBySquad(squad);
        units.stream().forEach(unit -> {unit.setSquad(null); unit.setUnitState(UnitState.IDLE);});
        squad.setSquadState(SquadState.CLOSED);
        squad.setDestination(Location.LOBBY);
        squad.refreshLastModified();

        for(IGuild guild : Application.DISCORD_CLIENT.getGuilds()) {
            if(guild.getName().equals("March of War")) {
                for(Unit unit : units){
                    IUser user = guild.getUserByID(unit.getId());
                    discordRoleService.removeSquadRole(guild, user);
                }
            }
        }
    }

    @Transactional
    public void newSquad(long unitId) throws UserInformationException {

        Unit unit = this.findUnitById(unitId);

        abortIfUnitAlreadyInASquad(unit);

        List<Squad> closedSquads = squadRepository.findBySquadState(SquadState.CLOSED);

        if(closedSquads.size() > 0){
            Squad squad = closedSquads.get(0);
            squad.setSquadState(SquadState.WAITING);
            squad.setDestination(Location.ABANDONED_BUNKER);

            unit.setSquad(squad);
            unit.setUnitState(UnitState.WAITING_IN_LOBBY);

            LOGGER.info("Unit [{}] formed new squad [{}].", unitId, squad.getId());
        } else {
            throw new UserInformationException(": maximum number of concurrent squads reached.");
        }
    }

    @Transactional
    public void joinSquad(long squadId, long unitId) throws UserInformationException {
        Unit unit = this.findUnitById(unitId);

        abortIfUnitAlreadyInASquad(unit);

        Squad squad = squadRepository.findOne(squadId);

        abortIfSquadAlreadySorteing(squad);

        if(Objects.isNull(squad)) {
            throw new UserInformationException(": could not find a squad with ID " + squadId    + ".");
        }

        if(squad.getSquadState().equals(SquadState.CLOSED)) {
            throw new UserInformationException(": squad " + squadId    + " is currently on a mission.");
        }

        if(unitRepository.findBySquad(squad).size() < MAXIMUM_NUMBER_OF_SQUAD_MEMBERS) {
            unit.setSquad(squad);
            unit.setUnitState(UnitState.WAITING_IN_LOBBY);

            LOGGER.info("Unit [{}] has joined squad [{}].", unitId, squad.getId());
        } else {
            throw new UserInformationException(": could not join squad. The squad is full.");
        }
    }

    @Transactional
    public void leaveSquad(long unitId) throws UserInformationException {
        Unit unit = this.findUnitById(unitId);

        if(Objects.nonNull(unit.getSquad())) {
            Squad squad = unit.getSquad();

            abortIfSquadAlreadySorteing(squad);

            if(unitRepository.findBySquad(squad).size() == 1) {
                squad.setSquadState(SquadState.CLOSED);
                squad.setDestination(Location.LOBBY);
            }

            unit.setSquad(null);
            unit.setUnitState(UnitState.IDLE);
            squad.refreshLastModified();

            LOGGER.info("Unit [{}] has left squad [{}].", unitId, squad.getId());
        }
    }
}
