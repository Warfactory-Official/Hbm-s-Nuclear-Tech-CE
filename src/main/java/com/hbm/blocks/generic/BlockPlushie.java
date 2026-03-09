package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.IModelRegister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import com.hbm.world.gen.nbt.INBTTileEntityTransformable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class BlockPlushie extends BlockContainer implements IBlockMulti, ITooltipProvider, INBTBlockTransformable, ICustomBlockItem {
    public static final PropertyInteger META = PropertyInteger.create("rot", 0, 15);

    public BlockPlushie(String name) {
        super(Material.CLOTH);

        setRegistryName(name);
        setTranslationKey(name);
        setSoundType(SoundType.CLOTH);

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

    @Override
    public @NotNull Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) {
        return ItemStack.EMPTY.getItem();
    }

    @Override
    public @NotNull ItemStack getPickBlock(@NotNull IBlockState state, @NotNull RayTraceResult target, World world, @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPlushie entity) {
            return new ItemStack(this, 1, entity.type.ordinal());
        }
        return super.getPickBlock(state, target, world, pos, player);
    }

    @Override
    public void harvestBlock(@NotNull World world, EntityPlayer player, @NotNull BlockPos pos, @NotNull IBlockState state, @Nullable TileEntity te, @NotNull ItemStack stack) {
        player.addStat(Objects.requireNonNull(StatList.getBlockStats(this)));
        player.addExhaustion(0.025F);

        if (!world.isRemote && !player.capabilities.isCreativeMode) {
            if (te instanceof TileEntityPlushie entity) {
                ItemStack drop = new ItemStack(this, 1, entity.type.ordinal());
                spawnAsEntity(world, pos, drop);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(@NotNull CreativeTabs itemIn, @NotNull NonNullList<ItemStack> items) {
        for (int i = 1; i < PlushieType.values().length; i++)
            items.add(new ItemStack(this, 1, i));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, EntityLivingBase placer, @NotNull ItemStack stack) {
        int meta = MathHelper.floor((double) ((placer.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
        worldIn.setBlockState(pos, this.getDefaultState().withProperty(META, meta), 2);

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityPlushie plushie) {
            plushie.type = PlushieType.values()[Math.abs(stack.getItemDamage()) % PlushieType.values().length];
            plushie.markDirty();
        }
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
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        return new TileEntityPlushie();
    }

    @Override
    public void registerItem() {
        ItemBlock itemBlock = new BlockPlushie.BlockPlushieItem(this);
        itemBlock.setRegistryName(Objects.requireNonNull(this.getRegistryName()));
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    private static class BlockPlushieItem extends ICustomBlockItem.CustomBlockItem implements IModelRegister {
        private BlockPlushieItem(Block block) {
            super(block);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void registerModels() {
            for (int meta = 0; meta < BlockPlushie.PlushieType.VALUES.length; meta++) {
                ModelLoader.setCustomModelResourceLocation(this, meta, new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory"));
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public @NotNull String getItemStackDisplayName(ItemStack stack) {
            PlushieType type = PlushieType.values()[Math.abs(stack.getItemDamage()) % PlushieType.values().length];
            return I18n.translateToLocalFormatted(this.getTranslationKey() + ".name", type == PlushieType.NONE ? "" : type.label).trim();
        }
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {

        TileEntityPlushie plushie = (TileEntityPlushie) world.getTileEntity(pos);
        if (plushie == null) {
            return false;
        }

        if (world.isRemote) {
            plushie.squishTimer = 11;
        } else {
            if (plushie.type == PlushieType.HUNDUN) {
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.hundunsMagnificentHowl, SoundCategory.BLOCKS, 100F, 1F);
            } else {
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.squeakyToy, SoundCategory.BLOCKS, 0.25F, 1F);
            }
        }
        return true;
    }

    @Override
    public int transformMeta(int meta, int coordBaseMode) {
        return (meta + coordBaseMode * 4) % 16;
    }

    @Override
    public int getSubCount() {
        return PlushieType.values().length;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) {
        PlushieType type = PlushieType.values()[Math.abs(stack.getItemDamage()) % PlushieType.values().length];
        if (type.inscription != null) list.add(type.inscription);
    }

    public enum PlushieType {
        NONE("NONE", null), YOMI("Yomi", "Hi! Can I be your rabbit friend?"), NUMBERNINE("Number Nine", "None of y'all deserve coal."), HUNDUN("Hundun", "混沌"), DERG("Dragon", "Squeeze him.");

        public static final BlockPlushie.PlushieType[] VALUES = values();

        public final String label;
        public final String inscription;

        PlushieType(String label, String inscription) {
            this.label = label;
            this.inscription = inscription;
        }
    }

    @AutoRegister
    public static class TileEntityPlushie extends TileEntity implements INBTTileEntityTransformable, ITickable {

        public PlushieType type = PlushieType.NONE;
        public int squishTimer;

        @Override
        public void update() {
            if (squishTimer > 0) squishTimer--;
        }

        @Override
        public @NotNull NBTTagCompound getUpdateTag() {
            return writeToNBT(super.getUpdateTag());
        }

        @Override
        public void handleUpdateTag(@NotNull NBTTagCompound tag) {
            readFromNBT(tag);
        }

        @Override
        public @Nullable SPacketUpdateTileEntity getUpdatePacket() {
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
            this.type = PlushieType.values()[Math.abs(nbt.getByte("type")) % PlushieType.values().length];
        }

        @Override
        public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            nbt.setByte("type", (byte) type.ordinal());
            return nbt;
        }

        @Override
        public void transformTE(World world, int coordBaseMode) {
            type = PlushieType.values()[world.rand.nextInt(PlushieType.values().length - 1) + 1];
        }
    }
}
