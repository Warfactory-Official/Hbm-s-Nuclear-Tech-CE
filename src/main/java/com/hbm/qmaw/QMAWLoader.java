package com.hbm.qmaw;

import com.google.gson.*;
import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Spaghetti("everything here is a fucking mess")
public class QMAWLoader implements ISelectiveResourceReloadListener {

    public static final HashSet<FileResourcePack> modResourcePacks = new HashSet<>();
    public static final HashSet<FolderResourcePack> folderResourcePacks = new HashSet<>();
    public static final Gson gson = new Gson();
    public static final JsonParser parser = new JsonParser();
    public static HashMap<String, QuickManualAndWiki> qmaw = new HashMap<>();
    public static HashMap<ComparableStack, QuickManualAndWiki> triggers = new HashMap<>();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        //mlbv: only reload on a complete resource reload
        for (VanillaResourceType type : VanillaResourceType.values()) {
            if (!resourcePredicate.test(type)) return;
        }
        long timestamp = System.currentTimeMillis();
        MainRegistry.logger.info("[QMAW] Reloading manual...");
        init();
        MainRegistry.logger.info("[QMAW] Loaded " + qmaw.size() + " manual entries! (" + (System.currentTimeMillis() - timestamp) + "ms)");
    }

    /** Searches the asset folder for QMAW format JSON files and adds entries based on that */
    public static void init() {
        //Force loads ALL packs. you have no say in this
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            File src = mod.getSource();
            if (src.isFile()) {
                modResourcePacks.add(new FileResourcePack(src));
            } if (src.isDirectory()) {
                folderResourcePacks.add(new FolderResourcePack(src));
            }
        }

        qmaw.clear();
        triggers.clear();
        agonyEngine();
    }

    /** "digital equivalent to holywater" yielded few results on google, if only i had the answer i would drown this entire class in it <br><br>
     * This affront to god can load QMAW definition files from four different sources:<br>
     * * Any mod's jar that has registered itself to include QMAW files<br>
     * * The dev environment, because "the mod file" would in this case be this very class file, and that's incorrect<br>
     * * ZIP-based resource packs<br>
     * * Folder-based resource packs
     * */
    public static void agonyEngine() {

        // I should stop accepting random PRs when in gym man...
        // Turns out ObfuscationReflectionHelper wasnt even needed. Thanks AccessTransformer!!
        for (FileResourcePack modResourcePack : modResourcePacks) {
            try {
                File file = ((AbstractResourcePack) modResourcePack).resourcePackFile;
                if (file != null) {
                    dissectZip(file);
                } else {
                    MainRegistry.logger.warn("[QMAWLoader] resourcePackFile is null for " + modResourcePack.getPackName());
                }
            } catch (Exception e) {
                MainRegistry.logger.error("[QMAWLoader] Failed to access private field for " + modResourcePack.getPackName() + ". THIS IS A BUG!", e);
            }
        }
        for (FolderResourcePack modResourcePack : folderResourcePacks) {
            try {
                File file = ((AbstractResourcePack) modResourcePack).resourcePackFile;
                if (file != null) {
                    dissectFolder(file);
                } else {
                    MainRegistry.logger.warn("[QMAWLoader] resourcePackFile is null for " + modResourcePack.getPackName());
                }
            } catch (Exception e) {
                MainRegistry.logger.error("[QMAWLoader] Failed to access private field for " + modResourcePack.getPackName() + ". THIS IS A BUG!", e);
            }
        }

        try {
            File devEnvManualFolder = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath().replace("/eclipse/.", "") + "/src/main/resources/assets/hbm/manual");
            if (devEnvManualFolder.exists() && devEnvManualFolder.isDirectory()) {
                MainRegistry.logger.info("[QMAW] Exploring " + devEnvManualFolder.getAbsolutePath());
                dissectManualFolder(devEnvManualFolder);
            }
        } catch (Exception e) {
            MainRegistry.logger.error("[QMAW] Failed to explore dev environment manual folder!", e);
        }

        try {
            ResourcePackRepository repo = Minecraft.getMinecraft().getResourcePackRepository();

            for (ResourcePackRepository.Entry entry : repo.getRepositoryEntries()) {
                IResourcePack pack = entry.getResourcePack();
                logPackAttempt(pack.getPackName());

                if (pack instanceof FileResourcePack) {
                    try {
                        File file = ((AbstractResourcePack) pack).resourcePackFile;
                        if (file != null) {
                            dissectZip(file);
                        } else {
                            MainRegistry.logger.warn("[QMAWLoader] resourcePackFile is null for " + pack.getPackName());
                        }
                    } catch (Exception e) {
                        MainRegistry.logger.error("[QMAWLoader] Failed to dissect FileResourcePack for " + pack.getPackName(), e);
                    }
                }

                if (pack instanceof FolderResourcePack) {
                    try {
                        File file = ((AbstractResourcePack) pack).resourcePackFile;
                        if (file != null) {
                            dissectFolder(file);
                        } else {
                            MainRegistry.logger.warn("[QMAWLoader] resourcePackFile is null for " + pack.getPackName());
                        }
                    } catch (Exception e) {
                        MainRegistry.logger.error("[QMAWLoader] Failed to dissect FolderResourcePack for " + pack.getPackName(), e);
                    }
                }
            }
        } catch (Exception e) {
            MainRegistry.logger.error("[QMAWLoader] Failed to process resource pack repository", e);
        }
    }


    public static void logPackAttempt(String name) { MainRegistry.logger.info("[QMAW] Dissecting resource " + name); }
    public static void logFoundManual(String name) { MainRegistry.logger.info("[QMAW] Found manual " + name); }

    /** You put your white gloves on, you get your hand in there, and then you iterate OVER THE ENTIRE FUCKING ZIP until we find things we deem usable */
    public static void dissectZip(File zipFile) {

        if(zipFile == null) {
            MainRegistry.logger.info("[QMAW] Pack file does not exist!");
            return;
        }

        ZipFile zip = null;

        try {
            zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> enumerator = zip.entries();

            while(enumerator.hasMoreElements()) {
                ZipEntry entry = enumerator.nextElement();
                String name = entry.getName();
                if(name.startsWith("assets/hbm/manual/") && name.endsWith(".json")) {
                    InputStream fileStream = zip.getInputStream(entry);
                    InputStreamReader reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8);
                    try {
                        JsonObject obj = (JsonObject) parser.parse(reader);
                        String manName = name.replace("assets/hbm/manual/", "");
                        registerJson(manName, obj);
                        reader.close();
                        logFoundManual(manName);
                    } catch(Exception ex) {
                        MainRegistry.logger.info("[QMAW] Error reading manual " + name + ": " + ex);
                    }
                }
            }

        } catch(Exception ex) {
            MainRegistry.logger.info("[QMAW] Error dissecting zip " + zipFile.getName() + ": " + ex);
        } finally {
            try {
                if(zip != null) zip.close();
            } catch(Exception ex) { }
        }
    }

    /** Opens a resource pack folder, skips to the manual folder, then tries to dissect that */
    public static void dissectFolder(File folder) {
        File manualFolder = new File(folder, "/assets/manual");
        if(manualFolder.exists() && manualFolder.isDirectory()) dissectManualFolder(manualFolder);
    }

    /** Anal bleeding */
    public static void dissectManualFolder(File folder) {

        File[] files = folder.listFiles();
        for(File file : files) {
            String name = file.getName();
            if(file.isFile() && name.endsWith(".json")) {
                try {
                    Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                    JsonObject obj = (JsonObject) parser.parse(reader);
                    registerJson(name, obj);
                    logFoundManual(name);
                } catch(Exception ex) {
                    MainRegistry.logger.info("[QMAW] Error reading manual " + name + ": " + ex);
                }
            } else if(file.isDirectory()) {
                dissectManualFolder(file); // scrape subfolders too lmao
            }
        }
    }

    /** Extracts all the info from a json file's main object to add a QMAW to the system. Very barebones, only handles name, icon and the localized text. */
    public static void registerJson(String file, JsonObject json) {

        String name = json.get("name").getAsString();
        QuickManualAndWiki qmaw = new QuickManualAndWiki(name);

        if(json.has("icon")) {
            qmaw.setIcon(SerializableRecipe.readItemStack(json.get("icon").getAsJsonArray()));
        }

        JsonObject title = json.get("title").getAsJsonObject();
        for(Entry<String, JsonElement> part : title.entrySet()) {
            qmaw.addTitle(part.getKey(), part.getValue().getAsString());
        }

        JsonObject content = json.get("content").getAsJsonObject();
        for(Entry<String, JsonElement> part : content.entrySet()) {
            qmaw.addLang(part.getKey(), part.getValue().getAsString());
        }

        JsonArray triggers = json.get("trigger").getAsJsonArray();

        for(JsonElement element : triggers) {
            ItemStack trigger = SerializableRecipe.readItemStack(element.getAsJsonArray());
            // items get renamed and removed all the time, so we add some more debug goodness for those cases
            if(trigger == null || trigger.getItem() == ModItems.nothing) {
                MainRegistry.logger.info("[QMAW] Manual " + file + " references nonexistant trigger " + element);
            } else {
                QMAWLoader.triggers.put(new ComparableStack(trigger).makeSingular(), qmaw);
            }
        }

        if(!qmaw.contents.isEmpty()) {
            QMAWLoader.qmaw.put(name, qmaw);
        }
    }
}