package com.catinbetween.minecraft.frequentflyer.mixin;

import com.catinbetween.minecraft.frequentflyer.FrequentFlyer;
import com.catinbetween.minecraft.frequentflyer.config.FrequentFlyerConfig;
import com.catinbetween.minecraft.frequentflyer.events.EventHandler;
import com.catinbetween.minecraft.frequentflyer.interfaces.FlyingPlayerEntity;
import com.mojang.authlib.GameProfile;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

import static com.catinbetween.minecraft.frequentflyer.events.EventHandler.FREQUENTFLYER;

@Mixin(ServerPlayerEntity.class)
public abstract class FrequentFlyerElytraMixin extends PlayerEntity implements FlyingPlayerEntity {

    //todo: fix fall damage if you are not flying and not creative ? has it always been like this?

    @Unique
    public abstract ServerWorld getWorld();

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
            UUID grantedByUUID = frequentflyer$getGrantedByPlayerUUID();
            if (player.getGameMode() == GameMode.SURVIVAL) {
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

            ItemStack chestSlot = getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
            if (!chestSlot.isEmpty() && chestSlot.getItem() == Items.ELYTRA ) {
                Item elytra = getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).getItem();
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : EnchantmentHelper.getEnchantments(chestSlot).getEnchantmentEntries()) {
                    Identifier enchant = ((RegistryEntry.Reference) entry.getKey()).registryKey().getValue();
                    level = entry.getIntValue();
                    if (FREQUENTFLYER.getValue().equals(enchant)) {
                        //here comes the fun
                        flightDamage--;
                        FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, "damage level: " + chestSlot.getDamage() + "/" + chestSlot.getMaxDamage() +  " flightdamage: " + flightDamage + "/" + flightDamageLimit);

                        if (flightDamage <= 0) {
                            if (elytra != null) {
                                chestSlot.damage(1, this);
                                FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, "Elytra damaged by flight, damage level: " + chestSlot.getDamage() + "/" + chestSlot.getMaxDamage());

                            }
                            flightDamageLimit = getY() <=  255 ? 3*level : level;
                            flightDamage = flightDamageLimit;
                        }

                    }
                }
                chestSlot.isDamageable();
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
        getAbilities().allowFlying = true;
        getAbilities().setFlySpeed(calculateFlySpeed(level));
        sendAbilitiesUpdate();

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

    @Inject(method = "changeGameMode", at = @At("RETURN"))
    private void onChangeGameMode( GameMode gameMode, CallbackInfoReturnable<Boolean> info ){
        if (gameMode != GameMode.SURVIVAL) {
            frequentflyer$allowFlight(1);
        }
    }


}