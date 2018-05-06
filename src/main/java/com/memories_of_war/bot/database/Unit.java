package com.memories_of_war.bot.database;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
public class Unit {

    public int UNIT_HEALTH_POINT_MULTIPLIER = 2;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	private Timestamp creationDate;

	private int healthPoints;

	private int currentHealthPoints;

	// combat proficiencies
    private int rifleProficiency;                       // UR bonus
    private int smgProficiency;                         // SU bonus
    private int machineGunProficiency;                  // EA bonus
    private int flamethrowerProficiency;
    private int atRifleProficiency;                     // SE bonus
    private int recoillessRifleProficiency;
    // grenade rifle/launcher                           // LJ bonus
    // mortar
    // sniper
    // shotgun                                          // AW bonus

    // terrain proficiencies.

    // skill proficiencies.
    private int leadership;



    /*

    Rifleman - basic
SMG
Machine Gun
Flamethrower
AT rifle
RPG
Assault rifleman
Mortar
     */


    private int leadershipProficiency;

	// new user constructor.
	public Unit() {
        this.rifleProficiency = 10;
	    this.healthPoints = this.rifleProficiency * this.UNIT_HEALTH_POINT_MULTIPLIER;
	    this.currentHealthPoints = this.healthPoints;
	}

	@Override
	public String toString() {
		return String.format(
				"Unit [id='%d', HP: %d/%d, Combat: %d]",
				this.id, this.currentHealthPoints, this.currentHealthPoints, this.rifleProficiency);
	}

}