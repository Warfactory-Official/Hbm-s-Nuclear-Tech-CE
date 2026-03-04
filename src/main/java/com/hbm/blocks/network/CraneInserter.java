package com.hbm.blocks.network;

import com.hbm.Tags;
import com.hbm.api.conveyor.IConveyorItem;
import com.hbm.api.conveyor.IConveyorPackage;
import com.hbm.api.conveyor.IEnterableBlock;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.InventoryHelper;
import com.hbm.tileentity.network.TileEntityCraneBase;
import com.hbm.tileentity.network.TileEntityCraneInserter;
import net.minecraft.block.BlockHorizontal;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class CraneInserter extends BlockCraneBase implements IEnterableBlock {
    public CraneInserter(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntityCraneBase createNewTileEntity(@NotNull World world, int meta) {
        return new TileEntityCraneInserter();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        super.registerSprite(map);
        this.iconDirectional = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in_top"));
        this.iconDirectionalUp = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in_side_up"));
        this.iconDirectionalDown = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in_side_down"));
        this.iconDirectionalTurnLeft = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in_top_left"));
        this.iconDirectionalTurnRight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in_top_right"));
        this.iconDirectionalSideLeftTurnUp = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in_side_left_turn_up"));
        this.iconDirectionalSideRightTurnUp = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in_side_right_turn_up"));
        this.iconDirectionalSideLeftTurnDown = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in_side_left_turn_down"));
        this.iconDirectionalSideRightTurnDown = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in_side_right_turn_down"));
        this.iconDirectionalSideUpTurnLeft = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in_side_up_turn_left"));
        this.iconDirectionalSideUpTurnRight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in_side_up_turn_right"));
        this.iconDirectionalSideDownTurnLeft = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in_side_down_turn_left"));
        this.iconDirectionalSideDownTurnRight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_in_side_down_turn_right"));
    }

    @Override
    public boolean canItemEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorItem entity) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        EnumFacing orientation = state.getValue(FACING);
        return dir == orientation;
    }

    @Override
    public void onItemEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorItem entity) {
        if (entity == null || entity.getItemStack().isEmpty() || entity.getItemStack().getCount() <= 0) {
            return;
        }

        ItemStack toAdd = entity.getItemStack().copy();
        
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        boolean worked = false;
        if(te instanceof TileEntityCraneInserter)
            worked = ((TileEntityCraneInserter)te).tryFillTeDirect(toAdd);

        if(!worked) {
            EntityItem drop = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, toAdd.copy());
            world.spawnEntity(drop);
        }
    }

    @Override
    public boolean canPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity) {
        return false;
    }

    @Override
    public void onPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity) { }

    @Override
    public void breakBlock(World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        TileEntity tileentity = world.getTileEntity(pos);

        if(tileentity instanceof TileEntityCraneInserter) {
            InventoryHelper.dropInventoryItems(world, pos, tileentity);
        }
        super.breakBlock(world, pos, state);
    }

}
