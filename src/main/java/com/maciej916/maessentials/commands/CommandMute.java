package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.TextUtils;
import com.maciej916.maessentials.classes.player.EssentialPlayer;
import com.maciej916.maessentials.classes.player.PlayerRestriction;
import com.maciej916.maessentials.data.DataManager;
import com.maciej916.maessentials.libs.Methods;
import com.maciej916.maessentials.libs.Time;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;

import static com.maciej916.maessentials.libs.Methods.currentTimestamp;

public class CommandMute {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("mute").requires(source -> source.hasPermissionLevel(2));
        builder
                .executes(context -> mute(context))
                        .then(Commands.argument("targetPlayer", EntityArgument.players())
                        .executes(context -> mutePlayer(context))
                                .then(Commands.argument("time", StringArgumentType.word())
                                .executes(context -> mutePlayerTime(context))
                                        .then(Commands.argument("reason", MessageArgument.message())
                                        .executes(context -> mutePlayerReason(context)))));

        dispatcher.register(builder);
    }

    private static int mute(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        player.sendMessage(TextUtils.translateFromJson("maessentials.provide.player"));
        return Command.SINGLE_SUCCESS;
    }

    private static int mutePlayer(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        player.sendMessage(TextUtils.translateFromJson("mute.maessentials.provide.time"));
        return Command.SINGLE_SUCCESS;
    }

    private static int mutePlayerTime(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "targetPlayer");
        String time = StringArgumentType.getString(context, "time");
        doMute(player, targetPlayer, time, "No reason provided");
        return Command.SINGLE_SUCCESS;
    }

    private static int mutePlayerReason(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "targetPlayer");
        String time = StringArgumentType.getString(context, "time").toLowerCase();
        ITextComponent reason = MessageArgument.getMessage(context, "reason");
        doMute(player, targetPlayer, time, reason.getString());
        return Command.SINGLE_SUCCESS;
    }

    private static void doMute(ServerPlayerEntity player, ServerPlayerEntity target, String time, String reason) {
        EssentialPlayer eslTargetPlayer = DataManager.getPlayer(target);
        PlayerRestriction mute = eslTargetPlayer.getRestrictions().getMute();

        long mutetime = Time.parseDate(time, true);
        if (time.equals("perm")) {
            mutetime = -1;
        }

        if (mutetime == 0) {
            player.sendMessage(TextUtils.translateFromJson("maessentials.illegal_date"));
            return;
        }

        if (mute != null && (currentTimestamp() < mute.getTime() ||  mute.getTime() == -1)) {
            player.sendMessage(TextUtils.translateFromJson("mute.maessentials.already_muted", target.getDisplayName().getFormattedText()));
            return;
        }

        eslTargetPlayer.getRestrictions().setMute(mutetime, reason);
        eslTargetPlayer.saveData();

        if (mutetime == -1) {
            player.server.getPlayerList().sendMessage(TextUtils.translateFromJson("mute.maessentials.success.perm", target.getDisplayName().getFormattedText(), player.getDisplayName().getFormattedText(), reason));
            target.sendMessage(TextUtils.translateFromJson("mute.maessentials.success.perm.target"));
        } else {
            String displayTime = Time.formatDate(mutetime - currentTimestamp());
            player.server.getPlayerList().sendMessage(TextUtils.translateFromJson("mute.maessentials.success", target.getDisplayName().getFormattedText(), player.getDisplayName().getFormattedText(), displayTime, reason));
            target.sendMessage(TextUtils.translateFromJson("mute.maessentials.success.target", displayTime));
        }

        target.sendMessage(TextUtils.translateFromJson("mute.maessentials.success.target.reason", reason));
    }
}
