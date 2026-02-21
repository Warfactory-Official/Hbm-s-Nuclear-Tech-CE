package com.hbm.items.armor;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ISatChip;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteSavedData;
import com.hbm.saveddata.satellites.SatelliteScanner;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.List;


public class ItemModLens extends ItemArmorMod implements ISatChip {

    public ItemModLens(String s) {
        super(ArmorModHandler.extra, true, false, false, false, s);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        list.add(TextFormatting.AQUA + "Satellite Frequency: " + this.getFreq(stack));
        list.add("");

        super.addInformation(stack, worldIn, list, flagIn);
    }

    @Override
    public void addDesc(List list, ItemStack stack, ItemStack armor) {
        list.add(TextFormatting.AQUA + "  " + stack.getDisplayName() + " (Freq: " + getFreq(stack) + ")");
    }

    @Override
    public void modUpdate(EntityLivingBase entity, ItemStack armor) {
        World world = entity.getEntityWorld();
        if(world.isRemote) return;

        if(!(entity instanceof EntityPlayer)) return;

        EntityPlayerMP player = (EntityPlayerMP) entity;
        ItemStack lens = ArmorModHandler.pryMods(armor)[ArmorModHandler.extra];

        if(lens == null) return;

        int freq = this.getFreq(lens);
        Satellite sat = SatelliteSavedData.getData(world).getSatFromFreq(freq);
        if(!(sat instanceof SatelliteScanner)){
            MainRegistry.logger.debug("Satellite not found: " + freq);
            return;
        }

        int x = (int) Math.floor(player.posX);
        int y = (int) Math.floor(player.posY);
        int z = (int) Math.floor(player.posZ);
        int range = 3;

        int cX = x >> 4;
        int cZ = z >> 4;

        int height = Math.max(Math.min(y + 10, 255), 64);
        int seg = (int) (world.getTotalWorldTime() % height);

        int hits = 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for(int chunkX = cX - range; chunkX <= cX + range; chunkX++) {
            for(int chunkZ = cZ - range; chunkZ <= cZ + range; chunkZ++) {
                Chunk chunk = world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
                if(chunk == null) continue;

                for(int ix = 0; ix < 16; ix++) {
                    for(int iz = 0; iz < 16; iz++) {
                        mutablePos.setPos((chunkX << 4) + ix, seg, (chunkZ << 4) + iz);
                        Block b = chunk.getBlockState(mutablePos).getBlock();
                        int aX = mutablePos.getX();
                        int aZ = mutablePos.getZ();

                        if(addIf(ModBlocks.ore_alexandrite, b, 1, aX, seg, aZ, I18nUtil.resolveKey("tile.ore_alexandrite.name"), 0x00ffff, player)) hits++;
                        if(addIf(ModBlocks.ore_oil, b, 300, aX, seg, aZ, I18nUtil.resolveKey("tile.ore_oil.name"), 0xa0a0a0, player)) hits++;
                        if(addIf(ModBlocks.ore_bedrock_oil, b, 300, aX, seg, aZ, I18nUtil.resolveKey("tile.ore_bedrock_oil.name"), 0xa0a0a0, player)) hits++;
                        if(addIf(ModBlocks.ore_coltan, b, 5, aX, seg, aZ, I18nUtil.resolveKey("tile.ore_coltan.name"), 0xa0a000, player)) hits++;
                        if(addIf(ModBlocks.stone_gneiss, b, 5000, aX, seg, aZ, I18nUtil.resolveKey("tile.stone_gneiss.name"), 0x8080ff, player)) hits++;
                        if(addIf(ModBlocks.ore_australium, b, 1000, aX, seg, aZ, I18nUtil.resolveKey("tile.ore_australium.name"), 0xffff00, player)) hits++;
                        if(addIf(Blocks.END_PORTAL_FRAME, b, 1, aX, seg, aZ, I18nUtil.resolveKey("neutrino.end_portal.name"), 0x40b080, player)) hits++;
                        if(addIf(ModBlocks.volcano_core, b, 1, aX, seg, aZ, I18nUtil.resolveKey("tile.volcano_core.name"), 0xff4000, player)) hits++;
                        if(addIf(ModBlocks.pink_log, b, 1, aX, seg, aZ, I18nUtil.resolveKey("tile.pink_log.name"), 0xff00ff, player)) hits++;
                        if(addIf(ModBlocks.crate_ammo, b, 1, aX, seg, aZ, null, 0x800000, player)) hits++;
                        if(addIf(ModBlocks.crate_can, b, 1, aX, seg, aZ, null, 0x800000, player)) hits++;
                        if(addIf(ModBlocks.ore_bedrock_block, b, 1, aX, seg, aZ, I18nUtil.resolveKey("tile.ore_bedrock_block.name"), 0xff0000, player))hits++;

                        if(hits > 100) return;
                    }
                }
            }
        }
    }

    private boolean addIf(Block target, Block b, int chance, int x, int y, int z, String label, int color, EntityPlayerMP player) {

        if(target == b && player.getRNG().nextInt(chance) == 0) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "marker");
            data.setInteger("color", color);
            data.setInteger("expires", 15_000);
            data.setDouble("dist", 300D);
            if(label != null) data.setString("label", label);
            PacketThreading.createSendToThreadedPacket(new AuxParticlePacketNT(data, x, y, z), player);
            return true;
        }

        return false;
    }
}
