package com.kurume_nct.friendly_mimic.inventory

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack

/**
 * Created by gedorinku on 2017/03/28.
 */
class ContainerMimicInventory(inventoryPlayer: InventoryPlayer, x: Int, y: Int, z: Int) : Container() {

    private val inventory: MimicInventory = MimicInventory(inventoryPlayer.player.world, x, y, z)

    init {
        inventory.openInventory(inventoryPlayer.player)

        for (j in 0..2) {
            for (k in 0..8) {
                this.addSlotToContainer(net.minecraft.inventory.Slot(inventory, k + j * 9, 8 + k * 18, 18 + j * 18))
            }
        }

        val base = 2 * 18 + 1

        for (j in 0..2) {
            for (k in 0..8) {
                this.addSlotToContainer(net.minecraft.inventory.Slot(inventoryPlayer, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + base))
            }
        }

        for (j in 0..8) {
            this.addSlotToContainer(net.minecraft.inventory.Slot(inventoryPlayer, j, 8 + j * 18, 161 + base))
        }
    }

    override fun canInteractWith(playerIn: EntityPlayer?): Boolean {
        return true
    }

    override fun transferStackInSlot(playerIn: EntityPlayer?, index: Int): ItemStack {
        val slot = inventorySlots[index] ?: return ItemStack.EMPTY
        if (!slot.hasStack) return ItemStack.EMPTY

        val itemStack = slot.stack
        val temp = itemStack.copy()

        if (index < inventory.sizeInventory &&
                !mergeItemStack(itemStack, inventory.sizeInventory, inventorySlots.size, true)) {
            return ItemStack.EMPTY
        }

        if (!mergeItemStack(itemStack, 0, inventory.sizeInventory, false)) {
            return ItemStack.EMPTY
        }

        if (itemStack.count == 0) {
            slot.putStack(ItemStack.EMPTY)
        } else {
            slot.onSlotChanged()
        }

        return temp
    }

    override fun onContainerClosed(playerIn: EntityPlayer?) {
        super.onContainerClosed(playerIn)
        inventory.closeInventory(playerIn)
    }
}