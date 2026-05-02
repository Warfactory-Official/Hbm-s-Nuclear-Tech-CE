package com.hbm.blocks.bomb;

import com.google.common.collect.ImmutableMap;
import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.interfaces.IBomb;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.render.block.RotatableStateMapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

public class BlockPlasticExplosive extends BlockDetonatable implements IBomb {
    public static final PropertyDirection FACING = BlockDirectional.FACING;

    public BlockPlasticExplosive(Material mat, SoundType soundType, String registryName, BlockBakeFrame blockFrame) {
        super(mat, registryName, 0, 0, 0, false, false, blockFrame);
        this.blockSoundType = soundType;
        this.META_COUNT = 0;
    }

    //I am not proud of this either
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new RotatableStateMapper(loc, FACING);
    }

    @Override
    public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {

        if (!world.isRemote) {
            new ExplosionVNT(world, pos, 20).makeStandard().setBlockProcessor(new BlockProcessorStandard().setNoDrop()).explode();
        }

        return BombReturnCode.DETONATED;
    }

    @Override
    public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
        explode(world, new BlockPos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z)), entity);//FIXME: This loses track of original detonator
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        var blockFrame = blockFrames[0];
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(blockFrame.getBaseModelLocation());

            ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();
            blockFrame.putTextures(textureMap);

            IModel retexturedModel = baseModel.retexture(textureMap.build());
            IBakedModel[] models = new IBakedModel[6];

            for (EnumFacing facing : EnumFacing.VALUES) {
                int xRot = BlockBakeFrame.getXRotationForFacing(facing);
                int yRot = BlockBakeFrame.getYRotationForFacing(facing);

                models[facing.ordinal()] = retexturedModel.bake(
                        ModelRotation.getModelRotation(xRot, yRot),
                        DefaultVertexFormats.BLOCK,
                        ModelLoader.defaultTextureGetter()
                );
            }

            // Inventory model = UP-facing variant
            ModelResourceLocation invLocation = new ModelResourceLocation(
                    Objects.requireNonNull(getRegistryName()), "inventory");
            event.getModelRegistry().putObject(invLocation, models[EnumFacing.UP.ordinal()]);

            // World variants
            for (EnumFacing facing : EnumFacing.VALUES) {
                ModelResourceLocation worldLoc = new ModelResourceLocation(
                        Objects.requireNonNull(getRegistryName()),
                        "facing=" + facing.getName()
                );
                event.getModelRegistry().putObject(worldLoc, models[facing.ordinal()]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(Objects.requireNonNull(this.getRegistryName()), "inventory"));
    }

    @Override
    public void registerSprite(TextureMap map) {
        blockFrames[0].registerBlockTextures(map);
    }


    //The ugly stuff, aka forcing all overridden methods to bypass BlockMeta's changes
    @Override
    public void getDrops(NonNullList list, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        ((Block) this).getDrops(list, world, pos, state, fortune);
    }

    @Override
    public void registerItem() {
        ItemBlock itemBlock = new ItemBlock(this);
        itemBlock.setRegistryName(this.getRegistryName());
        itemBlock.setCreativeTab(this.getCreativeTab());
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(this), 1, 0);
    }
}
