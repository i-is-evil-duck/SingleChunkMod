package com.j3ly.singlechunk;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SingleChunkCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("singlechunk")
            .then(Commands.literal("strip")
                .executes(ctx -> stripChunks(ctx.getSource()))
            )
        );
    }

    private static int stripChunks(CommandSourceStack source) {
        if (!(source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        ChunkPos playerChunk = player.chunkPosition();

        int radius = 500;
        int stripped = 0;

        for (int cx = playerChunk.x - radius; cx <= playerChunk.x + radius; cx++) {
            for (int cz = playerChunk.z - radius; cz <= playerChunk.z + radius; cz++) {
                if (cx == playerChunk.x && cz == playerChunk.z) {
                    level.setChunkForced(cx, cz, true);
                    continue;
                }

                if (!level.hasChunk(cx, cz)) continue;

                LevelChunk chunk = level.getChunk(cx, cz);
                int minX = cx * 16;
                int minZ = cz * 16;
                int minY = level.getMinBuildHeight();
                int maxY = level.getMaxBuildHeight();

                boolean changed = false;
                for (int x = 0; x < 16; x++) {
                    for (int y = minY; y < maxY; y++) {
                        for (int z = 0; z < 16; z++) {
                            if (!chunk.getBlockState(new BlockPos(minX + x, y, minZ + z)).isAir()) {
                                chunk.setBlockState(new BlockPos(minX + x, y, minZ + z), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), false);
                                changed = true;
                            }
                        }
                    }
                }

                if (changed) {
                    chunk.setUnsaved(true);
                    stripped++;
                }

                level.setChunkForced(cx, cz, false);
            }
        }

        int finalStripped = stripped;
        source.sendSuccess(() -> Component.literal("Stripped " + finalStripped + " chunks. Your chunk is kept."), true);
        return 1;
    }
}
