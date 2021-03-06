package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.utils.PermissionStrings;
import com.maciej916.maessentials.utils.Utils;
import com.maciej916.maessentials.config.ConfigValues;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CommandSpeed {

    private static final float flySpeedDefault = 0.05F;
    private static final float walkSpeedDefault = 0.1F;

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("speed").requires(Utils.hasPermission(PermissionStrings.COMMAND.SPEED))
            .then(Commands.argument("speed", IntegerArgumentType.integer()).executes((context) -> speed(context.getSource(), IntegerArgumentType.getInteger(context, "speed")))
                    .then(Commands.argument("targetPlayer", EntityArgument.players()).requires(Utils.hasPermission(PermissionStrings.COMMAND.SPEED_OTHERS)).executes((context) -> speed(context.getSource(), IntegerArgumentType.getInteger(context, "speed"), EntityArgument.getPlayer(context, "targetPlayer"))))
            )
        );
    }

    private static int speed(CommandSource source, int speed) throws CommandSyntaxException {
        ServerPlayerEntity player = source.asPlayer();
        doSpeed(player, speed, player);
        return Command.SINGLE_SUCCESS;
    }

    private static int speed(CommandSource source, int speed, ServerPlayerEntity target) throws CommandSyntaxException {
        ServerPlayerEntity player = source.asPlayer();
        doSpeed(player, speed, target);
        return Command.SINGLE_SUCCESS;
    }

    private static void doSpeed(ServerPlayerEntity player, int speed, ServerPlayerEntity target) {
        if (target.abilities.isFlying) {
            if (speed > ConfigValues.speed_max_fly) {
                player.sendMessage(Utils.translateFromJson("speed.maessentials.max_fly", ConfigValues.speed_max_fly));
            } else {
                float flySpeed = speed * flySpeedDefault;
                player.setAIMoveSpeed(flySpeed);
                if (player == target) {
                    player.sendMessage(Utils.translateFromJson("speed.maessentials.fly.self", speed));
                } else {
                    player.sendMessage(Utils.translateFromJson("speed.maessentials.fly.other", target.getDisplayName().getFormattedText(), speed));
                    target.sendMessage(Utils.translateFromJson("speed.maessentials.fly.self", speed));
                }
            }
        } else {
            if (speed > ConfigValues.speed_max_walk) {
                player.sendMessage(Utils.translateFromJson("speed.maessentials.max_walk", ConfigValues.speed_max_fly));
            } else {
                float walkSpeed = speed * walkSpeedDefault;
                player.setAIMoveSpeed(walkSpeed);
                if (player == target) {
                    player.sendMessage(Utils.translateFromJson("speed.maessentials.walk.self", speed));
                } else {
                    player.sendMessage(Utils.translateFromJson("speed.maessentials.walk.other", target.getDisplayName().getFormattedText(), speed));
                    target.sendMessage(Utils.translateFromJson("speed.maessentials.walk.self", speed));
                }
            }
        }
    }
}