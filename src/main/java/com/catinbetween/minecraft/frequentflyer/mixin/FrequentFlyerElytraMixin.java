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

    //todo: add durability check

    //todo: check if fuji truly gives everything that essentialcommands gives
    /*todo: implement own fly Command, with making oneself fly and others
        - write custom nbt tag to player for toggling flight and check for that before calculating all the rest at every tick
            - default: false for all players (like when it's not written, then write false)
        - tick check process:
            - if fly nbt attribute not there -> write it to false and go to false branch
            - check for nbt attribute
                - if true => then check for self fly command permission
                    - if true, skip elytra and enchantment checks
                    - if it's false, then check for elytra and enchantment checks
                - if false => do nothing, just return
        - fly command:
            - check if there is permission for the target
                -if true:
                    - check nbt toggle attribute for target;  if not there, write it to false
                    - if true, then toggle fly nbt attribute for target , set allowfly to true, set fly speed
                    - if false, then  toogle fly attribute for target, set allowfly to false, flying to false with falling effect
                -if false: do nothing, just return
    */

    //todo: enforce required advancement?

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
        getAbilities().setFlySpeed( level * FrequentFlyerConfig.INSTANCE.defaultFlySpeed );
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