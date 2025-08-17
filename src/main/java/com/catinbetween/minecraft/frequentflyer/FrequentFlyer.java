package com.catinbetween.minecraft.frequentflyer;


import com.catinbetween.minecraft.frequentflyer.command.CommandUtil;
import com.catinbetween.minecraft.frequentflyer.command.FlyCommand;
import com.catinbetween.minecraft.frequentflyer.config.FrequentFlyerConfig;

import com.catinbetween.minecraft.frequentflyer.events.EventHandler;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FrequentFlyer implements ModInitializer {

    public static final String MOD_ID = "frequentflyer";
    public static final String MOD_NAME = "FrequentFlyer";
    public static final String MOD_VER = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        log(Level.INFO, "version " + MOD_VER);
        FrequentFlyerConfig.loadConfig();
        log(Level.INFO, "Initialized successfully.");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("frequent_flyer")
                    .then(literal("reload").executes(FrequentFlyerConfig::commandReload)).requires(source -> source.hasPermissionLevel(4))
                    .then(literal("meow").executes(FrequentFlyerConfig::meow)).requires(source -> source.hasPermissionLevel(0))
                    .then(literal("debug").then(argument("value", StringArgumentType.greedyString())
                            .executes(FrequentFlyerConfig::commandDebug))).requires(source -> source.hasPermissionLevel(4))
            );
            dispatcher.register(literal("fly")
                    .requires(source -> EventHandler.hasFlyCommandPermission(source.getPlayer(), source.getPlayer()))
                    .then(argument("flight_enabled", BoolArgumentType.bool())
                            .executes(new FlyCommand()))
                    .then(argument("flight_enabled", BoolArgumentType.bool())
                            .then(CommandUtil.targetPlayerArgument()
                                    .executes(new FlyCommand())))
            );
        });
    }

    public static void log(Level level, String message) {
        if (FrequentFlyerConfig.INSTANCE.log == null || level.isMoreSpecificThan(FrequentFlyerConfig.INSTANCE.log))
            LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }
}