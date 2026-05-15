package com.hbm.tileentity.bomb;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.entity.missile.EntityMissileCustom;
import com.hbm.handler.MissileStruct;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IBomb;
import com.hbm.inventory.container.ContainerCompactLauncher;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUICompactLauncher;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.items.weapon.ItemMissile;
import com.hbm.items.weapon.ItemMissile.FuelType;
import com.hbm.items.weapon.ItemMissile.PartSize;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.TEMissileMultipartPacket;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityCompactLauncher extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiver, SimpleComponent, IGUIProvider, IConnectionAnchors {

	private AxisAlignedBB bb;
	public long power;
	public static final long maxPower = 100000;
	public int solid;
	public static final int maxSolid = 25000;
	public FluidTankNTM[] tanks;
    public MissileStruct load;
    public static final int clearingDuraction = 100;
	public int clearingTimer = 0;

	public TileEntityCompactLauncher() {
		super(8, true, true);
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.NONE, 25000).withOwner(this);
		tanks[1] = new FluidTankNTM(Fluids.NONE,25000).withOwner(this);
    }

	@Override
	public String getDefaultName() {
		return "container.compactLauncher";
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
		}
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	public int getSolidScaled(int i) {
		return (solid * i) / maxSolid;
	}

	@Override
	public void update() {
		updateTypes();
		if(!world.isRemote) {

			if(clearingTimer > 0) clearingTimer--;
			if(tanks[0].loadTank(2, 6, inventory)) ;
			if(tanks[1].loadTank(3, 7, inventory)) ;
			power = Library.chargeTEFromItems(inventory, 5, power, maxPower);

			if(inventory.getStackInSlot(4).getItem() == ModItems.rocket_fuel && solid + 250 <= maxSolid) {
				if (inventory.getStackInSlot(4).getCount() <= 1) {
					inventory.setStackInSlot(4, ItemStack.EMPTY);
				}
				inventory.getStackInSlot(4).splitStack(1);
				if (inventory.getStackInSlot(4).isEmpty()) {
					inventory.setStackInSlot(4, ItemStack.EMPTY);
				}
				solid += 250;
			}
			if(world.getTotalWorldTime() % 20 == 0)
				this.updateConnections();

			networkPackNT(20);
			MissileStruct multipart = getStruct(inventory.getStackInSlot(0));

			if(multipart != null)
				PacketDispatcher.wrapper.sendToAllAround(new TEMissileMultipartPacket(pos, multipart), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 200));
			else
				PacketDispatcher.wrapper.sendToAllAround(new TEMissileMultipartPacket(pos, new MissileStruct()), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 200));
			if(canLaunch()) {
				MutableBlockPos mPos = new BlockPos.MutableBlockPos();
				outer: 
				for(int x = -1; x <= 1; x++) {
					for(int z = -1; z <= 1; z++) {

						if(world.isBlockPowered(mPos.setPos(pos.getX() + x, pos.getY(), pos.getZ() + z))) {
							launch();
							break outer;
						}
					}
				}
			}
		} else {
			List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 10, pos.getZ() + 1.5));
			for(Entity e : entities) {
				if(e instanceof EntityMissileCustom) {
					for(int i = 0; i < 15; i++) MainRegistry.proxy.effectNT(HbmEffectNT.LaunchSmoke, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
					break;
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(solid);
		buf.writeInt(clearingTimer);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.solid = buf.readInt();
		this.clearingTimer = buf.readInt();
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
	}

	private static final int[][] CONN_OFFSETS = {{2,0,1},{2,0,-1},{-2,0,1},{-2,0,-1},{1,0,2},{-1,0,2},{1,0,-2},{-1,0,-2},{1,-1,1},{1,-1,-1},{-1,-1,1},{-1,-1,-1}};
	private static final ForgeDirection[] CONN_DIRS = {ForgeDirection.EAST,ForgeDirection.EAST,ForgeDirection.WEST,ForgeDirection.WEST,ForgeDirection.NORTH,ForgeDirection.NORTH,ForgeDirection.SOUTH,ForgeDirection.SOUTH,ForgeDirection.DOWN,ForgeDirection.DOWN,ForgeDirection.DOWN,ForgeDirection.DOWN};

	@Override
	public DirPos[] getConPos() {
		DirPos[] result = new DirPos[CONN_OFFSETS.length];
		for (int i = 0; i < CONN_OFFSETS.length; i++) {
			result[i] = new DirPos(pos.getX() + CONN_OFFSETS[i][0], pos.getY() + CONN_OFFSETS[i][1], pos.getZ() + CONN_OFFSETS[i][2], CONN_DIRS[i]);
		}
		return result;
	}

	private void updateConnections() {
		for (DirPos p : getConPos()) {
			this.trySubscribe(world, p.getPos().getX(), p.getPos().getY(), p.getPos().getZ(), p.getDir());
			this.trySubscribe(tanks[0].getTankType(), world, p.getPos().getX(), p.getPos().getY(), p.getPos().getZ(), p.getDir());
			this.trySubscribe(tanks[1].getTankType(), world, p.getPos().getX(), p.getPos().getY(), p.getPos().getZ(), p.getDir());
		}
	}

	public boolean canLaunch() {
        return power >= maxPower * 0.75 && isMissileValid() && hasDesignator() && hasFuel() && clearingTimer == 0;
    }

	public void launch() {

		world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.missileTakeoff, SoundCategory.BLOCKS, 10.0F, 1.0F);

		int tX = inventory.getStackInSlot(1).getTagCompound().getInteger("xCoord");
		int tZ = inventory.getStackInSlot(1).getTagCompound().getInteger("zCoord");

		ItemMissile chip = (ItemMissile) Item.getItemById(ItemCustomMissile.readFromNBT(inventory.getStackInSlot(0), "chip"));
		float c = (Float) chip.attributes[0];
		float f = 1.0F;

		if(getStruct(inventory.getStackInSlot(0)).fins != null) {
			ItemMissile fins = (ItemMissile) Item.getItemById(ItemCustomMissile.readFromNBT(inventory.getStackInSlot(0), "stability"));
			f = (Float) fins.attributes[0];
		}

		Vec3 target = Vec3.createVectorHelper(pos.getX() - tX, 0, pos.getZ() - tZ);
		target.xCoord *= c * f;
		target.zCoord *= c * f;

		target.rotateAroundY(world.rand.nextFloat() * 360);

		EntityMissileCustom missile = new EntityMissileCustom(world, pos.getX() + 0.5F, pos.getY() + 1.5F, pos.getZ() + 0.5F, tX + (int) target.xCoord, tZ + (int) target.zCoord, getStruct(inventory.getStackInSlot(0)));
		world.spawnEntity(missile);

		subtractFuel();
		clearingTimer = clearingDuraction;
		inventory.setStackInSlot(0, ItemStack.EMPTY);
	}

	private boolean hasFuel() {

		return solidState() != 0 && liquidState() != 0 && oxidizerState() != 0;
	}

	private void subtractFuel() {

		MissileStruct multipart = getStruct(inventory.getStackInSlot(0));

		if(multipart == null || multipart.fuselage == null)
			return;

		ItemMissile fuselage = (ItemMissile) multipart.fuselage;

		float f = (Float) fuselage.attributes[1];
		int fuel = (int) f;

		switch((FuelType) fuselage.attributes[0]) {
		case KEROSENE:
			tanks[0].drain(fuel, true);
			tanks[1].drain(fuel, true);
			break;
		case HYDROGEN:
			tanks[0].drain(fuel, true);
			tanks[1].drain(fuel, true);
			break;
		case XENON:
			tanks[0].drain(fuel, true);
			break;
		case BALEFIRE:
			tanks[0].drain(fuel, true);
			tanks[1].drain(fuel, true);
			break;
		case SOLID:
			this.solid -= fuel;
			break;
		default:
			break;
		}
        this.power -= maxPower * 0.75;
	}

	public static MissileStruct getStruct(ItemStack stack) {

		return ItemCustomMissile.getStruct(stack);
	}

	public boolean isMissileValid() {

		MissileStruct multipart = getStruct(inventory.getStackInSlot(0));

		if(multipart == null || multipart.fuselage == null)
			return false;

		ItemMissile fuselage = (ItemMissile) multipart.fuselage;

		return fuselage.top == PartSize.SIZE_10;
	}

	public boolean hasDesignator() {

		if(!inventory.getStackInSlot(1).isEmpty()) {

			return (inventory.getStackInSlot(1).getItem() == ModItems.designator || inventory.getStackInSlot(1).getItem() == ModItems.designator_range || inventory.getStackInSlot(1).getItem() == ModItems.designator_manual) && inventory.getStackInSlot(1).hasTagCompound();
		}

		return false;
	}

	public int solidState() {

		MissileStruct multipart = getStruct(inventory.getStackInSlot(0));

		if(multipart == null || multipart.fuselage == null)
			return -1;

		ItemMissile fuselage = (ItemMissile) multipart.fuselage;

		if((FuelType) fuselage.attributes[0] == FuelType.SOLID) {

			if(solid >= (Float) fuselage.attributes[1])
				return 1;
			else
				return 0;
		}

		return -1;
	}

	public int liquidState() {

		MissileStruct multipart = getStruct(inventory.getStackInSlot(0));

		if(multipart == null || multipart.fuselage == null)
			return -1;

		ItemMissile fuselage = (ItemMissile) multipart.fuselage;

		switch((FuelType) fuselage.attributes[0]) {
		case KEROSENE:
		case HYDROGEN:
		case XENON:
		case BALEFIRE:

			if(tanks[0].getFluidAmount() >= (Float) fuselage.attributes[1])
				return 1;
			else
				return 0;
		default:
			break;
		}

		return -1;
	}

	public int oxidizerState() {

		MissileStruct multipart = getStruct(inventory.getStackInSlot(0));

		if(multipart == null || multipart.fuselage == null)
			return -1;

		ItemMissile fuselage = (ItemMissile) multipart.fuselage;

		switch((FuelType) fuselage.attributes[0]) {
		case KEROSENE:
		case HYDROGEN:
		case BALEFIRE:

			if(tanks[1].getFluidAmount() >= (Float) fuselage.attributes[1])
				return 1;
			else
				return 0;
		default:
			break;
		}

		return -1;
	}

	public void updateTypes() {

		MissileStruct multipart = getStruct(inventory.getStackInSlot(0));

		if(multipart == null || multipart.fuselage == null)
			return;

		ItemMissile fuselage = multipart.fuselage;

		switch((FuelType) fuselage.attributes[0]) {
		case KEROSENE:
			tanks[0].setTankType(Fluids.KEROSENE);
			tanks[1].setTankType(Fluids.PEROXIDE);
			break;
		case HYDROGEN:
			tanks[0].setTankType(Fluids.HYDROGEN);
			tanks[1].setTankType(Fluids.OXYGEN);
			break;
		case XENON:
			tanks[0].setTankType(Fluids.XENON);
			break;
		case BALEFIRE:
			tanks[0].setTankType(Fluids.BALEFIRE);
			tanks[1].setTankType(Fluids.PEROXIDE);
			break;
		default:
			break;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		tanks[0].readFromNBT(compound, "tank0");
		tanks[0].readFromNBT(compound, "tank1");
		solid = compound.getInteger("solidfuel");
		power = compound.getLong("power");
	}

	@NotNull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		tanks[0].writeToNBT(compound, "tank0");
		tanks[1].writeToNBT(compound, "tank1");
		compound.setInteger("solidfuel", solid);
		compound.setLong("power", power);
		return compound;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (bb == null) bb = new AxisAlignedBB(pos.getX() - 2, pos.getY(), pos.getZ() - 2, pos.getX() + 3, pos.getY() + 7, pos.getZ() + 3);
		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public long getMaxPower() {
		return TileEntityCompactLauncher.maxPower;
	}

	public boolean setCoords(int x, int z){
		if(!inventory.getStackInSlot(1).isEmpty() && (inventory.getStackInSlot(1).getItem() == ModItems.designator || inventory.getStackInSlot(1).getItem() == ModItems.designator_range || inventory.getStackInSlot(1).getItem() == ModItems.designator_manual)){
			NBTTagCompound nbt;
			if(inventory.getStackInSlot(1).hasTagCompound())
				nbt = inventory.getStackInSlot(1).getTagCompound();
			else
				nbt = new NBTTagCompound();
			nbt.setInteger("xCoord", x);
			nbt.setInteger("zCoord", z);
			inventory.getStackInSlot(1).setTagCompound(nbt);
			return true;
		}
		return false;
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return null;
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[]{tanks[0], tanks[1]};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[]{tanks[0], tanks[1]};
	}

	// opencomputers interface

	@Override
	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "launchpad_compact";
	}

	@Callback(doc = "setTarget(x:int, z:int); saves coords in target designator item - returns true if it worked")
	@Optional.Method(modid = "opencomputers")
	public Object[] setTarget(Context context, Arguments args) {
		int x = args.checkInteger(0);
		int z = args.checkInteger(1);
		
		return new Object[] {setCoords(x, z)};
	}

	@Callback(doc = "launch(); tries to launch the rocket")
	@Optional.Method(modid = "opencomputers")
	public Object[] launch(Context context, Arguments args) {
		Block b = world.getBlockState(pos).getBlock();
		if(b instanceof IBomb){
			((IBomb)b).explode(world, pos, null);
		}
		return new Object[] {null};
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCompactLauncher(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICompactLauncher(player.inventory, this);
	}
}
