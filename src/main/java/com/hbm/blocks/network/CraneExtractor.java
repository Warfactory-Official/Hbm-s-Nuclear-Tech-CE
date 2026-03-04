package com.hbm.blocks.network;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.InventoryHelper;
import com.hbm.tileentity.network.TileEntityCraneBase;
import com.hbm.tileentity.network.TileEntityCraneExtractor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class CraneExtractor extends BlockCraneBase {
    public CraneExtractor(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntityCraneBase createNewTileEntity(@NotNull World world, int meta) {
        return new TileEntityCraneExtractor();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        super.registerSprite(map);
        this.iconDirectional = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out_top"));
        this.iconDirectionalUp = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out_side_up"));
        this.iconDirectionalDown = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out_side_down"));
        this.iconDirectionalTurnLeft = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out_top_left"));
        this.iconDirectionalTurnRight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out_top_right"));
        this.iconDirectionalSideLeftTurnUp = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out_side_left_turn_up"));
        this.iconDirectionalSideRightTurnUp = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out_side_right_turn_up"));
        this.iconDirectionalSideLeftTurnDown = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out_side_left_turn_down"));
        this.iconDirectionalSideRightTurnDown = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out_side_right_turn_down"));
        this.iconDirectionalSideUpTurnLeft = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out_side_up_turn_left"));
        this.iconDirectionalSideUpTurnRight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out_side_up_turn_right"));
        this.iconDirectionalSideDownTurnLeft = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out_side_down_turn_left"));
        this.iconDirectionalSideDownTurnRight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crane_out_side_down_turn_right"));
    }

    @Override
    public boolean canConnectRedstone(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public void breakBlock(World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        TileEntity tileentity = world.getTileEntity(pos);

        if(tileentity instanceof TileEntityCraneExtractor) {
            InventoryHelper.dropInventoryItems(world, pos, tileentity, 9, 17);
        }
        super.breakBlock(world, pos, state);
    }

}
