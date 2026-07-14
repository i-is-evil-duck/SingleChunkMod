package com.j3ly.singlechunk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SingleChunkEvents {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        BlockPos spawnPos = overworld.getSharedSpawnPos();
        ChunkPos spawnChunk = new ChunkPos(spawnPos);

        if (overworld.getChunkSource().getGenerator() instanceof SingleChunkGenerator generator) {
            generator.setSpawnChunkPos(spawnChunk);
        }

        overworld.setChunkForced(spawnChunk.x, spawnChunk.z, true);
    }
}
