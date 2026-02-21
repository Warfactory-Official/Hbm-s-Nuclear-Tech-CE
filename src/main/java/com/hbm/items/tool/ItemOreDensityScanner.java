package com.hbm.items.tool;

import com.hbm.items.ModItems;
import com.hbm.items.special.ItemBedrockOreBase;
import com.hbm.items.special.ItemBedrockOreNew;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacketLegacy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemOreDensityScanner extends Item {

    public ItemOreDensityScanner(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);

        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean bool) {

        if(!(entity instanceof EntityPlayerMP) || world.getTotalWorldTime() % 5 != 0) return;

        EntityPlayerMP player = (EntityPlayerMP) entity;

        for(ItemBedrockOreNew.BedrockOreType type : ItemBedrockOreNew.BedrockOreType.VALUES) {
            double level = ItemBedrockOreBase.getOreLevel((int) Math.floor(player.posX), (int) Math.floor(player.posZ), type);
            PacketDispatcher.wrapper.sendTo(new PlayerInformPacketLegacy(
                    new TextComponentTranslation("item.bedrock_ore.type." + type.suffix + ".name")
                            .appendText(": " + ((int) (level * 100) / 100D) + " (")
                            .appendSibling(
                                    new TextComponentTranslation(translateDensity(level))
                                            .setStyle(new Style().setColor(getColor(level)))
                            )
                            .appendText(")")
                            .setStyle(new Style().setColor(TextFormatting.RESET)),
                    777 + type.ordinal(), 4000), player);
        }
    }

    public static String translateDensity(double density) {
        if(density <= 0.1) return "item.ore_density_scanner.verypoor";
        if(density <= 0.35) return "item.ore_density_scanner.poor";
        if(density <= 0.75) return "item.ore_density_scanner.low";
        if(density >= 1.9) return "item.ore_density_scanner.excellent";
        if(density >= 1.65) return "item.ore_density_scanner.veryhigh";
        if(density >= 1.25) return "item.ore_density_scanner.high";
        return "item.ore_density_scanner.moderate";
    }

    public static TextFormatting getColor(double density) {
        if(density <= 0.1) return TextFormatting.DARK_RED;
        if(density <= 0.35) return TextFormatting.RED;
        if(density <= 0.75) return TextFormatting.GOLD;
        if(density >= 1.9) return TextFormatting.AQUA;
        if(density >= 1.65) return TextFormatting.BLUE;
        if(density >= 1.25) return TextFormatting.GREEN;
        return TextFormatting.YELLOW;
    }
}
