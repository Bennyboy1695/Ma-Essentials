
package com.maciej916.maessentials.libs;

import com.maciej916.maessentials.Utils;
import com.maciej916.maessentials.classes.Location;
import com.maciej916.maessentials.classes.kit.Kit;
import com.maciej916.maessentials.classes.player.EssentialPlayer;
import com.maciej916.maessentials.classes.teleport.TeleportRequest;
import com.maciej916.maessentials.classes.teleport.TeleportSimple;
import com.maciej916.maessentials.data.DataManager;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Optional;

import static com.maciej916.maessentials.MaEssentials.MODID;
import static com.maciej916.maessentials.libs.Teleport.*;

public class Methods {

    public static final SuggestionProvider<CommandSource> HOME_SUGGEST = (context, builder) -> ISuggestionProvider.suggest(DataManager.getPlayer(context.getSource().asPlayer()).getHomeData().getHomes().keySet().toArray(new String[0]), builder);

    public static final SuggestionProvider<CommandSource> PLAYERS_SUGGEST = (context, builder) -> ISuggestionProvider.suggest(DataManager.getPlayers().values().stream().map(EssentialPlayer::getUsername), builder);

    public static final SuggestionProvider<CommandSource> WARP_SUGGEST = (context, builder) -> ISuggestionProvider.suggest(DataManager.getWarp().getWarps().keySet().toArray(new String[0]), builder);

    public static final SuggestionProvider<CommandSource> KIT_SUGGEST = (context, builder) -> ISuggestionProvider.suggest(DataManager.getKit().getKits().keySet().toArray(new String[0]), builder);

    private static String getVersion() {
        Optional<? extends ModContainer> o = ModList.get().getModContainerById(MODID);
        if (o.isPresent()) {
            return o.get().getModInfo().getVersion().toString();
        }
        return "NONE";
    }

    public static boolean isDev() {
        String version = getVersion();
        return version.equals("NONE");
    }

    public static ArrayList<String> catalogFiles(String catalog) {
        File folder = new File(catalog);
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> data = new ArrayList<>();
        if (listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    String name = FilenameUtils.removeExtension(listOfFiles[i].getName());
                    data.add(name);
                }
            }
        }
        return data;
    }

    public static FileReader loadFile(String catalog, String fileName) throws Exception {
        return new FileReader(catalog + fileName + ".json");
    }

    public static boolean fileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public static long currentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    public static boolean giveKit(ServerPlayerEntity player, Kit kit) {
        try {
            ArrayList<ItemStack> items = kit.getItems();
            for (ItemStack item : items) {
                player.inventory.addItemStackToInventory(item);
            }
            return true;
        } catch (Exception e) {
            player.sendMessage(Utils.translateFromJson("kit.maessentials.parse_error"));
            return false;
        }
    }

    public static boolean simpleTeleport(ServerPlayerEntity player, Location location, String teleport, long delay) {
        EssentialPlayer eslPlayer = DataManager.getPlayer(player);

        if (eslPlayer.getTemp().isTeleportActive()) {
            player.sendMessage(Utils.translateFromJson("teleport.maessentials.active"));
            return false;
        }

        if (delay == 0) {
            player.sendMessage(Utils.translateFromJson("teleport.maessentials.teleported"));
            doTeleport(player, location, true, true);
            return true;
        }

        eslPlayer.getTemp().setTeleportActive(new Location(player));
        TeleportSimple tpS = new TeleportSimple(player, location, teleport, delay);
        doSimpleTeleport(tpS);
        return true;
    }

    public static boolean requestTeleport(ServerPlayerEntity creator, ServerPlayerEntity player, ServerPlayerEntity target, long delay) {
        EssentialPlayer eslPlayer = DataManager.getPlayer(player);

        if (eslPlayer.getTemp().isTeleportActive()) {
            player.sendMessage(Utils.translateFromJson("teleport.maessentials.active"));
            return false;
        }

        TeleportRequest existTpR = Teleport.findRequest(creator, player, target);
        if (existTpR != null) {
            player.sendMessage(Utils.translateFromJson("tpa.maessentials.exist", target.getDisplayName().getFormattedText()));
            return false;
        }

        TeleportRequest tpR = new TeleportRequest(creator, player, target, delay);
        doRequetTeleport(tpR);
        return true;
    }

    static boolean checkLocation(Location first, Location second) {
        return first.x == second.x && first.y == second.y && first.z == second.z && first.dimension == second.dimension;
    }

    static boolean checkDetailedLocation(Location first, Location second) {
        return first.x == second.x && first.y == second.y && first.z == second.z && first.dimension == second.dimension && first.rotationPitch == second.rotationPitch && first.rotationYaw == second.rotationYaw;
    }

    public static TextComponent formatText(String translationKey, Object... args) {
        return new TranslationTextComponent(translationKey, args);
    }

    static void kickPlayer(ServerPlayerEntity player, StringTextComponent op, String reason) {
        player.server.getPlayerList().sendMessage(formatText("kick.maessentials.done", player.getDisplayName().getFormattedText(), op, reason));
        player.connection.disconnect(new StringTextComponent(reason));
    }
}