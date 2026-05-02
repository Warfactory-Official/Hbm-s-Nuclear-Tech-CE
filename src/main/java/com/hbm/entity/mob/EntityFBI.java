package com.hbm.entity.mob;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.MobConfig;
import com.hbm.entity.mob.ai.EntityAIBreaking;
import com.hbm.entity.mob.ai.EntityAI_MLPF;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.util.Vec3NT;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
@AutoRegister(name = "entity_ntm_fbi", trackingRange = 1000, eggColors = {0x008000, 0x404040})
public class EntityFBI extends EntityMob implements IRangedAttackMob {
	
	public EntityFBI(World world) {
		super(world);
		((PathNavigateGround)this.getNavigator()).setBreakDoors(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIBreaking(this));
        this.tasks.addTask(2, new EntityAIAttackRanged(this, 1D, 20, 25, 15.0F));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(6, new EntityAI_MLPF(this, EntityPlayer.class, 100, 1D, 16));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, 0, false, false, _ -> true));
        this.setSize(0.6F, 1.8F);
        
        this.isImmuneToFire = true;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
	}
	
	@Override
	public boolean attackEntityFrom(@NotNull DamageSource source, float amount) {
		if(source instanceof EntityDamageSourceIndirect && source.getTrueSource() instanceof EntityFBI) {
    		return false;
    	}
		if(!this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty() && this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == Item.getItemFromBlock(Blocks.GLASS)) {
	    	if("oxygenSuffocation".equals(source.damageType))
	    		return false;
	    	if("thermal".equals(source.damageType))
	    		return false;
    	}
		return super.attackEntityFrom(source, amount);
	}
	
	@Override
	protected boolean canDespawn() {
		return false;
	}
	
	@Override
	protected void setEquipmentBasedOnDifficulty(@NotNull DifficultyInstance difficulty) {
		int equip = rand.nextInt(2);

        switch(equip) {
        case 0: this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.gun_heavy_revolver)); break;
        case 1: this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.gun_spas12)); break;
        }
        if(rand.nextInt(5) == 0) {
        	this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.security_helmet));
        	this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.security_plate));
        	this.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ModItems.security_legs));
        	this.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ModItems.security_boots));
        }

        if(this.world != null && this.world.provider.getDimension() != 0) {
        	this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Blocks.GLASS));
        	this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.paa_plate));
        	this.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ModItems.paa_legs));
        	this.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ModItems.paa_boots));
        }
	}
	
	@Override
	public boolean isPotionApplicable(@NotNull PotionEffect potioneffectIn) {
    	if(this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty())
            this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.gas_mask_m65));

        return false;
	}
	
	@Override
	public boolean isAIDisabled() {
		return false;
	}
	
	//combat vest = full diamond set
	@Override
	public int getTotalArmorValue() {
		return 20;
	}

	@Override
	public void attackEntityWithRangedAttack(@NotNull EntityLivingBase target, float distanceFactor) {
	}

	private static final Set<Block> canDestroy = new HashSet<>();

	static {
		canDestroy.add(Blocks.ACACIA_DOOR);
		canDestroy.add(Blocks.BIRCH_DOOR);
		canDestroy.add(Blocks.DARK_OAK_DOOR);
		canDestroy.add(Blocks.JUNGLE_DOOR);
		canDestroy.add(Blocks.OAK_DOOR);
		canDestroy.add(Blocks.SPRUCE_DOOR);
		canDestroy.add(Blocks.IRON_DOOR);
		canDestroy.add(Blocks.TRAPDOOR);
		canDestroy.add(ModBlocks.machine_press);
		canDestroy.add(ModBlocks.machine_epress);
		canDestroy.add(ModBlocks.machine_crystallizer);
		canDestroy.add(ModBlocks.machine_turbine);
		canDestroy.add(ModBlocks.machine_large_turbine);
		canDestroy.add(ModBlocks.crate_iron);
		canDestroy.add(ModBlocks.crate_steel);
		canDestroy.add(ModBlocks.machine_diesel);
		canDestroy.add(ModBlocks.machine_rtg_grey);
		canDestroy.add(ModBlocks.machine_minirtg);
		canDestroy.add(ModBlocks.machine_powerrtg);
		canDestroy.add(ModBlocks.machine_cyclotron);
		canDestroy.add(Blocks.CHEST);
		canDestroy.add(Blocks.TRAPPED_CHEST);
	}
	
	@Override
	public IEntityLivingData onInitialSpawn(@NotNull DifficultyInstance difficulty, IEntityLivingData livingdata) {
		this.setEquipmentBasedOnDifficulty(difficulty);
		return super.onInitialSpawn(difficulty, livingdata);
	}
	
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if(world.isRemote || this.getHealth() <= 0)
    		return;

    	if(this.ticksExisted % MobConfig.raidAttackDelay == 0) {
    		Vec3NT vec = new Vec3NT(MobConfig.raidAttackReach, 0, 0);
    		vec.rotateAroundYRad((float)(Math.PI * 2) * rand.nextFloat());

			Vec3NT vec3 = new Vec3NT(this.posX, this.posY + 0.5 + rand.nextFloat(), this.posZ);
			Vec3NT vec31 = new Vec3NT(vec3.x + vec.x, vec3.y + vec.y, vec3.z + vec.z);
            RayTraceResult mop = this.world.rayTraceBlocks(vec3.toVec3d(), vec31.toVec3d(), false, true, false);

            if(mop != null && mop.typeOfHit == Type.BLOCK) {

            	if(canDestroy.contains(world.getBlockState(mop.getBlockPos())))
            		world.destroyBlock(mop.getBlockPos(), false);
            }
            double range = 1.5;

        	List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(posX, posY, posZ, posX, posY, posZ).grow(range, range, range));

        	for(EntityItem item : items)
        		item.setFire(10);
    	}
	}
	
	@Override
	public void setSwingingArms(boolean swingingArms) {
		
	}
}
