package com.kurume_nct.friendly_mimic.entity

import com.kurume_nct.friendly_mimic.FriendlyMimicMod
import com.kurume_nct.friendly_mimic.forEach
import com.kurume_nct.friendly_mimic.inventory.MimicInventoryTag
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityAgeable
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.*
import net.minecraft.entity.passive.EntityTameable
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.network.datasync.DataParameter
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumHand
import net.minecraft.world.World

/**
 * Created by gedorinku on 2017/03/27.
 */
class EntityMimic : EntityTameable {

    var lidAngle = 0.0f
    var lidAngleTarget
        get() = dataManager.get(LID_ANGLE_TARGET)
        private set(value) = dataManager.set(LID_ANGLE_TARGET, value)
    var lidAnimationTick: Int = 0

    var inventoryOpenCount
        get() = dataManager.get(INVENTORY_OPEN_COUNT)
        set(value) = dataManager.set(INVENTORY_OPEN_COUNT, value)

    constructor(world: World) : super(world) {
        setSize(1.0f, 1.0f)
        isTamed = false
    }

    override fun entityInit() {
        super.entityInit()

        dataManager.register(LID_ANGLE_TARGET, 0.0f)
        for (i in 0 until INVENTORY_SIZE) {
            dataManager.register(INVENTORY_CONTENTS[i], ItemStack.EMPTY)
        }
        dataManager.register(INVENTORY_OPEN_COUNT, 0)
    }

    override fun onUpdate() {
        super.onUpdate()

        if (world.isRemote) return

        if (lidAnimationTick != 0 || lidAngleTarget != 0.0f) {
            lidAnimationTick--
            if (lidAnimationTick <= 0) {
                lidAnimationTick = 0
                closeLid(true)
            }
        }

        if (0 < inventoryOpenCount) {
            openLidFull(false)
        }
    }

    override fun initEntityAI() {
        aiSit = EntityAISit(this)
        tasks.addTask(1, EntityAISwimming(this))
        tasks.addTask(2, aiSit)
        tasks.addTask(3, EntityMimicAILeapAtTarget(this, 0.4f))
        tasks.addTask(4, EntityAIAttackMelee(this, 1.0, true))
        tasks.addTask(5, EntityAIFollowOwner(this, 1.0, 10.0f, 2.0f))
        tasks.addTask(6, EntityAIWanderAvoidWater(this, 1.0))
        this.targetTasks.addTask(1, EntityAIOwnerHurtByTarget(this))
        this.targetTasks.addTask(2, EntityAIOwnerHurtTarget(this))
        this.targetTasks.addTask(3, EntityAIHurtByTarget(this, true, *arrayOfNulls<Class<*>>(0)))
        this.targetTasks.addTask(4, EntityAIFindEntityNearestPlayer(this))
    }

    override fun attackEntityFrom(source: DamageSource?, amount: Float): Boolean {
        if (isEntityInvulnerable(source)) {
            return false
        }

        val entity = source?.entity
        aiSit?.setSitting(false)
        openLidHalf(true)
        return super.attackEntityFrom(source,
                if (entity != null && entity !is EntityPlayer && entity !is EntityArrow) {
                    (amount + 1.0f) / 2.0f
                } else {
                    amount
                })
    }

    override fun attackEntityAsMob(entityIn: Entity?): Boolean {
        val flag = entityIn?.attackEntityFrom(
                DamageSource.causeMobDamage(this),
                this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).attributeValue.toInt().toFloat())
                ?: return false

        if (flag) {
            this.applyEnchantments(this, entityIn)
        }

        return flag
    }

    override fun processInteract(player: EntityPlayer?, hand: EnumHand?): Boolean {
        val itemStack = player!!.getHeldItem(hand)
        val holdingRottenFlesh = !itemStack.isEmpty && itemStack.item == Items.ROTTEN_FLESH

        if (isTamed) {
            if (holdingRottenFlesh) {
                if (!player.capabilities.isCreativeMode) {
                    itemStack.shrink(1)
                }
                heal(4.0f)
                openLidHalf(true)

                return true
            }

            if (player.isSneaking) {
                playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.8f, 0.5f + rand.nextFloat() * 0.1f)
                openGUI(player)
                return true
            }

            if (isOwner(player) && !world.isRemote && !isBreedingItem(itemStack)) {
                aiSit?.setSitting(!isSitting)
                isJumping = false
                navigator.clearPathEntity()
                attackTarget = null
            }
        } else if (holdingRottenFlesh) {
            if (!player.capabilities.isCreativeMode) {
                itemStack.shrink(1)
            }
            if (!world.isRemote) {
                if (rand.nextInt(3) == 0) {
                    isTamed = true
                    navigator.clearPathEntity()
                    attackTarget = null
                    aiSit.setSitting(true)
                    health = HEALTH_TAMED.toFloat()
                    ownerId = player.uniqueID
                    playTameEffect(true)
                } else {
                    playTameEffect(false)
                }
            }

            return true
        }

        return super.processInteract(player, hand)
    }

    override fun isBreedingItem(stack: ItemStack?): Boolean = false

    override fun createChild(ageable: EntityAgeable?): EntityAgeable? = null

    override fun writeEntityToNBT(compound: NBTTagCompound?) {
        super.writeEntityToNBT(compound)

        val tagList = NBTTagList()
        for (i in 0 until INVENTORY_SIZE) {
            val itemStack = getInventoryContent(i)
            if (itemStack.isEmpty) continue
            val tagCompound = NBTTagCompound()
            tagCompound.setByte(KEY_SLOT, i.toByte())
            itemStack.writeToNBT(tagCompound)
            tagList.appendTag(tagCompound)
        }

        compound!!.setTag(KEY_ITEMS, tagList)

        compound.setFloat(KEY_LID_ANGLE_TARGET, lidAngleTarget)
    }

    override fun readEntityFromNBT(compound: NBTTagCompound?) {
        super.readEntityFromNBT(compound)

        //see net.minecraft.nbt.NBTBase.NBT_TYPES
        val listType = 10
        compound!!.getTagList(KEY_ITEMS, listType).forEach {
            val index = it.getByte(KEY_SLOT).toInt()
            if (index < INVENTORY_SIZE) {
                setInventoryContent(index, ItemStack(it))
            }
        }

        lidAngleTarget = compound.getFloat(KEY_LID_ANGLE_TARGET)
    }

    override fun applyEntityAttributes() {
        super.applyEntityAttributes()

        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).baseValue = 0.30000001192092896
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).baseValue = if (isTamed) HEALTH_TAMED else HEALTH

        attributeMap.registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).baseValue = 6.0
    }

    override fun onDeath(cause: DamageSource?) {
        super.onDeath(cause)

        if (world.isRemote || !isTamed) return
        (0 until INVENTORY_SIZE)
                .map { getInventoryContent(it) }
                .filterNot { it.isEmpty }
                .forEach {
                    dropItem(it.item, it.count)!!.setEntityItemStack(it)
                }
    }

    fun getInventoryContent(index: Int): ItemStack = dataManager.get(INVENTORY_CONTENTS[index])

    fun setInventoryContent(index: Int, itemStack: ItemStack) = dataManager.set(INVENTORY_CONTENTS[index], itemStack)

    fun openLidHalf(sound: Boolean) = openLid(LID_ANGLE_HALF, sound)

    fun openLidFull(sound: Boolean) = openLid(LID_ANGLE_FULL, sound)

    fun openLid(angle: Float, sound: Boolean) {
        if (world.isRemote) return

        lidAngleTarget = angle
        lidAnimationTick = Math.abs(angle / 0.1f + 0.5).toInt()
        if (sound) playLidSound()
    }

    fun closeLid(sound: Boolean) {
        if (world.isRemote) return

        lidAngleTarget = 0.0f
        if (sound) playLidSound()
    }

    fun playLidSound() = playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.8f, 0.5f + rand.nextFloat() * 0.1f)

    private fun openGUI(player: EntityPlayer) {
        player.tags.add(MimicInventoryTag.composeMimicInventoryTag(this))
        openLidFull(true)
        inventoryOpenCount++
        player.openGui(
                FriendlyMimicMod.instance,
                FriendlyMimicMod.MIMIC_GUI_ID,
                world,
                player.posX.toInt(), player.posY.toInt(), player.posZ.toInt())
    }

    companion object {
        const val HEALTH_TAMED = 40.0
        const val HEALTH = 40.0
        const val LID_ANGLE_HALF = -(Math.PI / 4.0).toFloat()
        const val LID_ANGLE_FULL = -(Math.PI / 2.0).toFloat()
        const val INVENTORY_SIZE = 27

        private const val KEY_ITEMS = "Items"
        private const val KEY_SLOT = "Slot"
        private const val KEY_LID_ANGLE_TARGET = "LidAngleTarget"

        private val LID_ANGLE_TARGET = EntityDataManager.createKey<Float>(
                EntityMimic::class.java, DataSerializers.FLOAT
        )
        private val INVENTORY_CONTENTS = Array<DataParameter<ItemStack>>(INVENTORY_SIZE) {
            EntityDataManager.createKey<ItemStack>(EntityMimic::class.java, DataSerializers.OPTIONAL_ITEM_STACK)
        }
        private val INVENTORY_OPEN_COUNT = EntityDataManager.createKey<Int>(EntityMimic::class.java, DataSerializers.VARINT)
    }
}