package com.j3ly.singlechunk;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;

public class SingleChunkWorldPreset {
    public static final ResourceKey<WorldPreset> SINGLE_CHUNK = ResourceKey.create(
        Registries.WORLD_PRESET,
        new ResourceLocation(SingleChunkMod.MOD_ID, "single_chunk")
    );

    public static void register(IEventBus eventBus) {
        eventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registries.CHUNK_GENERATOR)) {
                event.register(Registries.CHUNK_GENERATOR, helper -> {
                    helper.register(
                        new ResourceLocation(SingleChunkMod.MOD_ID, "single_chunk"),
                        SingleChunkGenerator.CODEC
                    );
                });
            }
        });
    }

    private static final ResourceKey<net.minecraft.world.level.dimension.DimensionType> OVERWORLD_DIMENSION_TYPE =
        ResourceKey.create(Registries.DIMENSION_TYPE, new ResourceLocation("overworld"));

    public static void bootstrap(BootstapContext<WorldPreset> context) {
        Holder<Biome> plains = context.lookup(Registries.BIOME).getOrThrow(Biomes.PLAINS);

        SingleChunkGenerator generator = new SingleChunkGenerator(plains);

        LevelStem overworld = new LevelStem(
            context.lookup(Registries.DIMENSION_TYPE).getOrThrow(OVERWORLD_DIMENSION_TYPE),
            generator
        );

        context.register(SINGLE_CHUNK, new WorldPreset(
            java.util.Map.of(LevelStem.OVERWORLD, overworld)
        ));
    }
}
