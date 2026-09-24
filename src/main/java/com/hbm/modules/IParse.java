package com.hbm.modules;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashMap;

public interface IParse {
    ReturnInfo eval(ParseContext ctx, String line, int index);
    void generateJumpPoints(ParseContext ctx, String line, int index);

    class ParseContext {
        public static final int MAX_BUFFER_LENGTH = 256;
        public static final int MAX_STACK_SIZE = 256;

        public World world;
        public NBTTagCompound variables = new NBTTagCompound();
        public HashMap<String, Integer> jmp = new HashMap<>();

        private String buffer = "";
        private final String[] stack = new String[MAX_STACK_SIZE];
        private int stackSize = 0;
        public String splitString = ";";
        public int clockSpeed = 1;
        public int current = 0;

        public ParseContext(World world) {
            this.world = world;
            Arrays.fill(this.stack, "");
        }

        public String readBuffer() {
            return this.buffer;
        }

        /** Sets the buffer and imposes length restrictions. Returns true if successful, and false if truncation has taken place. */
        public boolean writeBuffer(String buffer) {
            if (buffer.length() > MAX_BUFFER_LENGTH) {
                this.buffer = buffer.substring(0, MAX_BUFFER_LENGTH);
                return false;
            }

            this.buffer = buffer;
            return true;
        }

        public boolean push(String line) {
            if (stackSize >= MAX_STACK_SIZE) return false;
            if (line.length() > MAX_BUFFER_LENGTH) line = line.substring(0, MAX_BUFFER_LENGTH);
            stack[stackSize] = line;
            stackSize++;
            return true;
        }

        public String pop() {
            if (stackSize <= 0) return null;
            if (stackSize > MAX_STACK_SIZE) stackSize = MAX_STACK_SIZE;
            String ret = stack[stackSize - 1];
            stack[stackSize - 1] = "";
            stackSize--;
            return ret;
        }

        public String peek() {
            if (stackSize <= 0) return null;
            if (stackSize > MAX_STACK_SIZE) stackSize = MAX_STACK_SIZE;
            return stack[stackSize - 1];
        }

        public void turnOff() {
            this.clockSpeed = 1;
            this.current = 0;
            this.buffer = "";
            if (!this.variables.isEmpty()) this.variables = new NBTTagCompound();
        }

        public void readFromNBT(NBTTagCompound nbt, String[] script, IParse parser) {
            current = nbt.getInteger("current");
            clockSpeed = nbt.getInteger("clockSpeed");
            buffer = nbt.getString("buffer");
            splitString = nbt.getString("splitString");
            variables = nbt.getCompoundTag("variables");

            stackSize = Math.max(0, Math.min(MAX_STACK_SIZE, nbt.getInteger("stackSize")));
            for (int i = 0; i < stackSize; i++) stack[i] = nbt.getString("st" + i);
            for (int i = 0; i < script.length; i++) parser.generateJumpPoints(this, script[i], i);
        }

        public void writeToNBT(NBTTagCompound nbt) {
            nbt.setInteger("current", current);
            nbt.setInteger("clockSpeed", clockSpeed);
            nbt.setString("buffer", buffer);
            nbt.setString("splitString", splitString);
            nbt.setTag("variables", variables);

            nbt.setInteger("stackSize", stackSize);
            for (int i = 0; i < stackSize; i++) nbt.setString("st" + i, stack[i]);
        }
    }

    // Not in 1.7.10 but this does not affect anything except making mses1 easier to debug
    record ReturnInfo(EnumStatementReturn type, int line, String extraInfo) {
        public ReturnInfo(EnumStatementReturn type, int line) {
            this(type, line, "");
        }
    }

    enum EnumStatementReturn {
        /** The command executed correctly (more or less) */
        OK,
        /** The command hasn't been recognized */
        UNRECOGNIZED_COMMAND,
        /** The expected parameters aren't present, or the parameters couldn't be parsed (i.e. using an undefined jump point) */
        PARAMETER_ERROR,
        /** Requests the AUTOCAL unit to end the tick, regardless of how many clock cycles are left */
        END_TICK,
        /** Requests an AUTOCAL shutdown */
        SHUTDOWN,
        /** Skips the instruction, doesn't use up a clock cycle */
        SKIP,
        /** General undefined behavior */
        UNDEFINED,
        /** Stack ran full */
        STACK_EXCEEDED
    }
}
