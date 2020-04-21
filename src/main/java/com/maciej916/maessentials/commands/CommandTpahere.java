package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.PermissionStrings;
import com.maciej916.maessentials.Utils;
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
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import static com.maciej916.maessentials.libs.Methods.requestTeleport;

public class CommandTpahere {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("tpahere").requires(Utils.hasPermission(PermissionStrings.COMMAND.TPA_HERE));
        builder
                .executes(context -> tpahere(context))
                        .then(Commands.argument("targetPlayer", EntityArgument.players())
                        .executes(context -> tpahereArgs(context)));
        dispatcher.register(builder);
    }

    private static int tpahere(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        player.sendMessage(Utils.translateFromJson("maessentials.provide.player"));
        return Command.SINGLE_SUCCESS;
    }

    private static int tpahereArgs(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "targetPlayer");
        doTpa(player, targetPlayer);
        return Command.SINGLE_SUCCESS;
    }


    private static void doTpa(ServerPlayerEntity player, ServerPlayerEntity target) {
        EssentialPlayer eslPlayer = DataManager.getPlayer(player);

        if (player == target) {
            player.sendMessage(Utils.translateFromJson("tpa.maessentials.self"));
            return;
        }

        long cooldown = eslPlayer.getUsage().getTeleportCooldown("tpa", ConfigValues.tpa_cooldown);
        if (cooldown != 0) {
            player.sendMessage(Utils.translateFromJson("maessentials.cooldown.teleport", cooldown));
            return;
        }

        eslPlayer.getUsage().setCommandUsage("tpa");
        eslPlayer.saveData();
        if (requestTeleport(player, target, player, ConfigValues.tpa_timeout)) {
            player.sendMessage(Utils.translateFromJson("tpa.maessentials.request", target.getDisplayName().getFormattedText()));
            target.sendMessage(Utils.translateFromJson("tpahere.maessentials.request.target", player.getDisplayName().getFormattedText()));

            ClickEvent clickEventAccept = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tpaccept " + player.getDisplayName().getFormattedText());
            HoverEvent eventHoverAccept = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.translateFromJson("tpa.maessentials.request.target.accept.hover", "/tpaccept " + player.getDisplayName().getFormattedText()));
            TextComponent textAccept = new StringTextComponent("/tpaccept");
            textAccept.getStyle().setClickEvent(clickEventAccept);
            textAccept.getStyle().setHoverEvent(eventHoverAccept);
            target.sendMessage(Utils.translateFromJson("tpa.maessentials.request.target.accept", textAccept));

            ClickEvent clickEventDeny = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tpdeny " + player.getDisplayName().getFormattedText());
            HoverEvent eventHoverDeny = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.translateFromJson("tpa.maessentials.request.target.deny.hover", "/tpdeny " + player.getDisplayName().getFormattedText()));
            TextComponent textDeny = new StringTextComponent("/tpdeny");
            textDeny.getStyle().setClickEvent(clickEventDeny);
            textDeny.getStyle().setHoverEvent(eventHoverDeny);
            target.sendMessage(Utils.translateFromJson("tpa.maessentials.request.target.deny", textDeny));
        }
    }
}