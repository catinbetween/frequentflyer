package com.catinbetween.minecraft.frequentflyer.command;

import com.catinbetween.minecraft.frequentflyer.FrequentFlyer;
import com.catinbetween.minecraft.frequentflyer.config.FrequentFlyerConfig;
import com.catinbetween.minecraft.frequentflyer.events.EventHandler;
import com.catinbetween.minecraft.frequentflyer.interfaces.FlyingPlayerEntity;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.Objects;

import static com.catinbetween.minecraft.frequentflyer.events.EventHandler.hasFlyCommandPermission;

public class FlyCommand implements Command<CommandSourceStack> {

    public FlyCommand() {
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer target = CommandUtil.getCommandTargetPlayer(context);
        boolean flight_enabled = BoolArgumentType.getBool(context, "flight_enabled");

        String message;
        if (source == null) {
            message = "Player not found";
            context.getSource().sendFailure( Component.literal(message));
            return 0;
        } else if (target == null) {
            message = "Target player not found";
            context.getSource().sendFailure( Component.literal(message));
            return 0;
        }

        if (hasFlyCommandPermission(source.getPlayer(), target)) {
            boolean isTargetSelf = Objects.requireNonNull(source.getPlayer()).getUUID().equals(target.getUUID());
            FlyingPlayerEntity flyingPlayerEntity = (FlyingPlayerEntity) target;
            if (flight_enabled) {
                if (!isTargetSelf) {
                    message = String.format("granted flight for %s", target.getName().getString());

                } else {
                    message = String.format("granted flight for self: %s", target.getName().getString());
                }
                flyingPlayerEntity.frequentflyer$setGrantedByPlayerUUID(source.getPlayer().getUUID());

            } else {

                flyingPlayerEntity.frequentflyer$setGrantedByPlayerUUID(null);

                message = String.format("took fly grant away from %s", target.getName().getString());

            }

            String finalMessage = message;
            FrequentFlyer.log(FrequentFlyerConfig.INSTANCE.log, finalMessage);
            context.getSource().sendSuccess(() -> Component.literal(finalMessage), false);
            EventHandler.evaluateTickAllowFlight(target);
            return 1;
        }
        message = String.format("You do not have permission to grant fly for %s", target.getName().getString());
        context.getSource().sendFailure( Component.literal(message));
        return 0;
    }
}
