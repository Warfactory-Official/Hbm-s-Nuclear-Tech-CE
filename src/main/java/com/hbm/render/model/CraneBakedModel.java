package com.hbm.render.model;

import com.hbm.blocks.network.BlockCraneBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static com.hbm.blocks.network.BlockCraneBase.FACING;
import static com.hbm.blocks.network.BlockCraneBase.OUTPUT_OVERRIDE;

@SideOnly(Side.CLIENT)
public class CraneBakedModel extends AbstractBakedModel {
    private final BlockCraneBase block;

    public CraneBakedModel(BlockCraneBase block) {
        super(BakedModelTransforms.standardBlock());
        this.block = block;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        List<BakedQuad> quads = new ArrayList<>();

        if (state == null) state = block.getDefaultState();

        EnumFacing input = state.getValue(FACING);
        EnumFacing outputOverride = null;
        if (state instanceof IExtendedBlockState) {
            outputOverride = ((IExtendedBlockState) state).getValue(OUTPUT_OVERRIDE);
        }
        EnumFacing output = outputOverride != null ? outputOverride : input.getOpposite();

        if (side != null) {
            addQuadsForFace(state, side, input, output, quads);
        }

        return quads;
    }

    private void addQuadsForFace(IBlockState state, EnumFacing side, EnumFacing input, EnumFacing output, List<BakedQuad> quads) {
        TextureAtlasSprite sprite = getSpriteForSide(side, input, output);
        if (sprite == null) return;

        int rotation = getRotationForFace(side, input, output);

        FaceBakery bakery = new FaceBakery();
        Vector3f from = new Vector3f(0, 0, 0);
        Vector3f to = new Vector3f(16, 16, 16);

        BlockFaceUV uv = new BlockFaceUV(new float[]{0, 0, 16, 16}, rotation * 90);
        BlockPartFace partFace = new BlockPartFace(side, -1, "", uv);

        quads.add(bakery.makeBakedQuad(from, to, partFace, sprite, side, TRSRTransformation.identity(), null, true, true));
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return block.iconDirectional;
    }

    // 0=Top, 1=Right, 2=Bottom, 3=Left
    private int getLocalDir(EnumFacing face, EnumFacing dir) {
        if (dir == face || dir == face.getOpposite()) return -1;
        switch (face) {
            case DOWN:
                if (dir == EnumFacing.SOUTH) return 0;
                if (dir == EnumFacing.EAST) return 1;
                if (dir == EnumFacing.NORTH) return 2;
                if (dir == EnumFacing.WEST) return 3;
                break;
            case UP:
                if (dir == EnumFacing.NORTH) return 0;
                if (dir == EnumFacing.EAST) return 1;
                if (dir == EnumFacing.SOUTH) return 2;
                if (dir == EnumFacing.WEST) return 3;
                break;
            case NORTH:
                if (dir == EnumFacing.UP) return 0;
                if (dir == EnumFacing.WEST) return 1;
                if (dir == EnumFacing.DOWN) return 2;
                if (dir == EnumFacing.EAST) return 3;
                break;
            case SOUTH:
                if (dir == EnumFacing.UP) return 0;
                if (dir == EnumFacing.EAST) return 1;
                if (dir == EnumFacing.DOWN) return 2;
                if (dir == EnumFacing.WEST) return 3;
                break;
            case WEST:
                if (dir == EnumFacing.UP) return 0;
                if (dir == EnumFacing.SOUTH) return 1;
                if (dir == EnumFacing.DOWN) return 2;
                if (dir == EnumFacing.NORTH) return 3;
                break;
            case EAST:
                if (dir == EnumFacing.UP) return 0;
                if (dir == EnumFacing.NORTH) return 1;
                if (dir == EnumFacing.DOWN) return 2;
                if (dir == EnumFacing.SOUTH) return 3;
                break;
        }
        return -1;
    }

    private TextureAtlasSprite getSpriteForSide(EnumFacing side, EnumFacing input, EnumFacing output) {
        if (side == input) return side.getAxis().isHorizontal() ? block.iconSideIn : block.iconIn;
        if (side == output) return side.getAxis().isHorizontal() ? block.iconSideOut : block.iconOut;

        int inDir = getLocalDir(side, input);
        int outDir = getLocalDir(side, output);

        boolean isStraight = input == output.getOpposite();

        if (isStraight) {
            if (inDir == -1) return side.getAxis() == EnumFacing.Axis.Y ? block.iconTop : block.iconSide;

            if (side.getAxis().isHorizontal() && (input == EnumFacing.UP || input == EnumFacing.DOWN)) {
                return output == EnumFacing.UP ? block.iconDirectionalUp : block.iconDirectionalDown;
            }

            return block.iconDirectional;
        } else {
            if (inDir != -1 && outDir != -1) {
                if (side.getAxis() == EnumFacing.Axis.Y) {
                    int diff = (outDir - inDir + 4) % 4;
                    if (diff == 1) return block.iconDirectionalTurnLeft;
                    if (diff == 3) return block.iconDirectionalTurnRight;
                } else {
                    if (inDir == 3 && outDir == 0) return block.iconDirectionalSideLeftTurnUp;
                    if (inDir == 1 && outDir == 0) return block.iconDirectionalSideRightTurnUp;
                    if (inDir == 3 && outDir == 2) return block.iconDirectionalSideLeftTurnDown;
                    if (inDir == 1 && outDir == 2) return block.iconDirectionalSideRightTurnDown;
                    if (inDir == 0 && outDir == 3) return block.iconDirectionalSideUpTurnLeft;
                    if (inDir == 0 && outDir == 1) return block.iconDirectionalSideUpTurnRight;
                    if (inDir == 2 && outDir == 3) return block.iconDirectionalSideDownTurnLeft;
                    if (inDir == 2 && outDir == 1) return block.iconDirectionalSideDownTurnRight;
                }
            }
        }

        return side.getAxis() == EnumFacing.Axis.Y ? block.iconTop : block.iconSide;
    }

    private int getRotationForFace(EnumFacing side, EnumFacing input, EnumFacing output) {
        if (side == input || side == output) return 0;

        int inDir = getLocalDir(side, input);
        int outDir = getLocalDir(side, output);

        boolean isStraight = input == output.getOpposite();

        if (isStraight) {
            if (inDir != -1) {
                if (side.getAxis().isHorizontal() && (input == EnumFacing.UP || input == EnumFacing.DOWN)) {
                    return 0;
                }

                if (side.getAxis() == EnumFacing.Axis.Y && input.getAxis() == EnumFacing.Axis.Z) {
                    return (outDir + 2) % 4;
                }

                return outDir;
            }
        } else {
            if (inDir != -1 && outDir != -1) {
                if (side.getAxis() == EnumFacing.Axis.Y) {
                    if(input.getAxis() == EnumFacing.Axis.X && output.getAxis() == EnumFacing.Axis.Z) {
                        int rot;
                        if(input == EnumFacing.EAST) rot = output == EnumFacing.NORTH ? 1 : 3;
                        else rot = output == EnumFacing.NORTH ? 3 : 1;
                        return (outDir + rot) % 4;
                    }
                    return (inDir + 2) % 4;
                }
                return 0;
            }   
        }
        return 0;
    }
}