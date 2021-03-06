package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.utils.PermissionStrings;
import com.maciej916.maessentials.utils.Utils;
import com.maciej916.maessentials.classes.Location;
import com.maciej916.maessentials.classes.player.EssentialPlayer;
import com.maciej916.maessentials.config.ConfigValues;
import com.maciej916.maessentials.data.DataManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CommandSetHome {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("sethome").requires(Utils.hasPermission(PermissionStrings.COMMAND.HOME_SET));
        builder
                .executes(context -> setHome(context))
                        .then(Commands.argument("homeName", StringArgumentType.string())
                                .executes(context -> setHomeArgs(context)));
        dispatcher.register(builder);
    }

    private static int setHome(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        doSetHome(player, "home");
        return Command.SINGLE_SUCCESS;
    }

    private static int setHomeArgs(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        String homeName = StringArgumentType.getString(context, "homeName").toLowerCase();
        doSetHome(player, homeName);
        return Command.SINGLE_SUCCESS;
    }

    private static void doSetHome(ServerPlayerEntity player, String name) {
        EssentialPlayer eslPlayer = DataManager.getPlayer(player);

        int homes_limit = ConfigValues.homes_limit;
        if (player.hasPermissionLevel(1)) {
            homes_limit = ConfigValues.homes_limit_op;
        }

        if ((eslPlayer.getHomeData().getHomes().size() < homes_limit)  || (eslPlayer.getHomeData().getHomes().size() == homes_limit && eslPlayer.getHomeData().getHome(name) != null)) {
            eslPlayer.getHomeData().setHome(name, new Location(player));
            eslPlayer.saveHomes();
            player.sendMessage(Utils.translateFromJson("sethome.maessentials.done", name));
        } else {
            player.sendMessage(Utils.translateFromJson("sethome.maessentials.max_homes", homes_limit));
       }
    }
}
