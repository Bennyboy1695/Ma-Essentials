package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.PermissionStrings;
import com.maciej916.maessentials.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.GameType;

public class CommandGm {
    private static final SuggestionProvider<CommandSource> GM_SUGGEST = (context, builder) -> {
        String[] gm = {"0", "1", "2", "3"};
        return ISuggestionProvider.suggest(gm, builder);
    };

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("gm").requires(Utils.hasPermission(PermissionStrings.COMMAND.GAMEMODE));
        builder
                .executes(context -> gm(context))
                        .then(Commands.argument("gamemode", IntegerArgumentType.integer())
                        .suggests(GM_SUGGEST)
                        .executes(context -> gmSelf(context))
                                .then(Commands.argument("targetPlayer", EntityArgument.players())
                                .requires(Utils.hasPermission(PermissionStrings.COMMAND.GAMEMODE_OTHERS)).executes(context -> gmOthers(context))));

        dispatcher.register(builder);
    }

    private static int gm(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        player.sendMessage(Utils.translateFromJson("maessentials.provide.player"));
        return Command.SINGLE_SUCCESS;
    }

    private static int gmSelf(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        int gamemode = IntegerArgumentType.getInteger(context, "gamemode");
        gmManage(player, player, gamemode);
        return Command.SINGLE_SUCCESS;
    }

    private static int gmOthers(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "targetPlayer");
        int gamemode = IntegerArgumentType.getInteger(context, "gamemode");
        gmManage(player, targetPlayer, gamemode);
        return Command.SINGLE_SUCCESS;
    }

    private static void gmManage(ServerPlayerEntity player, ServerPlayerEntity targetPlayer, int gm) {
        boolean changed = false;
        switch (gm) {
            case 0:
                if (targetPlayer.interactionManager.getGameType() != GameType.SURVIVAL) {
                    targetPlayer.setGameType(GameType.SURVIVAL);
                    changed = true;
                }
                break;
            case 1:
                if (targetPlayer.interactionManager.getGameType() != GameType.CREATIVE) {
                    targetPlayer.setGameType(GameType.CREATIVE);
                    changed = true;
                }
                break;
            case 2:
                if (targetPlayer.interactionManager.getGameType() != GameType.ADVENTURE) {
                    targetPlayer.setGameType(GameType.ADVENTURE);
                    changed = true;
                }
                break;
            case 3:
                if (targetPlayer.interactionManager.getGameType() != GameType.SPECTATOR) {
                    targetPlayer.setGameType(GameType.SPECTATOR);
                    changed = true;
                }
                break;
            default :
                player.sendMessage(Utils.translateFromJson("gm.maessentials.invalid"));
        }

        if (changed) {
            String newGm = targetPlayer.interactionManager.getGameType().getDisplayName().getFormattedText();
            if (player == targetPlayer) {
                player.sendMessage(Utils.translateFromJson("gm.maessentials.self", newGm));
            } else {
                player.sendMessage(Utils.translateFromJson("gm.maessentials.player", targetPlayer.getDisplayName().getFormattedText(), newGm));
                targetPlayer.sendMessage(Utils.translateFromJson("gm.maessentials.self", newGm));
            }
        }
    }
}
