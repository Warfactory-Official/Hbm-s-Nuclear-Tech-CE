package com.hbm.processor;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.FieldModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.reflect.AccessFlag;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VAORenderCallsitePluginTest {

    @Test
    void rewritesStaticFinalFieldAndLeavesMutableReceiverUntouched() throws Exception {
        LinkedHashMap<String, String> finalSources = baseSources();
        finalSources.put("testcases.FinalRegistry", """
                package testcases;

                import com.hbm.render.loader.WaveFrontObjectVAO;
                import testsupport.HFRWavefrontObject;
                import testsupport.ResourceLocation;

                public class FinalRegistry {
                    public static final WaveFrontObjectVAO MODEL =
                            new HFRWavefrontObject(new ResourceLocation("test", "models/final.obj")).asVBO();
                }
                """);
        finalSources.put("testcases.UsesFinal", """
                package testcases;

                public class UsesFinal {
                    public void run() {
                        FinalRegistry.MODEL.renderPart("Body");
                    }
                }
                """);

        CompilationResult finalResult = compile(finalSources, Map.of("models/final.obj", "g Body\n"));
        assertTrue(finalResult.success(), finalResult.output());

        ClassModel finalClass = parseClass(finalResult, "testcases.UsesFinal");
        FieldModel finalHandle = findField(finalClass, name -> name.equals("$VAO_MODEL_Body"));
        assertNotNull(finalHandle);
        assertTrue(finalHandle.flags().has(AccessFlag.PRIVATE));
        assertTrue(finalHandle.flags().has(AccessFlag.STATIC));
        assertTrue(finalHandle.flags().has(AccessFlag.FINAL),
                "synth handle should still be ACC_FINAL after the post-flow symbol flip");
        assertTrue(hasSideOnlyAnnotation(finalHandle, "CLIENT"),
                "synth handle should carry @SideOnly(Side.CLIENT) so Forge strips it on the dedicated server");

        List<Instruction> finalRun = instructions(findMethod(finalClass, "run", "()V"));
        assertTrue(hasFieldAccess(finalRun, Opcode.GETSTATIC, "testcases/UsesFinal", "$VAO_MODEL_Body"));
        assertTrue(hasInvoke(finalRun, Opcode.INVOKEVIRTUAL, "com/hbm/render/loader/GroupHandle", "render"));
        assertFalse(hasInvoke(finalRun, Opcode.INVOKEVIRTUAL, "com/hbm/render/loader/WaveFrontObjectVAO", "renderPart"));
        assertFalse(hasStringConstant(finalRun, "Body"));

        List<Instruction> finalClinit = instructions(findMethod(finalClass, "<clinit>", "()V"));
        assertTrue(hasInvoke(finalClinit, Opcode.INVOKESTATIC,
                "net/minecraftforge/fml/common/FMLCommonHandler", "instance"));
        assertTrue(hasInvoke(finalClinit, Opcode.INVOKEVIRTUAL,
                "net/minecraftforge/fml/common/FMLCommonHandler", "getSide"));
        assertTrue(hasFieldAccess(finalClinit, Opcode.GETSTATIC,
                "net/minecraftforge/fml/relauncher/Side", "CLIENT"));
        assertTrue(hasFieldAccess(finalClinit, Opcode.GETSTATIC, "testcases/FinalRegistry", "MODEL"));
        assertTrue(hasInvoke(finalClinit, Opcode.INVOKEVIRTUAL,
                "com/hbm/render/loader/WaveFrontObjectVAO", "resolve"));
        assertTrue(hasFieldAccess(finalClinit, Opcode.PUTSTATIC, "testcases/UsesFinal", "$VAO_MODEL_Body"));
        assertTrue(hasStringConstant(finalClinit, "Body"));

        LinkedHashMap<String, String> mutableSources = baseSources();
        mutableSources.put("testcases.MutableRegistry", """
                package testcases;

                import com.hbm.render.loader.WaveFrontObjectVAO;
                import testsupport.HFRWavefrontObject;
                import testsupport.ResourceLocation;

                public class MutableRegistry {
                    public static WaveFrontObjectVAO MODEL =
                            new HFRWavefrontObject(new ResourceLocation("test", "models/mutable.obj")).asVBO();
                }
                """);
        mutableSources.put("testcases.UsesMutable", """
                package testcases;

                public class UsesMutable {
                    public void run() {
                        MutableRegistry.MODEL.renderPart("Body");
                    }
                }
                """);

        CompilationResult mutableResult = compile(mutableSources, Map.of("models/mutable.obj", "g Body\n"));
        assertTrue(mutableResult.success(), mutableResult.output());

        ClassModel mutableClass = parseClass(mutableResult, "testcases.UsesMutable");
        assertFalse(hasField(mutableClass, name -> name.equals("$VAO_MODEL_Body")));

        List<Instruction> mutableRun = instructions(findMethod(mutableClass, "run", "()V"));
        assertTrue(hasFieldAccess(mutableRun, Opcode.GETSTATIC, "testcases/MutableRegistry", "MODEL"));
        assertTrue(hasStringConstant(mutableRun, "Body"));
        assertTrue(hasInvoke(mutableRun, Opcode.INVOKEVIRTUAL, "com/hbm/render/loader/WaveFrontObjectVAO", "renderPart"));
    }

    @Test
    void unreadableObjFailsCompilation() throws Exception {
        LinkedHashMap<String, String> sources = baseSources();
        sources.put("testcases.Registry", """
                package testcases;

                import com.hbm.render.loader.WaveFrontObjectVAO;
                import testsupport.HFRWavefrontObject;
                import testsupport.ResourceLocation;

                public class Registry {
                    public static final WaveFrontObjectVAO MODEL =
                            new HFRWavefrontObject(new ResourceLocation("test", "models/missing.obj")).asVBO();
                }
                """);
        sources.put("testcases.UsesRegistry", """
                package testcases;

                public class UsesRegistry {
                    public void run() {
                        Registry.MODEL.renderPart("Body");
                    }
                }
                """);

        CompilationResult result = compile(sources, Map.of());
        assertFalse(result.success(), "Expected compilation to fail.");
        assertTrue(result.output().contains("could not read OBJ 'models/missing.obj'"), result.output());
    }

    @Test
    void excludingAllGroupsFailsCompilation() throws Exception {
        LinkedHashMap<String, String> sources = baseSources();
        sources.put("testcases.Registry", """
                package testcases;

                import com.hbm.render.loader.WaveFrontObjectVAO;
                import testsupport.HFRWavefrontObject;
                import testsupport.ResourceLocation;

                public class Registry {
                    public static final WaveFrontObjectVAO MODEL =
                            new HFRWavefrontObject(new ResourceLocation("test", "models/all-excluded.obj")).asVBO();
                }
                """);
        sources.put("testcases.UsesRegistry", """
                package testcases;

                public class UsesRegistry {
                    public void run() {
                        Registry.MODEL.renderAllExcept("Body");
                    }
                }
                """);

        CompilationResult result = compile(sources, Map.of("models/all-excluded.obj", "g Body\n"));
        assertFalse(result.success(), "Expected compilation to fail.");
        assertTrue(result.output().contains("resolves to an empty render sequence"), result.output());
    }

    @Test
    void resolvesImportedOwnerWithoutSimpleNameCollision() throws Exception {
        LinkedHashMap<String, String> sources = baseSources();
        sources.put("alpha.ModelRegistry", """
                package alpha;

                import com.hbm.render.loader.WaveFrontObjectVAO;
                import testsupport.HFRWavefrontObject;
                import testsupport.ResourceLocation;

                public class ModelRegistry {
                    public static final WaveFrontObjectVAO MODEL =
                            new HFRWavefrontObject(new ResourceLocation("test", "models/alpha.obj")).asVBO();
                }
                """);
        sources.put("beta.ModelRegistry", """
                package beta;

                import com.hbm.render.loader.WaveFrontObjectVAO;
                import testsupport.HFRWavefrontObject;
                import testsupport.ResourceLocation;

                public class ModelRegistry {
                    public static final WaveFrontObjectVAO MODEL =
                            new HFRWavefrontObject(new ResourceLocation("test", "models/beta.obj")).asVBO();
                }
                """);
        sources.put("testcases.UsesImportedRegistry", """
                package testcases;

                import alpha.ModelRegistry;

                public class UsesImportedRegistry {
                    public void run() {
                        ModelRegistry.MODEL.renderPart("AlphaOnly");
                    }
                }
                """);

        CompilationResult result = compile(sources, Map.of(
                "models/alpha.obj", "g AlphaOnly\n",
                "models/beta.obj", "g BetaOnly\n"
        ));
        assertTrue(result.success(), result.output());

        ClassModel classModel = parseClass(result, "testcases.UsesImportedRegistry");
        FieldModel handle = findField(classModel, name -> name.equals("$VAO_MODEL_AlphaOnly"));
        assertNotNull(handle);

        List<Instruction> clinit = instructions(findMethod(classModel, "<clinit>", "()V"));
        assertTrue(hasInvoke(clinit, Opcode.INVOKESTATIC,
                "net/minecraftforge/fml/common/FMLCommonHandler", "instance"));
        assertTrue(hasFieldAccess(clinit, Opcode.GETSTATIC,
                "net/minecraftforge/fml/relauncher/Side", "CLIENT"));
        assertTrue(hasFieldAccess(clinit, Opcode.GETSTATIC, "alpha/ModelRegistry", "MODEL"));
        assertFalse(hasFieldAccess(clinit, Opcode.GETSTATIC, "beta/ModelRegistry", "MODEL"));
        assertTrue(hasInvoke(clinit, Opcode.INVOKEVIRTUAL, "com/hbm/render/loader/WaveFrontObjectVAO", "resolve"));
        assertTrue(hasStringConstant(clinit, "AlphaOnly"));
    }

    private static LinkedHashMap<String, String> baseSources() {
        LinkedHashMap<String, String> sources = new LinkedHashMap<>();
        sources.put("com.hbm.render.loader.GroupHandle", """
                package com.hbm.render.loader;

                public class GroupHandle {
                    public void render() {
                    }

                    public void bindAndDraw() {
                    }

                    public static void unbind() {
                    }
                }
                """);
        sources.put("com.hbm.render.loader.WaveFrontObjectVAO", """
                package com.hbm.render.loader;

                public class WaveFrontObjectVAO {
                    public GroupHandle resolve(String name) {
                        return new GroupHandle();
                    }

                    public void renderPart(String name) {
                    }

                    public void renderOnly(String... names) {
                    }

                    public void renderAllExcept(String... names) {
                    }
                }
                """);
        sources.put("testsupport.ResourceLocation", """
                package testsupport;

                public class ResourceLocation {
                    public ResourceLocation(String namespace, String path) {
                    }
                }
                """);
        sources.put("testsupport.HFRWavefrontObject", """
                package testsupport;

                import com.hbm.render.loader.WaveFrontObjectVAO;

                public class HFRWavefrontObject {
                    public HFRWavefrontObject(ResourceLocation location) {
                    }

                    public WaveFrontObjectVAO asVBO() {
                        return new WaveFrontObjectVAO();
                    }
                }
                """);
        sources.put("net.minecraftforge.fml.relauncher.Side", """
                package net.minecraftforge.fml.relauncher;

                public enum Side {
                    CLIENT, SERVER
                }
                """);
        sources.put("net.minecraftforge.fml.relauncher.SideOnly", """
                package net.minecraftforge.fml.relauncher;

                @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
                public @interface SideOnly {
                    Side value();
                }
                """);
        sources.put("net.minecraftforge.fml.common.FMLCommonHandler", """
                package net.minecraftforge.fml.common;

                import net.minecraftforge.fml.relauncher.Side;

                public class FMLCommonHandler {
                    private static final FMLCommonHandler INSTANCE = new FMLCommonHandler();
                    public static FMLCommonHandler instance() {
                        return INSTANCE;
                    }
                    public Side getSide() {
                        return Side.SERVER;
                    }
                }
                """);
        return sources;
    }

    private static CompilationResult compile(Map<String, String> sources,
                                             Map<String, String> resources) throws IOException {
        Path workDir = Files.createTempDirectory("vao-render-callsites");
        Path resourceRoot = Files.createDirectories(workDir.resolve("resources"));
        Path outputDir = Files.createDirectories(workDir.resolve("out"));

        ArrayList<Path> sourceFiles = new ArrayList<>(sources.size());
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            sourceFiles.add(writeSource(workDir, entry.getKey(), entry.getValue()));
        }
        for (Map.Entry<String, String> entry : resources.entrySet()) {
            writeResource(resourceRoot, entry.getKey(), entry.getValue());
        }

        List<String> command = new ArrayList<>();
        command.add(findJavac().toString());
        command.add("-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED");
        command.add("-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED");
        command.add("-J--add-exports=jdk.compiler/com.sun.tools.javac.resources=ALL-UNNAMED");
        command.add("-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED");
        command.add("-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED");
        command.add("--release");
        command.add("25");
        command.add("-proc:none");
        command.add("-Xplugin:inline-vao-render-callsites " + resourceRoot);
        command.add("-classpath");
        command.add(findProcessorJar().toString());
        command.add("-d");
        command.add(outputDir.toString());
        for (Path sourceFile : sourceFiles) {
            command.add(sourceFile.toString());
        }

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output;
        try {
            output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            process.destroyForcibly();
            throw e;
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for javac.", e);
        }

        return new CompilationResult(exitCode == 0, output, outputDir);
    }

    private static ClassModel parseClass(CompilationResult result, String className) throws IOException {
        return ClassFile.of().parse(classFilePath(result.outputDir(), className));
    }

    private static Path classFilePath(Path outputDir, String className) {
        return outputDir.resolve(className.replace('.', '/') + ".class");
    }

    private static @Nullable FieldModel findField(ClassModel classModel, Predicate<String> nameTest) {
        for (FieldModel field : classModel.fields()) {
            if (nameTest.test(field.fieldName().stringValue())) {
                return field;
            }
        }
        return null;
    }

    private static boolean hasField(ClassModel classModel, Predicate<String> nameTest) {
        return findField(classModel, nameTest) != null;
    }

    private static @Nullable MethodModel findMethod(ClassModel classModel, String name, String descriptor) {
        for (MethodModel method : classModel.methods()) {
            if (method.methodName().equalsString(name) && method.methodType().equalsString(descriptor)) {
                return method;
            }
        }
        return null;
    }

    private static List<Instruction> instructions(MethodModel method) {
        assertNotNull(method, "Method not found");
        var code = method.code().orElseThrow();
        ArrayList<Instruction> instructions = new ArrayList<>();
        for (var element : code) {
            if (element instanceof Instruction instruction) {
                instructions.add(instruction);
            }
        }
        return instructions;
    }

    private static boolean hasFieldAccess(List<Instruction> instructions, Opcode opcode, String owner, String fieldName) {
        for (Instruction instruction : instructions) {
            if (!(instruction instanceof FieldInstruction fieldInstruction)) continue;
            if (fieldInstruction.opcode() != opcode) continue;
            if (!fieldInstruction.owner().asInternalName().equals(owner)) continue;
            if (!fieldInstruction.name().equalsString(fieldName)) continue;
            return true;
        }
        return false;
    }

    private static boolean hasInvoke(List<Instruction> instructions, Opcode opcode, String owner, String methodName) {
        for (Instruction instruction : instructions) {
            if (!(instruction instanceof InvokeInstruction invokeInstruction)) continue;
            if (invokeInstruction.opcode() != opcode) continue;
            if (!invokeInstruction.owner().asInternalName().equals(owner)) continue;
            if (!invokeInstruction.name().equalsString(methodName)) continue;
            return true;
        }
        return false;
    }

    private static boolean hasStringConstant(List<Instruction> instructions, String value) {
        for (Instruction instruction : instructions) {
            if (!(instruction instanceof ConstantInstruction constantInstruction)) continue;
            if (value.equals(constantInstruction.constantValue())) return true;
        }
        return false;
    }

    private static boolean hasSideOnlyAnnotation(FieldModel field, String sideConstantName) {
        var attr = field.findAttribute(Attributes.runtimeVisibleAnnotations());
        if (attr.isEmpty()) return false;
        for (Annotation annotation : attr.get().annotations()) {
            if (!annotation.className().equalsString("Lnet/minecraftforge/fml/relauncher/SideOnly;")) continue;
            for (AnnotationElement element : annotation.elements()) {
                if (!element.name().equalsString("value")) continue;
                if (!(element.value() instanceof AnnotationValue.OfEnum enumValue)) continue;
                if (!enumValue.className().equalsString("Lnet/minecraftforge/fml/relauncher/Side;")) continue;
                if (enumValue.constantName().equalsString(sideConstantName)) return true;
            }
        }
        return false;
    }

    private static Path writeSource(Path baseDir, String className, String source) throws IOException {
        int lastDot = className.lastIndexOf('.');
        String packageName = lastDot == -1 ? "" : className.substring(0, lastDot);
        String simpleName = className.substring(lastDot + 1);
        Path packageDir = packageName.isEmpty() ? baseDir : baseDir.resolve(packageName.replace('.', '/'));
        Files.createDirectories(packageDir);
        Path sourceFile = packageDir.resolve(simpleName + ".java");
        Files.writeString(sourceFile, source, StandardCharsets.UTF_8);
        return sourceFile;
    }

    private static void writeResource(Path resourceRoot, String resourcePath, String content) throws IOException {
        Path file = resourceRoot.resolve(resourcePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }

    private static Path findJavac() {
        String javaCommand = ProcessHandle.current().info().command().orElse(null);
        assertNotNull(javaCommand, "Current JVM command is unavailable");
        Path java = Path.of(javaCommand);
        Path javac = java.resolveSibling(isWindows() ? "javac.exe" : "javac");
        assertTrue(Files.exists(javac), "javac not found next to current JVM at " + java);
        return javac;
    }

    private static Path findProcessorJar() {
        Path root = Path.of(System.getProperty("user.dir"));
        Path jar = root.resolve("processor").resolve("build").resolve("libs").resolve("processor.jar");
        assertTrue(Files.exists(jar), "processor.jar not found at " + jar);
        return jar;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private record CompilationResult(boolean success, String output, Path outputDir) {
    }
}
