package com.catinbetween.minecraft.frequentflyer.interfaces;

public interface FlyingPlayerEntity {
    public void allowFlight(int level);
    public void disallowFlight();
    public void setFlying(boolean flying);
}