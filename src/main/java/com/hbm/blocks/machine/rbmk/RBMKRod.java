package com.hbm.blocks.machine.rbmk;

import com.hbm.handler.BossSpawnHandler;
import com.hbm.render.model.RBMKRodBakedModel;
import com.hbm.tileentity.TileEntityProxyInventory;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKRod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
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

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {

		if(meta >= offset)
			return new TileEntityRBMKRod();

		if(hasExtra(meta))
			return new TileEntityProxyInventory();

		return null;
	}

	@Override
	public boolean onBlockActivated(@NotNull World worldIn, BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ){
		BossSpawnHandler.markFBI(playerIn);
		return openInv(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn, hand);
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
				new RBMKRodBakedModel(sideSprite, innerSprite, topSprite, coverTopSprite, coverSideSprite, glassTopSprite, glassSideSprite));

		event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "normal"),
				new RBMKRodBakedModel(sideSprite, innerSprite, topSprite, coverTopSprite, coverSideSprite, glassTopSprite, glassSideSprite));
	}

}
