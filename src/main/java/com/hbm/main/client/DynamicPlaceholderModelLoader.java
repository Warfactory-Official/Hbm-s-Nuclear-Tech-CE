package com.hbm.main.client;

import com.hbm.Tags;
import com.hbm.items.ClaimedModelLocationRegistry;
import com.hbm.items.IModelLocationOwner;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public enum DynamicPlaceholderModelLoader implements ICustomModelLoader {
    INSTANCE;

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (!(modelLocation instanceof ModelResourceLocation)) {
            return false;
        }

        ModelResourceLocation location = (ModelResourceLocation) modelLocation;
        if (!Tags.MODID.equals(location.getNamespace())) {
            return false;
        }

        String path = location.getPath();
        return "inventory".equals(location.getVariant())
                && (path.startsWith("items/") || path.startsWith("teisr/"))
                && ClaimedModelLocationRegistry.owns(location);
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        ModelResourceLocation location = (ModelResourceLocation) modelLocation;
        IModelLocationOwner owner = ClaimedModelLocationRegistry.find(location);
        if (owner != null) {
            return owner.loadModel(location);
        }
        throw new IllegalStateException("No model owner found for " + location);
    }
}
