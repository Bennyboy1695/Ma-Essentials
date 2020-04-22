package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.utils.PermissionStrings;
import com.maciej916.maessentials.utils.Utils;
import com.maciej916.maessentials.classes.Location;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

import static com.maciej916.maessentials.libs.Teleport.doTeleport;

public class CommandTpall {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("tpall").requires(Utils.hasPermission(PermissionStrings.COMMAND.TP_ALL));
        builder.executes(context -> tpall(context));
        dispatcher.register(builder);
    }

    private static int tpall(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        Location location = new Location(player);

        for (ServerPlayerEntity tp : player.server.getPlayerList().getPlayers()) {
            if (player.getUniqueID() != tp.getUniqueID()) {
                doTeleport(tp, location, true, true);
                tp.sendMessage(Utils.translateFromJson("tpall.maessentials.teleported", player.getDisplayName().getFormattedText()));
            }
        }

        player.sendMessage(Utils.translateFromJson("tpall.maessentials.success"));
        return Command.SINGLE_SUCCESS;
    }
}
