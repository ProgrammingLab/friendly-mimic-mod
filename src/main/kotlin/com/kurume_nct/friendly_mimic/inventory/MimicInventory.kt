package com.kurume_nct.friendly_mimic.inventory

import com.kurume_nct.friendly_mimic.entity.EntityMimic
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.InventoryBasic
import net.minecraft.world.World

/**
 * Created by gedorinku on 2017/03/28.
 */
class MimicInventory(private val world: World)
    : InventoryBasic("MimicInventory", false, 27) {

    private lateinit var mimic: EntityMimic

    override fun openInventory(player: EntityPlayer?) {
        super.openInventory(player)

        if (player == null || world.isRemote) return

        val mimicOrNull = findMimic(player)
        if (mimicOrNull == null) {
            closeInventory(player)
            println("failed to find mimic.")
            return
        }
        mimic = mimicOrNull
        mimic.tags.remove(MimicInventoryTag.composeMimicInventoryTag(mimic))

        mimic.inventoryContents.forEachIndexed { index, itemStack -> setInventorySlotContents(index, itemStack) }

        addInventoryChangeListener {
            mimic.inventoryContents.forEachIndexed { index, _ ->
                mimic.inventoryContents[index] = getStackInSlot(index)
            }
        }
    }

    private fun findMimic(player: EntityPlayer): EntityMimic? {
        val uuids = player.tags
                .filter { MimicInventoryTag.isMimicInventoryTag(it) }
                .associate { Pair(it, MimicInventoryTag.getMimicUUID(it)) }
                .values
        val entities = world.getEntities(EntityMimic::class.java, { it!!.uniqueID.toString() in uuids })
        if (entities.size == 0) return null
        return entities.first()
    }
}