package com.hbm.render.loader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

import java.io.IOException;
import java.util.Map;

//mlbv: it is broken as-is; I have successfully fixed it, but afterward I realized that no one would actually need to
//reload models on texture change——who would do that anyway? this design makes average resourcepack reload much, much
//slower and mandates an in-memory mapping for HFR<->VAO; a lot of these HFR objects are only used once for constructing
//the VAO and then discarded for gc; retaining a strong reference, which is required for building the aforementioned
//mapping, would permanently pin them on heap and cause memory bloat. Additionally, this would absolutely not work
//with all the baked models that cache the state with zero invalidation.
//Conclusion: not worth the effort; remove it.
/*
public class HFRModelReloader implements IResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

        for(HFRWavefrontObject obj : HFRWavefrontObject.allModels) {
            try {
                obj.destroy();
                IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(obj.resource);
                obj.loadObjModel(resource.getInputStream());
                // MainRegistry.logger.info("Reloading OBJ " + obj.resource.getResourcePath());
            } catch(IOException e) { }
        }

        for(Map.Entry<WaveFrontObjectVAO, HFRWavefrontObject> entry : HFRWavefrontObject.allVBOs.entrySet()) {
            WaveFrontObjectVAO vbo = entry.getKey();
            HFRWavefrontObject obj = entry.getValue();

            vbo.destroy();
            vbo.load(obj);
            // MainRegistry.logger.info("Reloading VBO " + obj.resource.getResourcePath());
        }
    }
}
*/
