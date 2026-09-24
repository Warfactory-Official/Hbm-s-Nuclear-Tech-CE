package com.hbm.blocks.machine.rbmk;

import com.google.common.collect.ImmutableSet;
import com.hbm.Tags;
import com.hbm.api.block.IToolable;
import com.hbm.main.MainRegistry;
import com.hbm.render.icon.PaddedSpriteUtil;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.RBMKMiniPanelItemBakedModel;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKKeyPad;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

public class RBMKKeyPad extends RBMKMiniPanelBase implements IToolable {

	private static final ResourceLocation PART_SPRITE = new ResourceLocation(Tags.MODID, "models/network/keypad");
	private static final float[][] ITEM_UNIT_OFFSETS = {
			{-0.25F, 0.25F, 0.25F},
			{0.25F, 0.25F, 0.25F},
			{-0.25F, -0.25F, 0.25F},
			{0.25F, -0.25F, 0.25F},
	};

	public RBMKKeyPad(String s) {
		super(s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRBMKKeyPad();
	}

	@Override
	public boolean onScrew(World world,EntityPlayer player,int x,int y,int z,EnumFacing side,float fX,float fY,float fZ,EnumHand hand,ToolType tool) {
		if(tool != ToolType.SCREWDRIVER) return false;
		if(world.isRemote) FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
		return true;
	}

	@Override
	public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float hitX,float hitY,float hitZ) {
		if(world.isRemote) return true;
		if(player.isSneaking()) return false;

		if(hitX != 0 && hitX != 1 && hitZ != 0 && hitZ != 1 && side != EnumFacing.DOWN && side != EnumFacing.UP) {

			TileEntityRBMKKeyPad tile = (TileEntityRBMKKeyPad) world.getTileEntity(pos);
			int meta = world.getBlockState(pos).getValue(FACING).getIndex();

			int indexHit = 0;

			if(meta == 2 && hitX < 0.5) indexHit = 1;
			if(meta == 3 && hitX > 0.5) indexHit = 1;
			if(meta == 4 && hitZ > 0.5) indexHit = 1;
			if(meta == 5 && hitZ < 0.5) indexHit = 1;

			if(hitY < 0.5) indexHit += 2;

			if(!tile.keys[indexHit].active) return false;
			tile.keys[indexHit].click();
		}

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerSprite(TextureMap map) {
		super.registerSprite(map);
		PaddedSpriteUtil.register(map, PaddedSpriteUtil.inspectTexture(PART_SPRITE));
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected IBakedModel createItemModel() {
		HFRWavefrontObject model = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rbmk/button.obj"));
		return new RBMKMiniPanelItemBakedModel(this.sprite, Arrays.asList(
				texturedLayer(model, ImmutableSet.of("Socket"), PART_SPRITE, 0xFFFFFF, null, ITEM_UNIT_OFFSETS),
				texturedLayer(model, ImmutableSet.of("Button"), PART_SPRITE, 0xA60000, null, ITEM_UNIT_OFFSETS)));
	}
}
