package com.hbm.command;

import com.hbm.inventory.gui.GUIScreenWikiRender;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFFFluidDuct;
import com.hbm.items.machine.ItemRBMKPellet;
import com.hbm.items.special.ItemDepletedFuel;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.CommandException;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// horribly written command so you can render more than fucking guns without having to decompile the JAR
public class CommandWikiRender extends CommandBase {

    public static void register() {
        if(FMLLaunchHandler.side() != Side.CLIENT) return;
        ClientCommandHandler.instance.registerCommand(new CommandWikiRender());
    }

    @Override
    public String getName() {
        return "ntmwikirender";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return String.format(Locale.US, "%s/%s <type> %s- Render screenshots of a selected item type (e.g ItemGunBaseNT). Intended for developers and wiki editors only.",
                TextFormatting.GREEN, getName(), TextFormatting.LIGHT_PURPLE);
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(!(sender instanceof EntityPlayer)) {
            throw new PlayerNotFoundException("");
        }

        if(args.length == 0) {
            throw new WrongUsageException(getUsage(sender));
        }

        MainRegistry.logger.info("Taking a screenshot of " + args[0]);

        List<Item> ignoredItems = Arrays.asList(ModItems.achievement_icon, Items.SPAWN_EGG, Item.getItemFromBlock(Blocks.MOB_SPAWNER));

        List<Class<? extends Item>> collapsedClasses = Arrays.asList(ItemRBMKPellet.class, ItemDepletedFuel.class, ItemFFFluidDuct.class);

        String prefix = args[0];
        int slotScale = 16;
        boolean ignoreNonNTM = true;

        List<ItemStack> stacks = new ArrayList<>();
        for(Item item : Item.REGISTRY) {
            ResourceLocation name = item.getRegistryName();
            if(ignoreNonNTM && (name == null || !"hbm".equals(name.getNamespace())))
                continue;
            if(ignoredItems.contains(item))
                continue;
            Block block = Block.getBlockFromItem(item);
            if(!item.getClass().getSimpleName().equalsIgnoreCase(args[0])
                    && (block == Blocks.AIR || !block.getClass().getSimpleName().equalsIgnoreCase(args[0])))
                continue;
            if(collapsedClasses.contains(item.getClass())) {
                stacks.add(new ItemStack(item));
            } else {
                NonNullList<ItemStack> subItems = NonNullList.create();
                item.getSubItems(CreativeTabs.SEARCH, subItems);
                stacks.addAll(subItems);
            }
        }

        Minecraft.getMinecraft().player.closeScreen();
        Minecraft.getMinecraft().displayGuiScreen(new GUIScreenWikiRender(stacks.toArray(new ItemStack[0]), prefix, "wiki-block-renders-256", slotScale));
    }
}
