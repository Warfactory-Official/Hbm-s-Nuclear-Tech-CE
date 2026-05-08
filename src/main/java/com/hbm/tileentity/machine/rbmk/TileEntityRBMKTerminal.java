package com.hbm.tileentity.machine.rbmk;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.gui.GUIScreenRBMKTerminal;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityRBMKTerminal extends TileEntityLoadedBase implements ITickable, IGUIProvider, IControlReceiver {

    public final String[] history = new String[17];
    public String channel = "";
    public String repeatCmd = "";
    public boolean doesRepeat;

    public TileEntityRBMKTerminal() {
        for (int i = 0; i < this.history.length; i++) {
            this.history[i] = "";
        }
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (!this.channel.isEmpty() && !this.repeatCmd.isEmpty()) {
                RTTYSystem.broadcast(world, this.channel, this.repeatCmd);
            }
            this.networkPackNT(50);
        }
    }

    public void eval(String cmd) {
        if (cmd == null) return;

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
        for (int i = 0; i < this.history.length; i++) {
            this.history[i] = nbt.getString("history" + i);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("channel", this.channel);
        nbt.setString("repeatCmd", this.repeatCmd);
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
    public void receiveControl(NBTTagCompound data) {
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
}
