package com.memories_of_war.bot.database;

import com.memories_of_war.bot.utils.UnitState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class SquadService {

    public final int MAXIMUM_NUMBER_OF_SQUADS = 6;

    @Autowired
    private SquadRepository squadRepository;

    @Autowired
    private  UnitRepository unitRepository;

    private void abortIfMaximumNumberOfSquadsReached() throws Exception {
        if (squadRepository.count() >= MAXIMUM_NUMBER_OF_SQUADS) {
            throw new Exception(": maximum number of concurrent squads reached.");
        }
    }

    private void abortIfUnitAlreadyInASquad(Unit unit) throws Exception {
        if(Objects.nonNull(unit.getSquad())) {
            throw new Exception(": unit already in a squad. Use the `?squad leave` command to leave your current squad.");
        }
    }

    @Transactional(readOnly = true)
    private Unit findUnitById(long unitId) throws Exception {
        Unit unit = unitRepository.findOne(unitId);
        if(Objects.isNull(unit)) {
            throw new Exception(": no unit registered. Use the `?enlist` command before using this one.");
        } else {
            return unit;
        }
    }

    @Transactional()
    public void newSquad(long unitId) throws Exception {
        abortIfMaximumNumberOfSquadsReached();

        Unit unit = this.findUnitById(unitId);

        abortIfUnitAlreadyInASquad(unit);

        Squad squad = squadRepository.save(new Squad(squadRepository.count() + 1));
        unit.setSquad(squad);
        unit.setUnitState(UnitState.WAITING_IN_LOBBY);
    }

    @Transactional
    public void joinSquad(long squadId, long unitId) throws Exception {
        Unit unit = this.findUnitById(unitId);

        abortIfUnitAlreadyInASquad(unit);

        Squad squad = squadRepository.findOne(squadId);
        if(unitRepository.findBySquad(squad).size() < MAXIMUM_NUMBER_OF_SQUADS) {
            unit.setSquad(squad);
            unit.setUnitState(UnitState.WAITING_IN_LOBBY);
        } else {
            throw new Exception(": could not join squad. The squad is full.");
        }
    }

    @Transactional
    public void leaveSquad(long unitId) throws Exception {
        Unit unit = this.findUnitById(unitId);

        if(Objects.nonNull(unit.getSquad())) {
            Squad squad = unit.getSquad();

            if(unitRepository.findBySquad(squad).size() == 1) {
                squadRepository.delete(squad);
            }

            unit.setSquad(null);
            unit.setUnitState(UnitState.IDLE);
        }
    }
}
