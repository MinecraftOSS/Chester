package org.moss.discord.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WordUtil {

    public static List<String> splitLines(String input) {
        return Arrays.asList(input.split("\\r?\\n"));
    }

    public static List<String> splitWords(String input) {
        return Arrays.asList(input.split("\\b"));
    }

    public static List<String> removeEmpty(List<String> strings) {
        return strings.stream()
            .map(String::trim)
            .filter(s -> !s.equalsIgnoreCase(""))
            .collect(Collectors.toList());
    }

    public static String joinWords(List<String> words) {        
        Iterator<String> i = removeEmpty(words).iterator();
        if (!i.hasNext()) return "";

        String result = i.next();

        while (i.hasNext()) {
            String next = i.next();
            result = result 
                + (Pattern.matches("\\p{Punct}", next) ? "" : " ") + next;
        }

        return result;
    }



}
