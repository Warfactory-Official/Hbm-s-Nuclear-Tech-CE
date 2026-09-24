package com.hbm.render.tileentity;

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
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoRegister
public class RenderReeds extends FastTESR<TileEntityReeds> {
    private BlockReedsBakedModel cachedModel;

    @Override
    public void renderTileEntityFast(@NotNull TileEntityReeds te, double x, double y, double z, float partialTicks, int destroyStage, float partial, @NotNull BufferBuilder buffer) {
        BlockPos pos = te.getPos();
        IBlockState state = te.getWorld().getBlockState(pos);

        if (!(getModel(state) instanceof BlockReedsBakedModel baked)) return;

        World world = te.getWorld();

        int depth = te.getDepth();
        BlockPos.MutableBlockPos lightPos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < depth; i++) {
            List<BakedQuad> quads;

            if (i == 0) {
                quads = baked.getQuadsTop();
            } else if (i == depth - 1) {
                quads = baked.getQuadsBottom();
            } else {
                quads = baked.getQuadsMid();
            }

            lightPos.setPos(pos.getX(), pos.getY() - i, pos.getZ());
            int brightness = world.getCombinedLight(lightPos, 0);

            for (BakedQuad quad : quads) {
                buffer.addVertexData(quad.getVertexData());
                buffer.putPosition(x, y - i, z);
                buffer.putBrightness4(brightness, brightness, brightness, brightness);
            }
        }
    }

    private BlockReedsBakedModel getModel(IBlockState state) {
        if (cachedModel == null) {
            IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);

            if (model instanceof BlockReedsBakedModel reedsModel) {
                cachedModel = reedsModel;
            }
        }

        return cachedModel;
    }
}
