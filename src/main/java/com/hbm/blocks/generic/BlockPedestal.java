package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.capability.HbmCapability;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IGunClickable;
import com.hbm.inventory.recipes.PedestalRecipes;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.ExplosionSmallCreator;
import com.hbm.util.Compat;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@SuppressWarnings("deprecation")
public class BlockPedestal extends BlockContainer implements IGunClickable {
    public BlockPedestal(String s) {
        super(Material.ROCK);
        this.setRegistryName(s);
        this.setTranslationKey(s);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public @Nullable TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityPedestal();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) return false;

        TileEntityPedestal pedestal = (TileEntityPedestal) world.getTileEntity(pos);

        if(pedestal.item.isEmpty() && !player.getHeldItem(hand).isEmpty()) {
            if(world.isRemote) return true;
            pedestal.item = player.getHeldItem(hand).copy();
            player.setHeldItem(hand, ItemStack.EMPTY);
            pedestal.markDirty();
            world.notifyBlockUpdate(pos, state, state, 3);
            return true;
        } else if(!pedestal.item.isEmpty() && player.getHeldItem(hand).isEmpty()) {
            if(world.isRemote) return true;
            player.setHeldItem(hand, pedestal.item.copy());
            pedestal.item = ItemStack.EMPTY;
            pedestal.markDirty();
            world.notifyBlockUpdate(pos, state, state, 3);
            return true;
        } else if (!pedestal.item.isEmpty() && !player.getHeldItem(hand).isEmpty()) {
            if(world.isRemote) return true;
            ItemStack temp = player.getHeldItem(hand).copy();
            player.setHeldItem(hand, pedestal.item.copy());
            pedestal.item = temp;
            pedestal.markDirty();
            world.notifyBlockUpdate(pos, state, state, 3);
            return true;
        }

        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if(!world.isRemote) {
            TileEntityPedestal pedestal = (TileEntityPedestal) world.getTileEntity(pos);
            if(pedestal != null && pedestal.item != null) {
                EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, pedestal.item.copy());
                world.spawnEntity(item);
            }
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    public static TileEntityPedestal castOrNull(@Nullable TileEntity tile) {
        if(tile instanceof TileEntityPedestal) return (TileEntityPedestal) tile;
        return null;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if(!world.isRemote) {
            if(world.isBlockPowered(pos)) {
                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();

                TileEntityPedestal nw = castOrNull(Compat.getTileStandard(world, x + ForgeDirection.NORTH.offsetX * 2 + ForgeDirection.WEST.offsetX * 2, y, z + ForgeDirection.NORTH.offsetZ * 2 + ForgeDirection.WEST.offsetZ * 2));
                TileEntityPedestal n = castOrNull(Compat.getTileStandard(world, x + ForgeDirection.NORTH.offsetX * 3, y, z + ForgeDirection.NORTH.offsetZ * 3));
                TileEntityPedestal ne = castOrNull(Compat.getTileStandard(world, x + ForgeDirection.NORTH.offsetX * 2 + ForgeDirection.EAST.offsetX * 2, y, z + ForgeDirection.NORTH.offsetZ * 2 + ForgeDirection.EAST.offsetZ * 2));
                TileEntityPedestal w = castOrNull(Compat.getTileStandard(world, x + ForgeDirection.WEST.offsetX * 3, y, z + ForgeDirection.WEST.offsetZ * 3));
                TileEntityPedestal center = (TileEntityPedestal) world.getTileEntity(pos);
                TileEntityPedestal e = castOrNull(Compat.getTileStandard(world, x + ForgeDirection.EAST.offsetX * 3, y, z + ForgeDirection.EAST.offsetZ * 3));
                TileEntityPedestal sw = castOrNull(Compat.getTileStandard(world, x + ForgeDirection.SOUTH.offsetX * 2 + ForgeDirection.WEST.offsetX * 2, y, z + ForgeDirection.SOUTH.offsetZ * 2 + ForgeDirection.WEST.offsetZ * 2));
                TileEntityPedestal s = castOrNull(Compat.getTileStandard(world, x + ForgeDirection.SOUTH.offsetX * 3, y, z + ForgeDirection.SOUTH.offsetZ * 3));
                TileEntityPedestal se = castOrNull(Compat.getTileStandard(world, x + ForgeDirection.SOUTH.offsetX * 2 + ForgeDirection.EAST.offsetX * 2, y, z + ForgeDirection.SOUTH.offsetZ * 2 + ForgeDirection.EAST.offsetZ * 2));

                TileEntityPedestal[] tileArray = new TileEntityPedestal[] {nw, n, ne, w, center, e, sw, s, se};
                List<EntityPlayer> nearbyPlayers = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expand(20, 20, 20));

                outer: for(PedestalRecipes.PedestalRecipe recipe : PedestalRecipes.recipes) {

                    /// EXTRA CONDITIONS ///
                    if(recipe.extra == PedestalRecipes.PedestalExtraCondition.FULL_MOON) {
                        if(world.getCelestialAngle(0) < 0.35 || world.getCelestialAngle(0) > 0.65) continue;
                        if(world.provider.getMoonPhase(world.getWorldInfo().getWorldTime()) != 0) continue;
                    }

                    if(recipe.extra == PedestalRecipes.PedestalExtraCondition.NEW_MOON) {
                        if(world.getCelestialAngle(0) < 0.35 || world.getCelestialAngle(0) > 0.65) continue;
                        if(world.provider.getMoonPhase(world.getWorldInfo().getWorldTime()) != 4) continue;
                    }

                    if(recipe.extra == PedestalRecipes.PedestalExtraCondition.SUN) {
                        if(world.getCelestialAngle(0) > 0.15 && world.getCelestialAngle(0) < 0.85) continue;
                    }

                    if(recipe.extra == PedestalRecipes.PedestalExtraCondition.BAD_KARMA) {
                        boolean matches = false;
                        for(EntityPlayer player : nearbyPlayers) if(HbmCapability.getData(player).getReputation() <= -10) { matches = true; break; }
                        if(!matches) continue;
                    }

                    if(recipe.extra == PedestalRecipes.PedestalExtraCondition.GOOD_KARMA) {
                        boolean matches = false;
                        for(EntityPlayer player : nearbyPlayers) if(HbmCapability.getData(player).getReputation() >= 10) { matches = true; break; }
                        if(!matches) continue;
                    }

                    /// CHECK ITEMS ///
                    for(int i = 0; i < 9; i++) {
                        ItemStack pedestal = tileArray[i] != null ? tileArray[i].item : null;
                        if(pedestal == null && recipe.input[i] != null) continue outer;
                        if(pedestal != null && recipe.input[i] == null) continue outer;
                        if(pedestal == null && recipe.input[i] == null) continue;

                        if(!recipe.input[i].matchesRecipe(pedestal, true) || recipe.input[i].stacksize != pedestal.getCount()) continue outer;
                    }

                    /// REMOVE ITEMS ///
                    for(int i = 0; i < 9; i++) {
                        if(i == 4) continue;
                        ItemStack pedestal = tileArray[i] != null ? tileArray[i].item : null;
                        if(pedestal == null && recipe.input[i] == null) continue;
                        tileArray[i].item = null;
                        tileArray[i].markDirty();
                        world.notifyBlockUpdate(tileArray[i].getPos(), state, state, 3);
                    }

                    /// PRODUCE RESULT ///
                    center.item = recipe.output.copy();
                    center.markDirty();
                    world.notifyBlockUpdate(pos, state, state, 3);
                    ExplosionSmallCreator.composeEffect(world, x + 0.5, y + 1.5, z + 0.5, 10, 2.5F, 1F);

                    List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(x + 0.5, y, z + 0.5, x + 0.5, y, z + 0.5).expand(50, 50, 50));
                    for(EntityPlayer player : players) player.addStat(MainRegistry.statLegendary, 1);

                    return;
                }
            }
        }
    }

    @AutoRegister
    public static class TileEntityPedestal extends TileEntity {
        public ItemStack item = ItemStack.EMPTY;

        @Override
        public @Nullable SPacketUpdateTileEntity getUpdatePacket() {
            NBTTagCompound nbt = new NBTTagCompound();
            this.writeToNBT(nbt);
            return new SPacketUpdateTileEntity(this.pos, 0, nbt);
        }

        @Override
        public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
            this.readFromNBT(pkt.getNbtCompound());
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            this.item = new ItemStack(nbt.getCompoundTag("item"));
        }

        @Override
        public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            if(this.item != null) {
                NBTTagCompound stack = new NBTTagCompound();
                this.item.writeToNBT(stack);
                nbt.setTag("item", stack);
            }
            return nbt;
        }

        @Override
        public @NotNull NBTTagCompound getUpdateTag() {
            return this.writeToNBT(new NBTTagCompound());
        }

        @Override
        public void handleUpdateTag(NBTTagCompound tag) {
            this.readFromNBT(tag);
        }
    }
}
