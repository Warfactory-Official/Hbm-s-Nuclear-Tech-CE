package com.hbm.blocks.machine;

import com.hbm.api.block.ICrucibleAcceptor;
import com.hbm.api.block.IToolable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMold;
import com.hbm.items.machine.ItemMold.Mold;
import com.hbm.items.machine.ItemScraps;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.InventoryHelper;
import com.hbm.tileentity.machine.TileEntityFoundryCastingBase;
import com.hbm.util.I18nUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public abstract class FoundryCastingBase extends BlockContainer implements ICrucibleAcceptor, IToolable, ILookOverlay {

    protected FoundryCastingBase(String name) {
        super(Material.ROCK);
        this.setTranslationKey(name);
        this.setRegistryName(name);
        this.setSoundType(SoundType.METAL);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    public double getPH() {
        return 1;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public boolean canAcceptPartialPour(World world, BlockPos p, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
        return ((ICrucibleAcceptor) Objects.requireNonNull(world.getTileEntity(p))).canAcceptPartialPour(world, p, dX, dY, dZ, side, stack);
    }

    @Override
    public MaterialStack pour(World world, BlockPos p, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
        return ((ICrucibleAcceptor) Objects.requireNonNull(world.getTileEntity(p))).pour(world, p, dX, dY, dZ, side, stack);
    }

    @Override
    public boolean canAcceptPartialFlow(World world, BlockPos p, ForgeDirection side, MaterialStack stack) {
        return ((ICrucibleAcceptor) Objects.requireNonNull(world.getTileEntity(p))).canAcceptPartialFlow(world, p, side, stack);
    }

    @Override
    public MaterialStack flow(World world, BlockPos p, ForgeDirection side, MaterialStack stack) {
        return ((ICrucibleAcceptor) Objects.requireNonNull(world.getTileEntity(p))).flow(world, p, side, stack);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        TileEntityFoundryCastingBase cast = (TileEntityFoundryCastingBase) world.getTileEntity(pos);
        if (cast == null) return false;

        if (!cast.inventory.getStackInSlot(1).isEmpty()) {
            if (!player.inventory.addItemStackToInventory(cast.inventory.getStackInSlot(1).copy())) {
                world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, cast.inventory.getStackInSlot(1).copy()));
            } else {
                player.inventoryContainer.detectAndSendChanges();
            }
            cast.inventory.setStackInSlot(1, ItemStack.EMPTY);
            cast.markDirty();
            world.notifyBlockUpdate(pos, state, state, 3);
            return true;
        }

        ItemStack heldItem = player.getHeldItem(hand);
        //insert mold
        if (!heldItem.isEmpty() && heldItem.getItem() == ModItems.mold) {
            Mold mold = ((ItemMold) heldItem.getItem()).getMold(heldItem);

            if (mold.size == cast.getMoldSize()) {
                if (!cast.inventory.getStackInSlot(0).isEmpty()) {
                    ItemStack prevMold = cast.inventory.getStackInSlot(0);
                    if (!player.inventory.addItemStackToInventory(prevMold)) {
                        world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, prevMold));
                    }
                }

                ItemStack newMold = heldItem.copy();
                newMold.setCount(1);
                cast.inventory.setStackInSlot(0, newMold); // Set mold in Slot 0
                if(!player.isCreative())
                heldItem.shrink(1);

                world.playSound(null, pos, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.5F, 1.0F);
                cast.markDirty();
                world.notifyBlockUpdate(pos, state, state, 3);
                return true;
            }
        }

        //shovel scrap
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof ItemTool && heldItem.getItem().getToolClasses(heldItem).contains("shovel")) {
            if (cast.amount > 0) {
                ItemStack scrap = ItemScraps.create(new MaterialStack(cast.type, cast.amount));
                if (!player.inventory.addItemStackToInventory(scrap)) {
                    world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, scrap));
                } else {
                    player.inventoryContainer.detectAndSendChanges();
                }
                cast.amount = 0;
                cast.type = null;
                cast.markDirty();
                world.notifyBlockUpdate(pos, state, state, 3);
            }
            return true;
        }

        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntityFoundryCastingBase cast = (TileEntityFoundryCastingBase) world.getTileEntity(pos);
        if (cast == null) {
            super.breakBlock(world, pos, state);
            return;
        }
        if (cast.amount > 0) {
            ItemStack scrap = ItemScraps.create(new MaterialStack(cast.type, cast.amount));
            world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, scrap));
        }
        InventoryHelper.dropInventoryItems(world, pos, cast);

        super.breakBlock(world, pos, state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        super.randomDisplayTick(state, world, pos, rand);

        TileEntityFoundryCastingBase cast = (TileEntityFoundryCastingBase) world.getTileEntity(pos);
        if (Objects.requireNonNull(cast).amount > 0 && cast.amount >= cast.getCapacity()) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.25 + rand.nextDouble() * 0.5, pos.getY() + getPH(), pos.getZ() + 0.25 + rand.nextDouble() * 0.5, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) return false;

        TileEntityFoundryCastingBase cast = (TileEntityFoundryCastingBase) world.getTileEntity(new BlockPos(x, y, z));
        if (cast == null) return false;

        if (cast.inventory.getStackInSlot(0).isEmpty()) return false;
        if (cast.amount > 0) return false;

        if (!player.inventory.addItemStackToInventory(cast.inventory.getStackInSlot(0).copy())) {
            world.spawnEntity(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, cast.inventory.getStackInSlot(0).copy()));
        } else {
            player.inventoryContainer.detectAndSendChanges();
        }

        cast.inventory.setStackInSlot(0, ItemStack.EMPTY);
        cast.markDirty();
        IBlockState currentState = world.getBlockState(cast.getPos());
        world.notifyBlockUpdate(cast.getPos(), currentState, currentState, 3);

        return true;
    }

    @Override
    public void printHook(Pre event, World world, BlockPos pos) {
        TileEntityFoundryCastingBase cast = (TileEntityFoundryCastingBase) world.getTileEntity(pos);
        List<String> text = new ArrayList<>();
        if (cast == null) return;
//        ItemStack outputStack = cast.inventory.getStackInSlot(1);
        ItemStack moldStack = cast.inventory.getStackInSlot(0);

//        if (!outputStack.isEmpty()) {
//            text.add("§a" + I18nUtil.resolveKey("foundry.status.finished"));
//            text.add("  " + outputStack.getDisplayName());
//        } else
        if (moldStack.isEmpty()) {
            text.add("§c" + I18nUtil.resolveKey("foundry.noCast"));
        } else {
            Mold mold = ((ItemMold) moldStack.getItem()).getMold(moldStack);
            text.add("§e" + mold.getTitle());
            if (cast.type != null && cast.amount > 0) {
                text.add("&[" + cast.type.moltenColor + "&]" + I18nUtil.resolveKey(cast.type.getTranslationKey()) + ": " + cast.amount + " / " + cast.getCapacity());
            }
//            else if (cast.getCapacity() > 0) {
//                text.add("§7" + I18nUtil.resolveKey("foundry.empty") + ": 0 / " + cast.getCapacity());
//            }
//            if (cast.getCapacity() > 0 && cast.amount >= cast.getCapacity()) {
//                if (cast.cooloff < 100) {
//                    int progress = (int) (((100.0 - cast.cooloff) / 100.0) * 100);
//                    text.add("§b" + I18nUtil.resolveKey("foundry.status.cooling", progress));
//                } else {
//                    text.add("§a" + I18nUtil.resolveKey("foundry.status.ready"));
//                }
//            }
        }

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(this.getTranslationKey() + ".name"), 0xFF4000, 0x401000, text);
    }

    protected static void addBox(BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, double x1, double y1, double z1, double x2, double y2, double z2) {
        AxisAlignedBB bb = new AxisAlignedBB(pos.getX() + x1, pos.getY() + y1, pos.getZ() + z1, pos.getX() + x2, pos.getY() + y2, pos.getZ() + z2);
        if (entityBox.intersects(bb)) {
            collidingBoxes.add(bb);
        }
    }
}