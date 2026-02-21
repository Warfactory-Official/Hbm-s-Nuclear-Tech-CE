package com.hbm.handler;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.ForgeDirection;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.tileentity.IItemRendererProvider;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
// Th3_Sl1ze: basically, this thing renders the shaded item model of the dummy mechanism you're holding in hand if you look on a block.
// For now I turned it off cuz it's still not done. Change the "debug" boolean to true to enable it.
// TODO do smth with core preview position (it's slightly offset on some machines, check assembly machine for example)
// TODO implement correctRotation on all renderers where it's needed (definitely need to adjust some rotations & sizes)
public class PlacementPreviewHandler {

    private final boolean debug = false;
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if(!debug) return;
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;
        EntityPlayerSP player = mc.player;

        if (world == null || player == null) return;

        ItemStack held = player.getHeldItemMainhand();
        if (held.isEmpty()) held = player.getHeldItemOffhand();
        if (held.isEmpty() || !(held.getItem() instanceof ItemBlock)) return;

        Block block = ((ItemBlock) held.getItem()).getBlock();
        if (!(block instanceof BlockDummyable dummy)) return;

        RayTraceResult rt = mc.objectMouseOver;
        if (rt == null || rt.typeOfHit != RayTraceResult.Type.BLOCK) return;

        BlockPos target = rt.getBlockPos();
        EnumFacing side = rt.sideHit;
        BlockPos placePos = target.offset(side);

        ForgeDirection dir = yawToForgeDir(player);
        dir = dummy.getDirModified(dir);

        int x = placePos.getX();
        int y = placePos.getY() + dummy.getHeightOffset();
        int z = placePos.getZ();

        int o = -dummy.getOffset();

        boolean canPlace = dummy.checkRequirement(world, x, y, z, dir, o);

        BlockPos corePos = new BlockPos(x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o);

        renderGhost(event.getPartialTicks(), world, corePos, dummy, dir, canPlace);
    }

    private ForgeDirection yawToForgeDir(Entity player) {
        int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        return switch (i) {
            case 0 -> ForgeDirection.getOrientation(2); // NORTH
            case 1 -> ForgeDirection.getOrientation(5); // EAST
            case 2 -> ForgeDirection.getOrientation(3); // SOUTH
            case 3 -> ForgeDirection.getOrientation(4); // WEST
            default -> ForgeDirection.NORTH;
        };
    }

    private void renderGhost(float partialTicks, World world, BlockPos corePos, BlockDummyable block, ForgeDirection dir, boolean canPlace) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity cam = mc.getRenderViewEntity();
        if (cam == null) return;

        double camX = cam.lastTickPosX + (cam.posX - cam.lastTickPosX) * partialTicks;
        double camY = cam.lastTickPosY + (cam.posY - cam.lastTickPosY) * partialTicks;
        double camZ = cam.lastTickPosZ + (cam.posZ - cam.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(corePos.getX() - camX, corePos.getY() - camY, corePos.getZ() - camZ);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(-1f, -1f);

        if (canPlace) GlStateManager.color(0f, 1f, 0f, 0.45f);
        else          GlStateManager.color(1f, 0f, 0f, 0.45f);

        tryRenderModelViaItemRenderer(world, block, dir);

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }

    private void tryRenderModelViaItemRenderer(World world, BlockDummyable block, ForgeDirection dir) {
        // Th3_Sl1ze: strangely you need to set BOTH meta in constructor and blockMetadata itself by hand.
        // Tried to remove one of these, it didn't work. You're free to try though..
        int meta = dir.ordinal() + BlockDummyable.offset;
        TileEntity te = block.createNewTileEntity(world, meta);
        if (te == null) return;
        te.blockMetadata = meta;

        TileEntitySpecialRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(te);
        if (renderer == null) return;

        if (renderer instanceof IItemRendererProvider irp) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5, 0.0, 0.5);
            irp.correctRotation(te);

            Item item = Item.getItemFromBlock(block);
            ItemRenderBase ir = irp.getRenderer(item);
            if (ir != null) {
                ir.renderCommon();
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
        }
    }
}
