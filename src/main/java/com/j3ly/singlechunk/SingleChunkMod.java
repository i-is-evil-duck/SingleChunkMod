package com.j3ly.singlechunk;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SingleChunkMod.MOD_ID)
public class SingleChunkMod {
    public static final String MOD_ID = "singlechunk";

    public SingleChunkMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        SingleChunkWorldPreset.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(SingleChunkEvents.class);
    }
}
