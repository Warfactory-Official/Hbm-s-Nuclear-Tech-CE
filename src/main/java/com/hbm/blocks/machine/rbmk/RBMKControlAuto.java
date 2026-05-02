package com.hbm.blocks.machine.rbmk;

import com.hbm.blocks.ModBlocks;
import com.hbm.render.model.RBMKControlBakedModel;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKControlAuto;
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

public class RBMKControlAuto extends RBMKPipedBase {

	@SideOnly(Side.CLIENT) private TextureAtlasSprite bottomSprite;
	@SideOnly(Side.CLIENT) private TextureAtlasSprite lidSprite;

	public RBMKControlAuto(String s, String c){
		super(s, c);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		
		if(meta >= offset)
			return new TileEntityRBMKControlAuto();
		return null;
	}
	
	@Override
	public boolean onBlockActivated(@NotNull World worldIn, BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ){
		return openInv(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn, hand);
	}


    @Override
	public void registerSprite(TextureMap map) {
		super.registerSprite(map);
		if(this == ModBlocks.rbmk_control_reasim_auto) this.bottomSprite = map.registerSprite(new ResourceLocation("hbm", "blocks/rbmk/" + columnTexture + "_bottom"));
		else this.bottomSprite = topSprite;
		this.lidSprite = map.registerSprite(new ResourceLocation("hbm", "blocks/rbmk/rbmk_control_auto"));
	}

	@Override
	public void bakeModel(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "inventory"),
				new RBMKControlBakedModel(topSprite, sideSprite, pipeTop, pipeSide, null, null, null, null, lidSprite, true).setBottomSprite(bottomSprite));

		event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "normal"),
				new RBMKControlBakedModel(topSprite, sideSprite, pipeTop, pipeSide, null, null, null, null, lidSprite, false).setBottomSprite(bottomSprite));
	}
}
