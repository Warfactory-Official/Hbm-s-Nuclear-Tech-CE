package com.hbm.items.special;

import com.hbm.entity.mob.EntityDuck;
import com.hbm.entity.mob.EntityHunterChopper;
import com.hbm.entity.mob.EntityUFO;
import com.hbm.entity.mob.botprime.EntityBOTPrimeHead;
import com.hbm.items.ModItems;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;

import java.util.List;

public class ItemChopper extends Item {

	public ItemChopper(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return EnumActionResult.SUCCESS;
		} else {
			ItemStack stack = player.getHeldItem(hand);
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			
			//IBlockState blockState = world.getBlockState(pos);

			x += facing.getXOffset();
			y += facing.getYOffset();
			z += facing.getZOffset();
			double offset = 0.0D;

			//Drillgon200: No clue what 11 is supposed to mean. I'll just leave it and hope it doesn't break anything.
			//if(facing.ordinal() == 1 && blockState.getRenderType() == 11)
			//	offset = 0.5D;

			Entity entity = spawnCreature(world, stack.getItemDamage(), x + 0.5D, y + offset, z + 0.5D);

			if(entity != null) {
				if(entity instanceof EntityLivingBase && stack.hasDisplayName()) {
					((EntityLiving) entity).setCustomNameTag(stack.getDisplayName());
				}

				if(!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}
			}

			return EnumActionResult.SUCCESS;
		}
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if(world.isRemote) {
			return ActionResult.newResult(EnumActionResult.PASS, stack);

		} else {
			RayTraceResult RayTraceResult = this.rayTrace(world, player, true);

			if(RayTraceResult == null || RayTraceResult.typeOfHit == Type.MISS) {
				return ActionResult.newResult(EnumActionResult.PASS, stack);
			} else {
				if(RayTraceResult.typeOfHit == Type.BLOCK) {
					int i = RayTraceResult.getBlockPos().getX();
					int j = RayTraceResult.getBlockPos().getY();
					int k = RayTraceResult.getBlockPos().getZ();

					if(!world.canMineBlockBody(player, RayTraceResult.getBlockPos())) {
						return ActionResult.newResult(EnumActionResult.PASS, stack);
					}

					if(!player.canPlayerEdit(RayTraceResult.getBlockPos(), RayTraceResult.sideHit, stack)) {
						return ActionResult.newResult(EnumActionResult.PASS, stack);
					}

					if(world.getBlockState(RayTraceResult.getBlockPos()).getBlock() instanceof BlockLiquid) {
						Entity entity = spawnCreature(world, stack.getItemDamage(), i, j, k);

						if(entity != null) {
							if(entity instanceof EntityLivingBase && stack.hasDisplayName()) {
								((EntityLiving) entity).setCustomNameTag(stack.getDisplayName());
							}

							if(!player.capabilities.isCreativeMode) {
								stack.shrink(1);
							}
						}
					}
				}

				return ActionResult.newResult(EnumActionResult.PASS, stack);
			}
		}
	}
	
	public Entity spawnCreature(World world, int dmg, double x, double y, double z) {
		Entity entity = null;

		if(this == ModItems.spawn_chopper)
			entity = new EntityHunterChopper(world);

		if(this == ModItems.spawn_worm)
			entity = new EntityBOTPrimeHead(world);

		if(this == ModItems.spawn_ufo) {
			entity = new EntityUFO(world);
			((EntityUFO)entity).scanCooldown = 100;
			y += 35;
		}

		if(this == ModItems.spawn_duck)
			entity = new EntityDuck(world);

		if(entity != null) {

			EntityLiving entityliving = (EntityLiving) entity;
			entity.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(world.rand.nextFloat() * 360.0F), 0.0F);
			entityliving.rotationYawHead = entityliving.rotationYaw;
			entityliving.renderYawOffset = entityliving.rotationYaw;
			entityliving.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(x, y, z)), (IEntityLivingData) null);
			world.spawnEntity(entity);
		}

		return entity;
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(this == ModItems.spawn_worm) {
			tooltip.add("Without a player in survival mode");
			tooltip.add("to target, he struggles around a lot.");
			tooltip.add("");
			tooltip.add("He's doing his best so please show him");
			tooltip.add("some consideration.");
		}
	}
}
