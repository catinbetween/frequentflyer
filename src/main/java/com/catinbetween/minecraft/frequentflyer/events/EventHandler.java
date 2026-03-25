package com.catinbetween.minecraft.frequentflyer.events;

import com.catinbetween.minecraft.frequentflyer.FrequentFlyer;
import com.catinbetween.minecraft.frequentflyer.config.FrequentFlyerConfig;
import com.catinbetween.minecraft.frequentflyer.interfaces.FlyingPlayerEntity;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EventHandler {
    public static final ResourceKey<@NotNull Enchantment> FREQUENTFLYER = ResourceKey.create(
            Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("frequentflyer", "frequent_flyer"));

    private static final String SELF_FLY_PERMISSION = "frequentFlyer.ability.fly.self";
    private static final String OTHERS_FLY_PERMISSION = "frequentFlyer.ability.fly.others";
    private static final String MAIN_COMMAND_PERMISSION = "frequentFlyer.command.main";
    private static final String SILLY_COMMAND_PERMISSION = "frequentFlyer.command.silly";


    public static void evaluateTickAllowFlight( ServerPlayer player) {
        if (player.gameMode() == GameType.SURVIVAL) {
            FlyingPlayerEntity flyingPlayerEntity = (FlyingPlayerEntity) player;
            UUID grantedByPlayerUUID = flyingPlayerEntity.frequentflyer$getGrantedByPlayerUUID();

            ItemStack chestStack = player.getItemBySlot( net.minecraft.world.entity.EquipmentSlot.CHEST);
            boolean hasElytra = chestStack.getItem() == Items.ELYTRA;
            boolean canFlyWithElytra = false;
            flyingPlayerEntity.frequentflyer$setCanFlyWithElytra(false);
            int level = 1;

            if (hasElytra) {
                for (Object2IntMap.Entry<Holder<@NotNull Enchantment>> entry : EnchantmentHelper.getEnchantmentsForCrafting(chestStack).entrySet()) {
                    Identifier enchant = ((Holder.Reference) entry.getKey()).key().identifier();
                    level = entry.getIntValue();
                    if (FREQUENTFLYER.identifier().equals(enchant)) {
                        if (chestStack.getDamageValue() <= chestStack.getMaxDamage() - 32) {
                            canFlyWithElytra = true;
                        } else {
                            FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, String.format("Elytra is too damaged for flight: %s/%s", chestStack.getDamageValue(), chestStack.getMaxDamage()));
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

    public static boolean hasFlyCommandPermission( ServerPlayer player, ServerPlayer target) {
        LuckPerms luckPerms = getLuckPerms();
        if (luckPerms == null) {
            return false;
        }

        User luckpermsuser = luckPerms.getUserManager().getUser(player.getUUID());
        if (luckpermsuser == null)
            return false;
        return luckpermsuser.getCachedData().getPermissionData().checkPermission(player.getUUID().equals(target.getUUID()) ? SELF_FLY_PERMISSION : OTHERS_FLY_PERMISSION).asBoolean();

    }

    public static boolean hasMainCommandPermission( ServerPlayer player) {
        LuckPerms luckPerms = getLuckPerms();
        if (luckPerms == null) {
            return false;
        }

        User luckpermsuser = luckPerms.getUserManager().getUser(player.getUUID());
        if (luckpermsuser == null)
            return false;
        return luckpermsuser.getCachedData().getPermissionData().checkPermission( MAIN_COMMAND_PERMISSION).asBoolean();

    }

    public static boolean hasSillyCommandPermission( ServerPlayer player) {
        LuckPerms luckPerms = getLuckPerms();
        if (luckPerms == null) {
            return false;
        }

        User luckpermsuser = luckPerms.getUserManager().getUser(player.getUUID());
        if (luckpermsuser == null)
            return false;
        return luckpermsuser.getCachedData().getPermissionData().checkPermission( SILLY_COMMAND_PERMISSION).asBoolean();

    }

}
