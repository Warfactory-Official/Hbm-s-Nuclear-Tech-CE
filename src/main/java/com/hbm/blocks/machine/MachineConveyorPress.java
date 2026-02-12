package com.hbm.blocks.machine;

import com.hbm.api.block.IToolable;
import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.machine.ItemStamp;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.machine.TileEntityConveyorPress;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MachineConveyorPress extends BlockDummyable implements IConveyorBelt, ILookOverlay, IToolable, ITooltipProvider {

    public MachineConveyorPress(Material mat, String s) {
        super(mat, s);
    }

    private static ForgeDirection getTravelDirection(World world, int x, int y, int z, Vec3d itemPos) {
        int meta =
                world.getBlockState(new BlockPos(x, y - 1, z)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(x, y - 1, z))) - offset;
        return ForgeDirection.getOrientation(meta).getRotation(ForgeDirection.UP);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if (meta >= 12) return new TileEntityConveyorPress();
        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[]{2, 0, 0, 0, 0, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX
            , float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        } else {

            BlockPos corePos = this.findCore(world, pos);

            if (corePos == null) return false;

            TileEntity te = world.getTileEntity(corePos);

            if (!(te instanceof TileEntityConveyorPress press)) return false;

            if (!player.getHeldItem(EnumHand.MAIN_HAND).isEmpty() && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemStamp &&
                press.inventory.getStackInSlot(0).isEmpty()) {
                ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND).copy();
                stack.setCount(1);
                press.inventory.setStackInSlot(0, stack);
                player.getHeldItem(EnumHand.MAIN_HAND).shrink(1);
                world.playSound(null, pos, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.0F, 1.0F);
                press.markChanged();
                world.markBlockRangeForRenderUpdate(pos, pos);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand,
                           ToolType tool) {

        if (tool != ToolType.SCREWDRIVER) return false;

        BlockPos corePos = this.findCore(world, new BlockPos(x, y, z));

        if (corePos == null) return false;

        TileEntity te = world.getTileEntity(corePos);

        if (!(te instanceof TileEntityConveyorPress press)) return false;

        if (press.inventory.getStackInSlot(0).isEmpty()) return false;

        if (!player.inventory.addItemStackToInventory(press.inventory.getStackInSlot(0).copy())) {
            player.dropItem(press.inventory.getStackInSlot(0), false);
        } else {
            player.inventoryContainer.detectAndSendChanges();
        }
        press.inventory.setStackInSlot(0, ItemStack.EMPTY);
        press.markChanged();
        return true;
    }

    @Override
    public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {
        ForgeDirection dir = MachineConveyorPress.getTravelDirection(world, x, y, z, itemPos);
        Vec3d snap = this.getClosestSnappingPosition(world, new BlockPos(x, y, z), itemPos);
        Vec3d dest = new Vec3d(snap.x - dir.offsetX * speed, snap.y - dir.offsetY * speed, snap.z - dir.offsetZ * speed);
        Vec3d motion = new Vec3d((dest.x - itemPos.x), (dest.y - itemPos.y), (dest.z - itemPos.z));
        double len = motion.length();
        return new Vec3d(itemPos.x + motion.x / len * speed, itemPos.y + motion.y / len * speed, itemPos.z + motion.z / len * speed);
    }

    @Override
    public Vec3d getClosestSnappingPosition(World world, BlockPos pos, Vec3d itemPos) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        ForgeDirection dir = MachineConveyorPress.getTravelDirection(world, x, y, z, itemPos);
        double posX = x + 0.5;
        double posZ = z + 0.5;
        if (dir.offsetX != 0) posX = MathHelper.clamp(itemPos.x, x, x + 1);
        if (dir.offsetZ != 0) posZ = MathHelper.clamp(itemPos.z, z, z + 1);
        return new Vec3d(posX, y + 0.25, posZ);
    }

    @Override
    public boolean canItemStay(World world, int x, int y, int z, Vec3d itemPos) {
        IBlockState state = world.getBlockState(new BlockPos(x, y - 1, z));
        return state.getBlock() == this && state.getBlock().getMetaFromState(state) >= 12;
    }

    @Override
    public void printHook(Pre event, World world, BlockPos pos) {
        BlockPos corePos = this.findCore(world, pos);

        if(corePos == null)
            return;

        TileEntity te = world.getTileEntity(corePos);

        if (!(te instanceof TileEntityConveyorPress press)) return;

        List<String> text = new ArrayList<>();

        text.add(BobMathUtil.getShortNumber(press.power) + "HE / " + BobMathUtil.getShortNumber(TileEntityConveyorPress.maxPower) + "HE");
        text.add("Installed stamp: " +
                 ((press.syncStack == null || press.syncStack.isEmpty()) ? (TextFormatting.RED + "NONE") : press.syncStack.getDisplayName()));

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        this.addStandardInfo(tooltip);
    }
}
