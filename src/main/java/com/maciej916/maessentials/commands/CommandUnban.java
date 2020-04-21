package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.PermissionStrings;
import com.maciej916.maessentials.Utils;
import com.maciej916.maessentials.classes.player.EssentialPlayer;
import com.maciej916.maessentials.classes.player.PlayerRestriction;
import com.maciej916.maessentials.data.DataManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

import static com.maciej916.maessentials.libs.Methods.currentTimestamp;

public class CommandUnban {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("unban").requires(Utils.hasPermission(PermissionStrings.COMMAND.UNBAN))
                .executes((context) -> unban(context.getSource()))
                .then(Commands.argument("targetPlayer", StringArgumentType.word()).executes((context) -> unban(context.getSource(), StringArgumentType.getString(context, "targetPlayer"))))
        );
    }

    private static int unban(CommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.asPlayer();
        player.sendMessage(Utils.translateFromJson("maessentials.provide.player"));
        return Command.SINGLE_SUCCESS;
    }

    private static int unban(CommandSource source, String targetPlayer) throws CommandSyntaxException {
        ServerPlayerEntity player = source.asPlayer();
        EssentialPlayer eslTargetPlayer = DataManager.getPlayer(targetPlayer);

        if (eslTargetPlayer == null) {
            player.sendMessage(Utils.translateFromJson("maessentials.not_found.player", targetPlayer));
        } else {
            PlayerRestriction ban = eslTargetPlayer.getRestrictions().getBan();

            if (ban == null || (currentTimestamp() > ban.getTime() && ban.getTime() != -1)) {
                player.sendMessage(Utils.translateFromJson("unban.maessentials.not_banned", eslTargetPlayer.getUsername()));
            } else {
                eslTargetPlayer.getRestrictions().unBan();
                eslTargetPlayer.saveData();
                player.server.getPlayerList().sendMessage(Utils.translateFromJson("unban.maessentials.success", eslTargetPlayer.getUsername()));
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}