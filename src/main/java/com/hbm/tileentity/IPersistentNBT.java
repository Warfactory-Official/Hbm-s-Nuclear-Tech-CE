package com.hbm.tileentity;

import com.hbm.api.tile.IWorldRenameable;
import com.hbm.util.CompatExternal;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;

/**
 * Persistent block data helper
 *
 * <p>Lifecycle expectations: </p>
 * <ul>
 *   <li>Blocks must override {@link Block#breakBlock}, {@link Block#dropBlockAsItemWithChance} (empty), {@link Block#onBlockHarvested}, and {@link Block#onBlockPlacedBy}. Call the matching static helper here before invoking super where noted.</li>
 *   <li>Subclasses of {@link com.hbm.blocks.BlockDummyable BlockDummyable} already handle placement; you can skip overriding {@code onBlockPlacedBy} there.</li>
 *   <li>{@link #writeNBT(NBTTagCompound)} receives a new tag; put your contents under {@link #NBT_PERSISTENT_KEY} if you want to keep the item tag clean.</li>
 *   <li>{@link #readNBT(NBTTagCompound)} gets the full item tag; check both the root and {@link #NBT_PERSISTENT_KEY} so old items still load.</li>
 *   <li>Call {@link #setDestroyedByCreativePlayer()} in {@link #onBlockHarvested(World, BlockPos, EntityPlayer)} to skip drops for creative breaks.</li>
 * </ul>
 *
 * <p><strong>Warning:</strong> This differs from upstream because vanilla destroys blocks before tile removal.</p>
 *
 * <p>Reference design: {@link net.minecraft.block.BlockShulkerBox BlockShulkerBox} and
 * {@link net.minecraft.tileentity.TileEntityShulkerBox TileEntityShulkerBox}</p>
 */
public interface IPersistentNBT {

    String NBT_PERSISTENT_KEY = "persistent";

    /**
     * Call from {@link Block#breakBlock} before super. Server side only.
     *
     * @return true if item is dropped
     */
    static boolean breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        // intentionally avoided CompatExternal.getCoreFromPos to prevent duplicates, so that only the block that has the core TE would drop items
        TileEntity tile = worldIn.getTileEntity(pos);
        final boolean flag;
        if (tile instanceof IPersistentNBT persistentTE && persistentTE.shouldDrop()) {
            ItemStack itemstack = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().damageDropped(state));
            NBTTagCompound data = new NBTTagCompound();
            persistentTE.writeNBT(data);
            if (!data.isEmpty()) itemstack.setTagCompound(data);
            if (tile instanceof IWorldNameable nameable && nameable.hasCustomName()) {
                itemstack.setStackDisplayName(nameable.getName());
                if (tile instanceof IWorldRenameable rn) rn.setCustomName("");
            }
            Block.spawnAsEntity(worldIn, pos, itemstack);
            flag = true;
        } else flag = false;
        if (state.hasComparatorInputOverride()) {
            worldIn.updateComparatorOutputLevel(pos, state.getBlock());
        }
        return flag;
    }

    /**
     * Call from {@link Block#onBlockPlacedBy}. Server side reads persisted data; client still handles custom name.
     */
    static void onBlockPlacedBy(World worldIn, BlockPos pos, ItemStack stack) {
        if (!worldIn.isRemote && stack.hasTagCompound() && worldIn.getTileEntity(pos) instanceof IPersistentNBT persistentTE) {
            persistentTE.readNBT(stack.getTagCompound());
        }
        if (stack.hasDisplayName() && worldIn.getTileEntity(pos) instanceof IWorldRenameable renameable) {
            renameable.setCustomName(stack.getDisplayName());
        }
    }

    /**
     * Marks creative destruction so {@link #shouldDrop()} can skip spawning items.
     */
    static void onBlockHarvested(World worldIn, BlockPos pos, EntityPlayer player){
        if (player.capabilities.isCreativeMode && CompatExternal.getCoreFromPos(worldIn, pos) instanceof IPersistentNBT persistentTE) {
            persistentTE.setDestroyedByCreativePlayer();
        }
    }

    default boolean shouldDrop() {
        return !isDestroyedByCreativePlayer();
    }

    void setDestroyedByCreativePlayer();

    boolean isDestroyedByCreativePlayer();

    void writeNBT(NBTTagCompound nbt);

    void readNBT(NBTTagCompound nbt);
}
