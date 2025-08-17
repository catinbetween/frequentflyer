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
    private static final RegistryKey<Enchantment> FREQUENTFLYER = RegistryKey.of( RegistryKeys.ENCHANTMENT, Identifier.of( "catinbetween", "frequent_flyer") );

    public static void evaluateAllowFlight(ServerPlayerEntity player) {
        ItemStack chestStack = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
        if(player.getGameMode() == GameMode.SURVIVAL) {
            boolean hasElytra = chestStack.getItem() == Items.ELYTRA;
            Boolean allowFlight = false;
            int level = 1;

            if (hasElytra) {
                for ( Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : EnchantmentHelper.getEnchantments(chestStack).getEnchantmentEntries()) {
                    Identifier enchant = ((RegistryEntry.Reference)entry.getKey()).registryKey().getValue();
                    level = entry.getIntValue();
                    if(FREQUENTFLYER.getValue().equals( enchant )) {
                        allowFlight = true;
                        break;
                    }
                }
            }

           FlyingPlayerEntity playerEntity = (FlyingPlayerEntity)player;
           boolean hasFlyPermission = hasAnyLuckPermsFlyPermission(player);
           if (hasElytra && allowFlight || hasFlyPermission) {
               FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, String.format("allowing flight!, haselytra: %s, allowFlight: %s, hasFlyPermission: %s", hasElytra, allowFlight, hasFlyPermission));
               playerEntity.allowFlight( level );
           } else {
               FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, String.format("disallowing flight!, haselytra: %s, allowFlight: %s, hasFlyPermission: %s", hasElytra, allowFlight, hasFlyPermission));
               playerEntity.disallowFlight( );
           }
       }
    }

    private static boolean hasAnyLuckPermsFlyPermission(ServerPlayerEntity player) {

        LuckPerms luckPerms;

        try {
            luckPerms = LuckPermsProvider.get();

        } catch (NoClassDefFoundError e) {
            luckPerms = null;
        }

        if (luckPerms != null) {
            User luckpermsuser = luckPerms.getUserManager().getUser( player.getUuid() );

            if( luckpermsuser == null )
                return false;

            FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log,luckpermsuser.getUsername() + " has permissions: " + luckpermsuser.getCachedData().getPermissionData().getPermissionMap());

            CachedPermissionData permissionData = luckpermsuser.getCachedData()
                    .getPermissionData();

            boolean hasFrequentFlyerFlyCommandPerm = permissionData.checkPermission( "frequentFlyer.command.fly" ).asBoolean();
            boolean hasEssentialCommands = permissionData.checkPermission( "essentialcommands.fly.self" ).asBoolean();
            boolean hasFujiFly = permissionData.checkPermission( "fuji.fly" ).asBoolean();

            return hasEssentialCommands || hasFujiFly || hasFrequentFlyerFlyCommandPerm;
        }
        return false;
    }
}