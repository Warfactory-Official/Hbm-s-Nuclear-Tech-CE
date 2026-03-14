package com.hbm.api.redstoneoverradio;

public interface IRORInteractive extends IRORInfo {

    String NAME_SEPARATOR = "!";
    String PARAM_SEPARATOR = ":";

    String EX_NULL = "Exception: Null Command";
    String EX_NAME = "Exception: Multiple Name Separators";
    String EX_FORMAT = "Exception: Parameter in Invalid Format";

    /**
     * Runs a function on the ROR component, usually causing the component to change or do something. Returns are optional.
     */
    String runRORFunction(String name, String[] params);

    /**
     * Extracts the command name from a full command string
     */
    static String getCommand(String input) {
        if (input == null || input.isEmpty()) throw new RORFunctionException(EX_NULL);
        String[] parts = input.split(NAME_SEPARATOR);
        if (parts.length <= 0 || parts.length > 2) throw new RORFunctionException(EX_NAME);
        if (parts[0].isEmpty()) throw new RORFunctionException(EX_NULL);
        return parts[0];
    }

    /**
     * Extracts the param list from a full command string
     */
    static String[] getParams(String input) {
        if (input == null || input.isEmpty()) throw new RORFunctionException(EX_NULL);
        String[] parts = input.split(NAME_SEPARATOR);
        if (parts.length <= 0 || parts.length > 2) throw new RORFunctionException(EX_NAME);
        if (parts.length == 1) return new String[0];
        String paramList = parts[1];
        return paramList.split(PARAM_SEPARATOR);
    }

    static int parseInt(String val, int min, int max) {
        int result;
        try {
            result = Integer.parseInt(val);
        } catch (Exception x) {
            throw new RORFunctionException(EX_FORMAT);
        }
        if (result < min || result > max) throw new RORFunctionException(EX_FORMAT);
        return result;
    }
}
