package com.hbm.command;

import com.hbm.lib.HbmWorld;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.gen.nbt.SpawnCondition;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CommandLocate extends CommandBase {

    private static final int MAX_DISTANCE = 256;

    @Override
    public String getName() {
        return "ntmlocate";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return String.format(Locale.US,
                "%s/%s structure <name> %s- Locates the nearest structure with a given name.",
                TextFormatting.GREEN, getName(), TextFormatting.LIGHT_PURPLE
        );
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(!(sender instanceof EntityPlayer))
            throw new PlayerNotFoundException("");

        if(args.length != 2)
            throw new WrongUsageException(getUsage(sender));

        if(args[0].equals("structure")) {
            EntityPlayer player = (EntityPlayer) sender;

            SpawnCondition structure = NBTStructure.getStructure(args[1]);

            if(structure == null) {
                ITextComponent message = new TextComponentTranslation("commands.locate.no_match");
                message.getStyle().setColor(TextFormatting.RED);
                sender.sendMessage(message);
                return;
            }

            int chunkX = MathHelper.floor(player.posX) >> 4;
            int chunkZ = MathHelper.floor(player.posZ) >> 4;

            ChunkPos pos = getNearestLocationTo(structure, player.world, chunkX, chunkZ);

            if(pos == null) {
                ITextComponent message = new TextComponentTranslation("commands.locate.none_found");
                message.getStyle().setColor(TextFormatting.RED);
                sender.sendMessage(message);
                return;
            }

            ITextComponent message = new TextComponentTranslation("commands.locate.success.coordinates", structure.name, pos.x * 16, pos.z * 16);
            message.getStyle().setColor(TextFormatting.GREEN);
            sender.sendMessage(message);
        } else {
            throw new WrongUsageException(getUsage(sender));
        }
    }

    private ChunkPos getNearestLocationTo(SpawnCondition spawn, World world, int chunkX, int chunkZ) {
        if(HbmWorld.worldGenerator.getStructureAt(world, chunkX, chunkZ) == spawn)
            return new ChunkPos(chunkX, chunkZ);

        for(int radius = 1; radius < MAX_DISTANCE; radius++) {
            for(int x = chunkX - radius; x <= chunkX + radius; x++) {
                if(HbmWorld.worldGenerator.getStructureAt(world, x, chunkZ - radius) == spawn)
                    return new ChunkPos(x, chunkZ - radius);
                if(HbmWorld.worldGenerator.getStructureAt(world, x, chunkZ + radius) == spawn)
                    return new ChunkPos(x, chunkZ + radius);
            }
            for(int z = chunkZ - radius; z <= chunkZ + radius; z++) {
                if(HbmWorld.worldGenerator.getStructureAt(world, chunkX - radius, z) == spawn)
                    return new ChunkPos(chunkX - radius, z);
                if(HbmWorld.worldGenerator.getStructureAt(world, chunkX + radius, z) == spawn)
                    return new ChunkPos(chunkX + radius, z);
            }
        }

        return null;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if(args.length < 1)
            return Collections.emptyList();

        if(args.length == 1)
            return getListOfStringsMatchingLastWord(args, "structure");

        if(args.length == 2) {
            List<String> structures = NBTStructure.listStructures();
            return getListOfStringsMatchingLastWord(args, structures.toArray(new String[structures.size()]));
        }

        return Collections.emptyList();
    }

}
