package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.BossSpawnHandler;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntitySoyuzLauncher;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class SoyuzLauncher extends BlockDummyable {

    public static final int height = 4;
    private static boolean keepInventory;

    public SoyuzLauncher(Material materialIn, String s) {
        super(materialIn, s);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        if (meta >= 12) return new TileEntitySoyuzLauncher();
        if (meta >= 6) return new TileEntityProxyCombo(false, true, true);

        return null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX,
                                    float hitY, float hitZ) {

        if (world.isRemote) {
            return true;
        } else if (!player.isSneaking()) {
            int[] corePos = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());

            if (corePos == null) return false;

            BossSpawnHandler.markFBI(player);

            TileEntity te = world.getTileEntity(new BlockPos(corePos[0], corePos[1], corePos[2]));
            if (!(te instanceof TileEntitySoyuzLauncher)) return false;

            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, corePos[0], corePos[1], corePos[2]);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBlockPlacedBy(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase player, @NotNull ItemStack itemStack) {
        if (!(player instanceof EntityPlayer pl)) return;

        EnumHand hand = player.getHeldItemMainhand() == itemStack ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;

        int o = -getOffset();

        ForgeDirection dir = ForgeDirection.EAST;

        if (!checkRequirement(world, pos.getX(), pos.getY(), pos.getZ(), dir, o)) {
            world.setBlockToAir(pos);

            if (!pl.capabilities.isCreativeMode) {
                ItemStack stack = pl.inventory.mainInventory.get(pl.inventory.currentItem);
                Item item = Item.getItemFromBlock(this);

                if (stack.isEmpty()) {
                    pl.inventory.mainInventory.set(pl.inventory.currentItem, new ItemStack(this));
                } else {
                    if (stack.getItem() != item || stack.getCount() == stack.getMaxStackSize()) {
                        pl.inventory.addItemStackToInventory(new ItemStack(this));
                    } else {
                        pl.getHeldItem(hand).grow(1);
                    }
                }
            }

            return;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        world.setBlockState(new BlockPos(x + dir.offsetX * o, y + dir.offsetY * o + height, z + dir.offsetZ * o), this.getDefaultState().withProperty(META, dir.ordinal() + offset), 3);
        fillSpace(world, x, y, z, dir, o);
        world.scheduleUpdate(pos, this, 1);
        world.scheduleUpdate(pos, this, 2);
    }

    @Override
    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
        x = x + dir.offsetX * o;
        y = y + dir.offsetY * o + height;
        z = z + dir.offsetZ * o;

        if (!MultiblockHandlerXR.checkSpace(world, x, y, z, new int[]{0, 1, 6, 6, 6, 6}, x, y, z, dir)) return false;
        if (!MultiblockHandlerXR.checkSpace(world, x, y, z, new int[]{-2, 4, -3, 6, -3, 6}, x, y, z, dir)) return false;
        if (!MultiblockHandlerXR.checkSpace(world, x, y, z, new int[]{-2, 4, 6, -3, -3, 6}, x, y, z, dir)) return false;
        if (!MultiblockHandlerXR.checkSpace(world, x, y, z, new int[]{-2, 4, 6, -3, 6, -3}, x, y, z, dir)) return false;
        if (!MultiblockHandlerXR.checkSpace(world, x, y, z, new int[]{-2, 4, -3, 6, 6, -3}, x, y, z, dir)) return false;
        if (!MultiblockHandlerXR.checkSpace(world, x, y, z, new int[]{0, 4, 1, 1, -6, 8}, x, y, z, dir)) return false;
        return MultiblockHandlerXR.checkSpace(world, x, y, z, new int[]{0, 4, 2, 2, 9, -5}, x, y, z, dir);
    }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        x = x + dir.offsetX * o;
        y = y + dir.offsetY * o + height;
        z = z + dir.offsetZ * o;

        MultiblockHandlerXR.fillSpace(world, x, y, z, new int[]{0, 1, 6, 6, 6, 6}, this, dir);
        MultiblockHandlerXR.fillSpace(world, x, y, z, new int[]{-2, 4, -3, 6, -3, 6}, this, dir);
        MultiblockHandlerXR.fillSpace(world, x, y, z, new int[]{-2, 4, 6, -3, -3, 6}, this, dir);
        MultiblockHandlerXR.fillSpace(world, x, y, z, new int[]{-2, 4, 6, -3, 6, -3}, this, dir);
        MultiblockHandlerXR.fillSpace(world, x, y, z, new int[]{-2, 4, -3, 6, 6, -3}, this, dir);
        MultiblockHandlerXR.fillSpace(world, x, y, z, new int[]{0, 4, 1, 1, -6, 8}, this, dir);
        MultiblockHandlerXR.fillSpace(world, x, y, z, new int[]{0, 4, 2, 2, 9, -5}, this, dir);
        keepInventory = true;
        for (int ix = -6; ix <= 6; ix++) {
            for (int iz = -6; iz <= 6; iz++) {
                if (ix == 6 || ix == -6 || iz == 6 || iz == -6) {
                    this.makeExtra(world, x + ix, y, z + iz);
                    this.makeExtra(world, x + ix, y + 1, z + iz);
                }
            }
        }
        keepInventory = false;
    }

    @Override
    public int[] getDimensions() {
        // because we'll implement our own gnarly behavior here
        return new int[]{0, 0, 0, 0, 0, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public void breakBlock(@NotNull World world, @NotNull BlockPos pos, IBlockState state) {
        if (!keepInventory) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntitySoyuzLauncher) {
                InventoryHelper.dropInventoryItems(world, pos, te);
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.5;
                double z = pos.getZ() + 0.5;
                for (int l = 0; l < 6; l++)
                    world.spawnEntity(new EntityItem(world, x, y, z, new ItemStack(ModBlocks.struct_launcher, 64)));
                for (int l = 0; l < 4; l++)
                    world.spawnEntity(new EntityItem(world, x, y, z, new ItemStack(ModBlocks.concrete_smooth, 64)));
                for (int l = 0; l < 6; l++)
                    world.spawnEntity(new EntityItem(world, x, y, z, new ItemStack(ModBlocks.struct_scaffold, 64)));

                world.spawnEntity(new EntityItem(world, x, y, z, new ItemStack(ModBlocks.struct_launcher, 30)));
                world.spawnEntity(new EntityItem(world, x, y, z, new ItemStack(ModBlocks.struct_scaffold, 63)));
                world.spawnEntity(new EntityItem(world, x, y, z, new ItemStack(ModBlocks.concrete_smooth, 38)));
                world.spawnEntity(new EntityItem(world, x, y, z, new ItemStack(ModBlocks.struct_soyuz_core, 1)));

                world.notifyNeighborsOfStateChange(pos, state.getBlock(), true);
            }
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}
