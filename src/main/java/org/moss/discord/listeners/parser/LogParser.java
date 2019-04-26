package org.moss.discord.listeners.parser;

import org.javacord.api.entity.message.Message;

import java.util.Scanner;

public interface LogParser {

    void evaluate(Message message, Scanner log);

}
