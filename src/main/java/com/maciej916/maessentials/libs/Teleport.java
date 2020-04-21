package com.maciej916.maessentials.libs;

import com.maciej916.maessentials.Utils;
import com.maciej916.maessentials.classes.Location;
import com.maciej916.maessentials.classes.player.EssentialPlayer;
import com.maciej916.maessentials.classes.teleport.TeleportRequest;
import com.maciej916.maessentials.classes.teleport.TeleportSimple;
import com.maciej916.maessentials.config.ConfigValues;
import com.maciej916.maessentials.data.DataManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

import static com.maciej916.maessentials.libs.Methods.*;

public class Teleport {

    private static ArrayList<TeleportSimple> teleportSimple = new ArrayList<>();
    private static ArrayList<TeleportRequest> teleportRequests = new ArrayList<>();

    static void doSimpleTeleport(TeleportSimple simple) {
        teleportSimple.add(simple);
    }

    static void doRequetTeleport(TeleportRequest request) {
        teleportRequests.add(request);
    }

    static TeleportRequest findRequest(ServerPlayerEntity creator, ServerPlayerEntity player, ServerPlayerEntity target) {
        for (TeleportRequest tp : teleportRequests) {
            if (tp.getCreator() == creator && tp.getPlayer() == player && tp.getTarget() == target) {
                return tp;
            }
        }
        return null;
    }

    public static TeleportRequest findRequest(ServerPlayerEntity creator, ServerPlayerEntity target) {
        for (TeleportRequest tp : teleportRequests) {
            if (tp.getCreator() == target && (tp.getPlayer() == creator && tp.getTarget() == target) || (tp.getPlayer() == target && tp.getTarget() == creator)) {
                return tp;
            }
        }
        return null;
    }

    public static ArrayList<TeleportRequest> findRequest(ServerPlayerEntity player) {
        ArrayList<TeleportRequest> found = new ArrayList<>();
        for (TeleportRequest tp : teleportRequests) {
            if (tp.getPlayer() == player || tp.getTarget() == player) {
                found.add(tp);
            }
        }
        return found;
    }

    public static void acceptRequest(TeleportRequest request) {
        if (request.getDelay() == 0) {
            request.getPlayer().sendMessage(Utils.translateFromJson("teleport.maessentials.tpaccept.request", request.getTargetName()));
            request.getTarget().sendMessage(Utils.translateFromJson("teleport.maessentials.tpaccept.target", request.getPlayerName()));
            doTeleport(request.getPlayer(), request.getDestination(), true, true);
            teleportRequests.remove(request);
        } else {
            request.getPlayer().sendMessage(Utils.translateFromJson("teleport.maessentials.tpaccept.request.wait", ConfigValues.tpa_delay));
            request.getTarget().sendMessage(Utils.translateFromJson("teleport.maessentials.tpaccept.target.wait", request.getPlayerName()));
            EssentialPlayer eslPlayer = DataManager.getPlayer(request.getPlayer());
            eslPlayer.getTemp().setTeleportActive(new Location(request.getPlayer()));
            request.setAccepted();
        }
    }

    public static void declineRequest(TeleportRequest request) {
        request.getPlayer().sendMessage(Utils.translateFromJson("teleport.maessentials.tpdeny.request", request.getTargetName()));
        request.getTarget().sendMessage(Utils.translateFromJson("teleport.maessentials.tpdeny.target", request.getPlayerName()));
        teleportRequests.remove(request);
    }

    public static void checkTeleports() {
        checkSimple();

        if (ConfigValues.tpa_enable || isDev()) {
            checkRequest();
        }
    }

    private static void checkSimple() {
        try {
            ArrayList<TeleportSimple> del = new ArrayList<>();
            for (TeleportSimple tp : teleportSimple) {
                ServerPlayerEntity player = tp.getPlayer();
                EssentialPlayer eslPlayer = DataManager.getPlayer(player);
                Location playerLocation = new Location(player);
                Location tpLocation = eslPlayer.getTemp().getTeleportLocation();
                if (checkLocation(playerLocation, tpLocation)) {
                    if (currentTimestamp() >= tp.getTeleportTime()) {
                        player.sendMessage(Utils.translateFromJson("teleport.maessentials.teleported"));
                        eslPlayer.getTemp().setTeleportNotActive();
                        eslPlayer.getUsage().setTeleportUsage(tp.getType());
                        eslPlayer.saveData();
                        doTeleport(player, tp.getDestination(), true, true);
                        del.add(tp);
                    }
                } else {
                    player.sendMessage(Utils.translateFromJson("teleport.maessentials.moved"));
                    eslPlayer.getTemp().setTeleportNotActive();
                    del.add(tp);
                }
            }
            teleportSimple.removeAll(del);
        } catch(Exception e) {
            System.out.println("checkSimple error:");
            System.out.println(e);
        }
    }

    private static void checkRequest() {
        try {
            ArrayList<TeleportRequest> del = new ArrayList<>();
            for (TeleportRequest tp : teleportRequests) {
                if (tp.isAccepted()) {
                    ServerPlayerEntity player = tp.getPlayer();

                    if (player == null) continue;

                    EssentialPlayer eslPlayer = DataManager.getPlayer(player);
                    Location playerLocation = new Location(player);
                    Location tpLocation = eslPlayer.getTemp().getTeleportLocation();
                    if (checkLocation(playerLocation, tpLocation)) {
                        if (currentTimestamp() >= tp.getTeleportTime()) {
                            tp.getPlayer().sendMessage(Utils.translateFromJson("teleport.maessentials.tpaccept.request", tp.getTargetName()));
                            tp.getTarget().sendMessage(Utils.translateFromJson("teleport.maessentials.tpaccept.target", tp.getPlayerName()));
                            eslPlayer.getTemp().setTeleportNotActive();
                            eslPlayer.getUsage().setTeleportUsage("tpa");
                            eslPlayer.saveData();
                            doTeleport(tp.getPlayer(), new Location(tp.getTarget()), true, true);
                            del.add(tp);
                        }
                    } else {
                        tp.getPlayer().sendMessage(Utils.translateFromJson("teleport.maessentials.moved.request"));
                        tp.getTarget().sendMessage(Utils.translateFromJson("teleport.maessentials.moved.target", tp.getPlayerName()));
                        eslPlayer.getTemp().setTeleportNotActive();
                        del.add(tp);
                    }
                } else {
                    if (currentTimestamp() >= tp.getTimeout()) {
                        tp.getPlayer().sendMessage(Utils.translateFromJson("teleport.maessentials.expired.target", tp.getTargetName()));
                        tp.getTarget().sendMessage(Utils.translateFromJson("teleport.maessentials.expired.request", tp.getPlayerName()));
                        del.add(tp);
                    }
                }
            }
            teleportRequests.removeAll(del);
        } catch(Exception e) {
            System.out.println("checkRequest error:");
            System.out.println(e);
        }
    }

    public static void doTeleport(ServerPlayerEntity player, Location loc, boolean exact, boolean saveLastLocation) {
        try {
            if (saveLastLocation) {
                EssentialPlayer eslPlayer = DataManager.getPlayer(player);
                eslPlayer.getData().setLastLocation(new Location(player));
                eslPlayer.saveData();
            }

            ServerWorld worldDest = player.server.getWorld(loc.getDimension());
            if (exact) {
                player.teleport(worldDest, loc.x, loc.y, loc.z, loc.rotationYaw, loc.rotationPitch);
            } else {
                player.teleport(worldDest, loc.x + 0.5, loc.y + 0.5, loc.z, player.rotationYaw, player.rotationPitch);
            }
        } catch (Exception e) {
            Log.err("Failed to do teleport for player " + player.getDisplayName().getFormattedText() + ". Error: " + e.getMessage());
            player.sendMessage(Utils.translateFromJson("teleport.maessentials.teleport.failed"));
        }
    }
}