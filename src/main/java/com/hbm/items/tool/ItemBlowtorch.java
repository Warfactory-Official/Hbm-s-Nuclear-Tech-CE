package com.hbm.items.tool;

import com.hbm.api.block.IToolable;
import com.hbm.api.fluidmk2.IFillableItem;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class ItemBlowtorch extends Item implements IFillableItem {
    public ItemBlowtorch(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setMaxStackSize(1);
        this.setFull3D();
        this.setCreativeTab(MainRegistry.controlTab);

        IToolable.ToolType.TORCH.register(new ItemStack(this));
        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        if(this == ModItems.blowtorch) return type == Fluids.GAS;
        if(this == ModItems.acetylene_torch) return type == Fluids.UNSATURATEDS || type == Fluids.OXYGEN;

        return false;
    }

    @Override
    public int tryFill(FluidType type, int amount, ItemStack stack) {

        if (!acceptsFluid(type, stack))
            return amount;

        int toFill = Math.min(amount, getMaxFill(type) - this.getFill(stack, type));
        this.setFill(stack, type, this.getFill(stack, type) + toFill);

        return amount - toFill;
    }

    public int getFill(ItemStack stack, FluidType type) {
        if (!stack.hasTagCompound()) {
            initNBT(stack);
        }
        assert stack.getTagCompound() != null;
        return stack.getTagCompound().getInteger(type.getName());
    }

    public int getMaxFill(FluidType type) {
        if (type == Fluids.GAS) return 4_000;
        if (type == Fluids.UNSATURATEDS) return 8_000;
        if (type == Fluids.OXYGEN) return 16_000;

        return 0;
    }

    public void setFill(ItemStack stack, FluidType type, int fill) {
        if(!stack.hasTagCompound()) {
            initNBT(stack);
        }
        stack.getTagCompound().setInteger(type.getName(), fill);
    }

    public void initNBT(ItemStack stack) {
        stack.setTagCompound(new NBTTagCompound());

        if(this == ModItems.blowtorch) {
            this.setFill(stack, Fluids.GAS, this.getMaxFill(Fluids.GAS));
        }
        if(this == ModItems.acetylene_torch) {
            this.setFill(stack, Fluids.UNSATURATEDS, this.getMaxFill(Fluids.UNSATURATEDS));
            this.setFill(stack, Fluids.OXYGEN, this.getMaxFill(Fluids.OXYGEN));
        }
    }

    public static ItemStack getEmptyTool(Item item) {
        ItemBlowtorch tool = (ItemBlowtorch) item;
        ItemStack stack = new ItemStack(item);

        if(item == ModItems.blowtorch) {
            tool.setFill(stack, Fluids.GAS, 0);
        }
        if(item == ModItems.acetylene_torch) {
            tool.setFill(stack, Fluids.UNSATURATEDS, 0);
            tool.setFill(stack, Fluids.OXYGEN, 0);
        }

        return stack;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        Block b = world.getBlockState(pos).getBlock();

        if (b instanceof IToolable && !world.isRemote) {
            ItemStack stack = player.getHeldItem(hand);

            if (this == ModItems.blowtorch) {
                if (this.getFill(stack, Fluids.GAS) < 250) return EnumActionResult.FAIL;
            }

            if (this == ModItems.acetylene_torch) {
                if (this.getFill(stack, Fluids.UNSATURATEDS) < 20) return EnumActionResult.FAIL;
                if (this.getFill(stack, Fluids.OXYGEN) < 10) return EnumActionResult.FAIL;
            }

            if (((IToolable)b).onScrew(world, player, pos, facing, hitX, hitY, hitZ, hand, IToolable.ToolType.TORCH)) {
                if (this == ModItems.blowtorch) {
                    this.setFill(stack, Fluids.GAS, this.getFill(stack, Fluids.GAS) - 250);
                }

                if (this == ModItems.acetylene_torch) {
                    this.setFill(stack, Fluids.UNSATURATEDS, this.getFill(stack, Fluids.UNSATURATEDS) - 20);
                    this.setFill(stack, Fluids.OXYGEN, this.getFill(stack, Fluids.OXYGEN) - 10);
                }

                player.inventoryContainer.detectAndSendChanges();

                NBTTagCompound dPart = new NBTTagCompound();
                dPart.setByte("count", (byte) 10);
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.Tau, dPart, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 50));
                player.setHeldItem(hand, stack);
            }
        }

        return EnumActionResult.SUCCESS; // Due to minecraft being stupid i have to always return success
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getDurabilityForDisplay(stack) > 0;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        double frac = 0D;

        if(this == ModItems.blowtorch) {
            frac = (double) this.getFill(stack, Fluids.GAS) / (double) this.getMaxFill(Fluids.GAS);
        }

        if(this == ModItems.acetylene_torch) {
            frac = Math.min(
                    (double) this.getFill(stack, Fluids.UNSATURATEDS) / (double) this.getMaxFill(Fluids.UNSATURATEDS),
                    (double) this.getFill(stack, Fluids.OXYGEN) / (double) this.getMaxFill(Fluids.OXYGEN)
            );
        }

        return 1 - frac;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {

        if(this == ModItems.blowtorch) {
            list.add(TextFormatting.YELLOW + getFillGauge(stack, Fluids.GAS));
        }
        if(this == ModItems.acetylene_torch) {
            list.add(TextFormatting.YELLOW + getFillGauge(stack, Fluids.UNSATURATEDS));
            list.add(TextFormatting.AQUA + getFillGauge(stack, Fluids.OXYGEN));
        }
    }

    @SideOnly(Side.CLIENT)
    private String getFillGauge(ItemStack stack, FluidType type) {
        return type.getLocalizedName() + ": " + String.format(Locale.US, "%,d", this.getFill(stack, type)) + " / " + String.format(Locale.US, "%,d", this.getMaxFill(type));
    }

    @Override public boolean providesFluid(FluidType type, ItemStack stack) { return false; }
    @Override public int tryEmpty(FluidType type, int amount, ItemStack stack) { return amount; }

    @Override
    public FluidType getFirstFluidType(ItemStack stack) {
        return null;
    }

    @Override
    public int getFill(ItemStack stack) {
        return 0;
    }
}
