package com.kurume_nct.friendly_mimic

import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent

@Mod(modid = FriendlyMimicMod.MODID, version = FriendlyMimicMod.VERSION)
class FriendlyMimicMod {

    @EventHandler
    fun init(event: FMLInitializationEvent) {
        // some example code
        println("DIRT BLOCK >> " + Blocks.DIRT.unlocalizedName)
    }

    companion object {
        const val MODID = "friendly-mimic-mod"
        const val VERSION = "1.0"
    }
}
