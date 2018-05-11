package com.memories_of_war.bot.database;

import com.memories_of_war.bot.exceptions.UserInformationException;
import com.memories_of_war.bot.utils.SquadState;
import com.memories_of_war.bot.utils.UnitState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class SquadService {

    @Value("${discord.MAXIMUM_NUMBER_OF_SQUAD_MEMBERS}")
    private int MAXIMUM_NUMBER_OF_SQUAD_MEMBERS;

    @Autowired
    private SquadRepository squadRepository;

    @Autowired
    private  UnitRepository unitRepository;

    private void abortIfUnitAlreadyInASquad(Unit unit) throws UserInformationException {
        if(Objects.nonNull(unit.getSquad())) {
            throw new UserInformationException(": unit already in a squad. Use the `?squad leave` command to leave your current squad.");
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

    @Transactional()
    public void newSquad(long unitId) throws UserInformationException {
        Unit unit = this.findUnitById(unitId);

        abortIfUnitAlreadyInASquad(unit);

        List<Squad> closedSquads = squadRepository.findBySquadState(SquadState.CLOSED);

        if(closedSquads.size() > 0){
            Squad squad = closedSquads.get(0);
            squad.setSquadState(SquadState.WAITING);
            unit.setSquad(squad);
            unit.setUnitState(UnitState.WAITING_IN_LOBBY);
        } else {
            throw new UserInformationException(": maximum number of concurrent squads reached.");
        }
    }

    @Transactional
    public void joinSquad(long squadId, long unitId) throws UserInformationException {
        Unit unit = this.findUnitById(unitId);

        abortIfUnitAlreadyInASquad(unit);

        Squad squad = squadRepository.findOne(squadId);
        if(Objects.isNull(squad) || squad.getSquadState().equals(SquadState.CLOSED)) {
            throw new UserInformationException(": could not find a squad with ID " + squadId    + ".");
        }

        if(unitRepository.findBySquad(squad).size() < MAXIMUM_NUMBER_OF_SQUAD_MEMBERS) {
            unit.setSquad(squad);
            unit.setUnitState(UnitState.WAITING_IN_LOBBY);
        } else {
            throw new UserInformationException(": could not join squad. The squad is full.");
        }
    }

    @Transactional
    public void leaveSquad(long unitId) throws UserInformationException {
        Unit unit = this.findUnitById(unitId);

        if(Objects.nonNull(unit.getSquad())) {
            Squad squad = unit.getSquad();

            if(unitRepository.findBySquad(squad).size() == 1) {
                squad.setSquadState(SquadState.CLOSED);
            }

            unit.setSquad(null);
            unit.setUnitState(UnitState.IDLE);
        }
    }
}
