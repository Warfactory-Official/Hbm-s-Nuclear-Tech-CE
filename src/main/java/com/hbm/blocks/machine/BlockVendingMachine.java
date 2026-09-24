package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.interfaces.AutoRegister;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsVendingMachine;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

public class BlockVendingMachine extends BlockDummyable implements IBlockMulti, ICustomBlockItem {
    public BlockVendingMachine(String s) {
        super(Material.IRON, s);
    }

    @Override
    public @Nullable TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {
        if (meta >= 12) return new TileEntityVendingMachine();
        return null;
    }

    @Override
    public int getSubCount() {
        return 2;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {1, 0, 0, 0, 0, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (!stack.isEmpty() && stack.getItem() == ModItems.coin_token) {
            if (worldIn.isRemote) return true;

            BlockPos core = this.findCore(worldIn, pos);
            stack.shrink(1);
            if (core != null) {
                int meta = worldIn.getBlockState(core).getValue(META);
                boolean dropSnacks = worldIn.getBlockState(core.up()).getValue(META) >= extra;

                ItemStack drop = ItemPool.getStack(dropSnacks ? ItemPoolsVendingMachine.POOL_SNACKS : ItemPoolsVendingMachine.POOL_SODA, worldIn.rand);

                ForgeDirection dir = ForgeDirection.getOrientation(meta - offset);
                EntityItem item = new EntityItem(worldIn, pos.getX() + 0.5 + dir.offsetX * 0.75, core.getY() + 0.25, pos.getZ() + 0.5 + dir.offsetZ * 0.75, drop);
                worldIn.spawnEntity(item);

                worldIn.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.boltOpen, SoundCategory.BLOCKS, 1f, 0.75f);
            }

            return true;
        }

        return false;
    }

    @Override
    public void onBlockPlacedBy(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase player, @NotNull ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, player, itemStack);

        if (itemStack.getItemDamage() % 2 == 1 && world.getBlockState(pos.up()).getBlock() == this) {
            this.makeExtra(world, pos.getX(), pos.getY() + 1, pos.getZ());
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        BlockPos core = this.findCore(world, pos);
        int dmg = 0;
        if (core != null && world.getBlockState(core.up()).getValue(META) >= extra) dmg = 1;
        drops.add(new ItemStack(this, 1, dmg));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (int i = 0; i < getSubCount(); i++) items.add(new ItemStack(this, 1, i));
    }

    @AutoRegister
    public static class TileEntityVendingMachine extends TileEntity {
        private AxisAlignedBB bb = null;

        @Override
        public @NotNull AxisAlignedBB getRenderBoundingBox() {
            if (bb == null) bb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
            return bb;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public double getMaxRenderDistanceSquared() {
            return 65536.0D;
        }
    }
}
