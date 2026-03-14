package com.hbm.blocks.network;

import com.hbm.Tags;
import com.hbm.api.block.IBlockSideRotation;
import com.hbm.api.block.IToolable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemTooling;
import com.hbm.main.MainRegistry;
import com.hbm.render.model.CraneBakedModel;
import com.hbm.tileentity.network.TileEntityCraneBase;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

//mlbv: i have zero fucking idea how the direction ids are mapped to EnumFacing..
// Th3_Sl1ze: down-up-north-south-east-west. 0-1-2-3-4-5 respectively
public abstract class BlockCraneBase extends BlockContainer implements IToolable, ITooltipProvider, IDynamicModels, IBlockSideRotation {
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final IUnlistedProperty<EnumFacing> OUTPUT_OVERRIDE = new Properties.PropertyAdapter<>(PropertyDirection.create("output_override"));

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconSide;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconTop;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconIn;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconSideIn;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconOut;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconSideOut;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconDirectional;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconDirectionalUp;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconDirectionalDown;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconDirectionalTurnLeft;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconDirectionalTurnRight;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconDirectionalSideLeftTurnUp;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconDirectionalSideRightTurnUp;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconDirectionalSideLeftTurnDown;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconDirectionalSideRightTurnDown;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconDirectionalSideUpTurnLeft;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconDirectionalSideUpTurnRight;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconDirectionalSideDownTurnLeft;
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconDirectionalSideDownTurnRight;


    public BlockCraneBase(Material mat) {
        super(mat);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        IDynamicModels.INSTANCES.add(this);
    }

    @Override
    public abstract TileEntityCraneBase createNewTileEntity(@NotNull World worldIn, int meta);

    @Override
    public boolean onBlockActivated(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        Item heldItem = playerIn.getHeldItem(hand).getItem();
        if (heldItem instanceof ItemTooling || heldItem == ModItems.conveyor_wand) {
            return false;
        } else if(worldIn.isRemote) {
            return true;
        } else if(!playerIn.isSneaking()) {
            playerIn.openGui(MainRegistry.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, @NotNull BlockPos pos, IBlockState state, EntityLivingBase placer, @NotNull ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) return false;
        if (!(world.getTileEntity(new BlockPos(x, y, z)) instanceof TileEntityCraneBase craneTileEntity)) return false;
        if (player.isSneaking()) {
            craneTileEntity.setOutputOverride(side);
        } else {
            craneTileEntity.setInput(side);
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof IInventory) {
            InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory)tileentity);
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }


    @Override
    public @NotNull IBlockState getStateForPlacement(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, @NotNull EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public @NotNull IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{FACING}, new IUnlistedProperty[]{OUTPUT_OVERRIDE});
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityCraneBase te = (TileEntityCraneBase) world.getTileEntity(pos);
        if (te != null) {
            EnumFacing output = te.getOutputSide();
            return ((IExtendedBlockState) state).withProperty(OUTPUT_OVERRIDE, output);
        }
        return state;
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public int getRotationFromSide(IBlockAccess world, BlockPos pos, EnumFacing side) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) return 0;
        EnumFacing facing = state.getValue(FACING);
        if (facing.getAxis().isHorizontal() && side == EnumFacing.UP) {
            return switch (facing) {
                case NORTH -> 3;
                case EAST -> 1;
                case WEST -> 2;
                default -> 0;
            };
        }
        return 0;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        iconSide = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_side"));
        iconTop = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_top"));
        iconIn = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in"));
        iconSideIn = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_side_in"));
        iconOut = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out"));
        iconSideOut = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_side_out"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            IBakedModel bakedModel = new CraneBakedModel(this);
            event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "normal"), bakedModel);
            event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "inventory"), bakedModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected @NotNull ModelResourceLocation getModelResourceLocation(@NotNull IBlockState state) {
                return new ModelResourceLocation(loc, "normal");
            }
        };
    }
}
