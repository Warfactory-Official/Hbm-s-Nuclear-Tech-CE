package com.hbm.blocks.machine;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.handler.NTMToolHandler;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.MetaBlock;
import com.hbm.util.InventoryUtil;
import com.hbm.util.Tuple.Pair;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockICFComponent extends BlockMeta implements IToolable, ILookOverlay {

    public BlockICFComponent() {
        super(
                Material.IRON,
                SoundType.METAL,
                "icf_component",
                "icf_component",                 // meta 0
                "icf_component.vessel",          // meta 1
                "icf_component.vessel_welded",   // meta 2
                "icf_component.structure",       // meta 3
                "icf_component.structure_bolted" // meta 4
        );
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand,
                           ToolType tool) {
        if (world.isRemote) return false;

        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        int meta = getMetaFromState(state);
        Pair<AStack[], MetaBlock> result = NTMToolHandler.getConversions().get(new Pair<>(tool, new MetaBlock(this, meta)));

        if (result == null) return false;

        List<AStack> materials = new ArrayList<>(Arrays.asList(result.getKey()));
        if (materials.isEmpty() || InventoryUtil.doesPlayerHaveAStacks(player, materials, true)) {
            world.setBlockState(pos, getStateFromMeta(result.value.meta), 3);
            return true;
        }

        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        ItemStack held = Minecraft.getMinecraft().player.getHeldItemMainhand();
        ToolType tool = ToolType.getType(held);
        if (tool == null) return;

        IBlockState state = world.getBlockState(pos);
        int meta = getMetaFromState(state);

        Pair<AStack[], MetaBlock> result = NTMToolHandler.getConversions().get(new Pair<>(tool, new MetaBlock(this, meta)));

        if (result == null) return;

        List<String> text = new ArrayList<>();
        text.add(TextFormatting.GOLD + "Requires:");

        for (AStack stack : result.getKey()) {
            try {
                ItemStack display = stack.extractForCyclingDisplay(20);
                text.add("- " + display.getDisplayName() + " x" + display.getCount());
            } catch (Exception ex) {
                text.add(TextFormatting.RED + "- ERROR");
            }
        }

        String blockName = new ItemStack(this, 1, meta).getDisplayName();
        ILookOverlay.printGeneric(event, blockName, 0xffff00, 0x404000, text);
    }
}
