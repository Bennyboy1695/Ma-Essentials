package com.maciej916.maessentials.commands;

import com.maciej916.maessentials.utils.PermissionStrings;
import com.maciej916.maessentials.utils.Utils;
import com.maciej916.maessentials.classes.kit.Kit;
import com.maciej916.maessentials.classes.player.EssentialPlayer;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.Set;

public class CommandKit {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("kit").requires(Utils.hasPermission(PermissionStrings.COMMAND.KIT));
        builder
                .executes(context -> kit(context))
                .then(Commands.argument("kitName", StringArgumentType.word())
                        .suggests(Methods.KIT_SUGGEST)
                        .executes(context -> kitArgs(context)));
        dispatcher.register(builder);

        LiteralArgumentBuilder<CommandSource> builder2 = Commands.literal("kits").requires(Utils.hasPermission(PermissionStrings.COMMAND.KIT_LIST));
        builder2.executes(context -> kit(context));
        dispatcher.register(builder2);
    }
    private static int kit(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        Set<String> kits = DataManager.getKit().getKits().keySet();

        TextComponent kitList = (TextComponent) Utils.translateFromJson("kit.maessentials.list");
        if (kits.size() != 0) {
            int i = 1;
            for (String name : kits) {
                ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/kit " + name);
                HoverEvent eventHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.translateFromJson("kit.maessentials.list.kit", "/kit " + name));

                TextComponent kit = (TextComponent) Utils.translateFromJson("kit.maessentials.list.kit", name);
                kit.getStyle().setClickEvent(clickEvent);
                kit.getStyle().setHoverEvent(eventHover);

                kitList.appendSibling(kit);
                if (kits.size() != i) {
                    kitList.appendSibling(new StringTextComponent(", "));
                    i++;
                }
            }
        } else {
            kitList.appendSibling(new StringTextComponent("-"));
        }

        player.sendMessage(kitList);
        return Command.SINGLE_SUCCESS;
    }

    private static int kitArgs(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        String kitName = StringArgumentType.getString(context, "kitName").toLowerCase();
        doKit(player, kitName);
        return Command.SINGLE_SUCCESS;
    }

    private static void doKit(ServerPlayerEntity player, String name) {
        EssentialPlayer eslPlayer = DataManager.getPlayer(player);

        Kit kit = DataManager.getKit().getKit(name);
        if (kit == null) {
            player.sendMessage(Utils.translateFromJson("kit.maessentials.not_exist", name));
            return;
        }

        long cooldown = eslPlayer.getUsage().getKitCooldown(name, kit.getDuration());
        if (cooldown != 0) {
            String displayTime = Time.formatDate(cooldown);
            player.sendMessage(Utils.translateFromJson("kit.maessentials.wait", displayTime));
            return;
        }

        if (Methods.giveKit(player, kit)) {
            eslPlayer.getUsage().setKitUssage(name);
            eslPlayer.saveData();

            player.world.playSound((PlayerEntity)null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            player.sendMessage(Utils.translateFromJson("kit.maessentials.received", name));
        }
    }
}
