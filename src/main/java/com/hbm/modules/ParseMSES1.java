package com.hbm.modules;

import com.hbm.config.ServerConfig;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.util.Calculator;

import java.util.Locale;

public class ParseMSES1 implements IParse {
    @Override
    public ReturnInfo eval(ParseContext ctx, String line, int index) {
        String lower = line.toLowerCase(Locale.US);

        // Skip newlines
        if (lower.isBlank()) return new ReturnInfo(EnumStatementReturn.SKIP, index);

        // runs the calculation from the buffer, allows string substitution, saves result to buffer
        switch (lower) {
            case "eval" -> {
                if (ctx.readBuffer().isEmpty()) return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer is empty");
                String statement = substitute(ctx, ctx.readBuffer(), true);
                try {
                    double result = Calculator.evaluateExpression(statement);
                    ctx.writeBuffer("" + result);
                } catch (Throwable _) { return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer does not have a valid expression"); }
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }

            case "evalr" -> {
                if (ctx.readBuffer().isEmpty()) return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer is empty");
                String statement = substitute(ctx, ctx.readBuffer(), true);
                try {
                    double result = Calculator.evaluateExpression(statement);
                    ctx.writeBuffer("" + (int) Math.round(result));
                } catch (Throwable _) { return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer does not have a valid expression"); }
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // rounds the buffer down to the nearest integer
            case "rounddown", "floor" -> {
                if (ctx.readBuffer().isEmpty()) return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer is empty");
                try {
                    double d = Double.parseDouble(ctx.readBuffer());
                    ctx.writeBuffer("" + (int) Math.floor(d));
                } catch (Exception _) { return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer does not have a valid number"); }
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // rounds the buffer up to the nearest integer
            case "roundup", "ceil" -> {
                if (ctx.readBuffer().isEmpty()) return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer is empty");
                try {
                    double d = Double.parseDouble(ctx.readBuffer());
                    ctx.writeBuffer("" + (int) Math.ceil(d));
                } catch (Exception _) { return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer does not have a valid number"); }
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // rounds the buffer to the nearest integer (.5 cutoff rule)
            case "round", "nearest" -> {
                if (ctx.readBuffer().isEmpty()) return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer is empty");
                try {
                    double d = Double.parseDouble(ctx.readBuffer());
                    ctx.writeBuffer("" + (int) Math.round(d));
                } catch (Exception _) { return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer does not have a valid number"); }
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }
        }

        int space = line.indexOf(' ');

        String command;
        String args;

        if (space == -1) {
            command = lower;
            args = "";
        } else {
            command = lower.substring(0, space);
            args = line.substring(space + 1);
        }

        if (!command.equals(lower) && line.length() <= command.length() + 1) return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Expected an argument");

        switch (command) {
            // jump point destination, skip
            case "dest", "#" -> {
                return new ReturnInfo(EnumStatementReturn.SKIP, index);
            }

            // no operation, still eats up a clock cycle
            case "nop" -> {
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // sets the desired clock speed, then skips the operation
            case "clockspeed" -> {
                try {
                    int speed = Integer.parseInt(args);
                    if (speed < 1 || speed > ServerConfig.AUTOCAL_MAX_CLOCK.get())
                        return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Invalid clock speed");
                    ctx.clockSpeed = speed;
                } catch (Throwable _) {
                    return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Not a valid number");
                }
                return new ReturnInfo(EnumStatementReturn.SKIP, index);
            }


            // sets the script index to the jump point
            case "jmp" -> {
                String jmpKey = substitute(ctx, args, false);
                if (ctx.jmp.containsKey(jmpKey)) {
                    ctx.current = ctx.jmp.get(jmpKey);
                    return new ReturnInfo(EnumStatementReturn.OK, index);
                }
                return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Could not find jump destination");
            }


            // sets the script index to the jump point, if the buffer is the string 'true'
            case "jmpif" -> {
                if (!ctx.readBuffer().equals("true")) return new ReturnInfo(EnumStatementReturn.OK, index);
                String jmpKey = substitute(ctx, args, false);
                if (ctx.jmp.containsKey(jmpKey)) {
                    ctx.current = ctx.jmp.get(jmpKey);
                    return new ReturnInfo(EnumStatementReturn.OK, index);
                }
                return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Could not find jump destination");
            }


            // sets the script index to the jump point, if the buffer is the NOT 'true'
            case "jmpnot" -> {
                if (ctx.readBuffer().equals("true")) return new ReturnInfo(EnumStatementReturn.OK, index);
                String jmpKey = substitute(ctx, args, false);
                if (ctx.jmp.containsKey(jmpKey)) {
                    ctx.current = ctx.jmp.get(jmpKey);
                    return new ReturnInfo(EnumStatementReturn.OK, index);
                }
                return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Could not find jump destination");
            }


            // ends the tick regardless of remaining clock cycles
            case "endtick" -> {
                return new ReturnInfo(EnumStatementReturn.END_TICK, index);
            }


            // requests unit to shut down
            case "shutdown" -> {
                return new ReturnInfo(EnumStatementReturn.SHUTDOWN, index);
            }


            // loads the requested variable into the buffer
            case "load" -> {
                ctx.writeBuffer(ctx.variables.getString(args));
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // saves the buffer with the specified name
            case "save" -> {
                if (ctx.readBuffer().isEmpty())
                    return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Cannot save variable, buffer is empty");
                ctx.variables.setString(args, ctx.readBuffer());
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // stores the specified value in the buffer
            case "buffer" -> {
                ctx.writeBuffer(args);
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // runs the calculation, allows string substitution, saves result to buffer
            case "eval" -> {
                String statement = substitute(ctx, args, true);
                try {
                    double result = Calculator.evaluateExpression(statement);
                    ctx.writeBuffer("" + result);
                } catch (Throwable _) {
                    return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Not a valid expression");
                }
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // runs the calculation, allows string substitution, rounds, saves result to buffer,
            case "evalr" -> {
                String statement = substitute(ctx, args, true);
                try {
                    double result = Calculator.evaluateExpression(statement);
                    ctx.writeBuffer("" + (int) Math.round(result));
                } catch (Throwable _) {
                    return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Not a valid expression");
                }
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // concatenate, same as buffer but evaluates $var$ substitutions
            case "concat" -> {
                if (ctx.readBuffer().isEmpty())
                    return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Cannot concat, buffer is empty");
                ctx.writeBuffer(substitute(ctx, args, false));
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // compares the buffer with a value, allows substitutions
            case "eq" -> {
                if (ctx.readBuffer().isEmpty())
                    return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer is empty");
                ctx.writeBuffer(ctx.readBuffer().equals(substitute(ctx, args, false)) ? "true" : "false");
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // greater than buffer
            case "gtb" -> {
                if (ctx.readBuffer().isEmpty())
                    return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer is empty");
                try {
                    double buffer = Double.parseDouble(ctx.readBuffer());
                    double val = Double.parseDouble(substitute(ctx, args, false));
                    ctx.writeBuffer(val > buffer ? "true" : "false");
                } catch (Exception _) {
                    return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Not a valid number");
                }
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // lower than buffer
            case "ltb" -> {
                if (ctx.readBuffer().isEmpty())
                    return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer is empty");
                try {
                    double buffer = Double.parseDouble(ctx.readBuffer());
                    double val = Double.parseDouble(substitute(ctx, args, false));
                    ctx.writeBuffer(val < buffer ? "true" : "false");
                } catch (Exception _) {
                    return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Not a valid number");
                }
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // greater than or equal buffer
            case "geb" -> {
                if (ctx.readBuffer().isEmpty())
                    return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer is empty");
                try {
                    double buffer = Double.parseDouble(ctx.readBuffer());
                    double val = Double.parseDouble(substitute(ctx, args, false));
                    ctx.writeBuffer(val >= buffer ? "true" : "false");
                } catch (Exception _) {
                    return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Not a valid number");
                }
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // lower than or equal buffer
            case "leb" -> {
                if (ctx.readBuffer().isEmpty())
                    return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Buffer is empty");
                try {
                    double buffer = Double.parseDouble(ctx.readBuffer());
                    double val = Double.parseDouble(substitute(ctx, args, false));
                    ctx.writeBuffer(val <= buffer ? "true" : "false");
                } catch (Exception _) {
                    return new ReturnInfo(EnumStatementReturn.PARAMETER_ERROR, index, "Not a valid number");
                }
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // sends an RoR signal using the buffer's contents as the message over the supplied channel
            case "send" -> {
                if (ctx.readBuffer().isEmpty())
                    return new ReturnInfo(EnumStatementReturn.UNDEFINED, index, "Cannot send, buffer is empty");
                RTTYSystem.broadcast(ctx.world, substitute(ctx, args, false), ctx.readBuffer());
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }


            // listens to an RoR signal using the supplied channel name and saves it to the buffer
            case "listen" -> {
                RTTYSystem.RTTYChannel chan = RTTYSystem.listen(ctx.world, substitute(ctx, args, false));
                if (chan != null) ctx.writeBuffer(chan.signal + "");
                return new ReturnInfo(EnumStatementReturn.OK, index);
            }
        }

        return new ReturnInfo(EnumStatementReturn.UNRECOGNIZED_COMMAND, index);
    }

    public String substitute(ParseContext ctx, String statement, boolean forceNumber) {
        if (!statement.contains("$")) return statement;

        statement = "¤" + statement + "¤";

        String[] frags = statement.split("\\$");
        if (frags.length % 2 == 0 || frags.length < 3) return statement.substring(1, statement.length() - 1);

        // since var names are enclosed with $ signs, we assume that every evenly numbered fragment is a var name
        // example: 5 + $val1$ * $val2$ / (-$val3$)
        // equals "5 + ", "val1", "* ", "val2", "/ (-", "val3", ")"
        //         1       2       3     4       5       6       7
        for(int i = 1; i < frags.length; i += 2) {
            // special case, if we try to substitute $buffer$ then read the literal buffer
            if (frags[i].equals("buffer")) frags[i] = ctx.readBuffer();
            else {
                String variable = ctx.variables.getString(frags[i]);
                // unset variables substitute as 0 in numeric contexts so expressions stay parseable
                if (forceNumber && variable.isEmpty()) variable = "0";
                frags[i] = variable;
            }
        }

        String ret = String.join("", frags);
        return ret.substring(1, ret.length() - 1);
    }

    @Override
    public void generateJumpPoints(ParseContext ctx, String line, int index) {
        if (line.startsWith("dest ") && line.length() > 5) {
            ctx.jmp.put(line.substring(5), index);
        }
    }
}
