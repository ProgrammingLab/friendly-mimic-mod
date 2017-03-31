package com.kurume_nct.friendly_mimic.entity

import net.minecraft.entity.ai.EntityAILeapAtTarget

/**
 * Created by gedorinku on 2017/03/29.
 */
class EntityMimicAILeapAtTarget(private val entityMimic: EntityMimic, leapMotionY: Float)
    : EntityAILeapAtTarget(entityMimic, leapMotionY) {

    override fun startExecuting() {
        super.startExecuting()

        entityMimic.openLidHalf()
        println("leap")
    }
}