package com.memories_of_war.bot.database;

import com.memories_of_war.bot.utils.Location;
import com.memories_of_war.bot.utils.SquadState;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
public class Squad {

	@Id
	private Long id;

	@CreationTimestamp
    private Timestamp creationDate;

	@UpdateTimestamp
    private Timestamp lastModified;

	private SquadState squadState;

	private Location destination;

	public Squad() {
	    // for Hibernate.
    }

    public Squad(long squadId){
        this.id = squadId;
        this.squadState = SquadState.CLOSED;
        this.destination = Location.LOBBY;
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

    public void refreshLastModified() {
	    this.lastModified = Timestamp.valueOf(LocalDateTime.now());
    }

    public Timestamp getLastModified() {
	    return this.lastModified;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public String getFormattedDestination() {
        switch (this.destination) {
            case LOBBY:
                return "Lobby";
            case ABANDONED_BUNKER:
                return "Abandoned Bunker";
            default:
                return "Undefined";
        }
    }
}