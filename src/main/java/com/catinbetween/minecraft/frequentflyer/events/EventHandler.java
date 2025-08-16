package com.catinbetween.minecraft.frequentflyer.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catinbetween.minecraft.frequentflyer.interfaces.FlyingPlayerEntity;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;


public class EventHandler {
    private static final RegistryKey<Enchantment> FREQUENTFLYER = RegistryKey.of( RegistryKeys.ENCHANTMENT, Identifier.of( "catinbetween", "frequent_flyer") );
    private static final Logger log = LoggerFactory.getLogger( EventHandler.class );

    public static void evaluateAllowFlight(ServerPlayerEntity player) {
        ItemStack chestStack = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);

        boolean hasElytra = chestStack.getItem() == Items.ELYTRA;
        Boolean allowFlight = false;

        if (hasElytra) {
            for ( Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : EnchantmentHelper.getEnchantments(chestStack).getEnchantmentEntries()) {
                Identifier enchant = ((RegistryEntry.Reference)entry.getKey()).registryKey().getValue();
                if(FREQUENTFLYER.getValue().equals( enchant )) {
                    allowFlight = true;
                    break;
                }
            }
        }
        FlyingPlayerEntity playerEntity = (FlyingPlayerEntity)player;
        if (hasElytra && allowFlight) {
            //log.info( "allowing flight!, haselytra: {}, allowFlight: {}", hasElytra, allowFlight );
            playerEntity.allowFlight( );
        } else {
            //log.info( "disallowing flight , haselytra: {}, allowFlight: {}", hasElytra, allowFlight );
            playerEntity.disallowFlight( );
        }
    }

    public static void onPlayerTick(ServerPlayerEntity player){
        evaluateAllowFlight( player );
    }
}