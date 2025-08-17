package com.catinbetween.minecraft.frequentflyer.interfaces;

import net.minecraft.server.network.ServerPlayerEntity;

public interface FlyingPlayerEntity {
    public void frequentflyer$allowFlight(int level);
    public void frequentflyer$allowFlight(int level, ServerPlayerEntity grandtedByPlayer);
    public void frequentflyer$disallowFlight();
    public void setFlying(boolean flying);
}