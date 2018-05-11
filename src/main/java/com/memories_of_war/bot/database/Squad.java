package com.memories_of_war.bot.database;

import com.memories_of_war.bot.utils.SquadState;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
public class Squad {

	@Id
	private Long id;

	@CreationTimestamp
    private Timestamp creationDate;

	@UpdateTimestamp
    private Timestamp lastModified;

	private SquadState squadState;

	public Squad() {
	    // for Hibernate.
    }

    public Squad(long squadId){
        this.id = squadId;
        this.squadState = SquadState.CLOSED;
    }

    public Long getId() {
        return id;
    }

    public SquadState getSquadState() {
        return squadState;
    }

    public void setSquadState(SquadState squadState) {
        this.squadState = squadState;
    }
}