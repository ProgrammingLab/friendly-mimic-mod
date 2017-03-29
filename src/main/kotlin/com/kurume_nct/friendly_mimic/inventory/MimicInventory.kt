package com.kurume_nct.friendly_mimic.inventory

import com.kurume_nct.friendly_mimic.entity.EntityMimic
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.InventoryBasic
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * Created by gedorinku on 2017/03/28.
 */
class MimicInventory(
        private val world: World,
        private val x: Int,
        private val y: Int,
        private val z: Int)
    : InventoryBasic("MimicInventory", false, 27) {

    private lateinit var mimic: EntityMimic

    override fun openInventory(player: EntityPlayer?) {
        super.openInventory(player)

        if (player == null || world.isRemote) return

        val mimicOrNull = findMimic(player)
        if (mimicOrNull == null) {
            closeInventory(player)
            println("failed to find mimic. $x, $y, $z")
            return
        }
        mimic = mimicOrNull
        mimic.tags.remove(MimicInventoryTag.composeMimicInventoryTag(player.uniqueID))

        mimic.inventoryContents.forEachIndexed { index, itemStack -> setInventorySlotContents(index, itemStack) }

        addInventoryChangeListener {
            mimic.inventoryContents.forEachIndexed { index, _ ->
                mimic.inventoryContents[index] = getStackInSlot(index)
            }
        }
    }

    private fun findMimic(player: EntityPlayer): EntityMimic? =
            world.getEntitiesWithinAABB(
                    EntityMimic::class.java,
                    AxisAlignedBB(BlockPos(x - 8, y - 8, z - 8), BlockPos(x + 8, y + 8, z + 8)))
                    .find {
                        it.tags.any {
                            MimicInventoryTag.isMimicInventoryTag(it) &&
                                    MimicInventoryTag.getPlayerUUID(it) == player.uniqueID.toString()
                        }
                    }
}