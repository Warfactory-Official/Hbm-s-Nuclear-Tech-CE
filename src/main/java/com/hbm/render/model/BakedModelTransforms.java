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
    private static final ItemCameraTransforms PIPE_ITEM = buildPipeItem();

    private BakedModelTransforms() {
    }

    public static ItemCameraTransforms standardBlock() {
        return STANDARD_BLOCK;
    }

    public static ItemCameraTransforms pipeItem() {
        return PIPE_ITEM;
    }

    private static ItemCameraTransforms buildStandardBlock() {
        ItemTransformVec3f gui = new ItemTransformVec3f(
                new Vector3f(30, -135, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(0.625f, 0.625f, 0.625f)
        );

        ItemTransformVec3f thirdPerson = new ItemTransformVec3f(
                new Vector3f(75, 45, 0),
                new Vector3f(0, 1.5f / 16f, -2.5f / 16f),
                new Vector3f(0.5f, 0.5f, 0.5f)
        );

        ItemTransformVec3f firstPerson = new ItemTransformVec3f(
                new Vector3f(0, 45, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(0.5f, 0.5f, 0.5f)
        );

        ItemTransformVec3f ground = new ItemTransformVec3f(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 2f / 16f, 0),
                new Vector3f(0.5f, 0.5f, 0.5f)
        );

        ItemTransformVec3f head = new ItemTransformVec3f(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 13f / 16f, 7f / 16f),
                new Vector3f(1, 1, 1)
        );

        ItemTransformVec3f fixed = new ItemTransformVec3f(
                new Vector3f(0, 180, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(0.75f, 0.75f, 0.75f)
        );

        return new ItemCameraTransforms(thirdPerson, thirdPerson, firstPerson, firstPerson, head, gui, ground, fixed);
    }

    public static ItemCameraTransforms forDeco(ItemCameraTransforms standardBlock) {
        ItemTransformVec3f gui = new ItemTransformVec3f(
                new Vector3f(30, -45, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(0.625f, 0.625f, 0.625f)
        );

        return new ItemCameraTransforms(standardBlock.thirdperson_left, standardBlock.thirdperson_right, standardBlock.firstperson_left,
                standardBlock.firstperson_right, standardBlock.head, gui, standardBlock.ground, standardBlock.fixed);
    }

    private static ItemCameraTransforms buildPipeItem() {
        ItemTransformVec3f gui = new ItemTransformVec3f(
                new Vector3f(30, -45, 0),
                new Vector3f(0, 0.3f, 0),
                new Vector3f(0.8f, 0.8f, 0.8f)
        );

        ItemTransformVec3f thirdPerson = new ItemTransformVec3f(
                new Vector3f(75, 45, 0),
                new Vector3f(0, 0.25f, 0),
                new Vector3f(0.5f, 0.5f, 0.5f)
        );

        ItemTransformVec3f firstPerson = new ItemTransformVec3f(
                new Vector3f(0, 45, 0),
                new Vector3f(0, 0.25f, 0),
                new Vector3f(0.5f, 0.5f, 0.5f)
        );

        ItemTransformVec3f ground = new ItemTransformVec3f(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 2f / 16f, 0),
                new Vector3f(0.5f, 0.5f, 0.5f)
        );

        ItemTransformVec3f head = new ItemTransformVec3f(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 13f / 16f, 7f / 16f),
                new Vector3f(1, 1, 1)
        );

        ItemTransformVec3f fixed = new ItemTransformVec3f(
                new Vector3f(0, 180, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(0.75f, 0.75f, 0.75f)
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
}
