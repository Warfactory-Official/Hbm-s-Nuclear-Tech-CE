package com.hbm.entity.logic;

import net.minecraftforge.common.ForgeChunkManager.Ticket;

/**
 * Contract for entity-backed Forge chunk loaders.
 *
 * <p>Implementation notes:
 *
 * <p>1. {@link #init(Ticket) init(Ticket)} is used for both fresh tickets and tickets restored by Forge's
 * forced-chunk-loading callback. Treat it as a bind/refresh hook, not as the place where you
 * decide whether the entity should chunkload.
 *
 * <p>2. Do <strong>not</strong> request tickets from {@code entityInit()}. That old 1.7 pattern runs
 * during deserialization as well, so persisted entities can request a new ticket before Forge has a
 * chance to hand back the restored one.
 *
 * <p>3. Request tickets lazily once the entity is actually live on the server, usually from the
 * first server-side {@code onUpdate()} or from an explicit opt-in method such as a
 * {@code setChunkLoading()} toggle. If the entity was read from NBT, give the restore callback a
 * short grace period before requesting a new ticket.
 *
 * <p>4. If {@link #init(Ticket) init(Ticket)} receives a duplicate ticket while one is already bound, release the
 * new ticket immediately instead of silently leaking it.
 *
 * <p>5. On shutdown, death, or failed spawn, implementations must unforce all chunks and release the
 * ticket. Unforcing chunks alone is not enough; leaked tickets eventually exhaust the mod's ticket
 * budget and make later chunkloaders fail silently.
 *
 * <p>6. Persist only the state that says whether chunkloading should be enabled. Never persist or
 * reconstruct the ticket itself from NBT.
 */
public interface IChunkLoader {

	void init(Ticket ticket);
	void loadNeighboringChunks(int newChunkX, int newChunkZ);
}
