package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.utils.PermissionStrings;
import com.maciej916.maessentials.utils.Utils;
import com.maciej916.maessentials.data.DataLoader;
import com.maciej916.maessentials.libs.Log;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CommandMaeReload {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("maereload").requires(Utils.hasPermission(PermissionStrings.COMMAND.MAERELOAD));
        builder.executes(context -> reload(context));
        dispatcher.register(builder);
    }

    private static int reload(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        Log.log("Call DataLoader");
        DataLoader.load();
        player.sendMessage(Utils.translateFromJson("maereload.maessentials.done"));
        return Command.SINGLE_SUCCESS;
    }
}
