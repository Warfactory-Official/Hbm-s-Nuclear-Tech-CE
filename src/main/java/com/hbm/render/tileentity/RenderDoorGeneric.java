package com.hbm.render.tileentity;

import com.hbm.animloader.AnimatedModel;
import com.hbm.animloader.Animation;
import com.hbm.animloader.AnimationWrapper;
import com.hbm.animloader.AnimationWrapper.EndResult;
import com.hbm.animloader.AnimationWrapper.EndType;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IDoor;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.loader.IModelCustomNamed;
import com.hbm.tileentity.DoorDecl;
import com.hbm.tileentity.TileEntityDoorGeneric;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.nio.DoubleBuffer;

@AutoRegister
public class RenderDoorGeneric extends TileEntitySpecialRenderer<TileEntityDoorGeneric>
        implements IItemRendererProvider {

    private static final float[] tran = new float[3];
    private static final float[] orig = new float[3];
    private static final float[] rot = new float[3];
    private static DoubleBuffer buf = null;

    @Override
    public boolean isGlobalRenderer(TileEntityDoorGeneric te) {
        return true;
    }

    @Override
    public void render(
            TileEntityDoorGeneric te,
            double x,
            double y,
            double z,
            float partialTicks,
            int destroyStage,
            float alpha) {
        if (buf == null) {
            buf = GLAllocation.createDirectByteBuffer(8 * 4).asDoubleBuffer();
        }
        DoorDecl door = te.getDoorType();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);

        switch (te.getBlockMetadata() - BlockDummyable.offset) {
            case 2:
                GlStateManager.rotate(0 + 90, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(90 + 90, 0F, 1F, 0F);
                break;
            case 3:
                GlStateManager.rotate(180 + 90, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.rotate(270 + 90, 0F, 1F, 0F);
                break;
        }
        door.doOffsetTransform();

        double[][] clip = door.getClippingPlanes();
        for (int i = 0; i < clip.length; i++) {
            GL11.glEnable(GL11.GL_CLIP_PLANE0 + i);
            buf.put(clip[i]);
            buf.rewind();
            GL11.glClipPlane(GL11.GL_CLIP_PLANE0 + i, buf);
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableLighting();

        AnimatedModel animModel = door.getAnimatedModel();
        if (animModel != null) {
            Animation anim = door.getAnim();
            bindTexture(door.getTextureForPart(""));
            long time = System.currentTimeMillis();
            long startTime = te.state.isMovingState() ? te.animStartTime : time;
            boolean reverse = te.state == IDoor.DoorState.OPEN || te.state == IDoor.DoorState.CLOSING;
            AnimationWrapper w = new AnimationWrapper(startTime, anim).onEnd(new EndResult(EndType.STAY));
            if (reverse) w.reverse();
            animModel.controller.setAnim(w);
            animModel.renderAnimated(System.currentTimeMillis());
        } else {
            IModelCustomNamed model = door.getModel();

            long ms = System.currentTimeMillis() - te.animStartTime;
            float openTicks =
                    MathHelper.clamp(
                            te.state == IDoor.DoorState.CLOSING || te.state == IDoor.DoorState.CLOSED
                                    ? door.timeToOpen() * 50 - ms
                                    : ms,
                            0,
                            door.timeToOpen() * 50)
                            * 0.02F;
            for (String partName : model.getPartNames()) {
                if (!door.doesRender(partName, false))
                    continue;

                GlStateManager.pushMatrix();
                {
                    bindTexture(door.getTextureForPart(/*te.getSkinIndex(),*/ partName));
                    doPartTransform(door, partName, openTicks, false);
                    model.renderPart(partName);

                    for (String innerPartName : door.getChildren(partName)) {
                        if (!door.doesRender(innerPartName, true))
                            continue;

                        GlStateManager.pushMatrix();
                        {
                            bindTexture(door.getTextureForPart(/*te.getSkinIndex(),*/innerPartName));
                            doPartTransform(door, innerPartName, openTicks, true);
                            model.renderPart(innerPartName);
                        }
                        GlStateManager.popMatrix();
                    }
                }
                GlStateManager.popMatrix();
            }

        }

        for (int i = 0; i < clip.length; i++) {
            GL11.glDisable(GL11.GL_CLIP_PLANE0 + i);
        }

        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    public void doPartTransform(DoorDecl door, String name, float openTicks, boolean child) {
        door.getTranslation(name, openTicks, child, tran);
        door.getOrigin(name, orig);
        door.getRotation(name, openTicks, rot);
        GlStateManager.translate(orig[0], orig[1], orig[2]);
        if (rot[0] != 0) GL11.glRotated(rot[0], 1, 0, 0);
        if (rot[1] != 0) GL11.glRotated(rot[1], 0, 1, 0);
        if (rot[2] != 0) GL11.glRotated(rot[2], 0, 0, 1);
        GlStateManager.translate(-orig[0] + tran[0], -orig[1] + tran[1], -orig[2] + tran[2]);
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.large_vehicle_door);
    }

    @Override
    public Item[] getItemsForRenderer() {
        return new Item[]{
                Item.getItemFromBlock(ModBlocks.large_vehicle_door),
                Item.getItemFromBlock(ModBlocks.water_door),
                Item.getItemFromBlock(ModBlocks.qe_containment),
                Item.getItemFromBlock(ModBlocks.qe_sliding_door),
                Item.getItemFromBlock(ModBlocks.fire_door),
                Item.getItemFromBlock(ModBlocks.small_hatch),
                Item.getItemFromBlock(ModBlocks.round_airlock_door),
                Item.getItemFromBlock(ModBlocks.secure_access_door),
                Item.getItemFromBlock(ModBlocks.sliding_seal_door),
                Item.getItemFromBlock(ModBlocks.sliding_gate_door),
                Item.getItemFromBlock(ModBlocks.silo_hatch),
                Item.getItemFromBlock(ModBlocks.silo_hatch_large),
        };
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {

        if (item == Item.getItemFromBlock(ModBlocks.large_vehicle_door)) {
            return new ItemRenderBase() {
                public void renderInventory() {
                    GlStateManager.translate(0, -4, 0);
                    GlStateManager.scale(1.8, 1.8, 1.8);
                }

                public void renderCommon() {
                    bindTexture(ResourceManager.large_vehicle_door_tex);
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);
                    ResourceManager.large_vehicle_door.renderAll();
                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }
            };
        } else if (item == Item.getItemFromBlock(ModBlocks.water_door)) {
            return new ItemRenderBase() {
                public void renderInventory() {
                    GlStateManager.translate(0, -4, 0);
                    GlStateManager.scale(4, 4, 4);
                }

                public void renderCommon() {
                    bindTexture(ResourceManager.water_door_tex);
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);
                    ResourceManager.water_door.renderAll();
                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }
            };
        } else if (item == Item.getItemFromBlock(ModBlocks.qe_containment)) {
            return new ItemRenderBase() {
                public void renderInventory() {
                    GlStateManager.translate(0, -3.5, 0);
                    GlStateManager.scale(3.8, 3.8, 3.8);
                }

                public void renderCommon() {
                    bindTexture(ResourceManager.qe_containment_tex);
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);
                    ResourceManager.qe_containment_door.renderAllExcept("decal");
                    bindTexture(ResourceManager.qe_containment_decal);
                    ResourceManager.qe_containment_door.renderPart("decal");
                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }
            };
        } else if (item == Item.getItemFromBlock(ModBlocks.qe_sliding_door)) {
            return new ItemRenderBase() {
                public void renderInventory() {
                    GlStateManager.translate(0, -3.5, 0);
                    GlStateManager.scale(6, 6, 6);
                }

                public void renderCommon() {
                    bindTexture(ResourceManager.qe_sliding_door_tex);
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);
                    ResourceManager.qe_sliding_door.renderAll();
                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }
            };
        } else if (item == Item.getItemFromBlock(ModBlocks.fire_door)) {
            return new ItemRenderBase() {
                public void renderInventory() {
                    GlStateManager.translate(0, -3.5, 0);
                    GlStateManager.scale(3.6, 3.6, 3.6);
                }

                public void renderCommon() {
                    bindTexture(ResourceManager.fire_door_tex);
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);
                    ResourceManager.fire_door.renderAll();
                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }
            };
        } else if (item == Item.getItemFromBlock(ModBlocks.small_hatch)) {
            return new ItemRenderBase() {
                public void renderInventory() {
                    GlStateManager.translate(0, -4.5, 0);
                    GlStateManager.scale(6, 6, 6);
                }

                public void renderCommon() {
                    bindTexture(ResourceManager.small_hatch_tex);
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);
                    ResourceManager.small_hatch.renderAll();
                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }
            };
        } else if (item == Item.getItemFromBlock(ModBlocks.round_airlock_door)) {
            return new ItemRenderBase() {
                public void renderInventory() {
                    GlStateManager.translate(0, -4, 0);
                    GlStateManager.scale(3, 3, 3);
                }

                public void renderCommon() {
                    bindTexture(ResourceManager.round_airlock_door_tex);
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);
                    ResourceManager.round_airlock_door.renderAll();
                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }
            };
        } else if (item == Item.getItemFromBlock(ModBlocks.secure_access_door)) {
            return new ItemRenderBase() {
                public void renderInventory() {
                    GlStateManager.translate(0, -4, 0);
                    GlStateManager.scale(2.4, 2.4, 2.4);
                    GlStateManager.rotate(90, 0, -1, 0);
                }

                public void renderCommon() {
                    bindTexture(ResourceManager.secure_access_door_tex);
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);
                    ResourceManager.secure_access_door.renderAll();
                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }
            };
        } else if (item == Item.getItemFromBlock(ModBlocks.sliding_seal_door)) {
            return new ItemRenderBase() {
                public void renderInventory() {
                    GlStateManager.translate(0, -5, 0);
                    GlStateManager.scale(7, 7, 7);
                }

                public void renderCommon() {
                    bindTexture(ResourceManager.sliding_seal_door_tex);
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);
                    ResourceManager.sliding_seal_door.renderAll();
                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }
            };
        } else if (item == Item.getItemFromBlock(ModBlocks.sliding_gate_door)) {
            return new ItemRenderBase() {
                public void renderInventory() {
                    GlStateManager.translate(0, -5, 0);
                    GlStateManager.scale(7, 7, 7);
                }

                public void renderCommon() {
                    bindTexture(ResourceManager.sliding_gate_door_tex);
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);
                    ResourceManager.sliding_seal_door.renderAll();
                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }
            };
        } else if (item == Item.getItemFromBlock(ModBlocks.silo_hatch)) {
            return new ItemRenderBase() {
                @Override
                public void renderInventory() {
                    GlStateManager.translate(0, -2, 0);
                    GlStateManager.scale(2, 2, 2);
                }

                @Override
                public void renderCommon() {
                    bindTexture(ResourceManager.silo_hatch_tex);
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);
                    GlStateManager.rotate(90, 0, 1, 0);
                    ResourceManager.silo_hatch.renderPart("Frame");

                    GlStateManager.translate(0, 0.875, -1.875);
                    GlStateManager.rotate(-120, 1, 0, 0);
                    GlStateManager.translate(0, -0.875, 1.875);

                    GlStateManager.translate(0, 0.25, 0);
                    ResourceManager.silo_hatch.renderPart("Hatch");

                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }
            };
        } else if (item == Item.getItemFromBlock(ModBlocks.silo_hatch_large)) {
            return new ItemRenderBase() {
                @Override
                public void renderInventory() {
                    GlStateManager.translate(0, -2, 0);
                    GlStateManager.scale(1.5, 1.5, 1.5);
                }

                @Override
                public void renderCommon() {
                    bindTexture(ResourceManager.silo_hatch_large_tex);
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);

                    GlStateManager.translate(1, 0, 0);
                    GlStateManager.rotate(90, 0, 1, 0);
                    ResourceManager.silo_hatch_large.renderPart("Frame");

                    GlStateManager.translate(0, 0.875, -2.875);
                    GlStateManager.rotate(-120, 1, 0, 0);
                    GlStateManager.translate(0, -0.875, 2.875);

                    GlStateManager.translate(0, 0.25, 0);
                    ResourceManager.silo_hatch_large.renderPart("Hatch");

                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }
            };
        }


        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -4.5, 0);
                GlStateManager.scale(0.5, 0.5, 0.5);
            }

            public void renderCommon() {
                bindTexture(ResourceManager.transition_seal_tex);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                AnimationWrapper w =
                        new AnimationWrapper(System.currentTimeMillis(), ResourceManager.transition_seal_anim)
                                .onEnd(new EndResult(EndType.STAY, null));
                ResourceManager.transition_seal.controller.setAnim(w);
                ResourceManager.transition_seal.renderAnimated(System.currentTimeMillis());
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
