package com.hbm.render.block;

import com.hbm.main.MainRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.chisel.ctm.client.model.ModelBakedCTM;
import team.chisel.ctm.client.model.ModelCTM;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class CTMModelWrapper {

    public static IBakedModel wrap(IModel model, IBakedModel baked) {
        try {
            ModelCTM ctm = new ModelCTM(null, model, Int2ObjectMaps.emptyMap());
            ctm.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, rl -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString()));
            return new ModelBakedCTM(ctm, baked);
        } catch (IOException e) {
            MainRegistry.logger.error("Could not wrap CTM model", e);
            return baked;
        }
    }
}
