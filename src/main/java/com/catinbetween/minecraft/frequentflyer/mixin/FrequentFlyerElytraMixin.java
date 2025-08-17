package com.catinbetween.minecraft.frequentflyer.mixin;

import com.catinbetween.minecraft.frequentflyer.config.FrequentFlyerConfig;
import com.catinbetween.minecraft.frequentflyer.events.EventHandler;
import com.catinbetween.minecraft.frequentflyer.interfaces.FlyingPlayerEntity;
import com.mojang.authlib.GameProfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.luckperms.api.LuckPerms;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class FrequentFlyerElytraMixin extends PlayerEntity implements FlyingPlayerEntity {
    private static final Logger log = LoggerFactory.getLogger( FrequentFlyerElytraMixin.class );

    //todo: add durability check

    //todo: fly command completely disabled, check if fuji truly gives everything that essentialcommands gives
    // is it then necessary to check for permissions at all?
    // configurable?

    //todo: make flight speed configurable

    //todo: allow required advancement?
    private static final float DEFAULT_FLY_SPEED = 0.05F;

    private int tickCounter = 0;

    public FrequentFlyerElytraMixin(World world, GameProfile profile){
        super(world, profile);
    }




    @Inject( method = "jump" , at = @At("HEAD") )
    private void onJump(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        EventHandler.evaluateAllowFlight( player);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickMovement(CallbackInfo ci) {
        if (tickCounter % 20 == 0) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            EventHandler.evaluateAllowFlight( player );
            tickCounter = 0;
        }
        tickCounter++;
    }

    @Override
    public void allowFlight(int level) {
        getAbilities().allowFlying = true;
        getAbilities().setFlySpeed( level * DEFAULT_FLY_SPEED );
        sendAbilitiesUpdate();
    }

    @Override
    public void disallowFlight() {
        getAbilities().allowFlying = false;

        if (getAbilities().flying) {
            addStatusEffect( new StatusEffectInstance( StatusEffects.SLOW_FALLING, FrequentFlyerConfig.INSTANCE.slowFallingTime * 20 ) );
            getAbilities().flying = false;
        }
        sendAbilitiesUpdate();
    }

}