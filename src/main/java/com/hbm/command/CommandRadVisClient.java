package com.hbm.command;

import com.hbm.handler.radiation.RadVisOverlay;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.IClientCommand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class CommandRadVisClient extends CommandBase implements IClientCommand {

    @Override
    public String getName() {
        return "radvis";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return """
                Usage:
                 /radvis on|off
                 /radvis radius <rChunks>
                 /radvis mode <wire|slice|faces|state|errors>
                 /radvis y <auto|0-255>
                 /radvis depth <on|off>
                 /radvis alpha <0.0-1.0>
                 /radvis verify <on|off>
                 /radvis verifyInterval <ticks>
                 /radvis here
                 /radvis filterBox <dx> <dy> <dz>
                """;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(new TextComponentString(getUsage(sender)));
            return;
        }

        RadVisOverlay.Config cfg = RadVisOverlay.CONFIG;
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "on" -> {
                cfg.enabled = true;
                sender.sendMessage(new TextComponentString("RadVis enabled."));
            }
            case "off" -> {
                cfg.enabled = false;
                sender.sendMessage(new TextComponentString("RadVis disabled."));
            }
            case "radius" -> {
                if (args.length < 2) {
                    sender.sendMessage(new TextComponentString(getUsage(sender)));
                    return;
                }
                Integer r = parseIntSafe(args[1]);
                if (r == null) {
                    sender.sendMessage(new TextComponentString("Invalid radius: " + args[1]));
                    return;
                }
                r = Math.max(0, Math.min(8, r));
                cfg.radiusChunks = r;
                sender.sendMessage(new TextComponentString("RadVis radius set to " + r + " chunks."));
            }
            case "mode" -> {
                if (args.length < 2) {
                    sender.sendMessage(new TextComponentString(getUsage(sender)));
                    return;
                }
                RadVisOverlay.Mode mode = parseMode(args[1]);
                if (mode == null) {
                    sender.sendMessage(new TextComponentString("Unknown mode: " + args[1]));
                    return;
                }
                cfg.mode = mode;
                sender.sendMessage(new TextComponentString("RadVis mode set to " + mode.name().toLowerCase() + "."));
            }
            case "y" -> {
                if (args.length < 2) {
                    sender.sendMessage(new TextComponentString(getUsage(sender)));
                    return;
                }
                if ("auto".equalsIgnoreCase(args[1])) {
                    cfg.sliceAutoY = true;
                    sender.sendMessage(new TextComponentString("RadVis slice Y set to auto."));
                } else {
                    Integer y = parseIntSafe(args[1]);
                    if (y == null) {
                        sender.sendMessage(new TextComponentString("Invalid Y: " + args[1]));
                        return;
                    }
                    y = Math.max(0, Math.min(255, y));
                    cfg.sliceAutoY = false;
                    cfg.sliceY = y;
                    sender.sendMessage(new TextComponentString("RadVis slice Y set to " + y + "."));
                }
            }
            case "depth" -> {
                if (args.length < 2) {
                    sender.sendMessage(new TextComponentString(getUsage(sender)));
                    return;
                }
                Boolean v = parseToggle(args[1]);
                if (v == null) {
                    sender.sendMessage(new TextComponentString("Invalid depth value: " + args[1]));
                    return;
                }
                cfg.depth = v;
                sender.sendMessage(new TextComponentString("RadVis depth " + (v ? "on" : "off") + "."));
            }
            case "alpha" -> {
                if (args.length < 2) {
                    sender.sendMessage(new TextComponentString(getUsage(sender)));
                    return;
                }
                Float v = parseFloatSafe(args[1]);
                if (v == null) {
                    sender.sendMessage(new TextComponentString("Invalid alpha: " + args[1]));
                    return;
                }
                v = Math.max(0.0f, Math.min(1.0f, v));
                cfg.alpha = v;
                sender.sendMessage(new TextComponentString("RadVis alpha set to " + v + "."));
            }
            case "verify" -> {
                if (args.length < 2) {
                    sender.sendMessage(new TextComponentString(getUsage(sender)));
                    return;
                }
                Boolean v = parseToggle(args[1]);
                if (v == null) {
                    sender.sendMessage(new TextComponentString("Invalid verify value: " + args[1]));
                    return;
                }
                cfg.verify = v;
                sender.sendMessage(new TextComponentString("RadVis verify " + (v ? "on" : "off") + "."));
            }
            case "verifyinterval" -> {
                if (args.length < 2) {
                    sender.sendMessage(new TextComponentString(getUsage(sender)));
                    return;
                }
                Integer v = parseIntSafe(args[1]);
                if (v == null || v < 1) {
                    sender.sendMessage(new TextComponentString("Invalid verifyInterval: " + args[1]));
                    return;
                }
                cfg.verifyInterval = v;
                sender.sendMessage(new TextComponentString("RadVis verifyInterval set to " + v + " ticks."));
            }
            case "here" -> {
                if (sender instanceof EntityPlayer player) {
                    cfg.focusAnchor = player.getPosition();
                    cfg.focusEnabled = true;
                    sender.sendMessage(new TextComponentString("RadVis focus anchor set to your position."));
                } else {
                    sender.sendMessage(new TextComponentString("RadVis focus anchor requires a player."));
                }
            }
            case "filterbox" -> {
                if (args.length < 4) {
                    sender.sendMessage(new TextComponentString(getUsage(sender)));
                    return;
                }
                Integer dx = parseIntSafe(args[1]);
                Integer dy = parseIntSafe(args[2]);
                Integer dz = parseIntSafe(args[3]);
                if (dx == null || dy == null || dz == null) {
                    sender.sendMessage(new TextComponentString("Invalid filterBox values."));
                    return;
                }
                dx = Math.max(0, dx);
                dy = Math.max(0, dy);
                dz = Math.max(0, dz);
                cfg.focusDx = dx;
                cfg.focusDy = dy;
                cfg.focusDz = dz;
                if (cfg.focusAnchor == null && sender instanceof EntityPlayer player) {
                    cfg.focusAnchor = player.getPosition();
                }
                cfg.focusEnabled = !(dx == 0 && dy == 0 && dz == 0);
                sender.sendMessage(new TextComponentString("RadVis filterBox set to " + dx + " " + dy + " " + dz + "."));
            }
            default -> sender.sendMessage(new TextComponentString(getUsage(sender)));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "on", "off", "radius", "mode", "y", "depth", "alpha", "verify", "verifyInterval", "here",
                    "filterBox");
        }
        if (args.length == 2 && "mode".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, "wire", "slice", "faces", "state", "errors");
        }
        if (args.length == 2 && ("depth".equalsIgnoreCase(args[0]) || "verify".equalsIgnoreCase(args[0]))) {
            return getListOfStringsMatchingLastWord(args, "on", "off");
        }
        if (args.length == 2 && "y".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, "auto");
        }
        return Collections.emptyList();
    }

    private static Boolean parseToggle(String s) {
        if ("on".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s)) return true;
        if ("off".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) return false;
        return null;
    }

    private static Integer parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Float parseFloatSafe(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static RadVisOverlay.Mode parseMode(String s) {
        return switch (s.toLowerCase()) {
            case "wire" -> RadVisOverlay.Mode.WIRE;
            case "slice" -> RadVisOverlay.Mode.SLICE;
            case "faces" -> RadVisOverlay.Mode.FACES;
            case "state" -> RadVisOverlay.Mode.STATE;
            case "errors" -> RadVisOverlay.Mode.ERRORS;
            default -> null;
        };
    }
}
