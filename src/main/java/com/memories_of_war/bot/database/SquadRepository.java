package com.memories_of_war.bot.database;


import com.memories_of_war.bot.utils.SquadState;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SquadRepository extends CrudRepository<Squad, Long> {

    List<Squad> findBySquadState(SquadState squadState);
}