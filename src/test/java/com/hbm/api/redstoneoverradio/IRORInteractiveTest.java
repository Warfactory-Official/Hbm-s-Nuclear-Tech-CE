package com.hbm.api.redstoneoverradio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IRORInteractiveTest {

    @Test
    void getCommandLowercasesInput() {
        assertEquals("setmode", IRORInteractive.getCommand("SetMode!2"));
        assertEquals("setmode", IRORInteractive.getCommand("SETMODE!2"));
        assertEquals("setactive", IRORInteractive.getCommand("setActive!1"));
        assertEquals("ventco2", IRORInteractive.getCommand("ventCO2"));
    }

    @Test
    void getCommandKeepsLowercaseCommandsUnchanged() {
        assertEquals("enqueue", IRORInteractive.getCommand("enqueue!1:2:3"));
    }

    @Test
    void getParamsDoesNotChangeCase() {
        assertArrayEquals(new String[] {"2"}, IRORInteractive.getParams("setmode!2"));
        assertArrayEquals(new String[] {"Player_One"}, IRORInteractive.getParams("addWhitelist!Player_One"));
        assertArrayEquals(new String[0], IRORInteractive.getParams("ventco2"));
    }

    @Test
    void getCommandAndParamsSplitEnqueue() {
        String coords = 12 + IRORInteractive.PARAM_SEPARATOR + 65 + IRORInteractive.PARAM_SEPARATOR + -300;
        String cmd = "enqueue" + IRORInteractive.NAME_SEPARATOR + coords;
        assertEquals("enqueue", IRORInteractive.getCommand(cmd));
        assertArrayEquals(new String[] {"12", "65", "-300"}, IRORInteractive.getParams(cmd));
    }

    @Test
    void invalidCommandsThrow() {
        assertThrows(RORFunctionException.class, () -> IRORInteractive.getCommand(null));
        assertThrows(RORFunctionException.class, () -> IRORInteractive.getCommand(""));
        assertThrows(RORFunctionException.class, () -> IRORInteractive.getCommand("!2"));
        assertThrows(RORFunctionException.class, () -> IRORInteractive.getCommand("a!b!c"));
    }

    @Test
    void parseIntEnforcesRangeAndRoundsDoubles() {
        assertEquals(2, IRORInteractive.parseInt("2", 0, 2));
        assertEquals(2, IRORInteractive.parseInt("2.0", 0, 2));
        assertEquals(1, IRORInteractive.parseInt("0.6", 0, 2));
        assertThrows(RORFunctionException.class, () -> IRORInteractive.parseInt("3", 0, 2));
        assertThrows(RORFunctionException.class, () -> IRORInteractive.parseInt("-1", 0, 2));
        assertThrows(RORFunctionException.class, () -> IRORInteractive.parseInt("abc", 0, 2));
        assertThrows(RORFunctionException.class, () -> IRORInteractive.parseInt("", 0, 2));
    }
}
