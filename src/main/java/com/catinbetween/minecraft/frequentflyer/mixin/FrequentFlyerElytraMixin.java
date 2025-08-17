package com.catinbetween.minecraft.frequentflyer.mixin;

import com.catinbetween.minecraft.frequentflyer.FrequentFlyer;
import com.catinbetween.minecraft.frequentflyer.config.FrequentFlyerConfig;
import com.catinbetween.minecraft.frequentflyer.events.EventHandler;
import com.catinbetween.minecraft.frequentflyer.interfaces.FlyingPlayerEntity;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public abstract class FrequentFlyerElytraMixin extends PlayerEntity implements FlyingPlayerEntity {

    //todo: add durability check

    //todo: check if fuji truly gives everything that essentialcommands gives

    //todo: fix fall damage if you are not flying and not creative
    // actually use isFFenabled() now, just set the value instead of callling the functions, and call allow/dissallow every tick which check the value

    //todo: enforce required advancement?

    @Unique
    private int tickCounter = 0;

    @Unique
    public boolean isFfFlightEnabled = false;

    @Unique
    public UUID grantedByPlayerUUID = null;

    @Override
    public boolean frequentflyer$isFfFlightEnabled() {
        return isFfFlightEnabled;
    }

    @Override
    public UUID frequentflyer$getGrantedByPlayerUUID() {
        return grantedByPlayerUUID;
    }

    @Override
    public void frequentflyer$setIsFfFlightEnabled(boolean setIsFfFlightEnabled) {
        isFfFlightEnabled = setIsFfFlightEnabled;
    }

    @Override
    public void frequentflyer$setGrantedByPlayerUUID(UUID setGrantedByPlayerUUID) {
        grantedByPlayerUUID = setGrantedByPlayerUUID;
    }

    public FrequentFlyerElytraMixin(World world, GameProfile profile) {
        super(world, profile);
    }


    @Inject(method = "jump", at = @At("HEAD"))
    private void onJump(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        EventHandler.evaluateTickAllowFlight(player);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickMovement(CallbackInfo ci) {
        if (tickCounter % 20 == 0) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            EventHandler.evaluateTickAllowFlight(player);
            tickCounter = 0;
        }
        tickCounter++;
    }

    @Override
    public void frequentflyer$allowFlight(int level) {
        frequentflyer$allowFlight(level, null);
    }

    @Override
    public void frequentflyer$allowFlight(int level, UUID grandtedByPlayer) {
        frequentflyer$setIsFfFlightEnabled(true);
        if (grandtedByPlayer != null) {
            frequentflyer$setGrantedByPlayerUUID(grandtedByPlayer);
            FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, "UUID: " + grandtedByPlayer);
        }
        getAbilities().allowFlying = true;
        getAbilities().setFlySpeed(level * FrequentFlyerConfig.INSTANCE.defaultFlySpeed);
        sendAbilitiesUpdate();

    }


    @Override
    public void frequentflyer$disallowFlight() {
        frequentflyer$setIsFfFlightEnabled(false);
        frequentflyer$setGrantedByPlayerUUID(null);

        getAbilities().allowFlying = false;

        if (getAbilities().flying) {
            addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, FrequentFlyerConfig.INSTANCE.slowFallingTime * 20));
            getAbilities().flying = false;
        }
        sendAbilitiesUpdate();
    }

    @Inject(method = "writeCustomData", at = @At("RETURN"))
    private void onWriteCustomData(WriteView view, CallbackInfo ci) {
        if (view instanceof NbtWriteView nbtWriteView) {
            nbtWriteView.putBoolean("frequentFlyerFlightEnabled", isFfFlightEnabled);
            if (grantedByPlayerUUID != null) {
                nbtWriteView.putString("frequentFlyerGrantedBy", grantedByPlayerUUID.toString());
            } else {
                nbtWriteView.remove("frequentFlyerGrantedBy");
            }
        }
    }


    @Inject(method = "readCustomData", at = @At("RETURN"))
    private void onReadCustomDataFromTag(ReadView view, CallbackInfo ci) {
        if (view instanceof NbtReadView nbtReadView) {
            isFfFlightEnabled = nbtReadView.getBoolean("frequentFlyerFlightEnabled", false);
            String grantedByString = nbtReadView.getString("frequentFlyerGrantedBy", null);
            grantedByPlayerUUID = (grantedByString != null && !grantedByString.isEmpty()) ? UUID.fromString(grantedByString) : null;
        }
    }


}