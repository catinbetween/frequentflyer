package com.catinbetween.minecraft.frequentflyer.config;

import com.catinbetween.minecraft.frequentflyer.FrequentFlyer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


public class FrequentFlyerConfig {
    private static final File configDir = new File("config");
    private static final File configFile = new File("config/" + FrequentFlyer.MOD_ID + "_config.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().serializeNulls().create();
    public static FrequentFlyerConfig INSTANCE = new FrequentFlyerConfig();
    public String[] advancementsRequired = new String[]{"minecraft:end/elytra"};
    public transient Identifier[] advancements;
    public boolean enableFlyCommand = false;
    public int slowFallingTime = 10;
    public float defaultFlySpeed = 0.04F;
    public float flySpeedStep = 0.005F;
    public String logLevel = "INFO";
    public transient Level log;

    public static void loadConfig() {
        try {
            configDir.mkdirs();
            if (configFile.createNewFile()) {
                FileWriter fw = new FileWriter(configFile);
                fw.append(gson.toJson(INSTANCE));
                fw.close();
                FrequentFlyer.log(Level.INFO, "Default config generated.");
            } else {
                FileReader fr = new FileReader(configFile);
                INSTANCE = gson.fromJson(fr, FrequentFlyerConfig.class);
                fr.close();
                INSTANCE.generateTransients();
                FrequentFlyer.log(Level.INFO, "FrequentFlyerConfig loaded.");
                return;
            }
        } catch (Exception e) {
            FrequentFlyer.log(Level.WARN, "Error loading config, using default values.");
        }
        INSTANCE.generateTransients();
    }

    public static void saveConfigs() {
        try {
            configDir.mkdirs();
            FileWriter fw = new FileWriter(configFile);
            fw.append(gson.toJson(INSTANCE));
            fw.close();
            FrequentFlyer.log(Level.INFO, "FrequentFlyerConfig saved.");
        } catch (Exception e) {
            FrequentFlyer.log(Level.ERROR, "Error saving config");
        }
    }

    public static int meow(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("meow"), false);
        return 1;
    }

    public static int commandReload(CommandContext<CommandSourceStack> context) {

        context.getSource().sendSuccess(() -> Component.literal("FrequentFlyer: Reloading config..."), false);
        FrequentFlyer.log(Level.INFO, "Reloading config...");
        loadConfig();
        context.getSource().sendSuccess(() -> Component.literal("FrequentFlyer: Config reloaded."), false);
        FrequentFlyer.log(Level.INFO, "Config reloaded.");
        return 1;
    }

    public static int commandDebug(CommandContext<CommandSourceStack> context) {
        String value = context.getArgument("value", String.class);
        FrequentFlyerConfig.INSTANCE.logLevel = value;
        FrequentFlyerConfig.INSTANCE.log = Level.getLevel(FrequentFlyerConfig.INSTANCE.logLevel);

        context.getSource().sendSuccess(() -> Component.literal("FrequentFlyer: Log level set to " + value), false);
        FrequentFlyer.log(Level.INFO, "Log level set to " + value);
        return 1;
    }

    private void generateTransients() {
        advancements = new Identifier[advancementsRequired.length];
        for (int i = 0; i < advancements.length; i++) {
            advancements[i] = Identifier.parse(advancementsRequired[i]);
        }
        log = Level.getLevel(logLevel);
    }
}
