package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockTaint;
import com.hbm.explosion.ExplosionThermo;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.Random;

// FIXME: extend BlockDetonatable
// don't forget to delete == ModBlocks.red_barrel manual detonation checks when this got fixed, that's a temporary solution
public class RedBarrel extends BaseBarrel {

    //MrNorwood: This is pretty much required to prevent infinite recursion issues
    //TODO: WHY THE FUCK BARRELS DONT HAVE AN ABSTRACT CLASS BELOW IT?
    //FIXME: This kind of breaks large consecutive explosions, I dont want to deal with it atm
    private int explosionCount = 0;
    private static final int MAX_EXPLOSION_DEPTH = 100; // Limit to 100 explosions

    public RedBarrel(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add("Static fluid barrel");
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
        if (!worldIn.isRemote && worldIn instanceof WorldServer) {
            if (explosionCount >= MAX_EXPLOSION_DEPTH) {
                return;
            }

            explosionCount++;

            ((WorldServer) worldIn).addScheduledTask(() -> {
                explode(worldIn, pos.getX(), pos.getY(), pos.getZ());
                explosionCount--;
            });
        }
    }


    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if ((this == ModBlocks.red_barrel || this == ModBlocks.pink_barrel) && worldIn.getBlockState(pos.east()).getBlock() == Blocks.FIRE || worldIn.getBlockState(pos.west()).getBlock() == Blocks.FIRE || worldIn.getBlockState(pos.up()).getBlock() == Blocks.FIRE || worldIn.getBlockState(pos.down()).getBlock() == Blocks.FIRE || worldIn.getBlockState(pos.south()).getBlock() == Blocks.FIRE || worldIn.getBlockState(pos.north()).getBlock() == Blocks.FIRE) {
            if (!worldIn.isRemote && worldIn instanceof WorldServer) {
                ((WorldServer) worldIn).addScheduledTask(() -> {
                    explode(worldIn, pos.getX(), pos.getY(), pos.getZ());
                    worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
                });
            }
        }
    }

    public void explode(World world, int x, int y, int z) {

        if (this == ModBlocks.red_barrel || this == ModBlocks.pink_barrel)
            world.newExplosion(null, x + 0.5F, y + 0.5F, z + 0.5F, 2.5F, true, true);

        if (this == ModBlocks.lox_barrel) {

            world.newExplosion(null, x + 0.5F, y + 0.5F, z + 0.5F, 1F, false, false);

            ExplosionThermo.freeze(world, null, x, y, z, 7);
        }

        if (this == ModBlocks.taint_barrel) {

            world.newExplosion(null, x + 0.5F, y + 0.5F, z + 0.5F, 1F, false, false);

            Random rand = world.rand;
            MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int i = 0; i < 100; i++) {
                int a = rand.nextInt(9) - 4 + x;
                int b = rand.nextInt(9) - 4 + y;
                int c = rand.nextInt(9) - 4 + z;
                pos.setPos(a, b, c);
                IBlockState state = world.getBlockState(pos);
                if (state.isNormalCube() && !state.getBlock().isAir(state, world, pos)) {
                    world.setBlockState(pos, ModBlocks.taint.getDefaultState().withProperty(BlockTaint.TAINTAGE, rand.nextInt(3) + 4), 2);
                }
            }
        }
    }
}
