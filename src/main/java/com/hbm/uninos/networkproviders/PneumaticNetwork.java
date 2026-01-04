package com.hbm.uninos.networkproviders;

import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.machine.TileEntityMachineAutocrafter;
import com.hbm.tileentity.network.TileEntityPneumoTube;
import com.hbm.uninos.INetworkProvider;
import com.hbm.uninos.NodeNet;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Compat;
import com.hbm.util.ItemStackUtil;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import java.util.*;

public class PneumaticNetwork extends NodeNet<PneumaticNetwork.ReceiverTarget, TileEntityPneumoTube, TileEntityPneumoTube.PneumaticNode, PneumaticNetwork> {

    public static final byte SEND_FIRST = 0;
    public static final byte SEND_LAST = 1;
    public static final byte SEND_RANDOM = 2;

    public static final byte RECEIVE_ROBIN = 0;
    public static final byte RECEIVE_RANDOM = 1;

    public static final INetworkProvider<PneumaticNetwork> THE_PNEUMATIC_PROVIDER = PneumaticNetwork::new;

    public final Random rand = new Random();

    protected static final int TIMEOUT_MS = 1_000;
    // not actually individual items, but rather the total "mass", based on max stack size
    public static final int ITEMS_PER_TRANSFER = 64;

    public record ReceiverTarget(BlockPos pos, ForgeDirection pipeDir, TileEntityPneumoTube endpointTube) {
        public ReceiverTarget(BlockPos pos, ForgeDirection pipeDir, TileEntityPneumoTube endpointTube) {
            this.pos = pos.toImmutable();
            this.pipeDir = pipeDir;
            this.endpointTube = endpointTube;
        }

        public static ReceiverTarget key(BlockPos pos) {
            return new ReceiverTarget(pos, ForgeDirection.UNKNOWN, null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ReceiverTarget other)) return false;
            return Objects.equals(this.pos, other.pos);
        }

        @Override
        public int hashCode() {
            return TileEntityPneumoTube.getIdentifier(pos);
        }
    }

    @Override
    public void addReceiver(ReceiverTarget receiver) {
        if (receiver == null) return;
        this.receiverEntries.removeLong(receiver);
        super.addReceiver(receiver);
    }

    @Override
    public void update() {
        long now = System.currentTimeMillis();

        ObjectIterator<Object2LongMap.Entry<ReceiverTarget>> it = this.receiverEntries.object2LongEntrySet().fastIterator();
        while (it.hasNext()) {
            Object2LongMap.Entry<ReceiverTarget> e = it.next();
            ReceiverTarget rt = e.getKey();
            long ts = e.getLongValue();

            if (now - ts > TIMEOUT_MS) {
                it.remove();
                continue;
            }

            TileEntityPneumoTube tube = rt.endpointTube;
            World w = tube != null ? tube.getWorld() : null;
            if (w == null) continue;

            TileEntity tile = Compat.getTileStandard(w, rt.pos.getX(), rt.pos.getY(), rt.pos.getZ());
            if (tile == null || tile.isInvalid()) {
                it.remove();
            }
        }
    }

    public boolean send(TileEntity sourceTile, TileEntityPneumoTube tube, ForgeDirection accessDir, int sendOrder, int receiveOrder, int maxRange,
                        int nextReceiver) {

        if (sourceTile == null || tube == null) return false;
        if (this.receiverEntries.isEmpty()) return false;

        World world = tube.getWorld();
        if (world == null) return false;

        long now = System.currentTimeMillis();
        ObjectIterator<Object2LongMap.Entry<ReceiverTarget>> it = this.receiverEntries.object2LongEntrySet().fastIterator();
        while (it.hasNext()) {
            Object2LongMap.Entry<ReceiverTarget> e = it.next();
            if (now - e.getLongValue() > TIMEOUT_MS) it.remove();
        }
        if (this.receiverEntries.isEmpty()) return false;

        IItemHandler sourceHandler = resolveItemHandlerStrict(sourceTile, accessDir);
        if (sourceHandler == null || sourceHandler.getSlots() <= 0) return false;

        List<ReceiverCandidate> candidates = collectCandidates(world);
        if (candidates.isEmpty()) return false;

        // for round robin, receivers are ordered by proximity to the source
        candidates.sort(new ReceiverComparator(tube));
        ReceiverCandidate chosen = selectCandidate(candidates, receiveOrder, nextReceiver);
        if (chosen == null) return false;

        TileEntity destTile = chosen.tile;

        // range check (only if both are TEs — always true here)
        int dx = sourceTile.getPos().getX() - destTile.getPos().getX();
        int dy = sourceTile.getPos().getY() - destTile.getPos().getY();
        int dz = sourceTile.getPos().getZ() - destTile.getPos().getZ();
        int sq = dx * dx + dy * dy + dz * dz;
        if (sq > maxRange * maxRange) return false;

        IItemHandler destHandler = chosen.handler;

        int[] sourceSlotOrder = buildSlotOrder(sourceHandler);
        if (sendOrder == SEND_LAST) BobMathUtil.reverseIntArray(sourceSlotOrder);
        if (sendOrder == SEND_RANDOM) BobMathUtil.shuffleIntArray(sourceSlotOrder);

        int itemsLeftToSend = ITEMS_PER_TRANSFER;
        int itemHardCap = chosen.autocrafter ? 1 : ITEMS_PER_TRANSFER;
        boolean didSomething = false;

        for (int sourceSlot : sourceSlotOrder) {
            if (itemsLeftToSend <= 0) break;

            ItemStack sourceStack = sourceHandler.getStackInSlot(sourceSlot);
            if (sourceStack.isEmpty()) continue;

            // sender filter
            boolean match = tube.matchesFilter(sourceStack);
            if ((match && !tube.whitelist) || (!match && tube.whitelist)) continue;

            // receiver filter (endpoint tube)
            TileEntityPneumoTube endpointTube = chosen.endpointTube;
            if (endpointTube != null && endpointTube != tube) {
                match = endpointTube.matchesFilter(sourceStack);
                if ((match && !endpointTube.whitelist) || (!match && endpointTube.whitelist)) continue;
            }

            // the "mass" of an item. something that only stacks to 4 has a "mass" of 16. max transfer mass is 64, i.e. one standard stack, or one single unstackable item
            int proportionalValue = MathHelper.clamp(64 / sourceStack.getMaxStackSize(), 1, 64);

            if (itemsLeftToSend < proportionalValue) continue;

            // fill existing stacks first
            for (int destSlot = 0; destSlot < destHandler.getSlots(); destSlot++) {
                if (itemsLeftToSend < proportionalValue) break;

                ItemStack destStack = destHandler.getStackInSlot(destSlot);
                if (destStack.isEmpty()) continue;
                if (!ItemStackUtil.areStacksCompatible(sourceStack, destStack)) continue;

                int capacity = Math.min(destHandler.getSlotLimit(destSlot), destStack.getMaxStackSize());
                int space = capacity - destStack.getCount();
                if (space <= 0) continue;

                int maxByMass = Math.min(itemsLeftToSend / proportionalValue, itemHardCap);
                if (maxByMass <= 0) break;

                ItemStack currentSource = sourceHandler.getStackInSlot(sourceSlot);
                if (currentSource.isEmpty()) break;

                int attempt = Math.min(Math.min(space, currentSource.getCount()), maxByMass);
                if (attempt <= 0) continue;

                int moved = transferItems(sourceHandler, sourceSlot, destHandler, destSlot, attempt);
                if (moved > 0) {
                    itemsLeftToSend -= moved * proportionalValue;
                    didSomething = true;
                    if (itemsLeftToSend <= 0) break;
                    sourceStack = sourceHandler.getStackInSlot(sourceSlot);
                    if (sourceStack.isEmpty()) break;
                }
            }

            if (itemsLeftToSend <= 0) break;
            if (itemsLeftToSend < proportionalValue) continue;

            // empty slots
            for (int destSlot = 0; destSlot < destHandler.getSlots(); destSlot++) {
                if (itemsLeftToSend < proportionalValue) break;

                ItemStack destStack = destHandler.getStackInSlot(destSlot);
                if (!destStack.isEmpty()) continue;

                ItemStack currentSource = sourceHandler.getStackInSlot(sourceSlot);
                if (currentSource.isEmpty()) break;

                int slotLimit = destHandler.getSlotLimit(destSlot);
                int maxByMass = Math.min(itemsLeftToSend / proportionalValue, itemHardCap);

                int attempt = Math.min(Math.min(slotLimit, currentSource.getMaxStackSize()), Math.min(currentSource.getCount(), maxByMass));
                if (attempt <= 0) continue;

                int moved = transferItems(sourceHandler, sourceSlot, destHandler, destSlot, attempt);
                if (moved > 0) {
                    itemsLeftToSend -= moved * proportionalValue;
                    didSomething = true;
                    if (itemsLeftToSend <= 0) break;
                }
            }
        }

        if (didSomething) {
            sourceTile.markDirty();
            destTile.markDirty();
        }

        return didSomething;
    }

    private List<ReceiverCandidate> collectCandidates(World world) {
        List<ReceiverCandidate> list = new ArrayList<>(this.receiverEntries.size());

        ObjectIterator<Object2LongMap.Entry<ReceiverTarget>> it = this.receiverEntries.object2LongEntrySet().fastIterator();
        while (it.hasNext()) {
            Object2LongMap.Entry<ReceiverTarget> e = it.next();
            ReceiverTarget rt = e.getKey();

            TileEntity tile = Compat.getTileStandard(world, rt.pos.getX(), rt.pos.getY(), rt.pos.getZ());
            if (tile == null || tile.isInvalid()) {
                it.remove();
                continue;
            }

            IItemHandler handler = resolveItemHandlerStrict(tile, rt.pipeDir.getOpposite());
            if (handler == null || handler.getSlots() <= 0) {
                it.remove();
                continue;
            }

            list.add(new ReceiverCandidate(rt, tile, handler));
        }

        return list;
    }

    private ReceiverCandidate selectCandidate(List<ReceiverCandidate> candidates, int receiveOrder, int nextReceiver) {
        if (candidates.isEmpty()) return null;
        if (receiveOrder == RECEIVE_RANDOM) return candidates.get(rand.nextInt(candidates.size()));
        int idx = Math.floorMod(nextReceiver, candidates.size());
        return candidates.get(idx);
    }

    private static int[] buildSlotOrder(IItemHandler handler) {
        int[] order = new int[handler.getSlots()];
        for (int i = 0; i < order.length; i++) order[i] = i;
        return order;
    }

    private static int transferItems(IItemHandler source, int sourceSlot, IItemHandler dest, int destSlot, int maxAmount) {
        if (maxAmount <= 0) return 0;

        ItemStack simulatedExtraction = source.extractItem(sourceSlot, maxAmount, true);
        if (simulatedExtraction.isEmpty()) return 0;

        ItemStack simulatedInsertion = dest.insertItem(destSlot, simulatedExtraction, true);
        int accepted = simulatedExtraction.getCount() - simulatedInsertion.getCount();
        if (accepted <= 0) return 0;

        ItemStack extracted = source.extractItem(sourceSlot, accepted, false);
        if (extracted.isEmpty()) return 0;

        ItemStack leftover = dest.insertItem(destSlot, extracted, false);
        int inserted = extracted.getCount() - leftover.getCount();

        if (!leftover.isEmpty()) {
            ItemStack remainder = ItemHandlerHelper.insertItem(source, leftover, false);
            if (!remainder.isEmpty()) {
                remainder = source.insertItem(sourceSlot, remainder, false);
                if (!remainder.isEmpty()) {
                    inserted -= remainder.getCount();
                    if (inserted < 0) inserted = 0;
                }
            }
        }

        return inserted;
    }

    public static IItemHandler resolveItemHandlerStrict(TileEntity tile, ForgeDirection direction) {
        if (tile == null || tile.isInvalid()) return null;

        EnumFacing facing = (direction != null && direction != ForgeDirection.UNKNOWN) ? direction.toEnumFacing() : null;

        if (facing != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
            IItemHandler h = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
            if (h != null && h.getSlots() > 0) return h;
        }

        if (facing != null && tile instanceof ISidedInventory) {
            IItemHandler h = new SidedInvWrapper((ISidedInventory) tile, facing);
            if (h.getSlots() > 0) return h;
        }

        if (tile instanceof IInventory) {
            IItemHandler h = new InvWrapper((IInventory) tile);
            if (h.getSlots() > 0) return h;
        }

        return null;
    }

    public static boolean hasItemHandler(TileEntity tile, ForgeDirection direction) {
        return resolveItemHandlerStrict(tile, direction) != null;
    }

    private static final class ReceiverCandidate {
        final ReceiverTarget target;
        final TileEntity tile;
        final IItemHandler handler;
        final boolean autocrafter;
        final TileEntityPneumoTube endpointTube;

        ReceiverCandidate(ReceiverTarget target, TileEntity tile, IItemHandler handler) {
            this.target = target;
            this.tile = tile;
            this.handler = handler;
            this.autocrafter = tile instanceof TileEntityMachineAutocrafter;
            this.endpointTube = target.endpointTube;
        }
    }

    /**
     * Compares IInventory by distance, going off the assumption that they are TileEntities. Uses positional data for tie-breaking if the distance is the same.
     */
    private static final class ReceiverComparator implements Comparator<ReceiverCandidate> {
        private final TileEntityPneumoTube origin;

        ReceiverComparator(TileEntityPneumoTube origin) {
            this.origin = origin;
        }

        @Override
        public int compare(ReceiverCandidate c1, ReceiverCandidate c2) {
            TileEntity tile1 = c1.tile, tile2 = c2.tile;
            if (tile1 == null && tile2 != null) return 1;
            if (tile1 != null && tile2 == null) return -1;
            if (tile1 == null) return 0;

            int dist1 = squaredDistance(tile1, origin);
            int dist2 = squaredDistance(tile2, origin);
            if (dist1 == dist2) {
                return TileEntityPneumoTube.getIdentifier(tile1.getPos()) - TileEntityPneumoTube.getIdentifier(tile2.getPos());
            }
            return dist1 - dist2;
        }

        private static int squaredDistance(TileEntity tile, TileEntityPneumoTube origin) {
            int dx = tile.getPos().getX() - origin.getPos().getX();
            int dy = tile.getPos().getY() - origin.getPos().getY();
            int dz = tile.getPos().getZ() - origin.getPos().getZ();
            return dx * dx + dy * dy + dz * dz;
        }
    }
}
