package com.hbm.command;

import java.util.HashMap;

import com.hbm.config.ClientConfig;
import com.hbm.config.RunningConfig.ConfigWrapper;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import org.jetbrains.annotations.NotNull;

public class CommandReloadClient extends CommandReloadConfig {

    public static void register() {
        if(FMLCommonHandler.instance().getSide() != Side.CLIENT) return;
        ClientCommandHandler.instance.registerCommand(new CommandReloadClient());
    }

    @Override
    public @NotNull String getName() {
        return "ntmclient";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "/ntmclient help";
    }

    @Override public void help(ICommandSender sender, String[] args) {
        if(args.length >= 2) {
            String command = args[1];
            if("help".equals(command)) sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Shows usage for /ntmclient subcommands."));
            if("list".equals(command)) sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Shows all client variable names and values."));
            if("reload".equals(command)) sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Reads client variables from the config file."));
            if("get".equals(command)) sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Shows value for the specified variable name."));
            if("set".equals(command)) sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Sets a variable's value and saves it to the config file."));
        } else {
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/ntmclient " + TextFormatting.GOLD + "help " + TextFormatting.RED + "<command>"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/ntmclient " + TextFormatting.GOLD + "list"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/ntmclient " + TextFormatting.GOLD + "reload"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/ntmclient " + TextFormatting.GOLD + "get " + TextFormatting.RED + "<name>"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/ntmclient " + TextFormatting.GOLD + "set " + TextFormatting.RED + "<name> <value>"));
        }
    }

    @Override public HashMap<String, ConfigWrapper> getConfigMap() { return ClientConfig.configMap; }
    @Override public void refresh() { ClientConfig.refresh(); }
    @Override public void reload() { ClientConfig.reload(); }
    @Override public String getTitle() { return "CLIENT VARIABLES:"; }
}
