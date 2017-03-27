package com.kurume_nct.friendly_mimic

import com.kurume_nct.friendly_mimic.entity.EntityMimic
import com.kurume_nct.friendly_mimic.entity.RenderMimic
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.registry.RenderingRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.registry.EntityRegistry
import java.awt.Color

@Mod(modid = FriendlyMimicMod.MODID, version = FriendlyMimicMod.VERSION)
class FriendlyMimicMod {

    @EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        RenderingRegistry.registerEntityRenderingHandler(EntityMimic::class.java, ::RenderMimic)
        val mimicRegistryName = ResourceLocation(MODID, "entitymimic")
        val foreground = Color(0xA47227)
        val background = Color(0x332E25)
        EntityRegistry.registerModEntity(
                mimicRegistryName,
                EntityMimic::class.java,
                "mimic", 0, this, 80, 1, true,
                foreground.rgb, background.rgb)
    }

    companion object {
        const val MODID = "friendly-mimic-mod"
        const val VERSION = "1.0"
    }
}
