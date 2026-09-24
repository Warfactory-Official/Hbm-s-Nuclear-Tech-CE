package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.material.Mats;
import com.hbm.items.machine.ItemScraps;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

public class MachineArcFurnaceLarge extends BlockDummyable {

    public MachineArcFurnaceLarge(String s) {
        super(Material.IRON, s);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        if(meta >= 12) return new TileEntityMachineArcFurnaceLarge();
        if(meta >= 6) return new TileEntityProxyCombo().inventory().power();
        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {4, 0, 2, 2, 2, 2};
    }

    @Override
    public int getOffset() {
        return 2;
    }

    @Override
    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
        if(!super.checkRequirement(world, x, y, z, dir, o)) return false;
        return MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y, z + dir.offsetZ * o, new int[]{4, 0, 3, -2, 1, 1}, x, y, z, dir);
    }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);
        MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o, y, z + dir.offsetZ * o, new int[] {4, 0, 3, -2, 1, 1}, this, dir);

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        x += dir.offsetX * o;
        z += dir.offsetZ * o;

        this.makeExtra(world, x + dir.offsetX * 2 + rot.offsetX, y, z + dir.offsetZ * 2 + rot.offsetZ);
        this.makeExtra(world, x + dir.offsetX * 2 - rot.offsetX, y, z + dir.offsetZ * 2 - rot.offsetZ);
        this.makeExtra(world, x + rot.offsetX * 2 + dir.offsetX, y, z + rot.offsetZ * 2 + dir.offsetZ);
        this.makeExtra(world, x + rot.offsetX * 2 - dir.offsetX, y, z + rot.offsetZ * 2 - dir.offsetZ);
        this.makeExtra(world, x - rot.offsetX * 2 + dir.offsetX, y, z - rot.offsetZ * 2 + dir.offsetZ);
        this.makeExtra(world, x - rot.offsetX * 2 - dir.offsetX, y, z - rot.offsetZ * 2 - dir.offsetZ);
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {

        if(world.isRemote) {
            return true;
        } else if(!player.isSneaking()) {
            int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());

            if(posC == null)
                return false;
            if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof ItemTool && (player.getHeldItem(hand).getItem()).getToolClasses(player.getHeldItem(hand)).contains("shovel")) {
                TileEntityMachineArcFurnaceLarge crucible = (TileEntityMachineArcFurnaceLarge) world.getTileEntity(new BlockPos(posC[0], posC[1], posC[2]));

                for(Mats.MaterialStack stack : crucible.liquids) {
                    ItemStack scrap = ItemScraps.create(new Mats.MaterialStack(stack.material, stack.amount));
                    if(!player.inventory.addItemStackToInventory(scrap)) {
                        EntityItem item = new EntityItem(world, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ, scrap);
                        world.spawnEntity(item);
                    }
                }

                player.inventoryContainer.detectAndSendChanges();
                crucible.liquids.clear();
                crucible.markDirty();

            } else {
                FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, posC[0], posC[1], posC[2]);
            }
            return true;
        } else {
            return true;
        }
    }
}
