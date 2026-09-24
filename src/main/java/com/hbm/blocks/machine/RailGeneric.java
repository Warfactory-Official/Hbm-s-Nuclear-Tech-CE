package com.hbm.blocks.machine;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class RailGeneric extends BlockRailBase implements ITooltipProvider {

    public static final PropertyEnum<BlockRailBase.EnumRailDirection> SHAPE =
            PropertyEnum.create("shape", BlockRailBase.EnumRailDirection.class);

    protected static final float baseSpeed = 0.4F;
    protected float maxSpeed = 0.4F;
    protected boolean slopable = true;
    protected boolean flexible = true;

    public RailGeneric(String s, String tool, int harvestLevel) {
        super(false);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setHarvestLevel(tool, harvestLevel);
        this.setDefaultState(this.blockState.getBaseState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH));

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public IProperty<EnumRailDirection> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {SHAPE});
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(SHAPE).getMetadata();
    }

    @Override
    public float getRailMaxSpeed(World world, EntityMinecart cart, BlockPos pos) {
        return maxSpeed;
    }

    public RailGeneric setMaxSpeed(float speed) {
        this.maxSpeed = speed;
        return this;
    }

    public RailGeneric setFlexible(boolean flexible) {
        this.flexible = flexible;
        return this;
    }

    @Override
    public boolean canMakeSlopes(IBlockAccess world, BlockPos pos) {
        return slopable;
    }

    public RailGeneric setSlopable(boolean slopable) {
        this.slopable = slopable;
        return this;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
        float speed = maxSpeed / baseSpeed;

        if(speed != 1F) {
            tooltip.add((speed > 1 ? TextFormatting.BLUE : TextFormatting.RED) + "Speed: " + ((int) (speed * 100)) + "%");
        }

        if(!flexible) {
            tooltip.add(TextFormatting.RED + "Cannot be used for turns!");
        }

        if(!slopable) {
            tooltip.add(TextFormatting.RED + "Cannot be used for slopes!");
        }
    }
}
