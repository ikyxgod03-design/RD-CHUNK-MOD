package com.rdchunk;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Simple config: .minecraft/config/rdchunk.properties */
public final class RDChunkConfig {
    /** Tweaks only apply when render distance >= this value. */
    public int highRenderDistance = 8;
    /** Force chunk builder to fully threaded (no main-thread blocking). */
    public boolean threadedChunkBuilder = true;
    /** Cap biome blend radius at high render distance (lower = cheaper meshing). */
    public boolean limitBiomeBlend = true;
    public int biomeBlendMax = 3;
    /** Cap entity distance scaling at high render distance. */
    public boolean limitEntityDistance = true;
    public double entityDistanceMax = 1.0;
    /** Cap simulation distance at high render distance (less tick lag in singleplayer). */
    public boolean limitSimulationDistance = true;
    public int simulationDistanceMax = 10;

    public static RDChunkConfig load() {
        RDChunkConfig c = new RDChunkConfig();
        Path file = FabricLoader.getInstance().getConfigDir().resolve("rdchunk.properties");
        Properties p = new Properties();
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                p.load(in);
                c.highRenderDistance = Integer.parseInt(p.getProperty("highRenderDistance", "8").trim());
                c.threadedChunkBuilder = Boolean.parseBoolean(p.getProperty("threadedChunkBuilder", "true").trim());
                c.limitBiomeBlend = Boolean.parseBoolean(p.getProperty("limitBiomeBlend", "true").trim());
                c.biomeBlendMax = Integer.parseInt(p.getProperty("biomeBlendMax", "3").trim());
                c.limitEntityDistance = Boolean.parseBoolean(p.getProperty("limitEntityDistance", "true").trim());
                c.entityDistanceMax = Double.parseDouble(p.getProperty("entityDistanceMax", "1.0").trim());
                c.limitSimulationDistance = Boolean.parseBoolean(p.getProperty("limitSimulationDistance", "true").trim());
                c.simulationDistanceMax = Integer.parseInt(p.getProperty("simulationDistanceMax", "10").trim());
            } catch (IOException | RuntimeException e) {
                RDChunkClient.LOGGER.warn("[RD CHUNK] Config invalid, using defaults: {}", e.toString());
                return new RDChunkConfig();
            }
        } else {
            c.save(file);
        }
        return c;
    }

    private void save(Path file) {
        Properties p = new Properties();
        p.setProperty("highRenderDistance", Integer.toString(highRenderDistance));
        p.setProperty("threadedChunkBuilder", Boolean.toString(threadedChunkBuilder));
        p.setProperty("limitBiomeBlend", Boolean.toString(limitBiomeBlend));
        p.setProperty("biomeBlendMax", Integer.toString(biomeBlendMax));
        p.setProperty("limitEntityDistance", Boolean.toString(limitEntityDistance));
        p.setProperty("entityDistanceMax", Double.toString(entityDistanceMax));
        p.setProperty("limitSimulationDistance", Boolean.toString(limitSimulationDistance));
        p.setProperty("simulationDistanceMax", Integer.toString(simulationDistanceMax));
        try (OutputStream out = Files.newOutputStream(file)) {
            p.store(out, "RD CHUNK config");
        } catch (IOException ignored) { }
    }
}
