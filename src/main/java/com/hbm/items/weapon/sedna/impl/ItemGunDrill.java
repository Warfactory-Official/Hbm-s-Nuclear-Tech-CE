package com.hbm.items.weapon.sedna.impl;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.api.fluidmk2.IFillableItem;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.factory.XFactoryDrill;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.items.weapon.sedna.mags.MagazineEnergy;
import com.hbm.items.weapon.sedna.mags.MagazineFluid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGunDrill extends ItemGunBaseNT implements IFillableItem, IBatteryItem {

    public ItemGunDrill(WeaponQuality quality, String s, GunConfig... cfg) {
        super(quality, s, cfg);
    }
    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState) {
        int defaultLevel = ToolMaterial.IRON.getHarvestLevel();
        return XFactoryDrill.getModdableHarvestLevel(stack, defaultLevel);
    }
    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return 50.0F; // extremely fast to simulate instant mining
    }
    @Override
    public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
        return true; // this lets us break things that have no set harvest level (i.e. most NTM shit)
    }
    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        World world = player.world;
        IBlockState state = world.getBlockState(pos);
        if (!world.isRemote) {
            // Force block drops ignoring harvest checks
            state.getBlock().dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos); // actually remove the block
        }
        return true; // This is what bypasses the system in place on 1.12. Makes it work identical to 1.7. - Yeti
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        IMagazine mag = ((ItemGunBaseNT) stack.getItem())
                .getConfig(stack, 0)
                .getReceivers(stack)[0]
                .getMagazine(stack);

        // Only accept if it's a fluid magazine and supports this type
        if (mag instanceof MagazineFluid engine) {
            FluidType[] allowedFluids = new FluidType[] { Fluids.GASOLINE, Fluids.GASOLINE_LEADED, Fluids.COALGAS, Fluids.COALGAS_LEADED };
            boolean isSupportedType = false;
            for (FluidType accepted : engine.acceptedTypes) {
                if (accepted == type) {
                    isSupportedType = true;
                    break;
                }
            }
            boolean canInitializeEmpty = false;
            if (getMagCount(stack) == 0) {
                for (FluidType allowed : allowedFluids) {
                    if (allowed == type && isSupportedType) {
                        canInitializeEmpty = true;
                        break;
                    }
                }
            }
            return isSupportedType || canInitializeEmpty;
        }

        return false;
    }
    public static final int transferSpeed = 50;

    @Override
    public int tryFill(FluidType type, int amount, ItemStack stack) {
        if (!acceptsFluid(type, stack)) return amount;
        if (getMagCount(stack) == 0) setMagType(stack, type.getID());

        IMagazine mag = ((ItemGunBaseNT) stack.getItem())
                .getConfig(stack, 0)
                .getReceivers(stack)[0]
                .getMagazine(stack);

        int fill = getMagCount(stack);
        int capacity = mag.getCapacity(stack);
        int needed = capacity - fill;
        if (needed <= 0) return amount; // already full

        int toFill = Math.min(amount, needed);
        toFill = Math.min(toFill, transferSpeed);
        setMagCount(stack, fill + toFill);

        return amount - toFill; // return leftover
    }

    @Override
    public int tryEmpty(FluidType type, int amount, ItemStack stack) {
        int fill = getMagCount(stack);
        int toUnload = Math.min(fill, amount);
        toUnload = Math.min(toUnload, transferSpeed);
        setMagCount(stack, fill - toUnload);
        return toUnload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        IMagazine mag = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0)
                .getReceivers(stack)[0].getMagazine(stack);

        if (mag instanceof MagazineFluid engine) {
            FluidType type = engine.getType(stack, null);
            int amount = engine.getAmount(stack, null);
            int capacity = engine.getCapacity(stack);

            if (type == Fluids.NONE || amount <= 0) {
                tooltip.add(TextFormatting.RED + "Fuel: Empty (" + capacity + " mB)");
            } else {
                tooltip.add(TextFormatting.GREEN + "Fuel: " + type.getName() + " " + amount + "/" + capacity + " mB");
            }
        }
    }

    @Override public boolean providesFluid(FluidType type, ItemStack stack) { return false; }
    public static int getMagCount(ItemStack stack) { return ItemGunBaseNT.getValueInt(stack, MagazineFluid.KEY_MAG_COUNT + 0); }
    public static void setMagCount(ItemStack stack, int value) { ItemGunBaseNT.setValueInt(stack, MagazineFluid.KEY_MAG_COUNT + 0, value); }
    public static int getMagType(ItemStack stack) { return ItemGunBaseNT.getValueInt(stack, MagazineFluid.KEY_MAG_TYPE + 0); }
    public static void setMagType(ItemStack stack, int value) { ItemGunBaseNT.setValueInt(stack, MagazineFluid.KEY_MAG_TYPE + 0, value); }

    @Override
    public FluidType getFirstFluidType(ItemStack stack) {
        IMagazine mag = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
        if(mag instanceof MagazineFluid) return ((MagazineFluid) mag).getType(stack, null);
        return Fluids.NONE;
    }

    @Override
    public int getFill(ItemStack stack) {
        return getMagCount(stack);
    }

    @Override
    public void chargeBattery(ItemStack stack, long i) {
        IMagazine mag = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);

        if(mag instanceof MagazineEnergy engine) {
            engine.setAmount(stack, Math.min(engine.capacity, engine.getAmount(stack, null) + (int) i));
        }
    }

    @Override
    public void setCharge(ItemStack stack, long i) {
        IMagazine mag = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);

        if(mag instanceof MagazineEnergy engine) {
            engine.setAmount(stack, (int) i);
        }
    }

    @Override
    public void dischargeBattery(ItemStack stack, long i) {
        IMagazine mag = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);

        if(mag instanceof MagazineEnergy engine) {
            engine.setAmount(stack, Math.max(0, engine.getAmount(stack, null) - (int) i));
        }
    }

    @Override
    public long getCharge(ItemStack stack) {
        IMagazine mag = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);

        if(mag instanceof MagazineEnergy engine) {
            return engine.getAmount(stack, null);
        }

        return 0;
    }

    @Override
    public long getMaxCharge(ItemStack stack) {
        IMagazine mag = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);

        if(mag instanceof MagazineEnergy engine) {
            return engine.getCapacity(stack);
        }

        return 0;
    }

    @Override public long getChargeRate(ItemStack stack) { return 50_000; }
    @Override public long getDischargeRate(ItemStack stack) { return 0; }
}

