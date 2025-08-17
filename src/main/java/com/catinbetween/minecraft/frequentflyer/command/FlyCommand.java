package com.catinbetween.minecraft.frequentflyer.command;

import com.catinbetween.minecraft.frequentflyer.interfaces.FlyingPlayerEntity;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Objects;

import static com.catinbetween.minecraft.frequentflyer.events.EventHandler.hasFlyCommandPermission;

public class FlyCommand implements Command<ServerCommandSource> {

    public FlyCommand() {
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity target = CommandUtil.getCommandTargetPlayer(context);
        boolean flight_enabled = BoolArgumentType.getBool(context, "flight_enabled");

        String message;
        if (source == null) {
            message = "Player not found";
            context.getSource().sendError(Text.literal(message));
            return 0;
        } else if (target == null)  {
            message = "Target player not found";
            context.getSource().sendError(Text.literal(message));
            return 0;
        }

        if (hasFlyCommandPermission(source.getPlayer(), target)) {
            if (flight_enabled) {
                if (Objects.requireNonNull(source.getPlayer()).getUuid().equals(target.getUuid())) {
                    ((FlyingPlayerEntity) target).frequentflyer$allowFlight(3, source.getPlayer());
                } else {
                    ((FlyingPlayerEntity) target).frequentflyer$allowFlight(3);
                }
                message = String.format("enabled flight for %s", target.getName().getString());
            } else {
                ((FlyingPlayerEntity) target).frequentflyer$disallowFlight();
                message = String.format("disallowed flight for %s", target.getName().getString());
            }

            context.getSource().sendFeedback(() -> Text.literal(message), false);
            return 1;
        }
        message = String.format("You do not have permission to grant fly for %s", target.getName().getString());
        context.getSource().sendError(Text.literal(message));
        return 0;
    }
}
