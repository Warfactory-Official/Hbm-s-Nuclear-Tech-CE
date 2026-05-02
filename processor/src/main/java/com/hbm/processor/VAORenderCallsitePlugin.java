package com.hbm.processor;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.resources.CompilerProperties.Errors;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticFlag;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;

/**
 * Rewrites calls to {@code WaveFrontObjectVAO.render*} whose receiver is a recognized
 * {@code static final WaveFrontObjectVAO} field and whose string args are literals:
 * <ul>
 *   <li>{@code renderPart("x")} &rarr; {@code $H.render()} via a synthesized per-class handle.</li>
 *   <li>{@code renderOnly("a","b",...)} &rarr; a synthesized per-class static method that binds
 *       and draws each handle in OBJ order, then unbinds once.</li>
 *   <li>{@code renderAllExcept("x",...)} &rarr; same shape, with all groups minus the excluded set.</li>
 * </ul>
 * Literal group names are validated at compile time against the OBJ file; unknown names fail compilation.
 * Receivers must be referenced as {@code Owner.field}; static-imported VAO fields are not matched.
 */
public final class VAORenderCallsitePlugin implements Plugin {

    private static final String PLUGIN_NAME = "inline-vao-render-callsites";

    private static final String VAO_TYPE_SIMPLE = "WaveFrontObjectVAO";
    private static final String HFR_TYPE_SIMPLE = "HFRWavefrontObject";
    private static final String RL_TYPE_SIMPLE = "ResourceLocation";
    private static final String HANDLE_FQN = "com.hbm.render.loader.GroupHandle";
    private static final String SIDE_ONLY_FQN = "net.minecraftforge.fml.relauncher.SideOnly";
    private static final String SIDE_FQN = "net.minecraftforge.fml.relauncher.Side";
    private static final String FML_COMMON_HANDLER_FQN = "net.minecraftforge.fml.common.FMLCommonHandler";

    private static final String METHOD_RENDER_PART = "renderPart";
    private static final String METHOD_RENDER_ONLY = "renderOnly";
    private static final String METHOD_RENDER_EXCEPT = "renderAllExcept";

    private final ArrayList<JCCompilationUnit> units = new ArrayList<>(1024);
    private final Map<String, LinkedHashMap<String, String>> objPathsByOwner = new HashMap<>(256);
    private final Map<String, LinkedHashSet<String>> groupsByObjPath = new HashMap<>(128);
    private final Map<JCClassDecl, ArrayList<JCVariableDecl>> synthDeclsByHost = new IdentityHashMap<>();

    private Path resourceRoot;

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public void init(JavacTask task, String... args) {
        if (args.length < 1) {
            throw new IllegalStateException(
                    "VAORenderCallsitePlugin: missing resourceRoot arg (expected '-Xplugin:" + PLUGIN_NAME + " <resourceRoot>')");
        }

        String joinedPath = String.join(" ", args).trim();

        if (joinedPath.isEmpty()) {
            throw new IllegalStateException("VAORenderCallsitePlugin: resourceRoot path is empty");
        }

        this.resourceRoot = Paths.get(joinedPath);

        BasicJavacTask basic = (BasicJavacTask) task;
        Context context = basic.getContext();
        TreeMaker treeMaker = TreeMaker.instance(context);
        Names names = Names.instance(context);
        Log log = Log.instance(context);

        task.addTaskListener(new TaskListener() {
            private boolean transformed;

            @Override
            public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.PARSE
                        && e.getCompilationUnit() instanceof JCCompilationUnit compilationUnit) {
                    units.add(compilationUnit);
                }
            }

            @Override
            public void started(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.ENTER && !transformed) {
                    transformed = true;
                    transform(treeMaker, names, log);
                } else if (e.getKind() == TaskEvent.Kind.GENERATE) {
                    promoteSynthFieldsToFinal(e);
                }
            }
        });
    }

    private void transform(TreeMaker m, Names n, Log log) {
        discoverFields();
        for (JCCompilationUnit cu : units) {
            rewriteUnit(cu, m, n, log);
        }
    }

    private void discoverFields() {
        for (JCCompilationUnit cu : units) {
            String packageName = packageNameOf(cu);
            for (JCTree def : cu.defs) {
                if (!(def instanceof JCClassDecl classDecl)) continue;
                collectVaoFields(packageName, "", classDecl);
            }
        }
    }

    private void collectVaoFields(String packageName, String enclosingName, JCClassDecl classDecl) {
        String simpleName = classDecl.name.toString();
        String ownerName = enclosingName.isEmpty() ? qualifyName(packageName, simpleName) : enclosingName + "." + simpleName;
        for (JCTree inner : classDecl.defs) {
            if (inner instanceof JCClassDecl nested) {
                collectVaoFields(packageName, ownerName, nested);
                continue;
            }
            if (!(inner instanceof JCVariableDecl variable)) continue;
            if ((variable.mods.flags & (Flags.STATIC | Flags.FINAL)) != (Flags.STATIC | Flags.FINAL)) continue;
            if (variable.vartype == null) continue;
            if (!VAO_TYPE_SIMPLE.equals(simpleNameOf(variable.vartype.toString()))) continue;
            String objPath = extractObjPath(variable.init);
            if (objPath == null) continue;
            objPathsByOwner.computeIfAbsent(ownerName, ignored -> new LinkedHashMap<>())
                    .put(variable.name.toString(), objPath);
        }
    }

    /** Pattern: {@code new HFRWavefrontObject(new ResourceLocation(MODID, "path")).asVBO()} or {@code .asColoredVAO()}. */
    private static String extractObjPath(JCExpression init) {
        if (!(init instanceof JCMethodInvocation outer)) return null;
        if (!(outer.meth instanceof JCFieldAccess asVboSelect)) return null;
        String factoryName = asVboSelect.name.toString();
        if (!"asVBO".equals(factoryName) && !"asColoredVAO".equals(factoryName)) return null;
        if (!(asVboSelect.selected instanceof JCNewClass newHfr)) return null;
        if (!HFR_TYPE_SIMPLE.equals(simpleNameOf(newHfr.clazz.toString()))) return null;
        if (newHfr.args.isEmpty()) return null;
        JCExpression first = newHfr.args.get(0);
        if (!(first instanceof JCNewClass newRL)) return null;
        if (!RL_TYPE_SIMPLE.equals(simpleNameOf(newRL.clazz.toString()))) return null;
        if (newRL.args.size() < 2) return null;
        JCExpression pathArg = newRL.args.get(1);
        if (pathArg instanceof JCLiteral lit && lit.value instanceof String s) return s.toLowerCase(Locale.ROOT);
        return null;
    }

    private static String simpleNameOf(String typeName) {
        String s = typeName;
        int lt = s.indexOf('<');
        if (lt >= 0) s = s.substring(0, lt);
        int dot = s.lastIndexOf('.');
        return dot >= 0 ? s.substring(dot + 1) : s;
    }

    /** Returns groups in OBJ insertion order (case-sensitive). */
    private LinkedHashSet<String> groupsOf(String objPath) {
        LinkedHashSet<String> cached = groupsByObjPath.get(objPath);
        if (cached != null) return cached;
        LinkedHashSet<String> names = new LinkedHashSet<>();
        Path file = resourceRoot.resolve(objPath);
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                String t = line.trim();
                if (t.length() < 3) continue;
                char c0 = t.charAt(0);
                if ((c0 != 'g' && c0 != 'o') || t.charAt(1) != ' ') continue;
                String name = t.substring(2).trim();
                if (!name.isEmpty()) names.add(name);
            }
        } catch (IOException e) {
            throw pluginFailure("could not read OBJ '" + objPath + "' at '" + file + '\'', e);
        }
        groupsByObjPath.put(objPath, names);
        return names;
    }

    private static boolean containsIgnoreCase(java.util.Collection<String> haystack, String needle) {
        for (String s : haystack) if (s.equalsIgnoreCase(needle)) return true;
        return false;
    }

    private void rewriteUnit(JCCompilationUnit cu, TreeMaker m, Names n, Log log) {
        Map<JCClassDecl, LinkedHashMap<String, SynthField>> fieldsByHost = new IdentityHashMap<>();
        Map<JCClassDecl, LinkedHashMap<String, SynthMethod>> methodsByHost = new IdentityHashMap<>();

        new TreeTranslator() {
            final Deque<JCClassDecl> enclosing = new ArrayDeque<>();

            @Override
            public void visitClassDef(JCClassDecl cd) {
                // Anonymous classes have an empty name; pick the nearest named enclosing class as host
                // so synthesized static members land somewhere that can legally hold them.
                boolean pushed = cd.name != null && cd.name.length() > 0;
                if (pushed) enclosing.push(cd);
                super.visitClassDef(cd);
                if (pushed) enclosing.pop();
            }

            @Override
            public void visitApply(JCMethodInvocation inv) {
                super.visitApply(inv);
                if (enclosing.isEmpty()) return;

                CallInfo info = matchVaoCall(cu, inv);
                if (info == null) return;

                LinkedHashSet<String> groups = groupsOf(info.objPath);
                if (!validateLiteralGroups(log, inv, info, groups)) return;

                JCClassDecl host = enclosing.peek();
                LinkedHashMap<String, SynthField> fields =
                        fieldsByHost.computeIfAbsent(host, k -> new LinkedHashMap<>());

                switch (info.methodName) {
                    case METHOD_RENDER_PART -> {
                        SynthField handle = allocateHandle(fields, info.receiverOwner, info.receiverField,
                                info.literals.get(0));
                        result = m.Apply(
                                List.nil(),
                                m.Select(m.Ident(n.fromString(handle.fieldName)), n.fromString("render")),
                                List.nil());
                    }
                    case METHOD_RENDER_ONLY -> {
                        ArrayList<String> renderGroups = filterIncluded(groups, info.literals);
                        if (renderGroups.isEmpty()) panicEmptyRenderSequence(log, inv, info);
                        String methodName = installSequenceMethod(host, fields, methodsByHost,
                                info.receiverOwner, info.receiverField, "renderOnly", renderGroups);
                        result = m.Apply(List.nil(), m.Ident(n.fromString(methodName)), List.nil());
                    }
                    case METHOD_RENDER_EXCEPT -> {
                        ArrayList<String> renderGroups = filterExcluded(groups, info.literals);
                        if (renderGroups.isEmpty()) panicEmptyRenderSequence(log, inv, info);
                        String methodName = installSequenceMethod(host, fields, methodsByHost,
                                info.receiverOwner, info.receiverField, "renderAllExcept", renderGroups);
                        result = m.Apply(List.nil(), m.Ident(n.fromString(methodName)), List.nil());
                    }
                }
            }
        }.translate(cu);

        // Append synthesized fields + methods to each host class.
        for (var entry : fieldsByHost.entrySet()) {
            JCClassDecl host = entry.getKey();
            ArrayList<JCVariableDecl> tracked = synthDeclsByHost.computeIfAbsent(host, k -> new ArrayList<>());
            for (SynthField sf : entry.getValue().values()) {
                JCVariableDecl decl = buildHandleDecl(m, n, sf);
                tracked.add(decl);
                host.defs = host.defs.append(decl);
            }
            host.defs = host.defs.append(buildClientSideInitBlock(m, n, entry.getValue().values()));
        }
        for (var entry : methodsByHost.entrySet()) {
            JCClassDecl host = entry.getKey();
            for (SynthMethod sm : entry.getValue().values()) {
                host.defs = host.defs.append(buildSequenceMethodDecl(m, n, sm));
            }
        }
    }

    private SynthField allocateHandle(LinkedHashMap<String, SynthField> fields,
                                      String receiverOwner, String receiverField, String literal) {
        String key = receiverOwner + "#" + receiverField + "#" + literal.toLowerCase(java.util.Locale.ROOT);
        SynthField existing = fields.get(key);
        if (existing != null) return existing;
        String base = "$VAO_" + sanitize(receiverField) + "_" + sanitize(literal);
        int suffix = 0;
        String candidate = base;
        while (containsFieldName(fields, candidate)) candidate = base + "_" + (++suffix);
        SynthField created = new SynthField(candidate, receiverOwner, receiverField, literal);
        fields.put(key, created);
        return created;
    }

    private String installSequenceMethod(JCClassDecl host,
                                         LinkedHashMap<String, SynthField> fields,
                                         Map<JCClassDecl, LinkedHashMap<String, SynthMethod>> methodsByHost,
                                         String receiverOwner, String receiverField,
                                         String baseName, ArrayList<String> renderGroups) {
        LinkedHashMap<String, SynthMethod> methods =
                methodsByHost.computeIfAbsent(host, k -> new LinkedHashMap<>());

        StringBuilder keyJoiner = new StringBuilder(receiverOwner).append('#').append(receiverField).append('#').append(baseName);
        for (String g : renderGroups) keyJoiner.append('|').append(g.toLowerCase(java.util.Locale.ROOT));
        String key = keyJoiner.toString();

        SynthMethod existing = methods.get(key);
        if (existing != null) return existing.methodName;

        // Ensure a handle exists for each referenced group.
        ArrayList<String> handleFieldNames = new ArrayList<>(renderGroups.size());
        for (String g : renderGroups) {
            handleFieldNames.add(allocateHandle(fields, receiverOwner, receiverField, g).fieldName);
        }

        String methodBase = "$" + baseName + "_" + sanitize(receiverField) + "_"
                + String.format("%08x", key.hashCode());
        int suffix = 0;
        String candidate = methodBase;
        while (containsMethodName(methods, candidate)) candidate = methodBase + "_" + (++suffix);

        SynthMethod created = new SynthMethod(candidate, handleFieldNames);
        methods.put(key, created);
        return created.methodName;
    }

    private static ArrayList<String> filterIncluded(LinkedHashSet<String> groups, java.util.List<String> literals) {
        ArrayList<String> out = new ArrayList<>(Math.min(groups.size(), literals.size()));
        for (String g : groups) if (containsIgnoreCase(literals, g)) out.add(g);
        return out;
    }

    private static ArrayList<String> filterExcluded(LinkedHashSet<String> groups, java.util.List<String> literals) {
        ArrayList<String> out = new ArrayList<>(groups.size());
        for (String g : groups) if (!containsIgnoreCase(literals, g)) out.add(g);
        return out;
    }

    private static boolean validateLiteralGroups(Log log, JCMethodInvocation inv, CallInfo info,
                                                 LinkedHashSet<String> groups) {
        for (String literal : info.literals) {
            if (!containsIgnoreCase(groups, literal)) {
                log.error(DiagnosticFlag.API, inv.pos(), Errors.ProcMessager(
                        "[" + PLUGIN_NAME + "] group '" + literal + "' not found in '" + info.objPath
                                + "' (referenced via " + info.receiverOwner + "." + info.receiverField
                                + "." + info.methodName + ")"));
                return false;
            }
        }
        return true;
    }

    private static void panicEmptyRenderSequence(Log log, JCMethodInvocation inv, CallInfo info) {
        String message = "[" + PLUGIN_NAME + "] " + info.receiverOwner + "." + info.receiverField + "."
                + info.methodName + " resolves to an empty render sequence for '" + info.objPath + "'";
        log.error(DiagnosticFlag.API, inv.pos(), Errors.ProcMessager(message));
        throw pluginFailure(message);
    }

    private static boolean containsFieldName(Map<String, SynthField> m, String fieldName) {
        for (SynthField sf : m.values()) if (sf.fieldName.equals(fieldName)) return true;
        return false;
    }

    private static boolean containsMethodName(Map<String, SynthMethod> m, String methodName) {
        for (SynthMethod sm : m.values()) if (sm.methodName.equals(methodName)) return true;
        return false;
    }

    private CallInfo matchVaoCall(JCCompilationUnit cu, JCMethodInvocation inv) {
        if (!(inv.meth instanceof JCFieldAccess methSel)) return null;
        String methodName = methSel.name.toString();
        if (!METHOD_RENDER_PART.equals(methodName)
                && !METHOD_RENDER_ONLY.equals(methodName)
                && !METHOD_RENDER_EXCEPT.equals(methodName)) return null;

        // Receiver pattern: Owner.field, where Owner may be simple, nested, imported, or fully qualified.
        if (!(methSel.selected instanceof JCFieldAccess fa)) return null;
        String receiverOwner = resolveOwnerName(cu, selectChain(fa.selected));
        if (receiverOwner == null) return null;
        String receiverField = fa.name.toString();
        LinkedHashMap<String, String> fields = objPathsByOwner.get(receiverOwner);
        if (fields == null) return null;
        String objPath = fields.get(receiverField);
        if (objPath == null) return null;

        if (METHOD_RENDER_PART.equals(methodName) && inv.args.size() != 1) return null;
        if (inv.args.isEmpty()) return null;
        ArrayList<String> literals = new ArrayList<>(inv.args.size());
        for (JCExpression arg : inv.args) {
            if (!(arg instanceof JCLiteral lit) || !(lit.value instanceof String s)) return null;
            literals.add(s);
        }
        return new CallInfo(methodName, receiverOwner, receiverField, objPath, literals);
    }

    private String resolveOwnerName(JCCompilationUnit cu, String ownerText) {
        if (ownerText == null || ownerText.isEmpty()) return null;
        if (objPathsByOwner.containsKey(ownerText)) return ownerText;

        String packageName = packageNameOf(cu);
        String samePackageCandidate = qualifyName(packageName, ownerText);
        if (objPathsByOwner.containsKey(samePackageCandidate)) return samePackageCandidate;

        int firstDot = ownerText.indexOf('.');
        String firstSegment = firstDot >= 0 ? ownerText.substring(0, firstDot) : ownerText;
        String suffix = firstDot >= 0 ? ownerText.substring(firstDot) : "";

        String singleImportCandidate = null;
        LinkedHashSet<String> starImportCandidates = new LinkedHashSet<>();
        for (JCTree def : cu.defs) {
            if (!(def instanceof JCImport importDecl) || importDecl.staticImport) continue;
            if (!(importDecl.qualid instanceof JCExpression importedQualid)) continue;
            String importedName = selectChain(importedQualid);
            if (importedName == null) continue;

            if (importedName.endsWith(".*")) {
                String candidate = importedName.substring(0, importedName.length() - 2) + "." + ownerText;
                if (objPathsByOwner.containsKey(candidate)) {
                    starImportCandidates.add(candidate);
                }
                continue;
            }

            int importedLastDot = importedName.lastIndexOf('.');
            String importedSimpleName = importedLastDot >= 0 ? importedName.substring(importedLastDot + 1) : importedName;
            if (!importedSimpleName.equals(firstSegment)) continue;

            String candidate = importedName + suffix;
            if (!objPathsByOwner.containsKey(candidate)) continue;
            if (singleImportCandidate != null && !singleImportCandidate.equals(candidate)) {
                throw pluginFailure("ambiguous imported owner '" + ownerText + "' while resolving VAO receiver");
            }
            singleImportCandidate = candidate;
        }

        if (singleImportCandidate != null) return singleImportCandidate;
        if (starImportCandidates.isEmpty()) return null;
        if (starImportCandidates.size() == 1) return starImportCandidates.iterator().next();
        throw pluginFailure("ambiguous star-import owner '" + ownerText + "' while resolving VAO receiver");
    }

    /**
     * {@code @SideOnly(Side.CLIENT) private static GroupHandle $VAO_field_group;}
     * <p>
     * Declared without an initializer so {@code <clinit>} contains no {@code PUTSTATIC} to this
     * field outside the client-guarded block built by {@link #buildClientSideInitBlock}; Forge
     * strips {@code @SideOnly(Side.CLIENT)} members on the dedicated server, and an unguarded
     * {@code PUTSTATIC} to a stripped field would throw {@link NoSuchFieldError} during class
     * init.
     * <p>
     * {@link Flags#FINAL} is intentionally absent from the tree modifiers so javac's Flow pass
     * does not enforce blank-final definite-assignment on the conditional write. The flag is
     * OR'd onto the {@code VarSymbol} before code generation by {@link #promoteSynthFieldsToFinal},
     * so the emitted classfile still carries {@code ACC_FINAL} and lets the JIT constant-fold
     * reads once {@code <clinit>} has completed.
     */
    private JCVariableDecl buildHandleDecl(TreeMaker m, Names n, SynthField sf) {
        JCExpression handleType = qualIdent(m, n, HANDLE_FQN);
        JCAnnotation sideOnly = m.Annotation(
                qualIdent(m, n, SIDE_ONLY_FQN),
                List.of(m.Select(qualIdent(m, n, SIDE_FQN), n.fromString("CLIENT"))));
        JCModifiers mods = m.Modifiers(Flags.PRIVATE | Flags.STATIC, List.of(sideOnly));
        return m.VarDef(mods, n.fromString(sf.fieldName), handleType, null);
    }

    /**
     * Stamps {@link Flags#FINAL} onto each tracked synth field's {@code VarSymbol} right before
     * its declaring class goes through code generation. At this point Flow has already accepted
     * the non-final declaration (so blank-final DA never fired), but {@code Gen} has not yet
     * read the symbol's flags into the classfile, so the ACC_FINAL bit still lands in bytecode.
     */
    private void promoteSynthFieldsToFinal(TaskEvent e) {
        Object target = e.getTypeElement();
        if (target == null) return;
        for (var entry : synthDeclsByHost.entrySet()) {
            if (entry.getKey().sym != target) continue;
            for (JCVariableDecl decl : entry.getValue()) {
                if (decl.sym != null) decl.sym.flags_field |= Flags.FINAL;
            }
            return;
        }
    }

    /**
     * <pre>
     * static {
     *     if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
     *         $VAO_foo = Owner.field.resolve("foo");
     *         ...
     *     }
     * }
     * </pre>
     * The guarded {@code PUTSTATIC}s never execute on the dedicated server, so field stripping
     * does not trigger {@link NoSuchFieldError}.
     */
    private JCBlock buildClientSideInitBlock(TreeMaker m, Names n, Iterable<SynthField> fields) {
        JCExpression fmlInstance = m.Apply(
                List.nil(),
                m.Select(qualIdent(m, n, FML_COMMON_HANDLER_FQN), n.fromString("instance")),
                List.nil());
        JCExpression currentSide = m.Apply(
                List.nil(),
                m.Select(fmlInstance, n.fromString("getSide")),
                List.nil());
        JCExpression sideClient = m.Select(qualIdent(m, n, SIDE_FQN), n.fromString("CLIENT"));
        JCExpression isClient = m.Binary(JCTree.Tag.EQ, currentSide, sideClient);

        ListBuffer<JCStatement> thenStmts = new ListBuffer<>();
        for (SynthField sf : fields) {
            JCExpression receiver = m.Select(qualIdent(m, n, sf.receiverOwner), n.fromString(sf.receiverField));
            JCExpression resolveCall = m.Apply(
                    List.nil(),
                    m.Select(receiver, n.fromString("resolve")),
                    List.of(m.Literal(sf.literal)));
            thenStmts.append(m.Exec(m.Assign(m.Ident(n.fromString(sf.fieldName)), resolveCall)));
        }
        JCBlock thenBlock = m.Block(0L, thenStmts.toList());
        JCStatement ifStmt = m.If(isClient, thenBlock, null);
        return m.Block(Flags.STATIC, List.of(ifStmt));
    }

    /**
     * <pre>
     * private static void $methodName() {
     *     $H0.bindAndDraw();
     *     $H1.bindAndDraw();
     *     ...
     *     GroupHandle.unbind();
     * }
     * </pre>
     */
    private JCMethodDecl buildSequenceMethodDecl(TreeMaker m, Names n, SynthMethod sm) {
        ListBuffer<JCStatement> stmts = new ListBuffer<>();
        for (String handleField : sm.handleFieldNames) {
            stmts.append(m.Exec(m.Apply(
                    List.nil(),
                    m.Select(m.Ident(n.fromString(handleField)), n.fromString("bindAndDraw")),
                    List.nil())));
        }
        stmts.append(m.Exec(m.Apply(
                List.nil(),
                m.Select(qualIdent(m, n, HANDLE_FQN), n.fromString("unbind")),
                List.nil())));

        JCBlock body = m.Block(0L, stmts.toList());
        JCModifiers mods = m.Modifiers(Flags.PRIVATE | Flags.STATIC);
        JCExpression voidType = m.TypeIdent(TypeTag.VOID);
        return m.MethodDef(mods, n.fromString(sm.methodName), voidType,
                List.nil(), List.nil(), List.nil(), body, null);
    }

    private static String selectChain(JCExpression expr) {
        if (expr instanceof JCIdent ident) return ident.name.toString();
        if (!(expr instanceof JCFieldAccess access)) return null;
        String selected = selectChain(access.selected);
        if (selected == null) return null;
        return selected + "." + access.name;
    }

    private static String packageNameOf(JCCompilationUnit cu) {
        var packageName = cu.getPackageName();
        return packageName == null ? "" : packageName.toString();
    }

    private static String qualifyName(String packageName, String simpleName) {
        return packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
    }

    private static JCExpression qualIdent(TreeMaker m, Names n, String fqn) {
        String[] parts = fqn.split("\\.");
        JCExpression expr = m.Ident(n.fromString(parts[0]));
        for (int i = 1; i < parts.length; i++) expr = m.Select(expr, n.fromString(parts[i]));
        return expr;
    }

    private static String sanitize(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sb.append(Character.isJavaIdentifierPart(c) ? c : '_');
        }
        return sb.toString();
    }

    private static IllegalStateException pluginFailure(String message) {
        return new IllegalStateException("VAORenderCallsitePlugin: " + message);
    }

    private static IllegalStateException pluginFailure(String message, Throwable cause) {
        return new IllegalStateException("VAORenderCallsitePlugin: " + message, cause);
    }

    private record CallInfo(String methodName, String receiverOwner, String receiverField,
                            String objPath, ArrayList<String> literals) {}

    private record SynthField(String fieldName, String receiverOwner, String receiverField, String literal) {}

    private record SynthMethod(String methodName, ArrayList<String> handleFieldNames) {}
}
