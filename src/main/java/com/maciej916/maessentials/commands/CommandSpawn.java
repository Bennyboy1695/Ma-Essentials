package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.utils.PermissionStrings;
import com.maciej916.maessentials.utils.Utils;
import com.maciej916.maessentials.classes.Location;
import com.maciej916.maessentials.classes.player.EssentialPlayer;
import com.maciej916.maessentials.config.ConfigValues;
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
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import static com.maciej916.maessentials.libs.Methods.simpleTeleport;

public class CommandSpawn {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("spawn").requires(Utils.hasPermission(PermissionStrings.COMMAND.SPAWN));
        builder.executes(CommandSpawn::spawn)
                .then(Commands.argument("player", StringArgumentType.string()).suggests(Methods.PLAYERS_SUGGEST).requires(Utils.hasPermission(PermissionStrings.COMMAND.SPAWN_OTHERS)).executes(CommandSpawn::spawnOther));
        dispatcher.register(builder);
    }

    private static int spawn(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        EssentialPlayer eslPlayer = DataManager.getPlayer(player);

        long cooldown = eslPlayer.getUsage().getTeleportCooldown("spawn", ConfigValues.spawn_cooldown);
        if (cooldown != 0) {
            player.sendMessage(Utils.translateFromJson("maessentials.cooldown.teleport", cooldown));
            return Command.SINGLE_SUCCESS;
        }

        eslPlayer.getUsage().setCommandUsage("spawn");
        eslPlayer.saveData();

        Location location = DataManager.getWorld().getSpawn();
        if (simpleTeleport(player, location, "spawn", ConfigValues.spawn_delay)) {
            if (ConfigValues.spawn_delay == 0) {
                player.sendMessage(Utils.translateFromJson("spawn.maessentials.success"));
            } else {
                player.sendMessage(Utils.translateFromJson("spawn.maessentials.success.wait", ConfigValues.spawn_delay));
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int spawnOther(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String target = StringArgumentType.getString(context, "player");
        EssentialPlayer eslPlayer = DataManager.getPlayer(target);
        if (eslPlayer != null) {

            if (ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(target) != null) {
                ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(target);
                eslPlayer.getUsage().setCommandUsage("spawn");
                eslPlayer.saveData();

                Location location = DataManager.getWorld().getSpawn();
                if (simpleTeleport(player, location, "spawn", 0)) {
                    player.sendMessage(Utils.translateFromJson("spawn.other.maessentials.success"));
                    context.getSource().sendFeedback(Utils.translateFromJson("spawn.other.online.maessentials.success", target), false);
                }
            } else {
                eslPlayer.setNeedingSpawnMove(true);
                eslPlayer.saveData();

                context.getSource().sendFeedback(Utils.translateFromJson("spawn.other.offline.maessentials.success", target), false);

            }
        } else {
            context.getSource().sendFeedback(Utils.translateFromJson("player.not.found.maessentials", target), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}