package com.hbm.items.block;

import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.config.MachineConfig;
import com.hbm.config.ServerConfig;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.TileEntityCrate;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
// Th3_Sl1ze: achtung! Logic slightly differs from 1.7 upstream, I wouldn't change it
public class ItemBlockStorageCrate extends ItemBlock implements IGUIProvider {

    public ItemBlockStorageCrate(Block block, ResourceLocation s) {
        super(block);
        this.setRegistryName(s);
    }

    @Override
    public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World world, @NotNull EntityPlayer player, @NotNull EnumHand hand) {
        if (!ServerConfig.CRATE_OPEN_HELD.get()) return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(hand));

        if (!world.isRemote && !player.isSneaking()) {
            TileEntityCrate dummy = getDummyTE(player, world);

            if (dummy != null && dummy.canAccess(player)) {
                FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, (int) player.posX, -1, (int) player.posZ);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    private TileEntityCrate getDummyTE(EntityPlayer player, World world) {
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlockStorageCrate)) {
            return null;
        }

        BlockStorageCrate blockCrate = (BlockStorageCrate) this.block;
        TileEntity te = blockCrate.createNewTileEntity(world, 0);

        if (te instanceof TileEntityCrate crate) {
            crate.setWorld(world);
            crate.setPos(player.getPosition());
            crate.boundItem = stack;

            if (stack.hasTagCompound()) {
                crate.readNBT(stack.getTagCompound());
                crate.fillPercentage = (float) crate.getSize() / MachineConfig.crateByteSize * 100F;
            }
            return crate;
        }
        return null;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntityCrate dummy = getDummyTE(player, world);
        return dummy != null ? dummy.provideContainer(ID, player, world, x, y, z) : null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntityCrate dummy = getDummyTE(player, world);
        return dummy != null ? dummy.provideGUI(ID, player, world, x, y, z) : null;
    }
}
