package com.rdchunk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.PrioritizeChunkUpdates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDChunkClient implements ClientModInitializer {
    public static final String MOD_ID = "rdchunk";
    public static final Logger LOGGER = LoggerFactory.getLogger("RD CHUNK");

    private RDChunkConfig config;
    private Object lastLevel;

    @Override
    public void onInitializeClient() {
        config = RDChunkConfig.load();
        // Options may not be ready during init, so apply on world join instead.
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        LOGGER.info("RD CHUNK loaded");
    }

    private void onTick(Minecraft mc) {
        Object level = mc.level;
        if (level != null && level != lastLevel && mc.options != null) {
            lastLevel = level;
            apply(mc.options);
        } else if (level == null) {
            lastLevel = null;
        }
    }

    private void apply(Options o) {
        int rd = o.renderDistance().get();
        if (rd < config.highRenderDistance) return;

        // 1) Fully threaded chunk building: no main-thread waiting on chunk meshes.
        if (config.threadedChunkBuilder) {
            try {
                o.prioritizeChunkUpdates().set(PrioritizeChunkUpdates.NONE);
            } catch (Throwable t) { LOGGER.warn("chunk builder tweak skipped: {}", t.toString()); }
        }
        // 2) Cheaper chunk meshing: lower biome blend (never raises).
        if (config.limitBiomeBlend) {
            try {
                if (o.biomeBlendRadius().get() > config.biomeBlendMax)
                    o.biomeBlendRadius().set(config.biomeBlendMax);
            } catch (Throwable t) { LOGGER.warn("biome blend tweak skipped: {}", t.toString()); }
        }
        // 3) Fewer entities to render far away.
        if (config.limitEntityDistance) {
            try {
                if (o.entityDistanceScaling().get() > config.entityDistanceMax)
                    o.entityDistanceScaling().set(config.entityDistanceMax);
            } catch (Throwable t) { LOGGER.warn("entity distance tweak skipped: {}", t.toString()); }
        }
        // 4) Less server tick load in singleplayer.
        if (config.limitSimulationDistance) {
            try {
                if (o.simulationDistance().get() > config.simulationDistanceMax)
                    o.simulationDistance().set(config.simulationDistanceMax);
            } catch (Throwable t) { LOGGER.warn("simulation distance tweak skipped: {}", t.toString()); }
        }
        LOGGER.info("RD CHUNK applied optimizations (render distance {})", rd);
    }
}
