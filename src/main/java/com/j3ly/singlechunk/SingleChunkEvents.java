package com.j3ly.singlechunk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SingleChunkEvents {

    private static ChunkPos SPAWN_CHUNK = null;
    private static boolean initialized = false;

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.overworld();

        // Get the world spawn position
        BlockPos spawnPos = overworld.getSharedSpawnPos();
        SPAWN_CHUNK = new ChunkPos(spawnPos);

        // Set the spawn chunk on the generator so it knows which chunk to generate
        if (overworld.getChunkSource().getGenerator() instanceof SingleChunkGenerator generator) {
            generator.setSpawnChunkPos(SPAWN_CHUNK);
        }

        initialized = true;

        // Force-load the spawn chunk so it stays loaded
        overworld.setChunkForced(SPAWN_CHUNK.x, SPAWN_CHUNK.z, true);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide()) return;
        if (!initialized) return;
        if (SPAWN_CHUNK == null) return;

        ChunkPos loadedChunk = event.getChunk().getPos();

        // Unload non-spawn chunks immediately - they are void anyway
        if (loadedChunk.x != SPAWN_CHUNK.x || loadedChunk.z != SPAWN_CHUNK.z) {
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.setChunkForced(loadedChunk.x, loadedChunk.z, false);
            }
        }
    }
}
