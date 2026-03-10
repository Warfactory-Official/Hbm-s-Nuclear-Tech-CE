package com.hbm.render.tileentity;

import com.hbm.blocks.generic.BlockReeds;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.BlockReedsBakedModel;
import com.hbm.tileentity.TileEntityReeds;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoRegister
public class RenderReeds extends FastTESR<TileEntityReeds> {
    @Override
    public void renderTileEntityFast(@NotNull TileEntityReeds te, double x, double y, double z, float partialTicks, int destroyStage, float partial, @NotNull BufferBuilder buffer) {
        BlockPos pos = te.getPos();
        IBlockState state = te.getWorld().getBlockState(pos);
        World world = te.getWorld();
        IBlockState extended = state.getBlock().getExtendedState(state, world, pos);

        if (!(extended instanceof IExtendedBlockState ex)) return;

        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
        if (!(model instanceof BlockReedsBakedModel baked)) return;

        Integer depth = ex.getValue(BlockReeds.DEPTH);

        for (int i = 0; i < depth; i++) {
            List<BakedQuad> quads = baked.getQuadsMid();
            if (i == 0) quads = baked.getQuadsTop();
            else if (i == depth - 1) quads = baked.getQuadsBottom();

            BlockPos currentPos = pos.down(i);
            int brightness = world.getCombinedLight(currentPos, 0);

            for (BakedQuad quad : quads) {
                buffer.addVertexData(quad.getVertexData());
                buffer.putPosition(x, y - i, z);
                buffer.putBrightness4(brightness, brightness, brightness, brightness);
            }
        }
    }
}
