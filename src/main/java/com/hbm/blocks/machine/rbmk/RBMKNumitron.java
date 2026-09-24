package com.hbm.blocks.machine.rbmk;

import com.hbm.Tags;
import com.hbm.api.block.IToolable;
import com.hbm.main.MainRegistry;
import com.hbm.render.icon.PaddedSpriteUtil;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.RBMKMiniPanelItemBakedModel;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKNumitron;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;

public class RBMKNumitron extends RBMKMiniPanelBase implements IToolable {

	static final ResourceLocation PART_SPRITE = new ResourceLocation(Tags.MODID, "models/network/numitron");
	static final float[][] ITEM_UNIT_OFFSETS = {
			{0.0F, 0.25F, 0.25F},
			{0.0F, -0.25F, 0.25F},
	};

	public RBMKNumitron(String s) {
		super(s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRBMKNumitron();
	}

	@Override
	public boolean onScrew(World world,EntityPlayer player,int x,int y,int z,EnumFacing side,float fX,float fY,float fZ,EnumHand hand,ToolType tool) {
		if(tool != ToolType.SCREWDRIVER) return false;
		if(world.isRemote) FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
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
		HFRWavefrontObject model = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rbmk/numitron.obj"));
		return new RBMKMiniPanelItemBakedModel(this.sprite, Collections.singletonList(
				texturedLayer(model, null, PART_SPRITE, 0xFFFFFF, null, ITEM_UNIT_OFFSETS)));
	}
}
