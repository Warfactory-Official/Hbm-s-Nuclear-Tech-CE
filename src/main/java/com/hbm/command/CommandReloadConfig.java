package com.hbm.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.hbm.config.RunningConfig.ConfigWrapper;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;

public abstract class CommandReloadConfig extends CommandBase {

    @Override
    public boolean checkPermission(@NotNull MinecraftServer server, @NotNull ICommandSender sender) {
        return sender instanceof EntityPlayer;
    }

    public abstract void help(ICommandSender sender, String[] args);
    public abstract HashMap<String, ConfigWrapper> getConfigMap();
    public abstract void refresh();
    public abstract void reload();
    public abstract String getTitle();

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {

        if(args.length < 1) throw new CommandException(getUsage(sender));

        String operator = args[0];

        if("help".equals(operator)) {
            help(sender, args);
            return;
        }

        if("list".equals(operator)) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + getTitle()));
            for(Entry<String, ConfigWrapper> line : getConfigMap().entrySet()) {
                sender.sendMessage(new TextComponentString("  " + TextFormatting.GOLD + line.getKey() + ": " + TextFormatting.YELLOW + line.getValue().value));
            }
            return;
        }

        if("reload".equals(operator)) {
            reload();
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Variables loaded from config file."));
            return;
        }

        if(args.length < 2) throw new CommandException(getUsage(sender));

        String key = args[1];

        if("get".equals(operator)) {
            ConfigWrapper wrapper = getConfigMap().get(key);
            if(wrapper == null) throw new CommandException("Key does not exist.");
            sender.sendMessage(new TextComponentString(TextFormatting.GOLD + key + ": " + TextFormatting.YELLOW + wrapper.value));
            return;
        }

        if(args.length < 3) throw new CommandException(getUsage(sender));

        String value = args[2];

        if("set".equals(operator)) {
            ConfigWrapper wrapper = getConfigMap().get(key);
            if(wrapper == null) throw new CommandException("Key does not exist.");

            try {
                wrapper.update(value);
                refresh();
                sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Value updated."));
            } catch(Exception ex) {
                throw new CommandException("Error parsing type for " + wrapper.value.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());
            }

            return;
        }

        throw new CommandException(getUsage(sender));
    }

    @Override
    public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
        if(!(sender instanceof EntityPlayer)) return Collections.emptyList();
        if(args.length < 1) return Collections.emptyList();
        if(args.length == 1) return getListOfStringsMatchingLastWord(args, "list", "reload", "get", "set");
        String operator = args[0];
        if(args.length == 2 && ("get".equals(operator) || "set".equals(operator))) {
            return getListOfStringsMatchingLastWord(args, getConfigMap().keySet());
        }
        return Collections.emptyList();
    }
}