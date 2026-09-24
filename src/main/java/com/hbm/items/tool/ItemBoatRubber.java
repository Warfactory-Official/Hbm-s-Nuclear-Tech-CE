package com.hbm.items.tool;

import com.hbm.entity.item.EntityBoatRubber;
import com.hbm.items.ItemBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemBoatRubber extends ItemBase {

	public ItemBoatRubber(String s) {
		super(s);
		this.maxStackSize = 1;
		this.setCreativeTab(CreativeTabs.TRANSPORTATION);
	}

	@Override
	public @NotNull ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @NotNull EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		float f = 1.0F;
		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
		float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
		double posX = player.prevPosX + (player.posX - player.prevPosX) * (double) f;
		double posY = player.prevPosY + (player.posY - player.prevPosY) * (double) f + (double) player.getEyeHeight();
		double posZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) f;
		float compZ = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
		float compX = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
		float mult = -MathHelper.cos(-pitch * 0.017453292F);
		float lookY = MathHelper.sin(-pitch * 0.017453292F);
		float lookX = compX * mult;
		float lookZ = compZ * mult;
		double reach = 5.0D;

		Vec3d pos = new Vec3d(posX, posY, posZ);
		Vec3d target = pos.add(new Vec3d(lookX * reach, lookY * reach, lookZ * reach));
		RayTraceResult mop = world.rayTraceBlocks(pos, target, true);

		if(mop == null) {
			return ActionResult.newResult(EnumActionResult.PASS, stack);
		}

		Vec3d look = player.getLook(f);
		boolean flag = false;
		double width = 1.0D;
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(look.x * reach, look.y * reach, look.z * reach).grow(width, width, width));

		for(Entity entity : list) {

			if(entity.canBeCollidedWith()) {
				float border = entity.getCollisionBorderSize();
				AxisAlignedBB box = entity.getEntityBoundingBox().grow(border, border, border);

				if(box.contains(pos)) {
					flag = true;
				}
			}
		}

		if(flag) {
			return ActionResult.newResult(EnumActionResult.PASS, stack);
		}

		if(mop.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos hit = mop.getBlockPos();
			IBlockState state = world.getBlockState(hit);

			if(state.getBlock() == Blocks.SNOW_LAYER) {
				hit = hit.down();
			}

			EntityBoatRubber boat = new EntityBoatRubber(world, hit.getX() + 0.5D, hit.getY() + 1.0D, hit.getZ() + 0.5D);
			boat.rotationYaw = (float) (((MathHelper.floor((player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3) - 1) * 90);

			if(!world.getCollisionBoxes(boat, boat.getEntityBoundingBox().grow(-0.1D, -0.1D, -0.1D)).isEmpty()) {
				return ActionResult.newResult(EnumActionResult.FAIL, stack);
			}

			if(!world.isRemote) {
				world.spawnEntity(boat);
			}

			if(!player.capabilities.isCreativeMode) {
				stack.shrink(1);
			}
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}
}
