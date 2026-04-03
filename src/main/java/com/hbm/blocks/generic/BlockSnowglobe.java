package com.hbm.blocks.generic;

import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.gui.GUIScreenSnowglobe;
import com.hbm.items.IModelRegister;
import com.hbm.main.MainRegistry;
import com.hbm.main.client.NTMClientRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;

public class BlockSnowglobe extends BlockContainer implements INBTBlockTransformable, IGUIProvider, ICustomBlockItem {
    public static final PropertyInteger META = PropertyInteger.create("rot", 0, 15);
    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(4D / 16D, 0.0D, 4D / 16D, 1.0D - 4D / 16D, 0.3125D, 1.0D - 4D / 16D);

    public BlockSnowglobe(String regName) {
        super(Material.GLASS);

        setRegistryName(regName);
        setTranslationKey(regName);
        setSoundType(SoundType.GLASS);
        setDefaultState(this.blockState.getBaseState().withProperty(META, 0));

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        return BOUNDS;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess worldIn, @NotNull BlockPos pos) {
        return BOUNDS;
    }

    @Override
    public @NotNull Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public @NotNull ItemStack getPickBlock(@NotNull IBlockState state, @NotNull RayTraceResult target, World world, @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        TileEntitySnowglobe te = (TileEntitySnowglobe) world.getTileEntity(pos);

        if (te instanceof BlockSnowglobe.TileEntitySnowglobe entity) {
            return new ItemStack(this, 1, entity.type.ordinal());
        }
        return super.getPickBlock(state, target, world, pos, player);
    }

    @Override
    public boolean canHarvestBlock(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        return true;
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, @NotNull BlockPos pos, @NotNull IBlockState state, @Nullable TileEntity te, @NotNull ItemStack tool) {
        player.addStat(Objects.requireNonNull(StatList.getBlockStats(this)));
        player.addExhaustion(0.025F);

        if (!world.isRemote && !player.capabilities.isCreativeMode) {
            if (te instanceof BlockSnowglobe.TileEntitySnowglobe entity) {
                ItemStack drop = new ItemStack(this, 1, entity.type.ordinal());
                spawnAsEntity(world, pos, drop);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> items) {
        if (tab == CreativeTabs.SEARCH || tab == this.getCreativeTab()) {
            for (int i = 1; i < SnowglobeType.values().length; i++)
                items.add(new ItemStack(this, 1, i));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull IBlockState getStateForPlacement(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        int rotation = MathHelper.floor((double) ((placer.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
        return this.getDefaultState().withProperty(META, rotation);
    }

    @Override
    public int transformMeta(int meta, int coordBaseMode) {
        return (meta + coordBaseMode * 4) % 16;
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, META);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(META, meta & 15);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    public void onBlockPlacedBy(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntitySnowglobe snowglobe) {
            snowglobe.type = BlockSnowglobe.SnowglobeType.VALUES[Math.abs(stack.getItemDamage()) % BlockSnowglobe.SnowglobeType.VALUES.length];
            snowglobe.markDirty();
        }
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        return new TileEntitySnowglobe();
    }

    @Override
    public void registerItem() {
        ItemBlock itemBlock = new BlockSnowglobe.BlockSnowglobeItem(this);
        itemBlock.setRegistryName(Objects.requireNonNull(this.getRegistryName()));
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    private static class BlockSnowglobeItem extends CustomBlockItem implements IModelRegister {
        private BlockSnowglobeItem(Block block) {
            super(block);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void registerModels() {
            ModelResourceLocation syntheticLocation = NTMClientRegistry.getSyntheticTeisrModelLocation(this);
            for (int meta = 0; meta < BlockSnowglobe.SnowglobeType.VALUES.length; meta++) {
                ModelLoader.setCustomModelResourceLocation(this, meta, syntheticLocation);
            }
        }
    }

    @AutoRegister
    public static class TileEntitySnowglobe extends TileEntity {

        public SnowglobeType type = SnowglobeType.NONE;
        private AxisAlignedBB bb;

        @Override
        public @NotNull NBTTagCompound getUpdateTag() {
            return writeToNBT(super.getUpdateTag());
        }

        @Override
        public void handleUpdateTag(@NotNull NBTTagCompound tag) {
            readFromNBT(tag);
        }

        @Nullable
        @Override
        public SPacketUpdateTileEntity getUpdatePacket() {
            NBTTagCompound nbt = new NBTTagCompound();
            writeToNBT(nbt);
            return new SPacketUpdateTileEntity(this.pos, 0, nbt);
        }

        @Override
        public void onDataPacket(@NotNull NetworkManager net, SPacketUpdateTileEntity pkt) {
            readFromNBT(pkt.getNbtCompound());
        }

        @Override
        public void readFromNBT(@NotNull NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            this.type = SnowglobeType.values()[Math.abs(nbt.getByte("type")) % SnowglobeType.values().length];
        }

        @Override
        public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            nbt.setByte("type", (byte) type.ordinal());
            return nbt;
        }

        @Override
        public AxisAlignedBB getRenderBoundingBox() {
            if (bb == null) bb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            return bb;
        }
    }

    public enum SnowglobeType {
        NONE("NONE", null), RIVETCITY("Rivet City", "Welcome to Rivet City. Please wait while the bridge extends."), TENPENNYTOWER("Tenpenny Tower", "Tenpenny Tower is the brainchild of Allistair Tenpenny, a British refugee who came to the Capital Wasteland seeking his fortune."), LUCKY38("Lucky 38", "My guess? Leads to a big cashout at some casino - and if the \"38\" on it is any indication... well... Lucky 38 it is."), SIERRAMADRE("Sierra Madre", "It's the moment you've been waiting for, the reason we're all here - the Gala Event, the Grand Opening of the Sierra Madre Casino."), PRYDWEN("The Prydwen", "People of the Commonwealth. Do not interfere. Our intentions are peaceful. We are the Brotherhood of Steel.");

        public static final BlockSnowglobe.SnowglobeType[] VALUES = values();

        public final String label;
        public final String inscription;

        SnowglobeType(String label, String inscription) {
            this.label = label;
            this.inscription = inscription;
        }
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIScreenSnowglobe((TileEntitySnowglobe) world.getTileEntity(new BlockPos(x, y, z)));
    }
}
