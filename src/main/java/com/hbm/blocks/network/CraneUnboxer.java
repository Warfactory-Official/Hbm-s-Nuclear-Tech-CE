package com.hbm.blocks.network;

import com.hbm.Tags;
import com.hbm.api.conveyor.IConveyorItem;
import com.hbm.api.conveyor.IConveyorPackage;
import com.hbm.api.conveyor.IEnterableBlock;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.InventoryHelper;
import com.hbm.tileentity.network.TileEntityCraneBase;
import com.hbm.tileentity.network.TileEntityCraneUnboxer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class CraneUnboxer extends BlockCraneBase implements IEnterableBlock {
    public CraneUnboxer(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntityCraneBase createNewTileEntity(@NotNull World world, int meta) {
        return new TileEntityCraneUnboxer();
    }

    @Override
    public void registerSprite(TextureMap map) {
        super.registerSprite(map);
        this.iconIn = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_box"));
        this.iconSideIn = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_side_box"));
        this.iconDirectional = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_unboxer_top"));
        this.iconDirectionalUp = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_unboxer_side_up"));
        this.iconDirectionalDown = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_unboxer_side_down"));
        this.iconDirectionalTurnLeft = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_unboxer_top_left"));
        this.iconDirectionalTurnRight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_unboxer_top_right"));
        this.iconDirectionalSideLeftTurnUp = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_unboxer_side_left_turn_up"));
        this.iconDirectionalSideRightTurnUp = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_unboxer_side_right_turn_up"));
        this.iconDirectionalSideLeftTurnDown = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_unboxer_side_left_turn_down"));
        this.iconDirectionalSideRightTurnDown = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_unboxer_side_right_turn_down"));
        this.iconDirectionalSideUpTurnLeft = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_unboxer_side_up_turn_left"));
        this.iconDirectionalSideUpTurnRight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_unboxer_side_up_turn_right"));
        this.iconDirectionalSideDownTurnLeft = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_unboxer_side_down_turn_left"));
        this.iconDirectionalSideDownTurnRight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_unboxer_side_down_turn_right"));
    }

    @Override
    public boolean canItemEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorItem entity) {
        return false;
    }

    @Override
    public void onItemEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorItem entity) { }

    @Override
    public boolean canPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        EnumFacing orientation = state.getValue(FACING);
        return dir == orientation;
    }

    @Override
    public void onPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        boolean worked;
        if (te instanceof TileEntityCraneUnboxer) {

            for (ItemStack stack : entity.getItemStacks()) {
                if(stack == null || stack.isEmpty()) continue;
                worked = ((TileEntityCraneUnboxer)te).tryFillTeDirect(stack);

                if (stack.getCount() > 0 || !worked) {
                    EntityItem drop = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack.copy());
                    world.spawnEntity(drop);
                }
            }
        }
    }

    @Override
    public boolean hasComparatorInputOverride(@NotNull IBlockState blockState) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, @NotNull World world, @NotNull BlockPos pos) {
        return blockState.getComparatorInputOverride(world, pos);
    }

    @Override
    public void breakBlock(World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        TileEntity tileentity = world.getTileEntity(pos);

        if(tileentity instanceof TileEntityCraneUnboxer) {
            InventoryHelper.dropInventoryItems(world, pos, tileentity);
        }
        super.breakBlock(world, pos, state);
    }
}
