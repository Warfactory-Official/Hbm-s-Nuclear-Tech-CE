package com.hbm.blocks.machine;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.generic.BlockBakeBase;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BlockCMAnchor extends BlockBakeBase {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public BlockCMAnchor(String registryName) {
		super(Material.IRON, registryName, BlockBakeFrame.southFacingCube("cmt_terminal_side", "cmt_terminal_front"));
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	protected @NotNull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

	@Override
	public @NotNull IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public @NotNull IBlockState getStateForPlacement(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void bakeModel(ModelBakeEvent event) {
		try {
			IModel baseModel = ModelLoaderRegistry.getModel(blockFrame.getBaseModelLocation());
			ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();

			blockFrame.putTextures(textureMap);
			IModel retexturedModel = baseModel.retexture(textureMap.build());
			IBakedModel[] models = new IBakedModel[4];
			for (int i = 0; i < EnumFacing.HORIZONTALS.length; i++) {
				EnumFacing facing = EnumFacing.HORIZONTALS[i];
				models[i] = retexturedModel.bake(
						ModelRotation.getModelRotation(0, BlockBakeFrame.getYRotationForFacing(facing)), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
				);
			}
			ModelResourceLocation modelLocation = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory");
			event.getModelRegistry().putObject(modelLocation, models[2]);
			for (int index = 0; index < models.length; index++) {
				ModelResourceLocation worldLocation = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "facing=" + EnumFacing.HORIZONTALS[index].getName());
				event.getModelRegistry().putObject(worldLocation, models[index]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
