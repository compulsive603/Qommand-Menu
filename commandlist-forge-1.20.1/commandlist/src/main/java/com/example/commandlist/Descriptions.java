package com.example.commandlist;

import java.util.HashMap;
import java.util.Map;

/** Built-in descriptions for vanilla and Forge commands (Brigadier stores none). */
public final class Descriptions {
    private static final Map<String, String> VANILLA = new HashMap<>();
    private static final Map<String, String> FORGE = new HashMap<>();

    private Descriptions() {}

    private static void v(String names, String desc) {
        for (String n : names.split(" ")) VANILLA.put(n, desc);
    }

    static {
        v("advancement", "Give, remove or check player advancements.");
        v("attribute", "Read or modify an entity's attribute values.");
        v("ban", "Ban a player from the server.");
        v("ban-ip", "Ban an IP address from the server.");
        v("banlist", "Show the list of banned players or IPs.");
        v("bossbar", "Create and edit custom boss bars.");
        v("clear", "Remove items from a player's inventory.");
        v("clone", "Copy blocks from one area to another.");
        v("damage", "Deal damage to an entity.");
        v("data", "Get, merge or modify NBT data of blocks, entities and storage.");
        v("datapack", "Enable, disable or list data packs.");
        v("debug", "Start or stop a debug profiling session.");
        v("defaultgamemode", "Set the default game mode for new players.");
        v("deop", "Revoke operator status from a player.");
        v("difficulty", "Set or show the world difficulty.");
        v("effect", "Give or remove status effects.");
        v("enchant", "Enchant the item a player is holding.");
        v("execute", "Run another command with a changed executor, position or conditions.");
        v("experience xp", "Add, set or query experience points and levels.");
        v("fill", "Fill a region with a block.");
        v("fillbiome", "Change the biome of a region.");
        v("forceload", "Keep chunks loaded permanently.");
        v("function", "Run a data pack function.");
        v("gamemode", "Change a player's game mode.");
        v("gamerule", "Set or query game rules.");
        v("give", "Give items to players.");
        v("help", "Show help for commands.");
        v("item", "Replace or modify items in inventories and containers.");
        v("jfr", "Start or stop Java Flight Recorder profiling.");
        v("kick", "Kick a player from the server.");
        v("kill", "Kill entities (kills yourself if no target is given).");
        v("list", "List the players who are online.");
        v("locate", "Find the nearest structure, biome or point of interest.");
        v("loot", "Drop, give or insert loot from loot tables.");
        v("me", "Display a message about yourself.");
        v("msg tell w", "Send a private message to players.");
        v("op", "Grant operator status to a player.");
        v("pardon", "Unban a player.");
        v("pardon-ip", "Unban an IP address.");
        v("particle", "Create particles.");
        v("perf", "Capture server performance metrics.");
        v("place", "Place a feature, jigsaw, structure or template.");
        v("playsound", "Play a sound to players.");
        v("publish", "Open your singleplayer world to LAN.");
        v("recipe", "Give or take recipes from players.");
        v("reload", "Reload data packs, loot tables and functions from disk.");
        v("return", "Set the return value of a function.");
        v("ride", "Make entities mount or dismount each other.");
        v("save-all", "Save the world to disk.");
        v("save-off", "Disable automatic world saving.");
        v("save-on", "Enable automatic world saving.");
        v("say", "Broadcast a message to all players.");
        v("schedule", "Schedule a function to run later.");
        v("scoreboard", "Manage objectives, players and scores.");
        v("seed", "Show the world seed.");
        v("setblock", "Place or replace a single block.");
        v("setidletimeout", "Set the idle-kick timeout in minutes.");
        v("setworldspawn", "Set the world spawn point.");
        v("spawnpoint", "Set a player's spawn point.");
        v("spectate", "Spectate an entity while in spectator mode.");
        v("spreadplayers", "Teleport entities randomly within an area.");
        v("stop", "Stop the server.");
        v("stopsound", "Stop sounds that are playing for players.");
        v("summon", "Spawn an entity.");
        v("tag", "Add or remove tags on entities.");
        v("team", "Create and manage teams.");
        v("teammsg tm", "Send a message to your team.");
        v("teleport tp", "Teleport entities to a position or another entity.");
        v("tellraw", "Send a JSON-formatted message to players.");
        v("time", "Change or query the world time.");
        v("title", "Show on-screen titles and subtitles.");
        v("trigger", "Set a trigger objective (usable by non-operators).");
        v("weather", "Change the weather.");
        v("whitelist", "Manage the server whitelist.");
        v("worldborder", "Manage the world border.");

        FORGE.put("forge", "Forge utilities: tps, tag lists, entity/chunk tracking, generate, mods and more.");
        FORGE.put("config", "Show or open Forge/mod config files.");
    }

    public static boolean isVanilla(String name) { return VANILLA.containsKey(name); }
    public static boolean isForge(String name)   { return FORGE.containsKey(name); }
    public static String vanilla(String name)    { return VANILLA.get(name); }
    public static String forge(String name)      { return FORGE.get(name); }
}
