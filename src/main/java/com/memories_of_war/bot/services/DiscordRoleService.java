package com.memories_of_war.bot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

@Service
public class DiscordRoleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordRoleService.class);

    private final String SQUAD_MEMBER = "Squad Member";

    public void addSquadRole(IGuild guild, IUser user) {
        IRole squadMember = guild.getRolesByName(SQUAD_MEMBER).get(0);
        user.addRole(squadMember);
        LOGGER.info("Added Squad role to user [{}]", user.getLongID());
    }

    public void removeSquadRole(IGuild guild, IUser user) {
        IRole squadMember = guild.getRolesByName(SQUAD_MEMBER).get(0);
        user.removeRole(squadMember);
        LOGGER.info("Removed Squad role from user [{}]", user.getLongID());
    }
}
