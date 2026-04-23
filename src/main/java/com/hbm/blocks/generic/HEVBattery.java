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
import net.minecraft.client.renderer.block.model.BakedQuad;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HEVBattery extends BlockBakeBase {

    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.375D, 0.625D);

    public HEVBattery(Material material, String name) {
        super(material, name);
    }

    @Override
    public @NotNull BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess worldIn, @NotNull IBlockState state, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        return BOUNDS;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess worldIn, @NotNull BlockPos pos) {
        return BOUNDS;
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing side, float hitX, float hitY, float hitZ) {
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
        } catch (Exception ignored) {
        }

        TextureAtlasSprite sprite = (wavefront != null) ? Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("hbm:blocks/hev_battery_block") : Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();

        HFRWavefrontObject modelToUse = (wavefront != null) ? wavefront : new HFRWavefrontObject(new ResourceLocation("minecraft:empty"));

        IBakedModel baked = new BlockDecoBakedModel(modelToUse, sprite, true, 1.0F, 0.0F, -0.5F, 0.0F, 2, false) {
            @Override
            public @NotNull List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
                return super.getQuads(null, side, rand);
            }
        };

        event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "inventory"), baked);
        event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "normal"), baked);
    }
}
