package com.hbm.mixin;

import com.hbm.render.chunk.SectionGeometry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Chunk.class)
public abstract class MixinChunk {

    @Shadow
    @Final
    private World world;

    // HBM packet handlers run on the network thread (BufPacket).
    @Unique
    private boolean hbm$clientThread() {
        return world.isRemote && Minecraft.getMinecraft().isCallingFromMinecraftThread();
    }

    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void hbm$sectionGeometryBlock(BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> cir) {
        if (cir.getReturnValue() != null && hbm$clientThread()) SectionGeometry.blockChanged(pos, state);
    }

    @Inject(method = "removeTileEntity", at = @At("HEAD"))
    private void hbm$sectionGeometryTileRemoved(BlockPos pos, CallbackInfo ci) {
        if (hbm$clientThread()) SectionGeometry.tileRemoved(pos);
    }
}
