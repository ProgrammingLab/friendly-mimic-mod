package com.kurume_nct.friendly_mimic.entity

import net.minecraft.client.model.ModelChest
import net.minecraft.entity.Entity
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

/**
 * Created by gedorinku on 2017/03/27.
 */
@SideOnly(Side.CLIENT)
class ModelMimic : ModelChest() {

    init {
        //ModelChestのままだと微妙にずれるので．
        val diff = 0.5f
        chestLid.offsetX -= diff
        chestLid.offsetY += diff
        chestLid.offsetZ -= diff
        chestKnob.offsetX -= diff
        chestKnob.offsetY += diff
        chestKnob.offsetZ -= diff
        chestBelow.offsetX -= diff
        chestBelow.offsetY += diff
        chestBelow.offsetZ -= diff
    }

    override fun render(entityIn: Entity?, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, netHeadYaw: Float, headPitch: Float, scale: Float) {
        chestLid.rotateAngleX = (entityIn as EntityMimic).lidAngle
        renderAll()
        super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale)
    }
}