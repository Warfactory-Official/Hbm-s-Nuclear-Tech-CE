package com.hbm.blocks.machine.rbmk;

import com.google.common.collect.ImmutableSet;
import com.hbm.Tags;
import com.hbm.api.block.IToolable;
import com.hbm.main.MainRegistry;
import com.hbm.render.icon.PaddedSpriteUtil;
import com.hbm.render.icon.PaddedSpriteUtil.TextureInfo;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.RBMKMiniPanelBakedModel;
import com.hbm.render.model.RBMKMiniPanelItemBakedModel;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKIndicator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

public class RBMKIndicator extends RBMKMiniPanelBase implements IToolable {

	private static final Set<String> ITEM_PARTS = ImmutableSet.of("Base", "Light");
	private static final ResourceLocation PART_SPRITE = new ResourceLocation(Tags.MODID, "models/network/indicator");
	private static final float[][] ITEM_UNIT_OFFSETS = new float[][]{
			{0.75F,  0.3125F, 0.25F},
			{0.75F,  0.3125F, 0.75F},
			{0.75F,  0.0F,    0.25F},
			{0.75F,  0.0F,    0.75F},
			{0.75F, -0.3125F, 0.25F},
			{0.75F, -0.3125F, 0.75F},
	};

	public RBMKIndicator(String s) {
		super(s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRBMKIndicator();
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
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
	public void bakeModel(ModelBakeEvent event) {
		if(this.sprite == null) return;

		ModelResourceLocation worldLoc = new ModelResourceLocation(getRegistryName(), "normal");
		ModelResourceLocation invLoc = new ModelResourceLocation(getRegistryName(), "inventory");

		HFRWavefrontObject model = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rbmk/indicator.obj"));
		TextureInfo partTexture = PaddedSpriteUtil.inspectTexture(PART_SPRITE);
		TextureAtlasSprite partSprite = PaddedSpriteUtil.sprite(Minecraft.getMinecraft().getTextureMapBlocks(), partTexture);

		IBakedModel worldModel = new RBMKMiniPanelBakedModel(this.sprite, false);
		IBakedModel itemModel = new RBMKMiniPanelItemBakedModel(model, ITEM_PARTS,
				this.sprite, partSprite, ITEM_UNIT_OFFSETS, partTexture.uScale, partTexture.vScale);

		event.getModelRegistry().putObject(worldLoc, worldModel);
		event.getModelRegistry().putObject(invLoc, itemModel);
	}
}
