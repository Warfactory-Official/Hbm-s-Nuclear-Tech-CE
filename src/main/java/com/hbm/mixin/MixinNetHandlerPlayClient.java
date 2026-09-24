package com.hbm.mixin;

import com.hbm.render.chunk.SectionGeometry;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Shadow
    private WorldClient world;

    @Inject(method = "handleChunkData", at = @At("RETURN"))
    private void hbm$sectionGeometryChunk(SPacketChunkData packet, CallbackInfo ci) {
        SectionGeometry.chunkLoaded(world.getChunk(packet.getChunkX(), packet.getChunkZ()));
    }

    @Inject(method = "handleUpdateTileEntity", at = @At("RETURN"))
    private void hbm$sectionGeometryTileData(SPacketUpdateTileEntity packet, CallbackInfo ci) {
        TileEntity tile = world.getChunk(packet.getPos()).getTileEntity(packet.getPos(), Chunk.EnumCreateEntityType.CHECK);
        if (tile != null) SectionGeometry.tileChanged(tile);
    }
}
