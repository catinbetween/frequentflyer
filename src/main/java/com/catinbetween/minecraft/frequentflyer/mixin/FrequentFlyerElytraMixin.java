package com.catinbetween.minecraft.frequentflyer.mixin;

import com.catinbetween.minecraft.frequentflyer.FrequentFlyer;
import com.catinbetween.minecraft.frequentflyer.config.FrequentFlyerConfig;
import com.catinbetween.minecraft.frequentflyer.events.EventHandler;
import com.catinbetween.minecraft.frequentflyer.interfaces.FlyingPlayerEntity;
import com.mojang.authlib.GameProfile;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

import static com.catinbetween.minecraft.frequentflyer.events.EventHandler.FREQUENTFLYER;

@Mixin( ServerPlayer.class)
public abstract class FrequentFlyerElytraMixin extends Player implements FlyingPlayerEntity {

    //todo: fix fall damage if you are not flying and not creative ? has it always been like this?

    @Unique
    public abstract ServerLevel getWorld();

    @Unique
    private int tickCounter = 0;

    @Unique
    private int flightDamage = 0;

    @Unique
    private int flightDamageLimit = 3;

    @Unique
    private int level = 1;

    @Unique
    public boolean isFfFlightEnabled = false;

    @Unique
    public UUID grantedByPlayerUUID = null;

    @Unique
    public boolean canFlyWithElytra = false;

    @Override
    public boolean frequentflyer$getCanFlyWithElytra() {
        return canFlyWithElytra;
    }

    @Override
    public void frequentflyer$setCanFlyWithElytra(boolean boolCanFlyWithElytra) {
        canFlyWithElytra = boolCanFlyWithElytra;
    }

    @Override
    public boolean frequentflyer$isDiscoverable() {
        return true;
    }

    @Override
    public boolean frequentflyer$isTradeable() {
        return true;
    }

    @Override
    public boolean frequentflyer$canApplyAtEnchantingTable() {
        return true;
    }

    @Override
    public boolean frequentflyer$isAllowedOnBooks() {
        return true;
    }

    @Override
    public int frequentflyer$getLevel() {
        return level;
    }

    @Override
    public void frequentflyer$setLevel(int setLevel) {
        level = setLevel;
    }

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

    public FrequentFlyerElytraMixin( Level world, GameProfile profile) {
        super(world, profile);
    }


    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void onJump(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        EventHandler.evaluateTickAllowFlight(player);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickMovement(CallbackInfo ci) {
        if (tickCounter % 20 == 0) {
            ServerPlayer player = (ServerPlayer) (Object) this;
            EventHandler.evaluateTickAllowFlight(player);
            UUID grantedByUUID = frequentflyer$getGrantedByPlayerUUID();
            if (player.gameMode() == GameType.SURVIVAL) {
                if (frequentflyer$isFfFlightEnabled()) {
                    if (grantedByUUID != null) {
                        frequentflyer$allowFlight(level, grantedByUUID);
                    } else {
                        frequentflyer$allowFlight(level);
                        handleDurabilityCheck();
                    }
                } else {
                    frequentflyer$disallowFlight();
                }
            }
            tickCounter = 0;
        }
        tickCounter++;
    }

    @Unique
    private void handleDurabilityCheck() {
        FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, "Handling durability check for flight.");

        if (getAbilities().flying) {

            ItemStack chestSlot = getItemBySlot( net.minecraft.world.entity.EquipmentSlot.CHEST);
            if (!chestSlot.isEmpty() && chestSlot.getItem() == Items.ELYTRA ) {
                Item elytra = getItemBySlot( net.minecraft.world.entity.EquipmentSlot.CHEST).getItem();
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentHelper.getEnchantmentsForCrafting(chestSlot).entrySet()) {
                    Identifier enchant = ((Holder.Reference) entry.getKey()).key().identifier();
                    level = entry.getIntValue();
                    if (FREQUENTFLYER.identifier().equals(enchant)) {
                        //here comes the fun
                        flightDamage--;
                        FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, "damage level: " + chestSlot.getDamageValue() + "/" + chestSlot.getMaxDamage() +  " flightdamage: " + flightDamage + "/" + flightDamageLimit);

                        if (flightDamage <= 0) {
                            if (elytra != null) {
                                chestSlot.hurtWithoutBreaking(1, this);
                                FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, "Elytra damaged by flight, damage level: " + chestSlot.getDamageValue() + "/" + chestSlot.getMaxDamage());

                            }
                            flightDamageLimit = getY() <=  255 ? 3*level : level;
                            flightDamage = flightDamageLimit;
                        }

                    }
                }
                chestSlot.isDamageableItem();
            }
        }
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
        getAbilities().mayfly = true;
        getAbilities().setFlyingSpeed(calculateFlySpeed(level));
        onUpdateAbilities();

    }

    @Unique
    private float calculateFlySpeed(int level) {
        FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, "Calculated fly speed: " + (FrequentFlyerConfig.INSTANCE.defaultFlySpeed + level * FrequentFlyerConfig.INSTANCE.flySpeedStep));
        return FrequentFlyerConfig.INSTANCE.defaultFlySpeed  + level * FrequentFlyerConfig.INSTANCE.flySpeedStep;
    }


    @Override
    public void frequentflyer$disallowFlight() {
        frequentflyer$setIsFfFlightEnabled(false);
        frequentflyer$setGrantedByPlayerUUID(null);

        getAbilities().mayfly = false;

        if (getAbilities().flying) {
            addEffect(new MobEffectInstance( MobEffects.SLOW_FALLING, FrequentFlyerConfig.INSTANCE.slowFallingTime * 20));
            getAbilities().flying = false;
        }
        onUpdateAbilities();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void onWriteCustomData( ValueOutput view, CallbackInfo ci) {
        if (view instanceof TagValueOutput nbtWriteView) {
            nbtWriteView.putBoolean("frequentFlyerFlightEnabled", isFfFlightEnabled);
            if (grantedByPlayerUUID != null) {
                nbtWriteView.putString("frequentFlyerGrantedBy", grantedByPlayerUUID.toString());
            } else {
                nbtWriteView.discard("frequentFlyerGrantedBy");
            }
        }
    }


    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void onReadCustomDataFromTag( ValueInput view, CallbackInfo ci) {
        if (view instanceof TagValueInput nbtReadView) {
            isFfFlightEnabled = nbtReadView.getBooleanOr("frequentFlyerFlightEnabled", false);
            String grantedByString = nbtReadView.getStringOr("frequentFlyerGrantedBy", null);
            grantedByPlayerUUID = (grantedByString != null && !grantedByString.isEmpty()) ? UUID.fromString(grantedByString) : null;
        }
    }

    @Inject(method = "setGameMode", at = @At("RETURN"))
    private void onChangeGameMode( GameType gameMode, CallbackInfoReturnable<Boolean> info ){
        if (gameMode != GameType.SURVIVAL) {
            frequentflyer$allowFlight(1);
        }
    }


}
