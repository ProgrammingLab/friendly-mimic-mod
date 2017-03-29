package com.kurume_nct.friendly_mimic.inventory

import com.kurume_nct.friendly_mimic.FriendlyMimicMod
import java.util.*

/**
 * Created by gedorinku on 2017/03/29.
 */
object MimicInventoryTag {

    private const val HEADER = "${FriendlyMimicMod.MODID}:PLAYER_UUID:"

    fun isMimicInventoryTag(tag: String): Boolean = tag.startsWith(HEADER)

    fun getPlayerUUID(tag: String): String {
        if (!isMimicInventoryTag(tag)) {
            throw IllegalArgumentException("Not a MimicInventoryTag:$tag")
        }

        return tag.subSequence(HEADER.length, tag.length).toString()
    }

    fun composeMimicInventoryTag(playerUUID: UUID): String = HEADER + playerUUID.toString()
}