package com.catinbetween.minecraft.frequentflyer.interfaces;

import java.util.UUID;

public interface FlyingPlayerEntity {
    void frequentflyer$allowFlight(int level);

    void frequentflyer$allowFlight(int level, UUID grandtedByPlayer);

    void frequentflyer$disallowFlight();

    boolean isFfFlightEnabled = false;
    UUID grantedByPlayerUUID = null;

    boolean frequentflyer$isFfFlightEnabled();

    void frequentflyer$setIsFfFlightEnabled(boolean isFfFlightEnabled);

    UUID frequentflyer$getGrantedByPlayerUUID();

    void frequentflyer$setGrantedByPlayerUUID(UUID grantedByPlayerUUID);
}