package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.utils.PermissionStrings;
import com.maciej916.maessentials.utils.Utils;
import com.maciej916.maessentials.classes.Location;
import com.maciej916.maessentials.libs.Teleport;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.material.Material;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

public class CommandTop {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("top").requires(Utils.hasPermission(PermissionStrings.COMMAND.TOP));
        builder.executes(context -> top(context));
        dispatcher.register(builder);
    }

    private static int top(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        ServerPlayerEntity player = context.getSource().asPlayer();

        int x = (int) player.getPosX();
        int y = world.getMaxHeight();
        int z = (int) player.getPosZ();

        Chunk chunk = world.getChunk((int) player.getPosX() >> 4, (int)player.getPosZ()>> 4);

        while (y > 0) {
            y--;

            BlockPos groundPos = new BlockPos(x, y-2, z);
            if (!chunk.getBlockState(groundPos).getMaterial().equals(Material.AIR)) {
                BlockPos legPos = new BlockPos(x, y-1, z);
                if (chunk.getBlockState(legPos).getMaterial().equals(Material.AIR)) {
                    BlockPos headPos = new BlockPos(x, y, z);
                    if (chunk.getBlockState(headPos).getMaterial().equals(Material.AIR)) {
                        Location topLocation = new Location(player.getPosX(), y-1, player.getPosZ(), player.rotationYaw, player.rotationPitch, player.dimension.getId());
                        Teleport.doTeleport(player, topLocation, true, true);
                        player.sendMessage(Utils.translateFromJson("top.maessentials.teleported"));
                        break;
                    }
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}