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

    override fun doRender(entity: EntityMimic?, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float) {
        val lidAngleTarget = entity!!.lidAngleTarget
        if (entity.lidAngle != lidAngleTarget) {
            val motionSpeed = 0.1f
            val diff = lidAngleTarget - entity.lidAngle
            if (Math.abs(diff) <= motionSpeed) {
                entity.lidAngle = lidAngleTarget
                entity.closeLid()
                return
            }
            val motion = partialTicks * motionSpeed * if (diff < 0) -1.0f else 1.0f
            entity.lidAngle += motion
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks)
    }

    override fun getEntityTexture(entity: EntityMimic?): ResourceLocation? = MIMIC_TEXTURES

    companion object {
        private val MIMIC_TEXTURES = ResourceLocation("textures/entity/chest/normal.png")
    }
}