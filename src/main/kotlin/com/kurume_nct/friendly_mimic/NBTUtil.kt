package com.kurume_nct.friendly_mimic

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList

/**
 * Created by gedorinku on 2017/03/29.
 */
fun NBTTagList.forEach(f: (NBTTagCompound) -> Unit) {
    for (i in 0..(tagCount() - 1)) {
        f(getCompoundTagAt(i))
    }
}