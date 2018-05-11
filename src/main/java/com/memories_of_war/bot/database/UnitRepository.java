package com.memories_of_war.bot.database;


import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UnitRepository extends CrudRepository<Unit, Long> {
    List<Unit> findBySquad(Squad squad);
}