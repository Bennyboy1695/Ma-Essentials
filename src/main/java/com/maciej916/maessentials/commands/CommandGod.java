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

public class CommandGod {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("god").requires(Utils.hasPermission(PermissionStrings.COMMAND.GOD));
        builder
                .executes(context -> god(context))
                        .then(Commands.argument("targetPlayer", EntityArgument.players())
                                .requires(Utils.hasPermission(PermissionStrings.COMMAND.GOD_OTHERS)).executes(context -> godArgs(context)));

        dispatcher.register(builder);
    }

    private static int god(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        godManage(player, player);
        return Command.SINGLE_SUCCESS;
    }

    private static int godArgs(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "targetPlayer");
        godManage(player, targetPlayer);
        return Command.SINGLE_SUCCESS;
    }

    private static void godManage(ServerPlayerEntity player, ServerPlayerEntity target) {
        if (target.interactionManager.getGameType() == GameType.CREATIVE || target.interactionManager.getGameType() == GameType.SPECTATOR) {
            if (player == target) {
                target.sendMessage(Utils.translateFromJson("maessentials.invaild_gamemode"));
            } else {
                target.sendMessage(Utils.translateFromJson("maessentials.invaild_gamemode.player", target.getDisplayName().getFormattedText()));
            }
            return;
        }

        if (target.abilities.disableDamage) {
            target.abilities.disableDamage = false;

            if (player == target) {
                player.sendMessage(Utils.translateFromJson("god.maessentials.self.disabled"));
            } else {
                player.sendMessage(Utils.translateFromJson("god.maessentials.player.disabled", target.getDisplayName().getFormattedText()));
                target.sendMessage(Utils.translateFromJson("god.maessentials.self.disabled"));
            }
        } else {
            target.abilities.disableDamage = true;

            if (player == target) {
                player.sendMessage(Utils.translateFromJson("god.maessentials.self.enabled"));
            } else {
                player.sendMessage(Utils.translateFromJson("god.maessentials.player.enabled", target.getDisplayName().getFormattedText()));
                target.sendMessage(Utils.translateFromJson("god.maessentials.self.enabled"));
            }
        }
        target.sendPlayerAbilities();
    }
}
