package com.hbm.blocks.generic;

import com.hbm.Tags;
import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.items.armor.ArmorFSBPowered;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.model.BlockDecoBakedModel;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HEVBattery extends BlockBakeBase {

    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.375D, 0.625D);

    public HEVBattery(Material material, String name) {
        super(material, name);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDS;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return BOUNDS;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        } else if (!player.isSneaking()) {

            ItemStack helmet = player.inventory.armorInventory.get(3);
            if (ArmorFSB.hasFSBArmorIgnoreCharge(player) && !helmet.isEmpty() && helmet.getItem() instanceof ArmorFSBPowered) {

                for (ItemStack st : player.inventory.armorInventory) {
                    if (st.isEmpty()) continue;

                    if (st.getItem() instanceof IBatteryItem battery) {
                        long max = battery.getMaxCharge(st);
                        long charge = battery.getCharge(st);
                        long newcharge = Math.min(charge + 150000L, max);
                        battery.setCharge(st, newcharge);
                    }
                }


                world.playSound(null, pos, HBMSoundHandler.battery, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.setBlockToAir(pos);
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        HFRWavefrontObject wavefront = null;
        try {
            wavefront = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/battery.obj"));
        } catch (Exception ignored) {}

        if (wavefront == null) {
            TextureAtlasSprite missing = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
            IBakedModel baked = BlockDecoBakedModel.forBlock(new HFRWavefrontObject(new ResourceLocation("minecraft:empty")), missing, -0.5f);
            ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName(), "inventory");
            event.getModelRegistry().putObject(modelLocation, baked);
            ModelResourceLocation worldLocation = new ModelResourceLocation(getRegistryName(), "normal");
            event.getModelRegistry().putObject(worldLocation, baked);
        } else {
            TextureAtlasSprite sprite = Minecraft.getMinecraft()
                    .getTextureMapBlocks()
                    .getAtlasSprite(new ResourceLocation("hbm", "blocks/hev_battery_block").toString());
            IBakedModel baked = BlockDecoBakedModel.forBlock(wavefront, sprite, -0.5f);
            ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName(), "inventory");
            event.getModelRegistry().putObject(modelLocation, baked);
            ModelResourceLocation worldLocation = new ModelResourceLocation(getRegistryName(), "normal");
            event.getModelRegistry().putObject(worldLocation, baked);
        }
    }
}
