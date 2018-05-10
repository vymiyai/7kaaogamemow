package com.memories_of_war.bot.database;

import com.memories_of_war.bot.utils.Faction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
public class Unit {

    public int UNIT_HEALTH_POINT_MULTIPLIER = 2;

	@Id
	private Long id;

	@CreationTimestamp
    private Timestamp creationDate;

	@UpdateTimestamp
    private Timestamp lastModified;

	private int healthPoints;

	private int currentHealthPoints;

	private Faction faction;

	// combat proficiencies
    private int meleeProficiency;						// AW SE
    private int rifleProficiency;                       // UR LJ
    private int smgProficiency;                         // SU EA
    private int machineGunProficiency;                  // EA UR
    private int projectorProficiency;					// SE LJ
    private int atRifleProficiency;                     // SE SU
    private int rocketLauncherProficiency;				// EA AW
    private int sniperRifleProficiency;					// LJ SU
    private int shotgunProficiency;						// AW UR

    /*
    // terrain proficiencies.

    // skill proficiencies.
    private int leadership;
    private int leadershipProficiency;

    @Transient
    private CombatClass combatClass;
    */

    public Unit(){
        // for Hibernate.
    }

	// new user constructor.
	public Unit(long id, Faction faction) {
	    this.id = id;
	    this.faction = faction;

        this.meleeProficiency = 5;
	    this.healthPoints = this.meleeProficiency * this.UNIT_HEALTH_POINT_MULTIPLIER;
	    this.currentHealthPoints = this.healthPoints;
	}

    public void setMeleeProficiency(int meleeProficiency) {
        this.meleeProficiency = meleeProficiency;
    }

    public void setRifleProficiency(int rifleProficiency) {
        this.rifleProficiency = rifleProficiency;
    }

    public void setSmgProficiency(int smgProficiency) {
        this.smgProficiency = smgProficiency;
    }

    public void setMachineGunProficiency(int machineGunProficiency) {
        this.machineGunProficiency = machineGunProficiency;
    }

    public void setProjectorProficiency(int projectorProficiency) {
        this.projectorProficiency = projectorProficiency;
    }

    public void setAtRifleProficiency(int atRifleProficiency) {
        this.atRifleProficiency = atRifleProficiency;
    }

    public void setRocketLauncherProficiency(int rocketLauncherProficiency) {
        this.rocketLauncherProficiency = rocketLauncherProficiency;
    }

    public void setSniperRifleProficiency(int sniperRifleProficiency) {
        this.sniperRifleProficiency = sniperRifleProficiency;
    }

    public void setShotgunProficiency(int shotgunProficiency) {
        this.shotgunProficiency = shotgunProficiency;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public int getCurrentHealthPoints() {
        return currentHealthPoints;
    }

    public String getCombatProficienciesNames() {
        return "MELEE\n" +
            "RIFLE\n" +
            "SUBMACHINE GUN\n" +
            "MACHINE GUN\n" +
            "LIQUID PROJECTOR\n" +
            "ANTI-TANK RIFLE\n" +
            "ROCKET LAUNCHER\n" +
            "SNIPER RIFLE\n" +
            "SHOTGUN";
    }

    public String getCombatProficienciesLevels() {
        String format= "%d\n" +
                "%d\n" +
                "%d\n" +
                "%d\n" +
                "%d\n" +
                "%d\n" +
                "%d\n" +
                "%d\n" +
                "%d";

        return String.format(format,
                this.meleeProficiency,
                this.rifleProficiency,
                this.smgProficiency,
                this.machineGunProficiency,
                this.projectorProficiency,
                this.atRifleProficiency,
                this.rocketLauncherProficiency,
                this.sniperRifleProficiency,
                this.shotgunProficiency);
    }

    @Override
	public String toString() {
		return String.format(
				"Unit [id='%d', HP: %d/%d, Combat: %d]",
				this.id, this.currentHealthPoints, this.currentHealthPoints, this.meleeProficiency);
	}

}