package com.hbm.blocks.machine.rbmk;

import com.hbm.Tags;
import com.hbm.api.block.IToolable;
import com.hbm.main.MainRegistry;
import com.hbm.render.icon.PaddedSpriteUtil;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.RBMKMiniPanelItemBakedModel;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKGraph;

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

public class RBMKGraph extends RBMKMiniPanelBase implements IToolable {

	public RBMKGraph(String s) {
		super(s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRBMKGraph();
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
		PaddedSpriteUtil.register(map, PaddedSpriteUtil.inspectTexture(RBMKNumitron.PART_SPRITE));
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected IBakedModel createItemModel() {
		HFRWavefrontObject model = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rbmk/numitron.obj"));
		return new RBMKMiniPanelItemBakedModel(this.sprite, Collections.singletonList(
				texturedLayer(model, null, RBMKNumitron.PART_SPRITE, 0xFFFFFF, null, RBMKNumitron.ITEM_UNIT_OFFSETS)));
	}
}
