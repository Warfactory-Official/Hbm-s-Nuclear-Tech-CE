package com.hbm.integration.groovy;

import com.cleanroommc.groovyscript.api.GroovyPlugin;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;
import com.cleanroommc.groovyscript.documentation.linkgenerator.LinkGeneratorHooks;
import com.hbm.Tags;
import com.hbm.util.Compat;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;

@Optional.Interface(iface = "com.cleanroommc.groovyscript.api.GroovyPlugin", modid = Compat.ModIds.GROOVY_SCRIPT)
public class GroovyScriptModule implements GroovyPlugin {

    @Override
    public @NotNull String getModId() {
        return Tags.MODID;
    }

    @Override
    public @NotNull String getContainerName() {
        return Tags.MODNAME;
    }

    @Override
    public GroovyPropertyContainer createGroovyPropertyContainer() {
        return new HbmGroovyPropertyContainer();
    }

    @Override
    public void onCompatLoaded(GroovyContainer<?> groovyContainer) {
        LinkGeneratorHooks.registerLinkGenerator(new NTMLinkGenerator());
    }
}
