package com.kurume_nct.friendly_mimic

import com.kurume_nct.friendly_mimic.inventory.ContainerMimicInventory
import com.kurume_nct.friendly_mimic.gui.GUIMimicInventory
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler

/**
 * Created by gedorinku on 2017/03/28.
 */
class GUIHandler : IGuiHandler {
    override fun getClientGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? = when (ID) {
        FriendlyMimicMod.MIMIC_GUI_ID -> GUIMimicInventory(player!!.inventory)
        else -> null
    }

    override fun getServerGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? = when (ID) {
        FriendlyMimicMod.MIMIC_GUI_ID -> ContainerMimicInventory(player!!.inventory)
        else -> null
    }
}