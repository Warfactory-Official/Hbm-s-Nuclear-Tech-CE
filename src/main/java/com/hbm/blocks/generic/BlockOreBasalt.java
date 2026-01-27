package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.blocks.ModBlocks;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.*;

import static com.hbm.blocks.BlockEnums.EnumBasaltOreType;

public class BlockOreBasalt extends BlockEnumMeta<EnumBasaltOreType> {
    public BlockOreBasalt(String registryName) {
        super(Material.ROCK, SoundType.STONE, registryName, EnumBasaltOreType.VALUES, true, true);
    }

    @Override
    protected BlockBakeFrame[] generateBlockFrames(String registryName) {
        return Arrays.stream(blockEnum)
                .map(Enum::name)
                .map(name -> registryName + "." + name.toLowerCase(Locale.US))
                .map(texture -> new BlockBakeFrame(texture + "_top", texture))
                .toArray(BlockBakeFrame[]::new);
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entityIn) {
        int meta = world.getBlockState(pos).getValue(META);
        if(meta == EnumBasaltOreType.ASBESTOS.ordinal() && world.isAirBlock(pos.up())) {
            if(world.rand.nextInt(10) == 0) world.setBlockState(pos.up(), ModBlocks.gas_asbestos.getDefaultState());
            for(int i = 0; i < 5; i++)
                world.spawnParticle(EnumParticleTypes.TOWN_AURA,
                        pos.getX() + world.rand.nextFloat(),
                        pos.getY() + 1.1,
                        pos.getZ() + world.rand.nextFloat(),
                        0.0D, 0.0D, 0.0D);

        }
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random rand = ((World) world).rand;
        int meta = state.getValue(META);
        EnumBasaltOreType oreType = this.blockEnum[meta]; //Kind of ugly but whatever, I like it much more than if-else spam

        return Collections.singletonList(new ItemStack(oreType.getDrop(), oreType.getDropCount(rand.nextInt(fortune + 1))));
    }


}
