package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.PermissionStrings;
import com.maciej916.maessentials.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;

public class CommandRain {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("rain").requires(Utils.hasPermission(PermissionStrings.COMMAND.RAIN));
        builder.executes(context -> rain(context));
        dispatcher.register(builder);
    }

    private static int rain(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();

        for (ServerWorld serverworld : context.getSource().getServer().getWorlds()) {
            WorldInfo worldData = serverworld.getWorldInfo();
            worldData.setRaining(true);
            worldData.setThundering(false);
            worldData.setClearWeatherTime(0);
            worldData.setRainTime(6000);
        }

        player.sendMessage(Utils.translateFromJson("rain.maessentials.success"));
        return Command.SINGLE_SUCCESS;
    }
}