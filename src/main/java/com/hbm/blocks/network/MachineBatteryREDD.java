package com.hbm.blocks.network;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.storage.TileEntityBatteryREDD;
import com.hbm.util.BobMathUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class MachineBatteryREDD extends BlockDummyable implements IPersistentInfoProvider {

    public MachineBatteryREDD(String s) {
        super(Material.IRON, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if (meta >= 12) return new TileEntityBatteryREDD();
        if (meta >= 6) return new TileEntityProxyCombo().power().conductor();
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return super.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
    }

    @Override
    public int[] getDimensions() {
        return new int[]{9, 0, 2, 2, 4, 4};
    }

    @Override
    public int getOffset() {
        return 2;
    }

    @Override
    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x += dir.offsetX * o;
        z += dir.offsetZ * o;

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        this.makeExtra(world, x + dir.offsetX * 2 + rot.offsetX * 2, y, z + dir.offsetZ * 2 + rot.offsetZ * 2);
        this.makeExtra(world, x + dir.offsetX * 2 - rot.offsetX * 2, y, z + dir.offsetZ * 2 - rot.offsetZ * 2);
        this.makeExtra(world, x - dir.offsetX * 2 + rot.offsetX * 2, y, z - dir.offsetZ * 2 + rot.offsetZ * 2);
        this.makeExtra(world, x - dir.offsetX * 2 - rot.offsetX * 2, y, z - dir.offsetZ * 2 - rot.offsetZ * 2);
        this.makeExtra(world, x + rot.offsetX * 4, y, z + rot.offsetZ * 4);
        this.makeExtra(world, x - rot.offsetX * 4, y, z - rot.offsetZ * 4);
    }

    // IPersistentNBT

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        IPersistentNBT.breakBlock(world, pos, state);
        super.breakBlock(world, pos, state);
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        IPersistentNBT.onBlockHarvested(worldIn, pos, player);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
        player.addStat(Objects.requireNonNull(StatList.getBlockStats(this)), 1);
        player.addExhaustion(0.025F);
    }

    @Override
    public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List list, boolean ext) {
        if(persistentTag != null && persistentTag.hasKey("power"))
            list.add(TextFormatting.YELLOW + "" + BobMathUtil.format(new BigInteger(persistentTag.getByteArray("power"))) + " HE");
    }
}
