package com.catinbetween.minecraft.frequentflyer.interfaces;

import java.util.UUID;

public interface FlyingPlayerEntity {
    void frequentflyer$allowFlight(int level);

    void frequentflyer$allowFlight(int level, UUID grandtedByPlayer);

    void frequentflyer$disallowFlight();

    boolean frequentflyer$isFfFlightEnabled();

    void frequentflyer$setIsFfFlightEnabled(boolean isFfFlightEnabled);

    UUID frequentflyer$getGrantedByPlayerUUID();

    void frequentflyer$setGrantedByPlayerUUID(UUID grantedByPlayerUUID);

    void frequentflyer$setLevel(int level);

    boolean frequentflyer$isDiscoverable();

    boolean frequentflyer$isTradeable();

    boolean frequentflyer$canApplyAtEnchantingTable();

    boolean frequentflyer$isAllowedOnBooks();

    int frequentflyer$getLevel();

    void frequentflyer$setCanFlyWithElytra(boolean boolCanFlyWithElytra);

    boolean frequentflyer$getCanFlyWithElytra();
}