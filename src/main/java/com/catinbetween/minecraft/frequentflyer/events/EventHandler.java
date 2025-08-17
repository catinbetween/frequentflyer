package com.catinbetween.minecraft.frequentflyer.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Unique;

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
    private static final Logger log = LoggerFactory.getLogger( EventHandler.class );

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
           boolean hasFlyPermission = hasLpEssentialCommandsFlyPermission(player);
           if (hasElytra && allowFlight || hasLpEssentialCommandsFlyPermission(player)) {
               log.info( "allowing flight!, haselytra: {}, allowFlight: {}, hasFlyPermission: {}", hasElytra, allowFlight, hasFlyPermission );
               playerEntity.allowFlight( level );
           } else {
               log.info( "disallowing flight , haselytra: {}, allowFlight: {}, hasFlyPermission: {}", hasElytra, allowFlight, hasFlyPermission );
               playerEntity.disallowFlight( );
           }
       }
    }

    private static boolean hasLpEssentialCommandsFlyPermission(ServerPlayerEntity player) {

        LuckPerms luckPerms = LuckPermsProvider.get();

        if (luckPerms != null) {
            User luckpermsuser = luckPerms.getUserManager().getUser( player.getUuid() );

            if( luckpermsuser == null )
                return false;

            log.info( luckpermsuser.getUsername(), " has permissions: " + luckpermsuser.getCachedData().getPermissionData().getPermissionMap() );

            return luckpermsuser.getCachedData()
                    .getPermissionData()
                    .checkPermission( "essentialcommands.fly.self" )
                    .asBoolean();
        }
        return false;
    }
}