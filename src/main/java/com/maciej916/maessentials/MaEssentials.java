package com.maciej916.maessentials;

import com.maciej916.maessentials.config.ConfigHolder;
import com.maciej916.maessentials.data.DataLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

import java.util.logging.Logger;

@Mod(MaEssentials.MODID)
public class MaEssentials {
    public static final String MODID = "ma-essentials";

    public MaEssentials() {
        if (FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
            Logger.getLogger("[MA-Essentials]").warning("Non Server Environment detected. MA-Essentials will disable!");
            return;
        }
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        final ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC, MODID + ".toml");

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.register(Commands.class);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        DataLoader.setupMain(event);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        DataLoader.setupWorld(event);
        DataLoader.load();
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        ServerLifecycleHooks.getCurrentServer().getServerStatusResponse().getForgeData().getRemoteModData().remove(MODID);
    }
}
