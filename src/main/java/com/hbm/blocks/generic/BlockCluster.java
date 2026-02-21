package com.hbm.blocks.generic;

import com.hbm.api.block.IDrillInteraction;
import com.hbm.api.block.IMiningDrill;
import com.hbm.blocks.IOreType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.Random;

import static com.hbm.blocks.OreEnumUtil.OreEnum;

public class BlockCluster extends BlockNTMOre implements IDrillInteraction {

    public BlockCluster(String s, IOreType oreEnum) {
        super(s, oreEnum, 1);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
        if (player instanceof FakePlayer) {
            return;
        }

        if (!world.isRemote && world.getGameRules().getBoolean("doTileDrops") && !world.restoringBlockSnapshots) {

            ItemStack drop = getDrop(world.rand, state);

            if (drop == null)
                return;

            float f = 0.7F;
            double mX = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
            double mY = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
            double mZ = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;

            EntityItem entityitem = new EntityItem(world, (double) pos.getX() + mX, (double) pos.getY() + mY, (double) pos.getZ() + mZ, drop);
            entityitem.setPickupDelay(10);
            world.spawnEntity(entityitem);
        }
    }

    private ItemStack getDrop(Random rand, IBlockState state) {

        if (this.oreEnum != null)
            return oreEnum.getDropFunction().apply(state, rand);


        return null;
    }

    @Override
    public boolean canBreak(World world, int x, int y, int z, IBlockState state, IMiningDrill drill) {
        return drill.getDrillRating() >= 30;
    }

    @Override
    public ItemStack extractResource(World world, int x, int y, int z, IBlockState state, IMiningDrill drill) {
        return drill.getDrillRating() >= 30 ? getDrop(world.rand, state) : null;
    }

    @Override
    public float getRelativeHardness(World world, int x, int y, int z, IBlockState state, IMiningDrill drill) {
        return state.getBlockHardness(world, new BlockPos(x, y, z));
    }
}