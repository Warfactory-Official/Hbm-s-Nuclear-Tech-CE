package com.hbm.entity.logic;

import com.hbm.main.MainRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

public abstract class EntityExplosionChunkloading extends Entity implements IChunkLoader {

    private Ticket loaderTicket;
    private ChunkPos loadedChunk;
    private boolean awaitingTicketRestore;

    public EntityExplosionChunkloading(World world) {
        super(world);
    }

    @Override
    protected void entityInit() {
        // Ticket requests must not happen here; entityInit also runs during NBT restore.
    }

    @Override
    public void init(Ticket ticket) {
        if(!world.isRemote && ticket != null) {
            if(loaderTicket == null) {
                loaderTicket = ticket;
                loaderTicket.bindEntity(this);
                loaderTicket.getModData();
            } else if(loaderTicket != ticket) {
                ForgeChunkManager.releaseTicket(ticket);
            }
            ForgeChunkManager.forceChunk(loaderTicket, new ChunkPos(chunkCoordX, chunkCoordZ));
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(!world.isRemote) {
            requestChunkLoaderTicketIfNeeded();
        }
    }

    @Override
    public void loadNeighboringChunks(int newChunkX, int newChunkZ){
        // noop
    }

    public void loadChunk(int x, int z) {
        if(this.loadedChunk == null) {
            this.loadedChunk = new ChunkPos(x, z);
            ForgeChunkManager.forceChunk(loaderTicket, loadedChunk);
        }
    }

    public void clearChunkLoader() {
        if(!world.isRemote && loaderTicket != null) {
            ForgeChunkManager.releaseTicket(loaderTicket);
            this.loaderTicket = null;
        }
    }

    protected final void markChunkLoaderRestoredFromNBT() {
        this.awaitingTicketRestore = true;
    }

    protected final void requestChunkLoaderTicketIfNeeded() {
        if(world.isRemote || loaderTicket != null) return;
        if(awaitingTicketRestore) {
            awaitingTicketRestore = false;
            return;
        }
        init(ForgeChunkManager.requestTicket(MainRegistry.instance, world, Type.ENTITY));
    }
}
