package com.hbm.tileentity.machine.rbmk;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.blocks.machine.rbmk.RBMKRod;
import com.hbm.config.MobConfig;
import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.handler.CompatHandler;
import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.neutron.NeutronStream;
import com.hbm.handler.neutron.RBMKNeutronHandler;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerRBMKRod;
import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.gui.GUIRBMKRod;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemRBMKRod;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.rbmk.RBMKColumn.ColumnType;
import com.hbm.util.BufferUtil;
import com.hbm.util.ParticleUtil;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityRBMKRod extends TileEntityRBMKSlottedBase implements IRBMKFluxReceiver, IRBMKLoadable, IGUIProvider, SimpleComponent, CompatHandler.OCComponent {

	// New system!!
	// Used for receiving flux (calculating outbound flux/burning rods)
	public double fluxFastRatio;
	public double fluxQuantity;
	public double lastFluxQuantity;
	public double lastFluxRatio;

	public boolean hasRod;

	// Fuel rod item data client sync
	private String fuelYield;
	private String fuelXenon;
	private String fuelHeat;

    private static final Vec3d[] NEUTRON_VECTORS = {
            new Vec3d(0, 0, -1), // NORTH
            new Vec3d(1, 0, 0),  // EAST
            new Vec3d(0, 0, 1),  // SOUTH
            new Vec3d(-1, 0, 0)  // WEST
    };

    private int cherenkovVisualTimer = 0;

	@SideOnly(Side.CLIENT)
	public float fuelR;
	@SideOnly(Side.CLIENT)
	public float fuelG;
	@SideOnly(Side.CLIENT)
	public float fuelB;
	@SideOnly(Side.CLIENT)
	public float cherenkovR;
	@SideOnly(Side.CLIENT)
	public float cherenkovG;
	@SideOnly(Side.CLIENT)
	public float cherenkovB;

	public TileEntityRBMKRod() {
		super(1);
	}

	@Override
	public String getName() {
		return "container.rbmkRod";
	}
	
	@Override
	public boolean isModerated() {
		return ((RBMKRod)this.getBlockType()).moderated;
	}

	@Override
	public void receiveFlux(NeutronStream stream) {
		double fastFlux = this.fluxQuantity * this.fluxFastRatio;
		double fastFluxIn = stream.fluxQuantity * stream.fluxRatio;

		this.fluxQuantity += stream.fluxQuantity;
		fluxFastRatio = (fastFlux + fastFluxIn) / fluxQuantity;
	}
	
	@Override
	public void update() {

		if(!world.isRemote) {
			ItemStack stack = inventory.getStackInSlot(0).copy();
			if(stack.getItem() instanceof ItemRBMKRod rod) {
				double fluxRatioOut;
				double fluxQuantityOut;
				// Experimental flux ratio curve rods!
				// Again, nothing really uses this so its just idle code at the moment.
				if(rod.specialFluxCurve) {

					fluxRatioOut = rod.fluxRatioOut(this.fluxFastRatio, ItemRBMKRod.getEnrichment(stack));

					double fluxIn;

					fluxIn = rod.fluxFromRatio(this.fluxQuantity, this.fluxFastRatio);

					fluxQuantityOut = rod.burn(world, stack, fluxIn);
				} else {
					NType rType = rod.rType;
					if(rType == NType.SLOW)
						fluxRatioOut = 0;
					else
						fluxRatioOut = 1;

					double fluxIn = fluxFromType(rod.nType);
					fluxQuantityOut = rod.burn(world, stack, fluxIn);
				}

				rod.updateHeat(world, stack, 1.0D);
				this.heat += rod.provideHeat(world, stack, heat, 1.0D);
				inventory.setStackInSlot(0, stack);

                if (this.fluxQuantity > 0.01) {
                    this.cherenkovVisualTimer = 10;
                }
				
				if(!this.hasLid()) {
                    ChunkRadiationManager.proxy.incrementRad(world, pos, (float) (fluxQuantity * 0.05F), (float) (fluxQuantity * 10F));
                }
				
				super.update();

				if(this.heat > this.maxHeat()) {
					if (RBMKDials.getMeltdownsDisabled(world))
						ParticleUtil.spawnGasFlame(world, pos.getX() + 0.5, pos.getY() +RBMKDials.getColumnHeight(world) + 0.5, pos.getZ() + 0.5, 0, 0.2, 0);
					else this.meltdown();
					this.lastFluxRatio = 0;
					this.lastFluxQuantity = 0;
					this.fluxQuantity = 0;
					return;
				}
				if(this.heat > 10_000) this.heat = 10_000;
				this.lastFluxQuantity = this.fluxQuantity;
				this.lastFluxRatio = this.fluxFastRatio;

				this.fluxQuantity = 0;
				this.fluxFastRatio = 0;

				spreadFlux(fluxQuantityOut, fluxRatioOut);
				
				hasRod = true;
			} else {
				this.lastFluxRatio = 0;
				this.lastFluxQuantity = 0;
				this.fluxQuantity = 0;
				this.fluxFastRatio = 0;

				hasRod = false;

				super.update();
			}

            if(cherenkovVisualTimer > 0) {
                cherenkovVisualTimer--;
            }
		}
	}

//	@Override
//	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
//		if(itemStack.getItem() instanceof ItemRBMKRod) return !(ItemRBMKRod.getMeltdownPercent(itemStack) > 0);
//		return true;
//	}
	
	/**
	 * SLOW: full efficiency for slow neutrons, fast neutrons have half efficiency
	 * FAST: fast neutrons have 100% efficiency, slow only 30%
	 * ANY: just add together whatever we have because who cares
	 * @param type
	 * @return
	 */
	
	private double fluxFromType(NType type) {

		double fastFlux = this.fluxQuantity * this.fluxFastRatio;
		double slowFlux = this.fluxQuantity * (1 - this.fluxFastRatio);
        return switch (type) {
            case SLOW -> slowFlux + fastFlux * 0.5;
            case FAST -> fastFlux + slowFlux * 0.3;
            case ANY -> this.fluxQuantity;
        };
    }
	
	public static final ForgeDirection[] fluxDirs = new ForgeDirection[] {
			ForgeDirection.NORTH,
			ForgeDirection.EAST,
			ForgeDirection.SOUTH,
			ForgeDirection.WEST
	};
	
	protected static NType stream;

    private BlockPos posFlux;

    protected void spreadFlux(double flux, double ratio) {
        if(flux <= 0) {
            NeutronNodeWorld.removeNode(world, pos);
            return;
        }

        NeutronNodeWorld.StreamWorld streamWorld = NeutronNodeWorld.getOrAddWorld(world);
        RBMKNeutronHandler.RBMKNeutronNode node = (RBMKNeutronHandler.RBMKNeutronNode) streamWorld.getNode(pos);

        if(node == null) {
            node = RBMKNeutronHandler.makeNode(streamWorld, this);
            streamWorld.addNode(node);
        }

        for (Vec3d neutronVector : NEUTRON_VECTORS) {
            new RBMKNeutronHandler.RBMKNeutronStream(node, neutronVector, flux, ratio);
        }
    }
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.fluxQuantity = nbt.getDouble("fluxQuantity");
		this.fluxFastRatio = nbt.getDouble("fluxMod");
		this.hasRod = nbt.getBoolean("hasRod");
        this.cherenkovVisualTimer = nbt.getInteger("cherenkovTimer");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(!diag) {
			nbt.setDouble("fluxQuantity", this.lastFluxQuantity);
			nbt.setDouble("fluxMod", this.lastFluxRatio);
		} else {
			nbt.setDouble("fluxSlow", this.fluxQuantity * (1 - fluxFastRatio));
			nbt.setDouble("fluxFast", this.fluxQuantity * fluxFastRatio);
		}
        nbt.setInteger("cherenkovTimer", this.cherenkovVisualTimer);
		return nbt;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeDouble(this.lastFluxQuantity);
		buf.writeDouble(this.lastFluxRatio);
		buf.writeBoolean(this.hasRod);
        buf.writeInt(this.cherenkovVisualTimer);
		ItemStack stack = this.inventory.getStackInSlot(0);
		if(this.hasRod) {
			ItemRBMKRod rod = ((ItemRBMKRod)stack.getItem());
			BufferUtil.writeString(buf, ItemRBMKRod.getYield(stack) + " / " + rod.yield + " (" + (ItemRBMKRod.getEnrichment(stack) * 100) + "%)");
			BufferUtil.writeString(buf, ItemRBMKRod.getPoison(stack) + "%");
			BufferUtil.writeString(buf, ItemRBMKRod.getCoreHeat(stack) + " / " + ItemRBMKRod.getHullHeat(stack)  + " / " + rod.meltingPoint);
			// For client color sync
			BufferUtil.writeString(buf, rod.getRegistryName().getPath());
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.fluxQuantity = buf.readDouble();
		this.fluxFastRatio = buf.readDouble();
		this.hasRod = buf.readBoolean();
        this.cherenkovVisualTimer = buf.readInt();
		if(this.hasRod) {
			fuelYield = BufferUtil.readString(buf);
			fuelXenon = BufferUtil.readString(buf);
			fuelHeat = BufferUtil.readString(buf);
			ItemRBMKRod rod = (ItemRBMKRod) Item.getByNameOrId("hbm:" + BufferUtil.readString(buf));
			this.fuelR = rod.fuelR;
			this.fuelG = rod.fuelG;
			this.fuelB = rod.fuelB;
			this.cherenkovR = rod.cherenkovR;
			this.cherenkovG = rod.cherenkovG;
			this.cherenkovB = rod.cherenkovB;
		} else {
			fuelYield = fuelXenon = fuelHeat = null;
		}
	}

	public void getDiagData(NBTTagCompound nbt) {
		diag = true;
		this.writeToNBT(nbt);
		diag = false;

		if(fuelYield != null && fuelXenon != null && fuelHeat != null) {
			nbt.setString("f_yield", fuelYield);
			nbt.setString("f_xenon", fuelXenon);
			nbt.setString("f_heat", fuelHeat);
		}
	}
	
	@Override
	public void onMelt(int reduce) {
		int h = RBMKDials.getColumnHeight(world);
		reduce = MathHelper.clamp(reduce, 1, h);
		
		if(world.rand.nextInt(3) == 0)
			reduce++;
		
		boolean corium = inventory.getStackInSlot(0).getItem() instanceof ItemRBMKRod;
		
		if(corium && inventory.getStackInSlot(0).getItem() == ModItems.rbmk_fuel_drx) 
			RBMKBase.digamma = true;
		
		inventory.setStackInSlot(0, ItemStack.EMPTY);

		if(corium) {
			
			for(int i = h; i >= 0; i--) {
				
				if(i <= h + 1 - reduce) {
					world.setBlockState(new BlockPos(pos.getX(), pos.getY() + i, pos.getZ()), ModBlocks.corium_block.getDefaultState());
				} else {
					world.setBlockState(new BlockPos(pos.getX(), pos.getY() + i, pos.getZ()), Blocks.AIR.getDefaultState());
				}
				IBlockState state = world.getBlockState(pos.up(i));
				world.notifyBlockUpdate(pos.up(i), state, state, 3);
			}
			
			int count = 1 + world.rand.nextInt(RBMKDials.getColumnHeight(world));
			
			for(int i = 0; i < count; i++) {
				spawnDebris(DebrisType.FUEL);
			}
		} else {
			this.standardMelt(reduce);
		}
		
		spawnDebris(DebrisType.ELEMENT);
		
		if(this.getBlockMetadata() == RBMKBase.DIR_NORMAL_LID.ordinal() + RBMKBase.offset)
			spawnDebris(DebrisType.LID);

		if(MobConfig.enableElementals) {
			List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).grow(100, 100, 100));

			for(EntityPlayer player : players) {
				player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).setBoolean("radMark", true);
			}
		}
	}

	@Override
	public RBMKNeutronHandler.RBMKType getRBMKType() {
		return RBMKNeutronHandler.RBMKType.ROD;
	}

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.FUEL;
	}

	@Override
	public RBMKColumn getConsoleData() {
		RBMKColumn.FuelColumn data = (RBMKColumn.FuelColumn) super.getConsoleData();
		if(inventory.getStackInSlot(0).getItem() instanceof ItemRBMKRod rod) {
            data.enrichment = ItemRBMKRod.getEnrichment(inventory.getStackInSlot(0));
			data.xenon = ItemRBMKRod.getPoison(inventory.getStackInSlot(0));
			data.c_heat = ItemRBMKRod.getHullHeat(inventory.getStackInSlot(0));
			data.c_coreHeat = ItemRBMKRod.getCoreHeat(inventory.getStackInSlot(0));
			data.c_maxHeat = rod.meltingPoint;
		}
		return data;
	}

	@Override
	public boolean canLoad(ItemStack toLoad) {
		return toLoad != null && inventory.getStackInSlot(0).isEmpty();
	}

	@Override
	public void load(ItemStack toLoad) {
		inventory.setStackInSlot(0, toLoad.copy());
		this.markDirty();
	}

	@Override
	public boolean canUnload() {
		return !inventory.getStackInSlot(0).isEmpty();
	}

	@Override
	public ItemStack provideNext() {
		return inventory.getStackInSlot(0);
	}

	@Override
	public void unload() {
		inventory.setStackInSlot(0, ItemStack.EMPTY);
		this.markDirty();
	}

	// control panel

	@Override
	public Map<String, DataValue> getQueryData() {
		Map<String, DataValue> data = super.getQueryData();

		if (inventory.getStackInSlot(0).getItem() instanceof ItemRBMKRod) {
			ItemRBMKRod rod = ((ItemRBMKRod)inventory.getStackInSlot(0).getItem());
			data.put("enrichment", new DataValueFloat((float) ItemRBMKRod.getEnrichment(inventory.getStackInSlot(0))));
			data.put("xenon", new DataValueFloat((float) ItemRBMKRod.getPoison(inventory.getStackInSlot(0))));
			data.put("c_heat", new DataValueFloat((float) ItemRBMKRod.getHullHeat(inventory.getStackInSlot(0))));
			data.put("c_coreHeat", new DataValueFloat((float) ItemRBMKRod.getCoreHeat(inventory.getStackInSlot(0))));
			data.put("c_maxHeat", new DataValueFloat((float) rod.meltingPoint));
		}
		return data;
	}

	@Override
	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "rbmk_fuel_rod";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getHeat(Context context, Arguments args) {
		return new Object[] {heat};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getFluxQuantity(Context context, Arguments args) {
		return new Object[] {lastFluxQuantity};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getFluxRatio(Context context, Arguments args) {
		return new Object[] {fluxFastRatio};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getDepletion(Context context, Arguments args) {
		if(!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() instanceof ItemRBMKRod) {
			return new Object[] {ItemRBMKRod.getEnrichment(inventory.getStackInSlot(0))};
		}
		return new Object[] {"N/A"};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getXenonPoison(Context context, Arguments args) {
		if(!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() instanceof ItemRBMKRod) {
			return new Object[] {ItemRBMKRod.getPoison(inventory.getStackInSlot(0))};
		}
		return new Object[] {"N/A"};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getCoreHeat(Context context, Arguments args) {
		if(!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() instanceof ItemRBMKRod) {
			return new Object[] {ItemRBMKRod.getCoreHeat(inventory.getStackInSlot(0))};
		}
		return new Object[] {"N/A"};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getSkinHeat(Context context, Arguments args) {
		if(!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() instanceof ItemRBMKRod) {
			return new Object[] {ItemRBMKRod.getHullHeat(inventory.getStackInSlot(0))};
		}
		return new Object[] {"N/A"};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getType(Context context, Arguments args) {
		if(!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() instanceof ItemRBMKRod) {
			return new Object[] {inventory.getStackInSlot(0).getItem().getTranslationKey()};
		}
		return new Object[] {"N/A"};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getInfo(Context context, Arguments args) {
		List<Object> returnValues = new ArrayList<>();
		if(!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() instanceof ItemRBMKRod) {
			returnValues.add(ItemRBMKRod.getHullHeat(inventory.getStackInSlot(0)));
			returnValues.add(ItemRBMKRod.getCoreHeat(inventory.getStackInSlot(0)));
			returnValues.add(ItemRBMKRod.getEnrichment(inventory.getStackInSlot(0)));
			returnValues.add(ItemRBMKRod.getPoison(inventory.getStackInSlot(0)));
			returnValues.add(inventory.getStackInSlot(0).getItem().getTranslationKey());
		} else {
			for(int i = 0; i < 5; i++) returnValues.add("N/A");
		}

		return new Object[] {
				heat, returnValues.get(0), returnValues.get(1),
				fluxQuantity, fluxFastRatio, returnValues.get(2), returnValues.get(3), returnValues.get(4),
				((RBMKRod)this.getBlockType()).moderated, pos.getX(), pos.getY(), pos.getZ()
		};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getModerated(Context context, Arguments args) {
		return new Object[] {((RBMKRod)this.getBlockType()).moderated};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getCoordinates(Context context, Arguments args) {
		return new Object[] {pos.getX(), pos.getY(), pos.getZ()};
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerRBMKRod(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIRBMKRod(player.inventory, this);
	}
}
