package com.hbm.blocks.machine.rbmk;

import com.hbm.Tags;
import com.hbm.main.MainRegistry;
import com.hbm.render.icon.PaddedSpriteUtil;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.RBMKMiniPanelItemBakedModel;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKTerminal;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;

public class RBMKTerminal extends RBMKMiniPanelBase {

    private static final ResourceLocation PART_SPRITE = new ResourceLocation(Tags.MODID, "models/network/terminal");
    private static final float[][] ITEM_UNIT_OFFSETS = {
            {0.0F, 0.0F, 0.25F},
    };

    public RBMKTerminal(String registryName) {
        super(registryName);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityRBMKTerminal();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, net.minecraft.block.state.IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) return false;
        if (world.isRemote) {
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        super.registerSprite(map);
        PaddedSpriteUtil.register(map, PaddedSpriteUtil.inspectTexture(PART_SPRITE));
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected IBakedModel createItemModel() {
        HFRWavefrontObject model = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rbmk/terminal.obj"));
        return new RBMKMiniPanelItemBakedModel(this.sprite, Collections.singletonList(
                texturedLayer(model, null, PART_SPRITE, 0xFFFFFF, null, ITEM_UNIT_OFFSETS)));
    }
}
