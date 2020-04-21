package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.TextUtils;
import com.maciej916.maessentials.libs.Methods;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;

public class CommandBroadcast {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("broadcast").requires(source -> source.hasPermissionLevel(2));
        builder
                .executes(context -> broadcast(context))
                        .then(Commands.argument("message", MessageArgument.message())
                                .executes(context -> broadcastArgs(context)));
        dispatcher.register(builder);
    }

    private static int broadcast(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        player.sendMessage(TextUtils.translateFromJson("broadcast.maessentials.no_message"));
        return Command.SINGLE_SUCCESS;
    }

    private static int broadcastArgs(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        ITextComponent reason = MessageArgument.getMessage(context, "message");
        player.server.getPlayerList().sendMessage(TextUtils.translateFromJson("broadcast.maessentials.success", reason));
        return Command.SINGLE_SUCCESS;
    }
}
