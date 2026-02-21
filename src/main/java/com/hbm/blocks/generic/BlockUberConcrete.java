package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockUberConcrete extends BlockMeta {
   public static final BlockBakeFrame[] FRAMES = new BlockBakeFrame[5];
    static {
        FRAMES[0] = new BlockBakeFrame("concrete_super");
        for(int i = 0; i < 4; i++) {
            FRAMES[i+1] = new BlockBakeFrame( "concrete_super_m" + i);
        }
    }


    public BlockUberConcrete(String s) {
        super(Material.ROCK, s, (short) 15, false, FRAMES  );
        this.setTickRandomly(true);
        this.setSoundType(SoundType.STONE);
        this.separateTranslationKeys = false;
    }

    @SideOnly(Side.CLIENT)
    public void registerModel() {
        for (int meta = 0; meta < this.META_COUNT; meta++) {
            ModelLoader.setCustomModelResourceLocation(
                    Item.getItemFromBlock(this),
                    meta,
                    new ModelResourceLocation(this.getRegistryName(), "meta=" + meta)
            );
        }
    }


    private static BlockBakeFrame getIcon(int meta) {
        if(meta == 15) return FRAMES[4];
        if(meta == 14) return FRAMES[3];
        if(meta > 11) return FRAMES[2];
        if(meta > 9) return FRAMES[1];
        return FRAMES[0];
    }

    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        for (int meta = 0; meta < META_COUNT; meta++) {
            BlockBakeFrame blockFrame = blockFrames[meta % blockFrames.length];
            try {
                IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation(blockFrame.getBaseModel()));
                ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();
                var texture = new ResourceLocation(Tags.MODID, BlockBakeFrame.ROOT_PATH + getIcon(meta).textureArray[0]).toString();
                textureMap.put("all", texture);
                textureMap.put("particle",  texture);
                IModel retexturedModel = baseModel.retexture(textureMap.build());
                IBakedModel bakedModel = retexturedModel.bake(
                        ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
                );

                ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName(), "meta=" + meta);
                event.getModelRegistry().putObject(modelLocation, bakedModel);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
        int meta = state.getValue(META);

        if(rand.nextInt(meta + 1) > 0)
            return;

        if(meta < 15) {
            world.setBlockState(pos, state.withProperty(META, meta + 1), 3);
        } else {
            world.setBlockToAir(pos);

            if(world.isAirBlock(pos.down())) {
                world.setBlockState(pos, ModBlocks.concrete_super_broken.getDefaultState());
                return;
            }

            List<EnumFacing> sides = Arrays.asList(EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH);
            Collections.shuffle(sides);

            for(EnumFacing dir : sides) {
                BlockPos target = pos.offset(dir);
                if(world.isAirBlock(target) && world.isAirBlock(target.down())) {
                    EntityFallingBlock debris = new EntityFallingBlock(world, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, ModBlocks.concrete_super_broken.getDefaultState());
                    debris.fallTime = 2;
                    debris.setHurtEntities(false);
                    debris.setDropItemsWhenDead(true);
                    world.spawnEntity(debris);
                    return;
                }
            }

            world.setBlockState(pos, ModBlocks.concrete_super_broken.getDefaultState());
        }

    }
}
