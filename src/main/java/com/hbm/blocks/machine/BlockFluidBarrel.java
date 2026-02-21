package com.hbm.blocks.machine;

import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BaseBarrel;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.block.ItemBlockBase;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.machine.TileEntityBarrel;
import com.hbm.util.I18nUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.List;


public class BlockFluidBarrel extends BlockContainer implements ITooltipProvider, IPersistentInfoProvider, ICustomBlockItem {
    public static boolean keepInventory;
    private int capacity;

    public BlockFluidBarrel(Material materialIn, int cap, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        capacity = cap;

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityBarrel(capacity);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List<String> list, boolean ext) {
        FluidTankNTM tank = new FluidTankNTM(Fluids.NONE, 0);
        tank.readFromNBT(persistentTag, "tank");
        list.add(TextFormatting.YELLOW + "" + tank.getFill() + "/" + tank.getMaxFill() + "mB " + tank.getTankType().getLocalizedName());
    }

    @Override
    public void registerItem() {
        ItemBlock itemBlock = new ItemBlockBase(this);
        itemBlock.setRegistryName(this.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock);
    }


    @Override
    public void addInformation(ItemStack stack, World player, List<String> list, ITooltipFlag advanced) {
        if (this == ModBlocks.barrel_plastic) {
            list.add(TextFormatting.AQUA + I18nUtil.resolveKey("desc.capacity", "12,000"));
            list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannothot"));
            list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotcor"));
            list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotam"));
        }

        if (this == ModBlocks.barrel_corroded) {
            list.add(TextFormatting.AQUA + I18nUtil.resolveKey("desc.capacity", "6,000"));
            list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhot"));
            list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhighcor"));
            list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotam"));
            list.add(TextFormatting.RED + I18nUtil.resolveKey("desc.leaky"));
        }

        if (this == ModBlocks.barrel_iron) {
            list.add(TextFormatting.AQUA + I18nUtil.resolveKey("desc.capacity", "8,000"));
            list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhot"));
            list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotcor1"));
            list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotam"));
        }

        if (this == ModBlocks.barrel_steel) {
            list.add(TextFormatting.AQUA + I18nUtil.resolveKey("desc.capacity", "16,000"));
            list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhot"));
            list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.cancor"));
            list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannothighcor"));
            list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotam"));
        }

        if (this == ModBlocks.barrel_antimatter) {
            list.add(TextFormatting.AQUA + I18nUtil.resolveKey("desc.capacity", "16,000"));
            list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhot"));
            list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhighcor"));
            list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canam"));
        }

        if (this == ModBlocks.barrel_tcalloy) {
            list.add(TextFormatting.AQUA + I18nUtil.resolveKey("desc.capacity", "24,000"));
            list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhot"));
            list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhighcor"));
            list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotam"));
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;

        } else if (!player.isSneaking()) {
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            return true;

        } else if (player.isSneaking()) {
            TileEntityBarrel mileEntity = (TileEntityBarrel) world.getTileEntity(pos);

            if (!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof IItemFluidIdentifier) {
                FluidType type = ((IItemFluidIdentifier) player.getHeldItem(hand).getItem()).getType(world, pos.getX(), pos.getY(), pos.getZ(), player.getHeldItem(hand));

                mileEntity.tankNew.setTankType(type);
                mileEntity.markDirty();
                player.sendMessage(new TextComponentString("Changed type to ")
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))
                        .appendSibling(new TextComponentTranslation(type.getConditionalName()))
                        .appendSibling(new TextComponentString("!")));
            }
            return true;

        } else {
            return false;
        }
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!keepInventory)
            InventoryHelper.dropInventoryItems(worldIn, pos, worldIn.getTileEntity(pos));
        IPersistentNBT.breakBlock(worldIn, pos, state);
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        IPersistentNBT.onBlockPlacedBy(world, pos, stack);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        IPersistentNBT.onBlockHarvested(world, pos, player);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BaseBarrel.BARREL_BB;
    }
}
