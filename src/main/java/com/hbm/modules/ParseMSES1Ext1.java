package com.hbm.modules;

import com.hbm.tileentity.network.RTTYSystem;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * MSES1-FEIS: Machine Script, Equestrian Standard - First Extended Instruction Set (v1.1)
 *
 * Additions compared to v1 Standard:
 * * Support for splitter chars, splitting and split count
 * * Support for the stack, a secondary buffer that can push, pop and peek
 * * Support for getting string length and substring using first and last
 * * Support for listening only to recent RoR signals using poll
 *
 * @author hbm
 */
public class ParseMSES1Ext1 extends ParseMSES1 {

    @Override
    public ReturnInfo eval(ParseContext ctx, String line, int index) {
        String lower = line.toLowerCase(Locale.US);

        switch (lower) {
            case "splitcount" -> {
                if (ctx.readBuffer().isEmpty()) return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer is empty");
                String[] frags = ctx.readBuffer().split(Pattern.quote(ctx.splitString));
                ctx.writeBuffer(frags.length + "");
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }

            case "push" -> {
                if (ctx.readBuffer().isEmpty()) return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer is empty");
                if (!ctx.push(ctx.readBuffer())) return new ReturnInfo(EnumStatementReturn.STACK_EXCEEDED, index, "Stack is full");
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }

            case "pop" -> {
                String val = ctx.pop();
                if (val == null) return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Stack is empty");
                ctx.writeBuffer(val);
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }

            case "peek" -> {
                String val = ctx.peek();
                if (val == null) return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Stack is empty");
                ctx.writeBuffer(val);
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }

            case "length" -> {
                ctx.writeBuffer("" + ctx.readBuffer().length());
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }

            case "worldtime" -> {
                ctx.writeBuffer("" + ctx.world.getTotalWorldTime());
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }
        }

        int space = line.indexOf(' ');
        if (space == -1) return super.eval(ctx, line, index);

        String command = lower.substring(0, space);
        String args = line.substring(space + 1);

        switch (command) {
            case "splitter", "split", "push", "first", "last", "poll" -> {
                if (args.isEmpty()) return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Expected an argument");
            }
            default -> {
                return super.eval(ctx, line, index);
            }
        }

        switch (command) {
            case "splitter" -> {
                ctx.splitString = substitute(ctx, args, false);
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }

            case "split" -> {
                try {
                    int idx = Integer.parseInt(substitute(ctx, args, true));
                    if (idx < 1) return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Fragment index starts at 1");
                    String[] frags = ctx.readBuffer().split(Pattern.quote(ctx.splitString));
                    if (idx > frags.length) return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Fragment index out of bounds");
                    ctx.writeBuffer(frags[idx - 1]);
                    return new ReturnInfo(EnumStatementReturn.OK, index);
                } catch (Throwable _) {
                    return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Not a valid number");
                }
            }

            case "push" -> {
                if (!ctx.push(substitute(ctx, args, false))) return new ReturnInfo(EnumStatementReturn.STACK_EXCEEDED, index, "Stack is full");
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }

            case "first" -> {
                try {
                    int length = Math.min(Integer.parseInt(substitute(ctx, args, true)), ctx.readBuffer().length());
                    if (length < 0) return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Length cannot be negative");
                    ctx.writeBuffer(ctx.readBuffer().substring(0, length));
                    return new ReturnInfo(EnumStatementReturn.OK, index);
                } catch (Exception _) {
                    return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Not a valid number");
                }
            }

            case "last" -> {
                try {
                    int max = ctx.readBuffer().length();
                    int length = Math.min(Integer.parseInt(substitute(ctx, args, true)), max);
                    if (length < 0) return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Length cannot be negative");
                    ctx.writeBuffer(ctx.readBuffer().substring(max - length, max));
                    return new ReturnInfo(EnumStatementReturn.OK, index);
                } catch (Exception _) {
                    return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Not a valid number");
                }
            }

            case "poll" -> {
                RTTYSystem.RTTYChannel chan = RTTYSystem.listen(ctx.world, substitute(ctx, args, false));
                if (chan != null && chan.timeStamp >= ctx.world.getTotalWorldTime() - 1) ctx.writeBuffer(chan.signal + "");
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }
        }

        return super.eval(ctx, line, index);
    }
}
