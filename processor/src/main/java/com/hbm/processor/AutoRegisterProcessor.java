package com.hbm.processor;

import com.google.auto.service.AutoService;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.AutoRegisterContainer;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.hbm.interfaces.AutoRegister", "com.hbm.interfaces.AutoRegisterContainer"})
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public final class AutoRegisterProcessor extends AbstractProcessor {

    private static final String TESR_FQN = "net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer";
    private static final String TE_FQN = "net.minecraft.tileentity.TileEntity";
    private static final String ENTITY_FQN = "net.minecraft.entity.Entity";
    private static final String ICONFIGURABLE_FQN = "com.hbm.tileentity.IConfigurableMachine";
    private static final String RENDER_FQN = "net.minecraft.client.renderer.entity.Render";
    private static final String TEISR_FQN = "net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer";
    private static final String OBJECT_FQN = "java.lang.Object";

    private static final Pattern TE_PATTERN = Pattern.compile("^TileEntity");
    private static final Pattern TE_NAME_PATTERN = Pattern.compile("([a-z])([A-Z])");

    private final Map<String, EntityInfo> entitiesByName = new HashMap<>(256);
    private final Map<String, ClassName> entityNameOwners = new HashMap<>(256);

    private final Map<ClassName, String> tileEntities = new HashMap<>(512);
    private final Map<String, ClassName> tileEntityIdOwners = new HashMap<>(512);

    private final Map<ClassName, EntityRendererInfo> entityRenderersByEntity = new HashMap<>(256);
    private final Map<ClassName, ClassName> tileEntityRenderers = new HashMap<>(512);
    private final Map<String, TeisrInfo> itemRenderersByItemField = new HashMap<>(128);

    private final Set<ClassName> configurableMachines = new HashSet<>(64);

    private Filer filer;
    private Messager messager;
    private Types typeUtils;
    private Elements elementUtils;

    private TypeElement tesrElement;
    private TypeElement renderElement;

    private TypeMirror teisrType;
    private TypeMirror teType;
    private TypeMirror entityType;
    private TypeMirror configurableType;
    private TypeMirror objectType;

    private static String generateRegistrationId(String name) {
        name = TE_PATTERN.matcher(name).replaceFirst("");
        return "tileentity_" + TE_NAME_PATTERN.matcher(name).replaceAll("$1_$2").toLowerCase(Locale.ROOT);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();

        tesrElement = elementUtils.getTypeElement(TESR_FQN);
        renderElement = elementUtils.getTypeElement(RENDER_FQN);

        teisrType = typeMirrorOrNull(TEISR_FQN);
        teType = typeMirrorOrNull(TE_FQN);
        entityType = typeMirrorOrNull(ENTITY_FQN);
        configurableType = typeMirrorOrNull(ICONFIGURABLE_FQN);
        objectType = typeMirrorOrNull(OBJECT_FQN);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var annotatedElements = new LinkedHashSet<Element>(64);
        annotatedElements.addAll(roundEnv.getElementsAnnotatedWith(AutoRegister.class));
        annotatedElements.addAll(roundEnv.getElementsAnnotatedWith(AutoRegisterContainer.class));

        for (var element : annotatedElements) {
            if (!(element instanceof TypeElement typeElement)) continue;
            if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) continue;

            for (var annotation : typeElement.getAnnotationsByType(AutoRegister.class)) {
                try {
                    processAnnotation(typeElement, annotation);
                } catch (Exception e) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Could not process @AutoRegister: " + e.getMessage(), element);
                }
            }
        }

        if (roundEnv.processingOver() && hasAnyOutput()) {
            try {
                generateRegistrarFile();
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Failed to generate registrar file: " + e.getMessage());
            }
        }

        return true;
    }

    private boolean hasAnyOutput() {
        return !entitiesByName.isEmpty() || !tileEntities.isEmpty() || !entityRenderersByEntity.isEmpty() || !tileEntityRenderers.isEmpty() || !itemRenderersByItemField.isEmpty() || !configurableMachines.isEmpty();
    }

    private void processAnnotation(TypeElement annotatedElement, AutoRegister annotation) {
        var annotatedClass = ClassName.get(annotatedElement);

        // TESR<TileEntity>
        if (isSubtypeErased(annotatedElement, tesrElement)) {
            var teMirror = typeMirrorFromAnnotation(annotation, MirrorKind.TILEENTITY);
            var teArg = selectOrInferTypeArg(annotatedElement, tesrElement, teMirror, teType, "TileEntity", "tileentity");
            if (teArg == null) return;

            var teClass = classNameFromTypeMirror(teArg);
            if (teClass == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Invalid 'tileentity' type in @AutoRegister.", annotatedElement);
                return;
            }

            var prev = tileEntityRenderers.putIfAbsent(teClass, annotatedClass);
            if (prev != null && !prev.equals(annotatedClass)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Duplicate TESR registration: " + teClass.canonicalName() + " already has renderer " + prev.canonicalName() + ", cannot also register " + annotatedClass.canonicalName() + ".", annotatedElement);
            }
            return;
        }

        // Render<Entity>
        if (isSubtypeErased(annotatedElement, renderElement)) {
            var entMirror = typeMirrorFromAnnotation(annotation, MirrorKind.ENTITY);
            var entArg = selectOrInferTypeArg(annotatedElement, renderElement, entMirror, entityType, "Entity", "entity");
            if (entArg == null) return;

            var entityClass = classNameFromTypeMirror(entArg);
            if (entityClass == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Invalid 'entity' type in @AutoRegister.", annotatedElement);
                return;
            }

            var factory = annotation.factory().trim();
            var info = new EntityRendererInfo(entityClass, annotatedClass, factory);

            var prev = entityRenderersByEntity.putIfAbsent(entityClass, info);
            if (prev != null && !prev.rendererType().equals(annotatedClass)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Duplicate entity renderer registration: " + entityClass.canonicalName() + " already has renderer " + prev.rendererType().canonicalName() + ", cannot also register " + annotatedClass.canonicalName() + ".", annotatedElement);
            }
            return;
        }

        // TEISR
        if (isSubtypeErased(annotatedElement, teisrType)) {
            var itemField = annotation.item().trim();
            if (itemField.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "A TEISR class must specify the 'item' parameter in its @AutoRegister annotation.", annotatedElement);
                return;
            }

            boolean hasArrayArgs = annotation.constructorArgs().length > 0;
            boolean hasStringArgs = !annotation.constructorArgsString().isBlank();
            boolean hasInstanceField = !annotation.instanceField().isBlank();

            if (hasArrayArgs && hasStringArgs) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Cannot use both 'constructorArgs' and 'constructorArgsString'. Please use only one.", annotatedElement);
                return;
            }
            if (hasInstanceField && (hasArrayArgs || hasStringArgs)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Cannot use constructor args when 'instanceField' is set. Please use only one approach.", annotatedElement);
                return;
            }

            var ctorArgs = hasStringArgs ? annotation.constructorArgsString().trim() : String.join(", ", annotation.constructorArgs()).trim();
            var instanceField = annotation.instanceField().trim();

            var info = new TeisrInfo(annotatedClass, itemField, ctorArgs, instanceField);

            var prev = itemRenderersByItemField.putIfAbsent(itemField, info);
            if (prev != null && !prev.rendererType().equals(annotatedClass)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Duplicate TEISR registration: item field '" + itemField + "' already uses renderer " + prev.rendererType().canonicalName() + ", cannot also register " + annotatedClass.canonicalName() + ".", annotatedElement);
            }
            return;
        }

        // TileEntity
        if (isSubtypeErased(annotatedElement, teType)) {
            var annoName = annotation.name().trim();
            var regId = annoName.isEmpty() ? generateRegistrationId(annotatedElement.getSimpleName().toString()) : annoName;

            var regOwner = tileEntityIdOwners.putIfAbsent(regId, annotatedClass);
            if (regOwner != null && !regOwner.equals(annotatedClass)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Duplicate TileEntity registration id '" + regId + "' used by " + regOwner.canonicalName() + " and " + annotatedClass.canonicalName() + ".", annotatedElement);
                return;
            }

            var prevId = tileEntities.putIfAbsent(annotatedClass, regId);
            if (prevId != null && !prevId.equals(regId)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Conflicting TileEntity registration id for " + annotatedClass.canonicalName() + ": '" + prevId + "' vs '" + regId + "'.", annotatedElement);
                return;
            }

            if (isSubtypeErased(annotatedElement, configurableType)) {
                configurableMachines.add(annotatedClass);
            }
            return;
        }

        // Entity
        if (isSubtypeErased(annotatedElement, entityType)) {
            var name = annotation.name().trim();
            if (name.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Entity registration requires a non-empty 'name' parameter.", annotatedElement);
                return;
            }

            var owner = entityNameOwners.putIfAbsent(name, annotatedClass);
            if (owner != null && !owner.equals(annotatedClass)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Duplicate entity registration name '" + name + "' used by " + owner.canonicalName() + " and " + annotatedClass.canonicalName() + ".", annotatedElement);
                return;
            }

            int egg0 = 0;
            int egg1 = 0;
            int[] eggs = annotation.eggColors();
            if (eggs != null && eggs.length >= 2) {
                egg0 = eggs[0];
                egg1 = eggs[1];
            } else if (eggs != null && eggs.length != 0) {
                messager.printMessage(Diagnostic.Kind.ERROR, "eggColors must have length 0 or 2 (primary, secondary).", annotatedElement);
                return;
            }

            var info = new EntityInfo(annotatedClass, name, annotation.trackingRange(), annotation.updateFrequency(), annotation.sendVelocityUpdates(), egg0, egg1);
            var prev = entitiesByName.putIfAbsent(name, info);
            if (prev != null && !prev.type().equals(annotatedClass)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Duplicate entity registration name '" + name + "' already mapped to " + prev.type().canonicalName() + ", cannot also map to " + annotatedClass.canonicalName() + ".", annotatedElement);
            }
            return;
        }

        messager.printMessage(Diagnostic.Kind.ERROR, "Class is not a valid type for @AutoRegister. Must extend Entity, TileEntity, or a valid Renderer class.", annotatedElement);
    }

    private TypeMirror selectOrInferTypeArg(TypeElement annotatedElement, TypeElement targetRaw, TypeMirror fromAnnotation, TypeMirror bound, String boundDisplay, String annoFieldName) {
        TypeMirror chosen;

        if (isObjectType(fromAnnotation)) {
            chosen = inferFirstTypeArg(annotatedElement.asType(), targetRaw);
            if (chosen == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Could not infer " + boundDisplay + " type for renderer. Please specify it manually using '" + annoFieldName + " = ...'.", annotatedElement);
                return null;
            }
        } else {
            chosen = fromAnnotation;
            if (chosen == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Invalid '" + annoFieldName + "' type in @AutoRegister.", annotatedElement);
                return null;
            }
        }

        if (isTypeVarOrWildcard(chosen)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Could not resolve a concrete " + boundDisplay + " type for renderer (generic type variable/wildcard). Please specify '" + annoFieldName + " = ...' explicitly.", annotatedElement);
            return null;
        }

        if (bound != null && !isSubtypeErased(chosen, bound)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Invalid '" + annoFieldName + "' type in @AutoRegister: must be a subtype of " + boundDisplay + ".", annotatedElement);
            return null;
        }

        return chosen;
    }

    private void generateRegistrarFile() throws IOException {
        ClassName ENTITY_REGISTRY = ClassName.get("net.minecraftforge.fml.common.registry", "EntityRegistry");
        ClassName GAME_REGISTRY = ClassName.get("net.minecraftforge.fml.common.registry", "GameRegistry");
        ClassName CLIENT_REGISTRY = ClassName.get("net.minecraftforge.fml.client.registry", "ClientRegistry");
        ClassName RENDERING_REGISTRY = ClassName.get("net.minecraftforge.fml.client.registry", "RenderingRegistry");
        ClassName RESOURCE_LOCATION = ClassName.get("net.minecraft.util", "ResourceLocation");
        ClassName SIDE_ONLY = ClassName.get("net.minecraftforge.fml.relauncher", "SideOnly");
        ClassName SIDE = ClassName.get("net.minecraftforge.fml.relauncher", "Side");
        ClassName REF_STRINGS = ClassName.get("com.hbm", "Tags");
        ClassName MAIN_REGISTRY = ClassName.get("com.hbm.main", "MainRegistry");
        ClassName MOD_ITEMS = ClassName.get("com.hbm.items", "ModItems");
        ClassName ICONFIGURABLE_MACHINE = ClassName.get("com.hbm.tileentity", "IConfigurableMachine");

        var registrarBuilder = TypeSpec.classBuilder("GeneratedHBMRegistrar").addModifiers(Modifier.PUBLIC, Modifier.FINAL).addJavadoc("AUTO-GENERATED FILE. DO NOT MODIFY.");

        if (!configurableMachines.isEmpty()) {
            TypeName classOfConfigurable = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ICONFIGURABLE_MACHINE));
            TypeName listOfConfigurables = ParameterizedTypeName.get(ClassName.get(List.class), classOfConfigurable);
            registrarBuilder.addField(FieldSpec.builder(listOfConfigurables, "CONFIGURABLE_MACHINES").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer("new $T<>($L)", ArrayList.class, configurableMachines.size()).build());

            var block = CodeBlock.builder();
            configurableMachines.stream().sorted(Comparator.comparing(ClassName::canonicalName)).forEach(cn -> block.addStatement("$N.add($T.class)", "CONFIGURABLE_MACHINES", cn));
            registrarBuilder.addStaticBlock(block.build());
        }

        if (!entitiesByName.isEmpty()) {
            var method = MethodSpec.methodBuilder("registerEntities").addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(int.class).addParameter(int.class, "startId").addJavadoc("@param startId The starting ID for entity registration.\n@return The next available entity ID.");

            method.addStatement("int currentId = startId");

            entitiesByName.values().stream().sorted(Comparator.comparing(EntityInfo::name)).forEach(info -> {
                method.addStatement("$T.registerModEntity(new $T($T.MODID, $S), $T.class, $S, currentId++, $T.instance, $L, $L, $L)", ENTITY_REGISTRY, RESOURCE_LOCATION, REF_STRINGS, info.name(), info.type(), info.name(), MAIN_REGISTRY, info.trackingRange(), info.updateFrequency(), info.sendVelocityUpdates());

                if ((info.eggPrimary() | info.eggSecondary()) != 0) {
                    method.addStatement("$T.registerEgg(new $T($T.MODID, $S), $L, $L)", ENTITY_REGISTRY, RESOURCE_LOCATION, REF_STRINGS, info.name(), info.eggPrimary(), info.eggSecondary());
                }
            });

            method.addStatement("return currentId");
            registrarBuilder.addMethod(method.build());
        }

        if (!tileEntities.isEmpty()) {
            var method = MethodSpec.methodBuilder("registerTileEntities").addModifiers(Modifier.PUBLIC, Modifier.STATIC);

            tileEntities.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().canonicalName())).forEach(entry -> method.addStatement("$T.registerTileEntity($T.class, new $T($T.MODID, $S))", GAME_REGISTRY, entry.getKey(), RESOURCE_LOCATION, REF_STRINGS, entry.getValue()));

            registrarBuilder.addMethod(method.build());
        }

        if (!entityRenderersByEntity.isEmpty()) {
            var method = MethodSpec.methodBuilder("registerEntityRenderers").addModifiers(Modifier.PUBLIC, Modifier.STATIC).addAnnotation(AnnotationSpec.builder(SIDE_ONLY).addMember("value", "$T.CLIENT", SIDE).build());

            entityRenderersByEntity.values().stream().sorted(Comparator.comparing(e -> e.entityType().canonicalName())).forEach(info -> {
                if (info.factoryFieldName().isBlank()) {
                    method.addStatement("$T.registerEntityRenderingHandler($T.class, $T::new)", RENDERING_REGISTRY, info.entityType(), info.rendererType());
                } else {
                    method.addStatement("$T.registerEntityRenderingHandler($T.class, $T.$L)", RENDERING_REGISTRY, info.entityType(), info.rendererType(), info.factoryFieldName());
                }
            });

            registrarBuilder.addMethod(method.build());
        }

        if (!tileEntityRenderers.isEmpty()) {
            var method = MethodSpec.methodBuilder("registerTileEntityRenderers").addModifiers(Modifier.PUBLIC, Modifier.STATIC).addAnnotation(AnnotationSpec.builder(SIDE_ONLY).addMember("value", "$T.CLIENT", SIDE).build());

            tileEntityRenderers.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().canonicalName())).forEach(entry -> method.addStatement("$T.bindTileEntitySpecialRenderer($T.class, new $T())", CLIENT_REGISTRY, entry.getKey(), entry.getValue()));

            registrarBuilder.addMethod(method.build());
        }

        if (!itemRenderersByItemField.isEmpty()) {
            var method = MethodSpec.methodBuilder("registerItemRenderers").addModifiers(Modifier.PUBLIC, Modifier.STATIC).addAnnotation(AnnotationSpec.builder(SIDE_ONLY).addMember("value", "$T.CLIENT", SIDE).build());

            itemRenderersByItemField.values().stream().sorted(Comparator.comparing(TeisrInfo::itemFieldName)).forEach(info -> {
                if (info.instanceFieldName().isBlank()) {
                    method.addStatement("$T.$L.setTileEntityItemStackRenderer(new $T($L))", MOD_ITEMS, info.itemFieldName(), info.rendererType(), info.constructorArgs());
                } else {
                    method.addStatement("$T.$L.setTileEntityItemStackRenderer($T.$L)", MOD_ITEMS, info.itemFieldName(), info.rendererType(), info.instanceFieldName());
                }
            });

            registrarBuilder.addMethod(method.build());
        }

        JavaFile.builder("com.hbm.generated", registrarBuilder.build()).addFileComment("AUTO-GENERATED FILE. DO NOT MODIFY.").indent("    ").build().writeTo(filer);
    }

    private TypeMirror typeMirrorOrNull(String fqn) {
        var el = elementUtils.getTypeElement(fqn);
        return el == null ? null : el.asType();
    }

    private boolean isSubtypeErased(TypeElement element, TypeMirror targetType) {
        if (element == null || targetType == null) return false;
        return typeUtils.isSubtype(typeUtils.erasure(element.asType()), typeUtils.erasure(targetType));
    }

    private boolean isSubtypeErased(TypeElement element, TypeElement targetElement) {
        if (element == null || targetElement == null) return false;
        return isSubtypeErased(element, targetElement.asType());
    }

    private boolean isSubtypeErased(TypeMirror source, TypeMirror target) {
        if (source == null || target == null) return false;
        return typeUtils.isSubtype(typeUtils.erasure(source), typeUtils.erasure(target));
    }

    private boolean isObjectType(TypeMirror tm) {
        if (tm == null || objectType == null) return false;
        return typeUtils.isSameType(typeUtils.erasure(tm), typeUtils.erasure(objectType));
    }

    private static boolean isTypeVarOrWildcard(TypeMirror tm) {
        if (tm == null) return false;
        var k = tm.getKind();
        return k == TypeKind.TYPEVAR || k == TypeKind.WILDCARD;
    }

    private TypeMirror typeMirrorFromAnnotation(AutoRegister annotation, MirrorKind kind) {
        try {
            Class<?> c = switch (kind) {
                case ENTITY -> annotation.entity();
                case TILEENTITY -> annotation.tileentity();
            };
            // should be unreachable
            if (c == null) return null;
            var el = elementUtils.getTypeElement(c.getCanonicalName());
            return el == null ? null : el.asType();
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror();
        }
    }

    private TypeMirror inferFirstTypeArg(TypeMirror startingType, TypeElement targetRaw) {
        if (startingType == null || targetRaw == null) return null;

        var queue = new ArrayDeque<TypeMirror>(8);
        var visited = new HashSet<String>(32);

        queue.add(startingType);

        while (!queue.isEmpty()) {
            var current = queue.removeFirst();

            var visitKey = typeUtils.erasure(current).toString() + "|" + current;
            if (!visited.add(visitKey)) continue;

            if (current instanceof DeclaredType declared) {
                var raw = (TypeElement) declared.asElement();
                if (raw.equals(targetRaw)) {
                    var args = declared.getTypeArguments();
                    if (args.isEmpty()) return null;
                    return args.getFirst();
                }
            }

            for (var st : typeUtils.directSupertypes(current)) {
                queue.addLast(st);
            }
        }

        return null;
    }

    private ClassName classNameFromTypeMirror(TypeMirror tm) {
        if (tm == null) return null;
        var erased = typeUtils.erasure(tm);
        if (!(erased instanceof DeclaredType)) return null;
        var el = (TypeElement) typeUtils.asElement(erased);
        return el == null ? null : ClassName.get(el);
    }

    private enum MirrorKind {ENTITY, TILEENTITY}

    private record EntityInfo(ClassName type, String name, int trackingRange, int updateFrequency,
                              boolean sendVelocityUpdates, int eggPrimary, int eggSecondary) {
    }

    private record EntityRendererInfo(ClassName entityType, ClassName rendererType, String factoryFieldName) {
    }

    private record TeisrInfo(ClassName rendererType, String itemFieldName, String constructorArgs,
                             String instanceFieldName) {
    }
}
