package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.utils.PermissionStrings;
import com.maciej916.maessentials.utils.Utils;
import com.maciej916.maessentials.classes.Location;
import com.maciej916.maessentials.classes.player.EssentialPlayer;
import com.maciej916.maessentials.config.ConfigValues;
import com.maciej916.maessentials.data.DataManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CommandAfk {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("afk").requires(Utils.hasPermission(PermissionStrings.COMMAND.AFK));
        builder.executes(context -> afk(context));
        dispatcher.register(builder);
    }

    private static int afk(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        EssentialPlayer eslPlayer = DataManager.getPlayer(player);

        long cooldown = eslPlayer.getUsage().getCommandCooldown("afk", ConfigValues.afk_command_cooldown);
        if (cooldown != 0) {
            player.sendMessage(Utils.translateFromJson("maessentials.cooldown", cooldown));
            return Command.SINGLE_SUCCESS;
        }

        if (!eslPlayer.getTemp().isAfk()) {
            eslPlayer.getUsage().setCommandUsage("afk");
            eslPlayer.saveData();

            player.sendMessage(Utils.translateFromJson("afk.maessentials.afk.true", player.getDisplayName().getFormattedText()));
            eslPlayer.getTemp().setLocation(new Location(player));
            eslPlayer.getTemp().setAfk(true);
        }

        return Command.SINGLE_SUCCESS;
    }
}