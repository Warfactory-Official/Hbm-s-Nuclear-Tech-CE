package com.hbm.saveddata.satellites;

import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.entity.missile.EntitySatellitePod;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemDrive.EnumDriveType;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.util.EnumUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Satellite {

	public static List<Class<? extends Satellite>> satellites = new ArrayList<Class<? extends Satellite>>();
	public static HashMap<Item, Class<? extends Satellite>> itemToClass = new HashMap<Item, Class<? extends Satellite>>();
	public static HashMap<Integer, Class<? extends Satellite>> metaToClass = new HashMap<Integer, Class<? extends Satellite>>();

	public static final String CHAN_SATLINK = "SAT_LINK";

	public static final String CMD_SETTARGET = "settarget";
	public static final String CMD_GETTARGET = "gettarget";
	public static final String CMD_GETTARGETX = "gettargetx";
	public static final String CMD_GETTARGETZ = "gettargetz";

	public int targetX;
	public int targetZ;

	public String tx = "";

	public static enum InterfaceActions {
		HAS_MAP,		//lets the interface display loaded chunks
		CAN_CLICK,		//enables onClick events
		SHOW_COORDS,	//enables coordinates as a mouse tooltip
		HAS_RADAR,		//lets the interface display loaded entities
		HAS_ORES		//like HAS_MAP but only shows ores
	}
	
	public static enum CoordActions {
		HAS_Y		//enables the Y-coord field which is disabled by default
	}
	
	public static enum Interfaces {
		NONE,		//does not interact with any sat interface (i.e. asteroid miners)
		SAT_PANEL,	//allows to interact with the sat interface panel (for graphical applications)
		SAT_COORD	//allows to interact with the sat coord remote (for teleportation or other coord related actions)
	}

	public List<InterfaceActions> ifaceAcs = new ArrayList<InterfaceActions>();
	public List<CoordActions> coordAcs = new ArrayList<CoordActions>();
	public Interfaces satIface = Interfaces.NONE;
	
	public static void register() {

		// the list index is the persisted satellite id, so this order must never change
		registerSatellite(SatelliteMapper.class, EnumSatType.SPY, ModItems.sat_mapper);
		registerSatellite(SatelliteScanner.class, EnumSatType.SCANNER, ModItems.sat_scanner);
		registerSatellite(SatelliteRadar.class, EnumSatType.RADAR, ModItems.sat_radar);
		registerSatellite(SatelliteLaser.class, EnumSatType.DEATH_RAY, ModItems.sat_laser);
		registerSatellite(SatelliteResonator.class, EnumSatType.XENIUM_RESONATOR, ModItems.sat_resonator);
		registerSatellite(SatelliteRelay.class, EnumSatType.RELAY, ModItems.sat_foeq);
		registerSatellite(SatelliteMiner.class, EnumSatType.MINER_ASTRO, ModItems.sat_miner);
		registerSatellite(SatelliteLunarMiner.class, EnumSatType.MINER_LUNAR, ModItems.sat_lunar_miner);
		registerSatellite(SatelliteHorizons.class, null, ModItems.sat_gerald);
		registerSatellite(SatelliteDetector.class, EnumSatType.DETECTOR, null);
		registerSatellite(SatellitePrecisionLaser.class, EnumSatType.PRECISION_LASER, null);
		registerSatellite(SatelliteRayScan.class, EnumSatType.RAY_SCAN, null);
		registerSatellite(SatelliteScience.class, EnumSatType.SCIENCE, null);
	}

	private static void registerSatellite(Class<? extends Satellite> sat, EnumSatType type, Item legacy) {

		satellites.add(sat);
		if(type != null) metaToClass.put(type.ordinal(), sat);
		if(legacy != null) itemToClass.put(legacy, sat);
	}

	public static Class<? extends Satellite> getClassFromStack(ItemStack stack) {
		if(stack.isEmpty()) return null;
		if(stack.getItem() == ModItems.satellite) return metaToClass.get(stack.getItemDamage());
		return itemToClass.get(stack.getItem());
	}
	
	public static void orbit(World world, int id, int freq, double x, double y, double z) {
		orbit(world, id, ItemStack.EMPTY, freq, x, y, z);
	}

	public static void orbit(World world, int id, ItemStack part, int freq, double x, double y, double z) {

		if(world.isRemote) return;

		SatelliteSavedData data = SatelliteSavedData.getData(world);
		Satellite existing = data.getSatFromFreq(freq);

		if(existing != null) {
			existing.onPartDelivered(world, part);
			data.markDirty();
			return;
		}

		Satellite sat = create(id);
		if(sat != null) {
			data.sats.put(freq, sat);
			sat.setTarget((int) Math.floor(x), (int) Math.floor(z));
			RTTYSystem.broadcast(world, CHAN_SATLINK, "Established connection to " + sat.getType() + " at " + sat.targetX + " / " + sat.targetZ);
			sat.onOrbit(world, x, y, z);
			data.markDirty();
		}
	}
	
	public static Satellite create(int id) {
		
		Satellite sat = null;
		
		try {
			Class<? extends Satellite> c = satellites.get(id);
			sat = c.newInstance();
		} catch(Exception ex) {
		}
		
		return sat;
	}
	
	public static int getIDFromStack(ItemStack stack) {
		return satellites.indexOf(getClassFromStack(stack));
	}
	
	public int getID() {
		return satellites.indexOf(this.getClass());
	}
	
	/** Data currently loaded into the satellite, consumed by the tape drive over a sat link */
	public EnumDriveType driveInput = null;
	public EnumDriveType driveOutput = null;

	/** Items waiting in orbit for a sat dock to call them down */
	public ItemStackHandler requestableSlots = new ItemStackHandler(0);

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("targetX", targetX);
		nbt.setInteger("targetZ", targetZ);
		nbt.setString("tx", tx);
		if(driveInput != null) nbt.setInteger("driveInput", driveInput.ordinal());
		if(driveOutput != null) nbt.setInteger("driveOutput", driveOutput.ordinal());

		nbt.setTag("requestableSlots", this.requestableSlots.serializeNBT());
	}

	public void readFromNBT(NBTTagCompound nbt) {
		this.targetX = nbt.getInteger("targetX");
		this.targetZ = nbt.getInteger("targetZ");
		this.tx = nbt.getString("tx");
		this.driveInput = nbt.hasKey("driveInput") ? EnumUtil.grabEnumSafely(EnumDriveType.VALUES, nbt.getInteger("driveInput")) : null;
		this.driveOutput = nbt.hasKey("driveOutput") ? EnumUtil.grabEnumSafely(EnumDriveType.VALUES, nbt.getInteger("driveOutput")) : null;

		this.requestableSlots = new ItemStackHandler(0);
		if(nbt.hasKey("requestableSlots")) this.requestableSlots.deserializeNBT(nbt.getCompoundTag("requestableSlots"));
	}

	/** Returns true if requestable items are available, and sends them to the specified location */
	public boolean tryRequestItems(World world, int x, int y, int z) {

		if(this.requestableSlots == null || this.requestableSlots.getSlots() <= 0) return false;

		EntitySatellitePod pod = new EntitySatellitePod(world).setup(y, requestableSlots);
		pod.setPosition(x + 0.5, 300, z + 0.5);

		if(!world.spawnEntity(pod)) return false;

		this.requestableSlots = new ItemStackHandler(0);
		this.markDirty();

		return true;
	}

	public void onCommand(World world, String... cmd) {
		onCommandTarget(world, cmd);
		onCommandImpl(world, cmd);
	}

	public void onCommandTarget(World world, String... cmd) {
		if(cmd.length <= 0) return;

		if(cmd[0].equals(CMD_SETTARGET)) {
			if(cmd.length == 3) {
				targetX = IRORInteractive.parseInt(cmd[1], Integer.MIN_VALUE, Integer.MAX_VALUE);
				targetZ = IRORInteractive.parseInt(cmd[2], Integer.MIN_VALUE, Integer.MAX_VALUE);
			}
			if(cmd.length == 4) {
				targetX = IRORInteractive.parseInt(cmd[1], Integer.MIN_VALUE, Integer.MAX_VALUE);
				targetZ = IRORInteractive.parseInt(cmd[3], Integer.MIN_VALUE, Integer.MAX_VALUE);
			}
			return;
		}

		if(cmd[0].equals(CMD_GETTARGET)) {
			this.tx = targetX + ";" + targetZ;
			return;
		}

		if(cmd[0].equals(CMD_GETTARGETX)) {
			this.tx = "" + targetX;
			return;
		}

		if(cmd[0].equals(CMD_GETTARGETZ)) {
			this.tx = "" + targetZ;
		}
	}

	public void onCommandImpl(World world, String... cmd) { }

	public void setTarget(int x, int z) {
		this.targetX = x;
		this.targetZ = z;
	}

	public void onUpdateTick(World world) { }

	/** For subsequent items sent under the same frequency as an existing satellite */
	public void onPartDelivered(World world, ItemStack part) { }

	public boolean isDirty = false;

	public void markDirty() {
		this.isDirty = true;
	}

	public String getType() {
		return this.getClass().getSimpleName();
	}

	public ITextComponent[] getInfo(World world) {
		return new ITextComponent[0];
	}

	/** The check for if there's data available, may also call produceData if a cooldown has elapsed */
	public boolean hasData(World world) {
		return this.driveInput != null && this.driveOutput != null;
	}

	public EnumDriveType getOutputData(EnumDriveType input) {
		if(input == this.driveInput) return this.driveOutput;
		return null;
	}

	public void produceData(EnumDriveType input, EnumDriveType output) {
		this.driveInput = input;
		this.driveOutput = output;
	}

	public void consumeData() {
		this.driveInput = null;
		this.driveOutput = null;
	}
	
	/**
	 * Called when the satellite reaches space, used to trigger achievements and other funny stuff.
	 * @param x posX of the rocket
	 * @param y ditto
	 * @param z ditto
	 */
	public void onOrbit(World world, double x, double y, double z) { }
	
	/**
	 * Called by the sat interface when clicking on the screen
	 *
	 * @param player
	 * @param x      the x-coordinate translated from the on-screen coords to actual world coordinates
	 * @param z      ditto
	 */
	public void onClick(World world, EntityPlayerMP player, int x, int z) { }
	
	/**
	 * Called by the coord sat interface
	 * @param x the specified x-coordinate
	 * @param y ditto
	 * @param z ditto
	 */
	public void onCoordAction(World world, EntityPlayerMP player, int x, int y, int z) { }

	public abstract float[] getColor();
}
