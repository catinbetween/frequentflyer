package com.catinbetween.minecraft.frequentflyer.events;

import com.catinbetween.minecraft.frequentflyer.FrequentFlyer;
import com.catinbetween.minecraft.frequentflyer.config.FrequentFlyerConfig;
import com.catinbetween.minecraft.frequentflyer.interfaces.FlyingPlayerEntity;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.Level;

import java.util.UUID;

public class EventHandler {
    public static final RegistryKey<Enchantment> FREQUENTFLYER = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("catinbetween", "frequent_flyer"));

    private static final String SELF_FLY_PERMISSION = "frequentFlyer.ability.fly.self";
    private static final String OTHERS_FLY_PERMISSION = "frequentFlyer.ability.fly.others";
    private static final String MAIN_COMMAND_PERMISSION = "frequentFlyer.command.main";
    private static final String SILLY_COMMAND_PERMISSION = "frequentFlyer.command.silly";


    public static void evaluateTickAllowFlight(ServerPlayerEntity player) {
        if (player.getGameMode() == GameMode.SURVIVAL) {
            FlyingPlayerEntity flyingPlayerEntity = (FlyingPlayerEntity) player;
            UUID grantedByPlayerUUID = flyingPlayerEntity.frequentflyer$getGrantedByPlayerUUID();

            ItemStack chestStack = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
            boolean hasElytra = chestStack.getItem() == Items.ELYTRA;
            boolean canFlyWithElytra = false;
            flyingPlayerEntity.frequentflyer$setCanFlyWithElytra(false);
            int level = 1;

            if (hasElytra) {
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : EnchantmentHelper.getEnchantments(chestStack).getEnchantmentEntries()) {
                    Identifier enchant = ((RegistryEntry.Reference) entry.getKey()).registryKey().getValue();
                    level = entry.getIntValue();
                    if (FREQUENTFLYER.getValue().equals(enchant)) {
                        if (chestStack.getDamage() <= chestStack.getMaxDamage() - 32) {
                            canFlyWithElytra = true;
                        } else {
                            FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, String.format("Elytra is too damaged for flight: %s/%s", chestStack.getDamage(), chestStack.getMaxDamage()));
                        }
                        break;
                    }
                }
            }

            if (hasElytra && canFlyWithElytra) {
                flyingPlayerEntity.frequentflyer$setCanFlyWithElytra(true);
                flyingPlayerEntity.frequentflyer$setIsFfFlightEnabled(true);
                flyingPlayerEntity.frequentflyer$setLevel(level);
                FrequentFlyer.log(Level.DEBUG, String.format("allowing flight for %s by having elytra!, haselytra: %s, canFlyWithElytra: %s", player.getName().getString(), true, canFlyWithElytra));

            } else if (grantedByPlayerUUID != null) {
                flyingPlayerEntity.frequentflyer$setIsFfFlightEnabled(true);
                flyingPlayerEntity.frequentflyer$setLevel(level);
                FrequentFlyer.log(Level.DEBUG, String.format("allowing flight for %s by having it granted!, haselytra: %s, canFlyWithElytra: %s, grantedBy: %s", player.getName().getString(), hasElytra, canFlyWithElytra, grantedByPlayerUUID));

            } else {
                flyingPlayerEntity.frequentflyer$setIsFfFlightEnabled(false);
                flyingPlayerEntity.frequentflyer$setLevel(level);
                FrequentFlyer.log(Level.DEBUG, String.format("disallowing flight for %s!", player.getName().getString()));
            }

        }

    }

    private static LuckPerms getLuckPerms() {
        try {
            return LuckPermsProvider.get();
        } catch (NoClassDefFoundError e) {
            return null;
        }
    }

    public static boolean hasFlyCommandPermission(ServerPlayerEntity player, ServerPlayerEntity target) {
        LuckPerms luckPerms = getLuckPerms();
        if (luckPerms == null) {
            return false;
        }

        User luckpermsuser = luckPerms.getUserManager().getUser(player.getUuid());
        if (luckpermsuser == null)
            return false;
        return luckpermsuser.getCachedData().getPermissionData().checkPermission(player.getUuid().equals(target.getUuid()) ? SELF_FLY_PERMISSION : OTHERS_FLY_PERMISSION).asBoolean();

    }

    public static boolean hasMainCommandPermission(ServerPlayerEntity player) {
        LuckPerms luckPerms = getLuckPerms();
        if (luckPerms == null) {
            return false;
        }

        User luckpermsuser = luckPerms.getUserManager().getUser(player.getUuid());
        if (luckpermsuser == null)
            return false;
        return luckpermsuser.getCachedData().getPermissionData().checkPermission( MAIN_COMMAND_PERMISSION).asBoolean();

    }

    public static boolean hasSillyCommandPermission(ServerPlayerEntity player) {
        LuckPerms luckPerms = getLuckPerms();
        if (luckPerms == null) {
            return false;
        }

        User luckpermsuser = luckPerms.getUserManager().getUser(player.getUuid());
        if (luckpermsuser == null)
            return false;
        return luckpermsuser.getCachedData().getPermissionData().checkPermission( SILLY_COMMAND_PERMISSION).asBoolean();

    }

}