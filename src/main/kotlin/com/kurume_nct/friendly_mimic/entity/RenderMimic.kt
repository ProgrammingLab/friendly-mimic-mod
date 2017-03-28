package com.kurume_nct.friendly_mimic.entity

import net.minecraft.client.renderer.entity.RenderLiving
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

/**
 * Created by gedorinku on 2017/03/27.
 */
@SideOnly(Side.CLIENT)
class RenderMimic(manager: RenderManager) : RenderLiving<EntityMimic>(manager, ModelMimic(), 0.5f) {

    override fun getEntityTexture(entity: EntityMimic?): ResourceLocation? = MIMIC_TEXTURES

    companion object {
        private val MIMIC_TEXTURES = ResourceLocation("textures/entity/chest/normal.png")
    }
}