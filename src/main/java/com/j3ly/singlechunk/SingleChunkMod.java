package com.j3ly.singlechunk;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(SingleChunkMod.MOD_ID)
public class SingleChunkMod {
    public static final String MOD_ID = "singlechunk";

    public SingleChunkMod() {
        MinecraftForge.EVENT_BUS.register(SingleChunkEvents.class);
    }
}
