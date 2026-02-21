package com.hbm.tileentity.network.energy;

import com.hbm.api.energymk2.IEnergyConductorMK2;
import com.hbm.api.energymk2.IEnergyConnectorMK2;
import com.hbm.api.energymk2.Nodespace;
import com.hbm.api.energymk2.Nodespace.PowerNode;
import com.hbm.api.energymk2.PowerNetMK2;
import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

@AutoRegister
public class TileEntityCableBaseNT extends TileEntityLoadedBase implements IEnergyConductorMK2, ITickable {
	protected PowerNode node;

    private final EnumMap<EnumFacing, FENeighbor> feNeighbors = new EnumMap<>(EnumFacing.class);
    private int feNeighborScanCooldown = 0;

	@Override
	public void update() {
		if (!world.isRemote) {

			if (this.node == null || this.node.expired) {

				if (this.shouldCreateNode()) {
					this.node = Nodespace.getNode(world, pos);

					if (this.node == null || this.node.expired) {
						this.node = this.createNode();
						Nodespace.createNode(world, this.node);
					}
				}
			}
            if(GeneralConfig.autoCableConversion) {
                if (--this.feNeighborScanCooldown <= 0) {
                    this.feNeighborScanCooldown = 20;
                    this.refreshFENeighbors();
                }

                if (this.node != null && this.node.hasValidNet() && !this.feNeighbors.isEmpty() && GeneralConfig.conversionRateHeToRF > 0) {
                    this.handleFETransfers(this.node.net);
                }
            }
		}
	}

	public boolean shouldCreateNode() {
		return true;
	}

    @Override
    public void invalidate() {
        super.invalidate();

        if(!world.isRemote) {
            if(this.node != null) {
                Nodespace.destroyNode(world, pos);
            }
        }
        this.feNeighbors.clear();
    }

    private void refreshFENeighbors() {
        if (world == null) return;
        for (EnumFacing facing : EnumFacing.VALUES) {
            ForgeDirection dir = ForgeDirection.getOrientation(facing.getIndex());
            if (!this.canConnect(dir)) {
                this.feNeighbors.remove(facing);
                continue;
            }
            BlockPos neighborPos = this.pos.offset(facing);
            if (!world.isBlockLoaded(neighborPos)) {
                this.feNeighbors.remove(facing);
                continue;
            }
            TileEntity te = world.getTileEntity(neighborPos);
            if (te == null || te.isInvalid() || te instanceof IEnergyConnectorMK2) {
                this.feNeighbors.remove(facing);
                continue;
            }
            IEnergyStorage storage = te.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());
            if (storage != null) {
                this.feNeighbors.put(facing, new FENeighbor(neighborPos, facing.getOpposite(), storage, te));
            } else {
                this.feNeighbors.remove(facing);
            }
        }
    }

    private void handleFETransfers(PowerNetMK2 net) {
        if (this.node == null || this.node.connections == null) return;
        for (DirPos con : this.node.connections) {
            if (con == null) continue;
            ForgeDirection dir = con.getDir();
            if (dir == ForgeDirection.UNKNOWN) continue;
            EnumFacing facing = dir.toEnumFacing();
            FENeighbor neighbor = this.feNeighbors.get(facing);
            if (neighbor == null) continue;
            IEnergyStorage storage = neighbor.getStorage(world);
            if (storage == null) {
                this.feNeighbors.remove(facing);
                continue;
            }
            this.pullFromFEStorage(storage, net);
            this.pushToFEStorage(storage, net);
        }
    }

    private void pullFromFEStorage(IEnergyStorage storage, PowerNetMK2 net) {
        double rate = GeneralConfig.conversionRateHeToRF;
        if (rate <= 0D || !storage.canExtract()) return;
        int maxExtractFE = storage.extractEnergy(Integer.MAX_VALUE, true);
        if (maxExtractFE <= 0) return;
        long heToReceive = (long) Math.floor(maxExtractFE / rate);
        if (heToReceive <= 0) return;
        long leftoverHE = net.sendPowerDiode(heToReceive, true);
        long acceptedHE = heToReceive - leftoverHE;
        if (acceptedHE <= 0) return;
        long feCapacity = Math.round(acceptedHE * rate);
        int feToExtract = (int) Math.min(feCapacity, maxExtractFE);
        if (feToExtract <= 0) return;
        int feExtracted = storage.extractEnergy(feToExtract, false);
        if (feExtracted <= 0) return;
        long heInjected = (long) Math.floor(feExtracted / rate);
        if (heInjected > 0) {
            net.sendPowerDiode(heInjected, false);
        }
    }

    private void pushToFEStorage(IEnergyStorage storage, PowerNetMK2 net) {
        double rate = GeneralConfig.conversionRateHeToRF;
        if (rate <= 0D || !storage.canReceive()) return;
        int freeSpaceFE = storage.receiveEnergy(Integer.MAX_VALUE, true);
        if (freeSpaceFE <= 0) return;
        long heToExtract = (long) Math.floor(freeSpaceFE / rate);
        if (heToExtract <= 0) return;
        long extractedHE = net.extractPowerDiode(heToExtract, true);
        if (extractedHE <= 0) return;
        long feCapacity = Math.round(extractedHE * rate);
        int feToSend = (int) Math.min(feCapacity, freeSpaceFE);
        if (feToSend <= 0) return;
        int feReceived = storage.receiveEnergy(feToSend, false);
        if (feReceived <= 0) return;
        long heUsed = Math.min(extractedHE, (long) Math.floor(feReceived / rate));
        if (heUsed > 0) {
            net.extractPowerDiode(heUsed, false);
        }
    }

    private static final class FENeighbor {
        private final BlockPos pos;
        private final EnumFacing capabilitySide;
        @Nullable
        private IEnergyStorage cachedStorage;
        @Nullable
        private TileEntity cachedTile;

        private FENeighbor(BlockPos pos, EnumFacing capabilitySide, IEnergyStorage storage, TileEntity tile) {
            this.pos = pos;
            this.capabilitySide = capabilitySide;
            this.cachedStorage = storage;
            this.cachedTile = tile;
        }

        @Nullable
        private IEnergyStorage getStorage(World world) {
            if (world == null || !world.isBlockLoaded(pos)) return null;
            TileEntity current = world.getTileEntity(pos);
            if (current == null || current.isInvalid()) {
                this.cachedStorage = null;
                this.cachedTile = null;
                return null;
            }
            if (current != this.cachedTile) {
                this.cachedTile = current;
                this.cachedStorage = current.getCapability(CapabilityEnergy.ENERGY, this.capabilitySide);
            }
            return this.cachedStorage;
        }
    }

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir != ForgeDirection.UNKNOWN;
	}
//
//	@Override
//	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
//		if (capability == CapabilityEnergy.ENERGY && facing != null) {
//			ForgeDirection dir = ForgeDirection.getOrientation(facing.getIndex());
//			if (canConnect(dir) && this.node != null && this.node.hasValidNet()) {
//				return true;
//			}
//		}
//		return super.hasCapability(capability, facing);
//	}
//
//	@Override
//	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
//		if (capability == CapabilityEnergy.ENERGY && facing != null) {
//			ForgeDirection dir = ForgeDirection.getOrientation(facing.getIndex());
//			if (canConnect(dir) && this.node != null && this.node.hasValidNet()) {
//				return CapabilityEnergy.ENERGY.cast(new NTMCableEnergyCapabilityWrapper(this.node.net));
//			}
//		}
//		return super.getCapability(capability, facing);
//	}
}
