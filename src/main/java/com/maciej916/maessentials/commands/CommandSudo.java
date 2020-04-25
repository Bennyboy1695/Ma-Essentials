package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.utils.PermissionStrings;
import com.maciej916.maessentials.utils.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class CommandSudo {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("sudo").requires(Utils.hasPermission(PermissionStrings.COMMAND.SUDO));
        builder.then(Commands.argument("target", EntityArgument.player())).then(Commands.argument("string", StringArgumentType.greedyString())).executes(context -> sudo(context));
        dispatcher.register(builder);
    }

    private static int sudo(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");
        String string = StringArgumentType.getString(context, "string");
        if (string.toLowerCase().contains("c:")) {
            //TODO: fake chat from player
        } else {
            ServerLifecycleHooks.getCurrentServer().getCommandManager().handleCommand(target.getCommandSource(), string);
            context.getSource().sendFeedback(new StringTextComponent(TextFormatting.GREEN + "Forced " + target.getDisplayName().getFormattedText() + " to run command: " + string), true);
        }

        return Command.SINGLE_SUCCESS;
    }
}