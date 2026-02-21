package com.hbm.tileentity.machine.rbmk;

import com.hbm.blocks.machine.rbmk.RBMKControl;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.interfaces.ICopiable;
import com.hbm.inventory.container.ContainerRBMKControl;
import com.hbm.inventory.control_panel.ControlEvent;
import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.gui.GUIRBMKControl;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.rbmk.RBMKColumn.ColumnType;
import com.hbm.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AutoRegister
public class TileEntityRBMKControlManual extends TileEntityRBMKControl implements IControlReceiver, IGUIProvider, ICopiable {

    public RBMKColor color;
    public double startingLevel;

    public TileEntityRBMKControlManual() {
        super();
    }

    public void setColor(int color) {
        RBMKColor new_color = RBMKColor.VALUES[color];
        this.color = new_color;
    }

    public boolean isSameColor(int color) {
        return this.color == RBMKColor.VALUES[color];
    }

    @Override
    public String getName() {
        return "container.rbmkControl";
    }

    @Override
    public boolean isModerated() {
        return ((RBMKControl) this.getBlockType()).moderated;
    }

    @Override
    public void setTarget(double target) {
        this.targetLevel = target;
        this.startingLevel = this.level;
    }

    @Override
    public double getMult() {

        double surge = 0;

        if (this.targetLevel < this.startingLevel && Math.abs(this.level - this.targetLevel) > 0.01D) {
            surge = Math.sin(Math.pow((1D - this.level), 15) * Math.PI) * (this.startingLevel - this.targetLevel) * RBMKDials.getSurgeMod(world);

        }

        return this.level + surge;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return Vec3.createVectorHelper(pos.getX() - player.posX, pos.getY() - player.posY, pos.getZ() - player.posZ).length() < 20;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {

        if (data.hasKey("level")) {
            this.setTarget(data.getDouble("level"));
        }

        if (data.hasKey("color")) {
            int c = Math.abs(data.getInteger("color")) % RBMKColor.VALUES.length; //to stop naughty kids from sending packets that crash the server

            RBMKColor newCol = RBMKColor.VALUES[c];

            if (newCol == this.color) {
                this.color = null;
            } else {
                this.color = newCol;
            }
        }

        this.markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        if (nbt.hasKey("startingLevel"))
            this.startingLevel = nbt.getDouble("startingLevel");

        if (nbt.hasKey("color"))
            this.color = RBMKColor.VALUES[nbt.getInteger("color")];
        else
            this.color = null;
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setDouble("startingLevel", this.startingLevel);
        nbt.setDouble("mult", this.getMult());

        if (color != null)
            nbt.setInteger("color", color.ordinal());
        return nbt;
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(this.startingLevel);
        if (this.color != null)
            buf.writeInt(this.color.ordinal());
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.startingLevel = buf.readDouble();
        if (buf.isReadable(1)) {
            int color = buf.readInt();
            this.color = RBMKColor.VALUES[MathHelper.clamp(color, 0, RBMKColor.VALUES.length)];
        }
    }

    @Override
    public ColumnType getConsoleType() {
        return ColumnType.CONTROL;
    }

    @Override
    public RBMKColumn getConsoleData() {
        RBMKColumn.ControlColumn data = (RBMKColumn.ControlColumn) super.getConsoleData();

        if (this.color != null)
            data.color = (short) this.color.ordinal();
        else
            data.color = (short) -1;

        return data;
    }

    // control panel
    @Override
    public Map<String, DataValue> getQueryData() {
        Map<String, DataValue> data = super.getQueryData();

        if (this.color != null) {
            data.put("color", new DataValueFloat(this.color.ordinal()));
        }

        return data;
    }

    @Override
    public void receiveEvent(BlockPos from, ControlEvent e) {
        super.receiveEvent(from, e);

        if (e.name.equals("rbmk_ctrl_set_level")) {
            this.startingLevel = this.level;
            setTarget(Math.min(1, Math.max(0, e.vars.get("level").getNumber() / 100)));
            markDirty();
        }
        if (e.name.equals("rbmk_ctrl_set_color")) {
            this.color = RBMKColor.VALUES[(int) (e.vars.get("color").getNumber()) % RBMKColor.VALUES.length - 1];
        }
    }

    @Override
    public List<String> getInEvents() {
        List<String> events = new ArrayList<>(super.getInEvents());
        events.add("rbmk_ctrl_set_level");
        events.add("rbmk_ctrl_set_color");
        return events;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerRBMKControl(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIRBMKControl(player.inventory, this);
    }

    public NBTTagCompound getSettings(World world, int x, int y, int z) {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("color", color.ordinal());
        return data;
    }

    @Override
    public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
        if (nbt.hasKey("color")) color = EnumUtil.grabEnumSafely(RBMKColor.values(), nbt.getInteger("color"));
    }

    public enum RBMKColor {
        RED,
        YELLOW,
        GREEN,
        BLUE,
        PURPLE;

        public static final RBMKColor[] VALUES = values();
    }

    //Note: IDK how opencomupters works
    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getColor(Context context, Arguments args) {
        return new Object[] {this.color.ordinal()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] setColor(Context context, Arguments args) {
        int colorI = args.checkInteger(0);
        colorI = MathHelper.clamp(colorI, 0, 4);
        this.color = RBMKColor.values()[colorI];
        return new Object[] {true};
    }
}
