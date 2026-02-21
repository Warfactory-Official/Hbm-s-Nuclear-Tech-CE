package com.hbm.items.tool;

import com.hbm.api.energymk2.IEnergyConductorMK2;
import com.hbm.api.energymk2.Nodespace;
import com.hbm.api.energymk2.PowerNetMK2;
import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ItemBakedBase;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ChatBuilder;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemPowerNetTool extends ItemBakedBase {

    public ItemPowerNetTool(String s) {
        super(s);
    }

    private static final int RADIUS = 20;

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        BlockPos targetPos = pos;
        Block block = worldIn.getBlockState(targetPos).getBlock();

        if (block instanceof BlockDummyable) {
            int[] corePos = ((BlockDummyable) block).findCore(worldIn, targetPos.getX(), targetPos.getY(), targetPos.getZ());

            if (corePos != null) {
                targetPos = new BlockPos(corePos[0], corePos[1], corePos[2]);
            }
        }

        TileEntity te = worldIn.getTileEntity(targetPos);

        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (te instanceof IEnergyConductorMK2) {
            Nodespace.PowerNode node = Nodespace.getNode(worldIn, targetPos);

            if (node != null && node.hasValidNet()) {

                PowerNetMK2 net = node.net;
                String id = Integer.toHexString(net.hashCode());
                player.sendMessage(ChatBuilder.start("Start of diagnostic for network " + id).color(TextFormatting.GOLD).flush());
                player.sendMessage(ChatBuilder.start("Links: " + net.links.size()).color(TextFormatting.YELLOW).flush());
                player.sendMessage(ChatBuilder.start("Providers: " + net.providerEntries.size()).color(TextFormatting.YELLOW).flush());
                player.sendMessage(ChatBuilder.start("Receivers: " + net.receiverEntries.size()).color(TextFormatting.YELLOW).flush());
                player.sendMessage(ChatBuilder.start("End of diagnostic for network " + id).color(TextFormatting.GOLD).flush());

                for (Nodespace.PowerNode link : net.links) {
                    for (BlockPos linkPos : link.positions) {
                        NBTTagCompound data = new NBTTagCompound();
                        data.setString("type", "debug");
                        data.setInteger("color", 0xffff00);
                        data.setFloat("scale", 0.5F);
                        data.setString("text", id);
                        PacketThreading.createAllAroundThreadedPacket(
                                new AuxParticlePacketNT(data, linkPos.getX() + 0.5D, linkPos.getY() + 1.5D, linkPos.getZ() + 0.5D),
                                new NetworkRegistry.TargetPoint(worldIn.provider.getDimension(), linkPos.getX(), linkPos.getY(), linkPos.getZ(), RADIUS));
                    }
                }

            } else {
                player.sendMessage(ChatBuilder.start("Error: No network found!").color(TextFormatting.RED).flush());
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.RED + "Right-click cable to analyze the power net.");
        tooltip.add(TextFormatting.RED + "Links (cables, poles, etc.) are YELLOW");
        tooltip.add(TextFormatting.RED + "Subscribers (any receiver) are BLUE");
        tooltip.add(TextFormatting.RED + "Links with mismatching network info (BUGGED!) are RED");
        tooltip.add(TextFormatting.RED + "Displays stats such as link and subscriber count");
        tooltip.add(TextFormatting.RED + "Proxies are connection points for multiblock links (e.g. 4 for substations)");
        tooltip.add(TextFormatting.RED + "Particles only spawn in a " + RADIUS + " block radius!");
    }
}
