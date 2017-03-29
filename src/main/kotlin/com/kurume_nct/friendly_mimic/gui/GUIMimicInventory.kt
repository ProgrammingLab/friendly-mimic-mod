package com.kurume_nct.friendly_mimic.gui

import com.kurume_nct.friendly_mimic.inventory.ContainerMimicInventory
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

/**
 * Created by gedorinku on 2017/03/28.
 */
class GUIMimicInventory(
        inventoryPlayer: InventoryPlayer,
        x: Int, y: Int, z: Int)
    : GuiContainer(ContainerMimicInventory(inventoryPlayer, x, y, z)) {

    init {
        ySize = 166
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        fontRendererObj.drawString("Mimic", 8, 6, 0)
    }

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        mc.textureManager.bindTexture(textures)
        val x = (width - xSize) / 2
        val y = (height - ySize) / 2
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize)
    }

    companion object {
        private val textures = ResourceLocation("textures/gui/container/shulker_box.png")
    }
}