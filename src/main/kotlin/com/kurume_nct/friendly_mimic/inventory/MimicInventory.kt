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

        for (i in 0 until EntityMimic.INVENTORY_SIZE) {
            val itemStack = mimic.getInventoryContent(i)
            setInventorySlotContents(i, itemStack)
        }

        addInventoryChangeListener {
            for (i in 0 until EntityMimic.INVENTORY_SIZE) {
                mimic.setInventoryContent(i, getStackInSlot(i))
            }
        }
    }

    override fun closeInventory(player: EntityPlayer?) {
        super.closeInventory(player)

        if (player == null || world.isRemote) return
        mimic.inventoryOpenCount--
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