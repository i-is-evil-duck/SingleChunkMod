package com.j3ly.singlechunk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SingleChunkEvents {

    private static ChunkPos SPAWN_CHUNK = null;

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension() == Level.OVERWORLD) {
                BlockPos spawnPos = serverLevel.getSharedSpawnPos();
                SPAWN_CHUNK = new ChunkPos(spawnPos);
                serverLevel.setChunkForced(SPAWN_CHUNK.x, SPAWN_CHUNK.z, true);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        if (SPAWN_CHUNK == null) {
            BlockPos spawnPos = overworld.getSharedSpawnPos();
            SPAWN_CHUNK = new ChunkPos(spawnPos);
        }
        overworld.setChunkForced(SPAWN_CHUNK.x, SPAWN_CHUNK.z, true);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide()) return;
        if (SPAWN_CHUNK == null) return;

        ChunkPos loadedChunk = event.getChunk().getPos();

        if (loadedChunk.x == SPAWN_CHUNK.x && loadedChunk.z == SPAWN_CHUNK.z) return;

        if (event.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.setChunkForced(loadedChunk.x, loadedChunk.z, false);
        }

        if (event.getChunk() instanceof LevelChunk levelChunk) {
            int minY = levelChunk.getMinSection() * 16;
            int maxY = levelChunk.getMaxSection() * 16;
            for (int x = 0; x < 16; x++) {
                for (int y = minY; y < maxY; y++) {
                    for (int z = 0; z < 16; z++) {
                        levelChunk.setBlockState(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), false);
                    }
                }
            }
            levelChunk.setUnsaved(true);
        }
    }
}
