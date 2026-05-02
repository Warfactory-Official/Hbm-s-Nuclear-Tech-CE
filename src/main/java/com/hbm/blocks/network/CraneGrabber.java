package com.hbm.blocks.network;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.InventoryHelper;
import com.hbm.tileentity.network.TileEntityCraneBase;
import com.hbm.tileentity.network.TileEntityCraneGrabber;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class CraneGrabber extends BlockCraneBase {
    public CraneGrabber(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public void registerSprite(TextureMap map) {
        super.registerSprite(map);
        this.iconIn = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_pull"));
        this.iconSideIn = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_side_pull"));
        this.iconDirectional = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_grabber_top"));
        this.iconDirectionalUp = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_grabber_side_up"));
        this.iconDirectionalDown = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_grabber_side_down"));
        this.iconDirectionalTurnLeft = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_grabber_top_left"));
        this.iconDirectionalTurnRight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_grabber_top_right"));
        this.iconDirectionalSideLeftTurnUp = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_grabber_side_left_turn_up"));
        this.iconDirectionalSideRightTurnUp = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_grabber_side_right_turn_up"));
        this.iconDirectionalSideLeftTurnDown = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_grabber_side_left_turn_down"));
        this.iconDirectionalSideRightTurnDown = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_grabber_side_right_turn_down"));
        this.iconDirectionalSideUpTurnLeft = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_grabber_side_up_turn_left"));
        this.iconDirectionalSideUpTurnRight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_grabber_side_up_turn_right"));
        this.iconDirectionalSideDownTurnLeft = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_grabber_side_down_turn_left"));
        this.iconDirectionalSideDownTurnRight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_grabber_side_down_turn_right"));
    }

    @Override
    public TileEntityCraneBase createNewTileEntity(@NotNull World world, int meta) {
        return new TileEntityCraneGrabber();
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        TileEntityCraneGrabber crane = (TileEntityCraneGrabber) world.getTileEntity(pos);
        crane.isIndirectlyPowered = world.isBlockPowered(pos);
    }

    @Override
    public void breakBlock(World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        TileEntity tileentity = world.getTileEntity(pos);
        if(tileentity instanceof TileEntityCraneGrabber) {
            InventoryHelper.dropInventoryItems(world, pos, tileentity, 9, 10);
        }
        super.breakBlock(world, pos, state);
    }
}
