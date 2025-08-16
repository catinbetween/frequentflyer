package com.catinbetween.minecraft.frequentflyer;


import com.catinbetween.minecraft.frequentflyer.config.FrequentFlyerConfig;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;

public class FrequentFlyer implements ModInitializer{

	public static final String MOD_ID = "frequentflyer";
	public static final String MOD_NAME = "FrequentFlyer";
	public static final String MOD_VER = "1.0.0";

	public static Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialize() {
		log(Level.INFO, "version " + MOD_VER);
		FrequentFlyerConfig.loadConfig();
		log(Level.INFO, "Initialized successfully.");
	}

	public static void log(Level level, String message){
		if( FrequentFlyerConfig.INSTANCE.log == null || level.isMoreSpecificThan( FrequentFlyerConfig.INSTANCE.log))
			LOGGER.log(level, "["+MOD_NAME+"] " + message);
	}

}