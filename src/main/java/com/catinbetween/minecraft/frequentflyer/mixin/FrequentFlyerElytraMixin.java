package com.catinbetween.minecraft.frequentflyer.mixin;

import com.catinbetween.minecraft.frequentflyer.config.FrequentFlyerConfig;
import com.catinbetween.minecraft.frequentflyer.events.EventHandler;
import com.catinbetween.minecraft.frequentflyer.interfaces.FlyingPlayerEntity;
import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class FrequentFlyerElytraMixin extends PlayerEntity implements FlyingPlayerEntity {

    //todo: add durability check
    //todo: disable check when not in survival mode or if it has permissions etc
    //todo: add flight speed
    //todo: alloow required advancement?

    private int tickCounter = 0;

    public FrequentFlyerElytraMixin(World world, GameProfile profile){
        super(world, profile);
    }


    @Inject( method = "jump" , at = @At("HEAD") )
    private void onJump(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        EventHandler.evaluateAllowFlight( player );
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
    public void allowFlight() {
        getAbilities().allowFlying = true;
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


    @Shadow
    public void sendAbilitiesUpdate(){}

}