package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.utils.PermissionStrings;
import com.maciej916.maessentials.utils.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.GameType;

public class CommandFly {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("fly").requires(Utils.hasPermission(PermissionStrings.COMMAND.FLY));
        builder
                .executes(context -> fly(context))
                        .then(Commands.argument("targetPlayer", EntityArgument.players())
                                .requires(Utils.hasPermission(PermissionStrings.COMMAND.FLY_OTHERS)).executes(context -> flyArgs(context)));

        dispatcher.register(builder);
    }

    private static int fly(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        flyManage(player, player);
        return Command.SINGLE_SUCCESS;
    }

    private static int flyArgs(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "targetPlayer");
        flyManage(player, targetPlayer);
        return Command.SINGLE_SUCCESS;
    }

    private static void flyManage(ServerPlayerEntity player, ServerPlayerEntity target) {
        if (target.interactionManager.getGameType() == GameType.CREATIVE || target.interactionManager.getGameType() == GameType.SPECTATOR) {
            if (player == target) {
                target.sendMessage(Utils.translateFromJson("maessentials.invaild_gamemode"));
            } else {
                target.sendMessage(Utils.translateFromJson("maessentials.invaild_gamemode.player", target.getDisplayName().getFormattedText()));
            }
            return;
        }

        if (target.abilities.allowFlying) {
            target.abilities.allowFlying = false;
            target.abilities.isFlying = false;

            if (player == target) {
                player.sendMessage(Utils.translateFromJson("fly.maessentials.self.disabled"));
            } else {
                player.sendMessage(Utils.translateFromJson("fly.maessentials.player.disabled", target.getDisplayName().getFormattedText()));
                target.sendMessage(Utils.translateFromJson("fly.maessentials.self.disabled"));
            }
        } else {
            target.abilities.allowFlying = true;

            if (player == target) {
                player.sendMessage(Utils.translateFromJson("fly.maessentials.self.enabled"));
            } else {
                player.sendMessage(Utils.translateFromJson("fly.maessentials.player.enabled", target.getDisplayName().getFormattedText()));
                target.sendMessage(Utils.translateFromJson("fly.maessentials.self.enabled"));
            }
        }
        target.sendPlayerAbilities();
    }
}