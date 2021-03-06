package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.utils.PermissionStrings;
import com.maciej916.maessentials.utils.Utils;
import com.maciej916.maessentials.data.DataManager;
import com.maciej916.maessentials.libs.Methods;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CommandDelWarp {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("delwarp").requires(Utils.hasPermission(PermissionStrings.COMMAND.WARP_DELETE));
        builder
                .executes(context -> warp(context))
                        .then(Commands.argument("warpName", StringArgumentType.string())
                                .suggests(Methods.WARP_SUGGEST)
                                .executes(context -> warpArgs(context)));
        dispatcher.register(builder);
    }

    private static int warp(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        player.sendMessage(Utils.translateFromJson("warp.maessentials.specify_name"));
        return Command.SINGLE_SUCCESS;
    }

    private static int warpArgs(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        String warpName = StringArgumentType.getString(context, "warpName").toLowerCase();

        if (DataManager.getWarp().delWarp(warpName)) {
            player.sendMessage(Utils.translateFromJson("delwarp.maessentials.success", warpName));
        } else {
            player.sendMessage(Utils.translateFromJson("warp.maessentials.not_exist", warpName));
        }

        return Command.SINGLE_SUCCESS;
    }
}