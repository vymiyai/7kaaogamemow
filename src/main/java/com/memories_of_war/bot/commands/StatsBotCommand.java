package com.memories_of_war.bot.commands;

import com.memories_of_war.bot.database.Unit;
import com.memories_of_war.bot.database.UnitRepository;
import com.memories_of_war.bot.exceptions.UserInformationException;
import com.memories_of_war.bot.utils.Colors;
import com.memories_of_war.bot.utils.Emotes;
import com.memories_of_war.bot.utils.Flags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.List;

@Component
public class StatsBotCommand implements IBotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsBotCommand.class);

	@Autowired
	private UnitRepository unitRepository;

	@Override
	public void execute(String[] tokenizedMessage, MessageReceivedEvent event) {

        String mention = event.getAuthor().mention();
		Long discordId = event.getAuthor().getLongID();
		
		try {
            Unit unit = unitRepository.findOne(discordId);

            // throw error if user is not defined.
            if (unit == null)
                throw new UserInformationException(": user not registered.");

            IUser author = event.getAuthor();
            IGuild guild = event.getGuild();

            List<IRole> roles = author.getRolesForGuild(guild);

            String emoteAndFactionName = "factionless";
            String factionFlag = "";
            Color factionColor = Color.BLACK;
            for (IRole role : roles) {
                switch (role.getName()) {
                    case "Shogun Empire":
                        emoteAndFactionName = Emotes.SE + " " + role.getName();
                        factionFlag = Flags.SE;
                        factionColor = Colors.SE;
                        break;
                    case "African Warlords":
                        emoteAndFactionName = Emotes.AW + " " + role.getName();
                        factionFlag = Flags.AW;
                        factionColor = Colors.AW;
                        break;
                    case "European Alliance":
                        emoteAndFactionName = Emotes.EA + " " + role.getName();
                        factionFlag = Flags.EA;
                        factionColor = Colors.EA;
                        break;
                    case "Soviet Union":
                        emoteAndFactionName = Emotes.SU + " " + role.getName();
                        factionFlag = Flags.SU;
                        factionColor = Colors.SU;
                        break;
                    case "Latin Junta":
                        emoteAndFactionName = Emotes.LJ + " " + role.getName();
                        factionFlag = Flags.LJ;
                        factionColor = Colors.LJ;
                        break;
                    case "United Republic":
                        emoteAndFactionName = Emotes.UR + " " + role.getName();
                        factionFlag = Flags.UR;
                        factionColor = Colors.UR;
                        break;

                    default:
                        // do nothing.
                }
            }

            String unitState;
            switch (unit.getUnitState()) {
                case IDLE:
                    unitState = "Idle";
                    break;
                case WAITING_IN_LOBBY:
                    unitState = "Waiting in Lobby";
                    break;
                case IN_COMBAT:
                    unitState = "In Combat";
                    break;
                case IN_MOVEMENT:
                    unitState = "Moving";
                    break;
                default:
                    unitState = "Idle";
            }

            if (emoteAndFactionName.equals("factionless")) {
                throw new UserInformationException(": Discord user does not belong to a faction.");
            }

            EmbedBuilder builder = new EmbedBuilder();

            builder.withColor(factionColor);

            builder.withAuthorIcon(factionFlag);
            //builder.withAuthorName(event.getAuthor().getDisplayName(event.getGuild()));
            builder.withAuthorName(unit.getUnitName());
            builder.withAuthorUrl(factionFlag);

            builder.withThumbnail(event.getAuthor().getAvatarURL());

            //builder.withTitle("Profile");
            //builder.withDescription(user.getDescription());

            //builder.appendField("Allegiance", emoteAndFactionName, false);

            //builder.appendField("Gems", ":gem: " + resources.getGems() + "/1000", true);
            //builder.appendField("Gems spent", ":gem: " + resources.getSpentGems(), true);

            //builder.appendField("Wealth", ":moneybag: " + resources.getGold() + "/10000", true);
            //builder.appendField("Wealth spent", ":moneybag: " + resources.getSpentGold(), true);

            builder.appendField("Health Points", unit.getCurrentHealthPoints() + "/" + unit.getHealthPoints(), true);
            builder.appendField("Current State", unitState, true);

            builder.appendField("Combat Proficiency", unit.getCombatProficienciesNames(), true);
            builder.appendField("Level", unit.getCombatProficienciesLevels(), true);

            builder.appendField("Enlisted since", unit.getCreationDate().toString(), false);

            event.getChannel().sendMessage(builder.build());

        } catch (UserInformationException e) {
            event.getChannel().sendMessage(mention + e.getMessage());
        } catch (Exception e) {
            event.getChannel().sendMessage(mention + ": could not retrieve stats.");

            String errorMessage = String.format("User %s in channel %s:", event.getAuthor().getName(), event.getChannel().getName());
            LOGGER.error(errorMessage, e);
        }
	}

	@Override
	public String getCommandName() {
		return "?stats";
	}

	@Override
	public String getCommandDescription() {
		return "Type ?stats to check your character stats.";
	}

}
