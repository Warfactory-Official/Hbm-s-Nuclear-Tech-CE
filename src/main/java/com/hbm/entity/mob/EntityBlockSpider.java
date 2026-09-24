package com.hbm.entity.mob;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

@AutoRegister(name = "entity_taintcrawler", trackingRange = 1000)
public class EntityBlockSpider extends EntityMob {

	private static final DataParameter<Integer> BLOCK = EntityDataManager.createKey(EntityBlockSpider.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> META = EntityDataManager.createKey(EntityBlockSpider.class, DataSerializers.VARINT);

	public EntityBlockSpider(World world) {
		super(world);

		this.setSize(0.95F, 1.25F);
		this.tasks.addTask(1, new EntityAIWander(this, 0.5F));
		this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, 0, true, false, null));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(BLOCK, 1);
		this.dataManager.register(META, 0);
	}

	public void makeBlock(Block block, int meta) {
		this.dataManager.set(BLOCK, Block.getIdFromBlock(block));
		this.dataManager.set(META, meta);

		double health = Math.max(1D, block.getExplosionResistance(null));

		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
		this.setHealth(this.getMaxHealth());
	}

	public IBlockState getRenderState() {
		Block block = Block.getBlockById(this.dataManager.get(BLOCK));
		return block.getStateFromMeta(this.dataManager.get(META) & 15);
	}
}
