package com.hbm.render.model;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

@SideOnly(Side.CLIENT)
@SuppressWarnings("deprecation")
public final class BakedModelTransforms {

    private static final ItemCameraTransforms STANDARD_BLOCK = buildStandardBlock();
    private static final ItemCameraTransforms ISBRH = buildIsbrh();

    private BakedModelTransforms() {
    }

    public static ItemCameraTransforms standardBlock() {
        return STANDARD_BLOCK;
    }

    public static ItemCameraTransforms isbrh() {
        return ISBRH;
    }

    private static ItemCameraTransforms buildStandardBlock() {
        ItemTransformVec3f gui = new ItemTransformVec3f(
                new Vector3f(30, 225, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(0.625f, 0.625f, 0.625f)
        );

        ItemTransformVec3f thirdPerson = new ItemTransformVec3f(
                new Vector3f(75, 45, 0),
                new Vector3f(0, 2.5f / 16f, 0),
                new Vector3f(0.375f, 0.375f, 0.375f)
        );

        ItemTransformVec3f firstPerson = new ItemTransformVec3f(
                new Vector3f(0, 45, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(0.4f, 0.4f, 0.4f)
        );

        ItemTransformVec3f ground = new ItemTransformVec3f(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 3f / 16f, 0),
                new Vector3f(0.25f, 0.25f, 0.25f)
        );

        ItemTransformVec3f head = new ItemTransformVec3f(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(1, 1, 1)
        );

        ItemTransformVec3f fixed = new ItemTransformVec3f(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(0.5f, 0.5f, 0.5f)
        );

        return new ItemCameraTransforms(thirdPerson, thirdPerson, firstPerson, firstPerson, head, gui, ground, fixed);
    }

    private static ItemCameraTransforms buildIsbrh() {
        ItemTransformVec3f gui = new ItemTransformVec3f(
                new Vector3f(30, -45, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(0.625f, 0.625f, 0.625f)
        );

        ItemTransformVec3f thirdPerson = new ItemTransformVec3f(
                new Vector3f(70, -45, 0),
                new Vector3f(0, 2f / 16f, 0),
                new Vector3f(0.375f, 0.375f, 0.375f)
        );

        ItemTransformVec3f firstPerson = new ItemTransformVec3f(
                new Vector3f(0, 45, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(0.4f, 0.4f, 0.4f)
        );

        ItemTransformVec3f head = new ItemTransformVec3f(
                new Vector3f(0, 90, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(1, 1, 1)
        );

        ItemTransformVec3f ground = new ItemTransformVec3f(
                new Vector3f(0, 0, 0),
                new Vector3f(0, -0.25f, 0),
                new Vector3f(0.25f, 0.25f, 0.25f)
        );

        ItemTransformVec3f fixed = new ItemTransformVec3f(
                new Vector3f(0, 90, 0),
                new Vector3f(0, -0.035f, 0.03125f),
                new Vector3f(0.625f, 0.625f, 0.625f)
        );

        return new ItemCameraTransforms(thirdPerson, thirdPerson, firstPerson, firstPerson, head, gui, ground, fixed);
    }

    public static ItemCameraTransforms defaultItemTransforms() {
        ItemTransformVec3f thirdPerson = new ItemTransformVec3f(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 3f / 16f, 1f / 16f),
                new Vector3f(0.55f, 0.55f, 0.55f)
        );

        ItemTransformVec3f firstPerson = new ItemTransformVec3f(
                new Vector3f(0, -90, 25),
                new Vector3f(1.13f / 16f, 3.2f / 16f, 1.13f / 16f),
                new Vector3f(0.68f, 0.68f, 0.68f)
        );

        ItemTransformVec3f ground = new ItemTransformVec3f(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 2f / 16f, 0),
                new Vector3f(0.5f, 0.5f, 0.5f)
        );

        ItemTransformVec3f head = new ItemTransformVec3f(
                new Vector3f(0, 180, 0),
                new Vector3f(0, 13f / 16f, 7f / 16f),
                new Vector3f(1, 1, 1)
        );

        ItemTransformVec3f fixed = new ItemTransformVec3f(
                new Vector3f(0, 180, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(1, 1, 1)
        );

        return new ItemCameraTransforms(thirdPerson, thirdPerson, firstPerson, firstPerson, head, ItemTransformVec3f.DEFAULT, ground, fixed);
    }

    public static ItemCameraTransforms meteorSwordTransforms() {
        ItemTransformVec3f thirdPersonRight = new ItemTransformVec3f(
                new Vector3f(0F, -90F, 55F),
                new Vector3f(0F, 7F / 16F, 0F),
                new Vector3f(1.7F, 1.7F, 0.85F)
        );

        ItemTransformVec3f thirdPersonLeft = new ItemTransformVec3f(
                new Vector3f(0F, 90F, -55F),
                new Vector3f(0F, 7F / 16F, 0F),
                new Vector3f(1.7F, 1.7F, 0.85F)
        );

        ItemTransformVec3f firstPersonRight = new ItemTransformVec3f(
                new Vector3f(-20F, -90F, 10F),
                new Vector3f(1.13F / 16F, 5.2F / 16F, -0.26F / 16F),
                new Vector3f(1.36F, 1.36F, 0.68F)
        );

        ItemTransformVec3f firstPersonLeft = new ItemTransformVec3f(
                new Vector3f(-20F, -90F, -80F),
                new Vector3f(1.13F / 16F, 5.2F / 16F, -0.26F / 16F),
                new Vector3f(1.36F, 1.36F, 0.68F)
        );

        ItemTransformVec3f gui = new ItemTransformVec3f(
                new Vector3f(0F, 0F, 0F),
                new Vector3f(0F, 0F, 0F),
                new Vector3f(1.1F, 1.1F, 1.1F)
        );

        return new ItemCameraTransforms(
                thirdPersonLeft,
                thirdPersonRight,
                firstPersonLeft,
                firstPersonRight,
                ItemTransformVec3f.DEFAULT,
                gui,
                ItemTransformVec3f.DEFAULT,
                ItemTransformVec3f.DEFAULT
        );
    }
}
