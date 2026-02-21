package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntitySawmill;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MachineSawmill extends BlockDummyable implements ILookOverlay, ITooltipProvider {
    private static final AxisAlignedBB FIXED_BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.999D, 1.0D);

    public MachineSawmill(String s) {
        super(Material.IRON, s);
        this.bounding.add(new AxisAlignedBB(-1.5D, 0D, -1.5D, 1.5D, 1D, 1.5D));
        this.bounding.add(new AxisAlignedBB(-1.25D, 1D, -0.5D, -0.625D, 1.875D, 0.5D));
        this.bounding.add(new AxisAlignedBB(-0.625D, 1D, -1D, 1.375D, 2D, 1D));
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        return FIXED_BOX; // that should prevent items bouncing
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {

        if(meta >= 12)
            return new TileEntitySawmill();

        if(meta >= extra)
            return new TileEntityProxyCombo(true, false, false);

        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {1, 0, 1, 1, 1, 1};
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x = x + dir.offsetX * o;
        z = z + dir.offsetZ * o;

        this.makeExtra(world, x + 1, y, z);
        this.makeExtra(world, x - 1, y, z);
        this.makeExtra(world, x, y, z + 1);
        this.makeExtra(world, x, y, z - 1);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if(world.isRemote) {
            return true;

        } else if(!player.isSneaking()) {
            int[] corePos = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());

            if(corePos == null)
                return false;

            TileEntitySawmill sawmill = (TileEntitySawmill)world.getTileEntity(new BlockPos(corePos[0], corePos[1], corePos[2]));

            if(sawmill == null) {
                return false;
            }

            if(!sawmill.hasBlade && !player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() == ModItems.sawblade) {
                player.getHeldItem(hand).shrink(1);
                sawmill.hasBlade = true;
                sawmill.markDirty();
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.5F, 0.75F);
                return true;
            }

            if(!sawmill.inventory.getStackInSlot(1).isEmpty() || !sawmill.inventory.getStackInSlot(2).isEmpty()) {
                for(int i = 1; i < 3; i++) {
                    if(!sawmill.inventory.getStackInSlot(i).isEmpty()) {
                        if(!player.inventory.addItemStackToInventory(sawmill.inventory.getStackInSlot(i).copy())) {
                            player.dropItem(sawmill.inventory.getStackInSlot(i).copy(), false);
                        }
                        sawmill.inventory.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
                player.inventoryContainer.detectAndSendChanges();
                sawmill.markDirty();
                return true;

            } else {
                if(sawmill.inventory.getStackInSlot(0).isEmpty() && !player.getHeldItem(hand).isEmpty() && !(sawmill.getOutput(player.getHeldItem(hand)).isEmpty())) {
                    sawmill.inventory.setStackInSlot(0, player.getHeldItem(hand).copy());
                    sawmill.inventory.getStackInSlot(0).setCount(1);
                    player.getHeldItem(hand).shrink(1);
                    sawmill.markDirty();
                    player.inventoryContainer.detectAndSendChanges();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onBlockPlacedBy(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        if(stack.getItemDamage() == 1) {

            int i = MathHelper.floor(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
            int o = -getOffset();

            ForgeDirection dir = ForgeDirection.NORTH;
            if(i == 0) dir = ForgeDirection.getOrientation(2);
            if(i == 1) dir = ForgeDirection.getOrientation(5);
            if(i == 2) dir = ForgeDirection.getOrientation(3);
            if(i == 3) dir = ForgeDirection.getOrientation(4);

            dir = getDirModified(dir);

            TileEntity te = world.getTileEntity(pos.add(dir.offsetX * o, dir.offsetY * o, dir.offsetZ * o));

            if(te instanceof TileEntitySawmill) {
                ((TileEntitySawmill) te).hasBlade = false;
            }
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (!(world instanceof World)) {
            return;
        }
        int count = quantityDropped(state, fortune, ((World) world).rand);
        int dmg = 0;

        int[] corePos = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
        if (corePos != null) {
            BlockPos coreBlockPos = new BlockPos(corePos[0], corePos[1], corePos[2]);
            TileEntity tileEntity = world.getTileEntity(coreBlockPos);
            if (tileEntity instanceof TileEntitySawmill) {
                TileEntitySawmill stirling = (TileEntitySawmill) tileEntity;
                if (!stirling.hasBlade) {
                    dmg = 1;
                }
            }
        }

        for (int i = 0; i < count; i++) {
            Item item = getItemDropped(state, ((World) world).rand, fortune);
            if (item != null) {
                drops.add(new ItemStack(item, 1, dmg));
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        this.addStandardInfo(tooltip);
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        BlockPos corePos = this.findCore(world, pos);

        if(corePos == null)
            return;

        TileEntity te = world.getTileEntity(corePos);

        if(!(te instanceof TileEntitySawmill))
            return;

        TileEntitySawmill stirling = (TileEntitySawmill) te;

        List<String> text = new ArrayList();
        text.add(stirling.heat + "TU/t");

        double percent = (double) stirling.heat / (double) 300;
        int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int)(0xFF * percent) << 8);

        if(percent > 1D)
            color = 0xff0000;

        text.add("&[" + color + "&]" + ((stirling.heat * 1000 / 300) / 10D) + "%");

        int limiter = stirling.progress * 26 / stirling.processingTime;
        String bar = TextFormatting.GREEN + "[ ";
        for(int i = 0; i < 25; i++) {
            if(i == limiter) {
                bar += TextFormatting.RESET;
            }

            bar += "▏";
        }

        bar += TextFormatting.GREEN + " ]";

        text.add(bar);

        for(int i = 0; i < 3; i++) {
            if(!stirling.inventory.getStackInSlot(i).isEmpty()) {
                text.add((i == 0 ? (TextFormatting.GREEN + "-> ") : (TextFormatting.RED + "<- ")) + TextFormatting.RESET + stirling.inventory.getStackInSlot(i).getDisplayName() + (stirling.inventory.getStackInSlot(i).getCount() > 1 ? " x" + stirling.inventory.getStackInSlot(i).getCount() : ""));
            }
        }

        if(stirling.heat > 300) {
            text.add("&[" + (System.currentTimeMillis() % 1000 < 500 ? 0xff0000 : 0xffff00) + "&]! ! ! OVERSPEED ! ! !");
        }

        if(!stirling.hasBlade) {
            text.add("&[" + 0xff0000 + "&]Blade missing!");
        }

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }
}
