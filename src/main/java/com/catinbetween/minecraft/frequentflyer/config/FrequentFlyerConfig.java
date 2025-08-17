package com.catinbetween.minecraft.frequentflyer.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.catinbetween.minecraft.frequentflyer.FrequentFlyer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Level;
import net.minecraft.util.Identifier;


public class FrequentFlyerConfig {
    public static FrequentFlyerConfig INSTANCE = new FrequentFlyerConfig();

    private static final File configDir = new File("config");
    private static final File configFile = new File("config/" + FrequentFlyer.MOD_ID + "_config.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().serializeNulls().create();


    public String[] advancementsRequired = new String[]{"minecraft:end/elytra"};
    public transient Identifier[] advancements;
    public int slowFallingTime = 10;
    public float defaultFlySpeed = 0.016F;
    public String logLevel = "INFO";
    public transient Level log;

    public static void loadConfig(){
        try{
            configDir.mkdirs();
            if(configFile.createNewFile()){
                FileWriter fw = new FileWriter(configFile);
                fw.append(gson.toJson(INSTANCE));
                fw.close();
                FrequentFlyer.log(Level.INFO, "Default config generated.");
            }else{
                FileReader fr = new FileReader(configFile);
                INSTANCE = gson.fromJson(fr, FrequentFlyerConfig.class);
                fr.close();
                INSTANCE.generateTransients();
                FrequentFlyer.log(Level.INFO, "FrequentFlyerConfig loaded.");
                return;
            }
        }catch(Exception e){
            FrequentFlyer.log(Level.WARN, "Error loading config, using default values.");
        }
        INSTANCE.generateTransients();
    }

    public static void saveConfigs(){
        try{
            configDir.mkdirs();
            FileWriter fw = new FileWriter(configFile);
            fw.append(gson.toJson(INSTANCE));
            fw.close();
            FrequentFlyer.log(Level.INFO, "FrequentFlyerConfig saved.");
        }catch(Exception e){
            FrequentFlyer.log(Level.ERROR, "Error saving config");
        }
    }

    private void generateTransients(){
        advancements = new Identifier[advancementsRequired.length];
        for(int i = 0; i < advancements.length; i++){
            advancements[i] = Identifier.of(advancementsRequired[i]);
        }
        log = Level.getLevel(logLevel);
    }

    public static int meow(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("meow"), false);
        return 1;
    }

    public static int commandReload(CommandContext<ServerCommandSource> context) {

        context.getSource().sendFeedback(() -> Text.literal("FrequentFlyer: Reloading config..."), false);
        FrequentFlyer.log(Level.INFO, "Reloading config...");
        loadConfig();
        context.getSource().sendFeedback(() -> Text.literal("FrequentFlyer: Config reloaded."), false);
        FrequentFlyer.log(Level.INFO, "Config reloaded.");
        return 1;
    }

    public static int commandDebug(CommandContext<ServerCommandSource> context) {
        String value = context.getArgument("value", String.class);
        FrequentFlyerConfig.INSTANCE.logLevel = value;
        FrequentFlyerConfig.INSTANCE.log = Level.getLevel(FrequentFlyerConfig.INSTANCE.logLevel);

        context.getSource().sendFeedback(() -> Text.literal("FrequentFlyer: Log level set to " + value), false);
        FrequentFlyer.log(Level.INFO, "Log level set to " + value);
        return 1;
    }
}