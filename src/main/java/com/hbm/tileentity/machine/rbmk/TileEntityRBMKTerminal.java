package com.hbm.tileentity.machine.rbmk;

import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.gui.GUIScreenRBMKTerminal;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

@AutoRegister
@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public class TileEntityRBMKTerminal extends TileEntityLoadedBase implements ITickable, IGUIProvider, IControlReceiver, IRORInteractive, SimpleComponent, CompatHandler.OCComponent {

    public final String[] history = new String[17];
    public String channel = "";
    public String repeatCmd = "";
    public boolean doesRepeat;
    public boolean ocMode = false;

    public TileEntityRBMKTerminal() {
        Arrays.fill(this.history, "");
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (this.ocMode) {
                this.networkPackNT(10);
                return;
            }

            if (!this.channel.isEmpty() && !this.repeatCmd.isEmpty()) {
                RTTYSystem.broadcast(world, this.channel, this.repeatCmd);
            }
            this.networkPackNT(50);
        }
    }

    public void eval(String cmd) {
        if (cmd == null) return;

        if (this.ocMode) {
            push(cmd);
            this.markChanged();
            return;
        }

        push(cmd);
        if (cmd.isEmpty()) return;

        if (cmd.startsWith("chan ")) {
            this.channel = cmd.substring(5);
            push("Set channel to " + (this.channel.isEmpty() ? "<none>" : this.channel));
            this.markChanged();
            return;
        }

        if (cmd.equals("chan")) {
            this.channel = "";
            push("Set channel to <none>");
            this.markChanged();
            return;
        }

        if (cmd.startsWith("start ")) {
            this.repeatCmd = cmd.substring(6);
            push("Repeating signal on " + this.channel);
            this.markChanged();
            return;
        }

        if (cmd.equals("stop")) {
            this.repeatCmd = "";
            push("Stopping repeat signal");
            this.markChanged();
            return;
        }

        if (cmd.startsWith("send ")) {
            if (this.channel.isEmpty()) {
                push("Cannot send - no channel set");
                return;
            }
            RTTYSystem.broadcast(world, this.channel, cmd.substring(5));
            push("Sent signal on " + this.channel);
            return;
        }

        if (cmd.equals("horse")) {
            push("Horse.");
            return;
        }

        if (cmd.equals("selfdestruct")) {
            world.destroyBlock(pos, false);
            ExplosionVNT vnt = new ExplosionVNT(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5.0F, null);
            vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 50).setupPiercing(5F, 0.5F));
            vnt.setPlayerProcessor(new PlayerProcessorStandard());
            vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
            vnt.explode();
            return;
        }

        if (cmd.equals("clear")) {
            for (int i = 0; i < this.history.length; i++) {
                this.history[i] = "";
            }
            return;
        }

        push("Unrecognized command!");
    }

    public void push(String msg) {
        for (int i = this.history.length - 1; i > 0; i--) {
            this.history[i] = this.history[i - 1];
        }
        this.history[0] = msg;
        this.markChanged();
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(!this.repeatCmd.isEmpty());
        for (String line : this.history) {
            BufferUtil.writeString(buf, line);
        }
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.doesRepeat = buf.readBoolean();
        for (int i = 0; i < this.history.length; i++) {
            this.history[i] = BufferUtil.readString(buf);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.channel = nbt.getString("channel");
        this.repeatCmd = nbt.getString("repeatCmd");
        this.ocMode = nbt.getBoolean("ocMode");
        for (int i = 0; i < this.history.length; i++) {
            this.history[i] = nbt.getString("history" + i);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("channel", this.channel);
        nbt.setString("repeatCmd", this.repeatCmd);
        nbt.setBoolean("ocMode", this.ocMode);
        for (int i = 0; i < this.history.length; i++) {
            nbt.setString("history" + i, this.history[i]);
        }
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < 15D * 15D;
    }

    @Override
    public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {
        if (data.hasKey("cmd")) {
            eval(data.getString("cmd"));
            this.markChanged();
        }
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIScreenRBMKTerminal(this);
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                PREFIX_FUNCTION + "clear",
                PREFIX_FUNCTION + "write" + NAME_SEPARATOR + "text",
                PREFIX_FUNCTION + "set<line#>" + NAME_SEPARATOR + "text",
                PREFIX_FUNCTION + "submit" + NAME_SEPARATOR + "command"
        };
    }

    @Override
    public String runRORFunction(String name, String[] params) {

        if((PREFIX_FUNCTION + "clear").equals(name)) {
            Arrays.fill(history, "");
            this.markChanged();
            return null;
        }

        String allParams = String.join(" ", params);

        if((PREFIX_FUNCTION + "write").equals(name)) {
            this.push(allParams);
            this.markChanged();
            return null;
        }

        if(name.startsWith(PREFIX_FUNCTION + "set")) {
            int line = IRORInteractive.parseInt(name.substring(PREFIX_FUNCTION.length() + 3), 1, history.length) - 1;
            this.history[line] = allParams;
            this.markChanged();
            return null;
        }

        if((PREFIX_FUNCTION + "submit").equals(name)) {
            this.eval(allParams);
            return null;
        }

        return null;
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "rbmk_terminal";
    }

    @Callback(direct = true, limit = 2)
    @Optional.Method(modid = "opencomputers")
    public Object[] enableOCMode(Context context, Arguments args) {
        ocMode = args.checkBoolean(0);
        if(ocMode) {
            Arrays.fill(history, "");
            push("OC MODE ENABLED");
            push("Terminal ready.");
        }
        markDirty();
        return new Object[] {true};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] isOCMode(Context context, Arguments args) {
        return new Object[] {ocMode};
    }

    @Callback(direct = true, limit = 2)
    @Optional.Method(modid = "opencomputers")
    public Object[] write(Context context, Arguments args) {
        if(!ocMode) return new Object[] {false, "OC mode not enabled"};
        push(args.checkString(0));
        return new Object[] {true};
    }

    @Callback(direct = true, limit = 3)
    @Optional.Method(modid = "opencomputers")
    public Object[] writeln(Context context, Arguments args) {
        if(!ocMode) return new Object[] {false, "OC mode not enabled"};
        push(args.checkString(0));
        return new Object[] {true};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] readInput(Context context, Arguments args) {
        if(!ocMode) return new Object[] {"", "OC mode not enabled"};
        return new Object[] {history[0] != null ? history[0] : ""};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getAllHistory(Context context, Arguments args) {
        if(!ocMode) return new Object[] {null, "OC mode not enabled"};
        String[] copy = new String[history.length];
        for(int i = 0; i < history.length; i++) copy[i] = history[i] != null ? history[i] : "";
        return new Object[] {copy};
    }

    @Callback(direct = true, limit = 2)
    @Optional.Method(modid = "opencomputers")
    public Object[] clearScreen(Context context, Arguments args) {
        if(!ocMode) return new Object[] {false, "OC mode not enabled"};
        for(int i = 0; i < history.length; i++) history[i] = "";
        markDirty();
        return new Object[] {true};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[] {
            "enableOCMode", "isOCMode", "write", "writeln",
            "readInput", "getAllHistory", "clearScreen"
        };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        switch (method) {
            case "enableOCMode": return enableOCMode(context, args);
            case "isOCMode": return isOCMode(context, args);
            case "write": return write(context, args);
            case "writeln": return writeln(context, args);
            case "readInput": return readInput(context, args);
            case "getAllHistory": return getAllHistory(context, args);
            case "clearScreen": return clearScreen(context, args);
        }
        throw new NoSuchMethodException();
    }
}
