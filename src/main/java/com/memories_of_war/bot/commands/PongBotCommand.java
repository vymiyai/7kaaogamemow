package com.memories_of_war.bot.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

@Component
public class PongBotCommand implements IBotCommand {

    @Override
    public void execute(String[] tokenizedMessage, MessageReceivedEvent event) {
        StringBuilder response = new StringBuilder();
        response.append("Pong.");
        event.getChannel().sendMessage(response.toString());
    }

    @Override
    public String getCommandName() {
        return "?ping";
    }

    @Override
    public String getCommandDescription() {
        return "Type ?ping to pong.";
    }

}
