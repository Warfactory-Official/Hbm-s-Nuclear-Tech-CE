package com.hbm.tileentity.machine;

import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.api.redstoneoverradio.IRORValueProvider;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteRayScan;
import com.hbm.saveddata.satellites.SatelliteSavedData;
import com.hbm.tileentity.TileEntityTickingBase;
import com.hbm.tileentity.network.RTTYSystem;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister(name = "tileentity_satlink")
@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public class TileEntityMachineSatLink extends TileEntityTickingBase implements ITickable, IRORValueProvider, IRORInteractive, SimpleComponent, CompatHandler.OCComponent {

	public boolean connected;
	public int freq;

	public float rot = INACTIVE_ROT;
	public float prevRot = INACTIVE_ROT;
	public float lift = INACTIVE_LIFT;
	public float prevLift = INACTIVE_LIFT;

	public static final float SPEED = 0.25F;
	public static final float ACTIVE_ROT = -15F;
	public static final float ACTIVE_LIFT = -45F;
	public static final float INACTIVE_ROT = 0F;
	public static final float INACTIVE_LIFT = -85F;

	public ITextComponent[] info = new ITextComponent[0];

	@Override
	public String getInventoryName() {
		return "tile.machine_satlink.name";
	}

	@Override
	public void update() {

		if(!world.isRemote) {
			this.connected = false;

			if(world.getHeight(pos.getX(), pos.getZ()) <= pos.getY()) {
				SatelliteSavedData dat = SatelliteSavedData.getData(world);
				this.connected = dat.isFreqTaken(freq);
			}

			this.updateInfo(connected);
			this.networkPackNT(150);

		} else {

			this.prevRot = this.rot;
			this.prevLift = this.lift;

			float targetR = this.connected ? ACTIVE_ROT : INACTIVE_ROT;
			float targetL = this.connected ? ACTIVE_LIFT : INACTIVE_LIFT;

			if(Math.abs(rot - targetR) <= SPEED) rot = targetR;
			else if(rot < targetR) rot += SPEED;
			else if(rot > targetR) rot -= SPEED;

			if(Math.abs(lift - targetL) <= SPEED) lift = targetL;
			else if(lift < targetL) lift += SPEED;
			else if(lift > targetL) lift -= SPEED;
		}
	}

	protected void updateInfo(boolean canConnect) {

		if(!canConnect) {
			if(this.info.length > 0) this.info = new ITextComponent[0];
			return;
		}

		SatelliteSavedData dat = SatelliteSavedData.getData(world);
		Satellite sat = dat.getSatFromFreq(freq);

		if(sat != null) {
			this.info = sat.getInfo(world);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(connected);
		buf.writeInt(freq);
		buf.writeInt(info.length);
		for(ITextComponent comp : info) ByteBufUtils.writeUTF8String(buf, ITextComponent.Serializer.componentToJson(comp));
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.connected = buf.readBoolean();
		this.freq = buf.readInt();
		this.info = new ITextComponent[buf.readInt()];
		for(int i = 0; i < info.length; i++) {
			ITextComponent comp = ITextComponent.Serializer.jsonToComponent(ByteBufUtils.readUTF8String(buf));
			if(comp != null) this.info[i] = comp;
			else this.info[i] = new TextComponentString("");
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.freq = nbt.getInteger("freq");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("freq", freq);
		return super.writeToNBT(nbt);
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
				PREFIX_VALUE + "connected",
				PREFIX_VALUE + "freq",
				PREFIX_VALUE + "rx",
				PREFIX_VALUE + "type",
				PREFIX_FUNCTION + "setfreq" + NAME_SEPARATOR + "freq",
				PREFIX_FUNCTION + "tx" + NAME_SEPARATOR + "payload",
				PREFIX_FUNCTION + "txrx" + NAME_SEPARATOR + "return freq" + PARAM_SEPARATOR + "payload"
		};
	}

	@Override
	public String provideRORValue(String name) {

		if(name.equals(PREFIX_VALUE + "connected")) {
			return this.connected ? "TRUE" : "FALSE";
		}

		if(name.equals(PREFIX_VALUE + "freq")) {
			return "" + this.freq;
		}

		if(name.equals(PREFIX_VALUE + "type")) {
			SatelliteSavedData dat = SatelliteSavedData.getData(world);
			Satellite sat = dat.getSatFromFreq(this.freq);
			return sat != null ? sat.getType() : "";
		}

		if(name.equals(PREFIX_VALUE + "rx")) {
			SatelliteSavedData dat = SatelliteSavedData.getData(world);
			Satellite sat = dat.getSatFromFreq(this.freq);
			return sat != null ? sat.tx : "";
		}

		return null;
	}

	@Override
	public String runRORFunction(String name, String[] params) {

		if(name.equals(PREFIX_FUNCTION + "setfreq") && params.length == 1) {
			this.freq = IRORInteractive.parseInt(params[0], 0, 100_000);
			this.markChanged();
		}

		if(name.equals(PREFIX_FUNCTION + "tx") && params.length > 0) {
			SatelliteSavedData dat = SatelliteSavedData.getData(world);
			Satellite sat = dat.getSatFromFreq(this.freq);
			String[] cmd = String.join(IRORInteractive.PARAM_SEPARATOR, params).split(" ");
			if(sat != null) {
				sat.onCommand(world, cmd);
				dat.markDirty();
			}
			SatelliteRayScan.reportEvent(world, pos.getX(), pos.getY(), pos.getZ(), SatelliteRayScan.RayEvent.INFO_RADIO, 300);
			this.markChanged();
		}

		if(name.equals(PREFIX_FUNCTION + "txrx") && params.length > 1) {
			SatelliteSavedData dat = SatelliteSavedData.getData(world);
			Satellite sat = dat.getSatFromFreq(this.freq);

			String[] args = new String[params.length - 1];
			System.arraycopy(params, 1, args, 0, args.length);
			String[] cmd = String.join(IRORInteractive.PARAM_SEPARATOR, args).split(" ");

			if(sat != null) {
				sat.onCommand(world, cmd);
				RTTYSystem.broadcast(world, params[0], sat.tx);
				dat.markDirty();
			}
			SatelliteRayScan.reportEvent(world, pos.getX(), pos.getY(), pos.getZ(), SatelliteRayScan.RayEvent.INFO_RADIO, 300);
			this.markChanged();
		}

		return null;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos.getX() - 2, pos.getY(), pos.getZ() - 2, pos.getX() + 3, pos.getY() + 4, pos.getZ() + 3);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "ntm_satlink";
    }

    @Callback(direct = true, doc = "function():boolean -- Returns connection state")
    @Optional.Method(modid = "opencomputers")
    public Object[] isConnected(Context context, Arguments args) {
        return new Object[] { connected };
    }

    @Callback(direct = true, limit = 4, doc = "function(freq: number) -- Sets satellite frequency")
    @Optional.Method(modid = "opencomputers")
    public Object[] setFreq(Context context, Arguments args) {
        freq = args.checkInteger(0);
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function():number -- Gets satellite frequency")
    @Optional.Method(modid = "opencomputers")
    public Object[] getFreq(Context context, Arguments args) {
        return new Object[] { freq };
    }

    @Callback(direct = true, doc = "function():string -- Gets satellite type")
    @Optional.Method(modid = "opencomputers")
    public Object[] getType(Context context, Arguments args) {
        return new Object[] { provideRORValue(PREFIX_VALUE + "type") };
    }

    @Callback(direct = true, limit = 4, doc = "function(command: string) -- Transmits a command to the satellite")
    @Optional.Method(modid = "opencomputers")
    public Object[] send(Context context, Arguments args) {
        runRORFunction(PREFIX_FUNCTION + "tx", new String[]{args.checkString(0)});
        return new Object[] {};
    }

    @Callback(direct = true, limit = 4, doc = "function():string -- Gets received command from the satellite")
    @Optional.Method(modid = "opencomputers")
    public Object[] read(Context context, Arguments args) {
        return new Object[] { provideRORValue(PREFIX_VALUE + "rx") };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[] {
            "isConnected",
            "setFreq",
            "getFreq",
            "getType",
            "send",
            "read"
        };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        switch (method) {
            case "isConnected": return isConnected(context, args);
            case "setFreq": return setFreq(context, args);
            case "getFreq": return getFreq(context, args);
            case "getType": return getType(context, args);
            case "send": return send(context, args);
            case "read": return read(context, args);
        }
        throw new NoSuchMethodException();
    }
}
