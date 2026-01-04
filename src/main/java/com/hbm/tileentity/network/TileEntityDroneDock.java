package com.hbm.tileentity.network;

import com.hbm.entity.item.EntityRequestDrone;
import com.hbm.entity.item.EntityRequestDrone.DroneProgram;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.container.ContainerDroneDock;
import com.hbm.inventory.gui.GUIDroneDock;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemDrone;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.network.RequestNetwork.PathNode;
import com.hbm.tileentity.network.RequestNetwork.RequestNode;
import com.hbm.tileentity.network.RequestNetwork.OfferNode;
import com.hbm.util.SwappedHashSet;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AutoRegister
public class TileEntityDroneDock extends TileEntityRequestNetworkContainer implements IGUIProvider {

    public static final int pathingDepth = 10;
    private int deployCooldown = 0;

    public TileEntityDroneDock() {
        super(9);
    }

    @Override
    public String getName() {
        return "container.droneDock";
    }

    @Override
    public void update() {
        super.update();

        if (deployCooldown > 0) deployCooldown--;

        if(deployCooldown <= 0 && !world.isRemote && world.getTotalWorldTime() % 20 == 0 && this.hasDrone()) {

            // grab all nodes in a 5 chunk radius
            SwappedHashSet<PathNode> localNodes = getAllLocalNodes(world, pos.getX(), pos.getZ(), 5);
            List<RequestNode> requests = new ArrayList<>();
            List<OfferNode> offers = new ArrayList<>();

            for(PathNode local : localNodes) {
                if(local instanceof RequestNode) requests.add((RequestNode) local);
                if(local instanceof OfferNode) offers.add((OfferNode) local);
            }

            attempt: for(int i = 0; i < 5; i++) {

                // randomize!
                Collections.shuffle(requests);
                Collections.shuffle(offers);
                RequestNode firstRequest = null;

                // simply pick the first request node that has unfulfilled requests
                for(RequestNode request : requests) {
                    if(request.active && !request.request.isEmpty()) {
                        firstRequest = request;
                        break;
                    }
                }

                if(firstRequest != null) {

                    PathNode own = localNodes.getMap().get(pos.up().hashCode());
                    AStack request = firstRequest.request.get(world.rand.nextInt(firstRequest.request.size()));

                    outer: for(OfferNode offer : offers) {

                        for(ItemStack stack : offer.offer) {
                            if(offer.active && stack != null && request.matchesRecipe(stack, true)) {
                                if(tryEmbark(own, firstRequest, offer, request, localNodes)) break attempt; // if the drone can be pathed and spawned, stop doing more attempts
                                break outer; // if not, simply continue iterating over offer nodes
                            }
                        }
                    }
                }
            }
        }
    }

    public void dockDrone(int slot, ItemStack drone) {
        boolean empty = true;
        for (int i = 0; i < this.inventory.getSlots(); i++) {
            ItemStack stack = this.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) { empty = false; break; }
        }
        if (empty) this.deployCooldown = 100;
        this.inventory.setStackInSlot(slot, drone);
    }

    public boolean tryEmbark(PathNode dock, RequestNode request, OfferNode offer, AStack item, SwappedHashSet<PathNode> localNodes) {

        List<PathNode> dockToOffer = generatePath(dock, offer, localNodes);
        if(dockToOffer == null) return false;
        List<PathNode> offerToRequest = generatePath(offer, request, localNodes);
        if(offerToRequest == null) return false;
        List<PathNode> requestToDock = generatePath(request, dock, localNodes);
        if(requestToDock == null) return false;


        for(int i = 0; i < this.inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if(!stack.isEmpty() && stack.getItem() == ModItems.drone && stack.getItemDamage() == ItemDrone.EnumDroneType.REQUEST.ordinal()) {
                this.decrStackSize(i, 1);
                break;
            }
        }

        EntityRequestDrone drone = new EntityRequestDrone(world);
        drone.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);

        // write programming
        for(PathNode node : dockToOffer) drone.program.add(node.pos);
        drone.program.add(offer.pos);
        drone.program.add(item);
        for(PathNode node : offerToRequest) drone.program.add(node.pos);
        drone.program.add(request.pos);
        drone.program.add(DroneProgram.UNLOAD);
        for(PathNode node : requestToDock) drone.program.add(node.pos);
        drone.program.add(dock.pos);
        drone.program.add(DroneProgram.DOCK);

        world.spawnEntity(drone);
        this.world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.storageOpen, SoundCategory.BLOCKS, 2.0F, 1.0F);

        return true;
    }

    public List<PathNode> generatePath(PathNode start, PathNode end, SwappedHashSet<PathNode> localNodes) {

        List<List<PathNode>> paths = new ArrayList<>();
        List<PathNode> init = new ArrayList<>();
        init.add(start);
        paths.add(init);

        // breadth-first search
        for(int i = 0; i < pathingDepth; i++) {

            List<List<PathNode>> newPaths = new ArrayList<>();

            for(List<PathNode> oldPath : paths) {
                for(PathNode connectedUnsafe : oldPath.get(oldPath.size() - 1).reachableNodes) {

                    PathNode connectedSafe = localNodes.getMap().get(connectedUnsafe.hashCode()); // lookup to translate potentially outdated nodes into current ones
                    if(connectedSafe != null) {

                        List<PathNode> newPath = new ArrayList<>(oldPath);

                        if(connectedSafe.hashCode() == end.hashCode()) {
                            newPath.remove(0); // we only want the in-betweens
                            return newPath;
                        }

                        newPath.add(connectedSafe);
                        newPaths.add(newPath);
                    }
                }
            }

            paths = newPaths;
        }

        return null;
    }

    public boolean hasDrone() {

        for(int i = 0; i < this.inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if(!stack.isEmpty() && stack.getItem() == ModItems.drone && stack.getItemDamage() == ItemDrone.EnumDroneType.REQUEST.ordinal()) return true;
        }

        return false;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerDroneDock(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIDroneDock(player.inventory, this);
    }

    @Override
    public PathNode createNode(BlockPos pos) {
        return new PathNode(pos, this.reachableNodes);
    }
}
