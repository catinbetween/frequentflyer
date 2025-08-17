package com.catinbetween.minecraft.frequentflyer.events;

import com.catinbetween.minecraft.frequentflyer.FrequentFlyer;
import com.catinbetween.minecraft.frequentflyer.config.FrequentFlyerConfig;
import net.luckperms.api.cacheddata.CachedPermissionData;

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

public class EventHandler {
    private static final RegistryKey<Enchantment> FREQUENTFLYER = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("catinbetween", "frequent_flyer"));

    private static final String SELF_FLY_PERMISSION = "frequentFlyer.ability.fly.self";
    private static final String OTHERS_FLY_PERMISSION = "frequentFlyer.ability.fly.others";

      public static void evaluateTickAllowFlight(ServerPlayerEntity player) {
        ItemStack chestStack = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
        if (player.getGameMode() == GameMode.SURVIVAL) {
            boolean hasElytra = chestStack.getItem() == Items.ELYTRA;
            Boolean allowFlight = false;
            int level = 1;

            if (hasElytra) {
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : EnchantmentHelper.getEnchantments(chestStack).getEnchantmentEntries()) {
                    Identifier enchant = ((RegistryEntry.Reference) entry.getKey()).registryKey().getValue();
                    level = entry.getIntValue();
                    if (FREQUENTFLYER.getValue().equals(enchant)) {
                        allowFlight = true;
                        break;
                    }
                }
            }

            FlyingPlayerEntity playerEntity = (FlyingPlayerEntity) player;
            boolean hasFlyPermission = hasAnyLuckPermsFlyPermission(player);
            if (hasElytra && allowFlight || hasFlyPermission) {
                FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, String.format("allowing flight!, haselytra: %s, allowFlight: %s, hasFlyPermission: %s", hasElytra, allowFlight, hasFlyPermission));
                playerEntity.frequentflyer$allowFlight(level);
            } else {
                FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, String.format("disallowing flight!, haselytra: %s, allowFlight: %s, hasFlyPermission: %s", hasElytra, allowFlight, hasFlyPermission));
                playerEntity.frequentflyer$disallowFlight();
            }
        }
    }

    private static boolean hasAnyLuckPermsFlyPermission(ServerPlayerEntity player) {

        LuckPerms luckPerms = getLuckPerms();

        if (luckPerms != null) {
            User luckpermsuser = luckPerms.getUserManager().getUser(player.getUuid());

            if (luckpermsuser == null)
                return false;

            FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, luckpermsuser.getUsername() + " has permissions: " + luckpermsuser.getCachedData().getPermissionData().getPermissionMap());

            CachedPermissionData permissionData = luckpermsuser.getCachedData()
                    .getPermissionData();

            return permissionData.checkPermission(SELF_FLY_PERMISSION).asBoolean();
        }
        return false;
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

}