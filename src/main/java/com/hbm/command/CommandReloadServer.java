package com.hbm.command;

import java.util.HashMap;

import com.hbm.config.RunningConfig.ConfigWrapper;
import com.hbm.config.ServerConfig;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;

public class CommandReloadServer extends CommandReloadConfig {

    @Override
    public @NotNull String getName() {
        return "ntmserver";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "/ntmserver help";
    }

    @Override public void help(ICommandSender sender, String[] args) {
        if(args.length >= 2) {
            String command = args[1];
            if("help".equals(command)) sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Shows usage for /ntmserver subcommands."));
            if("list".equals(command)) sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Shows all server variable names and values."));
            if("reload".equals(command)) sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Reads server variables from the config file."));
            if("get".equals(command)) sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Shows value for the specified variable name."));
            if("set".equals(command)) sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Sets a variable's value and saves it to the config file."));
        } else {
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/ntmserver " + TextFormatting.GOLD + "help " + TextFormatting.RED + "<command>"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/ntmserver " + TextFormatting.GOLD + "list"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/ntmserver " + TextFormatting.GOLD + "reload"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/ntmserver " + TextFormatting.GOLD + "get " + TextFormatting.RED + "<name>"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/ntmserver " + TextFormatting.GOLD + "set " + TextFormatting.RED + "<name> <value>"));
        }
    }

    @Override public HashMap<String, ConfigWrapper> getConfigMap() { return ServerConfig.configMap; }
    @Override public void refresh() { ServerConfig.refresh(); }
    @Override public void reload() { ServerConfig.reload(); }
    @Override public String getTitle() { return "SERVER VARIABLES:"; }
}
