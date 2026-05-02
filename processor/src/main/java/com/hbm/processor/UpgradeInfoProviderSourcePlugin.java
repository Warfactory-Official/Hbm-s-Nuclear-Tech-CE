package com.hbm.processor;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;

import java.util.*;

public final class UpgradeInfoProviderSourcePlugin implements Plugin {

    private static final String PLUGIN_NAME = "inject-upgrade-info-provider-source";

    private static final String GUI_INFO_CONTAINER_SIMPLE = "GuiInfoContainer";
    private static final String SOURCE_INTERFACE_SIMPLE = "IUpgradeInfoProviderSource";
    private static final String SOURCE_INTERFACE_FQN = "com.hbm.inventory.gui.IUpgradeInfoProviderSource";
    private static final String PROVIDER_INTERFACE_SIMPLE = "IUpgradeInfoProvider";
    private static final String PROVIDER_INTERFACE_FQN = "com.hbm.tileentity.IUpgradeInfoProvider";
    private static final String ANNOTATION_SIMPLE = "UpgradeInfoProviderField";
    private static final String ANNOTATION_FQN = "com.hbm.interfaces.UpgradeInfoProviderField";
    private static final String INVALID_ANNOTATION_FIELD = "\u0000";

    // can't use List, otherwise it will resolve to com.sun.tools.javac.util.List
    private final ArrayList<JCCompilationUnit> parsedUnits = new ArrayList<>(1024);

    private static Set<String> collectProviderTypeNames(Map<String, ParsedClass> classesBySimpleName) {
        Set<String> providerTypeNames = new HashSet<>(128);
        providerTypeNames.add(PROVIDER_INTERFACE_SIMPLE);

        for (ParsedClass parsedClass : classesBySimpleName.values()) {
            if (parsedClass.directUpgradeProvider()) {
                providerTypeNames.add(parsedClass.simpleName());
            }
        }

        return providerTypeNames;
    }

    private static Plan resolveAnnotatedPlan(ParsedClass parsedClass, Map<String, ParsedClass> classesBySimpleName,
                                             Log log) {
        if (INVALID_ANNOTATION_FIELD.equals(parsedClass.annotatedField())) {
            throw pluginFailure(
                    "Invalid @" + ANNOTATION_SIMPLE + " usage on GUI '" + parsedClass.simpleName()
                            + "'. Use @" + ANNOTATION_SIMPLE + "(\"fieldName\").");
        }

        FieldLookup annotatedField = findFieldInHierarchy(parsedClass, parsedClass.annotatedField(),
                classesBySimpleName, new HashSet<>(8));
        if (!annotatedField.found()) {
            throw pluginFailure(
                    "GUI '" + parsedClass.simpleName() + "' references unknown field '" + parsedClass.annotatedField()
                            + "' in @" + ANNOTATION_SIMPLE + ".");
        }
        if (!annotatedField.accessible()) {
            throw pluginFailure(
                    "GUI '" + parsedClass.simpleName() + "' references inherited private field '" + parsedClass.annotatedField()
                            + "' in @" + ANNOTATION_SIMPLE + ", which the injected method cannot access.");
        }
        return Plan.annotated(parsedClass.annotatedField());
    }

    private static Plan failIfExpected(ParsedClass parsedClass, ArrayList<String> directProviderFields,
                                       Set<String> expectedGuiNames, Log log) {
        if (!expectedGuiNames.contains(parsedClass.simpleName())) {
            return Plan.none();
        }

        if (directProviderFields.isEmpty()) {
            throw pluginFailure(
                    "Could not infer an upgrade info provider source for GUI '" + parsedClass.simpleName()
                            + "'. Add @" + ANNOTATION_SIMPLE + "(\"fieldName\") to disambiguate.");
        } else {
            throw pluginFailure(
                    "Found multiple upgrade info provider candidates in GUI '" + parsedClass.simpleName() + "': "
                            + String.join(", ", directProviderFields)
                            + ". Add @" + ANNOTATION_SIMPLE + "(\"fieldName\") to select one explicitly.");
        }
    }

    private static FieldLookup findFieldInHierarchy(ParsedClass parsedClass, String fieldName,
                                                    Map<String, ParsedClass> classesBySimpleName,
                                                    Set<String> visiting) {
        if (!visiting.add(parsedClass.simpleName())) {
            return FieldLookup.NOT_FOUND;
        }

        for (JCTree definition : parsedClass.classDecl().defs) {
            if (definition instanceof JCVariableDecl variableDecl && fieldName.equals(
                    variableDecl.getName().toString())) {
                return FieldLookup.FOUND;
            }
        }

        ParsedClass superClass = classesBySimpleName.get(parsedClass.superSimpleName());
        if (superClass == null) {
            return FieldLookup.NOT_FOUND;
        }

        for (JCTree definition : superClass.classDecl().defs) {
            if (!(definition instanceof JCVariableDecl variableDecl)) {
                continue;
            }
            if (!fieldName.equals(variableDecl.getName().toString())) {
                continue;
            }
            return (variableDecl.mods.flags & Flags.PRIVATE) == 0 ? FieldLookup.FOUND : FieldLookup.INACCESSIBLE;
        }

        return findFieldInHierarchy(superClass, fieldName, classesBySimpleName, visiting);
    }

    private static JCMethodDecl createProviderMethod(TreeMaker treeMaker, Names names, Plan plan) {
        JCExpression returnType = qualIdent(treeMaker, names, PROVIDER_INTERFACE_FQN);
        JCExpression fieldAccess = treeMaker.Select(treeMaker.Ident(names.fromString("this")),
                names.fromString(plan.fieldName()));
        JCExpression returnExpression = plan.requiresCast() ? treeMaker.TypeCast(returnType, fieldAccess) : fieldAccess;
        JCBlock body = treeMaker.Block(0L, List.of(treeMaker.Return(returnExpression)));

        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), names.fromString("getUpgradeInfoProvider"),
                returnType, List.nil(), List.nil(), List.nil(), body, null);
    }

    private static boolean definesProviderMethod(JCClassDecl classDecl) {
        for (JCTree definition : classDecl.defs) {
            if (definition instanceof JCMethodDecl methodDecl && "getUpgradeInfoProvider".equals(
                    methodDecl.getName().toString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsType(List<JCExpression> expressions, String simpleName, String fqName) {
        for (JCExpression expression : expressions) {
            String current = expression.toString();
            if (current.equals(simpleName) || current.equals(fqName)) {
                return true;
            }
        }
        return false;
    }

    private static JCExpression qualIdent(TreeMaker treeMaker, Names names, String fqn) {
        String[] parts = fqn.split("\\.");
        JCExpression expression = treeMaker.Ident(names.fromString(parts[0]));
        for (int i = 1; i < parts.length; i++) {
            expression = treeMaker.Select(expression, names.fromString(parts[i]));
        }
        return expression;
    }

    private static String simpleName(JCTree tree) {
        return tree == null ? "" : simpleName(tree.toString());
    }

    private static String simpleName(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String value = text;
        int generic = value.indexOf('<');
        if (generic >= 0) {
            value = value.substring(0, generic);
        }
        int array = value.indexOf('[');
        if (array >= 0) {
            value = value.substring(0, array);
        }
        int dot = value.lastIndexOf('.');
        return dot >= 0 ? value.substring(dot + 1) : value;
    }

    private static IllegalStateException pluginFailure(String message) {
        return new IllegalStateException("Upgrade info provider source plugin: " + message);
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public void init(JavacTask task, String... args) {
        BasicJavacTask basicTask = (BasicJavacTask) task;
        Context context = basicTask.getContext();
        TreeMaker treeMaker = TreeMaker.instance(context);
        Names names = Names.instance(context);
        Log log = Log.instance(context);

        task.addTaskListener(new TaskListener() {
            private boolean transformed;

            @Override
            public void started(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.ENTER && !transformed) {
                    transformed = true;
                    transformAll(treeMaker, names, log);
                }
            }

            @Override
            public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.PARSE && e.getCompilationUnit() instanceof JCCompilationUnit compilationUnit) {
                    parsedUnits.add(compilationUnit);
                }
            }
        });
    }

    private void transformAll(TreeMaker treeMaker, Names names, Log log) {
        if (parsedUnits.isEmpty()) {
            return;
        }

        Map<String, ParsedClass> classesBySimpleName = collectClasses();
        Set<String> providerTypeNames = collectProviderTypeNames(classesBySimpleName);
        Set<String> expectedGuiNames = collectExpectedGuiNames(classesBySimpleName);
        Map<String, Boolean> guiSubclassCache = new HashMap<>(classesBySimpleName.size());

        Map<String, Plan> plansByClassName = new HashMap<>(classesBySimpleName.size());
        Set<String> resolving = new HashSet<>(64);

        for (ParsedClass parsedClass : classesBySimpleName.values()) {
            resolvePlan(parsedClass, classesBySimpleName, providerTypeNames, expectedGuiNames, guiSubclassCache,
                    plansByClassName, resolving, log);
        }

        for (ParsedClass parsedClass : classesBySimpleName.values()) {
            Plan plan = plansByClassName.get(parsedClass.simpleName());
            if (plan == null) {
                continue;
            }

            if (plan.injectsMethod() && !parsedClass.implementsSourceInterface()) {
                parsedClass.classDecl().implementing = parsedClass.classDecl().implementing.append(
                        qualIdent(treeMaker, names, SOURCE_INTERFACE_FQN));
            }

            if (plan.injectsMethod() && !parsedClass.definesProviderMethod()) {
                parsedClass.classDecl().defs = parsedClass.classDecl().defs.append(
                        createProviderMethod(treeMaker, names, plan));
            }
        }
    }

    private Map<String, ParsedClass> collectClasses() {
        Map<String, ParsedClass> classesBySimpleName = new LinkedHashMap<>(512);

        for (JCCompilationUnit compilationUnit : parsedUnits) {
            for (JCTree definition : compilationUnit.defs) {
                if (!(definition instanceof JCClassDecl classDecl)) {
                    continue;
                }

                String simpleName = classDecl.getSimpleName().toString();
                String superSimpleName = simpleName(classDecl.extending);

                classesBySimpleName.put(simpleName, new ParsedClass(classDecl, simpleName, superSimpleName,
                        containsType(classDecl.implementing, PROVIDER_INTERFACE_SIMPLE, PROVIDER_INTERFACE_FQN),
                        GUI_INFO_CONTAINER_SIMPLE.equals(superSimpleName),
                        containsType(classDecl.implementing, SOURCE_INTERFACE_SIMPLE, SOURCE_INTERFACE_FQN),
                        definesProviderMethod(classDecl), findAnnotatedField(classDecl)));
            }
        }

        return classesBySimpleName;
    }

    private Set<String> collectExpectedGuiNames(Map<String, ParsedClass> classesBySimpleName) {
        Set<String> expectedGuiNames = new HashSet<>(64);

        for (ParsedClass parsedClass : classesBySimpleName.values()) {
            if (!parsedClass.directUpgradeProvider()) {
                continue;
            }

            for (JCTree definition : parsedClass.classDecl().defs) {
                if (!(definition instanceof JCMethodDecl methodDecl)) {
                    continue;
                }
                if (!"provideGUI".equals(methodDecl.getName().toString()) || methodDecl.body == null) {
                    continue;
                }

                new TreeScanner() {
                    @Override
                    public void visitReturn(JCReturn returnTree) {
                        if (returnTree.expr instanceof JCNewClass newClass) {
                            String guiName = simpleName(newClass.clazz);
                            if (!guiName.isEmpty()) {
                                expectedGuiNames.add(guiName);
                            }
                        }
                        super.visitReturn(returnTree);
                    }
                }.scan(methodDecl.body);
            }
        }

        return expectedGuiNames;
    }

    private Plan resolvePlan(ParsedClass parsedClass, Map<String, ParsedClass> classesBySimpleName,
                             Set<String> providerTypeNames, Set<String> expectedGuiNames,
                             Map<String, Boolean> guiSubclassCache, Map<String, Plan> plansByClassName,
                             Set<String> resolving, Log log) {
        Plan cached = plansByClassName.get(parsedClass.simpleName());
        if (cached != null) {
            return cached;
        }

        if (!resolving.add(parsedClass.simpleName())) {
            return Plan.none();
        }

        Plan resolved;
        if (!isGuiInfoContainerSubclass(parsedClass, classesBySimpleName, guiSubclassCache, new HashSet<>(8))) {
            resolved = Plan.none();
        } else if (parsedClass.definesProviderMethod()) {
            resolved = Plan.inherited();
        } else if (parsedClass.annotatedField() != null) {
            resolved = resolveAnnotatedPlan(parsedClass, classesBySimpleName, log);
        } else {
            ArrayList<String> directProviderFields = getDirectProviderFields(parsedClass.classDecl(),
                    providerTypeNames);
            if (directProviderFields.size() == 1) {
                resolved = Plan.direct(directProviderFields.getFirst());
            } else {
                ParsedClass superClass = classesBySimpleName.get(parsedClass.superSimpleName());
                if (superClass != null) {
                    Plan superPlan = resolvePlan(superClass, classesBySimpleName, providerTypeNames, expectedGuiNames,
                            guiSubclassCache, plansByClassName, resolving, log);
                    if (superPlan.providesSource() || (superClass.implementsSourceInterface() && superClass.definesProviderMethod())) {
                        resolved = Plan.inherited();
                    } else {
                        resolved = failIfExpected(parsedClass, directProviderFields, expectedGuiNames, log);
                    }
                } else {
                    resolved = failIfExpected(parsedClass, directProviderFields, expectedGuiNames, log);
                }
            }
        }

        resolving.remove(parsedClass.simpleName());
        plansByClassName.put(parsedClass.simpleName(), resolved);
        return resolved;
    }

    private boolean isGuiInfoContainerSubclass(ParsedClass parsedClass, Map<String, ParsedClass> classesBySimpleName,
                                               Map<String, Boolean> guiSubclassCache, Set<String> visiting) {
        Boolean cached = guiSubclassCache.get(parsedClass.simpleName());
        if (cached != null) {
            return cached;
        }

        if (parsedClass.directGuiInfoSubclass()) {
            guiSubclassCache.put(parsedClass.simpleName(), true);
            return true;
        }

        if (!visiting.add(parsedClass.simpleName())) {
            return false;
        }

        ParsedClass superClass = classesBySimpleName.get(parsedClass.superSimpleName());
        boolean result = superClass != null && isGuiInfoContainerSubclass(superClass, classesBySimpleName,
                guiSubclassCache, visiting);
        visiting.remove(parsedClass.simpleName());
        guiSubclassCache.put(parsedClass.simpleName(), result);
        return result;
    }

    private ArrayList<String> getDirectProviderFields(JCClassDecl classDecl, Set<String> providerTypeNames) {
        ArrayList<String> fields = new ArrayList<>(2);

        for (JCTree definition : classDecl.defs) {
            if (!(definition instanceof JCVariableDecl variableDecl)) {
                continue;
            }
            if ((variableDecl.mods.flags & Flags.STATIC) != 0) {
                continue;
            }

            String fieldTypeName = simpleName(variableDecl.vartype);
            if (providerTypeNames.contains(fieldTypeName)) {
                fields.add(variableDecl.getName().toString());
            }
        }

        return fields;
    }

    private String findAnnotatedField(JCClassDecl classDecl) {
        for (JCAnnotation annotation : classDecl.mods.annotations) {
            String annotationName = annotation.annotationType.toString();
            if (!annotationName.equals(ANNOTATION_SIMPLE) && !annotationName.equals(ANNOTATION_FQN)) {
                continue;
            }

            if (annotation.args.isEmpty()) {
                return INVALID_ANNOTATION_FIELD;
            }

            JCTree first = annotation.args.getFirst();
            if (first instanceof JCTree.JCLiteral literal && literal.value instanceof String stringValue) {
                return stringValue;
            }

            if (first instanceof JCAssign assign && "value".equals(simpleName(
                    assign.lhs)) && assign.rhs instanceof JCTree.JCLiteral literal && literal.value instanceof String stringValue) {
                return stringValue;
            }

            return INVALID_ANNOTATION_FIELD;
        }

        return null;
    }

    private enum Mode {
        NONE, DIRECT, ANNOTATED, INHERITED
    }

    private enum FieldLookup {
        NOT_FOUND(false, false), INACCESSIBLE(true, false), FOUND(true, true);

        private final boolean found;
        private final boolean accessible;

        FieldLookup(boolean found, boolean accessible) {
            this.found = found;
            this.accessible = accessible;
        }

        boolean found() {
            return found;
        }

        boolean accessible() {
            return accessible;
        }
    }

    private record ParsedClass(JCClassDecl classDecl, String simpleName, String superSimpleName,
                               boolean directUpgradeProvider, boolean directGuiInfoSubclass,
                               boolean implementsSourceInterface, boolean definesProviderMethod,
                               String annotatedField) {
    }

    private record Plan(Mode mode, String fieldName, boolean requiresCast) {
        static Plan none() {
            return new Plan(Mode.NONE, null, false);
        }

        static Plan direct(String fieldName) {
            return new Plan(Mode.DIRECT, fieldName, false);
        }

        static Plan annotated(String fieldName) {
            return new Plan(Mode.ANNOTATED, fieldName, true);
        }

        static Plan inherited() {
            return new Plan(Mode.INHERITED, null, false);
        }

        boolean injectsMethod() {
            return mode == Mode.DIRECT || mode == Mode.ANNOTATED;
        }

        boolean providesSource() {
            return mode != Mode.NONE;
        }
    }
}
