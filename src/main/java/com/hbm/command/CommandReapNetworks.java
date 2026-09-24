package com.hbm.command;

import com.hbm.uninos.UniNodespace;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandReapNetworks extends CommandBase {

    @Override
    public String getName() {
        return "ntmreapnetworks";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/ntmreapnetworks";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {

        try {
            UniNodespace.clearNodespace();
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Nodespace cleared :)"));

        } catch(Exception ex) {
            sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "----------------------------------"));
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "An error has occoured during network reap, consult the log for details."));
            sender.sendMessage(new TextComponentString(TextFormatting.RED + ex.getLocalizedMessage()));
            sender.sendMessage(new TextComponentString(TextFormatting.RED + ex.getStackTrace()[0].toString()));
            sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "----------------------------------"));
            throw ex;
        }
    }
}
