package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.BeamPronter.EnumBeamType;
import com.hbm.render.misc.BeamPronter.EnumWaveType;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.machine.TileEntityCoreEmitter;
import com.hbm.tileentity.machine.TileEntityCoreInjector;
import com.hbm.tileentity.machine.TileEntityCoreReceiver;
import com.hbm.tileentity.machine.TileEntityCoreStabilizer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

@AutoRegister
public class RenderCoreComponent extends TileEntitySpecialRenderer<TileEntityMachineBase> implements IItemRendererProvider {
    @Override
    public void render(TileEntityMachineBase tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();

        GlStateManager.rotate(90, 0F, 1F, 0F);

        switch (tileEntity.getBlockMetadata()) {
            case 0 -> {
                GlStateManager.translate(0.0D, 0.5D, -0.5D);
                GlStateManager.rotate(90, 1F, 0F, 0F);
            }
            case 1 -> {
                GlStateManager.translate(0.0D, 0.5D, 0.5D);
                GlStateManager.rotate(90, -1F, 0F, 0F);
            }
            case 2 -> GlStateManager.rotate(90, 0F, 1F, 0F);
            case 4 -> GlStateManager.rotate(180, 0F, 1F, 0F);
            case 3 -> GlStateManager.rotate(270, 0F, 1F, 0F);
            case 5 -> GlStateManager.rotate(0, 0F, 1F, 0F);
        }

        GlStateManager.translate(0.0D, 0D, 0.0D);

        if (tileEntity instanceof TileEntityCoreEmitter) {
            bindTexture(ResourceManager.dfc_emitter_tex);
            ResourceManager.dfc_emitter.renderAll();
        }

        if (tileEntity instanceof TileEntityCoreReceiver) {
            bindTexture(ResourceManager.dfc_receiver_tex);
            ResourceManager.dfc_receiver.renderAll();
        }

        if (tileEntity instanceof TileEntityCoreInjector) {
            bindTexture(ResourceManager.dfc_injector_tex);
            ResourceManager.dfc_injector.renderAll();
        }

        if (tileEntity instanceof TileEntityCoreStabilizer) {
            bindTexture(ResourceManager.dfc_stabilizer_tex);
            ResourceManager.dfc_injector.renderAll();
        }

        if (tileEntity instanceof TileEntityCoreStabilizer) {
            GlStateManager.translate(0, 0.5, 0);
            int range = ((TileEntityCoreStabilizer) tileEntity).beam;

            if (range > 0) {
                BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.STRAIGHT, EnumBeamType.SOLID, 0x002333, 0x7F7F7F, 0, 1, 0F, 2, 0.125F);
                BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0x002333, 0x7F7F7F, (int) tileEntity.getWorld().getTotalWorldTime() * -8 % 360, range * 3, 0.125F, 2, 0.04F);
                BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0x003C56, 0x7F7F7F, (int) tileEntity.getWorld().getTotalWorldTime() * -8 % 360 + 180, range * 3, 0.125F, 2, 0.04F);
            }
        }

        if (tileEntity instanceof TileEntityCoreEmitter) {
            GlStateManager.translate(0, 0.5, 0);
            int range = ((TileEntityCoreEmitter) tileEntity).beam;

            if (range > 0) {
                float width = (float) Math.max(1, Math.log10(((TileEntityCoreEmitter) tileEntity).prev) - 6) / 8F;
                BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.STRAIGHT, EnumBeamType.SOLID, 0x401500, 0x7F7F7F, 0, 1, 0F, 2, width);
                BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x401500, 0x7F7F7F, (int) tileEntity.getWorld().getTotalWorldTime() % 1000, (int) (0.3F * range / width), width * 0.75F, 2, width * 0.5F);
                BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x5B1D00, 0x7F7F7F, (int) tileEntity.getWorld().getTotalWorldTime() % 1000 + 1, (int) (0.3F * range / width), width * 0.75F, 2, width * 0.5F);
            }
        }

        if (tileEntity instanceof TileEntityCoreInjector) {
            GlStateManager.translate(0, 0.5, 0);
            TileEntityCoreInjector injector = (TileEntityCoreInjector) tileEntity;
            int range = injector.beam;

            if (range > 0) {
                NTMRenderHelper.bindBlockTexture();
                if (injector.tanks[0].getFluidAmount() > 0)
                    BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.SPIRAL, EnumBeamType.SOLID, injector.tanks[0].getFluid().getFluid().getColor(), 0x7F7F7F, (int) tileEntity.getWorld().getTotalWorldTime() * -2 % 360, range, 0.09F, 3, 0.0625F);
                if (injector.tanks[1].getFluidAmount() > 0)
                    BeamPronter.prontBeam(new Vec3d(0, 0, range), EnumWaveType.SPIRAL, EnumBeamType.SOLID, injector.tanks[1].getFluid().getFluid().getColor(), 0x7F7F7F, (int) tileEntity.getWorld().getTotalWorldTime() * -2 % 360 + 180, range, 0.09F, 3, 0.0625F);
            }
        }

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return null;
    }

    @Override
    public Item[] getItemsForRenderer() {
        return new Item[]{
                Item.getItemFromBlock(ModBlocks.dfc_emitter),
                Item.getItemFromBlock(ModBlocks.dfc_receiver),
                Item.getItemFromBlock(ModBlocks.dfc_injector),
                Item.getItemFromBlock(ModBlocks.dfc_stabilizer)
        };
    }


    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -2.5, 0);
                double scale = 5;
                GlStateManager.scale(scale, scale, scale);
            }

            public void renderCommon(ItemStack item) {
                GlStateManager.scale(2, 2, 2);
                GlStateManager.rotate(90, 0, 1, 0);
                if (item.getItem() == Item.getItemFromBlock(ModBlocks.dfc_emitter)) {
                    bindTexture(ResourceManager.dfc_emitter_tex);
                    ResourceManager.dfc_emitter.renderAll();
                }
                if (item.getItem() == Item.getItemFromBlock(ModBlocks.dfc_receiver)) {
                    bindTexture(ResourceManager.dfc_receiver_tex);
                    ResourceManager.dfc_receiver.renderAll();
                }
                if (item.getItem() == Item.getItemFromBlock(ModBlocks.dfc_injector)) {
                    bindTexture(ResourceManager.dfc_injector_tex);
                    ResourceManager.dfc_injector.renderAll();
                }
                if (item.getItem() == Item.getItemFromBlock(ModBlocks.dfc_stabilizer)) {
                    bindTexture(ResourceManager.dfc_stabilizer_tex);
                    ResourceManager.dfc_injector.renderAll();
                }
            }
        };
    }
}
