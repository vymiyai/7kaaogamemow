package com.memories_of_war.bot.commands;

import com.memories_of_war.bot.database.Unit;
import com.memories_of_war.bot.utils.Faction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;

import java.util.Objects;

@Component
public class EnlistBotCommand implements IBotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnlistBotCommand.class);

    @Autowired
    protected com.memories_of_war.bot.database.UnitRepository UnitRepository;

    private boolean isFirstCharacter(Long discordId) throws Exception {
        if (this.UnitRepository.findOne(discordId) == null)
            return true;
        else
            throw new Exception("User already has a registered unit.");
    }

    @Override
    @Transactional
    public void execute(String[] tokenizedMessage, MessageReceivedEvent event) {

        String mention = event.getAuthor().mention();
        Long discordId = event.getAuthor().getLongID();

        try {
            this.isFirstCharacter(discordId);


            int meleeProficiency = 1;						// AW SE
            int rifleProficiency = 1;                       // UR LJ
            int smgProficiency = 1;                         // SU EA
            int machineGunProficiency = 1;                  // EA UR
            int projectorProficiency = 1;					// SE LJ
            int atRifleProficiency = 1;                     // SE SU
            int rocketLauncherProficiency = 1;				// EA AW
            int sniperRifleProficiency = 1;					// LJ SU
            int shotgunProficiency = 1;						// AW UR
            Faction faction = null;
            for (IRole role : event.getAuthor().getRolesForGuild(event.getGuild())) {
                switch (role.getName()) {
                    case "Shogun Empire":
                        faction = Faction.SHOGUN_EMPIRE;
                        meleeProficiency = 5;
                        projectorProficiency = 3;
                        atRifleProficiency = 3;
                        break;
                    case "African Warlords":
                        faction = Faction.AFRICAN_WARLORDS;
                        shotgunProficiency = 5;
                        meleeProficiency = 3;
                        rocketLauncherProficiency = 3;
                        break;
                    case "European Alliance":
                        faction = Faction.EUROPEAN_ALLIANCE;
                        machineGunProficiency = 5;
                        smgProficiency = 3;
                        rocketLauncherProficiency = 3;
                        break;
                    case "Soviet Union":
                        faction = Faction.SOVIET_UNION;
                        smgProficiency = 5;
                        atRifleProficiency = 3;
                        sniperRifleProficiency = 3;
                        break;
                    case "Latin Junta":
                        faction = Faction.LATIN_JUNTA;
                        sniperRifleProficiency = 5;
                        rifleProficiency = 3;
                        projectorProficiency = 3;
                        break;
                    case "United Republic":
                        faction = Faction.UNITED_REPUBLIC;
                        rifleProficiency = 5;
                        machineGunProficiency = 3;
                        shotgunProficiency = 3;
                        break;
                    default:
                        // do nothing.
                }
            }

            if(Objects.isNull(faction)) {
                throw new Exception("Discord user does not belong to a faction.");
            }

            // create the new user.
            Unit unit = new Unit(discordId, event.getAuthor().getDisplayName(event.getGuild()), faction);
            unit.setMeleeProficiency(meleeProficiency);
            unit.setRifleProficiency(rifleProficiency);
            unit.setSmgProficiency(smgProficiency);
            unit.setMachineGunProficiency(machineGunProficiency);
            unit.setProjectorProficiency(projectorProficiency);
            unit.setAtRifleProficiency(atRifleProficiency);
            unit.setRocketLauncherProficiency(rocketLauncherProficiency);
            unit.setSniperRifleProficiency(sniperRifleProficiency);
            unit.setShotgunProficiency(shotgunProficiency);

            this.UnitRepository.save(unit);

            event.getChannel().sendMessage(mention + ": character created.");

        } catch (Exception e) {
            event.getChannel().sendMessage(mention + ": could not register character.");

            String errorMessage = String.format("User %s in channel %s:", event.getAuthor().getName(), event.getChannel().getName());
            LOGGER.error(errorMessage, e);
        }
    }

    @Override
    public String getCommandName() {
        return "?enlist";
    }

    @Override
    public String getCommandDescription() {
        return "Type ?enlist to register an ARENAgma character.";
    }

}
