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
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumHand
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

/**
 * Created by gedorinku on 2017/03/27.
 */
class EntityMimic : EntityTameable {

    var lidAngle = 0.0f
    var lidAngleTarget = 0.0f
        get
        private set

    val inventoryContents: Array<ItemStack> = Array<ItemStack>(27) {
        ItemStack.EMPTY
    }

    constructor(world: World) : super(world) {
        setSize(1.0f, 1.0f)
        isTamed = false
    }

    override fun initEntityAI() {
        aiSit = EntityAISit(this)
        tasks.addTask(1, EntityAISwimming(this))
        tasks.addTask(2, aiSit)
        tasks.addTask(3, EntityAILeapAtTarget(this, 0.4f))
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
        openLidHalf()
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
                openLidHalf()

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
                    health = HEALTH_FRIENDLY.toFloat()
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
        inventoryContents
                .forEachIndexed({ index, itemStack ->
                    if (!itemStack.isEmpty) {
                        val tagCompound = NBTTagCompound()
                        tagCompound.setByte("Slot", index.toByte())
                        itemStack.writeToNBT(tagCompound)
                        tagList.appendTag(tagCompound)
                    }
                })

        compound!!.setTag("Items", tagList)
    }

    override fun readEntityFromNBT(compound: NBTTagCompound?) {
        super.readEntityFromNBT(compound)

        //see net.minecraft.nbt.NBTBase.NBT_TYPES
        val listType = 10
        compound!!.getTagList("Items", listType).forEach {
            val index = it.getByte("Slot").toInt()
            if (index < inventoryContents.size) {
                inventoryContents[index] = ItemStack(it)
            }
        }
    }

    override fun applyEntityAttributes() {
        super.applyEntityAttributes()

        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).baseValue = 0.30000001192092896
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).baseValue = if (isTamed) HEALTH_FRIENDLY else HEALTH

        attributeMap.registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).baseValue = 2.0
    }

    @SideOnly(Side.CLIENT)
    fun openLidHalf() {
        lidAngleTarget = LID_ANGLE_HALF_OPEN
        playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.8f, 0.5f + rand.nextFloat() * 0.1f)
    }

    @SideOnly(Side.CLIENT)
    fun closeLid() {
        lidAngleTarget = 0.0f
        playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.8f, 0.5f + rand.nextFloat() * 0.1f)
    }

    private fun openGUI(player: EntityPlayer) {
        player.tags.add(MimicInventoryTag.composeMimicInventoryTag(this))
        player.openGui(
                FriendlyMimicMod.instance,
                FriendlyMimicMod.MIMIC_GUI_ID,
                world,
                player.posX.toInt(), player.posY.toInt(), player.posZ.toInt())
    }

    companion object {
        const val HEALTH_FRIENDLY = 20.0
        const val HEALTH = 8.0
        const val LID_ANGLE_HALF_OPEN = -(Math.PI / 4.0).toFloat()
    }
}