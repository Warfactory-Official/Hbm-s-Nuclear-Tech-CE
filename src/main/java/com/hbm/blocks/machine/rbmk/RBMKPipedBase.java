package com.hbm.blocks.machine.rbmk;

import com.hbm.render.model.RBMKControlBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class RBMKPipedBase extends RBMKBase {

    @SideOnly(Side.CLIENT) protected TextureAtlasSprite pipeTop;
    @SideOnly(Side.CLIENT) protected TextureAtlasSprite pipeSide;

    protected RBMKPipedBase(String s, String c) {
        super(s, c);
    }

    @Override
    public void registerSprite(TextureMap map) {
        super.registerSprite(map);
        pipeTop = map.registerSprite(new ResourceLocation("hbm", "blocks/rbmk/" + columnTexture + "_pipe_top"));
        pipeSide = map.registerSprite(new ResourceLocation("hbm", "blocks/rbmk/" + columnTexture + "_pipe_side"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void bakeModel(ModelBakeEvent event) {
        event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "inventory"),
                new RBMKControlBakedModel(topSprite, sideSprite, pipeTop, pipeSide, coverTopSprite, coverSideSprite, glassTopSprite, glassSideSprite, null, true));

        event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "normal"),
                new RBMKControlBakedModel(topSprite, sideSprite, pipeTop, pipeSide, coverTopSprite, coverSideSprite, glassTopSprite, glassSideSprite, null, false));
    }
}
