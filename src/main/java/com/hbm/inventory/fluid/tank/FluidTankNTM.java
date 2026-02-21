package com.hbm.inventory.fluid.tank;

import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.GuiInfoContainer;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.util.RenderUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FluidTankNTM implements IFluidHandler, IFluidTank, Cloneable {

    public static final List<IFluidLoadingHandler> loadingHandlers = new ArrayList<>();
    public static final Set<Item> noDualUnload = new HashSet<>();

    static {
        loadingHandlers.add(new FluidLoaderStandard()); //Don't get fooled, this is responsible for fluid containers
        loadingHandlers.add(new FluidLoaderFillableItem());
        loadingHandlers.add(new FluidLoaderInfinite());
        loadingHandlers.add(new FluidLoaderForge());
        noDualUnload.add(ModItems.chlorine_pinwheel);
        noDualUnload.add(ModItems.fluid_barrel_infinite);
        noDualUnload.add(ModItems.inf_water);
        noDualUnload.add(ModItems.inf_water_mk2);
    }

    @Deprecated
    public int index = 0;
    @NotNull
    FluidType type;
    int fluid;
    int maxFluid;
    int pressure = 0;

    public FluidTankNTM(@NotNull FluidType type, int maxFluid) {
        this.type = type;
        this.maxFluid = maxFluid;
    }

    @Deprecated // indices are no longer needed
    public FluidTankNTM(@NotNull FluidType type, int maxFluid, int index) {
        this.type = type;
        this.maxFluid = maxFluid;
        this.index = index;
    }

    public FluidTankNTM withPressure(int pressure) {
        if (this.pressure != pressure) this.setFill(0);
        this.pressure = pressure;
        return this;
    }

    @NotNull
    public FluidType getTankType() {
        return type;
    }

    public void setTankType(FluidType type) {
        if (type == null) {
            type = Fluids.NONE;
        }
        if (this.type == type) return;

        this.type = type;
        this.setFill(0);
    }

    public void resetTank() {
        this.type = Fluids.NONE;
        this.fluid = 0;
        this.pressure = 0;
    }

    /** Changes type and pressure based on a fluid stack, useful for changing tank types based on recipes */
    public FluidTankNTM conform(com.hbm.inventory.fluid.FluidStack stack) {
        this.setTankType(stack.type);
        this.withPressure(stack.pressure);
        return this;
    }

    public Fluid getTankTypeFF() {
        return this.type.getFF();
    }

    public int getFill() {
        return fluid;
    }

    public void setFill(int i) {
        fluid = i;
    }

    public int getMaxFill() {
        return maxFluid;
    }

    public int getPressure() {
        return pressure;
    }

    public int changeTankSize(int size) {
        maxFluid = size;
        if (fluid > maxFluid) {
            int dif = fluid - maxFluid;
            fluid = maxFluid;
            return dif;
        }
        return 0;
    }

    //Fills tank from canisters
    public boolean loadTank(int in, int out, @NotNull IItemHandler slots) {

        if (slots.getStackInSlot(in).isEmpty()) return false;

        boolean isInfiniteBarrel = slots.getStackInSlot(in).getItem() == ModItems.fluid_barrel_infinite;

        if (!isInfiniteBarrel && pressure != 0) return false;

        int prev = this.getFill();

        for (IFluidLoadingHandler handler : loadingHandlers) {
            if (handler.emptyItem(slots, in, out, this)) {
                break;
            }
        }

        return this.getFill() > prev;
    }

    //Fills canisters from tank
    public boolean unloadTank(int in, int out, @NotNull IItemHandler slots) {

        if (slots.getStackInSlot(in).isEmpty()) return false;

        int prev = this.getFill();

        for (IFluidLoadingHandler handler : loadingHandlers) {
            if (handler.fillItem(slots, in, out, this)) {
                break;
            }
        }

        return this.getFill() < prev;
    }

    public boolean setType(int in, @NotNull IItemHandler slots) {
        return setType(in, in, slots);
    }

    /**
     * Changes the tank type and returns true if successful
     *
     * @param in
     * @param out
     * @param slots
     * @return
     */
    public boolean setType(int in, int out, @NotNull IItemHandler slots) {

        if (!slots.getStackInSlot(in).isEmpty() && slots.getStackInSlot(in).getItem() instanceof IItemFluidIdentifier id) {

            if (in == out) {
                FluidType newType = id.getType(null, 0, 0, 0, slots.getStackInSlot(in));

                if (type != newType) {
                    type = newType;
                    fluid = 0;
                    return true;
                }

            } else if (slots.getStackInSlot(out).isEmpty()) {
                FluidType newType = id.getType(null, 0, 0, 0, slots.getStackInSlot(in));
                if (type != newType) {
                    type = newType;
                    slots.insertItem(out, slots.getStackInSlot(in).copy(), false);
                    slots.getStackInSlot(in).shrink(1);
                    fluid = 0;
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Renders the fluid texture into a GUI, with the height based on the fill state
     *
     * @param x      the tank's left side
     * @param y      the tank's bottom side (convention from the old system, changing it now would be a pain in the ass)
     * @param z      the GUI's zLevel
     * @param width
     * @param height
     */
    public void renderTank(int x, int y, double z, int width, int height) {
        renderTank(x, y, z, width, height, 0);
    }

    public void renderTank(int x, int y, double z, int width, int height, int orientation) {
        boolean wasBlendEnabled = RenderUtil.isBlendEnabled();
        if (!wasBlendEnabled) GlStateManager.enableBlend();

        int color = type.getTint();
        double r = ((color & 0xff0000) >> 16) / 255D;
        double g = ((color & 0x00ff00) >> 8) / 255D;
        double b = ((color & 0x0000ff) >> 0) / 255D;
        GL11.glColor3d(r, g, b);

        y -= height;

        Minecraft.getMinecraft().getTextureManager().bindTexture(type.getTexture());

        int i = maxFluid != 0 ? (fluid * height) / maxFluid : 0;

        double minX = x;
        double maxX = x;
        double minY = y;
        double maxY = y;

        double minV = 1D - i / 16D;
        double maxV = 1D;
        double minU = 0D;
        double maxU = width / 16D;

        if (orientation == 0) {
            maxX += width;
            minY += height - i;
            maxY += height;
        }

        if (orientation == 1) {
            i = (fluid * width) / maxFluid;
            maxX += i;
            maxY += height;

            minV = 0D;
            maxV = height / 16D;
            minU = 1D;
            maxU = 1D - i / 16D;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(minX, maxY, z).tex(minU, maxV).endVertex();
        bufferbuilder.pos(maxX, maxY, z).tex(maxU, maxV).endVertex();
        bufferbuilder.pos(maxX, minY, z).tex(maxU, minV).endVertex();
        bufferbuilder.pos(minX, minY, z).tex(minU, minV).endVertex();
        tessellator.draw();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (!wasBlendEnabled) GlStateManager.disableBlend();
    }

    public void renderTankInfo(@NotNull GuiInfoContainer gui, int mouseX, int mouseY, int x, int y, int width, int height) {
        if (x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY) {

            List<String> list = new ArrayList();
            list.add(this.type.getLocalizedName());
            list.add(fluid + "/" + maxFluid + "mB");

            if (this.pressure != 0) {
                list.add(TextFormatting.RED + "Pressure: " + this.pressure + " PU");
            }

            type.addInfo(list);
            gui.drawFluidInfo(list.toArray(new String[0]), mouseX, mouseY);
        }
    }

    //Called by TE to save fillstate
    public void writeToNBT(NBTTagCompound nbt, String s) {
        nbt.setInteger(s, fluid);
        nbt.setInteger(s + "_max", maxFluid);
        nbt.setInteger(s + "_type", type.getID());
        nbt.setShort(s + "_p", (short) pressure);
    }

    //Called by TE to load fillstate
    public void readFromNBT(@NotNull NBTTagCompound nbt, String s) {
        fluid = nbt.getInteger(s);
        int max = nbt.getInteger(s + "_max");
        if (max > 0) maxFluid = max;

        fluid = MathHelper.clamp(fluid, 0, max);

        type = Fluids.fromNameCompat(nbt.getString(s + "_type")); //compat
        if (type == Fluids.NONE) type = Fluids.fromID(nbt.getInteger(s + "_type"));

        this.pressure = nbt.getShort(s + "_p");
    }

    public void serialize(@NotNull ByteBuf buf) {
        buf.writeInt(fluid);
        buf.writeInt(maxFluid);
        buf.writeInt(type.getID());
        buf.writeShort((short) pressure);
    }

    public void deserialize(@NotNull ByteBuf buf) {
        fluid = buf.readInt();
        maxFluid = buf.readInt();
        type = Fluids.fromID(buf.readInt());
        pressure = buf.readShort();
    }

    /**
     * @deprecated use {@link #getTankType()}, {@link #getFill()}, {@link #getMaxFill()} whenever possible
     */
    @NotNull
    @Override
    @Deprecated
    public IFluidTankProperties[] getTankProperties() {
        Fluid fluid = getTankTypeFF();
        int amount = getFill();
        int capacity = getMaxFill();

        FluidStack stack = (fluid != null && amount > 0) ? new FluidStack(fluid, amount) : null;

        return new IFluidTankProperties[]{new FluidTankProperties(stack, capacity)};
    }

    /**
     * @deprecated use {@link #getTankType()} whenever possible
     */
    @Nullable
    @Override
    @Deprecated
    public FluidStack getFluid() {
        Fluid fluid = getTankTypeFF();
        int amount = getFill();

        return (fluid != null && amount > 0) ? new FluidStack(fluid, amount) : null;
    }

    /**
     * @deprecated use {@link #getFill()} whenever possible
     */
    @Override
    @Deprecated
    public int getFluidAmount() {
        return getFill();
    }

    /**
     * @deprecated use {@link #getMaxFill()} whenever possible
     */
    @Override
    @Deprecated
    public int getCapacity() {
        return getMaxFill();
    }

    /**
     * @deprecated use {@link #getTankType()}, {@link #getFill()}, {@link #getMaxFill()} whenever possible
     */
    @NotNull
    @Override
    @Deprecated
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(getFluid(), getCapacity());
    }

    /**
     * @deprecated use {@link #fill(FluidType, int, boolean)} and {@link #setFill(int)} whenever possible
     */
    @Override
    @Deprecated
    public int fill(@Nullable FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }
        if (this.type == Fluids.NONE) {
            FluidType incomingType = NTMFluidCapabilityHandler.getFluidType(resource.getFluid());
            if (incomingType == null || incomingType == Fluids.NONE) return 0;
            int toTransfer = Math.min(getMaxFill(), resource.amount);
            if (doFill) {
                this.type = incomingType;
                this.setFill(toTransfer);
            }
            return toTransfer;
        } else {
            Fluid currentForgeFluid = getTankTypeFF();
            if (currentForgeFluid == null || !currentForgeFluid.equals(resource.getFluid())) {
                return 0;
            }
            int demand = getMaxFill() - getFill();
            int toTransfer = Math.min(demand, resource.amount);
            if (doFill && toTransfer > 0) {
                setFill(getFill() + toTransfer);
            }
            return toTransfer;
        }
    }

    public int fill(@Nullable FluidType incomingType, int amount, boolean doFill) {
        if (incomingType == null || incomingType == Fluids.NONE || amount <= 0) return 0;
        if (this.type == Fluids.NONE) {
            int toTransfer = Math.min(getMaxFill(), amount);
            if (doFill) {
                this.type = incomingType;
                this.setFill(toTransfer);
            }
            return toTransfer;
        } else {
            if (!this.type.equals(incomingType)) return 0;
            int toTransfer = Math.min(getMaxFill() - getFill(), amount);
            if (doFill && toTransfer > 0) setFill(getFill() + toTransfer);
            return toTransfer;
        }
    }

    /**
     * @deprecated use {@link #setFill(int)} whenever possible
     */
    @Nullable
    @Override
    @Deprecated
    public FluidStack drain(@Nullable FluidStack resource, boolean doDrain) {
        Fluid currentType = getTankTypeFF();
        if (resource == null || !resource.getFluid().equals(currentType)) return null;
        int toDrain = Math.min(resource.amount, getFill());
        FluidStack drained = new FluidStack(currentType, toDrain);
        if (doDrain) {
            setFill(getFill() - toDrain);
        }
        return drained;
    }

    /**
     * @deprecated use {@link #setFill(int)} whenever possible
     */
    @Nullable
    @Override
    @Deprecated
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (getTankType() == Fluids.NONE || getTankTypeFF() == null || getFill() == 0) return null;
        int toDrain = Math.min(maxDrain, getFill());
        FluidStack drained = new FluidStack(getTankTypeFF(), toDrain);
        if (doDrain) setFill(getFill() - toDrain);
        return drained;
    }

    @Override
    public FluidTankNTM clone() {
        try {
            return (FluidTankNTM) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
