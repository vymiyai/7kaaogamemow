package com.memories_of_war.bot.exceptions;

public class UserInformationException extends Exception {
    public UserInformationException(String informationToUser) {
        super(informationToUser);
    }
}
