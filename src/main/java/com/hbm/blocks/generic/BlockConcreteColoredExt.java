package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.util.I18nUtil;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;

public class BlockConcreteColoredExt extends BlockEnumMeta<BlockConcreteColoredExt.EnumConcreteType> {

    public BlockConcreteColoredExt(Material material, SoundType type, String name, EnumConcreteType[] enumValues, boolean multiName, boolean multiTex) {
        super(material, type, name, enumValues, multiName, multiTex);
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type) {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
        super.addInformation(stack, world, list, flag);
        list.add(TextFormatting.RED + I18nUtil.resolveKey("tile.nospawn"));
    }

    @Override
    protected BlockBakeFrame[] generateBlockFrames(String registryName) {
        BlockBakeFrame[] frames = new BlockBakeFrame[EnumConcreteType.VALUES.length];
        for (EnumConcreteType type : EnumConcreteType.VALUES) {
            String name = registryName + "." + type.name().toLowerCase(Locale.US);
            if (type == EnumConcreteType.MACHINE_STRIPE) {
                String machine = registryName + "." + EnumConcreteType.MACHINE.name().toLowerCase(Locale.US);
                frames[type.ordinal()] = BlockBakeFrame.cube(
                        machine, // up
                        machine, // down
                        name,  // north
                        name,  // south
                        name,  // west
                        name   // east
                );
            } else {
                frames[type.ordinal()] = BlockBakeFrame.cubeAll(name);
            }
        }
        return frames;
    }

    public enum EnumConcreteType {
        MACHINE,
        MACHINE_STRIPE,
        INDIGO,
        PURPLE,
        PINK,
        HAZARD,
        SAND,
        BRONZE;

        public static final EnumConcreteType[] VALUES = values();
    }
}
