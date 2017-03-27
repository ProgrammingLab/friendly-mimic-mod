package com.kurume_nct.friendly_mimic.entity

import net.minecraft.entity.monster.EntityMob
import net.minecraft.world.World

/**
 * Created by gedorinku on 2017/03/27.
 */
class EntityMimic : EntityMob {

    constructor(world: World) : super(world) {
        setSize(1.0f, 1.0f)
    }

    override fun initEntityAI() {
        //TODO()
    }
}