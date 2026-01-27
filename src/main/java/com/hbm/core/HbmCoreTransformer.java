package com.hbm.core;

import net.minecraft.launchwrapper.IClassTransformer;

public final class HbmCoreTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || transformedName == null) return basicClass;

        // a class is transformed at most once
        return switch (transformedName) {
            case GlStateManagerTransformer.TARGET ->
                    GlStateManagerTransformer.transform(name, transformedName, basicClass);
            case ContainerTransformer.TARGET ->
                    ContainerTransformer.transform(name, transformedName, basicClass);
            case InventoryPlayerTransformer.TARGET ->
                    InventoryPlayerTransformer.transform(name, transformedName, basicClass);
            case ForgeHooksTransformer.TARGET ->
                    ForgeHooksTransformer.transform(name, transformedName, basicClass);
            case PlayerInteractionManagerTransformer.TARGET ->
                    PlayerInteractionManagerTransformer.transform(name, transformedName, basicClass);
            case FMLNetworkTransformer.TARGET_DISPATCHER ->
                    FMLNetworkTransformer.transformNetworkDispatcher(name, transformedName, basicClass);
            case FMLNetworkTransformer.TARGET_PACKET ->
                    FMLNetworkTransformer.transformFMLProxyPacket(name, transformedName, basicClass);
            case EntityItemHazardTransformer.TARGET ->
                    EntityItemHazardTransformer.transform(name, transformedName, basicClass);
//            case ResourceAccessTransformer.TARGET ->
//                    ResourceAccessTransformer.transform(name, transformedName, basicClass);
            case BlockDummyAirTransformer.TARGET ->
                    BlockDummyAirTransformer.transform(name,transformedName,basicClass);
            default -> basicClass;
        };
    }
}
