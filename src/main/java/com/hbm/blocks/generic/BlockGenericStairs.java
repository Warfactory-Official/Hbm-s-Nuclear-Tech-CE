package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IDynamicModels;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class BlockGenericStairs extends BlockStairs implements IDynamicModels {

  public static final List<Object[]> recipeGen = new ArrayList<>();

  protected BlockBakeFrame blockFrame;

  public BlockGenericStairs(Block block, String registryName) {
    this(block, registryName, block.getRegistryName().getPath(), 0);
  }

  public BlockGenericStairs(Block block, String registryName, String texture, int recipeMeta) {
    super(block.getDefaultState());

    this.setTranslationKey(registryName);
    this.setRegistryName(registryName);
    this.useNeighborBrightness = true;

    this.blockFrame =
        new BlockBakeFrame(texture) {
          @Override
          public void putTextures(ImmutableMap.Builder<String, String> builder) {
            String sprite = this.getSpriteLoc(0).toString();
            builder.put("bottom", sprite);
            builder.put("top", sprite);
            builder.put("side", sprite);
            builder.put("particle", sprite);
          }
        };

    IDynamicModels.INSTANCES.add(this);
    if (recipeMeta >= 0) {
      recipeGen.add(new Object[] {block, recipeMeta, this});
    }

    ModBlocks.ALL_BLOCKS.add(this);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerModel() {
    ModelLoader.setCustomStateMapper(this, getStateMapper(this.getRegistryName()));
    ModelLoader.setCustomModelResourceLocation(
        Item.getItemFromBlock(this),
        0,
        new ModelResourceLocation(this.getRegistryName(), "meta=0"));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerSprite(TextureMap map) {
    if (blockFrame == null) return;
    blockFrame.registerBlockTextures(map);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void bakeModel(ModelBakeEvent event) {
    IModel baseStraight;
    IModel baseInner;
    IModel baseOuter;
    try {
      baseStraight = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/stairs"));
      baseInner =
          ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/inner_stairs"));
      baseOuter =
          ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/outer_stairs"));
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    bakeStairs(event, blockFrame, baseStraight, baseInner, baseOuter, 0, false);
  }

  void bakeStairs(
      ModelBakeEvent event,
      BlockBakeFrame blockFrame,
      IModel baseStraight,
      IModel baseInner,
      IModel baseOuter,
      int meta,
      boolean multiMeta) {
    try {
      IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
      ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();
      blockFrame.putTextures(textureMap);
      IModel straight = baseStraight.retexture(textureMap.build());
      IModel inner = baseInner.retexture(textureMap.build());
      IModel outer = baseOuter.retexture(textureMap.build());

      for (EnumHalf half : EnumHalf.values()) {
        for (EnumFacing facing :
            new EnumFacing[] {
              EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST
            }) {

          String variantIn = "half=" + half.getName() + ",facing=" + facing.getName();

          if (multiMeta) {
            variantIn = "meta=" + meta + "," + variantIn;
          }

          for (EnumShape shape : EnumShape.values()) {
            ModelRotation modelRotation;

            if (shape == EnumShape.STRAIGHT) {
              modelRotation = rotationFor(facing, half);
            } else {
              modelRotation = getCornerRotation(facing, half, shape);
            }

            IModel model =
                switch (shape) {
                  case STRAIGHT -> straight;
                  case INNER_LEFT, INNER_RIGHT -> inner;
                  case OUTER_LEFT, OUTER_RIGHT -> outer;
                };

            IBakedModel bakedModel =
                model.bake(
                    modelRotation, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());

            modelRegistry.putObject(
                new ModelResourceLocation(
                    getRegistryName(), variantIn + ",shape=" + shape.getName()),
                bakedModel);
          }
        }
      }

      IBakedModel bakedItem =
          straight.bake(
              ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
      ModelResourceLocation itemMrl = new ModelResourceLocation(getRegistryName(), "meta=" + meta);
      modelRegistry.putObject(itemMrl, bakedItem);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static ModelRotation getCornerRotation(
      EnumFacing facing, EnumHalf half, EnumShape shape) {

    int x = (half == EnumHalf.TOP) ? 180 : 0;

    int baseY =
        switch (facing) {
          case EAST -> 0;
          case SOUTH -> 90;
          case WEST -> 180;
          default -> 270;
        };

    int y = baseY;

    switch (shape) {
      case INNER_RIGHT, OUTER_RIGHT:
        {
          if (half == EnumHalf.TOP) {
            y = baseY + 90;
          }
          break;
        }
      case INNER_LEFT, OUTER_LEFT:
        {
          if (half == EnumHalf.BOTTOM) {
            y = baseY + 270;
          }
          break;
        }
    }

    return getModelRotation(x, y % 360);
  }

  private static ModelRotation getModelRotation(int x, int y) {
    if (x == 0 && y == 0) return ModelRotation.X0_Y0;
    if (x == 0 && y == 90) return ModelRotation.X0_Y90;
    if (x == 0 && y == 180) return ModelRotation.X0_Y180;
    if (x == 0 && y == 270) return ModelRotation.X0_Y270;
    if (x == 180 && y == 0) return ModelRotation.X180_Y0;
    if (x == 180 && y == 90) return ModelRotation.X180_Y90;
    if (x == 180 && y == 180) return ModelRotation.X180_Y180;
    if (x == 180 && y == 270) return ModelRotation.X180_Y270;
    return ModelRotation.X0_Y0;
  }

  static ModelRotation rotationFor(EnumFacing facing, EnumHalf half) {
    int y =
        switch (facing) {
          case EAST -> 0;
          case SOUTH -> 90;
          case WEST -> 180;
          default -> 270;
        };
    int x = (half == EnumHalf.TOP) ? 180 : 0;
    return getModelRotation(x, y);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public StateMapperBase getStateMapper(ResourceLocation loc) {
    return new StateMapperBase() {
      @Override
      protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        EnumHalf half = state.getValue(HALF);
        EnumShape shape = state.getValue(SHAPE);
        return new ModelResourceLocation(
            loc,
            "half=" + half.getName() + ",facing=" + facing.getName() + ",shape=" + shape.getName());
      }
    };
  }

  @Override
  public void addInformation(
      ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
    float hardness = this.getExplosionResistance(null);
    if (hardness > 50) {
      tooltip.add("§6" + I18nUtil.resolveKey("trait.blastres", hardness));
    }
  }
}
