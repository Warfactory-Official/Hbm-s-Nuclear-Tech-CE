package com.hbm.integration.groovy;

import com.cleanroommc.groovyscript.documentation.linkgenerator.BasicLinkGenerator;
import com.hbm.Tags;

public class NTMLinkGenerator extends BasicLinkGenerator {
    @Override
    public String id() {
        return Tags.MODID;
    }

    @Override
    protected String version() {
        return Tags.VERSION;
    }

    @Override
    protected String domain() {
        return "https://github.com/MisterNorwood/Hbm-s-Nuclear-Tech-CE/";
    }
}
