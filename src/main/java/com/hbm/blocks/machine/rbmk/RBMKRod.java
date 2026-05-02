package com.hbm.blocks.machine.rbmk;

import com.hbm.handler.BossSpawnHandler;
import com.hbm.items.machine.ItemRBMKRod;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.model.RBMKRodBakedModel;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.TileEntityProxyInventory;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKBase;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKRod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class RBMKRod extends RBMKBase {

	@SideOnly(Side.CLIENT) protected TextureAtlasSprite innerSprite;
	@SideOnly(Side.CLIENT) protected TextureAtlasSprite fuelSprite;

	public boolean moderated;

	public RBMKRod(boolean moderated, String s, String c) {
		super(s, c);
		this.moderated = moderated;
	}

	@Override public boolean isFullCube(@NotNull IBlockState state) { return false; }

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta >= offset) return new TileEntityRBMKRod();
		if(hasExtra(meta)) return new TileEntityProxyCombo().inventory();
		return null;
	}

	@Override
	public boolean onBlockActivated(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ){
		BossSpawnHandler.markFBI(playerIn);
		if(worldIn.isRemote) return true;

		TileEntity te = this.findCoreTE(worldIn, pos);
		if(!(te instanceof TileEntityRBMKRod rbmk)) return false;
		if(!playerIn.getHeldItem(hand).isEmpty() && playerIn.getHeldItem(hand).getItem() instanceof ItemRBMKRod && rbmk.inventory.getStackInSlot(0).isEmpty()) {
			ItemStack rod = playerIn.getHeldItem(hand).copy();
			rod.setCount(1);
			rbmk.inventory.setStackInSlot(0, rod);
			if(!playerIn.capabilities.isCreativeMode) playerIn.getHeldItem(hand).shrink(1);
			worldIn.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.0F, 1.0F);
			return false;
		}
		return openInv(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn, hand);
	}

	@Override
	public void breakBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
		int meta = getMetaFromState(state);

		if(meta >= offset && !RBMKDials.getMeltdownsDisabled(world)) {
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof TileEntityRBMKRod tile) {
                if(TileEntityRBMKBase.explodeOnBroken) {
					if(!tile.inventory.getStackInSlot(0).isEmpty() && tile.inventory.getStackInSlot(0).getItem() instanceof ItemRBMKRod && ItemRBMKRod.getHullHeat(tile.inventory.getStackInSlot(0)) >= 1500) {
						tile.meltdown();
					}
				}
			}
		}

		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void registerSprite(TextureMap map) {
		super.registerSprite(map);
		this.innerSprite = map.registerSprite(new ResourceLocation("hbm", "blocks/rbmk/rbmk_element_inner"));
		this.fuelSprite = map.registerSprite(new ResourceLocation("hbm", "blocks/rbmk/rbmk_element_fuel"));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void bakeModel(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "inventory"),
				new RBMKRodBakedModel(sideSprite, innerSprite, topSprite, coverTopSprite, coverSideSprite, glassTopSprite, glassSideSprite, true));

		event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "normal"),
				new RBMKRodBakedModel(sideSprite, innerSprite, topSprite, coverTopSprite, coverSideSprite, glassTopSprite, glassSideSprite, false));
	}

}
