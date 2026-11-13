package com.example.commandlist;

/** One root command with everything the menu shows about it. */
public record CommandInfo(String name, String usage, long firstSeen,
                          String modId, String modName, String description,
                          boolean needsArgs) {
}
