package com.j3ly.singlechunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SingleChunkGenerator extends ChunkGenerator {
    public static final Codec<SingleChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Biome.CODEC.fieldOf("biome").forGetter(g -> g.biome)
        ).apply(instance, SingleChunkGenerator::new)
    );

    private final Holder<Biome> biome;
    private ChunkPos spawnChunkPos = null;

    public SingleChunkGenerator(Holder<Biome> biome) {
        super(new FixedBiomeSource(biome));
        this.biome = biome;
    }

    public void setSpawnChunkPos(ChunkPos pos) {
        this.spawnChunkPos = pos;
    }

    public ChunkPos getSpawnChunkPos() {
        return spawnChunkPos;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        // No caves/carvers anywhere
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
        if (!isSpawnChunk(chunk.getPos())) {
            return; // Void world for non-spawn chunks - no blocks placed
        }

        // Build a simple flat spawn chunk
        ChunkPos pos = chunk.getPos();
        int startX = pos.getMinBlockX();
        int startZ = pos.getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Bedrock at bottom
                chunk.setBlockState(new BlockPos(startX + x, -64, startZ + z), Blocks.BEDROCK.defaultBlockState(), false);

                // Stone layers
                for (int y = -63; y <= 0; y++) {
                    chunk.setBlockState(new BlockPos(startX + x, y, startZ + z), Blocks.STONE.defaultBlockState(), false);
                }

                // Dirt
                for (int y = 1; y <= 3; y++) {
                    chunk.setBlockState(new BlockPos(startX + x, y, startZ + z), Blocks.DIRT.defaultBlockState(), false);
                }

                // Grass on top
                chunk.setBlockState(new BlockPos(startX + x, 4, startZ + z), Blocks.GRASS_BLOCK.defaultBlockState(), false);
            }
        }
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
        // Only spawn mobs in spawn chunk
    }

    @Override
    public int getGenDepth() {
        return 384; // -64 to 320
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        if (!isSpawnChunk(chunk.getPos())) {
            // Return empty chunk for non-spawn chunks - completely void
            return CompletableFuture.completedFuture(chunk);
        }

        // For spawn chunk, buildSurface already handled it
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        if (!isSpawnChunk(new ChunkPos(x >> 4, z >> 4))) {
            return Integer.MIN_VALUE; // Void - no terrain
        }
        return 4; // Grass level in spawn chunk
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        if (!isSpawnChunk(new ChunkPos(x >> 4, z >> 4))) {
            return new NoiseColumn(-64, new BlockState[0]); // Empty column for void
        }

        BlockState[] states = new BlockState[384];
        states[0] = Blocks.BEDROCK.defaultBlockState();
        for (int y = 1; y < 64; y++) states[y] = Blocks.STONE.defaultBlockState();
        for (int y = 65; y < 68; y++) states[y] = Blocks.DIRT.defaultBlockState();
        states[68] = Blocks.GRASS_BLOCK.defaultBlockState();

        return new NoiseColumn(-64, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("SingleChunk: " + (isSpawnChunk(new ChunkPos(pos)) ? "Spawn" : "Void"));
    }

    private boolean isSpawnChunk(ChunkPos pos) {
        if (spawnChunkPos == null) return false;
        return pos.x == spawnChunkPos.x && pos.z == spawnChunkPos.z;
    }

    @Override
    public void createStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState structureState,
                                 StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager templateManager) {
        // No structures anywhere
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structureManager, ChunkAccess chunk) {
        // No structure references
    }
}
